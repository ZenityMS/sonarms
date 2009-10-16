package net.login;

import java.rmi.RemoteException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import client.MapleClient;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 * 
 * @author Matze
 */
public class LoginWorker implements Runnable {
    private static LoginWorker instance = new LoginWorker();
    private Deque<MapleClient> waiting;
    private Set<String> waitingNames;

    private LoginWorker() {
        waiting = new LinkedList<MapleClient>();
        waitingNames = new HashSet<String>();
    }

    public static LoginWorker getInstance() {
        return instance;
    }

    public void registerClient(MapleClient c) {
        synchronized (waiting) {
            if (!waiting.contains(c) && !waitingNames.contains(c.getAccountName().toLowerCase())) {
                waiting.add(c);
                waitingNames.add(c.getAccountName().toLowerCase());
                c.updateLoginState(MapleClient.LOGIN_WAITING);
            }
        }
    }

    public void deregisterClient(MapleClient c) {
        synchronized (waiting) {
            waiting.remove(c);
            if (c.getAccountName() != null)
                waitingNames.remove(c.getAccountName().toLowerCase());
        }
    }

    public void run() {
        try {
            int possibleLogins = 10;
            LoginServer.getInstance().getWorldInterface().isAvailable();
            for (int i = 0; i < possibleLogins; i++) {
                final MapleClient client;
                synchronized (waiting) {
                    if (waiting.isEmpty())
                        break;
                    client = waiting.removeFirst();
                }
                waitingNames.remove(client.getAccountName().toLowerCase());
                if (client.finishLogin(true) == 0) {
                    client.getSession().write(MaplePacketCreator.getAuthSuccessRequestPin(client.getAccountName()));
                    client.setIdleTask(TimerManager.getInstance().schedule(new Runnable() {
                        public void run() {
                            client.getSession().close();
                        }
                    }, 600 * 10000));
                } else
                    client.getSession().write(MaplePacketCreator.getLoginFailed(7));
            }
            Map<Integer, Integer> load = LoginServer.getInstance().getWorldInterface().getChannelLoad();
            double loadFactor = 1200 / ((double) LoginServer.getInstance().getUserLimit() / load.size());
            for (Entry<Integer, Integer> entry : load.entrySet())
                load.put(entry.getKey(), Math.min(1200, (int) (entry.getValue() * loadFactor)));
            LoginServer.getInstance().setLoad(load);
        } catch (RemoteException ex) {
            LoginServer.getInstance().reconnectWorld();
        }
    }
}
