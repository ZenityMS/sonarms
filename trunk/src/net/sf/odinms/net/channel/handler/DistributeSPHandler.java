
package net.sf.odinms.net.channel.handler;

import java.util.Map.Entry;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.server.constants.Skills;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributeSPHandler extends AbstractMaplePacketHandler {

	private static Logger log = LoggerFactory.getLogger(DistributeSPHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		int skillid = slea.readInt();
		boolean isBegginnerSkill = false;

		MapleCharacter player = c.getPlayer();
		int remainingSp = player.getRemainingSp();
		if (skillid == Skills.Beginner.ThreeSnails || skillid == Skills.Beginner.Recovery || skillid == Skills.Beginner.NimbleFeet) {
			int snailsLevel = player.getSkillLevel(SkillFactory.getSkill(Skills.Beginner.ThreeSnails));
			int recoveryLevel = player.getSkillLevel(SkillFactory.getSkill(Skills.Beginner.Recovery));
			int nimbleFeetLevel = player.getSkillLevel(SkillFactory.getSkill(Skills.Beginner.NimbleFeet));
			remainingSp = Math.min((player.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
			isBegginnerSkill = true;
		}
		ISkill skill = SkillFactory.getSkill(skillid);
		int maxlevel = skill.getMaxLevel();
		if (skill.isFourthJob()) {
			maxlevel = player.getMasterLevel(skill);
		}
		int curLevel = player.getSkillLevel(skill);
                if ((remainingSp > 0 && (curLevel + 1 <= maxlevel)) && skill.canBeLearnedBy(player.getJob())) {
			if (skill.hasRequiredSkillLevels()) {
				for (Entry<Integer, Integer> reqLevel : skill.getRequiredSkillLevels().entrySet()) {
					if (player.getSkillLevel(SkillFactory.getSkill(reqLevel.getKey())) < reqLevel.getValue()) {
						AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without pre-requisite (" + skillid + ":" + reqLevel.getKey() + ")");
						return;
					}
				}
			}
			if (!isBegginnerSkill) {
				player.setRemainingSp(player.getRemainingSp() - 1);
			}
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
			player.changeSkillLevel(skill, curLevel + 1, player.getMasterLevel(skill));
		} else if (!skill.canBeLearnedBy(player.getJob())) {
			AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill for a different job (" + player.getJob().name() + ":" + skillid + ")");
		} else if (!(remainingSp > 0 && curLevel + 1 <= maxlevel)) {
			log.info("[h4x] Player {} is distributing SP to {} without having any", player.getName(), Integer.valueOf(skillid));
		}
	}
}
