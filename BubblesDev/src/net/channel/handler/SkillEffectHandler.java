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

import client.MapleClient;
import constants.SkillConstants;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SkillEffectHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int skillId = slea.readInt();
        int level = slea.readByte();
        byte flags = slea.readByte();
        int speed = slea.readByte();
        switch (skillId) {
            case SkillConstants.FPMage.Explosion:
            case SkillConstants.FPArchMage.BigBang:
            case SkillConstants.ILArchMage.BigBang:
            case SkillConstants.Bishop.BigBang:
            case SkillConstants.Bowmaster.Hurricane:
            case SkillConstants.Marksman.PiercingArrow:
            case SkillConstants.ChiefBandit.Chakra:
            case SkillConstants.Brawler.CorkscrewBlow:
            case SkillConstants.Gunslinger.Grenade:
            case SkillConstants.Corsair.RapidFire:
            case SkillConstants.WindArcher.Hurricane:
            case SkillConstants.NightWalker.PoisonBomb:
            case SkillConstants.ThunderBreaker.CorkscrewBlow:
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillEffect(c.getPlayer(), skillId, level, flags, speed), false);
                break;
            default:
                System.out.println(c.getPlayer() + " entered SkillEffectHandler without being handled.");
                return;
        }
    }
}
