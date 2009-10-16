package net.login.handler;

import java.util.Calendar;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DeleteCharHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int idate = slea.readInt();
        int cid = slea.readInt();
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, idate - year * 10000 - month * 100);
        int state = 0x12;
        if (c.checkBirthDate(cal)) {
            state = 0;
            if (!c.deleteCharacter(cid))
                state = 0x12;
        }
        c.getSession().write(MaplePacketCreator.deleteCharResponse(cid, state));
    }
}