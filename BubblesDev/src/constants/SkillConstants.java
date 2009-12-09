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
package constants;

/**
 *
 * @author Jay Estrella
 */
public class SkillConstants {
    public static class All {
        public static final int Attack = 0;
    }

    public static class Beginner {
        public static final int BlessingOfTheFairty = 12, EchoOfHero = 1005, FollowTheLead = 8, MonsterRider = 1004, NimbleFeet = 1002, Recovery = 1001, BambooRain = 1009, InvincibleBarrier = 1010, BeserkFury = 1011;
    }

    public static class Swordman {
        public static final int ImprovedMaxHpIncrease = 1000001, IronBody = 1000003;
    }

    public static class Fighter {
        public static final int AxeBooster = 1101005, AxeMastery = 1100001, PowerGuard = 1101007, Rage = 1101006, SwordBooster = 1101004, SwordMastery = 1100000;
    }

    public static class Crusader {
        public static final int ArmorCrash = 1111007, AxeComa = 1111006, AxePanic = 1111004, ComboAttack = 1111002, Shout = 1111008, SwordComa = 1111005, SwordPanic = 1111003;
    }

    public static class Hero {
        public static final int Achilles = 1120004, AdvancedComboAttack = 1120003, Enrage = 1121010, Guardian = 1120005, HerosWill = 1121011, MapleWarrior = 1121000, MonsterMagnet = 1121001, PowerStance = 1121002;
    }

    public static class Page {
        public static final int BwBooster = 1201005, BwMastery = 1200001, PowerGuard = 1201007, SwordBooster = 1201004, SwordMastery = 1200000, Threaten = 1201006;
    }

    public static class WhiteKnight {
        public static final int BwFireCharge = 1211004, BwIceCharge = 1211006, BwLitCharge = 1211008, ChargeBlow = 1211002, MagicCrash = 1211009, SwordFireCharge = 1211003, SwordIceCharge = 1211005, SwordLitCharge = 1211007;
    }

    public static class Paladin {
        public static final int Achilles = 1220005, AdvancedCharge = 1220010, BwHolyCharge = 1221004, Guardian = 1220006, HeavensHammer = 1221011, HerosWill = 1221012, MapleWarrior = 1221000, MonsterMagnet = 1221001, PowerStance = 1221002, SwordHolyCharge = 1221003;
    }

    public static class Spearman {
        public static final int HyperBody = 1301007, IronWill = 1301006, PolearmBooster = 1301005, PolearmMastery = 1300001, SpearBooster = 1301004, SpearMastery = 1300000;
    }

    public static class DragonKnight {
        public static final int DragonBlood = 1311008, DragonRoar = 1311006, ElementalResistance = 1310000, PowerCrash = 1311007, Sacrifice = 1311005;
    }

    public static class DarkKnight {
        public static final int Achilles = 1320005, AuraOfBeholder = 1320008, Beholder = 1321007, Berserk = 1320006, HerosWill = 1321010, HexOfBeholder = 1320009, MapleWarrior = 1321000, MonsterMagnet = 1321001, PowerStance = 1321002;
    }

    public static class Magician {
        public static final int ImprovedMaxMpIncrease = 2000001, MagicArmor = 2001003, MagicGuard = 2001002;
    }

    public static class FPWizard {
        public static final int Meditation = 2101001, MpEater = 2100000, PoisonBreath = 2101005, Slow = 2101003;
    }

    public static class FPMage {
        public static final int Explosion = 2111002, ElementAmplification = 2110001, ElementComposition = 2111006, PartialResistance = 2110000, PoisonMist = 2111003, Seal = 2111004, SpellBooster = 2111005;
    }

    public static class FPArchMage {
        public static final int BigBang = 2121001, Elquines = 2121005, FireDemon = 2121003, HerosWill = 2121008, Infinity = 2121004, ManaReflection = 2121002, MapleWarrior = 2121000, Paralyze = 2121006;
    }

    public static class ILWizard {
        public static final int ColdBeam = 2201004, Meditation = 2201001, MpEater = 2200000, Slow = 2201003;
    }

    public static class ILMage {
        public static final int ElementAmplification = 2210001, ElementComposition = 2211006, IceStrike = 2211002, PartialResistance = 2210000, Seal = 2211004, SpellBooster = 2211005;
    }

    public static class ILArchMage {
        public static final int BigBang = 2221001, Blizzard = 2221007, HerosWill = 2221008, IceDemon = 2221003, Ifrit = 2221005, Infinity = 2221004, ManaReflection = 2221002, MapleWarrior = 2221000;
    }

    public static class Cleric {
        public static final int Bless = 2301004, Heal = 2301002, Invincible = 2301003, MpEater = 2300000;
    }

    public static class Priest {
        public static final int Dispel = 2311001, Doom = 2311005, ElementalResistance = 2310000, HolySymbol = 2311003, MysticDoor = 2311002, SummonDragon = 2311006;
    }

    public static class Bishop {
        public static final int Bahamut = 2321003, BigBang = 2321001, HerosWill = 2321009, HolyShield = 2321005, Infinity = 2321004, ManaReflection = 2321002, MapleWarrior = 2321000, Resurrection = 2321006;
    }

    public static class Archer {
        public static final int CriticalShot = 3000001, Focus = 3001003;
    }

    public static class Hunter {
        public static final int ArrowBomb = 3101005, BowBooster = 3101002, BowMastery = 3100000, SoulArrow = 3101004;
    }

    public static class Ranger {
        public static final int MortalBlow = 3110001, Puppet = 3111002, SilverHawk = 3111005;
    }

    public static class Bowmaster {
        public static final int Concentrate = 3121008, Hamstring = 3121007, HerosWill = 3121009, Hurricane = 3121004, MapleWarrior = 3121000, Phoenix = 3121006, SharpEyes = 3121002;
    }

    public static class Crossbowman {
        public static final int CrossbowBooster = 3201002, CrossbowMastery = 3200000, SoulArrow = 3201004;
    }

    public static class Sniper {
        public static final int Blizzard = 3211003, GoldenEagle = 3211005, MortalBlow = 3210001, Puppet = 3211002;
    }

    public static class Marksman {
        public static final int Blind = 3221006, Frostprey = 3221005, HerosWill = 3221008, MapleWarrior = 3221000, PiercingArrow = 3221001, SharpEyes = 3221002, Snipe = 3221007;
    }

    public static class Rogue {
        public static final int DarkSight = 4001003, Disorder = 4001002, DoubleStab = 4001334, LuckySeven = 4001344;
    }

    public static class Assassin {
        public static final int Steal = 4201004, ClawBooster = 4101003, ClawMastery = 4100000, CriticalThrow = 4100001, Drain = 4101005, Haste = 4101004;
    }

    public static class Hermit {
        public static final int Alchemist = 4110000, Avenger = 4111005, MesoUp = 4111001, ShadowMeso = 4111004, ShadowPartner = 4111002, ShadowWeb = 4111003;
    }

    public static class NightLord {
        public static final int HerosWill = 4121009, MapleWarrior = 4121000, NinjaAmbush = 4121004, NinjaStorm = 4121008, ShadowShifter = 4120002, ShadowStars = 4121006, Taunt = 4121003, TripleThrow = 4121007, VenomousStar = 4120005;
    }

    public static class Bandit {
        public static final int DaggerBooster = 4201002, DaggerMastery = 4200000, Haste = 4201003, SavageBlow = 4201005, Steal = 4201004;
    }

    public static class ChiefBandit {
        public static final int Assaulter = 4211002, BandOfThieves = 4211004, Chakra = 4211001, MesoExplosion = 4211006, MesoGuard = 4211005, Pickpocket = 4211003;
    }

    public static class Shadower {
        public static final int Assassinate = 4221001, BoomerangStep = 4221007, HerosWill = 4221008, MapleWarrior = 4221000, NinjaAmbush = 4221004, ShadowShifter = 4220002, Smokescreen = 4221006, Taunt = 4221003, VenomousStab = 4220005;
    }

    public static class Pirate {
        public static final int Dash = 5001005;
    }

    public static class Brawler {
        public static final int BackspinBlow = 5101002, CorkscrewBlow = 5101004, DoubleUppercut = 5101003, ImproveMaxHp = 5100000, KnucklerBooster = 5101006, KnucklerMastery = 5100001, MpRecovery = 5101005, OakBarrel = 5101007;
    }

    public static class Marauder {
        public static final int EnergyCharge = 5110001, EnergyDrain = 5111004, StunMastery = 5110000, Transformation = 5111005;
    }

    public static class Buccaneer {
        public static final int Barrage = 5121007, Demolition = 5121004, MapleWarrior = 5121000, PiratesRage = 5121008, Snatch = 5121005, SpeedInfusion = 5121009, SuperTransformation = 5121003, TimeLeap = 5121010;
    }

    public static class Gunslinger {
        public static final int BlankShot = 5201004, Grenade = 5201002, GunBooster = 5201003, GunMastery = 5200000;
    }

    public static class Outlaw {
        public static final int Flamethrower = 5211004, Gaviota = 5211002, HomingBeacon = 5211006, IceSplitter = 5211005, Octopus = 5211001;
    }

    public static class Corsair {
        public static final int AerialStrike = 5221003, Battleship = 5221006, Bullseye = 5220011, ElementalBoost = 5220001, Hypnotize = 5221009, MapleWarrior = 5221000, RapidFire = 5221004, SpeedInfusion = 5221010, WrathOfTheOctopi = 5220002;
    }

    public static class Gm {
        public static final int AntiMacro = 9001009, Bless = 9101003, Haste = 9101000, Hide = 9001004, HyperBody = 9001008, Resurrection = 9001005, SuperDragonRoar1 = 9001001, SuperDragonRoar2 = 9001006, Teleport1 = 9001002, Teleport2 = 9001007;
    }

    public static class SuperGm {
        public static final int Bless = 9101003, Haste = 9101001, HealPlusDispel = 9101000, Hide = 9101004, HolySymbol = 9101002, HyperBody = 9101008, Resurrection = 9101005, SuperDragonRoar = 9101006, Teleport = 9101007;
    }

    public static class Noblesse {
        public static final int BlessingOfTheFairy = 10000012, EchoOfHero = 10001005, Maker = 10001007, MonsterRider = 10001004, NimbleFeet = 10001002, Recovery = 10001001, BambooRain = 10001009, InvincibleBarrier = 10001010, BeserkFury = 10001011;
    }

    public static class DawnWarrior {
        public static final int AdvancedCombo = 11110005, Coma = 11111003, ComboAttack = 11111001, FinalAttack = 11101002, IronBody = 11001001, MaxHpEnhancement = 11000000, Panic = 11111002, Rage = 11101003, Soul = 11001004, SoulCharge = 11111007, SwordBooster = 11101001, SwordMastery = 11100000;
    }

    public static class BlazeWizard {
        public static final int ElementalReset = 12101005, ElementAmplification = 12110001, FireStrike = 12111006, Flame = 12001004, FlameGear = 12111005, Ifrit = 12111004, IncreasingMaxMp = 12000000, MagicArmor = 12001002, MagicGuard = 12001001, Meditation = 12101000, Seal = 12111002, Slow = 12101001, SpellBooster = 12101004;
    }

    public static class WindArcher {
        public static final int EagleEye = 13111005, BowBooster = 13101001, BowMastery = 13100000, CriticalShot = 13000000, FinalAttack = 13101002, Focus = 13001002, Hurricane = 13111002, Puppet = 13111004, SoulArrow = 13101003, Storm = 13001004, WindPiercing = 13111006, WindShot = 13111007, WindWalk = 13101006;
    }

    public static class NightWalker {
        public static final int Alchemist = 14110003, Disorder = 14001002, DarkSight = 14001003, Darkness = 14001005, ClawBooster = 14101002, ClawMastery = 14100000, CriticalThrow = 14100001, Haste = 14101003, PoisonBomb = 14111006, ShadowPartner = 14111000, ShadowWeb = 14111001, Vanish = 14100005, Vampire = 14101006, Venom = 14110004;
    }

    public static class ThunderBreaker {
        public static final int CorkscrewBlow = 15101003, Dash = 15001003, EnergyCharge = 15100004, EnergyDrain = 15111001, ImproveMaxHp = 15100000, KnucklerBooster = 15101002, KnucklerMastery = 15100001, Lightning = 15001004, LightningCharge = 15101006, Spark = 15111006, SpeedInfusion = 15111005, Transformation = 15111002;
    }
}
