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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.SkillFactory;
import constants.SkillConstants;
import java.util.List;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.CharacterIdChannelPair;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.guild.MapleAlliance;
import net.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {
    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int cid = slea.readInt();
        MapleCharacter player = null;
        try {
            player = MapleCharacter.loadCharFromDB(cid, c, true);
            c.setPlayer(player);
        } catch (Exception e) {
        }
        c.setAccID(player.getAccountID());
        int state = c.getLoginState();
        boolean allowLogin = true;
        ChannelServer channelServer = c.getChannelServer();
        synchronized (this) {
            try {
                WorldChannelInterface worldInterface = channelServer.getWorldInterface();
                if (state == MapleClient.LOGIN_SERVER_TRANSITION)
                    for (String charName : c.loadCharacterNames(c.getWorld()))
                        if (worldInterface.isConnected(charName)) {
                            allowLogin = false;
                            break;
                        }
            } catch (RemoteException e) {
                channelServer.reconnectWorld();
                allowLogin = false;
            }
            if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                c.setPlayer(null);
                c.getSession().close(true);
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        }
        ChannelServer cserv = c.getChannelServer();
        cserv.addPlayer(player);
        try {
            List<PlayerBuffValueHolder> buffs = ChannelServer.getInstance(c.getChannel()).getWorldInterface().getBuffsFromStorage(cid);
            if (buffs != null)
                c.getPlayer().silentGiveBuffs(buffs);
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
            ps.setInt(1, c.getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int skillid = rs.getInt("SkillID");
                final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                if (length + startTime < System.currentTimeMillis())
                    continue;
                c.getPlayer().giveCoolDowns(skillid, startTime, length);
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
            ps.setInt(1, c.getPlayer().getId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, c.getPlayer().getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                Connection x = DatabaseConnection.getConnection();
                try {
                    PreparedStatement pss = x.prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
                    pss.setInt(1, c.getPlayer().getId());
                    pss.executeUpdate();
                    pss.close();
                } catch (Exception e) {
                }
                c.getSession().write(MaplePacketCreator.sendDueyMSG((byte) 0x1B));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        c.getSession().write(MaplePacketCreator.getCharInfo(player));
        if (player.isGM())
            SkillFactory.getSkill(SkillConstants.SuperGm.Hide).getEffect(SkillFactory.getSkill(SkillConstants.SuperGm.Hide).getMaxLevel()).applyTo(player);
        player.sendKeymap();
        c.getPlayer().sendMacros();
        player.getMap().addPlayer(player);
        try {
            int buddyIds[] = player.getBuddylist().getBuddyIds();
            cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds)) {
                BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                player.getBuddylist().put(ble);
            }
            c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        } catch (Exception e) {
            channelServer.reconnectWorld();
        }
        c.getSession().write(MaplePacketCreator.loadFamily(player));
        if (player.getFamilyId() > 0)
            c.getSession().write(MaplePacketCreator.getFamilyInfo(player));
        if (player.getGuildId() > 0)
            try {
                c.getChannelServer().getWorldInterface().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(MaplePacketCreator.showGuildInfo(player));
                int allianceId = player.getGuild().getAllianceId();
                if (allianceId > 0) {
                    MapleAlliance newAlliance = channelServer.getWorldInterface().getAlliance(allianceId);
                    if (newAlliance == null) {
                        newAlliance = MapleAlliance.loadAlliance(allianceId);
                        channelServer.getWorldInterface().addAlliance(allianceId, newAlliance);
                    }
                    try {
                        c.getSession().write(MaplePacketCreator.getAllianceInfo(newAlliance));
                        c.getSession().write(MaplePacketCreator.getGuildAlliances(newAlliance, c));
                        c.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, true), player.getId(), -1);
                    } catch (Exception e) {
                    }
                }
                c.getPlayer().showNote();
                if (player.getParty() != null)
                    channelServer.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
                player.updatePartyMemberHP();
            } catch (Exception e) {
                e.printStackTrace();
                channelServer.reconnectWorld();
            }
        for (MapleQuestStatus status : player.getStartedQuests())
            if (status.hasMobKills())
                c.getSession().write(MaplePacketCreator.updateQuestMobKills(status));
        CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), "Default Group", pendingBuddyRequest.getId(), -1, false));
            c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
        c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        c.getSession().write(MaplePacketCreator.updateGender(player));
        player.checkMessenger();
        c.getSession().write(MaplePacketCreator.enableReport());
        if (player.getLevel() > 19 && !player.isCygnus() && player.getCygnusLinkId() == 0 && !player.hasWatchedCygnusIntro()) {
            player.startCygnusIntro();
            player.setWatchedCygnusIntro(true);
        }
        if (player.isCygnus()) // KoC char
            player.changeSkillLevel(SkillFactory.getSkill(SkillConstants.Noblesse.BlessingOfTheFairy), player.getLinkedLevel() / 10, 20);
        player.checkBerserk();
        player.expirationTask();
    }
}
