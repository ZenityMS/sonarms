package net.channel.handler;

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import client.ISkill;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpecialMoveHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readShort();
        slea.readShort();
        int skillid = slea.readInt();
        Point pos = null;
        int __skillLevel = slea.readByte();
        ISkill skill = SkillFactory.getSkill(skillid);
        int skillLevel = c.getPlayer().getSkillLevel(skill);
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0)
            if (c.getPlayer().skillisCooling(skillid)) {
                c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.COOLDOWN_HACK);
                return;
            } else if (skillid != 5221006) {
                c.getSession().write(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
                ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), skillid), effect.getCooldown() * 1000);
                c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
            }
        if (skillid == 1121001 || skillid == 1221001 || skillid == 1321001) { // Monster Magnet
            int num = slea.readInt();
            int mobId;
            byte success;
            for (int i = 0; i < num; i++) {
                mobId = slea.readInt();
                success = slea.readByte();
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showMagnet(mobId, success), false);
                MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(mobId);
                if (monster != null)
                    monster.switchController(c.getPlayer(), monster.isControllerHasAggro());
            }
            byte direction = slea.readByte();
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showBuffeffect(c.getPlayer().getId(), skillid, 1, direction), false);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        } else if (skillid == 5121010) { // Timeleap
            MapleParty p = c.getPlayer().getParty();
            if (p != null)
                for (MaplePartyCharacter mpc : p.getMembers())
                    for (ChannelServer cserv : ChannelServer.getAllInstances())
                        if (cserv.getPlayerStorage().getCharacterById(mpc.getId()) != null)
                            cserv.getPlayerStorage().getCharacterById(mpc.getId()).removeAllCooldownsExcept(5121010);
            c.getPlayer().removeAllCooldownsExcept(5121010);
        } else if (skillid == 5101005) {// MP Recovery
            ISkill s = SkillFactory.getSkill(5101005);
            MapleStatEffect ef = s.getEffect(c.getPlayer().getSkillLevel(s));
            int lose = (int) c.getPlayer().getMaxHp() / ef.getX();
            c.getPlayer().setHp(c.getPlayer().getHp() - lose);
            c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getHp());
            int gain = (int) lose * (ef.getY() / 100);
            c.getPlayer().setMp(c.getPlayer().getMp() + gain);
            c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getMp());
        }
        if (slea.available() == 5)
            pos = new Point(slea.readShort(), slea.readShort());
        if (skillLevel == 0 || skillLevel != __skillLevel)
            return;
        else if (c.getPlayer().isAlive())
            if (skill.getId() != 2311002 || c.getPlayer().canDoor())
                skill.getEffect(skillLevel).applyTo(c.getPlayer(), pos);
            else {
                c.getPlayer().message("Please wait 5 seconds before casting Mystic Door again");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        else
            c.getSession().write(MaplePacketCreator.enableActions());
    }
}