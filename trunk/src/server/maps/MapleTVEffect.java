package server.maps;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import net.world.remote.WorldChannelInterface;
import server.TimerManager;
import tools.MaplePacketCreator;

/*
 * MapleTVEffect
 * Created by Lios
 * All credits to Cheetah and MrMysterious for creating
 * the MapleTV Method!
 */
public class MapleTVEffect {
    private static List<String> message = new LinkedList<String>();
    private static MapleCharacter user;
    private static boolean active;
    private static int type;
    private static MapleCharacter partner = null;
    private static MapleClient c;

    public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_) {
        message = msg;
        user = user_;
        type = type_;
        partner = partner_;
        broadCastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        active = set;
    }

    private void broadCastTV(boolean active_) {
        WorldChannelInterface wci = user.getClient().getChannelServer().getWorldInterface();
        setActive(active_);
        try {
            if (active_) {
                wci.broadcastMessage(null, MaplePacketCreator.enableTV().getBytes());
                wci.broadcastMessage(null, MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner).getBytes());
                scheduleCancel();
            } else
                wci.broadcastMessage(null, MaplePacketCreator.removeTV().getBytes());
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
    }

    private void scheduleCancel() {
        int delay = 15000; // default. cbf adding it to switch
        if (type == 4)
            delay = 30000;
        else if (type == 5)
            delay = 60000;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                broadCastTV(false);
            }
        }, delay);
    }
}  