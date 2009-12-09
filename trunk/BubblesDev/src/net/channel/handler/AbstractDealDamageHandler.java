/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import tools.Randomizer;
import constants.SkillConstants;
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
        public int numAttacked, numDamage, numAttackedAndDamage;
        public int skill, stance, direction, charge, display;
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
        if (!player.isAlive())
            return;
        if (attackCount != attack.numDamage && attack.skill != SkillConstants.ChiefBandit.MesoExplosion && attack.skill != SkillConstants.NightWalker.Vampire && attack.skill != SkillConstants.WindArcher.WindShot)
            return;
        int totDamage = 0;
        final MapleMap map = player.getMap();
        if (attack.skill == SkillConstants.ChiefBandit.MesoExplosion) {
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
                    else if (mapitem.getMeso() == 0)
                        return;
                } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER)
                    return;
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
                if ((attack.skill == 0 || attack.skill == 4001334 || attack.skill == 4201005 || attack.skill == 4211002 || attack.skill == 4211004 || attack.skill == 4221001 || attack.skill == 4221003 || attack.skill == 4221007) && player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null)
                    handlePickPocket(player, monster, oned);
                else if (attack.skill == SkillConstants.Marksman.Snipe)
                    totDamageToOneMonster = 195000 + Randomizer.getInstance().nextInt(5000);
                else if (attack.skill == SkillConstants.Marauder.EnergyDrain || attack.skill == SkillConstants.ThunderBreaker.EnergyDrain || attack.skill == SkillConstants.NightWalker.Vampire || attack.skill == SkillConstants.Assassin.Drain)
                    player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
                else if (attack.skill == SkillConstants.Assassin.Steal) {
                    ISkill steal = SkillFactory.getSkill(SkillConstants.Assassin.Steal);
                    if (steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) {
                        int toSteal = monster.getDrop();
                        MapleInventoryManipulator.addById(player.getClient(), toSteal, (short) 1, "");
                        monster.addStolen(toSteal);
                    }
                } else if (attack.skill == SkillConstants.FPArchMage.FireDemon)// Fire Demon, Ice Effectiveness
                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(SkillConstants.FPArchMage.FireDemon).getEffect(player.getSkillLevel(SkillFactory.getSkill(SkillConstants.FPArchMage.FireDemon))).getDuration() * 1000);
                else if (attack.skill == SkillConstants.ILArchMage.IceDemon) // Ice Demon, Fire Effectiveness
                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(SkillConstants.ILArchMage.IceDemon).getEffect(player.getSkillLevel(SkillFactory.getSkill(SkillConstants.ILArchMage.IceDemon))).getDuration() * 1000);
                else if (attack.skill == SkillConstants.Outlaw.HomingBeacon || attack.skill == SkillConstants.Corsair.Bullseye)
                    player.setMarkedMonster(monster.getObjectId());
                if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                    ISkill hamstring = SkillFactory.getSkill(SkillConstants.Bowmaster.Hamstring);
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
                int id = 0;
                id = player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005);
                ISkill type = SkillFactory.getSkill(id);
                if (player.getSkillLevel(type) > 0 && (player.getJob().equals(MapleJob.NIGHTLORD) || player.getJob().equals(MapleJob.SHADOWER) || player.getJob().isA(MapleJob.NIGHTWALKER3))) {
                    MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                    for (int i = 0; i < attackCount; i++)
                        if (venomEffect.makeChanceResult())
                            if (monster.getVenomMulti() < 3) {
                                monster.setVenomMulti((monster.getVenomMulti() + 1));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, false);
                                monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                            }
                }
                if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0)
                    if (attackEffect.makeChanceResult())
                        monster.applyStatus(player, new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, false), attackEffect.isPoison(), attackEffect.getDuration());
                if (attack.isHH && !monster.isBoss())
                    map.damageMonster(player, monster, monster.getHp() - 1);
                else if (attack.isHH && monster.isBoss()) {
                    int HHDmg = (int) (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(1221011).getEffect(player.getSkillLevel(SkillFactory.getSkill(1221011))).getDamage() / 100));
                    map.damageMonster(player, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)));
                } else
                    map.damageMonster(player, monster, totDamageToOneMonster);
            }
        }
    }

    private void handlePickPocket(MapleCharacter player, MapleMonster monster, Pair<Integer, List<Integer>> oned) {
        ISkill pickpocket = SkillFactory.getSkill(4211003);
        int delay = 0;
        int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
        Point monsterPosition = monster.getPosition();
        for (Integer eachd : oned.getRight())
            if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                final int todrop = Math.min((int) Math.max(((double) eachd / (double) 200) * (double) maxmeso, (double) 1), maxmeso);
                final MapleMap tdmap = player.getMap();
                final Point tdpos = new Point((int) (monsterPosition.getX() + Randomizer.getInstance().nextInt(101) - 50), (int) (monsterPosition.getY()));
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
        if (ret.skill == 2121001 || ret.skill == 2221001 || ret.skill == 2321001 || ret.skill == 5201002 || ret.skill == 5101004 || ret.skill == 15101003 || ret.skill == 14111006)
            ret.charge = lea.readInt();
        else
            ret.charge = 0;
        if (ret.skill == 1221011)
            ret.isHH = true;
        lea.readLong();
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
            if (ret.skill == 3121004 || ret.skill == 3221001 || ret.skill == 5221004 || ret.skill == 13111002)//hurricanes
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
}
