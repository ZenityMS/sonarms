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

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DistributeAPHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int num = slea.readInt();
        if (c.getPlayer().getRemainingAp() > 0) {
            addStat(c, num);
            c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    static void addStat(MapleClient c, int id) {
        int max = 999;
        switch (id) {
            case 64: // Str
                if (c.getPlayer().getStr() >= max)
                    return;
                c.getPlayer().addStat(1, 1);
                break;
            case 128: // Dex
                if (c.getPlayer().getDex() >= max)
                    return;
                c.getPlayer().addStat(2, 1);
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
                addHP(c.getPlayer(), addHP(c));
                break;
            case 8192: // MP
                addMP(c.getPlayer(), addMP(c));
                break;
            default:
                c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                return;
        }
    }

    static int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob job = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpApUsed() > 9999 || MaxHP >= 30000)
            return MaxHP;
        ISkill improvingMaxHP = null;
        int improvingMaxHPLevel = 0;
        if (job.isA(MapleJob.BEGINNER))
            MaxHP += rand(8, 12);
        else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001);
            improvingMaxHPLevel = player.getSkillLevel(improvingMaxHP);
            if (improvingMaxHPLevel > 0)
                MaxHP += rand(20, 24) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            else
                MaxHP += rand(20, 24);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1))
            MaxHP += rand(6, 10);
        else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1))
            MaxHP += rand(16, 20);
        else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1))
            MaxHP += rand(20, 24);
        else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = player.getSkillLevel(improvingMaxHP);
            if (improvingMaxHPLevel > 0)
                MaxHP += rand(16, 20) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            else
                MaxHP += rand(16, 20);
        }
        return MaxHP;
    }

    static int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getMpApUsed() == 10000 || player.getMaxMp() == 30000)
            return MaxMP;
        if (player.getJob().isA(MapleJob.BEGINNER) || player.isCygnus())
            MaxMP += rand(6, 8);
        else if (player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1))
            MaxMP += rand(2, 4);
        else if (player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)) {
            ISkill improvingMaxMP = player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001);
            int improvingMaxMPLevel = player.getSkillLevel(improvingMaxMP);
            if (improvingMaxMPLevel > 0)
                MaxMP += rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
            else
                MaxMP += rand(18, 20);
        } else if (player.getJob().getId() > 299) //surprisingly actually works
            MaxMP += rand(10, 12);
        return MaxMP;
    }

    static void addHP(MapleCharacter player, int amount) {
        amount = Math.min(30000, amount);
        player.setHpApUsed(player.getHpApUsed() + 1);
        player.setMaxHp(amount);
        player.updateSingleStat(MapleStat.MAXHP, amount);
    }

    static void addMP(MapleCharacter player, int MaxMP) {
        MaxMP = Math.min(30000, MaxMP);
        player.setMpApUsed(player.getMpApUsed() + 1);
        player.setMaxMp(MaxMP);
        player.updateSingleStat(MapleStat.MAXMP, MaxMP);
    }

    private static int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }
}
