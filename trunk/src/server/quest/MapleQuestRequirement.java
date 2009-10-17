package server.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import client.IItem;
import client.MapleCharacter;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleQuestStatus;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;

/**
 *
 * @author Matze
 */
public class MapleQuestRequirement {
    private MapleQuestRequirementType type;
    private MapleData data;
    private MapleQuest quest;

    public MapleQuestRequirement(MapleQuest quest, MapleQuestRequirementType type, MapleData data) {
        this.type = type;
        this.data = data;
        this.quest = quest;
    }

    boolean check(MapleCharacter c, Integer npcid) {
        switch (getType()) {
            case JOB:
                for (MapleData jobEntry : getData().getChildren())
                    if (c.getJob().equals(MapleJob.getById(MapleDataTool.getInt(jobEntry))) || c.gmLevel() > 0)
                        return true;
                return false;
            case QUEST:
                for (MapleData questEntry : getData().getChildren()) {
                    MapleQuestStatus q = c.getQuest(MapleQuest.getInstance(MapleDataTool.getInt(questEntry.getChildByPath("id"))));
                    if (q == null && MapleQuestStatus.Status.getById(MapleDataTool.getInt(questEntry.getChildByPath("state"))).equals(MapleQuestStatus.Status.NOT_STARTED))
                        continue;
                    if (q == null || !q.getStatus().equals(MapleQuestStatus.Status.getById(MapleDataTool.getInt(questEntry.getChildByPath("state")))))
                        return false;
                }
                return true;
            case ITEM:
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (MapleData itemEntry : getData().getChildren()) {
                    int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
                    short quantity = 0;
                    MapleInventoryType iType = ii.getInventoryType(itemId);
                    for (IItem item : c.getInventory(iType).listById(itemId))
                        quantity += item.getQuantity();
                    if (quantity < MapleDataTool.getInt(itemEntry.getChildByPath("count")) || MapleDataTool.getInt(itemEntry.getChildByPath("count")) <= 0 && quantity > 0)
                        return false;
                }
                return true;
            case MIN_LEVEL:
                return c.getLevel() >= MapleDataTool.getInt(getData());
            case MAX_LEVEL:
                return c.getLevel() <= MapleDataTool.getInt(getData());
            case END_DATE:
                String timeStr = MapleDataTool.getString(getData());
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(timeStr.substring(0, 4)), Integer.parseInt(timeStr.substring(4, 6)), Integer.parseInt(timeStr.substring(6, 8)), Integer.parseInt(timeStr.substring(8, 10)), 0);
                return cal.getTimeInMillis() >= System.currentTimeMillis();
            case MOB:
                for (MapleData mobEntry : getData().getChildren()) {
                    int mobId = MapleDataTool.getInt(mobEntry.getChildByPath("id"));
                    int killReq = MapleDataTool.getInt(mobEntry.getChildByPath("count"));
                    if (c.getQuest(quest).getMobKills(mobId) < killReq)
                        return false;
                }
                return true;
            case NPC:
                return npcid == null || npcid == MapleDataTool.getInt(getData());
            case FIELD_ENTER:
                MapleData zeroField = getData().getChildByPath("0");
                if (zeroField != null)
                    return MapleDataTool.getInt(zeroField) == c.getMapId();
                return false;
            case INTERVAL:
                return !c.getQuest(quest).getStatus().equals(MapleQuestStatus.Status.COMPLETED) || c.getQuest(quest).getCompletionTime() <= System.currentTimeMillis() - MapleDataTool.getInt(getData()) * 60 * 1000;
            default:
                return true;
        }
    }

    public MapleQuestRequirementType getType() {
        return type;
    }

    public MapleData getData() {
        return data;
    }

    @Override
    public String toString() {
        return type.toString() + " " + data.toString() + " " + quest.toString();
    }

    List<Integer> getQuestItemsToShowOnlyIfQuestIsActivated() {
        if (type != MapleQuestRequirementType.ITEM)
            return null;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Integer> delta = new ArrayList<Integer>();
        for (MapleData itemEntry : getData().getChildren()) {
            int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
            if (ii.isQuestItem(itemId))
                delta.add(itemId);
        }
        return Collections.unmodifiableList(delta);
    }
}