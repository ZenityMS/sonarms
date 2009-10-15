package net.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MapleMessenger implements Serializable {
    private List<MapleMessengerCharacter> members = new LinkedList<MapleMessengerCharacter>();
    private int id;
    private boolean pos0 = false;
    private boolean pos1 = false;
    private boolean pos2 = false;

    public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
        this.members.add(chrfor);
        int position = getLowestPosition();
        chrfor.setPosition(position);
        this.id = id;
    }

    public void addMember(MapleMessengerCharacter member) {
        members.add(member);
        int position = getLowestPosition();
        member.setPosition(position);
    }

    public void removeMember(MapleMessengerCharacter member) {
        int position = member.getPosition();
        if (position == 0)
            pos0 = false;
        else if (position == 1)
            pos1 = false;
        else if (position == 2)
            pos2 = false;
        members.remove(member);
    }

    public void silentRemoveMember(MapleMessengerCharacter member) {
        members.remove(member);
    }

    public void silentAddMember(MapleMessengerCharacter member, int position) {
        members.add(member);
        member.setPosition(position);
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public int getLowestPosition() {
        int position;
        if (pos0)
            if (pos1) {
                this.pos2 = true;
                position = 2;
            } else {
                this.pos1 = true;
                position = 1;
            }
        else {
            this.pos0 = true;
            position = 0;
        }
        return position;
    }

    public int getPositionByName(String name) {
        for (MapleMessengerCharacter messengerchar : members)
            if (messengerchar.getName().equals(name))
                return messengerchar.getPosition();
        return 4;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return 31 + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MapleMessenger other = (MapleMessenger) obj;
        if (id != other.id)
            return false;
        return true;
    }
}

