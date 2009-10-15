package client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

/**
 *
 * @author Danny (Leifde)
 */
public class PetDataFactory {
    private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
    private static Map<Pair<Integer, Integer>, PetCommand> petCommands = new HashMap<Pair<Integer, Integer>, PetCommand>();
    private static Map<Integer, Integer> petHunger = new HashMap<Integer, Integer>();

    public static PetCommand getPetCommand(int petId, int skillId) {
        PetCommand ret = petCommands.get(new Pair<Integer, Integer>(Integer.valueOf(petId), Integer.valueOf(skillId)));
        if (ret != null)
            return ret;
        synchronized (petCommands) {
            ret = petCommands.get(new Pair<Integer, Integer>(Integer.valueOf(petId), Integer.valueOf(skillId)));
            if (ret == null) {
                MapleData skillData = dataRoot.getData("Pet/" + petId + ".img");
                int prob = 0;
                int inc = 0;
                if (skillData != null) {
                    prob = MapleDataTool.getInt("interact/" + skillId + "/prob", skillData, 0);
                    inc = MapleDataTool.getInt("interact/" + skillId + "/inc", skillData, 0);
                }
                ret = new PetCommand(petId, skillId, prob, inc);
                petCommands.put(new Pair<Integer, Integer>(Integer.valueOf(petId), Integer.valueOf(skillId)), ret);
            }
            return ret;
        }
    }

    public static int getHunger(int petId) {
        Integer ret = petHunger.get(Integer.valueOf(petId));
        if (ret != null)
            return ret;
        synchronized (petHunger) {
            ret = petHunger.get(Integer.valueOf(petId));
            if (ret == null)
                ret = Integer.valueOf(MapleDataTool.getInt(dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry"), 1));
            return ret;
        }
    }
}