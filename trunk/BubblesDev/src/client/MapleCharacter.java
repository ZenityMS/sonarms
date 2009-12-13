/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package client;

import client.anticheat.CheatTracker;
import constants.ExpTable;
import constants.SkillConstants;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import net.MaplePacket;
import net.PacketProcessor;
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
import server.maps.MapleMapEffect;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocation;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer {
    private static Logger log = LoggerFactory.getLogger(PacketProcessor.class);
    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int id;
    private int level;
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
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage;
    private int currentType = 0;
    private int currentTab = 1;
    private int chair;
    private int itemEffect;
    private int paypalnx;
    private int maplepoints;
    private int cardnx;
    private int guildid;
    private int guildrank;
    private int allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private int familyId;
    private int bookCover;
    private int cygnusLinkId = 0;
    private int linkedLevel = 0;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int dropMod = 1;
    private int possibleReports = 10;
    private int dojoPoints;
    private int vanquisherStage;
    private int lastDojoStage;
    private int dojoEnergy;
    private int vanquisherKills;
    private int warpToId;
    private long dojoFinish;
    private long lastfametime;
    private double expMod = 1;
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
    private boolean whitechat = true;
    private boolean Berserk;
    private boolean hasMerchant;
    private boolean watchedCygnusIntro;
    private boolean finishedDojoTutorial;
    private String name;
    private String chalktext;
    private String search = null;
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private BuddyList buddylist;
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
    private SavedLocation savedLocations[];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Map<Integer, String> entered = new LinkedHashMap<Integer, String>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = Collections.synchronizedMap(new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>());
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    private Map<Integer, Boolean> isLinkedCache = new LinkedHashMap<Integer, Boolean>();
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    // anticheat related information
    private CheatTracker anticheat;
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?> fullnessSchedule;
    private ScheduledFuture<?> fullnessSchedule_1;
    private ScheduledFuture<?> fullnessSchedule_2;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private static List<Pair<Byte, Integer>> inventorySlots = new ArrayList<Pair<Byte, Integer>>();
    private LinkedList<String> commands = new LinkedList<String>();
    private ArrayList<Integer> excluded = new ArrayList<Integer>();
    private MonsterBook monsterbook;

    public MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values())
            inventory[type.ordinal()] = new MapleInventory(type, (byte) getSlots(type));
        savedLocations = new SavedLocation[SavedLocationType.values().length];
        for (int i = 0; i < SavedLocationType.values().length; i++)
            savedLocations[i] = null;
        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
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
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
        ret.maplemount = null;
        int[] key = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41};
        int[] type = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4};
        int[] action = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23};
        for (int i = 0; i < key.length; i++)
            ret.keymap.put(key[i], new MapleKeyBinding(type[i], action[i]));
        return ret;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId)))
            this.coolDowns.remove(skillId);
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCommandToList(String command) {
        commands.add(command);
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (party != null) {
                int highest = level, lowest = level;
                for (MaplePartyCharacter mpc : party.getMembers())
                    if (mpc.getLevel() > highest)
                        highest = mpc.getLevel();
                    else if (mpc.getLevel() < lowest)
                        lowest = mpc.getLevel();
                pts += (highest - lowest < 30) ? 0 : -pts;
            } else
                pts++;
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
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
            this.str += up;
            updateSingleStat(MapleStat.STR, getStr());
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, getDex());
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, getInt());
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, getLuk());
        }
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason) {
		if (lastmonthfameids == null) {
			throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
		}
		try {
			getClient().banMacs();
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
			ps.setInt(1, 1);
			ps.setString(2, reason);
			ps.setInt(3, accountid);
			ps.executeUpdate();
			ps.close();
			ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
			String[] ipSplit = client.getSession().getRemoteAddress().toString().split(":");
			ps.setString(1, ipSplit[0]);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
		}
		client.getSession().close();
	}

    public void ban(String reason, boolean dc) {
        try {
            client.banMacs();
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
        client.disconnect();
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
                PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
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
                } else if ((getJob().isA(MapleJob.THIEF) || getJob().isA(MapleJob.NIGHTWALKER1)) && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
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
                realTarget.client.getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
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
                    door.sendDestroyData(chr.client);
                for (MapleCharacter chr : door.getTown().getCharacters())
                    door.sendDestroyData(chr.client);
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        if (effect.isMonsterRiding())
            if (effect.getSourceId() != SkillConstants.Corsair.Battleship) {
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

    public void cancelFullnessSchedule(int petSlot) {//with fall through, no NPE
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
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.getSession().write(MaplePacketCreator.cancelBuff(buffstats));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public static boolean canCreateChar(String name) {
        if (name.length() < 4 || name.length() > 12)
            return false;
        return getIdByName(name) < 0 && !name.toLowerCase().contains("gm") && Pattern.compile("[a-zA-Z0-9_-]{3,12}").matcher(name).matches();
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0)
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
        updateSingleStat(MapleStat.AVAILABLESP, remainingSp);
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
        //handle KoC here
        else if (job_ > 0 && job_ != 1000) {
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
        client.getSession().write(MaplePacketCreator.updatePlayerStats(statup));
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

    public CheatTracker getCheatTracker() {
		return anticheat;
	}

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
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

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map_ = ChannelServer.getInstance(client.getChannel()).getMapFactory().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
        warpPacket.setOnSend(new Runnable() {
            @Override
            public void run() {
                map.removePlayer(MapleCharacter.this);
                if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    map = to;
                    setPosition(pos);
                    map.addPlayer(MapleCharacter.this);
                    if (party != null) {
                        silentPartyUpdate();
                        client.getSession().write(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
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
        client.getSession().write(warpPacket);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
        skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
        this.client.getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
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
        ISkill BerserkX = SkillFactory.getSkill(SkillConstants.DarkKnight.Berserk);
        final int skilllevel = getSkillLevel(BerserkX);
        if (chr.getJob().equals(MapleJob.DARKKNIGHT) && skilllevel > 0) {
            Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
            BerserkSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    client.getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                }
            }, 5000, 3000);
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1)
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                wci.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
                wci.updateMessenger(getMessenger().getId(), name, client.getChannel());
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
        savedLocations[type.ordinal()] = null;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            ISkill battleship = SkillFactory.getSkill(SkillConstants.Corsair.Battleship);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            getClient().getSession().write(MaplePacketCreator.skillCooldown(SkillConstants.Corsair.Battleship, cooldown));
            addCooldown(SkillConstants.Corsair.Battleship, System.currentTimeMillis(), cooldown * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(this, SkillConstants.Corsair.Battleship), cooldown * 1000));
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            resetBattleshipHp();
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
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
                            getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == SkillConstants.DarkKnight.Beholder) {
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
            return;
        try {
            client.getChannelServer().getWorldInterface().disbandGuild(guildid);
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
                client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
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
                client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        this.diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs)
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId())))
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid)
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case SkillConstants.DarkKnight.Beholder:
            case SkillConstants.FPArchMage.Elquines:
            case SkillConstants.ILArchMage.Ifrit:
            case SkillConstants.Priest.SummonDragon:
            case SkillConstants.Bishop.Bahamut:
            case SkillConstants.Ranger.Puppet:
            case SkillConstants.Ranger.SilverHawk:
            case SkillConstants.Sniper.Puppet:
            case SkillConstants.Sniper.GoldenEagle:
            case SkillConstants.Hermit.ShadowPartner:
                return true;
            default:
                return false;
        }
    }

    public void dispelSeal() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases)
            if (disease == MapleDisease.SEAL) {
                disease_.add(disease);
                client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        this.diseases.clear();
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

    public void dropMessage(String message) {
        dropMessage(0, message);
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
            client.getSession().write(MaplePacketCreator.updatePlayerStats(stats));
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid))
            entered.put(mapid, script);
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getMessenger() != null) {
            WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
            try {
                wci.updateMessenger(getMessenger().getId(), getName(), client.getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public void expirationTask() {
        long expiration, currenttime = System.currentTimeMillis();
        List<IItem> toberemove = new ArrayList<IItem>(); // This is here to prevent deadlock.
        for (MapleInventory inv : inventory) {
            for (IItem item : inv.list()) {
                expiration = item.getExpiration();
                if (expiration != -1)
                    if (currenttime < expiration) {
                        client.getSession().write(MaplePacketCreator.itemExpired(item.getItemId()));
                        toberemove.add(item);
                    }
            }
            for (IItem item : toberemove)
                MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
            toberemove.clear();
        }
    }

    public enum FameStatus {
        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void forceUpdateItem(MapleInventoryType type, IItem item) {
        client.getSession().write(MaplePacketCreator.clearInventoryItem(type, item.getPosition(), false));
        client.getSession().write(MaplePacketCreator.addInventorySlot(type, item, false));
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        if (level < getMaxLevel()) {
            if ((long) this.exp.get() + (long) gain > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                gain -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(gain));
            if (show && gain != 0)
                client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white));
            if (gmLevel > 0)
                while (exp.get() >= ExpTable.getExpNeededForLevel(level))
                    levelUp();
            else if (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp();
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
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

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public int getBattleshipHp() {
        return battleshipHp;
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

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
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
                return paypalnx;
            case 2:
                return maplepoints;
            default:
                return cardnx;
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

    public int getCygnusLinkId() {
        return cygnusLinkId;
    }

    public int getDex() {
        return dex;
    }

    public List<MapleDisease> getDiseases() {
        synchronized (diseases) {
            return Collections.unmodifiableList(diseases);
        }
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return lastDojoStage;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public int getDropMod() {
        return dropMod;
    }

    public int getEnergyBar() {
        return this.energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public ArrayList<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public double getExpMod() {
        return expMod;
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamilyEntry getFamily() {
        return MapleFamily.getMapleFamily(this);
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public int getGender() {
        return gender;
    }

    public boolean getGMChat() {
        return whitechat;
    }

    public MapleGuild getGuild() {
        try {
            return client.getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
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
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT id FROM characters WHERE name = ? AND world = ?");
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
		} catch (SQLException e) {
			log.error("ERROR", e);
		}
		return -1;
	}

    public static int getIdByName(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
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

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public int getLevel() {
        return level;
    }

    public int getLinkedLevel() {
        return linkedLevel;
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

    public int getMarkedMonster() {
        return markedMonster;
    }

    public int getMasterLevel(ISkill skill) {
        if (skills.get(skill) == null)
            return 0;
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMesosTraded() {
        return mesosTraded;
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

    public int getMiniGamePoints(String type, boolean omok) {
        int points = 0;
        String game = omok ? "omok" : "matchcard";
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT " + game + type + " FROM characters WHERE name = '" + name + "'");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return 0;
            } else
                points = rs.getInt(0);
            rs.close();
            ps.close();
            return points;
        } catch (Exception e) {
        }
        return points;
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
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

    public int getPossibleReports() {
        return possibleReports;
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

    public int getSavedLocation(String type) {
        int m = savedLocations[SavedLocationType.fromString(type).ordinal()].getMapId();
        clearSavedLocation(SavedLocationType.fromString(type));
        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null)
            return 0;
        return ret.skillevel;
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
        for (Pair<Byte, Integer> curPair : inventorySlots)
            if (curPair.getLeft() == b)
                return curPair.getRight();
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

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        int time = (int) ((length + starttime) - System.currentTimeMillis());
        addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time));
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        giveDebuff(disease, skill, false);
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill, boolean cpq) {
        if (this.isAlive() && diseases.size() < 2 || cpq) {
            List<Pair<MapleDisease, Integer>> disease_ = new ArrayList<Pair<MapleDisease, Integer>>();
            disease_.add(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
            this.diseases.add(disease);
            client.getSession().write(MaplePacketCreator.giveDebuff(disease_, skill));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, disease_, skill), false);
        }
    }

    public int gmLevel() {
        return gmLevel;
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    public void guildUpdate() {
        if (this.guildid < 1)
            return;
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0)
                client.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() {
        ISkill energycharge = isCygnus() ? SkillFactory.getSkill(SkillConstants.ThunderBreaker.EnergyCharge) : SkillFactory.getSkill(SkillConstants.Marauder.EnergyCharge);
        int energyChargeSkillLevel = getSkillLevel(energycharge);
        MapleStatEffect ceffect = null;
        ceffect = energycharge.getEffect(energyChargeSkillLevel);
        TimerManager tMan = TimerManager.getInstance();
        if (energyChargeSkillLevel > 0) {
            if (energybar < 10000) {
                energybar += 102;
                if (energybar > 10000)
                    energybar = 10000;
                client.getSession().write(MaplePacketCreator.giveEnergyCharge(energybar));
                client.getSession().write(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
                getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
                if (energybar == 10000)
                    getMap().broadcastMessage(this, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
            }
            if (energybar >= 10000 && energybar < 11000) {
                energybar = 15000;
                final MapleCharacter chr = this;
                tMan.schedule(new Runnable() {
                    @Override
                    public void run() {
                        client.getSession().write(MaplePacketCreator.giveEnergyCharge(0));
                        getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
                        energybar = 0;
                    }
                }, ceffect.getDuration());
            }
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? SkillConstants.DawnWarrior.ComboAttack : SkillConstants.Crusader.ComboAttack;
        ISkill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.getSession().write(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat, false, false, getMount()));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
    }

    public void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO);
        int oid = isCygnus() ? SkillConstants.DawnWarrior.ComboAttack : SkillConstants.Crusader.ComboAttack;
        int advcomboid = isCygnus() ? SkillConstants.DawnWarrior.AdvancedCombo : SkillConstants.Hero.AdvancedComboAttack;
        ISkill combo = SkillFactory.getSkill(oid);
        ISkill advcombo = SkillFactory.getSkill(advcomboid);
        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getSkillLevel(advcombo);
        if (advComboSkillLevel > 0)
            ceffect = advcombo.getEffect(advComboSkillLevel);
        else
            ceffect = combo.getEffect(getSkillLevel(combo));
        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult())
                if (neworbcount <= ceffect.getX())
                    neworbcount++;
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));
            client.getSession().write(MaplePacketCreator.giveBuff(oid, duration, stat, false, false, getMount()));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
        }
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet())
            if (entered.get(mapId).equals(script))
                return true;
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId))
            if (entered.get(mapId).equals(script))
                return true;
        return false;
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

    public boolean hasWatchedCygnusIntro() {
        return watchedCygnusIntro;
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
        if (getMeso() < 1500000) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(guildid);
        } catch (Exception e) {
            client.getChannelServer().reconnectWorld();
            return;
        }
        gainMeso(-1500000, true, false, true);
    }

    public boolean inCS() {
        return incs;
    }

    public boolean inMTS() {
        return inmts;
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs)
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid)
                return true;
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null)
            return false;
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return job.getId() / 1000 == 1;
    }

    public boolean isGM() {
        return gmLevel > 0;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLinked() {
        if (isLinkedCache.get(id) != null)
            return isLinkedCache.get(id);
        else
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT knightId from koccharacters WHERE linkedId = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                isLinkedCache.put(id, rs.last());
                ps.close();
                rs.close();
            } catch (Exception e) {
            }
        return isLinkedCache.get(id);
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
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
        ISkill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;
        if (isCygnus() && level < 70)
            remainingAp++;
        remainingAp += 5;
        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE) {
            maxhp += rand(12, 16);
            maxmp += rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(SkillConstants.DawnWarrior.MaxHpEnhancement) : SkillFactory.getSkill(SkillConstants.Swordman.ImprovedMaxHpIncrease);
            if (job.isA(MapleJob.CRUSADER))
                improvingMaxMP = SkillFactory.getSkill(1210000);
            else if (job.isA(MapleJob.DAWNWARRIOR2))
                improvingMaxMP = SkillFactory.getSkill(11110000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(24, 28);
            maxmp += rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(SkillConstants.BlazeWizard.IncreasingMaxMp) : SkillFactory.getSkill(SkillConstants.Magician.ImprovedMaxMpIncrease);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += rand(10, 14);
            maxmp += rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += rand(20, 24);
            maxmp += rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(SkillConstants.ThunderBreaker.ImproveMaxHp) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(22, 28);
            maxmp += rand(18, 23);
        }
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1)))
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1)))
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        maxmp += getTotalInt() / 10;
        exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
        if (exp.get() < 0)
            exp.set(0);
        level++;
        if (level >= getMaxLevel())
            exp.set(0);
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, maxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, maxmp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, level));
        if (job.getId() % 1000 > 0) {
            remainingSp += 3;
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, remainingSp));
        }
        setHp(maxhp);
        setMp(maxmp);
        client.getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        silentPartyUpdate();
        guildUpdate();
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        try {
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
            ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            ret.vanquisherKills = rs.getInt("vanquisherKills");
            if (rs.getInt("job") / 1000 == 1 && ret.gmLevel < 1) {
                PreparedStatement pss = con.prepareStatement("SELECT linkedId FROM koccharacters WHERE knightId = ?");
                pss.setInt(1, charid);
                ResultSet rss = pss.executeQuery();
                int linkedChar = -1;
                if (rss.next())
                    linkedChar = rss.getInt("linkedId");
                rss.close();
                pss.close();
                pss = con.prepareStatement("SELECT level FROM characters WHERE id = ?");
                pss.setInt(1, linkedChar);
                rss = pss.executeQuery();
                if (rss.next())
                    ret.linkedLevel = rss.getInt("level");
                else
                    ret.linkedLevel = 0;
                rss.close();
                pss.close();
            } else
                ret.linkedLevel = 200;
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
            ret.allianceRank = rs.getInt("allianceRank");
            ret.familyId = rs.getInt("familyId");
            ret.bookCover = rs.getInt("monsterbookcover");
            ret.monsterbook = new MonsterBook();
            ret.monsterbook.loadCards(charid);
            ret.cygnusLinkId = rs.getInt("cygnuslink");
            ret.watchedCygnusIntro = rs.getInt("watchedcygnusintro") == 1;
            ret.vanquisherStage = rs.getInt("vanquisherStage");
            ret.dojoPoints = rs.getInt("dojoPoints");
            ret.lastDojoStage = rs.getInt("lastDojoStage");
            ret.whitechat = rs.getInt("gm") > 0;
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
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
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
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null)
                        ret.party = party;
                } catch (Exception ex) {
                    client.getChannelServer().reconnectWorld();
                }
                int messengerid = rs.getInt("messengerid");
                int position = rs.getInt("messengerposition");
                if (messengerid > 0 && position < 4 && position > -1)
                    try {
                        WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
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
                ret.client.setAccountName(rs.getString("name"));
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
                long currenttime = System.currentTimeMillis();
                long expiration = rs.getLong("expiredate");
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
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setFlag((byte) rs.getInt("flag"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    if (expiration != -1)
                        if (currenttime < expiration) {
                            equip.setExpiration(currenttime);
                            ret.getInventory(type).addFromDB(equip);
                        } else
                            client.getSession().write(MaplePacketCreator.serverNotice(5, MapleItemInformationProvider.getInstance().getName(equip.getItemId()) + " has expired from your inventory."));
                    else
                        ret.getInventory(type).addFromDB(equip);
                } else {
                    Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                    item.setOwner(rs.getString("owner"));
                    if (expiration != -1)
                        if (currenttime < expiration) {
                            item.setExpiration(currenttime);
                            ret.getInventory(type).addFromDB(item);
                        } else
                            client.getSession().write(MaplePacketCreator.serverNotice(5, MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " has expired from your inventory."));
                    else
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
                ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next())
                    ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
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
                ret.resetBattleshipHp();
                ret.silentEnforceMaxHpMp();
            }
            int mountid = ret.isCygnus() ? 10001004 : 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), mountid);
                ret.maplemount.setExp(mountexp);
                ret.maplemount.setLevel(mountlevel);
                ret.maplemount.setTiredness(mounttiredness);
                ret.maplemount.setActive(false);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
                ret.maplemount.setExp(mountexp);
                ret.maplemount.setLevel(mountlevel);
                ret.maplemount.setTiredness(mounttiredness);
                ret.maplemount.setActive(false);
            }
            return ret;
        } catch (Exception e) {
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
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
        public long startTime, length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void message(String m) {
        dropMessage(5, m);
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

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
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
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (IItem equip : getInventory(MapleInventoryType.EQUIPPED)) {
                    ps.setInt(2, equip.getItemId());
                    ps.setInt(3, equip.getPosition());
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
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
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
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
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
        client.getSession().write(MaplePacketCreator.enableActions());
    }

    private void prepareBeholderEffect() {
        final int beholder = SkillConstants.DarkKnight.Beholder;
        if (beholderHealingSchedule != null)
            beholderHealingSchedule.cancel(false);
        if (beholderBuffSchedule != null)
            beholderBuffSchedule.cancel(false);
        ISkill bHealing = SkillFactory.getSkill(SkillConstants.DarkKnight.AuraOfBeholder);
        int bHealingLvl = getSkillLevel(bHealing);
        if (bHealingLvl > 0) {
            final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
            int healInterval = healEffect.getX() * 1000;
            beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    addHP(healEffect.getHp());
                    client.getSession().write(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                }
            }, healInterval, healInterval);
        }
        ISkill bBuff = SkillFactory.getSkill(SkillConstants.DarkKnight.HexOfBeholder);
        if (getSkillLevel(bBuff) > 0) {
            final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
            int buffInterval = buffEffect.getX() * 1000;
            beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    buffEffect.applyTo(MapleCharacter.this);
                    client.getSession().write(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
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
                client.getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
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
                        client.getSession().write(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
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

    public void removeDiseases() {
        diseases.clear();
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
        for (Pair<Byte, Integer> curPair : inventorySlots)
            if ((Byte) curPair.getLeft() == b) {
                inventorySlots.remove(curPair);
                return;
            }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(SkillConstants.Corsair.Battleship)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId()))
            entered.remove(map.getId());
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId))
            entered.remove(mapId);
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet())
            if (entered.get(mapId).equals(script))
                entered.remove(mapId);
    }

    public void saveCooldowns() {
        if (getAllCooldowns().size() > 0)
            for (PlayerCoolDownValueHolder cooling : getAllCooldowns())
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, getId());
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException se) {
                }
    }

    public void saveGuildStatus() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, allianceRank);
            ps.setInt(4, id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public void saveToDB(boolean update) {
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            if (update)
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, cygnuslink = ?, watchedcygnusintro = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ? WHERE id = ?");
            else
                ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, mountlevel, mounttiredness, mountexp, equipslots, useslots, setupslots, etcslots, monsterbookcover, cygnuslink, watchedcygnusintro, vanquisherStage, dojopoints, lastDojoStage, finishedDojoTutorial, vanquisherKills, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,?,?,?,?,?)");
            if (gmLevel < 1 && level > (isCygnus() ? 120 : 200))
                ps.setInt(1, isCygnus() ? 120 : 200);
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
            else if (map.getForcedReturnId() != 999999999)
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
            if (maplemount != null) {
                ps.setInt(29, maplemount.getLevel());
                ps.setInt(30, maplemount.getExp());
                ps.setInt(31, maplemount.getTiredness());
            } else {
                ps.setInt(29, 1);
                ps.setInt(30, 0);
                ps.setInt(31, 0);
            }
            for (int i = 32; i < 36; i++)
                ps.setInt(i, getSlots((byte) (i - 32)));
            if (update)
                monsterbook.saveCards(getId());
            ps.setInt(36, bookCover);
            ps.setInt(37, cygnusLinkId);
            ps.setInt(38, watchedCygnusIntro ? 1 : 0);
            ps.setInt(39, vanquisherStage);
            ps.setInt(40, dojoPoints);
            ps.setInt(41, lastDojoStage);
            ps.setInt(42, finishedDojoTutorial ? 1 : 0);
            ps.setInt(43, vanquisherKills);
            if (update)
                ps.setInt(44, id);
            else {
                ps.setInt(44, accountid);
                ps.setString(45, name);
                ps.setInt(46, world);
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
                ps.addBatch();
            }
            ps.executeBatch();
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
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, petid, expiredate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (MapleInventory iv : inventory) {
                ps.setInt(3, iv.getType().getType());
                for (IItem item : iv.list()) {
                    ps.setInt(1, id);
                    ps.setInt(2, item.getItemId());
                    ps.setInt(4, item.getPosition());
                    ps.setInt(5, item.getQuantity());
                    ps.setString(6, item.getOwner());
                    ps.setInt(7, item.getPetId());
                    ps.setLong(8, item.getExpiration());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    int itemid;
                    if (rs.next())
                        itemid = rs.getInt(1);
                    else {
                        rs.close();
                        throw new RuntimeException("Inserting char failed.");
                    }
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
                        pse.setInt(20, 0);
                        pse.setInt(21, equip.getVicious());
                        pse.setInt(22, item.getFlag());
                        pse.executeUpdate();
                    }
                    rs.close();
                }
            }
            pse.close();
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values())
                if (savedLocations[savedLocationType.ordinal()] != null) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                    ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                    ps.addBatch();
                }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies())
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setInt(3, entry.getGroup().startsWith("Default") ? 0 : 1);
                    ps.addBatch();
                }
            ps.executeBatch();
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
            pse.close();
            ps = con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ?, gm = ? WHERE id = ?");
            ps.setInt(1, paypalnx);
            ps.setInt(2, maplepoints);
            ps.setInt(3, cardnx);
            ps.setInt(4, gmLevel);
            ps.setInt(5, client.getAccID());
            ps.executeUpdate();
            if (storage != null)
                storage.saveToDB();
            if (gmLevel > 0) {
                ps = con.prepareStatement("INSERT INTO gmlog (`cid`, `command`) VALUES (?, ?)");
                ps.setInt(1, id);
                for (String com : commands) {
                    ps.setString(2, com);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            ps.close();
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
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
        client.getSession().write(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++)
            if (skillMacros[i] != null)
                macros = true;
        if (macros)
            client.getSession().write(MaplePacketCreator.getMacros(skillMacros));
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

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null)
            mgc.setAllianceRank(rank);
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
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

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setCygnusLinkId(int id) {
        this.cygnusLinkId = id;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.lastDojoStage = x;
    }
    
    public void setDojoStart() {
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis() + ((stage > 36 ? 15 : stage / 6 + 5) | 0) * 60000;
    }

    public void setDropMod() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11))
            this.dropMod = 2;
        this.dropMod = 1;
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

    public void setExpMod(boolean monsterbook, boolean enableMonsterBook) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11))
            this.expMod = 2;
        else
            this.expMod = 1;
        if (monsterbook)
            if (enableMonsterBook)
                this.expMod *= 1.15;
            else
                this.expMod /= 1.15;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
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
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            return;
        }
        hasMerchant = set;
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

    public String getMapName(int mapId) {
        return client.getChannelServer().getMapFactory().getMap(mapId).getMapName();
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
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

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        String name2 = visitor.getName();
        String game = omok ? "omok" : "matchcard";
        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET " + game + "wins = " + game + "wins + 1 WHERE name = ?");
                ps.setString(1, winnerslot == 1 ? name : name2);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE characters SET " + game + "losses = " + game + "losses + 1 WHERE name = ?");
                ps.setString(1, winnerslot == 1 ? name2 : name);
                ps.executeUpdate();
                ps.close();
            }
            if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET " + game + "ties = " + game + "ties + 1 WHERE name = ? or name = ?");
                ps.setString(1, name);
                ps.setString(2, name2);
                ps.executeUpdate();
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
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
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = " + code);
        ps.executeUpdate();
        ps.close();
        ps = DatabaseConnection.getConnection().prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
        ps.setString(1, this.getName());
        ps.setString(2, code);
        ps.executeUpdate();
        ps.close();
    }

    public void setParty(MapleParty party) {
        this.party = party;
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

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWatchedCygnusIntro(boolean set) {
        this.watchedCygnusIntro = set;
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

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 0)
            time = ((stage > 36 ? 15 : stage / 6 + 5) | 0) * 60;
        else
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        client.getSession().write(MaplePacketCreator.getClock((int) time));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.getPlayer().changeMap(client.getChannelServer().getMapFactory().getMap(925020000));
            }
        }, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then warp
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
                client.getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(MapleCharacter.this));
            } catch (Exception e) {
                e.printStackTrace();
                client.getChannelServer().reconnectWorld();
            }
    }

    public static class SkillEntry {
        public int skillevel, masterlevel;

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

    public void startCygnusIntro() {
        cancelAllBuffs(); //you shouldn't have buffs anyways
        client.getSession().write(MaplePacketCreator.cygnusIntroDisableUI(true));
        client.getSession().write(MaplePacketCreator.cygnusIntroLock(true));
        saveLocation("CYGNUSINTRO");
        MapleMap introMap = client.getChannelServer().getMapFactory().getMap(913040000);
        changeMap(introMap, introMap.getPortal(0));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.getSession().write(MaplePacketCreator.cygnusIntroDisableUI(false));
                client.getSession().write(MaplePacketCreator.cygnusIntroLock(false));
            }
        }, 54 * 1000);
        savedLocations[SavedLocationType.CYGNUSINTRO.ordinal()] = null;
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
                    client.getSession().write(MaplePacketCreator.updatePet(pet));
                }
            }
        }, 1800000, 1800000);
        switch (petSlot) {
            case 0:
                fullnessSchedule = schedule;
            case 1:
                fullnessSchedule_1 = schedule;
            case 2:
                fullnessSchedule_2 = schedule;
        }
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().getSession().write(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getClient().getSession().write(mapEffect.makeDestroyData());
            }
        }, duration);
    }

    public void startMapTimeLimitTask(final MapleMap from, final MapleMap to) {
        if (to.getTimeLimit() > 0 && from != null) {
            final MapleCharacter chr = this;
            mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    MaplePortal pfrom = null;
                    int id = from.getId();
                    if (id == 100020000 || id == 105040304 || id == 105050100 || id == 221023400)
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
        whitechat = !whitechat;
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
        if (this.getPet(this.getPetIndex(pet)) != null)
            this.getPet(this.getPetIndex(pet)).saveToDb();
        cancelFullnessSchedule(getPetIndex(pet));
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        client.getSession().write(MaplePacketCreator.petStatUpdate(this));
        client.getSession().write(MaplePacketCreator.enableActions());
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
                        other.client.getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
                }
        }
    }

    public void updateQuest(MapleQuestStatus quest) {
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

    public Collection<MapleInventory> allInventories() {
        return Arrays.asList(inventory);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    public void sendSpawnData(MapleClient client) {
        if ((this.isHidden() && gmLevel > 0) || !this.isHidden()) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            for (int i = 0; i < 3; i++)
                if (pets[i] != null)
                    client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
        }
    }

    @Override
    public void setObjectId(int id) {
        return;
    }

    @Override
    public String toString() {
        return name;
    }
    //Wedding -- TODO
//    private LinkedList<MapleCharacter> guestlist = new LinkedList<MapleCharacter>();
//    private int isMarried;
//    private int isGuest;
//    private boolean weddingStarted;
//    public boolean getWeddingStarted() {return weddingStarted;}
//    public void setWeddingStarted(boolean b) {this.weddingStarted = b;}
//    public boolean isMarried() {
//        return isMarried > 0;
//    }
//    public int getMarried() {
//        return isMarried;
//    }
//    public boolean isGuest(int id) {
//        return isGuest == id;
//    }
//    public void setGuest(int id) {
//        this.isGuest = id;
//    }
//    public void addToGuest(MapleCharacter mc) {
//        if (mc.haveItem(4031377) && mc.isGuest(id))
//            return;
//        MapleInventoryManipulator.addById(mc.getClient(), 4031377, (short) 1, null);
//        guestlist.add(mc);
//        mc.setGuest(id);
//        MapleInventoryManipulator.removeById(client, MapleInventoryType.ETC, 4031377, 1, false, false);
//    }
}
