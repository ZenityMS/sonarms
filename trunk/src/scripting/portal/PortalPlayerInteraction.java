package scripting.portal;

import client.MapleClient;
import scripting.AbstractPlayerInteraction;
import server.MaplePortal;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SavedLocationType;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {
    private MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c);
        this.portal = portal;
    }

    public MaplePortal getPortal() {
        return portal;
    }

    public boolean isMonster(MapleMapObject o) {
        return o.getType() == MapleMapObjectType.MONSTER;
    }
}