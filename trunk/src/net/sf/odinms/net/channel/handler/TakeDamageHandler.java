package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.constants.Skills;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MobAttackInfo;
import net.sf.odinms.server.life.MobAttackInfoFactory;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;

public class TakeDamageHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter player = c.getPlayer();
		slea.readInt();
		int damagefrom = slea.readByte();
		slea.readByte();
		int damage = slea.readInt();
		int oid = 0;
		int monsteridfrom = 0;
		int pgmr = 0;
		int direction = 0;
		int pos_x = 0;
		int pos_y = 0;
		int fake = 0;
		boolean is_pgmr = false;
		boolean is_pg = true;
		int mpattack = 0;
		MapleMonster attacker = null;
		if (damagefrom != -2) {
			monsteridfrom = slea.readInt();
			oid = slea.readInt();
			attacker = (MapleMonster) player.getMap().getMapObject(oid);
			direction = slea.readByte();
		}

		if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
			MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
			if (attackInfo.isDeadlyAttack()) {
				mpattack = player.getMp() - 1;
			}
			mpattack += attackInfo.getMpBurn();
			MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
			if (skill != null && damage > 0) {
				skill.applyEffect(player, attacker, false);
			}
			if (attacker != null) {
				attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
			}
		}
		if (damage == -1) {
			int job = (int) (player.getJob().getId() / 10 - 40);
			fake = 4020002 + (job * 100000);
		}
		if (damage < -1 || damage > 60000) {
			AutobanManager.getInstance().addPoints(c, 1000, 60000, "Taking abnormal amounts of damge from " + monsteridfrom + ": " + damage);
			return;
		}
		player.getCheatTracker().checkTakeDamage();
		if (damage > 0) {
			player.getCheatTracker().setAttacksWithoutHit(0);
			player.getCheatTracker().resetHPRegen();
		}
		if (damage > 0 && !player.isHidden()) {
                    if (MapleLifeFactory.getMonster(monsteridfrom) != null) {
			if (damagefrom == -1) {
				Integer pguard = player.getBuffedValue(MapleBuffStat.POWERGUARD);
				if (pguard != null) {
					// why do we have to do this? -.- the client shows the damage...
					attacker = (MapleMonster) player.getMap().getMapObject(oid);
					if (attacker != null && !attacker.isBoss()) {
						int bouncedamage = (int) (damage * (pguard.doubleValue() / 100));
						bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
						player.getMap().damageMonster(player, attacker, bouncedamage);
						damage -= bouncedamage;
						player.getMap().broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), false, true);
					}
				}
			}
			if (damagefrom != -2) {
				Integer achilles = 0;
				ISkill achilles1 = null;
				switch (player.getJob().getId()) {
					case 112:
						achilles = player.getSkillLevel(SkillFactory.getSkill(1120004));
						achilles1 = SkillFactory.getSkill(1120004);
						break;
					case 122:
						achilles = player.getSkillLevel(SkillFactory.getSkill(1220005));
						achilles1 = SkillFactory.getSkill(1220005);
						break;
					case 132:
						achilles = player.getSkillLevel(SkillFactory.getSkill(1320005));
						achilles1 = SkillFactory.getSkill(1320005);
						break;
				}
				if (achilles != 0) {
					int x = achilles1.getEffect(achilles).getX();
					double multiplier = x / 1000.0;
					int newdamage = (int) (multiplier * damage);
					damage = newdamage;
				}
			}
			Integer mguard = player.getBuffedValue(MapleBuffStat.MAGIC_GUARD);
			Integer mesoguard = player.getBuffedValue(MapleBuffStat.MESOGUARD);
			if (mguard != null && mpattack == 0) {
				int mploss = (int) (damage * (mguard.doubleValue() / 100.0));
				int hploss = damage - mploss;
				if (mploss > player.getMp()) {
					hploss += mploss - player.getMp();
					mploss = player.getMp();
				}
				player.addMPHP(-hploss, -mploss);
			} else if (mesoguard != null) {
				damage = (damage % 2 == 0) ? damage / 2 : (damage / 2) + 1;
				int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
				if (player.getMeso() < mesoloss) {
					player.gainMeso(-player.getMeso(), false);
					player.cancelBuffStats(MapleBuffStat.MESOGUARD);
				} else {
					player.gainMeso(-mesoloss, false);
				}
				player.addMPHP(-damage, -mpattack);
			} else {
				player.addMPHP(-damage, -mpattack);
			}
			Integer battleship = player.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
			if (battleship != null) {
				if (battleship.intValue() == Skills.Corsair.Battleship) {
					player.decreaseBattleshipHp(damage);
				}
			}
                        } else {
                        new ServernoticeMapleClientMessageCallback(6, c).dropMessage("No packet editing my dear");
                        }
		}

		if (!player.isHidden()) {
			player.getMap().broadcastMessage(player, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, player.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
		}
                if (player.getMap().getId() >= 925020000 && player.getMap().getId() < 925030000) {
                    player.setDojoEnergy(player.isGM() ? 300 : player.getDojoEnergy() < 300 ? player.getDojoEnergy() + 1 : 0);
                    player.getClient().getSession().write(MaplePacketCreator.getEnergy(player.getDojoEnergy()));
                }
	}
}
