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
package client;

import constants.SkillConstants;
import java.util.ArrayList;
import java.util.List;

import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.life.Element;

public class Skill implements ISkill {
    private int id;
    private List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
    private Element element;
    private int animationTime;

    private Skill(int id) {
        super();
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public static Skill loadFromData(int id, MapleData data) {
        Skill ret = new Skill(id);
        boolean isBuff = false;
        int skillType = MapleDataTool.getInt("skillType", data, -1);
        String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null)
            ret.element = Element.getFromChar(elem.charAt(0));
        else
            ret.element = Element.NEUTRAL;
        MapleData effect = data.getChildByPath("effect");
        if (skillType != -1) {
            if (skillType == 2)
                isBuff = true;
        } else {
            MapleData action = data.getChildByPath("action");
            MapleData hit = data.getChildByPath("hit");
            MapleData ball = data.getChildByPath("ball");
            isBuff = effect != null && hit == null && ball == null;
            isBuff |= action != null && MapleDataTool.getString("0", action, "").equals("alert2");
            switch (id) {
                case 1121006: // rush
                case 1221007: // rush
                case 1311005: // sacrifice
                case 1321003: // rush
                case 2111002: // explosion
                case 2111003: // poison mist
                case 2301002: // heal
                case 3110001: // mortal blow
                case 3210001: // mortal blow
                case 4101005: // drain
                case 4111003: // shadow web
                case 4201004: // steal
                case 4221006: // smokescreen
                case 9101000: // heal + dispel
                case 1121001: // monster magnet
                case 1221001: // monster magnet
                case 1321001: // monster magnet
                case 5201006: // Recoil Shot
                case 5111004: // energy drain
                case 12111005: // blaze wizard thing
                case 14111001: // Shadow Web
                case 14111006: // poison bomb side effect?
                case 14101006: // vampire
                    isBuff = false;
                    break;
                case SkillConstants.Beginner.Recovery:
                case SkillConstants.Beginner.NimbleFeet:
                case SkillConstants.Beginner.MonsterRider:
                case SkillConstants.Beginner.EchoOfHero:
                case SkillConstants.Swordman.IronBody:
                case SkillConstants.Fighter.AxeBooster:
                case SkillConstants.Fighter.PowerGuard:
                case SkillConstants.Fighter.Rage:
                case SkillConstants.Fighter.SwordBooster:
                case SkillConstants.Crusader.ArmorCrash:
                case SkillConstants.Crusader.ComboAttack:
                case SkillConstants.Hero.Enrage:
                case SkillConstants.Hero.HerosWill:
                case SkillConstants.Hero.MapleWarrior:
                case SkillConstants.Hero.PowerStance:
                case SkillConstants.Page.BwBooster:
                case SkillConstants.Page.PowerGuard:
                case SkillConstants.Page.SwordBooster:
                case SkillConstants.Page.Threaten:
                case SkillConstants.WhiteKnight.BwFireCharge:
                case SkillConstants.WhiteKnight.BwIceCharge:
                case SkillConstants.WhiteKnight.BwLitCharge:
                case SkillConstants.WhiteKnight.MagicCrash:
                case SkillConstants.WhiteKnight.SwordFireCharge:
                case SkillConstants.WhiteKnight.SwordIceCharge:
                case SkillConstants.WhiteKnight.SwordLitCharge:
                case SkillConstants.Paladin.BwHolyCharge:
                case SkillConstants.Paladin.HerosWill:
                case SkillConstants.Paladin.MapleWarrior:
                case SkillConstants.Paladin.PowerStance:
                case SkillConstants.Paladin.SwordHolyCharge:
                case SkillConstants.Spearman.HyperBody:
                case SkillConstants.Spearman.IronWill:
                case SkillConstants.Spearman.PolearmBooster:
                case SkillConstants.Spearman.SpearBooster:
                case SkillConstants.DragonKnight.DragonBlood:
                case SkillConstants.DragonKnight.PowerCrash:
                case SkillConstants.DarkKnight.AuraOfBeholder:
                case SkillConstants.DarkKnight.Beholder:
                case SkillConstants.DarkKnight.HerosWill:
                case SkillConstants.DarkKnight.HexOfBeholder:
                case SkillConstants.DarkKnight.MapleWarrior:
                case SkillConstants.DarkKnight.PowerStance:
                case SkillConstants.Magician.MagicGuard:
                case SkillConstants.Magician.MagicArmor:
                case SkillConstants.FPWizard.Meditation:
                case SkillConstants.FPWizard.Slow:
                case SkillConstants.FPMage.Seal:
                case SkillConstants.FPMage.SpellBooster:
                case SkillConstants.FPArchMage.Elquines:
                case SkillConstants.FPArchMage.HerosWill:
                case SkillConstants.FPArchMage.Infinity:
                case SkillConstants.FPArchMage.ManaReflection:
                case SkillConstants.FPArchMage.MapleWarrior:
                case SkillConstants.ILWizard.Meditation:
                case SkillConstants.ILMage.Seal:
                case SkillConstants.ILWizard.Slow:
                case SkillConstants.ILMage.SpellBooster:
                case SkillConstants.ILArchMage.HerosWill:
                case SkillConstants.ILArchMage.Ifrit:
                case SkillConstants.ILArchMage.Infinity:
                case SkillConstants.ILArchMage.ManaReflection:
                case SkillConstants.ILArchMage.MapleWarrior:
                case SkillConstants.Cleric.Invincible:
                case SkillConstants.Cleric.Bless:
                case SkillConstants.Priest.Dispel:
                case SkillConstants.Priest.Doom:
                case SkillConstants.Priest.HolySymbol:
                case SkillConstants.Priest.SummonDragon:
                case SkillConstants.Bishop.Bahamut:
                case SkillConstants.Bishop.HerosWill:
                case SkillConstants.Bishop.HolyShield:
                case SkillConstants.Bishop.Infinity:
                case SkillConstants.Bishop.ManaReflection:
                case SkillConstants.Bishop.MapleWarrior:
                case SkillConstants.Archer.Focus:
                case SkillConstants.Hunter.BowBooster:
                case SkillConstants.Hunter.SoulArrow:
                case SkillConstants.Ranger.Puppet:
                case SkillConstants.Ranger.SilverHawk:
                case SkillConstants.Bowmaster.Concentrate:
                case SkillConstants.Bowmaster.HerosWill:
                case SkillConstants.Bowmaster.MapleWarrior:
                case SkillConstants.Bowmaster.Phoenix:
                case SkillConstants.Bowmaster.SharpEyes:
                case SkillConstants.Crossbowman.CrossbowBooster:
                case SkillConstants.Crossbowman.SoulArrow:
                case SkillConstants.Sniper.GoldenEagle:
                case SkillConstants.Sniper.Puppet:
                case SkillConstants.Marksman.Blind:
                case SkillConstants.Marksman.Frostprey:
                case SkillConstants.Marksman.HerosWill:
                case SkillConstants.Marksman.MapleWarrior:
                case SkillConstants.Marksman.SharpEyes:
                case SkillConstants.Rogue.DarkSight:
                case SkillConstants.Assassin.ClawBooster:
                case SkillConstants.Assassin.Haste:
                case SkillConstants.Hermit.MesoUp:
                case SkillConstants.Hermit.ShadowPartner:
                case SkillConstants.NightLord.HerosWill:
                case SkillConstants.NightLord.MapleWarrior:
                case SkillConstants.NightLord.NinjaAmbush:
                case SkillConstants.NightLord.ShadowStars:
                case SkillConstants.Bandit.DaggerBooster:
                case SkillConstants.Bandit.Haste:
                case SkillConstants.ChiefBandit.MesoGuard:
                case SkillConstants.ChiefBandit.Pickpocket:
                case SkillConstants.Shadower.HerosWill:
                case SkillConstants.Shadower.MapleWarrior:
                case SkillConstants.Shadower.NinjaAmbush:
                case SkillConstants.Pirate.Dash:
                case SkillConstants.Marauder.Transformation:
                case SkillConstants.Buccaneer.SuperTransformation:
                case SkillConstants.Outlaw.Gaviota:
                case SkillConstants.Outlaw.Octopus:
                case SkillConstants.Corsair.Battleship:
                case SkillConstants.Corsair.WrathOfTheOctopi:
//                case SkillConstants.Gm.Haste:
                case SkillConstants.SuperGm.Haste:
                case SkillConstants.SuperGm.HolySymbol:
                case SkillConstants.SuperGm.Bless:
                case SkillConstants.SuperGm.Hide:
                case SkillConstants.SuperGm.HyperBody:
                case SkillConstants.Noblesse.BlessingOfTheFairy:
                case SkillConstants.Noblesse.EchoOfHero:
                case SkillConstants.Noblesse.MonsterRider:
                case SkillConstants.Noblesse.NimbleFeet:
                case SkillConstants.Noblesse.Recovery:
                case SkillConstants.DawnWarrior.ComboAttack:
                case SkillConstants.DawnWarrior.FinalAttack:
                case SkillConstants.DawnWarrior.IronBody:
                case SkillConstants.DawnWarrior.Rage:
                case SkillConstants.DawnWarrior.Soul:
                case SkillConstants.DawnWarrior.SoulCharge:
                case SkillConstants.DawnWarrior.SwordBooster:
                case SkillConstants.BlazeWizard.ElementalReset:
                case SkillConstants.BlazeWizard.Flame:
                case SkillConstants.BlazeWizard.Ifrit:
                case SkillConstants.BlazeWizard.MagicArmor:
                case SkillConstants.BlazeWizard.MagicGuard:
                case SkillConstants.BlazeWizard.Meditation:
                case SkillConstants.BlazeWizard.Seal:
                case SkillConstants.BlazeWizard.Slow:
                case SkillConstants.BlazeWizard.SpellBooster:
                case SkillConstants.WindArcher.BowBooster:
                case SkillConstants.WindArcher.EagleEye:
                case SkillConstants.WindArcher.FinalAttack:
                case SkillConstants.WindArcher.Focus:
                case SkillConstants.WindArcher.Puppet:
                case SkillConstants.WindArcher.SoulArrow:
                case SkillConstants.WindArcher.Storm:
                case SkillConstants.WindArcher.WindWalk:
                case SkillConstants.NightWalker.ClawBooster:
                case SkillConstants.NightWalker.Darkness:
                case SkillConstants.NightWalker.DarkSight:
                case SkillConstants.NightWalker.Haste:
                case SkillConstants.NightWalker.ShadowPartner:
                case SkillConstants.ThunderBreaker.Dash:
                case SkillConstants.ThunderBreaker.EnergyCharge:
                case SkillConstants.ThunderBreaker.EnergyDrain:
                case SkillConstants.ThunderBreaker.KnucklerBooster:
                case SkillConstants.ThunderBreaker.Lightning:
                case SkillConstants.ThunderBreaker.LightningCharge:
                case SkillConstants.ThunderBreaker.SpeedInfusion:
                case SkillConstants.ThunderBreaker.Transformation:
                    isBuff = true;
                    break;
            }
        }
        for (MapleData level : data.getChildByPath("level"))
            ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
        ret.animationTime = 0;
        if (effect != null)
            for (MapleData effectEntry : effect)
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
        return ret;
    }

    @Override
    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    @Override
    public int getMaxLevel() {
        return effects.size();
    }

    @Override
    public boolean isFourthJob() {
        return (id / 10000) % 10 == 2;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public int getAnimationTime() {
        return animationTime;
    }

    @Override
    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }
}
