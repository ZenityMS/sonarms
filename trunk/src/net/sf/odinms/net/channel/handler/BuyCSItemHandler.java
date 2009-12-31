package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.CashItemFactory;
import net.sf.odinms.server.CashItemInfo;

public final class BuyCSItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int action = slea.readByte();
        slea.readByte();
        if (action == 3) {
            final int useNX = slea.readInt();
            final int snCS = slea.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(snCS);
            if (c.getPlayer().getCSPoints(useNX) >= item.getPrice())
                c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
            else {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (item.getId() >= 5000000 && item.getId() <= 5000100) {
                final int petId = MaplePet.createPet(item.getId());
                if (petId == -1)
                    return;
                MapleInventoryManipulator.addById(c, item.getId(), (short) 1, "Cash Item was purchased.", null, petId);
            } else
                MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "Cash Item was purchased.");
            c.getSession().write(MaplePacketCreator.showBoughtCSItem(c, item));
            showCS(c);
        } else if (action == 31) {
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (c.getPlayer().getMeso() >= item.getPrice() && (item.getId() == 4031180 || item.getId() == 4031192)) {
                c.getPlayer().gainMeso(-item.getPrice(), false);
                MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), null);
                c.getSession().write(MaplePacketCreator.showBoughtCSQuestItem(item.getId()));
            }
        }
    }

    private static final void showCS(MapleClient c) {
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
