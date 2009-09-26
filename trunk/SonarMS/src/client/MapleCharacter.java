package client;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import client.anticheat.CheatTracker;
import tools.DatabaseConnection;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.remote.WorldChannelInterface;
import scripting.event.EventInstanceManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.HiredMerchant;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer {
    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int id;
    private int level;
    private int reborns;
    private int str;
    private int dex;
    private int luk;
    private int int_;
    private int hp;
    private int maxhp;
    private int mp;
    private int maxmp;
    private int mpApUsed;
    private int hpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int remainingSp;
    private int savedLocations[];
    private int fame;
    private int initialSpawnPoint,  mapid;
    private int gender;
    private int currentPage;
    private int currentType = 0;
    private int currentTab = 1;
    private int chair;
    private int itemEffect;
    private int paypalnx;
    private int maplepoints;
	private int pvpkills;
	private int pvpdeaths;
    private int cardnx;
    private int guildid;
    private int guildrank;
    private int messengerposition = 4;
    private int slots = 0;
    private int canTalk;
    private int partnerid;
    private int marriageQuestLevel;
    private int married;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private long lastfametime;
    private transient int localmaxhp;
    private transient int localmaxmp;
    private transient int localstr;
    private transient int localdex;
    private transient int localluk;
    private transient int localint_;
    private transient int magic;
    private transient int watk;
    private transient int localmaxbasedamage;
    private boolean hidden;
    private boolean canDoor = true;
    private boolean incs;
    private boolean inmts;
    private boolean gmchattype;
    private boolean Berserk;
    private boolean hasMerchant;
    private String name;
    private String chalktext;
    private String search = null;
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private BuddyList buddylist;
    private CheatTracker anticheat;
    private EventInstanceManager eventInstance = null;
    private HiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    private MapleMap map;
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = Collections.synchronizedMap(new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>());
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?> fullnessSchedule;
    private ScheduledFuture<?> fullnessSchedule_1;
    private ScheduledFuture<?> fullnessSchedule_2;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private static List<Pair<Byte, Integer>> inventorySlots = new ArrayList<Pair<Byte, Integer>>();
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private List<Pair<Integer, String>> commands = new ArrayList<Pair<Integer, String>>();
    public long lastSmega = 0;//lol public
    private PirateShip pirateShip = null;
    private boolean inHide;

    private MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values())
            inventory[type.ordinal()] = new MapleInventory(type, (byte) getSlots(type));
        savedLocations = new int[SavedLocationType.values().length];
        for (int i = 0; i < SavedLocationType.values().length; i++)
            savedLocations[i] = -1;
        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        anticheat = new CheatTracker(this);
        setPosition(new Point(0, 0));
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = c.gmLevel();
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
        ret.maplemount = null;
        int[] num1 = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 29, 31, 34, 35, 37, 38, 40, 41, 43, 44, 45, 46, 48, 50, 56, 57, 59, 60, 61, 62, 63, 64, 65};
        int[] num2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6};
        int[] num3 = {10, 12, 13, 18, 24, 21, 8, 5, 0, 4, 1, 19, 14, 15, 52, 2, 17, 11, 3, 20, 16, 23, 9, 50, 51, 6, 22, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106};
        for (int i = 0; i < num1.length; i++)// key, type, action
            ret.keymap.put(num1[i], new MapleKeyBinding(num2[i], num3[i]));
        return ret;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId)))
            this.coolDowns.remove(skillId);
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCommandToList(String command) {
        commands.add(new Pair<Integer, String>(this.getId(), command));
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMarriageQuestLevel() {
        marriageQuestLevel++;
    }
	
	public int getPvpKills() {
        return pvpkills;
    }
	
	public int getPvpDeaths() {
        return pvpdeaths;
    }

    public boolean inJail() {
        return getMapId() == 200090300 || getMapId() == 980000404;
    }

    public void setPvpDeaths(int amount) {
        this.pvpdeaths = amount;
    }

    public void setPvpKills(int amount) {
        this.pvpkills = amount;
    }

    public void gainPvpDeath() {
        this.pvpdeaths += 1;
    }

    public void gainPvpKill() {
        this.pvpkills += 1;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.MP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++)
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str = getStr() + up;
            updateSingleStat(MapleStat.STR, getStr());
        } else if (type == 2) {
            this.dex = getDex() + up;
            updateSingleStat(MapleStat.DEX, getDex());
        } else if (type == 3) {
            this.int_ = getInt() + up;
            updateSingleStat(MapleStat.INT, getInt());
        } else if (type == 4) {
            this.luk = getLuk() + up;
            updateSingleStat(MapleStat.LUK, getLuk());
        }
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason, boolean dc) {
        try {
            getClient().banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            ps.setString(1, client.getSession().getRemoteAddress().toString().split(":")[0]);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        if (!dc) {
            client.getSession().write(MaplePacketCreator.sendGMPolice(0, reason, 1000000));
            TimerManager.getInstance().schedule(new Runnable() {
                public void run() {
                    client.getSession().close();
                }
            }, 10000);
        } else
            this.getClient().disconnect();
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId)
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            else
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
                psb.setString(1, reason);
                psb.setInt(2, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
        }
        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        if (watk == 0)
            maxbasedamage = 1;
        else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                int mainstat;
                int secondarystat;
                if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                } else {
                    mainstat = localstr;
                    secondarystat = localdex;
                }
                maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk) + 10;
            } else
                maxbasedamage = 0;
        }
        return maxbasedamage;
    }

    public void cancelAllBuffs() {
        for (MapleBuffStatValueHolder mbsvh : new LinkedList<MapleBuffStatValueHolder>(effects.values()))
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public static class CancelCooldownAction implements Runnable {
        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.getClient().getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite)
            buffstats = getBuffStats(effect, startTime);
        else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups)
                buffstats.add(statup.getLeft());
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor())
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters())
                    door.sendDestroyData(chr.getClient());
                for (MapleCharacter chr : door.getTown().getCharacters())
                    door.sendDestroyData(chr.getClient());
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        if (effect.isMonsterRiding())
            if (effect.getSourceId() != 5221006) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
            if (effect.isHide() && (MapleCharacter) getMap().getMapObject(getObjectId()) != null) {
                this.hidden = false;
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                for (int i = 0; i < 3; i++)
                    if (pets[i] != null)
                        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pets[i], false, false), false);
            }
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

    public void cancelFullnessSchedule(int petSlot) {
        switch (petSlot) {
            case 0:
                fullnessSchedule.cancel(false);
            case 1:
                fullnessSchedule_1.cancel(false);
            case 2:
                fullnessSchedule_2.cancel(false);
        }
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new LinkedList<MapleBuffStatValueHolder>(effects.values()))
            if (mbsvh.effect.isMagicDoor())
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null)
            mapTimeLimitTask.cancel(false);
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            getClient().getSession().write(MaplePacketCreator.cancelBuff(buffstats));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public boolean canDoor() {
        return canDoor;
    }
	
	public boolean isPvPMap() {
        return getMapId() == 800020400;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel() > 0)
            return FameStatus.OK;
        else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24)
            return FameStatus.NOT_TODAY;
        else if (lastmonthfameids.contains(Integer.valueOf(from.getId())))
            return FameStatus.NOT_THIS_MONTH;
        else
            return FameStatus.OK;
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void changeJob(MapleJob newJob) {
        this.job = newJob;
        this.remainingSp++;
        if (newJob.getId() % 10 == 2)
            this.remainingSp += 2;
        else if (newJob.getId() % 10 > 1)
            this.remainingAp += 5;
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
        updateSingleStat(MapleStat.JOB, newJob.getId());
        int job_ = this.job.getId();
        if (job_ == 100)
            maxhp += rand(200, 250);
        else if (job_ == 200)
            maxmp += rand(100, 150);
        else if (job_ % 100 == 0) {
            maxhp += rand(100, 150);
            maxhp += rand(25, 50);
        } else if (job_ > 0 && job_ < 200)
            maxhp += rand(300, 350);
        else if (job_ < 300)
            maxmp += rand(450, 500);
        else if (job_ > 0) {
            maxhp += rand(300, 350);
            maxmp += rand(150, 200);
        }
        if (maxhp >= 30000)
            maxhp = 30000;
        if (maxmp >= 30000)
            maxmp = 30000;
        setHp(maxhp);
        setMp(maxmp);
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        recalcLocalStats();
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        silentPartyUpdate();
        guildUpdate();
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0)
            keymap.put(Integer.valueOf(key), keybinding);
        else
            keymap.remove(Integer.valueOf(key));
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, portal);
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        if (to.getId() == 100000200 || to.getId() == 211000100 || to.getId() == 220000300)
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this));
        else
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
        warpPacket.setOnSend(new Runnable() {
            @Override
            public void run() {
                map.removePlayer(MapleCharacter.this);
                if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    map = to;
                    setPosition(pos);
                    map.addPlayer(MapleCharacter.this);
                    if (party != null) {
                        silentPartyUpdate();
                        getClient().getSession().write(MaplePacketCreator.updateParty(getClient().getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                        updatePartyMemberHP();
                    }
                    if (getMap().getHPDec() > 0)
                        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                doHurtHp();
                            }
                        }, 10000);
                }
            }
        });
        getClient().getSession().write(warpPacket);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
        skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
        this.getClient().getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null)
            BerserkSchedule.cancel(false);
        final MapleCharacter chr = this;
        ISkill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = getSkillLevel(BerserkX);
        if (chr.getJob().equals(MapleJob.DARKKNIGHT) && skilllevel > 0) {
            Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
            BerserkSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    getClient().getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                }
            }, 5000, 3000);
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1)
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                wci.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(client.getPlayer(), messengerposition), messengerposition);
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro())
            if (monster.getController() == this)
                monster.setControllerHasAggro(true);
            else
                monster.switchController(this, true);
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = -1;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel)
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect)
                            addMbsvh = false;
                    if (addMbsvh)
                        effectsToCancel.add(mbsvh);
                    if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true));
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == 1321007) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel)
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0)
                    cancelEffectCancelTasks.schedule.cancel(false);
        }
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1)
            this.getClient().disconnect();
        try {
            client.getChannelServer().getWorldInterface().disbandGuild(this.guildid);
        } catch (Exception e) {
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new LinkedList<MapleBuffStatValueHolder>(effects.values()))
            if (mbsvh.effect.isSkill())
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    public void dispelDebuffs() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases)
            if (disease == MapleDisease.WEAKEN || disease != MapleDisease.DARKNESS || disease != MapleDisease.SEAL || disease != MapleDisease.POISON) {
                disease_.add(disease);
                getClient().getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            } else
                return;
        this.diseases.clear();
    }

    public void dispelSeduce() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases)
            if (disease == MapleDisease.SEDUCE) {
                disease_.add(disease);
                getClient().getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        this.diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs)
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() == 1004 || mbsvh.effect.getSourceId() == 1321007 || mbsvh.effect.getSourceId() == 2121005 || mbsvh.effect.getSourceId() == 2221005 || mbsvh.effect.getSourceId() == 2311006 || mbsvh.effect.getSourceId() == 2321003 || mbsvh.effect.getSourceId() == 3111002 || mbsvh.effect.getSourceId() == 3111005 || mbsvh.effect.getSourceId() == 3211002 || mbsvh.effect.getSourceId() == 3211005 || mbsvh.effect.getSourceId() == 4111002))
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid)
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null)
            return;
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void doReborn() {
        reborns++;
        setLevel(1);
        setExp(0);
        setJob(MapleJob.BEGINNER);
        updateSingleStat(MapleStat.LEVEL, 1);
        updateSingleStat(MapleStat.JOB, 0);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void message(String m) {
        dropMessage(6, m);
    }

    public void dropMessage(int type, String message) {
        client.getSession().write(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0)
            getClient().getSession().write(MaplePacketCreator.updatePlayerStats(stats));
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getClient().getPlayer().getMessenger() != null) {
            WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
            try {
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (Exception e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
    }

    public enum FameStatus {
        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        if (getLevel() < getClient().getChannelServer().getLevelCap()) {
            if ((long) this.exp.get() + (long) gain > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                gain -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(gain));
        } else
            return;
        if (show && gain != 0)
            client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white));
        if (getClient().getChannelServer().getMultiLevel())
            while (level < getClient().getChannelServer().getLevelCap() && exp.get() >= ExpTable.getExpNeededForLevel(level))
                levelUp();
        else if (level < getClient().getChannelServer().getLevelCap() && exp.get() >= ExpTable.getExpNeededForLevel(level)) {
            levelUp();
            int need = ExpTable.getExpNeededForLevel(level);
            if (exp.get() >= need) {
                setExp(need - 1);
                updateSingleStat(MapleStat.EXP, need);
            }
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions) {
        gainMeso(gain, show, enableActions, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show)
            client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, inChat));
    }

    public void gainSlots(byte b, int slots) {
        setSlots(b, getSlots(b) + slots);
    }

    public void genericGuildMessage(int code) {
        this.client.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values())
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values())
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        return ret;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null)
            return null;
        return Long.valueOf(mbsvh.startTime);
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null)
            return null;
        return Integer.valueOf(mbsvh.value);
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null)
            return -1;
        return mbsvh.effect.getSourceId();
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet())
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime))
                stats.add(stateffect.getKey());
        return stats;
    }

    public boolean getCanTalk() {
        return this.canTalk != 2;
    }

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public CheatTracker getCheatTracker() {
        return anticheat;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values())
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED))
                ret.add(q);
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return this.paypalnx;
            case 2:
                return this.maplepoints;
            default:
                return this.cardnx;
        }
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxBaseDamage() {
        return localmaxbasedamage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public List<MapleDisease> getDiseases() {
        synchronized (diseases) {
            return Collections.unmodifiableList(diseases);
        }
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public int getDropMod() {
        int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11))
            return 2;
        return 1;
    }

    public int getEnergyBar() {
        return this.energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public int getExp() {
        return exp.get();
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public int getGender() {
        return gender;
    }

    public boolean getGMChat() {
        return !gmchattype;
    }

    public MapleGuild getGuild() {
        try {
            return getClient().getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public int getHair() {
        return hair;
    }

    public boolean getHide() {
        return inHide;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpApUsed() {
        return hpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getIdByName(String name, int world) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ? AND world = ?");
            ps.setString(1, name);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
        }
        return -1;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = inventory[type.ordinal()];
        int possesed = iv.countById(itemid);
        if (checkEquipped)
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);

        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getLevel() {
        return level;
    }

    public int getLuk() {
        return luk;
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null)
            return map.getId();
        return mapid;
    }

    public int getMarriageQuestLevel() {
        return marriageQuestLevel;
    }

    public int getMasterLevel(ISkill skill) {
        if (skills.get(skill) == null)
            return 0;
        return skills.get(skill).masterlevel;
    }

    public int getMatchCardPoints(String type) {
        int points = 0;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            points = rs.getInt("matchcard" + type);
            rs.close();
            ps.close();
            return points;
        } catch (Exception e) {
        }
        return points;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public int getMpApUsed() {
        return mpApUsed;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++)
            if (pets[i] == null)
                return i;
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                ret++;
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public int getNXCodeItem(String code) throws SQLException {
        int item = -1;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `item` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            item = rs.getInt("item");
        rs.close();
        ps.close();
        return item;
    }

    public int getNXCodeType(String code) throws SQLException {
        int type = -1;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `type` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            type = rs.getInt("type");
        rs.close();
        ps.close();
        return type;
    }

    public boolean getNXCodeValid(String code, boolean validcode) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            validcode = rs.getInt("valid") == 0 ? false : true;
        rs.close();
        ps.close();
        return validcode;
    }

    public int getOmokPoints(String type) {
        int points = 0;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                ps.close();
            points = rs.getInt("omok" + type);
            ps.close();
            return points;
        } catch (Exception e) {
        }
        return points;
    }

    public MapleCharacter getPartner() {
        MapleCharacter test = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(partnerid);
        return test != null ? test : null;
    }

    public int getPartnerId() {
        return partnerid;
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public int getPetIndex(int petId) {
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                if (pets[i].getUniqueId() == petId)
                    return i;
        return -1;
    }

    public int getPetIndex(MaplePet pet) {
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                if (pets[i].getUniqueId() == pet.getUniqueId())
                    return i;
        return -1;
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public PirateShip getPirateShip() {
        return pirateShip;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest))
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        return quests.get(quest);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getSavedLocation(SavedLocationType type) {
        return savedLocations[type.ordinal()];
    }

    public int getFM() {
        int m = savedLocations[SavedLocationType.FREE_MARKET.ordinal()];
        clearSavedLocation(SavedLocationType.FREE_MARKET);
        return m;
    }

    public String getSearch() {
        return this.search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(ISkill skill) {
        if (skills.get(skill) == null)
            return 0;
        return skills.get(skill).skillevel;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public int getSlots(byte b) {
        for (Pair curPair : inventorySlots)
            if ((Byte) curPair.getLeft() == b)
                return (Integer) curPair.getRight();
        return 100;
    }

    public int getSlots(MapleInventoryType t) {
        return getSlots(t.getType());
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values())
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED))
                ret.add(q);
        return Collections.unmodifiableList(ret);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null)
            return null;
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        int time = (int) ((length + starttime) - System.currentTimeMillis());
        addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time));
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        if (this.isAlive() && diseases.size() < 2) {
            List<Pair<MapleDisease, Integer>> disease_ = new ArrayList<Pair<MapleDisease, Integer>>();
            disease_.add(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
            this.diseases.add(disease);
            getClient().getSession().write(MaplePacketCreator.giveDebuff(disease_, skill));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, disease_, skill), false);
        }
    }

    public int gmLevel() {
        return this.gmLevel;
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    public void guildUpdate() {
        if (this.guildid < 1)
            return;
        mgc.setLevel(this.level);
        mgc.setJobId(this.job.getId());
        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
        } catch (Exception re) {
        }
    }

    public void handleEnergyChargeGain() {
        ISkill energycharge = SkillFactory.getSkill(5110001);
        if (getSkillLevel(energycharge) > 0) {
            if (energybar < 10000) {
                energybar += 102;
                if (energybar > 10000)
                    energybar = 10000;
                getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(energybar, 0));
            }
            if (energybar >= 10000 && energybar < 11000) {
                energybar = 15000;
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(0, 0));
                        energybar = 0;
                    }
                }, energycharge.getEffect(getSkillLevel(energycharge)).getDuration());
            }
        }
    }

    public void handleOrbconsume() {
        ISkill combo = SkillFactory.getSkill(1111002);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat, false, false, getMount()));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
    }

    public void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO);
        ISkill combo = SkillFactory.getSkill(1111002);
        ISkill advcombo = SkillFactory.getSkill(1120003);
        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getSkillLevel(advcombo);
        if (advComboSkillLevel > 0)
            ceffect = advcombo.getEffect(advComboSkillLevel);
        else
            ceffect = combo.getEffect(getSkillLevel(combo));
        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult())
                if (neworbcount < ceffect.getX() + 1)
                    neworbcount++;
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));
            getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat, false, false, getMount()));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
        }
    }

    public int hasEXPCard() {
        int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11))
            return 2;
        return 1;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return haveItem(itemid, 1, false, true);
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped)
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        return greaterOrEquals ? possesed >= quantity : possesed == quantity;
    }

    public void increaseGuildCapacity() {
        if (this.getMeso() < 1500000) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(this.guildid);
        } catch (Exception e) {
            client.getChannelServer().reconnectWorld();
            return;
        }
        this.gainMeso(-1500000, true, false, true);
    }

    public boolean inCS() {
        return this.incs;
    }

    public boolean inMTS() {
        return this.inmts;
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs)
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid)
                return true;
        return false;
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null)
            return false;
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMarried() {
        return married > 0;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return this.party.getLeader() == this.party.getMemberById(this.getId());
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0)
            chair = 0;
        if (hpDecreaseTask != null)
            hpDecreaseTask.cancel(false);
    }

    public void levelUp() {
        ISkill improvingMaxHP = null;
        int improvingMaxHPLevel = 0;
        ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
        int improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
        remainingAp += 5;
        if (job == MapleJob.BEGINNER) {
            maxhp += rand(12, 16);
            maxmp += rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR)) {
            improvingMaxHP = SkillFactory.getSkill(1000001);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(24, 28);
            maxmp += rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN)) {
            maxhp += rand(10, 14);
            maxmp += rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF)) {
            maxhp += rand(20, 24);
            maxmp += rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.PIRATE)) {
            improvingMaxHP = SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(22, 28);
            maxmp += rand(18, 23);
        }
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE)))
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job == MapleJob.CRUSADER || job == MapleJob.HERO))
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        maxmp += getTotalInt() / 10;
        exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
        level++;
        if (level >= getClient().getChannelServer().getLevelCap() && gmLevel() < 1) {
            exp.set(0);
            try {
                getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), MaplePacketCreator.serverNotice(6, "[Notice] " + getName() + " has reached Level " + level + " !").getBytes());
            } catch (Exception e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(remainingAp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, Integer.valueOf(exp.get())));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, Integer.valueOf(level)));
        if (job != MapleJob.BEGINNER) {
            remainingSp += 3;
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, Integer.valueOf(remainingSp)));
        }
        setHp(maxhp);
        setMp(maxmp);
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        silentPartyUpdate();
        guildUpdate();
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
        ps.setInt(1, charid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next())
            throw new RuntimeException("Loading char failed (not found)");
        ret.name = rs.getString("name");
        ret.level = rs.getInt("level");
		ret.pvpdeaths = rs.getInt("pvpdeaths");
        ret.pvpkills = rs.getInt("pvpkills");
        ret.reborns = rs.getInt("reborns");
        ret.fame = rs.getInt("fame");
        ret.str = rs.getInt("str");
        ret.dex = rs.getInt("dex");
        ret.int_ = rs.getInt("int");
        ret.luk = rs.getInt("luk");
        ret.exp.set(rs.getInt("exp"));
        ret.hp = rs.getInt("hp");
        ret.maxhp = rs.getInt("maxhp");
        ret.mp = rs.getInt("mp");
        ret.maxmp = rs.getInt("maxmp");
        ret.hpApUsed = rs.getInt("hpApUsed");
        ret.mpApUsed = rs.getInt("mpApUsed");
        ret.hasMerchant = rs.getInt("HasMerchant") == 1;
        ret.remainingSp = rs.getInt("sp");
        ret.remainingAp = rs.getInt("ap");
        ret.meso.set(rs.getInt("meso"));
        ret.gmLevel = rs.getInt("gm");
        ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
        ret.gender = rs.getInt("gender");
        ret.job = MapleJob.getById(rs.getInt("job"));
        ret.hair = rs.getInt("hair");
        ret.face = rs.getInt("face");
        ret.accountid = rs.getInt("accountid");
        ret.mapid = rs.getInt("map");
        ret.initialSpawnPoint = rs.getInt("spawnpoint");
        ret.world = rs.getInt("world");
        ret.rank = rs.getInt("rank");
        ret.rankMove = rs.getInt("rankMove");
        ret.jobRank = rs.getInt("jobRank");
        ret.jobRankMove = rs.getInt("jobRankMove");
        int mountexp = rs.getInt("mountexp");
        int mountlevel = rs.getInt("mountlevel");
        int mounttiredness = rs.getInt("mounttiredness");
        ret.guildid = rs.getInt("guildid");
        ret.guildrank = rs.getInt("guildrank");
        if (ret.guildid > 0)
            ret.mgc = new MapleGuildCharacter(ret);
        int buddyCapacity = rs.getInt("buddyCapacity");
        ret.buddylist = new BuddyList(buddyCapacity);
        Pair<Byte, Integer> e = new Pair<Byte, Integer>((byte) 1, rs.getInt("equipslots"));
        inventorySlots.add(e);
        e = new Pair<Byte, Integer>((byte) 2, rs.getInt("useslots"));
        inventorySlots.add(e);
        e = new Pair<Byte, Integer>((byte) 3, rs.getInt("setupslots"));
        inventorySlots.add(e);
        e = new Pair<Byte, Integer>((byte) 4, rs.getInt("etcslots"));
        inventorySlots.add(e);
        if (channelserver) {
            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null)
                ret.map = mapFactory.getMap(100000000);
            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0);
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());
            int partyid = rs.getInt("party");
            if (partyid >= 0)
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null && party.getMemberById(ret.id) != null)
                        ret.party = party;
                } catch (Exception ex) {
                    client.getChannelServer().reconnectWorld();
                }
            int messengerid = rs.getInt("messengerid");
            int position = rs.getInt("messengerposition");
            if (messengerid > 0 && position < 4 && position > -1)
                try {
                    WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                    MapleMessenger messenger = wci.getMessenger(messengerid);
                    if (messenger != null) {
                        ret.messenger = messenger;
                        ret.messengerposition = position;
                    }
                } catch (Exception ez) {
                    client.getChannelServer().reconnectWorld();
                }
        }
        rs.close();
        ps.close();
        ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
        ps.setInt(1, ret.accountid);
        rs = ps.executeQuery();
        while (rs.next()) {
            ret.getClient().setAccountName(rs.getString("name"));
            ret.paypalnx = rs.getInt("paypalNX");
            ret.maplepoints = rs.getInt("mPoints");
            ret.cardnx = rs.getInt("cardNX");
        }
        rs.close();
        ps.close();
        String sql = "SELECT * FROM inventoryitems LEFT JOIN inventoryequipment USING (inventoryitemid) WHERE characterid = ?";
        if (!channelserver)
            sql += " AND inventorytype = " + MapleInventoryType.EQUIPPED.getType();
        ps = con.prepareStatement(sql);
        ps.setInt(1, charid);
        rs = ps.executeQuery();
        while (rs.next()) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) rs.getInt("inventorytype"));
            if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
                int itemid = rs.getInt("itemid");
                Equip equip = new Equip(itemid, (byte) rs.getInt("position"), rs.getInt("ringid"));
                equip.setOwner(rs.getString("owner"));
                equip.setQuantity((short) rs.getInt("quantity"));
                equip.setAcc((short) rs.getInt("acc"));
                equip.setAvoid((short) rs.getInt("avoid"));
                equip.setDex((short) rs.getInt("dex"));
                equip.setHands((short) rs.getInt("hands"));
                equip.setHp((short) rs.getInt("hp"));
                equip.setInt((short) rs.getInt("int"));
                equip.setJump((short) rs.getInt("jump"));
                equip.setLuk((short) rs.getInt("luk"));
                equip.setMatk((short) rs.getInt("matk"));
                equip.setMdef((short) rs.getInt("mdef"));
                equip.setMp((short) rs.getInt("mp"));
                equip.setSpeed((short) rs.getInt("speed"));
                equip.setStr((short) rs.getInt("str"));
                equip.setWatk((short) rs.getInt("watk"));
                equip.setWdef((short) rs.getInt("wdef"));
                equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                equip.setLocked((byte) rs.getInt("locked"));
                equip.setLevel((byte) rs.getInt("level"));
                ret.getInventory(type).addFromDB(equip);
            } else {
                Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                item.setOwner(rs.getString("owner"));
                ret.getInventory(type).addFromDB(item);
            }
        }
        rs.close();
        ps.close();
        if (channelserver) {
            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
            while (rs.next()) {
                MapleQuest q = MapleQuest.getInstance(rs.getInt("quest"));
                MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                long cTime = rs.getLong("time");
                if (cTime > -1)
                    status.setCompletionTime(cTime * 1000);
                status.setForfeited(rs.getInt("forfeited"));
                ret.quests.put(q, status);
                pse.setInt(1, rs.getInt("queststatusid"));
                ResultSet rsMobs = pse.executeQuery();
                while (rsMobs.next())
                    status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                rsMobs.close();
            }
            rs.close();
            ps.close();
            pse.close();
            ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel FROM skills WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next())
                ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getInt("skilllevel"), rs.getInt("masterlevel")));
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int position = rs.getInt("position");
                SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                ret.skillMacros[position] = macro;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int key = rs.getInt("key");
                int type = rs.getInt("type");
                int action = rs.getInt("action");
                ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                String locationType = rs.getString("locationtype");
                int mapid = rs.getInt("map");
                ret.savedLocations[SavedLocationType.valueOf(locationType).ordinal()] = mapid;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            ret.lastfametime = 0;
            ret.lastmonthfameids = new ArrayList<Integer>(31);
            while (rs.next()) {
                ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
            }
            rs.close();
            ps.close();
            ret.buddylist.loadFromDb(charid);
            ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
            ret.recalcLocalStats();
            ret.silentEnforceMaxHpMp();
        }
        if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        } else {
            ret.maplemount = new MapleMount(ret, 0, 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        }
        return ret;
    }

    private static class MapleBuffStatValueHolder {
        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {
        public int skillId;
        public long startTime,  length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void maxAllSkills() {
        if (getClient().getChannelServer().getSkillMaxing() || getClient().getPlayer().gmLevel() > 0) {
            int[] skill = {8, 1000, 1001, 1002, 1003, 1004, 1005, 1000000, 1000001, 1000002, 1001003, 1001004, 1001005, 1100000, 1100001,
                1100002, 1100003, 1101004, 1101005, 1101006, 1101007, 1110000, 1110001, 1111002, 1111003, 1111004, 1111005, 1111006, 1111007,
                1111008, 1120003, 1120004, 1120005, 1121000, 1121001, 1121002, 1121006, 1121008, 1121010, 1121011, 1200000, 1200001, 1200002,
                1200003, 1201004, 1201005, 1201006, 1201007, 1210000, 1210001, 1211002, 1211003, 1211004, 1211005, 1211006, 1211007, 1211008,
                1211009, 1220005, 1220006, 1220010, 1221000, 1221001, 1221002, 1221003, 1221004, 1221007, 1221009, 1221011, 1221012, 1300000,
                1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007, 1310000, 1311001, 1311002, 1311003, 1311004, 1311005, 1311006,
                1311007, 1311008, 1320005, 1320006, 1320008, 1320009, 1321000, 1321001, 1321002, 1321003, 1321007, 1321010, 2000000, 2000001,
                2001002, 2001003, 2001004, 2001005, 2100000, 2101001, 2101002, 2101003, 2101004, 2101005, 2110000, 2110001, 2111002, 2111003,
                2111004, 2111005, 2111006, 2121000, 2121001, 2121002, 2121003, 2121004, 2121005, 2121006, 2121007, 2121008, 2200000, 2201001,
                2201002, 2201003, 2201004, 2201005, 2210000, 2210001, 2211002, 2211003, 2211004, 2211005, 2211006, 2221000, 2221001, 2221002,
                2221003, 2221004, 2221005, 2221006, 2221007, 2221008, 2300000, 2301001, 2301002, 2301003, 2301004, 2301005, 2310000, 2311001,
                2311002, 2311003, 2311004, 2311005, 2311006, 2321000, 2321001, 2321002, 2321003, 2321004, 2321005, 2321006, 2321007, 2321008,
                2321009, 3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005, 3110000,
                3110001, 3111002, 3111003, 3111004, 3111005, 3111006, 3120005, 3121000, 3121002, 3121003, 3121004, 3121006, 3121007, 3121008,
                3121009, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005, 3210000, 3210001, 3211002, 3211003, 3211004, 3211005, 3211006,
                3220004, 3221000, 3221001, 3221002, 3221003, 3221005, 3221006, 3221007, 3221008, 4000000, 4000001, 4001002, 4001003, 4001334,
                4001344, 4100000, 4100001, 4100002, 4101003, 4101004, 4101005, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006,
                4120002, 4120005, 4121000, 4121003, 4121004, 4121006, 4121007, 4121008, 4121009, 4200000, 4200001, 4201002, 4201003, 4201004,
                4201005, 4210000, 4211001, 4211002, 4211003, 4211004, 4211005, 4211006, 4220002, 4220005, 4221000, 4221001, 4221003, 4221004,
                4221006, 4221007, 4221008, 5000000, 5001001, 5001002, 5001003, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005,
                5101006, 5101007, 5110000, 5110001, 5111002, 5111004, 5111005, 5111006, 5121000, 5121001, 5121002, 5121003, 5121004, 5121005,
                5121007, 5121008, 5121009, 5121010, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5210000, 5211001, 5211002,
                5211004, 5211005, 5211006, 5220001, 5220002, 5220011, 5221000, 5221003, 5221004, 5221006, 5221007, 5221008, 5221009, 5221010};
            for (int a : skill)
                maxSkill(a);
            if (gmLevel() > 0) {
                int[] skillgm = {9001000, 9001001, 9001002, 9101000, 9101001, 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008};
                for (int a : skillgm)
                    maxSkill(a);
            }
        }
    }

    public void maxSkill(int skillid) {
        ISkill skill_ = SkillFactory.getSkill(skillid);
        int maxlevel = skill_.getMaxLevel();
        changeSkillLevel(skill_, maxlevel, maxlevel);
    }

    public void mobKilled(int id) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null))
                continue;
            if (q.mobKilled(id)) {
                client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null))
                    client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
            }
        }
    }

    public void modifyCSPoints(int type, int dx) {
        if (type == 1)
            this.paypalnx += dx;
        else if (type == 2)
            this.maplepoints += dx;
        else if (type == 4)
            this.cardnx += dx;
    }

    public void Mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public class PirateShip {
        private int maxHp,  hp;

        public PirateShip(MapleCharacter mc) {
            int totHp = (mc.getLevel() - 120) * 2000 + (mc.getSkillLevel(SkillFactory.getSkill(5221006)) * 4000);
            this.maxHp = totHp;
            this.hp = totHp;
        }

        public int getMaxHp() {
            return maxHp;
        }

        public int getHp() {
            return hp;
        }

        public boolean damageShip(int dmg) {
            hp -= dmg;
            return hp > 0;
        }
    }

    public void playerNPC(MapleCharacter v, int scriptId) {
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, v.getName());
                ps.setInt(2, v.getHair());
                ps.setInt(3, v.getFace());
                ps.setInt(4, v.getSkinColor().getId());
                ps.setInt(5, getPosition().x);
                ps.setInt(6, getPosition().y);
                ps.setInt(7, getMapId());
                ps.setInt(8, scriptId);
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, getPosition().x + 50);
                ps.setInt(11, getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                ps.close();
                MapleInventory equips = v.getInventory(MapleInventoryType.EQUIPPED);
                List<Pair<Byte, Integer>> equipped = new LinkedList<Pair<Byte, Integer>>();
                for (IItem equip : equips) {
                    int pos = equip.getPosition() * -1;
                    if (pos != 111 && pos != 11)
                        if (pos > 100)
                            equipped.add(new Pair<Byte, Integer>((byte) (pos - 100), equip.getItemId()));
                        else if (equips.getItem((byte) (pos + 100)) == null)
                            equipped.add(new Pair<Byte, Integer>((byte) pos, equip.getItemId()));
                }
                for (Pair<Byte, Integer> e : equipped) {
                    ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                    ps.setInt(1, npcId);
                    ps.setInt(2, e.getRight());
                    ps.setInt(3, e.getLeft());
                    ps.executeUpdate();
                }
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPCs pn = new PlayerNPCs(rs);
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    MapleMap m = channel.getMapFactory().getMap(getMapId());
                    m.broadcastMessage(MaplePacketCreator.SpawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
                equipped.clear();
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playerDead() {
        cancelAllBuffs();
        dispelDebuffs();
        if (getEventInstance() != null)
            getEventInstance().playerKilled(this);
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            possesed--;
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (getJob() != MapleJob.BEGINNER) {
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown())
                XPdummy /= 100;
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel()))
                if (getLuk() <= 100 && getLuk() > 8)
                    XPdummy *= (200 - getLuk()) / 2000;
                else if (getLuk() < 8)
                    XPdummy /= 10;
                else
                    XPdummy /= 20;
            if (getExp() > XPdummy)
                gainExp(-XPdummy, false, false);
            else
                gainExp(-getExp(), false, false);
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null)
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null)
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        getClient().getSession().write(MaplePacketCreator.enableActions());
    }

    private void prepareBeholderEffect() {
        if (beholderHealingSchedule != null)
            beholderHealingSchedule.cancel(false);
        if (beholderBuffSchedule != null)
            beholderBuffSchedule.cancel(false);
        ISkill bHealing = SkillFactory.getSkill(1320008);
        int bHealingLvl = getSkillLevel(bHealing);
        if (bHealingLvl > 0) {
            final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
            int healInterval = healEffect.getX() * 1000;
            beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    addHP(healEffect.getHp());
                    getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, 5), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(1321007, 2), false);
                }
            }, healInterval, healInterval);
        }
        ISkill bBuff = SkillFactory.getSkill(1320009);
        if (getSkillLevel(bBuff) > 0) {
            final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
            int buffInterval = buffEffect.getX() * 1000;
            beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    buffEffect.applyTo(MapleCharacter.this);
                    getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, (int) (Math.random() * 3) + 6), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2), false);
                }
            }, buffInterval, buffInterval);
        }
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null)
            dragonBloodSchedule.cancel(false);
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                addHP(-bloodEffect.getX());
                getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
                checkBerserk();
            }
        }, 4000, 4000);
    }

    private int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null)
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null)
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null)
            watk += watkbuff.intValue();
        if (job.isA(MapleJob.BOWMAN)) {
            ISkill expert = null;
            if (job.isA(MapleJob.MARKSMAN))
                expert = SkillFactory.getSkill(3220004);
            else if (job.isA(MapleJob.BOWMASTER))
                expert = SkillFactory.getSkill(3120005);
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0)
                    watk += expert.getEffect(boostLevel).getX();
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null)
            magic += matkbuff.intValue();
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null)
            speed += speedbuff.intValue();
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null)
            jump += jumpbuff.intValue();
        if (speed > 140)
            speed = 140;
        if (jump > 123)
            jump = 123;
        localmaxbasedamage = calculateMaxBaseDamage(watk);
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp)
            updatePartyMemberHP();
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers())
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null)
                        getClient().getSession().write(
                                MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        if (effect.isHide()) {
            this.hidden = true;
            getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
        } else if (effect.isDragonBlood())
            prepareDragonBlood(effect);
        else if (effect.isBerserk())
            checkBerserk();
        else if (effect.isBeholder())
            prepareBeholderEffect();
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups())
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values())
            if (mcvh.skillId != id)
                coolDowns.remove(mcvh.skillId);
    }

    public void removeBuffStat(MapleBuffStat effect) {
        effects.remove(effect);
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId))
            this.coolDowns.remove(skillId);
    }

    public void removeDisease(MapleDisease disease) {
        synchronized (diseases) {
            if (diseases.contains(disease))
                diseases.remove(disease);
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
        if (shift_left)
            if (slot > -1)
                for (int i = slot; i < 3; i++)
                    if (i != 2)
                        pets[i] = pets[i + 1];
                    else
                        pets[i] = null;
    }

    public void removeSlots(byte b) {
        for (Pair curPair : inventorySlots)
            if ((Byte) curPair.getLeft() == b) {
                inventorySlots.remove(curPair);
                return;
            }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void saveGuildStatus() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?");
            ps.setInt(1, this.guildid);
            ps.setInt(2, this.guildrank);
            ps.setInt(3, this.id);
            ps.execute();
            ps.close();
        } catch (Exception e) {
        }
    }

    public void saveLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = getMapId();
    }

    public void saveToDB(boolean update) {
        Connection con = DatabaseConnection.getConnection();
        try {
            for (Pair<Integer, String> com : commands) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO gmlog (cid, command) VALUES (?, ?)");
                ps.setInt(1, com.getLeft());
                ps.setString(2, com.getRight());
                ps.executeUpdate();
                ps.close();
            }
        } catch (Exception e) {
        }
        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            if (update)
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, reborns = ?, pvpkills = ?, pvpdeaths = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, married = ?, partnerid = ?, cantalk = ?, marriagequest = ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?  WHERE id = ?");
            else
                ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, reborns, pvpkills, pvpdeaths, mountlevel, mounttiredness, mountexp, married, partnerid, cantalk, marriagequest, equipslots, useslots, setupslots, etcslots, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,?,?,?)");
            if (gmLevel < 1 && level > 250)
                ps.setInt(1, 250);
            else
                ps.setInt(1, level);
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, Math.abs(exp.get()));
            ps.setInt(8, hp);
            ps.setInt(9, mp);
            ps.setInt(10, maxhp);
            ps.setInt(11, maxmp);
            ps.setInt(12, remainingSp);
            ps.setInt(13, remainingAp);
            ps.setInt(14, gmLevel);
            ps.setInt(15, skinColor.getId());
            ps.setInt(16, gender);
            ps.setInt(17, job.getId());
            ps.setInt(18, hair);
            ps.setInt(19, face);
            if (map == null)
                ps.setInt(20, 0);
			else if ((map.getId() > 910000020 && map.getId() <= 910000021) || isPvPMap()) {
                ps.setInt(20, 910000000);
            } else if (map.getForcedReturnId() != 999999999)
                ps.setInt(20, map.getForcedReturnId());
            else
                ps.setInt(20, map.getId());
            ps.setInt(21, meso.get());
            ps.setInt(22, hpApUsed);
            ps.setInt(23, mpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001)
                ps.setInt(24, 0);
            else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null)
                    ps.setInt(24, closest.getId());
                else
                    ps.setInt(24, 0);
            }
            if (party != null)
                ps.setInt(25, party.getId());
            else
                ps.setInt(25, -1);
            ps.setInt(26, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
            ps.setInt(29, reborns);
			ps.setInt(30, pvpkills);
            ps.setInt(31, pvpdeaths);
            if (maplemount != null) {
                ps.setInt(32, maplemount.getLevel());
                ps.setInt(33, maplemount.getExp());
                ps.setInt(34, maplemount.getTiredness());
            } else {
                ps.setInt(32, 1);
                ps.setInt(33, 0);
                ps.setInt(34, 0);
            }
            ps.setInt(35, married);
            ps.setInt(36, partnerid);
            ps.setInt(37, canTalk);
            ps.setInt(38, marriageQuestLevel);
            ps.setInt(39, getSlots((byte) 1));
            ps.setInt(40, getSlots((byte) 2));
            ps.setInt(41, getSlots((byte) 3));
            ps.setInt(42, getSlots((byte) 4));
            if (update)
                ps.setInt(43, id);
            else {
                ps.setInt(43, accountid);
                ps.setString(44, name);
                ps.setInt(45, world);
            }
            int updateRows = ps.executeUpdate();
            if (!update) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next())
                    this.id = rs.getInt(1);
                else
                    throw new RuntimeException("Inserting char failed.");
            } else if (updateRows < 1)
                throw new RuntimeException("Character not in database (" + id + ")");
            ps.close();
            for (int i = 0; i < 3; i++)
                if (pets[i] != null)
                    pets[i].saveToDb();
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                ps.setInt(2, keybinding.getKey().intValue());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.executeUpdate();
            }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
            for (int i = 0; i < 5; i++) {
                SkillMacro macro = skillMacros[i];
                if (macro != null) {
                    ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, getId());
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.executeUpdate();
                    ps.close();
                }
            }
            ps = con.prepareStatement("DELETE FROM inventoryitems WHERE characterid = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, petid) VALUES (?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (MapleInventory iv : inventory) {
                ps.setInt(3, iv.getType().getType());
                for (IItem item : iv.list()) {
                    ps.setInt(1, id);
                    ps.setInt(2, item.getItemId());
                    ps.setInt(4, item.getPosition());
                    ps.setInt(5, item.getQuantity());
                    ps.setString(6, item.getOwner());
                    ps.setInt(7, item.getPetId());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    int itemid;
                    if (rs.next())
                        itemid = rs.getInt(1);
                    else
                        throw new RuntimeException("Inserting char failed.");
                    if (iv.getType().equals(MapleInventoryType.EQUIP) || iv.getType().equals(MapleInventoryType.EQUIPPED)) {
                        pse.setInt(1, itemid);
                        IEquip equip = (IEquip) item;
                        pse.setInt(2, equip.getUpgradeSlots());
                        pse.setInt(3, equip.getLevel());
                        pse.setInt(4, equip.getStr());
                        pse.setInt(5, equip.getDex());
                        pse.setInt(6, equip.getInt());
                        pse.setInt(7, equip.getLuk());
                        pse.setInt(8, equip.getHp());
                        pse.setInt(9, equip.getMp());
                        pse.setInt(10, equip.getWatk());
                        pse.setInt(11, equip.getMatk());
                        pse.setInt(12, equip.getWdef());
                        pse.setInt(13, equip.getMdef());
                        pse.setInt(14, equip.getAcc());
                        pse.setInt(15, equip.getAvoid());
                        pse.setInt(16, equip.getHands());
                        pse.setInt(17, equip.getSpeed());
                        pse.setInt(18, equip.getJump());
                        pse.setInt(19, equip.getRingId());
                        pse.setInt(20, equip.getLocked());
                        pse.executeUpdate();
                    }
                }
            }
            ps.close();
            pse.close();
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.executeUpdate();
            }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values())
                if (savedLocations[savedLocationType.ordinal()] != -1) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations[savedLocationType.ordinal()]);
                    ps.executeUpdate();
                }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 0)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies())
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.executeUpdate();
                }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, id);
            for (MapleQuestStatus q : quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus().getId());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                ps.setInt(5, q.getForfeited());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                for (int mob : q.getMobKills().keySet()) {
                    pse.setInt(1, rs.getInt(1));
                    pse.setInt(2, mob);
                    pse.setInt(3, q.getMobKills(mob));
                    pse.executeUpdate();
                }
                rs.close();
            }
            ps.close();
            pse.close();
            ps = con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ? WHERE id = ?");
            ps.setInt(1, paypalnx);
            ps.setInt(2, maplepoints);
            ps.setInt(3, cardnx);
            ps.setInt(4, client.getAccID());
            ps.executeUpdate();
            ps.close();
            if (storage != null)
                storage.saveToDB();
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (Exception e1) {
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
            }
        }
    }

    public void sendKeymap() {
        getClient().getSession().write(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++)
            if (skillMacros[i] != null)
                macros = true;
        if (macros)
            getClient().getSession().write(MaplePacketCreator.getMacros(skillMacros));
    }

    public void sendNote(String to, String msg) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setString(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    private void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null)
            return;
        mbsvh.value = value;
    }

    public void setCanTalk(int yes) {
        this.canTalk = yes;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGMLevel(int level) {
        this.gmLevel = level;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0)
            if (mgc == null)
                mgc = new MapleGuildCharacter(this);
            else
                mgc.setGuildId(guildid);
        else
            mgc = null;
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null)
            mgc.setGuildRank(_rank);
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = " + getId());
            ps.setInt(1, set ? 1 : 0);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            return;
        }
        hasMerchant = set;
    }

    public void setHide(boolean b) {
        this.inHide = b;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0)
            thp = 0;
        if (thp > localmaxhp)
            thp = localmaxhp;
        this.hp = thp;
        if (!silent)
            updatePartyMemberHP();
        if (oldHp > hp && !isAlive())
            playerDead();
    }

    public void setHpApUsed(int hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, x);
        updateSingleStat(MapleStat.MP, x);
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setInCS(boolean b) {
        this.incs = b;
    }

    public void setInMTS(boolean b) {
        this.inmts = b;
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMarriageQuestLevel(int nf) {
        marriageQuestLevel = nf;
    }

    public void setMarried(int m) {
        this.married = m;
    }

    public void setMatchCardPoints(MapleCharacter visitor, int winnerslot) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        String n = this.getName();
        String name2 = visitor.getName();
        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET matchcardwins = matchcardwins + 1 WHERE name = ?");
                if (winnerslot == 1)
                    ps.setString(1, n);
                else
                    ps.setString(1, name2);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE characters SET matchcardlosses = matchcardlosses + 1 WHERE name = ?");
                if (winnerslot == 1)
                    ps.setString(1, name2);
                else
                    ps.setString(1, n);
                ps.executeUpdate();
                ps.close();
            } else {
                ps = con.prepareStatement("UPDATE characters SET matchcardties = matchcardties + 1 WHERE name = ? OR name = ?");
                ps.setString(1, n);
                ps.setString(2, name2);
                ps.executeUpdate();
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0)
            tmp = 0;
        if (tmp > localmaxmp)
            tmp = localmaxmp;
        this.mp = tmp;
    }

    public void setMpApUsed(int mpApUsed) {
        this.mpApUsed = mpApUsed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNXCodeUsed(String code) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = " + code);
        ps.executeUpdate();
        ps = con.prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
        ps.setString(1, this.getName());
        ps.setString(2, code);
        ps.executeUpdate();
        ps.close();
    }

    public void setOmokPoints(MapleCharacter visitor, int winnerslot) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        String n = this.getName();
        String name2 = visitor.getName();
        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET omokwins = omokwins + 1 WHERE name = ?");
                if (winnerslot == 1)
                    ps.setString(1, n);
                else
                    ps.setString(1, name2);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE characters SET omoklosses = omoklosses + 1 WHERE name = ?");
                if (winnerslot == 1)
                    ps.setString(1, name2);
                else
                    ps.setString(1, n);
                ps.executeUpdate();
                ps.close();
            }
            if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET omokties = omokties + 1 WHERE name = ? AND name = ?");
                ps.setString(1, n);
                ps.setString(2, name2);
                ps.executeUpdate();
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    public void setPartnerId(int pem) {
        this.partnerid = pem;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public void setPirateShip(PirateShip set) {
        this.pirateShip = set;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setSlots(byte b, int slots) {
        removeSlots(b);
        inventorySlots.add(new Pair<Byte, Integer>(b, slots));
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showNote() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            rs.first();
            client.getSession().write(MaplePacketCreator.showNotes(rs, rs.getRow()));
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs)
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
    }

    public void silentPartyUpdate() {
        if (party != null)
            try {
                getClient().getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(MapleCharacter.this));
            } catch (Exception e) {
                getClient().getChannelServer().reconnectWorld();
            }
    }

    public static class SkillEntry {
        public int skillevel,  masterlevel;

        public SkillEntry(int skillevel, int masterlevel) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public boolean skillisCooling(int skillId) {
        return this.coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        ScheduledFuture<?> schedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                int newFullness = pet.getFullness() - decrease;
                if (newFullness <= 5) {
                    pet.setFullness(15);
                    unequipPet(pet, true, true);
                } else {
                    pet.setFullness(newFullness);
                    getClient().getSession().write(MaplePacketCreator.updatePet(pet));
                }
            }
        }, 60000, 60000);
        switch (petSlot) {
            case 0:
                fullnessSchedule = schedule;
            case 1:
                fullnessSchedule_1 = schedule;
            case 2:
                fullnessSchedule_2 = schedule;
        }
    }

    public void startMapTimeLimitTask(final MapleMap from, final MapleMap to) {
        if (to.getTimeLimit() > 0 && from != null) {
            final MapleCharacter chr = this;
            mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    MaplePortal pfrom = null;
                    if (MapleItemInformationProvider.getInstance().isMiniDungeonMap(from.getId()))
                        pfrom = from.getPortal("MD00");
                    else
                        pfrom = from.getPortal(0);
                    if (pfrom != null)
                        chr.changeMap(from, pfrom);
                }
            }, from.getTimeLimit() * 1000, from.getTimeLimit() * 1000);
        }
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void toggleGMChat() {
        gmchattype = !gmchattype;
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                unequipPet(pets[i], true);
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        cancelFullnessSchedule(getPetIndex(pet));
        pet.saveToDb();
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        getClient().getSession().write(MaplePacketCreator.petStatUpdate(this));
        getClient().getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers())
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null)
                        other.getClient().getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
                }
        }
    }

    public void updateQuest(MapleQuestStatus quest) {
        //        this.updateQuest(quest);
        quests.put(quest.getQuest(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId()));
            client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 8));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED))
            client.getSession().write(MaplePacketCreator.completeQuest(this, (short) quest.getQuest().getId()));
        else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED))
            client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        client.getSession().write(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval))), itemReaction));
    }

    @Override
    public Collection<MapleInventory> allInventories() {
        return Arrays.asList(inventory);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if ((this.isHidden() && client.getPlayer().gmLevel() > 0) || !this.isHidden()) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            for (int i = 0; i < 3; i++)
                if (pets[i] != null)
                    client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
        }
    }

    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.name;
    }
	
	public boolean inBlockedMap() {
        return isPvPMap() || inJail();
    }
}