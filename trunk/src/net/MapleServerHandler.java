package net;

import client.MapleClient;
import net.channel.ChannelServer;
import net.login.LoginWorker;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

public class MapleServerHandler extends IoHandlerAdapter {
    private final static short MAPLE_VERSION = 62;
    private PacketProcessor processor;
    private int channel = -1;
    private boolean trace = false;

    public MapleServerHandler(PacketProcessor processor) {
        this.processor = processor;
    }

    public MapleServerHandler(PacketProcessor processor, int channel) {
        this.processor = processor;
        this.channel = channel;
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        Runnable r = ((MaplePacket) message).getOnSend();
        if (r != null)
            r.run();
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        System.out.println(MapleClient.getLogMessage(client, cause.getMessage()) + " " + cause);
        cause.printStackTrace();
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("IoSession with " + session.getRemoteAddress() + " opened");
        if (channel > -1)
            if (ChannelServer.getInstance(channel).isShutdown()) {
                session.close();
                return;
            }
        byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - MAPLE_VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, MAPLE_VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setChannel(channel);
        session.write(MaplePacketCreator.getHello(MAPLE_VERSION, ivSend, ivRecv, false));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
        session.setIdleTime(IdleStatus.READER_IDLE, 30);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 30);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        synchronized (session) {
            MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
            if (client != null) {
                client.disconnect();
                LoginWorker.getInstance().deregisterClient(client);
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        MaplePacketHandler packetHandler = processor.getHandler(packetId);
        if (packetHandler != null && packetHandler.validateState(client))
            try {
                if (trace) {
                    @SuppressWarnings("unused")
                    String from = "";
                    if (client.getPlayer() != null)
                        from = "from " + client.getPlayer().getName() + " ";
                }
//                String name = packetHandler.getClass().getSimpleName().toLowerCase();
//                System.out.println(packetHandler.getClass().getSimpleName());
//                System.out.println(HexTool.toString(content));
                packetHandler.handlePacket(slea, client);
            } catch (Throwable t) {
            }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null)
            client.sendPing();
        super.sessionIdle(session, status);
    }
}
