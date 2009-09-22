package server.life;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import client.MapleCharacter;
import server.maps.MapleMap;

public class SpawnPoint {
    private MapleMonster monster;
    private Point pos;
    private long nextPossibleSpawn;
    private int mobTime;
    private AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private boolean immobile;
    private boolean boss;

    public SpawnPoint(MapleMonster monster, Point pos, int mobTime) {
        super();
        this.monster = monster;
        this.pos = new Point(pos);
        this.mobTime = mobTime;
        this.immobile = !monster.isMobile();
        this.nextPossibleSpawn = System.currentTimeMillis();
        this.boss = monster.isBoss();
    }

    public boolean shouldSpawn() {
        if (mobTime < 0 || ((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 2)
            return false;
        return nextPossibleSpawn <= (System.currentTimeMillis());
    }

    public MapleMonster spawnMonster(MapleMap mapleMap) {
        MapleMonster mob = new MapleMonster(monster);
        mob.setPosition(new Point(pos));
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled(MapleMonster monster, MapleCharacter highestDamageChar) {
                nextPossibleSpawn = System.currentTimeMillis();
                if (mobTime > 0)
                    if (boss)
                        nextPossibleSpawn += mobTime / 10 * ((double) (2.5 + 10 * Math.random())) * 1000;
                    else
                        nextPossibleSpawn += mobTime * 1000;
                else
                    nextPossibleSpawn += monster.getAnimationTime("die1");
                spawnedMonsters.decrementAndGet();
            }
        });
        mapleMap.spawnMonster(mob);
        return mob;
    }
}