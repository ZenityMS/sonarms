package client.command;

import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import server.MapleItemInformationProvider;
import client.Item;  
import client.MapleInventoryType;
import scripting.npc.NPCScriptManager;
import tools.MaplePacketCreator;
import net.channel.ChannelServer.*;
class PlayerCommand {
    static void execute(MapleClient c, String[] splitted) {
        MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("@commands") || splitted[0].equalsIgnoreCase("@help")) {
            player.dropMessage("@str/@dex/@int/@luk <number> ~ with these commands you will never have to add AP the slow way.");
            player.dropMessage("@rebirth ~ Rebirths you at level 200.");
			player.dropMessage("@expfix ~ Fixes -Exp");
			player.dropMessage("@checkstats ~ Shows your stats.");
			player.dropMessage("@emo ~ Kill yourself");
			player.dropMessage("@bigshop ~ Opens the Big Shop.");
            player.dropMessage("@hair ~ Change the way you look.");
			player.dropMessage("@recharge ~ Recharges stars.");
			player.dropMessage("@petname ~ Change the name of your pet.");
			player.dropMessage("@cody, storage, spinel ~ Opens the NPC chosen");
        } else if (splitted[0].equals("@dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
		} else if (splitted[0].equalsIgnoreCase("@checkstats")) {
                player.dropMessage("Your stats are:");
                player.dropMessage("Str: " + player.getStr());
                player.dropMessage("Dex: " + player.getDex());
                player.dropMessage("Int: " + player.getInt());
                player.dropMessage("Luk: " + player.getLuk());
                player.dropMessage("Available AP: " + player.getRemainingAp());
		} else if (splitted[0].equalsIgnoreCase("@expfix")) {
                player.setExp(0);
                player.updateSingleStat(MapleStat.EXP, player.getExp());
		} else if (splitted[0].equals("@emo")) {
                player.setHp(0);
                player.updateSingleStat(MapleStat.HP, 0);
		} else if (splitted[0].equalsIgnoreCase("@fakerelog") || splitted[0].equalsIgnoreCase("!fakerelog")) {
                c.getSession().write(MaplePacketCreator.getCharInfo(player));
                player.getMap().removePlayer(player);
                player.getMap().addPlayer(player);
		} else if (splitted[0].equalsIgnoreCase("@cody")) {
                NPCScriptManager.getInstance().start(c, 9200000);
		} else if (splitted[0].equals("@bigshop")) {
				NPCScriptManager.getInstance().start(c, 22000, null, null);
        } else if (splitted[0].equals("@hair")) {
				NPCScriptManager.getInstance().start(c, 9900001, null, null);
		} else if (splitted[0].equals("@petname")) {
				NPCScriptManager.getInstance().start(c, 9120008, null, null);
        } else if (splitted[0].equalsIgnoreCase("@storage")) {
                player.getStorage().sendStorage(c, 2080005);
		} else if (splitted[0].equalsIgnoreCase("@spinel")) {
                NPCScriptManager.getInstance().start(c, 9000020);
		} else if (splitted[0].equals("!rechargestars")) {
    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    for (IItem stars : c.getPlayer().getInventory(MapleInventoryType.USE) .list()) {
    if (ii.isThrowingStar(stars.getItemId())){
    stars.setQuantity(ii.getSlotMax(stars.getItemId()) );
    c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
    }
    player.dropMessage("Recharged.");
}
        } else if (splitted[0].equals("@rebirth"))
            if (player.getLevel() >= c.getChannelServer().getLevelCap())
                player.doReborn();
            else
                player.message("You are not level " + c.getChannelServer().getLevelCap() + ".");
        else if (splitted[0].equals("@str") || splitted[0].equals("@dex") || splitted[0].equals("@int") || splitted[0].equals("@luk")) {
            int x = Integer.parseInt(splitted[1]), max = c.getChannelServer().getMaxStat();
            if (x > 0 && x <= player.getRemainingAp() && x < max - 4) {
                if (splitted[0].equals("@str") && x + player.getStr() < max)
                    player.addStat(1, x);
                else if (splitted[0].equals("@dex") && x + player.getDex() < max)
                    player.addStat(2, x);
                else if (splitted[0].equals("@int") && x + player.getInt() < max)
                    player.addStat(3, x);
                else if (splitted[0].equals("@luk") && x + player.getLuk() < max)
                    player.addStat(4, x);
                else {
                    player.message("The stat cannot exceed " + max + ".");
                    return;
                }
                player.setRemainingAp(player.getRemainingAp() - x);
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            } else
                player.message("You do not have enough AP.");
        } else
            player.message("Player Command " + splitted[0] + " does not exist.");
    }
}