package scripting.npc;

import java.util.HashMap;
import java.util.Map;
import javax.script.Invocable;
import client.MapleClient;
import client.MapleCharacter;
import scripting.AbstractScriptManager;

/**
 *
 * @author Matze
 */
public class NPCScriptManager extends AbstractScriptManager {
    private Map<MapleClient, NPCConversationManager> cms = new HashMap<MapleClient, NPCConversationManager>();
    private Map<MapleClient, NPCScript> scripts = new HashMap<MapleClient, NPCScript>();
    private static NPCScriptManager instance = new NPCScriptManager();

    public synchronized static NPCScriptManager getInstance() {
        return instance;
    }

    public void start(MapleClient c, int npc) {
        start(c, npc, null, null);
    }

    public void start(MapleClient c, int npc, String filename, MapleCharacter chr) {
        try {
            NPCConversationManager cm = new NPCConversationManager(c, npc, chr);
            cm.dispose();
            if (cms.containsKey(c))
                return;
            cms.put(c, cm);
            Invocable iv = getInvocable("npc/" + npc + ".js", c);
            if (filename != null)
                iv = getInvocable("npc/" + filename + ".js", c);
            if (iv == null || NPCScriptManager.getInstance() == null) {
                cm.dispose();
                return;
            }
            engine.put("cm", cm);
            NPCScript ns = iv.getInterface(NPCScript.class);
            scripts.put(c, ns);
            if (chr != null)
                ns.start(chr);
            else
                ns.start();
        } catch (Exception e) {
            System.out.println("Error: NPC" + npc);
            e.printStackTrace();
            dispose(c);
            cms.remove(c);
        }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) {
        NPCScript ns = scripts.get(c);
        if (ns != null)
            try {
                ns.action(mode, type, selection);
            } catch (Exception e) {
                System.out.println("Error executing NPC script.");
                e.printStackTrace();
                dispose(c);
            }
    }

    public void dispose(NPCConversationManager cm) {
        cms.remove(cm.getClient());
        scripts.remove(cm.getClient());
        resetContext("npc/" + cm.getNpc() + ".js", cm.getClient());
    }

    public void dispose(MapleClient c) {
        if (cms.get(c) != null)
            dispose(cms.get(c));
    }

    public NPCConversationManager getCM(MapleClient c) {
        return cms.get(c);
    }
}
