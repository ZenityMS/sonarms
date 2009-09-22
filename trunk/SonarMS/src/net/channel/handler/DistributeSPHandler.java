package net.channel.handler;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeSPHandler extends AbstractMaplePacketHandler {
    private class SP {
        private int id;
        private MapleClient c;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public MapleClient getClient() {
            return c;
        }

        public void setClient(MapleClient c) {
            this.c = c;
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        SP sp = new SP();
        sp.setClient(c);
        sp.setId(slea.readInt());
        int skillid = sp.getId();
        MapleCharacter player = sp.getClient().getPlayer();
        int remainingSp = player.getRemainingSp();
        boolean isBeginnerSkill = false;
        if (skillid == 1000 || skillid == 1001 || skillid == 1002) { // beginner skill
            remainingSp = Math.min((player.getLevel() - 1), player.getClient().getChannelServer().getSkillMaxing() ? 9 : 6);
            for (int i = 1000; i < 1003; i++)
                remainingSp -= player.getSkillLevel(SkillFactory.getSkill(i));
            isBeginnerSkill = true;
        }
        ISkill skill = SkillFactory.getSkill(skillid);
        int curLevel = player.getSkillLevel(skill);
        if ((remainingSp > 0 && curLevel + 1 <= (skill.isFourthJob() ? player.getMasterLevel(skill) : skill.getMaxLevel())) && skill.canBeLearnedBy(player.getJob())) {
            if (!isBeginnerSkill)
                player.setRemainingSp(player.getRemainingSp() - 1);
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
            player.changeSkillLevel(skill, curLevel + 1, player.getMasterLevel(skill));
        }
    }
}