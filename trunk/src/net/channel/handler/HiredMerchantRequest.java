package net.channel.handler;

import java.util.Arrays;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author XoticStory
 */
public class HiredMerchantRequest extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 23000, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT)).size() == 0 && c.getPlayer().getMapId() > 910000000 && c.getPlayer().getMapId() < 910000023)
            if (!c.getPlayer().hasMerchant())
                c.getSession().write(MaplePacketCreator.hiredMerchantBox());
            else
                c.getPlayer().dropMessage(1, "You already have a store open.");
        else
            c.getPlayer().dropMessage(1, "You cannot open your hired merchant here.");
    }
}