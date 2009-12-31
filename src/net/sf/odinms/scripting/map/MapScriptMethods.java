package net.sf.odinms.scripting.map;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.scripting.AbstractPlayerInteraction;

import net.sf.odinms.client.MapleCharacter;

public class MapScriptMethods extends AbstractPlayerInteraction {
    public MapScriptMethods(MapleClient c) {
    	super(c);
    }

    @Deprecated
    public MapleCharacter getChar() {
        return getPlayer();
    }
}
