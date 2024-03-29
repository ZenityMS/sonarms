package net.sf.odinms.net.channel.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacter.CancelCooldownAction;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class MagicDamageHandler extends AbstractDealDamageHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		//attack air
		//23 00 03 01 00 00 00 00 00 90 01 04 DB 82 A9 00 FB FC D7 00
		//attack air
		//25 00 03 01 BE BC 21 00 00 2F 06 06 A1 1B 66 01 00 00 5F 00

		AttackInfo attack = parseDamage(slea, false);
		MapleCharacter player = c.getPlayer();

		MaplePacket packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance,
			attack.numAttackedAndDamage, attack.allDamage, -1, attack.speed);
			if (attack.skill == 2121001 || attack.skill == 2221001 || attack.skill == 2321001) {
				packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance,
					attack.numAttackedAndDamage, attack.allDamage, attack.charge, attack.speed);
			}
		player.getMap().broadcastMessage(player, packet, false, true);

		MapleStatEffect effect = attack.getAttackEffect(c.getPlayer());
		int maxdamage;

		// TODO fix magic damage calculation
		maxdamage = 99999;

		ISkill skill = SkillFactory.getSkill(attack.skill);
		int skillLevel = c.getPlayer().getSkillLevel(skill);
		MapleStatEffect effect_ = skill.getEffect(skillLevel);
		if (effect_.getCooldown() > 0) {
			c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
			ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), attack.skill), effect_.getCooldown() * 1000);
			c.getPlayer().addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, timer);
		}

		applyAttack(attack, player, maxdamage, effect.getAttackCount());

		// MP Eater
		for (int i = 1; i <= 3; i++) {
			ISkill eaterSkill = SkillFactory.getSkill(2000000 + i * 100000);
			int eaterLevel = player.getSkillLevel(eaterSkill);
			if (eaterLevel > 0) {
				for (Pair<Integer, List<Integer>> singleDamage : attack.allDamage) {
					eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage.getLeft()), 0);
				}
				break;
			}
		}
	}
}
