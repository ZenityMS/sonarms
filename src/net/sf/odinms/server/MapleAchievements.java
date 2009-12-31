
package net.sf.odinms.server;

import java.util.ArrayList;
import java.util.List;
import net.sf.odinms.tools.Pair;

/**
 *
 * @author Patrick/PurpleMadness
 */
public class MapleAchievements {

    private List<Pair<Integer, MapleAchievement>> achievements = new ArrayList<Pair<Integer, MapleAchievement>>();
    private static MapleAchievements instance = null;

    protected MapleAchievements() {
        achievements.add(new Pair<Integer, MapleAchievement>(1, new MapleAchievement("finished the training camp", 250, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(2, new MapleAchievement("completed the BossQuest", 17500)));
        achievements.add(new Pair<Integer, MapleAchievement>(3, new MapleAchievement("killed Anego", 3500)));
        achievements.add(new Pair<Integer, MapleAchievement>(4, new MapleAchievement("reached Level 70", 5000)));
        achievements.add(new Pair<Integer, MapleAchievement>(5, new MapleAchievement("reached Level 120", 7500)));
        achievements.add(new Pair<Integer, MapleAchievement>(6, new MapleAchievement("killed a boss", 1000)));
        achievements.add(new Pair<Integer, MapleAchievement>(7, new MapleAchievement("equipped a dragon item", 3000)));
        achievements.add(new Pair<Integer, MapleAchievement>(8, new MapleAchievement("reached the meso cap", 10000)));
        achievements.add(new Pair<Integer, MapleAchievement>(9, new MapleAchievement("reached 50 fame", 2000)));
        achievements.add(new Pair<Integer, MapleAchievement>(10, new MapleAchievement("killed their first Papulatus", 2500)));
        achievements.add(new Pair<Integer, MapleAchievement>(11, new MapleAchievement("saw a GM", 500, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(12, new MapleAchievement("succesfully scrolled an item", 1000, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(13, new MapleAchievement("earned a Zakum Helm", 2500)));
        achievements.add(new Pair<Integer, MapleAchievement>(14, new MapleAchievement("said cc plz", 100, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(15, new MapleAchievement("flew to Victoria Island by Shanks", 500, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(16, new MapleAchievement("killed the almighty Zakum", 5000)));
        achievements.add(new Pair<Integer, MapleAchievement>(17, new MapleAchievement("completed a trade", 250, false)));
        achievements.add(new Pair<Integer, MapleAchievement>(18, new MapleAchievement("killed a Snail", 100, false)));
            achievements.add(new Pair<Integer, MapleAchievement>(19, new MapleAchievement("killed a Pianus", 2500)));
            achievements.add(new Pair<Integer, MapleAchievement>(20, new MapleAchievement("hit more than 10,000 damage to one monster", 3000)));
            achievements.add(new Pair<Integer, MapleAchievement>(21, new MapleAchievement("hit 99,999 damage to one monster", 6000)));
            achievements.add(new Pair<Integer, MapleAchievement>(22, new MapleAchievement("reached level 200", 35000)));
            achievements.add(new Pair<Integer, MapleAchievement>(23, new MapleAchievement("won Field of Judgement", 3500)));
            achievements.add(new Pair<Integer, MapleAchievement>(24, new MapleAchievement("created a Guild", 2000)));
            achievements.add(new Pair<Integer, MapleAchievement>(25, new MapleAchievement("completed the Guild Quest", 3000)));
        achievements.add(new Pair<Integer, MapleAchievement>(26, new MapleAchievement("killed Horntail", 30000)));
    }

    public static MapleAchievements getInstance() {
        if (instance == null) {
            instance = new MapleAchievements();
        }
        return instance;
    }

    public MapleAchievement getById(int id) {
        for (Pair<Integer, MapleAchievement> achievement : this.achievements) {
            if (achievement.getLeft() == id) {
                return achievement.getRight();
            }
        }
        return null;
    }

    public Integer getByMapleAchievement(MapleAchievement ma) {
        for (Pair<Integer, MapleAchievement> achievement : this.achievements) {
            if (achievement.getRight() == ma) {
                return achievement.getLeft();
            }
        }
        return null;
    }
}  