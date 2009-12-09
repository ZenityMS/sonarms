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

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import client.ISkill;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.SkillFactory;
import net.MaplePacket;
import server.MapleStatEffect;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MagicDamageHandler extends AbstractDealDamageHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        AttackInfo attack = parseDamage(slea, false);
        MapleCharacter player = c.getPlayer();
        MaplePacket packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, -1, attack.speed);
        if (attack.skill == 2121001 || attack.skill == 2221001 || attack.skill == 2321001)
            packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.charge, attack.speed);
        player.getMap().broadcastMessage(player, packet, false, true);
        MapleStatEffect effect = attack.getAttackEffect(c.getPlayer());
        int maxdamage = 99999;
        ISkill skill = SkillFactory.getSkill(attack.skill);
        int skillLevel = c.getPlayer().getSkillLevel(skill);
        MapleStatEffect effect_ = skill.getEffect(skillLevel);
        if (effect_.getCooldown() > 0)
            if (player.skillisCooling(attack.skill)) {
                return;
            } else {
                c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), attack.skill), effect_.getCooldown() * 1000);
                player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, timer);
            }
        applyAttack(attack, player, maxdamage, effect.getAttackCount());
        // MP Eater
        for (int i = 1; i < 4; i++) {
            ISkill eaterSkill = SkillFactory.getSkill(2000000 + i * 100000);
            int eaterLevel = player.getSkillLevel(eaterSkill);
            if (eaterLevel > 0) {
                for (Pair<Integer, List<Integer>> singleDamage : attack.allDamage)
                    eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage.getLeft()), 0);
                break;
            }
        }
    }
}