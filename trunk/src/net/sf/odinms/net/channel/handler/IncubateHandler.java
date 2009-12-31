package net.sf.odinms.net.channel.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map.Entry;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.InventoryException;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;

/**
 *
 * @author Pilseung([Only registered and activated users can see links. ])
 */
public class IncubateHandler {
    public static boolean getIncubatedItem(MapleClient c) {
        HashMap<String, String> IncubatedItem = new HashMap<String, String>();
        try {
        FileReader fl = new FileReader("config/incubator.properties");
        BufferedReader br = new BufferedReader(fl);
        String[] readSplit = new String[2];
        String readLine = null;
        while ((readLine = br.readLine()) != null) {
            readSplit = readLine.split(" - ");
            IncubatedItem.put(readSplit[0], readSplit[1]);
        }
        fl.close();
        br.close();
        } catch (Exception e) {
            System.out.print(e);
            return false;
        }
        int rand = (int) (Math.random() * IncubatedItem.entrySet().size());
        int hmany = 0;
        int itemcode = 0;
        int amount = 0;
        for (Entry<String, String> entry : IncubatedItem.entrySet()) {
            hmany++;
            if(hmany == rand) {
                try {
                    itemcode = Integer.parseInt(entry.getKey());
                    amount = Integer.parseInt(entry.getValue());
                    break;
                } catch (Exception e) {
                    System.out.print(e);
                    return false;
                }
            }
        }
        if (itemcode == 0 || amount == 0)
            return false;
        boolean isfullslot = false;
        if (getInventory(c, MapleInventoryType.EQUIP).isFull(1) || getInventory(c, MapleInventoryType.USE).isFull(3) || getInventory(c, MapleInventoryType.ETC).isFull(1)) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            return false;
        }
        MapleInventoryManipulator.addById(c, itemcode, (short) amount, "get incubated item");
        new ServernoticeMapleClientMessageCallback(1, c).dropMessage("You have received a special item. Check your inventory to find out what it is.");
        return true;
    }

    private static MapleInventory getInventory(MapleClient c, MapleInventoryType type) {
        return c.getPlayer().getInventory(type);
    }
}  