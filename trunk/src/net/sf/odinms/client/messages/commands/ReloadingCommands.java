package net.sf.odinms.client.messages.commands;

import java.rmi.RemoteException;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.net.ExternalCodeTableGetter;
import net.sf.odinms.net.PacketProcessor;
import net.sf.odinms.net.RecvPacketOpcode;
import net.sf.odinms.net.SendPacketOpcode;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.scripting.map.MapScriptManager;
import net.sf.odinms.scripting.portal.PortalScriptManager;
import net.sf.odinms.scripting.reactor.ReactorScriptManager;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.life.MapleMonsterInformationProvider;

public class ReloadingCommands implements Command {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReloadingCommands.class);

	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equals("!reloadGuilds")) {
			try
			{
				mc.dropMessage("Attempting to reload all guilds... this may take a while...");
				cserv.getWorldInterface().clearGuilds();
				mc.dropMessage("Completed.");
			}
			catch (RemoteException re)
			{
				mc.dropMessage("RemoteException occurred while attempting to reload guilds.");
				log.error("RemoteException occurred while attempting to reload guilds.", re);
			}
		} else if (splitted[0].equals("!reloadOps")) {
			try {
				ExternalCodeTableGetter.populateValues(SendPacketOpcode.getDefaultProperties(), SendPacketOpcode.values());
				ExternalCodeTableGetter.populateValues(RecvPacketOpcode.getDefaultProperties(), RecvPacketOpcode.values());
			} catch (Exception e) {
				log.error("Failed to reload props", e);
			}
			PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
			PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
		} else if (splitted[0].equals("!clearPortalScripts")) {
			PortalScriptManager.getInstance().clearScripts();
		} else if (splitted[0].equals("!reloadDrops")) {
			MapleMonsterInformationProvider.getInstance().clearDrops();
		} else if (splitted[0].equals("!reloadReactorDrops")) {
			ReactorScriptManager.getInstance().clearDrops();
		} else if (splitted[0].equals("!reloadShops")) {
			MapleShopFactory.getInstance().clear();
		} else if (splitted[0].equals("!reloadEvents")) {
			for (ChannelServer instance : ChannelServer.getAllInstances()) {
				instance.reloadEvents();
			}
		} else if (splitted[0].equals("!reloadCommands")) {
			CommandProcessor.getInstance().reloadCommands();
		}   else if (splitted[0].equals("!clearmapscripts")) {
			MapScriptManager.getInstance().clearScripts();
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("reloadGuilds", "", "", 100),
			new CommandDefinition("reloadOps", "", "", 100),
			new CommandDefinition("reloadPortalScripts", "", "", 100),
			new CommandDefinition("reloadDrops", "", "", 100),
			new CommandDefinition("reloadReactorDrops", "", "", 100),
			new CommandDefinition("reloadShops", "", "", 100),
			new CommandDefinition("reloadEvents", "", "", 100),
			new CommandDefinition("reloadCommands", "", "", 100),
			new CommandDefinition("clearmapscripts", "", "", 100),
		};
	}

}
