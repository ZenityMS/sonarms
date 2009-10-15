package net.login.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ServerlistRequestHandler extends AbstractMaplePacketHandler {
    private String[] names = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Elnido", "Kastia", "Judis", "Arkenia", "Plana", "Galicia", "Kalluna", "Stius", "Croa", "Zenith", "Medere"};

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        for (int i = 0; i < Math.min(1, 15); i++)//input world numbers here change 1 number of worlds
            c.getSession().write(MaplePacketCreator.getServerList(i, serverName(i), LoginServer.getInstance().getLoad()));
        c.getSession().write(MaplePacketCreator.getEndOfServerList());
    }

    private String serverName(int a) {
        return names[a];
    }
}