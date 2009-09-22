package net.channel.handler;

import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author PurpleMadness
 */
public class UseMountFoodHandler extends AbstractMaplePacketHandler {
    private int[] mount = {1, 6, 25, 50, 105, 134, 196, 254, 263, 315, 367, 430, 543, 587, 679, 725, 897, 1146, 1394, 1701, 2247, 2543, 2898, 3156, 3313, 3584, 3923, 4150, 4305, 4550};

    private int getMountExpNeededForLevel(int level) {
        return mount[level];
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        slea.readShort();
        int itemid = slea.readInt();
        if (c.getPlayer().getInventory(MapleInventoryType.USE).findById(itemid) != null)
            if (c.getPlayer().getMount() != null) {
                c.getPlayer().getMount().setTiredness(c.getPlayer().getMount().getTiredness() - 30);
                c.getPlayer().getMount().setExp((int) ((Math.random() * 26) + 12) * ChannelServer.getInstance(c.getChannel()).getMountRate() + c.getPlayer().getMount().getExp());
                int level = c.getPlayer().getMount().getLevel();
                boolean levelup = c.getPlayer().getMount().getExp() >= getMountExpNeededForLevel(level) && level < 31 && c.getPlayer().getMount().getTiredness() != 0;
                if (levelup)
                    c.getPlayer().getMount().setLevel(level + 1);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateMount(c.getPlayer().getId(), c.getPlayer().getMount(), levelup));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
            } else
                c.getPlayer().dropMessage("Please get on your mount first before using the mount food.");
    }
}