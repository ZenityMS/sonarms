package net.sf.odinms.client.messages.commands;

import java.io.File;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.tools.MaplePacketCreator;

public class PlayerCommands implements Command {

	 public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
	MapleCharacter player = c.getPlayer();
     
        if (splitted[0].equals("@str") || splitted[0].equals("@dex") || splitted[0].equals("@int") || splitted[0].equals("@luk")) {
            int x = Integer.parseInt(splitted[1]), max = 32767;
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
                    player.dropMessage("The stat cannot exceed " + max + ".");
                    return;
                }
                player.setRemainingAp(player.getRemainingAp() - x);
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
                } else
                player.dropMessage("You do not have enough AP.");
                if (splitted[0].equals("@cody"))
                NPCScriptManager.getInstance().start(c, 9200000);
                if (splitted[0].equals("@spinel"))
                NPCScriptManager.getInstance().start(c, 9000020);
                if (splitted[0].equals("@maxskills"))
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
                if (splitted[0].equals("@dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.dropMessage("You have been disposed.");
                }

        }
         }


	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
                    new CommandDefinition("str", "<amount>", "Sets your strength to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("int", "<amount>", "Sets your intelligence to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("luk", "<amount>", "Sets your luck to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("dex", "<amount>", "Sets your dexterity to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("hp", "<amount>", "Sets your hp to a higher amount if you have enough AP or takes it away if you aren't over 30000 HP.", 0),
                    new CommandDefinition("mp", "<amount>", "Sets your mp to a higher amount if you have enough AP or takes it away if you aren't over 30000 MP.", 0),
                    new CommandDefinition("cody", "<open>", "Opens the NPC Cody", 0),
                    new CommandDefinition("spinel", "<open>", "Opens the NPC Spinel.", 0),
                    new CommandDefinition("maxskills", "<open>", "Maxes your skills.", 0),
                    new CommandDefinition("dispose", "<open>", "Use if stuck.", 0),
		};
        }
}