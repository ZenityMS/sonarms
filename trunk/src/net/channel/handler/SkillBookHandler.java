package net.channel.handler;

import java.util.Map;
import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.ISkill;
import client.SkillFactory;

public class SkillBookHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        MapleCharacter player = c.getPlayer();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() == 1) {
            if (toUse.getItemId() != itemId)
                return;
            Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getSkillStats(toUse.getItemId(), c.getPlayer().getJob().getId());
            boolean canuse = false;
            boolean success = false;
            int skill = 0;
            int maxlevel = 0;
            if (skilldata == null)
                return;
            if (skilldata.get("skillid") == 0)
                canuse = false;
            else if (player.getMasterLevel(SkillFactory.getSkill(skilldata.get("skillid"))) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) {
                canuse = true;
                if ((int) Math.floor(Math.random() * 100) + 1 <= skilldata.get("success") && skilldata.get("success") != 0) {
                    success = true;
                    ISkill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
                    player.changeSkillLevel(skill2, player.getSkillLevel(skill2), skilldata.get("masterLevel"));
                } else {
                    success = false;
                    player.dropMessage("The skill book lights up, but the item winds up as if nothing happened.");
                }
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            } else
                canuse = false;
            player.getClient().getSession().write(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, canuse, success));
        }
    }
}