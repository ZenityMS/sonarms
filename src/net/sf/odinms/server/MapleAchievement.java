package net.sf.odinms.server;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.tools.MaplePacketCreator;

/**
 *
 * @author Patrick/PurpleMadness
 */
public class MapleAchievement {
    private String name;
    private int reward;
    private boolean notice;

    public MapleAchievement(String name, int reward){
        this.name = name;
        this.reward = reward;
        this.notice = true;
    }

    public MapleAchievement(String name, int reward, boolean notice){
       this.name = name;
       this.reward = reward;
       this.notice = notice;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getReward(){
        return reward;
    }

    public void setReward(int reward){
        this.reward = reward;
    }

    public void finishAchievement(MapleCharacter player){
        try {
            player.addCSPoints(4, reward);
            player.addAchivements(1);
            player.addPoints(5);
            player.setAchievementFinished(MapleAchievements.getInstance().getByMapleAchievement(this));
            player.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[Achievement] You've gained " + reward + " NX & 5 REWARD POINTS as you " + name + "."));
            if (notice && !player.isGM())
                ChannelServer.getInstance(player.getClient().getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Achievement] Congratulations to " + player.getName() + " as they just " + name + "!").getBytes());
        } catch (RemoteException e) {
            player.getClient().getChannelServer().reconnectWorld();
        }
    }
}  