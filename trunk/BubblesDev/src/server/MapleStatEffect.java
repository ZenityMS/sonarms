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
package server;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleMount;
import client.MapleStat;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.InventoryConstants;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import net.world.PlayerCoolDownValueHolder;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import constants.SkillConstants;
import net.MaplePacket;

/**
 * @author Matze
 * @author Frz
 */
public class MapleStatEffect implements Serializable {
    private static final long serialVersionUID = 3692756402846632237L;
    private short watk, matk, wdef, mdef, acc, avoid, hands, speed, jump;
    private short hp, mp;
    private double hpR, mpR;
    private short mpCon, hpCon;
    private int duration;
    private boolean overTime;
    private int sourceid;
    private int moveTo;
    private boolean skill;
    private List<Pair<MapleBuffStat, Integer>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, bulletCount, bulletConsume;
    private Point lt, rb;
    private int mobCount;
    private int moneyCon;
    private int cooldown;
    private int morphId = 0;
    private boolean isGhost;
    private int fatigue;

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime) {
        return loadFromData(source, skillid, true, overtime);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, false, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, Integer>> list, MapleBuffStat buffstat, Integer val) {
        if (val.intValue() != 0)
            list.add(new Pair<MapleBuffStat, Integer>(buffstat, val));
    }

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.duration = MapleDataTool.getIntConvert("time", source, -1);
        ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
        ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
        ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
        ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
        ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
        ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
        int iprop = MapleDataTool.getInt("prop", source, 100);
        ret.prop = iprop / 100.0;
        ret.mobCount = MapleDataTool.getInt("mobCount", source, 1);
        ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
        ret.morphId = MapleDataTool.getInt("morph", source, 0);
        ret.isGhost = MapleDataTool.getInt("ghost", source, 0) != 0;
        ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0);
        ret.sourceid = sourceid;
        ret.skill = skill;
        if (!ret.skill && ret.duration > -1)
            ret.overTime = true;
        else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }
        ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();
        ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
        ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
        ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
        ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
        ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
        ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
        ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
        ret.jump = (short) MapleDataTool.getInt("jump", source, 0);
        if (ret.overTime && ret.getSummonMovementType() == null) {
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
        }
        int x = MapleDataTool.getInt("x", source, 0);
        ret.x = x;
        ret.y = MapleDataTool.getInt("y", source, 0);
        ret.damage = MapleDataTool.getIntConvert("damage", source, 100);
        ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1);
        ret.bulletCount = MapleDataTool.getIntConvert("bulletCount", source, 1);
        ret.bulletConsume = MapleDataTool.getIntConvert("bulletConsume", source, 0);
        ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
        ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
        ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
        ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);
        Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<MonsterStatus, Integer>();
        if (skill)
            switch (sourceid) {
                case SkillConstants.NightLord.NinjaAmbush:
                case SkillConstants.Shadower.NinjaAmbush:
                    monsterStatus.put(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(1));
                    break;
                case SkillConstants.Magician.MagicGuard:
                case SkillConstants.BlazeWizard.MagicGuard:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, Integer.valueOf(x)));
                    break;
                case SkillConstants.Cleric.Invincible:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, Integer.valueOf(x)));
                    break;
                case SkillConstants.Gm.Hide:
                case SkillConstants.SuperGm.Hide:
                    ret.duration = 7200000;
                    ret.overTime = true;
                case SkillConstants.Rogue.DarkSight:
                case SkillConstants.WindArcher.WindWalk:
                case SkillConstants.NightWalker.DarkSight:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, Integer.valueOf(x)));
                    break;
                case SkillConstants.ChiefBandit.Pickpocket:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, Integer.valueOf(x)));
                    break;
                case SkillConstants.ChiefBandit.MesoGuard:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, Integer.valueOf(x)));
                    break;
                case SkillConstants.Hermit.MesoUp:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, Integer.valueOf(x)));
                    break;
                case SkillConstants.Hermit.ShadowPartner:
                case SkillConstants.NightWalker.ShadowPartner:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, Integer.valueOf(x)));
                    break;
                case SkillConstants.Priest.MysticDoor:
                case SkillConstants.Hunter.SoulArrow:
                case SkillConstants.Crossbowman.SoulArrow:
                case SkillConstants.WindArcher.SoulArrow:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, Integer.valueOf(x)));
                    break;
                case SkillConstants.WhiteKnight.BwFireCharge:
                case SkillConstants.WhiteKnight.BwIceCharge:
                case SkillConstants.WhiteKnight.BwLitCharge:
                case SkillConstants.WhiteKnight.SwordFireCharge:
                case SkillConstants.WhiteKnight.SwordIceCharge:
                case SkillConstants.WhiteKnight.SwordLitCharge:
                case SkillConstants.Paladin.BwHolyCharge:
                case SkillConstants.Paladin.SwordHolyCharge:
                case SkillConstants.DawnWarrior.SoulCharge:
                case SkillConstants.ThunderBreaker.LightningCharge:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, Integer.valueOf(x)));
                    break;
                case SkillConstants.FPArchMage.ManaReflection:
                case SkillConstants.ILArchMage.ManaReflection:
                case SkillConstants.Bishop.ManaReflection:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, Integer.valueOf(1)));
                    break;
                case SkillConstants.Fighter.AxeBooster:
                case SkillConstants.Fighter.SwordBooster:
                case SkillConstants.Page.BwBooster:
                case SkillConstants.Page.SwordBooster:
                case SkillConstants.Spearman.PolearmBooster:
                case SkillConstants.Spearman.SpearBooster:
                case SkillConstants.Hunter.BowBooster:
                case SkillConstants.Crossbowman.CrossbowBooster:
                case SkillConstants.Assassin.ClawBooster:
                case SkillConstants.Bandit.DaggerBooster:
                case SkillConstants.FPMage.SpellBooster:
                case SkillConstants.ILMage.SpellBooster:
                case SkillConstants.Brawler.KnucklerBooster:
                case SkillConstants.Gunslinger.GunBooster:
                case SkillConstants.DawnWarrior.SwordBooster:
                case SkillConstants.BlazeWizard.SpellBooster:
                case SkillConstants.WindArcher.BowBooster:
                case SkillConstants.NightWalker.ClawBooster:
                case SkillConstants.ThunderBreaker.KnucklerBooster:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, Integer.valueOf(x)));
                    break;
                case SkillConstants.Corsair.SpeedInfusion:
                case SkillConstants.Buccaneer.SpeedInfusion:
                case SkillConstants.ThunderBreaker.SpeedInfusion:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, Integer.valueOf(x)));
                    break;
                case SkillConstants.Pirate.Dash:
                case SkillConstants.ThunderBreaker.Dash:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH, Integer.valueOf(x)));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH2, Integer.valueOf(ret.y)));
                    break;
                case SkillConstants.Fighter.PowerGuard:
                case SkillConstants.Page.PowerGuard:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, Integer.valueOf(x)));
                    break;
                case SkillConstants.Spearman.HyperBody:
                case SkillConstants.SuperGm.HyperBody:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYHP, Integer.valueOf(x)));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYMP, Integer.valueOf(ret.y)));
                    break;
                case SkillConstants.Beginner.Recovery:
                case SkillConstants.Noblesse.Recovery:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, Integer.valueOf(x)));
                    break;
                case SkillConstants.Crusader.ComboAttack:
                case SkillConstants.DawnWarrior.ComboAttack:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, Integer.valueOf(1)));
                    break;
                case SkillConstants.Beginner.MonsterRider:
                case SkillConstants.Noblesse.MonsterRider:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(1)));
                    break;
                case SkillConstants.Corsair.Battleship:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(sourceid)));
                    break;
                case SkillConstants.Bowmaster.Concentrate:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONCENTRATE, x));
                    break;
                case SkillConstants.DragonKnight.DragonRoar:
                    ret.hpR = -x / 100.0;
                    break;
                case SkillConstants.DragonKnight.DragonBlood:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGONBLOOD, Integer.valueOf(ret.x)));
                    break;
                case SkillConstants.Hero.MapleWarrior:
                case SkillConstants.Paladin.MapleWarrior:
                case SkillConstants.DarkKnight.MapleWarrior:
                case SkillConstants.FPArchMage.MapleWarrior:
                case SkillConstants.ILArchMage.MapleWarrior:
                case SkillConstants.Bishop.MapleWarrior:
                case SkillConstants.Bowmaster.MapleWarrior:
                case SkillConstants.Marksman.MapleWarrior:
                case SkillConstants.NightLord.MapleWarrior:
                case SkillConstants.Shadower.MapleWarrior:
                case SkillConstants.Corsair.MapleWarrior:
                case SkillConstants.Buccaneer.MapleWarrior:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, Integer.valueOf(ret.x)));
                    break;
                case SkillConstants.Bowmaster.SharpEyes:
                case SkillConstants.Marksman.SharpEyes:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, Integer.valueOf(ret.x << 8 | ret.y)));
                    break;
                case SkillConstants.Rogue.Disorder:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case SkillConstants.Corsair.Hypnotize:
                    monsterStatus.put(MonsterStatus.INERTMOB, 1);
                    break;
                case SkillConstants.Page.Threaten:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case SkillConstants.Crusader.AxeComa:
                case SkillConstants.Crusader.SwordComa:
                case SkillConstants.Crusader.Shout:
                case SkillConstants.WhiteKnight.ChargeBlow:
                case SkillConstants.Hunter.ArrowBomb:
                case SkillConstants.ChiefBandit.Assaulter:
                case SkillConstants.Shadower.BoomerangStep:
                case SkillConstants.Brawler.BackspinBlow:
                case SkillConstants.Brawler.DoubleUppercut:
                case SkillConstants.Buccaneer.Demolition:
                case SkillConstants.Buccaneer.Snatch:
                case SkillConstants.Buccaneer.Barrage:
                case SkillConstants.Gunslinger.BlankShot:
                case SkillConstants.DawnWarrior.Coma:
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case SkillConstants.NightLord.Taunt:
                case SkillConstants.Shadower.Taunt:
                    monsterStatus.put(MonsterStatus.SHOWDOWN, Integer.valueOf(1));
                    break;
                case SkillConstants.ILWizard.ColdBeam:
                case SkillConstants.ILMage.IceStrike:
                case SkillConstants.ILArchMage.Blizzard:
                case SkillConstants.ILMage.ElementComposition:
                case SkillConstants.Sniper.Blizzard:
                case SkillConstants.Outlaw.IceSplitter:
                case SkillConstants.FPArchMage.Paralyze:
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    ret.duration *= 2; // freezing skills are a little strange
                    break;
                case SkillConstants.FPWizard.Slow:
                case SkillConstants.ILWizard.Slow:
                case SkillConstants.BlazeWizard.Slow:
                    monsterStatus.put(MonsterStatus.SPEED, Integer.valueOf(ret.x));
                    break;
                case SkillConstants.FPWizard.PoisonBreath:
                case SkillConstants.FPMage.ElementComposition:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    break;
                case SkillConstants.Priest.Doom:
                    monsterStatus.put(MonsterStatus.DOOM, Integer.valueOf(1));
                    break;
                case SkillConstants.Ranger.Puppet:
                case SkillConstants.Sniper.Puppet:
                case SkillConstants.WindArcher.Puppet:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, Integer.valueOf(1)));
                    break;
                case SkillConstants.Ranger.SilverHawk:
                case SkillConstants.Sniper.GoldenEagle:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case SkillConstants.FPArchMage.Elquines:
                case SkillConstants.Marksman.Frostprey:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case SkillConstants.Priest.SummonDragon:
                case SkillConstants.Bowmaster.Phoenix:
                case SkillConstants.ILArchMage.Ifrit:
                case SkillConstants.Bishop.Bahamut:
                case SkillConstants.DarkKnight.Beholder:
                case SkillConstants.Outlaw.Octopus:
                case SkillConstants.Corsair.WrathOfTheOctopi:
                case SkillConstants.Outlaw.Gaviota:
                case SkillConstants.DawnWarrior.Soul:
                case SkillConstants.BlazeWizard.Flame:
                case SkillConstants.WindArcher.Storm:
                case SkillConstants.NightWalker.Darkness:
                case SkillConstants.ThunderBreaker.Lightning:
                case SkillConstants.BlazeWizard.Ifrit:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    break;
                case SkillConstants.Priest.HolySymbol:
                case SkillConstants.SuperGm.HolySymbol:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, Integer.valueOf(x)));
                    break;
                case SkillConstants.ILMage.Seal:
                case SkillConstants.FPMage.Seal:
                    monsterStatus.put(MonsterStatus.SEAL, 1);
                    break;
                case SkillConstants.Hermit.ShadowWeb: // shadow web
                case SkillConstants.NightWalker.ShadowWeb:
                    monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case SkillConstants.NightLord.ShadowStars:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOW_CLAW, Integer.valueOf(0)));
                    break;
                case SkillConstants.FPArchMage.Infinity:
                case SkillConstants.ILArchMage.Infinity:
                case SkillConstants.Bishop.Infinity:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, Integer.valueOf(x)));
                    break;
                case SkillConstants.Hero.PowerStance:
                case SkillConstants.Paladin.PowerStance:
                case SkillConstants.DarkKnight.PowerStance:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, Integer.valueOf(iprop)));
                    break;
                case SkillConstants.Beginner.EchoOfHero:
                case SkillConstants.Noblesse.EchoOfHero:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, Integer.valueOf(ret.x)));
                    break;
                case SkillConstants.Bishop.HolyShield:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(x)));
                    break;
                case SkillConstants.Bowmaster.Hamstring:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.SPEED, x);
                    break;
                case SkillConstants.Marksman.Blind:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.ACC, x);
                    break;
                case SkillConstants.FPArchMage.FireDemon:
                case SkillConstants.ILArchMage.IceDemon:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case SkillConstants.Outlaw.HomingBeacon:
                case SkillConstants.Corsair.Bullseye:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, Integer.valueOf(x)));
                    break;
                case SkillConstants.DawnWarrior.FinalAttack:
                case SkillConstants.WindArcher.FinalAttack:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, Integer.valueOf(x)));
                    break;
                case SkillConstants.Beginner.BeserkFury:
                case SkillConstants.Noblesse.BeserkFury:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, Integer.valueOf(1)));
                    break;
                case SkillConstants.Beginner.InvincibleBarrier:
                case SkillConstants.Noblesse.InvincibleBarrier:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1)));
                    break;
                default:
                    break;
            }
        if (ret.isMorph())
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(ret.getMorph())));
        if (ret.isGhost && !skill)
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.GHOST_MORPH, Integer.valueOf(1)));
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult())
            switch (sourceid) { // MP eater
                case SkillConstants.FPWizard.MpEater:
                case SkillConstants.ILWizard.MpEater:
                case SkillConstants.Cleric.MpEater:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER)
                        return;
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
                        }
                    }
                    break;
            }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos);
    }

    private boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos) {
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);
        if (primary)
            if (itemConNo != 0)
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleItemInformationProvider.getInstance().getInventoryType(itemCon), itemCon, itemConNo, false, true);
        List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (!primary && isResurrection()) {
            hpchange = applyto.getMaxHp();
            applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
        }
        if (isDispel() && makeChanceResult())
            applyto.dispelDebuffs();
        if (isHeroWill())
            applyto.dispelSeduce();
        if (hpchange != 0) {
            if (hpchange < 0 && (-hpchange) > applyto.getHp())
                return false;
            int newHp = applyto.getHp() + hpchange;
            if (newHp < 1)
                newHp = 1;
            applyto.setHp(newHp);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
        }
        if (mpchange != 0) {
            if (mpchange < 0 && -mpchange > applyto.getMp())
                return false;
            applyto.setMp(applyto.getMp() + mpchange);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
        }
        applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true));
        if (moveTo != -1)
            if (applyto.getMap().getReturnMapId() != applyto.getMapId()) {
                MapleMap target;
                if (moveTo == 999999999)
                    target = applyto.getMap().getReturnMap();
                else {
                    target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61)
                        if (target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20)
                            if (target.getId() / 10000000 != applyto.getMapId() / 10000000)
                                return false;
                }
                applyto.changeMap(target);
            } else
                return false;
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 0; i < 255; i++) { // impose order...
                IItem item = use.getItem((byte) i);
                if (item != null) {
                    boolean isStar = InventoryConstants.isThrowingStar(item.getItemId());
                    if (isStar && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0)
                return false;
            else
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, true);
        }
        if (overTime || isCygnusFA())
            applyBuffEffect(applyfrom, applyto, primary);
        if (primary && (overTime || isHeal()))
            applyBuff(applyfrom);
        if (primary && isMonsterBuff())
            applyMonsterBuff(applyfrom);
        if (this.getFatigue() != 0)
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null && pos != null) {
            final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.getSummons().put(sourceid, tosummon);
            tosummon.addHP(x);
            if (isBeholder())
                tosummon.addHP(1);
        }
        if (isMagicDoor()) { // Magic Door
            Point doorPosition = new Point(applyto.getPosition());
            MapleDoor door = new MapleDoor(applyto, doorPosition);
            applyto.getMap().spawnDoor(door);
            applyto.addDoor(door);
            door = new MapleDoor(door);
            applyto.addDoor(door);
            door.getTown().spawnDoor(door);
            if (applyto.getParty() != null) // update town doors
                applyto.silentPartyUpdate();
            applyto.disableDoor();
        } else if (isMist()) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), sourceid != SkillConstants.Shadower.Smokescreen, false);
        } else if (isTimeLeap()) // Time Leap
            for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns())
                if (i.skillId != SkillConstants.Buccaneer.TimeLeap)
                    applyto.removeCooldown(i.skillId);
        return true;
    }

    private void applyBuff(MapleCharacter applyfrom) {
        if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<MapleCharacter>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive()))
                        affectedp.add(affected);
                    if (isTimeLeap())
                        for (PlayerCoolDownValueHolder i : affected.getAllCooldowns())
                            affected.removeCooldown(i.skillId);
                }
            }
            for (MapleCharacter affected : affectedp) {
                applyTo(applyfrom, affected, false, null);
                affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
                affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
            }
        }
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        ISkill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            MapleMonster monster = (MapleMonster) mo;
            if (makeChanceResult())
                monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, false), isPoison(), getDuration());
            i++;
            if (i >= mobCount)
                break;
        }
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime) {
        int localDuration = duration;
        localDuration = alchemistModifyVal(chr, localDuration, false);
        CancelEffectAction cancelAction = new CancelEffectAction(chr, this, starttime);
        ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + localDuration) - System.currentTimeMillis()));
        chr.registerEffect(this, starttime, schedule);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
            if (!tosummon.isPuppet()) {
                chr.getMap().spawnSummon(tosummon);
                chr.getSummons().put(sourceid, tosummon);
                tosummon.addHP(x);
            }
        }
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        if (sourceid != SkillConstants.Corsair.Battleship) {
            if (!this.isMonsterRiding())
                applyto.cancelEffect(this, true, -1);
        } else
            applyto.cancelEffect(this, true, -1);
        List<Pair<MapleBuffStat, Integer>> localstatups = statups;
        int localDuration = duration;
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        int localX = x;
        int localY = y;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingLevel = 0;
            IItem mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
            if (mount != null)
                ridingLevel = mount.getItemId();
            if (sourceid == SkillConstants.Corsair.Battleship)
                ridingLevel = 1932000;
            else {
                if (applyto.getMount() == null)
                    applyto.mount(ridingLevel, sourceid);
                applyto.getMount().startSchedule();
            }
            if (sourceid == SkillConstants.Corsair.Battleship)
                givemount = new MapleMount(applyto, 1932000, SkillConstants.Corsair.Battleship);
            else
                givemount = applyto.getMount();
            localDuration = sourceid;
            localsourceid = ridingLevel;
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
        } else if (isSkillMorph())
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, getMorph(applyto)));
        if (primary)
            localDuration = alchemistModifyVal(applyfrom, localDuration, false);
        if (localstatups.size() > 0) {
            MaplePacket buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups);
            if (isDash())
                if ((applyto.getJob().getId() / 100) % 10 != 5)
                    applyto.changeSkillLevel(SkillFactory.getSkill(sourceid), 0, 10);
                else
                    applyto.getClient().getSession().write(MaplePacketCreator.giveDash(Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH, 1)), sourceid, localX, localY, seconds));
            else if (isInfusion())
                applyto.getClient().getSession().write(MaplePacketCreator.giveInfusion(seconds, x));
            else if (isMonsterRiding())
                buff = MaplePacketCreator.giveBuff(localsourceid, localDuration, localstatups);
            else if (isCygnusFA())
                buff = MaplePacketCreator.giveFinalAttack(sourceid, seconds);
            else if (isHomingBeacon())
                buff = MaplePacketCreator.useHomingBeacon(applyto, sourceid, statups);
            applyto.getClient().getSession().write(buff);
        }
        if (isDash())
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showDash(applyto.getId(), sourceid, localDuration, localstatups), false);
        else if (isInfusion())
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showSpeedInfusion(applyto.getId(), sourceid, localDuration, localstatups), false);
        else if (isDs()) {
            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat, false), false);
        } else if (isCombo()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, false), false);
        } else if (isMonsterRiding()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 1));
            if (applyto.getMount().getItemId() != 0)
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, givemount), false);
            localDuration = duration;
        } else if (isShadowPartner()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, false), false);
        } else if (isSoulArrow()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, false), false);
        } else if (isEnrage())
            applyto.handleOrbconsume();
        else if (isMorph()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, true), false);
        } else if (isTimeLeap())
            for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns())
                if (i.skillId != SkillConstants.Buccaneer.TimeLeap)
                    applyto.removeCooldown(i.skillId);
        if (localstatups.size() > 0) {
            long starttime = System.currentTimeMillis();
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
            ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, starttime, schedule);
        }
        if (primary)
            if (isDash())
                if ((applyto.getJob().getId() / 100) % 10 != 5)
                    applyto.changeSkillLevel(SkillFactory.getSkill(sourceid), 0, 10);
                else
                    applyto.getClient().getSession().write(MaplePacketCreator.giveDash(Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH, 1)), sourceid, localX, localY, seconds));
            else if (isInfusion())
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignInfusion(applyto.getId(), x, seconds), false);
            else
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, (byte) 3), false);
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if (hp != 0)
            if (!skill)
                if (primary)
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                else
                    hpchange += hp;
            else
                hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
            applyfrom.checkBerserk();
        }
        if (primary)
            if (hpCon != 0)
                hpchange -= hpCon;
        if (isChakra())
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0)
            if (primary)
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            else
                mpchange += mp;
        if (mpR != 0)
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        if (primary)
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                if (isAFpMage || isCygnus || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    ISkill amp;
                    amp = isAFpMage ? SkillFactory.getSkill(SkillConstants.FPMage.ElementAmplification) : (isCygnus ? SkillFactory.getSkill(SkillConstants.BlazeWizard.ElementAmplification) : SkillFactory.getSkill(SkillConstants.ILMage.ElementAmplification));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        MapleStatEffect ampStat = amp.getEffect(ampLevel);
                        mod = ampStat.getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null)
                    mpchange = 0;
                if (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null) {
                    Integer concentrate = applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE);
                    mpchange -= (int) (mpchange * (concentrate.doubleValue() / 100));
                }
            }
        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null)
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = SkillConstants.Hermit.Alchemist;
        if (chr.isCygnus())
            id = SkillConstants.NightWalker.Alchemist;
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        if (alchemistLevel == 0)
            return null;
        return SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        switch (sourceid) {
            case SkillConstants.Beginner.EchoOfHero:
            case SkillConstants.Noblesse.EchoOfHero:
            case SkillConstants.SuperGm.HealPlusDispel:
            case SkillConstants.SuperGm.Haste:
            case SkillConstants.SuperGm.HolySymbol:
            case SkillConstants.SuperGm.Bless:
            case SkillConstants.SuperGm.Resurrection:
            case SkillConstants.SuperGm.HyperBody:
                return true;
            default:
                return false;
        }
    }

    private boolean isMonsterBuff() {
        if (!skill)
            return false;
        switch (sourceid) {
            case SkillConstants.Page.Threaten:
            case SkillConstants.FPWizard.Slow:
            case SkillConstants.ILWizard.Slow:
            case SkillConstants.FPMage.Seal:
            case SkillConstants.ILMage.Seal:
            case SkillConstants.Priest.Doom:
            case SkillConstants.Hermit.ShadowWeb:
            case SkillConstants.NightLord.NinjaAmbush:
            case SkillConstants.Shadower.NinjaAmbush:
            case SkillConstants.BlazeWizard.Slow:
            case SkillConstants.BlazeWizard.Seal:
            case SkillConstants.NightWalker.ShadowWeb:
                return true;
        }
        return false;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null)
            return false;
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == SkillConstants.Paladin.SwordHolyCharge || sourceid == SkillConstants.Paladin.BwHolyCharge || sourceid == SkillConstants.DawnWarrior.SoulCharge) // wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        return true;
    }

    private boolean isHeal() {
        return sourceid == SkillConstants.Cleric.Heal || sourceid == SkillConstants.SuperGm.HealPlusDispel;
    }

    private boolean isResurrection() {
        return sourceid == SkillConstants.Bishop.Resurrection || sourceid == SkillConstants.Gm.Resurrection || sourceid == SkillConstants.SuperGm.Resurrection;
    }

    private boolean isTimeLeap() {
        return sourceid == SkillConstants.Buccaneer.TimeLeap;
    }

    public boolean isHide() {
        return skill && (sourceid == SkillConstants.Gm.Hide || sourceid == SkillConstants.SuperGm.Hide);
    }

    public boolean isDragonBlood() {
        return skill && sourceid == SkillConstants.DragonKnight.DragonBlood;
    }

    public boolean isBerserk() {
        return skill && sourceid == SkillConstants.DarkKnight.Berserk;
    }

    private boolean isDs() {
        return skill && (sourceid == SkillConstants.Rogue.DarkSight || sourceid == SkillConstants.WindArcher.WindWalk || sourceid == SkillConstants.NightWalker.DarkSight);
    }

    private boolean isCombo() {
        return skill && (sourceid == SkillConstants.Crusader.ComboAttack || sourceid == SkillConstants.DawnWarrior.ComboAttack);
    }

    private boolean isEnrage() {
        return skill && sourceid == SkillConstants.Hero.Enrage;
    }

    public boolean isBeholder() {
        return skill && sourceid == SkillConstants.DarkKnight.Beholder;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == SkillConstants.Hermit.ShadowPartner || sourceid == SkillConstants.NightWalker.ShadowPartner);
    }

    private boolean isChakra() {
        return skill && sourceid == SkillConstants.ChiefBandit.Chakra;
    }

    private boolean isHomingBeacon() {
        return skill && (sourceid == SkillConstants.Outlaw.HomingBeacon || sourceid == SkillConstants.Corsair.Bullseye);
    }

    public boolean isMonsterRiding() {
        return skill && (sourceid % 10000000 == 1004 || sourceid == SkillConstants.Corsair.Battleship);
    }

    public boolean isMagicDoor() {
        return skill && sourceid == SkillConstants.Priest.MysticDoor;
    }

    public boolean isPoison() {
        return skill && (sourceid == SkillConstants.FPMage.PoisonMist || sourceid == SkillConstants.FPWizard.PoisonBreath || sourceid == SkillConstants.FPMage.ElementComposition || sourceid == SkillConstants.NightWalker.PoisonBomb);
    }

    private boolean isMist() {
        return skill && (sourceid == SkillConstants.FPMage.PoisonMist || sourceid == SkillConstants.Shadower.Smokescreen || sourceid == SkillConstants.BlazeWizard.FlameGear || sourceid == SkillConstants.NightWalker.PoisonBomb);
    }

    private boolean isSoulArrow() {
        return skill && (sourceid == SkillConstants.Hunter.SoulArrow || sourceid == SkillConstants.Crossbowman.SoulArrow || sourceid == SkillConstants.WindArcher.SoulArrow);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == SkillConstants.NightLord.ShadowStars;
    }

    private boolean isDispel() {
        return skill && (sourceid == SkillConstants.Priest.Dispel || sourceid == SkillConstants.SuperGm.HealPlusDispel);
    }

    private boolean isHeroWill() {
        if (skill)
            switch (sourceid) {
                case SkillConstants.Hero.HerosWill:
                case SkillConstants.Paladin.HerosWill:
                case SkillConstants.DarkKnight.HerosWill:
                case SkillConstants.FPArchMage.HerosWill:
                case SkillConstants.ILArchMage.HerosWill:
                case SkillConstants.Bishop.HerosWill:
                case SkillConstants.Bowmaster.HerosWill:
                case SkillConstants.Marksman.HerosWill:
                case SkillConstants.NightLord.HerosWill:
                case SkillConstants.Shadower.HerosWill:
                case SkillConstants.Buccaneer.PiratesRage:
                    return true;
            }
        return false;
    }

    private boolean isDash() {
        return skill && (sourceid == SkillConstants.Pirate.Dash || sourceid == SkillConstants.ThunderBreaker.Dash);
    }

    private boolean isSkillMorph() {
        return skill && (sourceid == SkillConstants.Buccaneer.SuperTransformation || sourceid == SkillConstants.Marauder.Transformation || sourceid == SkillConstants.WindArcher.EagleEye || sourceid == SkillConstants.ThunderBreaker.Transformation);
    }

    private boolean isInfusion() {
        return skill && (sourceid == SkillConstants.Buccaneer.SpeedInfusion || sourceid == SkillConstants.Corsair.SpeedInfusion || sourceid == SkillConstants.ThunderBreaker.SpeedInfusion);
    }

    private boolean isCygnusFA() {
        return skill && (sourceid == SkillConstants.DawnWarrior.FinalAttack || sourceid == SkillConstants.WindArcher.FinalAttack);
    }

    private boolean isMorph() {
        return morphId > 0;
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId % 10 == 0)
            return morphId + chr.getGender();
        return morphId + 100 * chr.getGender();
    }

    private SummonMovementType getSummonMovementType() {
        if (!skill)
            return null;
        switch (sourceid) {
            case SkillConstants.Ranger.Puppet:
            case SkillConstants.Sniper.Puppet:
            case SkillConstants.WindArcher.Puppet:
            case SkillConstants.Outlaw.Octopus:
            case SkillConstants.Corsair.WrathOfTheOctopi:
                return SummonMovementType.STATIONARY;
            case SkillConstants.Ranger.SilverHawk:
            case SkillConstants.Sniper.GoldenEagle:
            case SkillConstants.Priest.SummonDragon:
            case SkillConstants.Marksman.Frostprey:
            case SkillConstants.Bowmaster.Phoenix:
            case SkillConstants.Outlaw.Gaviota:
                return SummonMovementType.CIRCLE_FOLLOW;
            case SkillConstants.DarkKnight.Beholder:
            case SkillConstants.FPArchMage.Elquines:
            case SkillConstants.ILArchMage.Ifrit:
            case SkillConstants.Bishop.Bahamut:
            case SkillConstants.DawnWarrior.Soul:
            case SkillConstants.BlazeWizard.Flame:
            case SkillConstants.BlazeWizard.Ifrit:
            case SkillConstants.WindArcher.Storm:
            case SkillConstants.NightWalker.Darkness:
            case SkillConstants.ThunderBreaker.Lightning:
                return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private static class CancelEffectAction implements Runnable {
        private MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private long startTime;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime) {
            this.effect = effect;
            this.target = new WeakReference<MapleCharacter>(target);
            this.startTime = startTime;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null)
                realTarget.cancelEffect(effect, false, startTime);
        }
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getMatk() {
        return matk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<MapleBuffStat, Integer>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public int getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }
}
