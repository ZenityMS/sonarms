package server;

import client.MapleCharacter;
import client.MapleJob;
import net.StringValueHolder;
import net.world.MaplePartyCharacter;
import tools.MaplePacketCreator;

/**
 *
 * @author AngelSL
 */
public class FourthJobQuestsPortalHandler {
    public enum FourthJobQuests implements StringValueHolder {
        RUSH("s4rush"),
        BERSERK("s4berserk");
        private final String name;

        private FourthJobQuests(String Newname) {
            this.name = Newname;
        }

        @Override
        public String getValue() {
            return name;
        }
    }

    public static boolean handlePortal(String name, MapleCharacter c) {
        if (name.equals(FourthJobQuests.RUSH.getValue())) {
            if (!(c.getParty().getLeader().getId() == c.getId()) && !checkRush(c)) {
                c.dropMessage("You step into the portal, but it swiftly kicks you out.");
                c.getClient().getSession().write(MaplePacketCreator.enableActions());
            }
            if (!(c.getParty().getLeader().getId() == c.getId()) && checkRush(c)) {
                c.dropMessage("You're not the party leader.");
                c.getClient().getSession().write(MaplePacketCreator.enableActions());
                return true;
            }
            if (!checkRush(c)) {
                c.dropMessage("Someone in your party is not a 4th Job warrior.");
                c.getClient().getSession().write(MaplePacketCreator.enableActions());
                return true;
            }
            c.getClient().getChannelServer().getEventSM().getEventManager("4jrush").startInstance(c.getParty(), c.getMap());
            return true;
        } else if (name.equals(FourthJobQuests.BERSERK.getValue())) {
            if (!c.haveItem(4031475)) {
                c.dropMessage("The portal to the Forgotten Shrine is locked");
                c.getClient().getSession().write(MaplePacketCreator.enableActions());
                return true;
            }
            c.getClient().getChannelServer().getEventSM().getEventManager("4jberserk").startInstance(c.getParty(), c.getMap());
            return true;
        }
        return false;
    }

    private static boolean checkRush(MapleCharacter c) {
        for (MaplePartyCharacter mpc : c.getParty().getMembers())
            if (mpc.getJobId() % 100 != 2 || !mpc.getJob().isA(MapleJob.WARRIOR))
                return false;
        return true;
    }
}
