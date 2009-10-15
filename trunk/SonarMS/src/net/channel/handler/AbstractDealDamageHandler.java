package net.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleJob;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {
    public static class AttackInfo {
        public int numAttacked,  numDamage,  numAttackedAndDamage;
        public int skill,  stance,  direction,  charge;
        public List<Pair<Integer, List<Integer>>> allDamage;
        public boolean isHH = false;
        public int speed = 4;

        private MapleStatEffect getAttackEffect(MapleCharacter chr, ISkill theSkill) {
            ISkill mySkill = theSkill;
            if (mySkill == null)
                mySkill = SkillFactory.getSkill(skill);
            int skillLevel = chr.getSkillLevel(mySkill);
            if (skillLevel == 0)
                return null;
            return mySkill.getEffect(skillLevel);
        }

        public MapleStatEffect getAttackEffect(MapleCharacter chr) {
            return getAttackEffect(chr, null);
        }
    }

    protected synchronized void applyAttack(AttackInfo attack, MapleCharacter player, int maxDamagePerMonster, int attackCount) {
        player.getCheatTracker().checkAttack(attack.skill);
        ISkill theSkill = null;
        MapleStatEffect attackEffect = null;
        if (attack.skill != 0) {
            theSkill = SkillFactory.getSkill(attack.skill);
            attackEffect = attack.getAttackEffect(player, theSkill);
            if (attackEffect == null) {
                player.getClient().getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (attack.skill != 2301002)
                if (player.isAlive())
                    attackEffect.applyTo(player);
                else
                    player.getClient().getSession().write(MaplePacketCreator.enableActions());
        }
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        if (attackCount != attack.numDamage && attack.skill != 4211006) {
            player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT, attack.numDamage + "/" + attackCount);
            return;
        }
        int totDamage = 0;
        final MapleMap map = player.getMap();
        if (attack.skill == 4211006) {
            int delay = 0;
            for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
                MapleMapObject mapobject = map.getMapObject(oned.getLeft().intValue());
                if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    if (mapitem.getMeso() > 9)
                        synchronized (mapitem) {
                            if (mapitem.isPickedUp())
                                return;
                            TimerManager.getInstance().schedule(new Runnable() {
                                public void run() {
                                    map.removeMapObject(mapitem);
                                    map.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem.getPosition());
                                    mapitem.setPickedUp(true);
                                }
                            }, delay);
                            delay += 100;
                        }
                    else if (mapitem.getMeso() == 0) {
                        player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                        return;
                    }
                } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                    player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return;
                }
            }
        }
        for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
            MapleMonster monster = map.getMonsterByOid(oned.getLeft().intValue());
            if (monster != null) {
                int totDamageToOneMonster = 0;
                for (Integer eachd : oned.getRight())
                    totDamageToOneMonster += eachd.intValue();
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                Point playerPos = player.getPosition();
                if (totDamageToOneMonster > attack.numDamage + 1) {
                    int dmgCheck = player.getCheatTracker().checkDamage(totDamageToOneMonster);
                    if (dmgCheck > 5 && totDamageToOneMonster < 99999)
                        player.getCheatTracker().registerOffense(CheatingOffense.SAME_DAMAGE, dmgCheck + " times: " + totDamageToOneMonster);
                }
                double distance = playerPos.distanceSq(monster.getPosition());
                if (distance > 400000.0)
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, Double.toString(Math.sqrt(distance)));
                player.checkMonsterAggro(monster);
                if (attack.skill == 2301002 && !monster.getUndead()) {
                    player.getCheatTracker().registerOffense(CheatingOffense.HEAL_ATTACKING_UNDEAD);
                    return;
                }
                if ((attack.skill == 0 || attack.skill == 4001334 || attack.skill == 4201005 || attack.skill == 4211002 || attack.skill == 4211004 || attack.skill == 4221001 || attack.skill == 4221003 || attack.skill == 4221007) && player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null)
                    handlePickPocket(player, monster, oned);
                if (attack.skill == 3221007) //snipe
                    totDamageToOneMonster = 95000 + (int) Math.random() * 4999;
                if (attack.skill == 4101005) { // drain
                    ISkill drain = SkillFactory.getSkill(4101005);
                    int gainhp = (int) ((double) totDamageToOneMonster * (double) drain.getEffect(player.getSkillLevel(drain)).getX() / 100.0);
                    gainhp = Math.min(monster.getMaxHp(), Math.min(gainhp, player.getMaxHp() / 2));
                    player.addHP(gainhp);
                }
                if (attack.skill == 5111004) { // energy drain
                    int gainhpE = (int) ((double) totDamage * (double) SkillFactory.getSkill(5111004).getEffect(player.getSkillLevel(SkillFactory.getSkill(5111004))).getX() / 100.0);
                    gainhpE = Math.min(monster.getMaxHp(), Math.min(gainhpE, player.getMaxHp() / 2));
                    player.addHP(gainhpE);
                }
                if (attack.skill == 4201004)
                    handleSteal(player, monster);
                if (attack.skill == 2121003) {// Fire Demon, Ice Effectiveness
                    MapleStatEffect effect = SkillFactory.getSkill(2121003).getEffect(player.getSkillLevel(SkillFactory.getSkill(2121003)));
                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, effect.getDuration() * 1000);
                }
                if (attack.skill == 2221003) { // Ice Demon, Fire Effectiveness
                    MapleStatEffect effect = SkillFactory.getSkill(2121003).getEffect(player.getSkillLevel(SkillFactory.getSkill(2121003)));
                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, effect.getDuration() * 1000);
                }
                if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                    ISkill hamstring = SkillFactory.getSkill(3121007);
                    if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, false);
                        monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                    }
                }
                if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                    ISkill blind = SkillFactory.getSkill(3221006);
                    if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, false);
                        monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                    }
                }
                if (player.getJob().isA(MapleJob.WHITEKNIGHT)) {
                    int[] charges = new int[]{1211005, 1211006};
                    for (int charge : charges) {
                        ISkill chargeSkill = SkillFactory.getSkill(charge);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {
                            final ElementalEffectiveness iceEffectiveness = monster.getEffectiveness(Element.ICE);
                            if (totDamageToOneMonster > 0 && iceEffectiveness == ElementalEffectiveness.NORMAL || iceEffectiveness == ElementalEffectiveness.WEAK) {
                                MapleStatEffect chargeEffect = chargeSkill.getEffect(player.getSkillLevel(chargeSkill));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), chargeSkill, false);
                                monster.applyStatus(player, monsterStatusEffect, false, chargeEffect.getY() * 2000);
                            }
                            break;
                        }
                    }
                }
                ISkill venomNL = SkillFactory.getSkill(4120005);
                ISkill venomShadower = SkillFactory.getSkill(4220005);
                if (player.getSkillLevel(venomNL) > 0) {
                    MapleStatEffect venomEffect = venomNL.getEffect(player.getSkillLevel(venomNL));
                    for (int i = 0; i < attackCount; i++)
                        if (venomEffect.makeChanceResult())
                            if (monster.getVenomMulti() < 3) {
                                monster.setVenomMulti((monster.getVenomMulti() + 1));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), venomNL, false);
                                monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                            }
                } else if (player.getSkillLevel(venomShadower) > 0) {
                    MapleStatEffect venomEffect = venomShadower.getEffect(player.getSkillLevel(venomShadower));
                    for (int i = 0; i < attackCount; i++)
                        if (venomEffect.makeChanceResult())
                            if (monster.getVenomMulti() < 3) {
                                monster.setVenomMulti((monster.getVenomMulti() + 1));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), venomShadower, false);
                                monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                            }
                }
                if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0)
                    if (attackEffect.makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, false);
                        monster.applyStatus(player, monsterStatusEffect, attackEffect.isPoison(), attackEffect.getDuration());
                    }
                if (attack.isHH && !monster.isBoss())
                    map.damageMonster(player, monster, monster.getHp() - 1);
                else if (attack.isHH && monster.isBoss()) {
                    int HHDmg = (int) (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(1221011).getEffect(player.getSkillLevel(SkillFactory.getSkill(1221011))).getDamage() / 100));
                    int sanctuaryDamage = (int) (Math.floor(Math.random() * (HHDmg - HHDmg * .80) + HHDmg * .80));
                    map.damageMonster(player, monster, sanctuaryDamage);
                } else
                    map.damageMonster(player, monster, totDamageToOneMonster);
            }
        }
        if (totDamage > 1) {
            player.getCheatTracker().setAttacksWithoutHit(player.getCheatTracker().getAttacksWithoutHit() + 1);
            final int offenseLimit;
            switch (attack.skill) {
                case 3121004: // Hurricane
                case 5221004: // Rapidfire
                    offenseLimit = 300;
                    break;
                default:
                    offenseLimit = 100;
                    break;
            }
            if (player.getCheatTracker().getAttacksWithoutHit() > offenseLimit)
                player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(player.getCheatTracker().getAttacksWithoutHit()));
        }
    }

    private void handlePickPocket(MapleCharacter player, MapleMonster monster, Pair<Integer, List<Integer>> oned) {
        ISkill pickpocket = SkillFactory.getSkill(4211003);
        int delay = 0;
        int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
        int reqdamage = 20000;
        Point monsterPosition = monster.getPosition();
        for (Integer eachd : oned.getRight())
            if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                double perc = (double) eachd / (double) reqdamage;
                final int todrop = Math.min((int) Math.max(perc * (double) maxmeso, (double) 1), maxmeso);
                final MapleMap tdmap = player.getMap();
                final Point tdpos = new Point((int) (monsterPosition.getX() + (Math.random() * 100) - 50), (int) (monsterPosition.getY()));
                final MapleMonster tdmob = monster;
                final MapleCharacter tdchar = player;
                TimerManager.getInstance().schedule(new Runnable() {
                    public void run() {
                        tdmap.spawnMesoDrop(todrop, todrop, tdpos, tdmob, tdchar, false);
                    }
                }, delay);

                delay += 100;
            }
    }

    public AttackInfo parseDamage(LittleEndianAccessor lea, boolean ranged) {
        AttackInfo ret = new AttackInfo();
        lea.readByte();
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new ArrayList<Pair<Integer, List<Integer>>>();
        ret.skill = lea.readInt();
        if (ret.skill == 2121001 || ret.skill == 2221001 || ret.skill == 2321001 || ret.skill == 5201002 || ret.skill == 5101004)
            ret.charge = lea.readInt();
        else
            ret.charge = 0;
        if (ret.skill == 1221011)
            ret.isHH = true;
        lea.readByte();
        ret.stance = lea.readByte();
        if (ret.skill == 4211006)
            return parseMesoExplosion(lea, ret);
        if (ranged) {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.readByte();
            ret.direction = lea.readByte();
            lea.skip(7);
            if (ret.skill == 3121004 || ret.skill == 3221001 || ret.skill == 5221004)
                lea.skip(4);
        } else {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.skip(4);
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(14);
            List<Integer> allDamageNumbers = new ArrayList<Integer>();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (ret.skill == 3221007)
                    damage += 0x80000000;
                allDamageNumbers.add(Integer.valueOf(damage));
            }
            if (ret.skill != 5221004)
                lea.skip(4);
            ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(oid), allDamageNumbers));
        }
        return ret;
    }

    public AttackInfo parseMesoExplosion(LittleEndianAccessor lea, AttackInfo ret) {
        if (ret.numAttackedAndDamage == 0) {
            lea.skip(10);
            int bullets = lea.readByte();
            for (int j = 0; j < bullets; j++) {
                int mesoid = lea.readInt();
                lea.skip(1);
                ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(mesoid), null));
            }
            return ret;
        } else
            lea.skip(6);
        for (int i = 0; i < ret.numAttacked + 1; i++) {
            int oid = lea.readInt();
            if (i < ret.numAttacked) {
                lea.skip(12);
                int bullets = lea.readByte();
                List<Integer> allDamageNumbers = new ArrayList<Integer>();
                for (int j = 0; j < bullets; j++) {
                    int damage = lea.readInt();
                    allDamageNumbers.add(Integer.valueOf(damage));
                }
                ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(oid), allDamageNumbers));
                lea.skip(4);
            } else {
                int bullets = lea.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = lea.readInt();
                    lea.skip(1);
                    ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(mesoid), null));
                }
            }
        }
        return ret;
    }

    private void handleSteal(MapleCharacter chr, MapleMonster mob) {
        ISkill steal = SkillFactory.getSkill(4201004);
        if (steal.getEffect(chr.getSkillLevel(steal)).makeChanceResult()) {
            int toSteal = mob.getDrop();
            MapleInventoryManipulator.addById(chr.getClient(), toSteal, (short) 1, "");
            mob.addStolen(toSteal);
        }
    }
}
