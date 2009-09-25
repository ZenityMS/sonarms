package net.world.guild;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import client.MapleCharacter;
import client.MapleClient;
import tools.DatabaseConnection;
import net.MaplePacket;
import net.channel.pvp.MaplePvp;
import net.channel.ChannelServer;
import net.channel.remote.ChannelWorldInterface;
import net.world.WorldRegistryImpl;
import tools.MaplePacketCreator;

public class MapleGuild implements java.io.Serializable {
    public final static int CREATE_GUILD_COST = 1500000;
    public final static int CHANGE_EMBLEM_COST = 5000000;
    public final static int INCREASE_CAPACITY_COST = 500000;

    private enum BCOp {
        NONE, DISBAND, EMBELMCHANGE
    }
    private List<MapleGuildCharacter> members;
    private String rankTitles[] = new String[5]; // 1 = master, 2 = jr, 5 = lowest member
    private String name;
    private int id;
    private int gp;
    private int logo;
    private int logoColor;
    private int leader;
    private int capacity;
    private int logoBG;
    private int logoBGColor;
    private String notice;
    private int signature;
    private Map<Integer, List<Integer>> notifications = new LinkedHashMap<Integer, List<Integer>>();
    private boolean bDirty = true;

    public MapleGuild(MapleGuildCharacter initiator) {
        int guildid = initiator.getGuildId();
        members = new ArrayList<MapleGuildCharacter>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM guilds WHERE guildid=" + guildid);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                id = -1;
                ps.close();
                rs.close();
                return;
            }
            id = guildid;
            name = rs.getString("name");
            gp = rs.getInt("GP");
            logo = rs.getInt("logo");
            logoColor = rs.getInt("logoColor");
            logoBG = rs.getInt("logoBG");
            logoBGColor = rs.getInt("logoBGColor");
            capacity = rs.getInt("capacity");
            for (int i = 1; i <= 5; i++)
                rankTitles[i - 1] = rs.getString("rank" + i + "title");
            leader = rs.getInt("leader");
            notice = rs.getString("notice");
            signature = rs.getInt("signature");
            ps.close();
            rs.close();
            ps = con.prepareStatement("SELECT id, name, level, job, guildrank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                return;
            }
            do {
                members.add(new MapleGuildCharacter(rs.getInt("id"), rs.getInt("level"), rs.getString("name"), -1, rs.getInt("job"), rs.getInt("guildrank"), guildid, false));
            } while (rs.next());
            setOnline(initiator.getId(), true, initiator.getChannel());
            ps.close();
            rs.close();
        } catch (SQLException se) {
            System.out.println("unable to read guild information from sql" + se);
            return;
        }
    }

    public void buildNotifications() {
        if (!bDirty)
            return;
        Set<Integer> chs = WorldRegistryImpl.getInstance().getChannelServer();
        if (notifications.keySet().size() != chs.size()) {
            notifications.clear();
            for (Integer ch : chs)
                notifications.put(ch, new java.util.LinkedList<Integer>());
        } else
            for (List<Integer> l : notifications.values())
                l.clear();
        synchronized (members) {
            for (MapleGuildCharacter mgc : members) {
                if (!mgc.isOnline())
                    continue;
                List<Integer> ch = notifications.get(mgc.getChannel());
                if (ch == null)
                    System.out.println("Unable to connect to channel " + mgc.getChannel());
                else
                    ch.add(mgc.getId());
            }
        }
        bDirty = false;
    }

    public void writeToDB(boolean bDisband) {
        Connection con = DatabaseConnection.getConnection();
        try {
            if (!bDisband) {
                String sql = "UPDATE guilds SET " + "GP = ?, " + "logo = ?, " + "logoColor = ?, " + "logoBG = ?, " + "logoBGColor = ?, ";
                for (int i = 0; i < 5; i++)
                    sql += "rank" + (i + 1) + "title = ?, ";
                sql += "capacity = ?, " + "notice = ? WHERE guildid = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, gp);
                ps.setInt(2, logo);
                ps.setInt(3, logoColor);
                ps.setInt(4, logoBG);
                ps.setInt(5, logoBGColor);
                for (int i = 6; i < 11; i++)
                    ps.setString(i, rankTitles[i - 6]);
                ps.setInt(11, capacity);
                ps.setString(12, notice);
                ps.setInt(13, this.id);
                ps.execute();
                ps.close();
            } else {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();
                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();
                this.broadcast(MaplePacketCreator.guildDisband(this.id));
            }
        } catch (SQLException se) {
        }
    }

    public int getId() {
        return id;
    }

    public int getLeaderId() {
        return leader;
    }

    public int getGP() {
        return gp;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int l) {
        logo = l;
    }

    public int getLogoColor() {
        return logoColor;
    }

    public void setLogoColor(int c) {
        logoColor = c;
    }

    public int getLogoBG() {
        return logoBG;
    }

    public void setLogoBG(int bg) {
        logoBG = bg;
    }

    public int getLogoBGColor() {
        return logoBGColor;
    }

    public void setLogoBGColor(int c) {
        logoBGColor = c;
    }

    public String getNotice() {
        if (notice == null)
            return "";
        return notice;
    }

    public String getName() {
        return name;
    }

    public java.util.Collection<MapleGuildCharacter> getMembers() {
        return java.util.Collections.unmodifiableCollection(members);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSignature() {
        return signature;
    }

    public void broadcast(MaplePacket packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public void broadcast(MaplePacket packet, int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    public void broadcast(MaplePacket packet, int exceptionId, BCOp bcop) {
        WorldRegistryImpl wr = WorldRegistryImpl.getInstance();
        Set<Integer> chs = wr.getChannelServer();
        synchronized (notifications) {
            if (bDirty)
                buildNotifications();
            try {
                ChannelWorldInterface cwi;
                for (Integer ch : chs) {
                    cwi = wr.getChannel(ch);
                    if (notifications.get(ch).size() > 0)
                        if (bcop == BCOp.DISBAND)
                            cwi.setGuildAndRank(notifications.get(ch), 0, 5, exceptionId);
                        else if (bcop == BCOp.EMBELMCHANGE)
                            cwi.changeEmblem(this.id, notifications.get(ch), new MapleGuildSummary(this));
                        else
                            cwi.sendPacket(notifications.get(ch), packet, exceptionId);
                }
            } catch (java.rmi.RemoteException re) {
                System.out.println("Failed to contact channel(s) for broadcast.");
            }
        }
    }

    public void guildMessage(MaplePacket serverNotice) {
        for (MapleGuildCharacter mgc : members)
            for (ChannelServer cs : ChannelServer.getAllInstances())
                if (cs.getPlayerStorage().getCharacterById(mgc.getId()) != null) {
                    MapleCharacter chr = cs.getPlayerStorage().getCharacterById(mgc.getId());
                    chr.getClient().getSession().write(serverNotice);
                    break;
                }
    }

    public void setOnline(int cid, boolean online, int channel) {
        boolean bBroadcast = true;
        for (MapleGuildCharacter mgc : members)
            if (mgc.getId() == cid) {
                if (mgc.isOnline() && online)
                    bBroadcast = false;
                mgc.setOnline(online);
                mgc.setChannel(channel);
                break;
            }
        if (bBroadcast)
            this.broadcast(MaplePacketCreator.guildMemberOnline(id, cid, online), cid);
        bDirty = true;
    }

    public void guildChat(String name, int cid, String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }

    public String getRankTitle(int rank) {
        return rankTitles[rank - 1];
    }

    public static int createGuild(int leaderId, String name) {
        try {
            Properties dbProp = new Properties();
            InputStreamReader is = new FileReader("db.properties");
            dbProp.load(is);
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                ps.close();
                rs.close();
                return 0;
            }
            ps.close();
            rs.close();
            ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`) VALUES (?, ?, ?)");
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) System.currentTimeMillis());
            ps.execute();
            ps.close();
            ps = con.prepareStatement("SELECT guildid FROM guilds WHERE leader = ?");
            ps.setInt(1, leaderId);
            rs = ps.executeQuery();
            rs.first();
            int guildid = rs.getInt("guildid");
            rs.close();
            ps.close();
            return guildid;
        } catch (Exception e) {
            return 0;
        }
    }

    public int addGuildMember(MapleGuildCharacter mgc) {
        synchronized (members) {
            if (members.size() >= capacity)
                return 0;
            for (int i = members.size() - 1; i >= 0; i--)
                if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(mgc.getName()) < 0) {
                    members.add(i + 1, mgc);
                    bDirty = true;
                    break;
                }
        }
        this.broadcast(MaplePacketCreator.newGuildMember(mgc));
        return 1;
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        this.broadcast(MaplePacketCreator.memberLeft(mgc, false));
        synchronized (members) {
            members.remove(mgc);
            bDirty = true;
        }
    }

    private void sendUnkwnNote(String to, String msg, String from) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setString(1, to);
        ps.setString(2, from);
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        synchronized (members) {
            java.util.Iterator<MapleGuildCharacter> itr = members.iterator();
            MapleGuildCharacter mgc;
            while (itr.hasNext()) {
                mgc = itr.next();
                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgc, true));
                    itr.remove();
                    bDirty = true;
                    try {
                        if (mgc.isOnline())
                            WorldRegistryImpl.getInstance().getChannel(mgc.getChannel()).setGuildAndRank(cid, 0, 5);
                        else {
                            try {
                                initiator.getName();
                                sendUnkwnNote(mgc.getName(), "You have been expelled from the guild.", initiator.getName());
                            } catch (SQLException e) {
                                System.out.println("expelMember - MapleGuild " + e);
                            }
                            WorldRegistryImpl.getInstance().getChannel(1).setOfflineGuildStatus((short) 0, (byte) 5, cid);
                        }
                    } catch (RemoteException re) {
                        re.printStackTrace();
                        return;
                    }
                    return;
                }
            }
            System.out.println("Unable to find member with name " + name + " and id " + cid);
        }
    }

    public void changeRank(int cid, int newRank) {
        for (MapleGuildCharacter mgc : members)
            if (cid == mgc.getId()) {
                try {
                    if (mgc.isOnline())
                        WorldRegistryImpl.getInstance().getChannel(mgc.getChannel()).setGuildAndRank(cid, this.id, newRank);
                    else
                        WorldRegistryImpl.getInstance().getChannel(1).setOfflineGuildStatus((short) this.id, (byte) newRank, cid);
                } catch (RemoteException re) {
                    re.printStackTrace();
                    return;
                }
                mgc.setGuildRank(newRank);
                this.broadcast(MaplePacketCreator.changeRank(mgc));
                return;
            }
    }

    public void setGuildNotice(String notice) {
        this.notice = notice;
        writeToDB(false);
        this.broadcast(MaplePacketCreator.guildNotice(this.id, notice));
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        for (MapleGuildCharacter member : members)
            if (mgc.equals(member)) {
                member.setJobId(mgc.getJobId());
                member.setLevel(mgc.getLevel());
                this.broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
                break;
            }
    }

    public void changeRankTitle(String[] ranks) {
        for (int i = 0; i < 5; i++)
            rankTitles[i] = ranks[i];
        this.broadcast(MaplePacketCreator.rankTitleChange(this.id, ranks));
        this.writeToDB(false);
    }

    public void disbandGuild() {
        this.writeToDB(true);
        this.broadcast(null, -1, BCOp.DISBAND);
    }

    public void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.writeToDB(false);
        this.broadcast(null, -1, BCOp.EMBELMCHANGE);
    }

    public MapleGuildCharacter getMGC(int cid) {
        for (MapleGuildCharacter mgc : members)
            if (mgc.getId() == cid)
                return mgc;
        return null;
    }

    public boolean increaseCapacity() {
        if (capacity > 99)
            return false;
        capacity += 5;
        this.writeToDB(false);
        this.broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));
        return true;
    }

    public void gainGP(int amount) {
        this.gp += amount;
        this.writeToDB(false);
        this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
    }

    public static MapleGuildResponse sendInvite(MapleClient c, String targetName) {
        MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null)
            return MapleGuildResponse.NOT_IN_CHANNEL;
        if (mc.getGuildId() > 0)
            return MapleGuildResponse.ALREADY_IN_GUILD;
        mc.getClient().getSession().write(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName()));
        return null;
    }

    public static void displayGuildRanks(MapleClient c, int npcid) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `name`, `GP`, `logoBG`, `logoBGColor`, " + "`logo`, `logoColor` FROM guilds ORDER BY `GP` DESC LIMIT 50");
            ResultSet rs = ps.executeQuery();
            c.getSession().write(MaplePacketCreator.showGuildRanks(npcid, rs));
            ps.close();
            rs.close();
        } catch (Exception e) {
            System.out.println("failed to display guild ranks. " + e);
        }
    }
}
