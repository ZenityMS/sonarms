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

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import client.ISkill;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import constants.SkillConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SpecialMoveHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int skillid = slea.readInt();
        Point pos = null;
        int __skillLevel = slea.readByte();
        ISkill skill = SkillFactory.getSkill(skillid);
        int skillLevel = c.getPlayer().getSkillLevel(skill);
        if (skillid == 1010 || skillid == 1011 || skillid == 10001010 || skillid == 10001011) {
            skillLevel = 1;
            c.getPlayer().setDojoEnergy(0);
            c.getSession().write(MaplePacketCreator.getEnergy(0));
        }
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0)
            if (c.getPlayer().skillisCooling(skillid))
                return;
            else if (skillid != 5221006) {
                c.getSession().write(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
                ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), skillid), effect.getCooldown() * 1000);
                c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
            }
        if (skillid == SkillConstants.Hero.MonsterMagnet || skillid == SkillConstants.Paladin.MonsterMagnet || skillid == SkillConstants.DarkKnight.MonsterMagnet) { // Monster Magnet
            int num = slea.readInt();
            int mobId;
            byte success;
            for (int i = 0; i < num; i++) {
                mobId = slea.readInt();
                success = slea.readByte();
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showMagnet(mobId, success), false);
                MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(mobId);
                if (monster != null)
                    monster.switchController(c.getPlayer(), monster.isControllerHasAggro());
            }
            byte direction = slea.readByte();
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showBuffeffect(c.getPlayer().getId(), skillid, 1, direction), false);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        } else if (skillid == SkillConstants.Buccaneer.TimeLeap) { // Timeleap
            MapleParty p = c.getPlayer().getParty();
            if (p != null)
                for (MaplePartyCharacter mpc : p.getMembers())
                    for (ChannelServer cserv : ChannelServer.getAllInstances())
                        if (cserv.getPlayerStorage().getCharacterById(mpc.getId()) != null)
                            cserv.getPlayerStorage().getCharacterById(mpc.getId()).removeAllCooldownsExcept(5121010);
            c.getPlayer().removeAllCooldownsExcept(SkillConstants.Buccaneer.TimeLeap);
        } else if (skillid == 5101005) {// MP Recovery
            ISkill s = SkillFactory.getSkill(5101005);
            MapleStatEffect ef = s.getEffect(c.getPlayer().getSkillLevel(s));
            int lose = (int) c.getPlayer().getMaxHp() / ef.getX();
            c.getPlayer().setHp(c.getPlayer().getHp() - lose);
            c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getHp());
            int gain = (int) lose * (ef.getY() / 100);
            c.getPlayer().setMp(c.getPlayer().getMp() + gain);
            c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getMp());
        } else if (skillid % 10000000 == 1004)
            slea.readShort();
        if (slea.available() == 5)
            pos = new Point(slea.readShort(), slea.readShort());
        if (skillLevel == 0 || skillLevel != __skillLevel)
            return;
        else if (c.getPlayer().isAlive())
            if (skill.getId() != SkillConstants.Priest.MysticDoor || c.getPlayer().canDoor())
                skill.getEffect(skillLevel).applyTo(c.getPlayer(), pos);
            else {
                c.getPlayer().message("Please wait 5 seconds before casting Mystic Door again");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        else
            c.getSession().write(MaplePacketCreator.enableActions());
    }
}
