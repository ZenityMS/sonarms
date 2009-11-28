/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.sf.odinms.net.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MapleWeaponType;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.client.status.MonsterStatusEffect;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.Element;
import net.sf.odinms.server.life.ElementalEffectiveness;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapItem;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.data.input.LittleEndianAccessor;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {
	//private static Logger log = LoggerFactory.getLogger(AbstractDealDamageHandler.class);

	protected static class AttackInfo {
		public int numAttacked, numDamage, numAttackedAndDamage;
		public int skill, stance, direction, charge;
		public List<Pair<Integer, List<Integer>>> allDamage;
		public boolean isHH = false;
		public int speed = 4;

		private MapleStatEffect getAttackEffect(MapleCharacter chr, ISkill theSkill) {
			ISkill mySkill = theSkill;
			if (mySkill == null) {
				mySkill = SkillFactory.getSkill(skill);
			}
			int skillLevel = chr.getSkillLevel(mySkill);
			if (mySkill.getId() == 1009 || mySkill.getId() == 10001009) {
				skillLevel = 1;
			}
			if (skillLevel == 0) {
				return null;
			}
			return mySkill.getEffect(skillLevel);
		}

		public MapleStatEffect getAttackEffect(MapleCharacter chr) {
			return getAttackEffect(chr, null);
		}
	}

	protected void applyAttack(AttackInfo attack, MapleCharacter player, int maxDamagePerMonster, int attackCount) {
		player.getCheatTracker().resetHPRegen();
		player.getCheatTracker().checkAttack(attack.skill);
		
		ISkill theSkill = null;
		MapleStatEffect attackEffect = null;
		if (attack.skill != 0) {
			theSkill = SkillFactory.getSkill(attack.skill);
			attackEffect = attack.getAttackEffect(player, theSkill);
			if (attackEffect == null) {
				AutobanManager.getInstance().autoban(player.getClient(),
					"Using a skill he doesn't have (" + attack.skill + ")");
			}
			if (attack.skill != 2301002) {
				// heal is both an attack and a special move (healing)
				// so we'll let the whole applying magic live in the special move part
				if (player.isAlive()) {
					attackEffect.applyTo(player);
				} else {
					player.getClient().getSession().write(MaplePacketCreator.enableActions());
				}
			}
		}
		if (!player.isAlive()) {
			player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
			return;
		}
		// meso explosion has a variable bullet count
		if (attackCount != attack.numDamage && attack.skill != 4211006 && attack.numDamage != attackCount*2) {
			player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT,
				attack.numDamage + "/" + attackCount);
		}
		int totDamage = 0;
		MapleMap map = player.getMap();
		
		if (attack.skill == 4211006) { // meso explosion
			for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
				MapleMapObject mapobject = map.getMapObject(oned.getLeft().intValue());
				
				if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
					MapleMapItem mapitem = (MapleMapItem) mapobject;
					if (mapitem.getMeso() > 0) {
						synchronized (mapitem) {
							if (mapitem.isPickedUp())
								return;
							map.removeMapObject(mapitem);
							map.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem.getPosition());
							mapitem.setPickedUp(true);
						}
					} else if (mapitem.getMeso() == 0) {
						player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
						return;
					}
				} else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
					player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
					return; // etc explosion, exploding nonexistant things, etc.
				}
			}
		}
		
		for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
			MapleMonster monster = map.getMonsterByOid(oned.getLeft().intValue());

			if (monster != null) {
				int totDamageToOneMonster = 0;
				for (Integer eachd : oned.getRight()) {
					totDamageToOneMonster += eachd.intValue();
				}
				totDamage += totDamageToOneMonster;

				Point playerPos = player.getPosition();
				if (totDamageToOneMonster > attack.numDamage + 1) {
					int dmgCheck = player.getCheatTracker().checkDamage(totDamageToOneMonster);
					if (dmgCheck > 5 && totDamageToOneMonster < 99999) {
						player.getCheatTracker().registerOffense(CheatingOffense.SAME_DAMAGE, dmgCheck + " times: " +
							totDamageToOneMonster);
					}
				}
				
				checkHighDamage(player, monster, attack, theSkill, attackEffect, totDamageToOneMonster, maxDamagePerMonster);
				
				double distance = playerPos.distanceSq(monster.getPosition());
				if (distance > 400000.0) { // 600^2, 550 is approximatly the range of ultis
					player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, Double.toString(Math.sqrt(distance)));
				}
				
				if (attack.skill == 2301002 && !monster.getUndead()) {
					player.getCheatTracker().registerOffense(CheatingOffense.HEAL_ATTACKING_UNDEAD);
					return;
				}

				//TO-DO: shorten this
				if (attack.skill == 5111004) { // Energy Drain
					ISkill edrain = SkillFactory.getSkill(5111004);
					int gainhp;
					gainhp = (int) ((double) totDamage * (double) edrain.getEffect(player.getSkillLevel(edrain)).getX() / 100.0);
					gainhp = Math.min(monster.getMaxHp(), Math.min(gainhp, player.getMaxHp() / 2));
					player.addHP(gainhp);
				} else if (attack.skill == 15100004) {
					ISkill edrain = SkillFactory.getSkill(15100004);
					int gainhp;
					gainhp = (int) ((double) totDamage * (double) edrain.getEffect(player.getSkillLevel(edrain)).getX() / 100.0);
					gainhp = Math.min(monster.getMaxHp(), Math.min(gainhp, player.getMaxHp() / 2));
					player.addHP(gainhp);
				}
				
				if (!monster.isControllerHasAggro()) {
					if (monster.getController() == player) {
						monster.setControllerHasAggro(true);
					} else {
						monster.switchController(player, true);
					}
				}
				// only ds, sb, assaulter, normal (does it work for thieves, bs, or assasinate?)
				if ((attack.skill == 4001334 || attack.skill == 4201005 || attack.skill == 0 || attack.skill == 4211002 || attack.skill == 4211004) &&
					player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
					handlePickPocket(player, monster, oned);
				}
				if (attack.skill == 4101005) { // drain
					ISkill drain = SkillFactory.getSkill(4101005);
					int gainhp = (int) ((double) totDamageToOneMonster *
						(double) drain.getEffect(player.getSkillLevel(drain)).getX() / 100.0);
					gainhp = Math.min(monster.getMaxHp(), Math.min(gainhp, player.getMaxHp() / 2));
					player.addHP(gainhp);
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
					int[] charges = new int[] { 1211005, 1211006 };
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
				if (player.getSkillLevel(venomNL) <= 0) {
					venomNL = SkillFactory.getSkill(14110004); //Night walker venom
				}
				ISkill venomShadower = SkillFactory.getSkill(4220005);
				if (player.getSkillLevel(venomNL) > 0) {
					MapleStatEffect venomEffect = venomNL.getEffect(player.getSkillLevel(venomNL));
					for (int i = 0; i < attackCount; i++) {
						if (venomEffect.makeChanceResult() == true) {
							if (monster.getVenomMulti() < 3) {
								monster.setVenomMulti((monster.getVenomMulti()+1));
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), venomNL, false);
								monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);    
							}
						}
					}
				} else if (player.getSkillLevel(venomShadower) > 0) {
					MapleStatEffect venomEffect = venomShadower.getEffect(player.getSkillLevel(venomShadower));
					for (int i = 0; i < attackCount; i++) {
						if (venomEffect.makeChanceResult() == true) {
							if (monster.getVenomMulti() < 3) {
								monster.setVenomMulti((monster.getVenomMulti()+1));
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), venomShadower, false);
								monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);    
							}
						}
					}
				}
				if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
					if (attackEffect.makeChanceResult()) {
						MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, false);
						monster.applyStatus(player, monsterStatusEffect, attackEffect.isPoison(), attackEffect.getDuration());
					}
				}
				if (attack.isHH && !monster.isBoss()) {
					map.damageMonster(player, monster, monster.getHp() - 1);
				} else if (attack.isHH && monster.isBoss()) {
					IItem weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
					@SuppressWarnings("unused")
					MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
					// TODO: Find out HH's damage formula. Vana people seem to think its Range * Damage %
					//	    So... how do I calculate range?
				} else {
					map.damageMonster(player, monster, totDamageToOneMonster);
				}
			}
		}
		if (totDamage > 1) {
			player.getCheatTracker().setAttacksWithoutHit(player.getCheatTracker().getAttacksWithoutHit() + 1);
			final int offenseLimit;
			if (attack.skill != 3121004) {
				offenseLimit = 100;
			} else {
				offenseLimit = 300;				
			}
			if (player.getCheatTracker().getAttacksWithoutHit() > offenseLimit) {
				player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT,
					Integer.toString(player.getCheatTracker().getAttacksWithoutHit()));
			}
		}
	}

	private void handlePickPocket(MapleCharacter player, MapleMonster monster, Pair<Integer, List<Integer>> oned) {
		ISkill pickpocket = SkillFactory.getSkill(4211003);
		int delay = 0;
		int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
		int reqdamage = 20000;
		Point monsterPosition = monster.getPosition();
		
		for (Integer eachd : oned.getRight()) {
			if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
				double perc = (double) eachd / (double) reqdamage;

				final int todrop = Math.min((int) Math.max(perc * (double) maxmeso, (double) 1),
					maxmeso);
				final MapleMap tdmap = player.getMap();
				final Point tdpos = new Point((int) (monsterPosition.getX() + (Math.random() * 100) - 50),
											  (int) (monsterPosition.getY()));
				final MapleMonster tdmob = monster;
				final MapleCharacter tdchar = player;

				TimerManager.getInstance().schedule(new Runnable() {
					public void run() {
						tdmap.spawnMesoDrop(todrop, todrop, tdpos, tdmob, tdchar, false);
					}
				}, delay);

				delay += 200;
			}
		}
	}

	private void checkHighDamage(MapleCharacter player, MapleMonster monster, AttackInfo attack, ISkill theSkill,
								MapleStatEffect attackEffect, int damageToMonster, int maximumDamageToMonster) {
		int elementalMaxDamagePerMonster;
		Element element = Element.NEUTRAL;
		if (theSkill != null) {
			element = theSkill.getElement();
			int skillId = theSkill.getId();
			if (skillId == 3221007) {
				maximumDamageToMonster = 99999;
			} else if (skillId == 4221001) {
				maximumDamageToMonster = 400000;
			}
		}
		if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
			int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);
			switch (chargeSkillId) {
				case 1211003:
				case 1211004:
					element = Element.FIRE;
					break;
				case 1211005:
				case 1211006:
					element = Element.ICE;
					break;
				case 1211007:
				case 1211008:
					element = Element.LIGHTING;
					break;
				case 1221003:
				case 1221004:
					element = Element.HOLY;
					break;
			}
			ISkill chargeSkill = SkillFactory.getSkill(chargeSkillId);
			maximumDamageToMonster *= chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getDamage() / 100.0;
		}
		if (element != Element.NEUTRAL) {
			double elementalEffect;
			if (attack.skill == 3211003 || attack.skill == 3111003) { // inferno and blizzard
				elementalEffect = attackEffect.getX() / 200.0;
			} else {
				elementalEffect = 0.5;
			}
			switch (monster.getEffectiveness(element)) {
				case IMMUNE:
					elementalMaxDamagePerMonster = 1;
					break;
				case NORMAL:
					elementalMaxDamagePerMonster = maximumDamageToMonster;
					break;
				case WEAK:
					elementalMaxDamagePerMonster = (int) (maximumDamageToMonster * (1.0 + elementalEffect));
					break;
				case STRONG:
					elementalMaxDamagePerMonster = (int) (maximumDamageToMonster * (1.0 - elementalEffect));
					break;
				default:
					throw new RuntimeException("Unknown enum constant");
			}
		} else {
			elementalMaxDamagePerMonster = maximumDamageToMonster;
		}
		
		if (damageToMonster > elementalMaxDamagePerMonster) {
			player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE);
			// log.info("[h4x] Player {} is doing high damage to one monster: {} (maxdamage: {}, skill:
			// {})",
			// new Object[] { player.getName(), Integer.valueOf(totDamageToOneMonster),
			// Integer.valueOf(maxDamagePerMonster), Integer.valueOf(attack.skill) });
			if (attack.skill != 1009 && attack.skill != 10001009) { //bamboo skills damage 30% of the monsters hp
				if (damageToMonster > elementalMaxDamagePerMonster * 3) { // * 3 until implementation of lagsafe pingchecks for buff expiration
					AutobanManager.getInstance().autoban(player.getClient(), damageToMonster +
						" damage (level: " + player.getLevel() + " watk: " + player.getTotalWatk() +
						" skill: " + attack.skill + ", monster: " + monster.getId() + " assumed max damage: " +
						elementalMaxDamagePerMonster + ")");
				}
			} else { //probably could be shortened, don't care though
				int maxDamage = (int)Math.floor(monster.getMaxHp() * 0.3);
				if (damageToMonster > maxDamage) {
					AutobanManager.getInstance().autoban(player.getClient(), damageToMonster +
							" damage (level: " + player.getLevel() + " watk: " + player.getTotalWatk() +
							" skill: " + attack.skill + ", monster: " + monster.getId() + " assumed max damage: " +
							maxDamage + ")");
				}
			}
		}
	}

	public AttackInfo parseDamage(LittleEndianAccessor lea, boolean ranged) {
    	// TODO we need information if an attack was a crit or not but it does not seem to be in this packet - find out
		// if it is o.o
		// noncrit strafe
		// 24 00
		// 01
		// 14
		// FE FE 30 00
		// 00
		// 97
		// 04 06 99 2F EE 00 04 00 00 00 41
		// 6B 00 00 00
		// 06 81 00 01 00 00 5F 00 00 00 5F 00 D2 02
		// A3 19 00 00 43 0C 00 00 AD 0B 00 00 DB 12 00 00 64 00 5F 00
		//
		// fullcrit strafe:
		// 24 00 01 14 FE FE 30 00 00 97 04 06 F5 C3 EE 00 04 00 00 00 41
		// 6B 00 00 00
		// 06 81 00 01 00 00 5F 00 00 00 5F 00 D2 02
		// 6E 0F 00 00 EA 12 00 00 58 15 00 00 56 11 00 00 64 00 5F 00
		
		AttackInfo ret = new AttackInfo();
		
		lea.readByte();
		ret.numAttackedAndDamage = lea.readByte();
		ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF; // guess why there are no skills damaging more than
		// 15 monsters...
		ret.numDamage = ret.numAttackedAndDamage & 0xF; // how often each single monster was attacked o.o
		ret.allDamage = new ArrayList<Pair<Integer, List<Integer>>>();
		ret.skill = lea.readInt();
		
		if (ret.skill == 2121001 || ret.skill == 2221001 || ret.skill == 2321001 || ret.skill == 5201002 || ret.skill == 14111006 ||
				ret.skill == 5101004 || ret.skill == 15101003) {
			ret.charge = lea.readInt();
		} else {
			ret.charge = 0;
		}
		if (ret.skill == 1221011) {
			ret.isHH = true;
		}
		
		lea.skip(4); // Added on v.79 MSEA and v.74 GlobalMS
		lea.skip(4); // added v66+?
		lea.readByte(); // always 0 (?)
		ret.stance = lea.readByte();
		
		if (ret.skill == 4211006) {
			System.out.println("\nPacket recieved: \n" + lea.toString() + "\n");
			return parseMesoExplosion(lea, ret);
		}

		if (ranged) {
			lea.readByte();
			ret.speed = lea.readByte();
			lea.readByte();
			ret.direction = lea.readByte(); // contains direction on some 4th job skills
			lea.skip(7);
			// hurricane and pierce have extra 4 bytes :/
			if (ret.skill == 3121004 || ret.skill == 3221001 || ret.skill == 5221004 || ret.skill == 13111002) {
				lea.skip(4);
			}
		} else {
			lea.readByte();
			ret.speed = lea.readByte();
			lea.skip(4);
			if (ret.skill == 5201002) {
				lea.skip(4);
			}
		}

		for (int i = 0; i < ret.numAttacked; i++) {
			int oid = lea.readInt();
			lea.skip(14); // seems to contain some position info o.o

			List<Integer> allDamageNumbers = new ArrayList<Integer>();
			for (int j = 0; j < ret.numDamage; j++) {
				int damage = lea.readInt();
				allDamageNumbers.add(Integer.valueOf(damage));
			}
			if (ret.skill != 5221004) {
				lea.skip(4);
			}
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
		
		} else {
		        lea.skip(6);
		}
		
		for (int i = 0; i < ret.numAttacked + 1; i++) {
		
		        int oid = lea.readInt();
		
		        if (i < ret.numAttacked) {
		                lea.skip(12);
		                int bullets = lea.readByte();
		
		                List<Integer> allDamageNumbers = new ArrayList<Integer>();
		                for (int j = 0; j < bullets; j++) {
		                        int damage = lea.readInt();
		                        // System.out.println("Damage: " + damage);
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
