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
import java.util.Collection;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.CharacterIdChannelPair;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerLoggedinHandler extends AbstractMaplePacketHandler {
    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
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
                c.getSession().close();
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        }
        ChannelServer cserv = c.getChannelServer();
        cserv.addPlayer(player);
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM CoolDowns WHERE charid = ?");
            ps.setInt(1, c.getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() < 1)
                    continue;
                if (rs.getInt("SkillID") != 4111002)
                    c.getPlayer().giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
            }
            ps = con.prepareStatement("DELETE FROM CoolDowns WHERE charid = ?");
            ps.setInt(1, c.getPlayer().getId());
            ps.executeUpdate();
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
        c.getSession().write(MaplePacketCreator.getCharInfo(player));
        if (player.gmLevel() > 0) {
            int[] skills = {4201003, 2301004, 1301007, 2311003, 9101004};
            for (int i : skills)
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
        }
        player.sendKeymap();
        c.getPlayer().sendMacros();
        player.getMap().addPlayer(player);
        try {
            Collection<BuddylistEntry> buddies = player.getBuddylist().getBuddies();
            int buddyIds[] = player.getBuddylist().getBuddyIds();
            cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            if (player.getParty() != null)
                channelServer.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            CharacterIdChannelPair[] onlineBuddies = cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                player.getBuddylist().put(ble);
            }
            c.getSession().write(MaplePacketCreator.updateBuddylist(buddies));
        } catch (Exception e) {
            channelServer.reconnectWorld();
        }
        if (player.getGuildId() > 0)
            try {
                c.getChannelServer().getWorldInterface().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(MaplePacketCreator.showGuildInfo(player));
            } catch (Exception e) {
                channelServer.reconnectWorld();
            }
        try {
            c.getPlayer().showNote();
        } catch (Exception e) {
        }
        player.updatePartyMemberHP();
        for (MapleQuestStatus status : player.getStartedQuests())
            if (status.hasMobKills())
                c.getSession().write(MaplePacketCreator.updateQuestMobKills(status));
        CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), pendingBuddyRequest.getId(), -1, false));
            c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getName()));
        }
        c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        c.getSession().write(MaplePacketCreator.updateGender(player));
        player.checkMessenger();
        player.checkBerserk();
    }
}