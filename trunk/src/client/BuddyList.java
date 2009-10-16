package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public class BuddyList {
    public enum BuddyOperation {
        ADDED, DELETED
    }

    public enum BuddyAddResult {
        BUDDYLIST_FULL, ALREADY_ON_LIST, OK
    }
    private Map<Integer, BuddylistEntry> buddies = new LinkedHashMap<Integer, BuddylistEntry>();
    private int capacity;
    private LinkedList<CharacterNameAndId> pendingRequests = new LinkedList<CharacterNameAndId>();

    public BuddyList(int capacity) {
        super();
        this.capacity = capacity;
    }

    public boolean contains(int characterId) {
        return buddies.containsKey(Integer.valueOf(characterId));
    }

    public boolean containsVisible(int characterId) {
        BuddylistEntry ble = buddies.get(characterId);
        if (ble == null)
            return false;
        return ble.isVisible();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BuddylistEntry get(int characterId) {
        return buddies.get(Integer.valueOf(characterId));
    }

    public BuddylistEntry get(String characterName) {
        for (BuddylistEntry ble : buddies.values())
            if (ble.getName().toLowerCase().equals(characterName.toLowerCase()))
                return ble;
        return null;
    }

    public void put(BuddylistEntry entry) {
        buddies.put(Integer.valueOf(entry.getCharacterId()), entry);
    }

    public void remove(int characterId) {
        buddies.remove(Integer.valueOf(characterId));
    }

    public Collection<BuddylistEntry> getBuddies() {
        return buddies.values();
    }

    public boolean isFull() {
        return buddies.size() >= capacity;
    }

    public int[] getBuddyIds() {
        int buddyIds[] = new int[buddies.size()];
        int i = 0;
        for (BuddylistEntry ble : buddies.values())
            buddyIds[i++] = ble.getCharacterId();
        return buddyIds;
    }

    public void loadFromDb(int characterId) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT b.buddyid, b.pending, c.name as buddyname FROM buddies as b, characters as c WHERE c.id = b.buddyid AND b.characterid = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                if (rs.getInt("pending") == 1)
                    pendingRequests.add(new CharacterNameAndId(rs.getInt("buddyid"), rs.getString("buddyname")));
                else
                    put(new BuddylistEntry(rs.getString("buddyname"), rs.getInt("buddyid"), -1, true));
            rs.close();
            ps.close();
            ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM buddies WHERE pending = 1 AND characterid = ?");
            ps.setInt(1, characterId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CharacterNameAndId pollPendingRequest() {
        return pendingRequests.pop();
    }

    public void addBuddyRequest(MapleClient c, int cidFrom, String nameFrom, int channelFrom) {
        put(new BuddylistEntry(nameFrom, cidFrom, channelFrom, false));
        if (pendingRequests.isEmpty())
            c.getSession().write(MaplePacketCreator.requestBuddylistAdd(cidFrom, nameFrom));
        else
            pendingRequests.add(new CharacterNameAndId(cidFrom, nameFrom));
    }
}