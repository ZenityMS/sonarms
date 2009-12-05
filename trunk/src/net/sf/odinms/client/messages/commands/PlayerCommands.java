package net.sf.odinms.client.messages.commands;

import java.util.ArrayList;
import java.util.List;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.MaplePacketCreator;

public class PlayerCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
	MapleCharacter player = c.getPlayer();

	if(splitted[0].equals("@spinel")) {
		NPCScriptManager npc = NPCScriptManager.getInstance();
		npc.start(c, 9000020);
        } else if (splitted[0].equals("@str") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@dex")) {
            int amount = Integer.parseInt(splitted[1]);
		boolean str = splitted[0].equals("@str");
		boolean Int = splitted[0].equals("@int");
		boolean luk = splitted[0].equals("@luk");
		boolean dex = splitted[0].equals("@dex");
          if(amount > 0 && amount <= player.getRemainingAp() && amount <= 32763 || amount < 0 && amount >= -32763 && Math.abs(amount) + player.getRemainingAp() <= 32767) {
		if (str && amount + player.getStr() <= 32767 && amount + player.getStr() >= 4) {
		player.setStr(player.getStr() + amount);
		player.updateSingleStat(MapleStat.STR, player.getStr());
		player.setRemainingAp(player.getRemainingAp() - amount);
		player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
	} else if (Int && amount + player.getInt() <= 32767 && amount + player.getInt() >= 4) {
		player.setInt(player.getInt() + amount);
		player.updateSingleStat(MapleStat.INT, player.getInt());
		player.setRemainingAp(player.getRemainingAp() - amount);
		player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
	} else if (luk && amount + player.getLuk() <= 32767 && amount + player.getLuk() >= 4) {
		player.setLuk(player.getLuk() + amount);
		player.updateSingleStat(MapleStat.LUK, player.getLuk());
		player.setRemainingAp(player.getRemainingAp() - amount);
		player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
	} else if (dex && amount + player.getDex() <= 32767 && amount + player.getDex() >= 4) {
		player.setDex(player.getDex() + amount);
		player.updateSingleStat(MapleStat.DEX, player.getDex());
		player.setRemainingAp(player.getRemainingAp() - amount);
		player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
	} else {
	mc.dropMessage("Please make sure the stat you are trying to raise is not over 32,767 or under 4.");
	}
		} else {
			mc.dropMessage("Please make sure your AP is not over 32,767 and you have enough to distribute.");
		}
	} else if (splitted[0].equals("@emo")) {
		player.setHp(0);
		player.updateSingleStat(MapleStat.HP, 0);
	} else if (splitted[0].equals("@expfix")) {
                player.setExp(0);
                player.updateSingleStat(MapleStat.EXP, player.getExp());
		mc.dropMessage("Your exp has been fixed~!");
	} else if (splitted[0].equals("@myap")){
		mc.dropMessage("You currently have " + c.getPlayer().getStr() + " STR, " + c.getPlayer().getDex() + " DEX, " + c.getPlayer().getLuk() + " LUK, " + c.getPlayer().getInt() + " INT.");
		mc.dropMessage("You currently have " + c.getPlayer().getRemainingAp() + " Ability Points.");
	} else if (splitted[0].equals("@commands")) {
		mc.dropMessage("The player Commands are:");
		mc.dropMessage("@str, @int, @dex, @luk, @save, @expfix, @myap, @emo, @rebirth, @statreset, @dispose, @starterpack");
	} else if (splitted[0].equals("@rebirth")) {
                if (player.getLevel() >= 200) {
                    int totalrebirth = c.getPlayer().getReborns() +1;
                    mc.dropMessage("You have been reborned. For a total of " + totalrebirth + " times.");
                    player.doReborn();
                } else {
                    mc.dropMessage("You are not level 200 yet!");
                }
        } else if (splitted[0].equals("@save")) {              
                    player.saveToDB(true);
                    mc.dropMessage("Character information saved.");              	
        } else if (splitted[0].equals("@dispose")) {
                    NPCScriptManager.getInstance().dispose(c);
                    c.getSession().write(MaplePacketCreator.enableActions());
                    mc.dropMessage("Done.");
        } else if (splitted[0].equals("@statreset")) {
                    if (player.getMeso() >= 5000000) {
			int str = c.getPlayer().getStr();
			int dex = c.getPlayer().getDex();
			int int_ = c.getPlayer().getInt();
			int luk = c.getPlayer().getLuk();
			int newap = c.getPlayer().getRemainingAp() + (str - 4) + (dex - 4) + (int_ - 4) + (luk - 4);
			c.getPlayer().setStr(4);
			c.getPlayer().setDex(4);
			c.getPlayer().setInt(4);
			c.getPlayer().setLuk(4);
			c.getPlayer().setRemainingAp(newap);
			List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
			stats.add(new Pair<MapleStat, Integer>(MapleStat.STR, Integer.valueOf(str)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.DEX, Integer.valueOf(dex)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.INT, Integer.valueOf(int_)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.LUK, Integer.valueOf(luk)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(newap)));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(stats));
                        mc.dropMessage("Your stats have been reset. Please CC or Relogin to apply the changes");
                    } else {
                        mc.dropMessage("Not enough mesos. You need 5mill to apreset");
                    }
        }
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
                    new CommandDefinition("myap", "", "See your ap from anywhere", 0),
                    new CommandDefinition("buff", "", "Get buff from anywhere", 0),                   
                    new CommandDefinition("emo", "", "Self Killing", 0),
                    new CommandDefinition("expfix", "", "Fixed negative exp", 0),
                    new CommandDefinition("commands", "", "Does Sexual Commands", 0),
                    new CommandDefinition("save", "", "S3xual So S3xual Saves UR ACC", 0),
                    new CommandDefinition("str", "<amount>", "Sets your strength to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("int", "<amount>", "Sets your intelligence to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("luk", "<amount>", "Sets your luck to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("dex", "<amount>", "Sets your dexterity to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("statreset", "", "Resets your stats", 0),
                    new CommandDefinition("dispose", "", "Stuck", 0),
                    new CommandDefinition("rebirth", "", "Guess", 0),                   
                    new CommandDefinition("spinel", "", "Spinel is pregnant", 0),
		};
	}

    }