package net.channel.handler;

import client.ISkill;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeAPHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int update = slea.readInt();
        if (c.getPlayer().getRemainingAp() > 0) {
            int max = c.getChannelServer().getMaxStat();
            switch (update) {
                case 64: // Str
                    if (c.getPlayer().getStr() >= max)
                        return;
                    c.getPlayer().addStat(1,1);
                    break;
                case 128: // Dex
                    if (c.getPlayer().getDex() >= max)
                        return;
                    c.getPlayer().addStat(2,1);
                    break;
                case 256: // Int
                    if (c.getPlayer().getInt() >= max)
                        return;
                    c.getPlayer().addStat(3, 1);
                    break;
                case 512: // Luk
                    if (c.getPlayer().getLuk() >= max)
                        return;
                    c.getPlayer().addStat(4, 1);
                    break;
                case 2048: // HP
                    addHP(c);
                    break;
                case 8192: // MP
                    addMP(c);
                    break;
                default:
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                    return;
            }
            c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void addHP(MapleClient c) {
        int MaxHP = c.getPlayer().getMaxHp();
        if (c.getPlayer().getHpApUsed() > 9999 || MaxHP >= 30000)
            return;
        ISkill improvingMaxHP = null;
        int improvingMaxHPLevel = 0;
        if (c.getPlayer().getJob().isA(MapleJob.BEGINNER))
            MaxHP += rand(8, 12);
        else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
            improvingMaxHP = SkillFactory.getSkill(1000001);
            improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            if (improvingMaxHPLevel > 0)
                MaxHP += rand(20, 24) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            else
                MaxHP += rand(20, 24);
        } else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN))
            MaxHP += rand(6, 10);
        else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN))
            MaxHP += rand(16, 20);
        else if (c.getPlayer().getJob().isA(MapleJob.THIEF))
            MaxHP += rand(20, 24);
        else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
            improvingMaxHP = SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            if (improvingMaxHPLevel > 0)
                MaxHP += rand(16, 20) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            else
                MaxHP += rand(16, 20);
        }
        MaxHP = Math.min(30000, MaxHP);
        c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() + 1);
        c.getPlayer().setMaxHp(MaxHP);
        c.getPlayer().updateSingleStat(MapleStat.MAXHP, MaxHP);
    }

    public static void addMP(MapleClient c) {
        int MaxMP = c.getPlayer().getMaxMp();
        if (c.getPlayer().getMpApUsed() == 10000 || c.getPlayer().getMaxMp() == 30000)
            return;
        if (c.getPlayer().getJob().isA(MapleJob.BEGINNER))
            MaxMP += rand(6, 8);
        else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR))
            MaxMP += rand(2, 4);
        else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
            ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
            int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
            if (improvingMaxMPLevel > 0)
                MaxMP += rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
            else
                MaxMP += rand(18, 20);
        } else if (c.getPlayer().getJob().getId() > 299)
            MaxMP += rand(10, 12);
        MaxMP = Math.min(30000, MaxMP);
        c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() + 1);
        c.getPlayer().setMaxMp(MaxMP);
        c.getPlayer().updateSingleStat(MapleStat.MAXMP, MaxMP);
    }

    private static int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }
}
