/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.sf.odinms.net.channel.handler;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import net.sf.odinms.client.BuddylistEntry;
import net.sf.odinms.client.CharacterNameAndId;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.CharacterIdChannelPair;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.PlayerBuffValueHolder;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerLoggedinHandler extends AbstractMaplePacketHandler {
	private static final Logger log = LoggerFactory.getLogger(PlayerLoggedinHandler.class);

	@Override
	public boolean validateState(MapleClient c) {
		return !c.isLoggedIn();
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int cid = slea.readInt();
		MapleCharacter player = null;
		try {
			player = MapleCharacter.loadCharFromDB(cid, c, true);
			c.setPlayer(player);
		} catch (SQLException e) {
			log.error("Loading the char failed", e);
		}
		c.setAccID(player.getAccountID());
		int state = c.getLoginState();
		boolean allowLogin = true;
		ChannelServer channelServer = c.getChannelServer();
		synchronized (this) {
			try {
				WorldChannelInterface worldInterface = channelServer.getWorldInterface();
				if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
					for (String charName : c.loadCharacterNames(c.getWorld())) {
						if (worldInterface.isConnected(charName)) {
							log.warn(MapleClient.getLogMessage(player, "Attempting to doublelogin with " + charName));
							allowLogin = false;
							break;
						}
					}
				}
			} catch (RemoteException e) {
				channelServer.reconnectWorld();
				allowLogin = false;
			}
			if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
				c.setPlayer(null); //REALLY prevent the character from getting deregistered as it is not registered
				c.getSession().close();
				return;
			}
			c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
		}
		
		ChannelServer cserv = ChannelServer.getInstance(c.getChannel());
		cserv.addPlayer(player);
		
		try {
			WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
			List<PlayerBuffValueHolder> buffs = wci.getBuffsFromStorage(cid);
			if (buffs != null) {
				c.getPlayer().silentGiveBuffs(buffs);
			}
			List<PlayerCoolDownValueHolder> cooldowns = wci.getCooldownsFromStorage(cid);
			if (cooldowns != null) {
				c.getPlayer().giveCoolDowns(cooldowns);
			}
		} catch (RemoteException e) {
			c.getChannelServer().reconnectWorld();
		}
		
		c.getSession().write(MaplePacketCreator.getCharInfo(player));
		
		player.getMap().addPlayer(player);
		//c.getSession().write(MaplePacketCreator.getFamilyData(c));
		try {
			Collection<BuddylistEntry> buddies = player.getBuddylist().getBuddies();
			int buddyIds[] = player.getBuddylist().getBuddyIds();
			
			cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
			if (player.getParty() != null) {
				channelServer.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
			}
			
			CharacterIdChannelPair[] onlineBuddies = cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds);
			for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
				BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
				ble.setChannel(onlineBuddy.getChannel());
				player.getBuddylist().put(ble);
			}
			c.getSession().write(MaplePacketCreator.updateBuddylist(buddies));
			
			c.getPlayer().sendMacros();
			
			try {
				c.getPlayer().showNote();
			} catch (SQLException e) {log.error("LOADING NOTE", e);}
			
			if (player.getGuildId() > 0)
			{
				c.getChannelServer().getWorldInterface().setGuildMemberOnline(
						player.getMGC(), true, c.getChannel());
				c.getSession().write(MaplePacketCreator.showGuildInfo(player));
			}
		} catch (RemoteException e) {
			log.info("REMOTE THROW", e);
			channelServer.reconnectWorld();
		}
		player.updatePartyMemberHP();
		player.sendKeymap();
		
		//c.getSession().write(MaplePacketCreator.weirdStatUpdate());
		
		for (MapleQuestStatus status : player.getStartedQuests()) {
			if (status.hasMobKills()) {
				c.getSession().write(MaplePacketCreator.updateQuestMobKills(status));
			}
		}
		
		CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
		if (pendingBuddyRequest != null) {
			player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), "Default Group", pendingBuddyRequest.getId(), -1, false));
			c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
		}
        player.checkMessenger();
        
        //Enable the reporting system
        c.getSession().write(MaplePacketCreator.enableReport(true));
        
        //Check to see if they can do ~CyGnUs~
		if (player.getLevel() >= 20 && !player.getJob().isA(MapleJob.NOBLESSE) && player.getCygnusLinkId() == 0
				&& !player.hasWatchedCygnusIntro()) {
			player.startCygnusIntro();
			player.setWatchedCygnusIntro(true);
		}
	}
}
