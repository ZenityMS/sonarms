package net.world;

import java.awt.Point;
import java.io.Serializable;
import client.MapleJob;
import client.MapleCharacter;

public class MaplePartyCharacter implements Serializable {
    private String name;
    private int id;
    private int level;
    private int channel;
    private int jobid;
    private int mapid;
    private int doorTown = 999999999;
    private int doorTarget = 999999999;
    private Point doorPosition = new Point(0, 0);
    private boolean online;
    private int gender;
    private boolean married;
    private MapleJob job;

    public MaplePartyCharacter(MapleCharacter maplechar) {
        this.name = maplechar.getName();
        this.level = maplechar.getLevel();
        this.channel = maplechar.getClient().getChannel();
        this.id = maplechar.getId();
        this.jobid = maplechar.getJob().getId();
        this.mapid = maplechar.getMapId();
        this.online = true;
        this.gender = maplechar.getGender();
        this.married = maplechar.isMarried();
        this.job = maplechar.getJob();
        if (maplechar.getDoors().size() > 0) {
            this.doorTown = maplechar.getDoors().get(0).getTown().getId();
            this.doorTarget = maplechar.getDoors().get(0).getTarget().getId();
            this.doorPosition = maplechar.getDoors().get(0).getTargetPosition();
        }
    }

    public MaplePartyCharacter() {
        this.name = "";
    }

    public MapleJob getJob() {
        return job;
    }

    public int getLevel() {
        return level;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getMapid() {
        return mapid;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobid;
    }

    public int getDoorTown() {
        return doorTown;
    }

    public int getDoorTarget() {
        return doorTarget;
    }

    public Point getDoorPosition() {
        return doorPosition;
    }

    @Override
    public int hashCode() {
        return 31 + ((name == null) ? 0 : name.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MaplePartyCharacter other = (MaplePartyCharacter) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
