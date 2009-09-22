package net.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import client.MapleCharacter;
import tools.DatabaseConnection;
import net.MaplePacket;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.channel.remote.ChannelWorldInterface;
import net.mina.MapleCodecFactory;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.guild.MapleGuildSummary;
import net.world.remote.WorldChannelInterface;
import net.world.remote.WorldRegistry;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.MapleTrade;
import server.ShutdownServer;
import server.TimerManager;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class ChannelServer implements Runnable, ChannelServerMBean {
    private static int uniqueID = 1;
    private int port = 7575;
    private static Properties initialProp;
    private static WorldRegistry worldRegistry;
    private PlayerStorage players = new PlayerStorage();
    private String serverMessage;
    private int expRate,  mesoRate,  dropRate,  bossdropRate,  petExpRate,  mountExpRate;
    private boolean dropUndroppables,  moreThanOne;
    private int channel;
    private String key;
    private Properties props = new Properties();
    private ChannelWorldInterface cwi;
    private WorldChannelInterface wci = null;
    private IoAcceptor acceptor;
    private String ip;
    private boolean shutdown = false,  finishedShutdown = false;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private static Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
    private static Map<String, ChannelServer> pendingInstances = new HashMap<String, ChannelServer>();
    private Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
    private Boolean worldReady = true;
    private int instanceId = 0;
    private boolean mts,  skillMaxing,  playerCommands,  multiLevel;
    private int levelCap;
    private int maxStat;

    private ChannelServer(String key) {
        mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
        this.key = key;
    }

    public static WorldRegistry getWorldRegistry() {
        return worldRegistry;
    }
    
    public int getInstanceId() {
        return instanceId;
    }

    public int getMaxStat() {
        return maxStat;
    }

    public void setInstanceId(int k) {
        instanceId = k;
    }

    public void addInstanceId() {
        instanceId++;
    }

    public void reconnectWorld() {
        try {
            wci.isAvailable();
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                worldReady = false;
            }
            synchronized (cwi) {
                synchronized (worldReady) {
                    if (worldReady)
                        return;
                }
                System.out.println("Reconnecting to world server");
                synchronized (wci) {
                    try {
                        initialProp = new Properties();
                        FileReader fr = new FileReader(System.getProperty("channel.config"));
                        initialProp.load(fr);
                        fr.close();
                        Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
                        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
                        cwi = new ChannelWorldInterfaceImpl(this);
                        wci = worldRegistry.registerChannelServer(key, cwi);
                        props = wci.getGameProperties();
                        expRate = Integer.parseInt(props.getProperty("world.exp"));
                        mesoRate = Integer.parseInt(props.getProperty("world.meso"));
                        dropRate = Integer.parseInt(props.getProperty("world.drop"));
                        bossdropRate = Integer.parseInt(props.getProperty("world.bossdrop"));
                        petExpRate = Integer.parseInt(props.getProperty("world.petExp"));
                        mountExpRate = Integer.parseInt(props.getProperty("world.mountExp"));
                        levelCap = Integer.parseInt(props.getProperty("world.levelCap"));
                        serverMessage = props.getProperty("world.serverMessage");
                        dropUndroppables = Boolean.parseBoolean(props.getProperty("world.alldrop", "false"));
                        moreThanOne = Boolean.parseBoolean(props.getProperty("world.morethanone", "false"));
                        mts = Boolean.parseBoolean(props.getProperty("world.mts", "false"));
                        skillMaxing = Boolean.parseBoolean(props.getProperty("world.skillMaxing", "false"));
                        playerCommands = Boolean.parseBoolean(props.getProperty("world.playerCommands", "false"));
                        multiLevel = Boolean.parseBoolean(props.getProperty("world.multiLevel", "false"));
                        maxStat = Integer.parseInt(props.getProperty("world.maxStat"));
                        Properties dbProp = new Properties();
                        fr = new FileReader("db.properties");
                        dbProp.load(fr);
                        fr.close();
                        DatabaseConnection.setProps(dbProp);
                        DatabaseConnection.getConnection();
                        wci.serverReady();
                    } catch (Exception e) {
                        System.out.println("Reconnecting failed " + e);
                    }
                    worldReady = true;
                }
            }
            synchronized (worldReady) {
                worldReady.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        try {
            cwi = new ChannelWorldInterfaceImpl(this);
            wci = worldRegistry.registerChannelServer(key, cwi);
            props = wci.getGameProperties();
            expRate = Integer.parseInt(props.getProperty("world.exp"));
            mesoRate = Integer.parseInt(props.getProperty("world.meso"));
            dropRate = Integer.parseInt(props.getProperty("world.drop"));
            bossdropRate = Integer.parseInt(props.getProperty("world.bossdrop"));
            petExpRate = Integer.parseInt(props.getProperty("world.petExp"));
            mountExpRate = Integer.parseInt(props.getProperty("world.mountExp"));
            levelCap = Integer.parseInt(props.getProperty("world.levelCap"));
            serverMessage = props.getProperty("world.serverMessage");
            dropUndroppables = Boolean.parseBoolean(props.getProperty("world.alldrop", "false"));
            moreThanOne = Boolean.parseBoolean(props.getProperty("world.morethanone", "false"));
            eventSM = new EventScriptManager(this, props.getProperty("channel.events").split(","));
            mts = Boolean.parseBoolean(props.getProperty("world.mts", "false"));
            skillMaxing = Boolean.parseBoolean(props.getProperty("world.skillMaxing", "false"));
            playerCommands = Boolean.parseBoolean(props.getProperty("world.playerCommands", "false"));
            multiLevel = Boolean.parseBoolean(props.getProperty("world.multiLevel", "false"));
            maxStat = Integer.parseInt(props.getProperty("world.maxStat"));
            Properties dbProp = new Properties();
            FileReader fileReader = new FileReader("db.properties");
            dbProp.load(fileReader);
            fileReader.close();
            DatabaseConnection.setProps(dbProp);
            DatabaseConnection.getConnection();
            Connection c = DatabaseConnection.getConnection();
            try {
                PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
                ps.executeUpdate();
                ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                System.out.println("Could not reset databases " + ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        port = Integer.parseInt(props.getProperty("channel.net.port"));
        ip = props.getProperty("channel.net.interface") + ":" + port;
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        acceptor = new SocketAcceptor();
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        try {
            MapleServerHandler serverHandler = new MapleServerHandler(PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER), channel);
            acceptor.bind(new InetSocketAddress(port), serverHandler, cfg);
            System.out.println("Channel " + getChannel() + ": Listening on port " + port);
            wci.serverReady();
            eventSM.init();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Binding to port " + port + " failed (ch: " + getChannel() + ") ");
        }
    }

    public void shutdown() {
        shutdown = true;
        List<CloseFuture> futures = new LinkedList<CloseFuture>();
        Collection<MapleCharacter> allchars = players.getAllCharacters();
        MapleCharacter chrs[] = allchars.toArray(new MapleCharacter[allchars.size()]);
        for (MapleCharacter chr : chrs) {
            if (chr.getTrade() != null)
                MapleTrade.cancelTrade(chr);
            if (chr.getEventInstance() != null)
                chr.getEventInstance().playerDisconnected(chr);
            chr.saveToDB(true);
            if (chr.getCheatTracker() != null)
                chr.getCheatTracker().dispose();
            removePlayer(chr);
            if (chr.getHiredMerchant().isOwner(chr))
                chr.getHiredMerchant().closeShop(chr.getClient());
        }
        for (MapleCharacter chr : chrs)
            futures.add(chr.getClient().getSession().close());
        for (CloseFuture future : futures)
            future.join(500);
        finishedShutdown = true;
        wci = null;
        cwi = null;
    }

    public void unbind() {
        acceptor.unbindAll();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    private static ChannelServer newInstance(String key) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        ChannelServer instance = new ChannelServer(key);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(instance, new ObjectName("net.channel:type=ChannelServer,name=ChannelServer" + uniqueID++));
        pendingInstances.put(key, instance);
        return instance;
    }

    public static ChannelServer getInstance(int channel) {
        return instances.get(channel);
    }

    public void addPlayer(MapleCharacter chr) {
        players.registerPlayer(chr);
        chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
    }

    public IPlayerStorage getPlayerStorage() {
        return players;
    }

    public boolean allowMTS() {
        return mts;
    }

    public void removePlayer(MapleCharacter chr) {
        players.deregisterPlayer(chr);
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public int getLevelCap() {
        return levelCap;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public void setServerMessage(String newMessage) {
        serverMessage = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
    }

    public void broadcastPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters())
            chr.getClient().getSession().write(data);
    }

    public int getExpRate() {
        return expRate;
    }

    public void setExpRate(int expRate) {
        this.expRate = expRate;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        if (pendingInstances.containsKey(key))
            pendingInstances.remove(key);
        if (instances.containsKey(channel))
            instances.remove(channel);
        instances.put(channel, this);
        this.channel = channel;
        this.mapFactory.setChannel(channel);
    }

    public static Collection<ChannelServer> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public boolean getSkillMaxing() {
        return skillMaxing;
    }

    public boolean allowPlayerCommands() {
        return playerCommands;
    }

    public String getIP() {
        return ip;
    }

    public String getIP(int channel) {
        try {
            return getWorldInterface().getIP(channel);
        } catch (RemoteException e) {
            System.out.println("Lost connection to world server " + e);
            throw new RuntimeException("Lost connection to world server");
        }
    }

    public WorldChannelInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady)
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
        }
        return wci;
    }

    public String getProperty(String name) {
        return props.getProperty(name);
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void shutdown(int time) {
        TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
    }

    public int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public void setMesoRate(int x) {
        this.mesoRate = x;
    }

    public int getDropRate() {
        return dropRate;
    }

    public void setDropRate(int x) {
        this.dropRate = Math.min(x, 50);
    }

    public int getMountRate() {
        return mountExpRate;
    }

    public int getBossDropRate() {
        return bossdropRate;
    }

    public void setBossDropRate(int x) {
        this.bossdropRate = x;
    }

    public int getPetExpRate() {
        return petExpRate;
    }

    public boolean allowUndroppablesDrop() {
        return dropUndroppables;
    }

    public boolean allowMoreThanOne() {
        return moreThanOne;
    }
    
    public MapleGuild getGuild(MapleGuildCharacter mgc) {
        int gid = mgc.getGuildId();
        MapleGuild g = null;
        try {
            g = this.getWorldInterface().getGuild(gid, mgc);
        } catch (RemoteException re) {
            System.out.println("RemoteException while fetching MapleGuild. " + re);
            return null;
        }
        if (gsStore.get(gid) == null)
            gsStore.put(gid, new MapleGuildSummary(g));
        return g;
    }

    public MapleGuildSummary getGuildSummary(int gid) {
        if (gsStore.containsKey(gid))
            return gsStore.get(gid);
        else
            try {
                MapleGuild g = this.getWorldInterface().getGuild(gid, null);
                if (g != null)
                    gsStore.put(gid, new MapleGuildSummary(g));
                return gsStore.get(gid);
            } catch (RemoteException re) {
                System.out.println("RemoteException while fetching GuildSummary. " + re);
                return null;
            }
    }

    public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
        gsStore.put(gid, mgs);
    }

    public void reloadGuildSummary() {
        try {
            MapleGuild g;
            for (int i : gsStore.keySet()) {
                g = this.getWorldInterface().getGuild(i, null);
                if (g != null)
                    gsStore.put(i, new MapleGuildSummary(g));
                else
                    gsStore.remove(i);
            }
        } catch (RemoteException re) {
            System.out.println("RemoteException while reloading GuildSummary." + re);
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        initialProp = new Properties();
        initialProp.load(new FileReader(System.getProperty("channel.config")));
        Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
        for (int i = 0; i < Integer.parseInt(initialProp.getProperty("channel.count", "0")); i++)
            newInstance(initialProp.getProperty("channel." + i + ".key")).run();
        DatabaseConnection.getConnection(); // touch - so we see database problems early...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (ChannelServer channel : getAllInstances())
                    for (MapleCharacter mc : channel.getPlayerStorage().getAllCharacters())
                        mc.saveToDB(true);
            }
        });
    }

    public void broadcastGMPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters())
            if (chr.gmLevel() > 0)
                chr.getClient().getSession().write(data);
    }

    public boolean getMultiLevel() {
        return multiLevel;
    }

    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters())
            mc.getClient().getSession().write(MaplePacketCreator.sendYellowTip(msg));
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters())
            mc.dropMessage(msg);
    }
}