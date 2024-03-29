package net.sf.odinms.client;

import net.sf.odinms.server.constants.Skills;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import net.sf.odinms.server.MapleAchievements;  
import net.sf.odinms.client.anticheat.CheatTracker;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.server.maps.MapleMapEffect;
import net.sf.odinms.database.DatabaseException;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.PacketProcessor;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleMessenger;
import net.sf.odinms.net.world.MapleMessengerCharacter;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.PlayerBuffValueHolder;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.scripting.event.EventInstanceManager;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePlayerShop;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.MapleStorage;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.AbstractAnimatedMapleMapObject;
import net.sf.odinms.server.maps.MapleDoor;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapFactory;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.MapleSummon;
import net.sf.odinms.server.maps.SavedLocationType;
import net.sf.odinms.server.maps.SavedLocation;
import net.sf.odinms.server.quest.MapleCustomQuest;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.net.world.guild.*;

import net.sf.odinms.server.life.MobSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer {
	private static Logger log = LoggerFactory.getLogger(PacketProcessor.class);
	public static final double MAX_VIEW_RANGE_SQ = 850 * 850;
	private int world;
	private int accountid;
	private int rank;
	private int rankMove;
	private int jobRank;
	private int jobRankMove;
	private String name;
	private int level;
	private int str, dex, luk, int_;
	private AtomicInteger exp = new AtomicInteger();
	private int hp, maxhp;
	private int mp, maxmp;
	private int mpApUsed, hpApUsed;
	private int hair, face;
	private AtomicInteger meso = new AtomicInteger();
	private int remainingAp, remainingSp;
        private SavedLocation savedLocations[];
	private int fame;
	private int points;
	private int reborns;
	private long lastfametime;
	private List<Integer> lastmonthfameids;
	private int equipSlots = 100;
	private int useSlots = 100;
	private int setupSlots = 100;
	private int etcSlots = 100;
        private long dojoFinish;
	private int cashSlots = 100;
	// local stats represent current stats of the player to avoid expensive operations
	private transient int localmaxhp, localmaxmp;
	private transient int localstr, localdex, localluk, localint_;
	private transient int magic, watk;
	private transient double speedMod, jumpMod;
	private transient int localmaxbasedamage;
	private int id;
	private int parentId;
	private int childId;
        private int achivementpoints;
	private MapleClient client;
	private MapleMap map;
	private int initialSpawnPoint;
	// mapid is only used when calling getMapId() with map == null, it is not updated when running in channelserver mode
	private int mapid;
	private MapleShop shop = null;
	private MaplePlayerShop playerShop = null;
	private MapleStorage storage = null;
	private MaplePet[] pets = new MaplePet[3];
	private SkillMacro[] skillMacros = new SkillMacro[5];
	private MapleTrade trade = null;
	private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
	private MapleJob job = MapleJob.BEGINNER;
	private int gender;
	private int gmLevel;
	private boolean hidden;
	private boolean canDoor = true;
	private int chair;
        private boolean DojoFinished;
        private int DojoPoints;
        private int dojoEnergy;
        private int DojoCompleted;
	private int itemEffect;
	private MapleParty party;
	private EventInstanceManager eventInstance = null;
	private MapleInventory[] inventory;
	private Map<MapleQuest, MapleQuestStatus> quests;
	private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
	private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
	private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
	private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>();
	private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
	private List<MapleDoor> doors = new ArrayList<MapleDoor>();
	private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
	private BuddyList buddylist;
	private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
	// anticheat related information
	private CheatTracker anticheat;
	private ScheduledFuture<?> dragonBloodSchedule;
	private ScheduledFuture<?> mapTimeLimitTask = null;
        private List<Integer> finishedAchievements = new ArrayList<Integer>();  
	//guild related information
	private int guildid;
	private int guildrank;
	private MapleGuildCharacter mgc = null;
	// cash shop related information
	private int cardNX;
	private int maplePoints;
	private int paypalNX;
	private boolean incs;
	private MapleMessenger messenger = null;
	int messengerposition = 4;
	private ScheduledFuture<?>[] fullnessSchedule = new ScheduledFuture<?>[3];
	private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
	private int markedMonster = 0;
	private int energyChargeLevel = 0;
	private int energybar = 0;
	private ScheduledFuture<?> energyChargeSchedule;
	private Byte hammerSlot = null;
	private int npcId = -1;
	private int battleshipHp = 0;
	private Map<Integer, String> entered = new LinkedHashMap<Integer, String>();

	private MapleCharacter() {
		setStance(0);
		inventory = new MapleInventory[MapleInventoryType.values().length];
		for (MapleInventoryType type : MapleInventoryType.values()) {
			inventory[type.ordinal()] = new MapleInventory(type, (byte) 100);
		}

                savedLocations = new SavedLocation[SavedLocationType.values().length];
                for (int i = 0; i < SavedLocationType.values().length; i++)
                    savedLocations[i] = null;

		quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
		anticheat = new CheatTracker(this);
		setPosition(new Point(0, 0));
	}

	public MapleCharacter getThis() {
		return this;
	}

	public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
		MapleCharacter ret = new MapleCharacter();
		ret.client = client;
		ret.id = charid;

		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
		ps.setInt(1, charid);
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
			throw new RuntimeException("Loading the Char Failed (char not found)");
		}
		ret.name = rs.getString("name");
		ret.childId = rs.getInt("childId");
		ret.parentId = rs.getInt("parentId");
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
		ret.reborns = rs.getInt("reborns");
                ret.achivementpoints = rs.getInt("achivementpoints");

		ret.hpApUsed = rs.getInt("hpApUsed");
		ret.mpApUsed = rs.getInt("mpApUsed");
		ret.remainingSp = rs.getInt("sp");
		ret.remainingAp = rs.getInt("ap");

		ret.meso.set(rs.getInt("meso"));

		ret.gmLevel = rs.getInt("gm") == 0 ? 0 : 1000;

		ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
		ret.gender = rs.getInt("gender");
		ret.job = MapleJob.getById(rs.getInt("job"));

		ret.hair = rs.getInt("hair");
		ret.face = rs.getInt("face");
		ret.DojoFinished = rs.getInt("DojoFinished") == 1;

		ret.accountid = rs.getInt("accountid");

		ret.mapid = rs.getInt("map");
		ret.initialSpawnPoint = rs.getInt("spawnpoint");
		ret.world = rs.getInt("world");
                ret.DojoPoints = rs.getInt("DojoPoints");
                ret.DojoCompleted = rs.getInt("DojoCompleted");
                
		ret.rank = rs.getInt("rank");
		ret.rankMove = rs.getInt("rankMove");
		ret.jobRank = rs.getInt("jobRank");
		ret.jobRankMove = rs.getInt("jobRankMove");

		ret.guildid = rs.getInt("guildid");
		ret.guildrank = rs.getInt("guildrank");
		if (ret.guildid > 0) {
			ret.mgc = new MapleGuildCharacter(ret);
		}
		int buddyCapacity = rs.getInt("buddyCapacity");
		ret.buddylist = new BuddyList(buddyCapacity);
		if (channelserver) {
			MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
			ret.map = mapFactory.getMap(ret.mapid);
			if (ret.map == null) { //char is on a map that doesn't exist warp it to henesys

				ret.map = mapFactory.getMap(100000000);
			}
                        else if (ret.map.getForcedReturnId() != 999999999) {
				ret.map = mapFactory.getMap(ret.map.getForcedReturnId());
			}
			MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
			if (portal == null) {
				portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead

				ret.initialSpawnPoint = 0;
			}
			ret.setPosition(portal.getPosition());

			int partyid = rs.getInt("party");
			if (partyid >= 0) {
				try {
					MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
					if (party != null && party.getMemberById(ret.id) != null) {
						ret.party = party;
					}
				} catch (RemoteException e) {
					client.getChannelServer().reconnectWorld();
				}
			}

			int messengerid = rs.getInt("messengerid");
			int position = rs.getInt("messengerposition");
			if (messengerid > 0 && position < 4 && position > -1) {
				try {
					WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
					MapleMessenger messenger = wci.getMessenger(messengerid);
					if (messenger != null) {
						ret.messenger = messenger;
						ret.messengerposition = position;
					}
				} catch (RemoteException e) {
					client.getChannelServer().reconnectWorld();
				}
			}
		}

		rs.close();
		ps.close();

		ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
		ps.setInt(1, ret.accountid);
		rs = ps.executeQuery();
		while (rs.next()) {
			ret.getClient().setAccountName(rs.getString("name"));
			ret.cardNX = rs.getInt("cardNX");
			ret.maplePoints = rs.getInt("maplePoints");
			ret.paypalNX = rs.getInt("paypalNX");
			ret.points = rs.getInt("points");
		}
		rs.close();
		ps.close();

		String sql = "SELECT * FROM inventoryitems " + "LEFT JOIN inventoryequipment USING (inventoryitemid) " + "WHERE characterid = ?";
		if (!channelserver) {
			sql += " AND inventorytype = " + MapleInventoryType.EQUIPPED.getType();
		}
		ps = con.prepareStatement(sql);
		ps.setInt(1, charid);

		rs = ps.executeQuery();
		while (rs.next()) {
			MapleInventoryType type = MapleInventoryType.getByType((byte) rs.getInt("inventorytype"));

			if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
				int itemid = rs.getInt("itemid");
				Equip equip = new Equip(itemid, (byte) rs.getInt("position"));
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
				equip.setLevel((byte) rs.getInt("level"));
				equip.setHammers((byte) rs.getInt("hammers"));
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
				if (cTime > -1) {
					status.setCompletionTime(cTime * 1000);
				}
				status.setForfeited(rs.getInt("forfeited"));
				ret.quests.put(q, status);
				pse.setInt(1, rs.getInt("queststatusid"));
				ResultSet rsMobs = pse.executeQuery();
				while (rsMobs.next()) {
					status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
				}
				rsMobs.close();
			}
			rs.close();
			ps.close();
			pse.close();

			ps = con.prepareStatement("SELECT skillid,skilllevel, masterlevel FROM skills WHERE characterid = ?");
			ps.setInt(1, charid);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getInt("skilllevel"), rs.getInt("masterlevel")));
			}
			rs.close();
			ps.close();

			ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
			ps.setInt(1, charid);
			rs = ps.executeQuery();

			while (rs.next()) {
				int skill1 = rs.getInt("skill1");
				int skill2 = rs.getInt("skill2");
				int skill3 = rs.getInt("skill3");
				String name = rs.getString("name");
				int shout = rs.getInt("shout");
				int position = rs.getInt("position");
				SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, position);
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
				ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
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
                        String achsql = "SELECT * FROM achievements WHERE accountid = ?";
                                ps = con.prepareStatement(achsql);
                                ps.setInt(1, ret.accountid);
                                rs = ps.executeQuery();
                                while (rs.next()) {
                                    ret.finishedAchievements.add(rs.getInt("achievementid"));
                        }
			ret.buddylist.loadFromDb(charid);
			ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
		}

		ret.recalcLocalStats();
		ret.silentEnforceMaxHpMp();
		ret.resetBattleshipHp();
		return ret;
	}

	public static MapleCharacter getDefault(MapleClient client, int chrid) {
		MapleCharacter ret = getDefault(client);
		ret.id = chrid;
		return ret;
	}

	public static MapleCharacter getDefault(MapleClient client) {
		MapleCharacter ret = new MapleCharacter();
		ret.client = client;
		ret.hp = 50;
		ret.maxhp = 50;
		ret.mp = 50;
		ret.maxmp = 50;
		ret.map = null;
		ret.exp.set(0);
		ret.gmLevel = 0;
		ret.job = MapleJob.BEGINNER;
		ret.meso.set(0);
		ret.level = 1;
		ret.accountid = client.getAccID();
		ret.buddylist = new BuddyList(25);
		ret.cardNX = 0;
		ret.maplePoints = 0;
		ret.paypalNX = 0;
		ret.incs = false;
		ret.childId = 0;
		ret.parentId = 0;

		ret.keymap.put(Integer.valueOf(18), new MapleKeyBinding(4, 0));
		ret.keymap.put(Integer.valueOf(65), new MapleKeyBinding(6, 106));
		ret.keymap.put(Integer.valueOf(2), new MapleKeyBinding(4, 10));
		ret.keymap.put(Integer.valueOf(23), new MapleKeyBinding(4, 1));
		ret.keymap.put(Integer.valueOf(3), new MapleKeyBinding(4, 12));
		ret.keymap.put(Integer.valueOf(4), new MapleKeyBinding(4, 13));
		ret.keymap.put(Integer.valueOf(5), new MapleKeyBinding(4, 18));
		ret.keymap.put(Integer.valueOf(6), new MapleKeyBinding(4, 21));
		ret.keymap.put(Integer.valueOf(16), new MapleKeyBinding(4, 8));
		ret.keymap.put(Integer.valueOf(17), new MapleKeyBinding(4, 5));
		ret.keymap.put(Integer.valueOf(19), new MapleKeyBinding(4, 4));
		ret.keymap.put(Integer.valueOf(25), new MapleKeyBinding(4, 19));
		ret.keymap.put(Integer.valueOf(26), new MapleKeyBinding(4, 14));
		ret.keymap.put(Integer.valueOf(27), new MapleKeyBinding(4, 15));
		ret.keymap.put(Integer.valueOf(29), new MapleKeyBinding(5, 52));
		ret.keymap.put(Integer.valueOf(31), new MapleKeyBinding(4, 2));
		ret.keymap.put(Integer.valueOf(34), new MapleKeyBinding(4, 17));
		ret.keymap.put(Integer.valueOf(35), new MapleKeyBinding(4, 11));
		ret.keymap.put(Integer.valueOf(37), new MapleKeyBinding(4, 3));
		ret.keymap.put(Integer.valueOf(38), new MapleKeyBinding(4, 20));
		ret.keymap.put(Integer.valueOf(40), new MapleKeyBinding(4, 16));
		ret.keymap.put(Integer.valueOf(43), new MapleKeyBinding(4, 9));
		ret.keymap.put(Integer.valueOf(44), new MapleKeyBinding(5, 50));
		ret.keymap.put(Integer.valueOf(45), new MapleKeyBinding(5, 51));
		ret.keymap.put(Integer.valueOf(46), new MapleKeyBinding(4, 6));
		ret.keymap.put(Integer.valueOf(50), new MapleKeyBinding(4, 7));
		ret.keymap.put(Integer.valueOf(56), new MapleKeyBinding(5, 53));
		ret.keymap.put(Integer.valueOf(59), new MapleKeyBinding(6, 100));
		ret.keymap.put(Integer.valueOf(60), new MapleKeyBinding(6, 101));
		ret.keymap.put(Integer.valueOf(61), new MapleKeyBinding(6, 102));
		ret.keymap.put(Integer.valueOf(62), new MapleKeyBinding(6, 103));
		ret.keymap.put(Integer.valueOf(63), new MapleKeyBinding(6, 104));
		ret.keymap.put(Integer.valueOf(64), new MapleKeyBinding(6, 105));

		ret.recalcLocalStats();

		return ret;
	}

	public void saveToDB(boolean update) {
		Connection con = DatabaseConnection.getConnection();
		try {
			// clients should not be able to log back before their old state is saved (see MapleClient#getLoginState) so we are save to switch to a very low isolation level here
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			// connections are thread local now, no need to
			// synchronize anymore =)
			con.setAutoCommit(false);
			PreparedStatement ps;
			if (update) {
				ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, " + "map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, childId = ?, reborns = ?, achivementpoints = ?, DojoFinished = ?, DojoPoints = ?, DojoCompleted = ? WHERE id = ?");
			} else {
				ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, accountid, name, world, childId, parentId, reborns, achivementpoints, DojoFinished, DojoPoints, DojoCompleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}

			ps.setInt(1, level);
			ps.setInt(2, fame);
			ps.setInt(3, str);
			ps.setInt(4, dex);
			ps.setInt(5, luk);
			ps.setInt(6, int_);
			ps.setInt(7, exp.get());
			ps.setInt(8, hp);
			ps.setInt(9, mp);
			ps.setInt(10, maxhp);
			ps.setInt(11, maxmp);
			ps.setInt(12, remainingSp);
			ps.setInt(13, remainingAp);
			ps.setInt(14, (gmLevel > 0 ? 1 : 0));
			ps.setInt(15, skinColor.getId());
			ps.setInt(16, gender);
			ps.setInt(17, job.getId());
			ps.setInt(18, hair);
			ps.setInt(19, face);
			if (map == null) {
				ps.setInt(20, 0);
			} else {
				if (map.getForcedReturnId() != 999999999) {
					ps.setInt(20, map.getForcedReturnId());
				} else {
					ps.setInt(20, map.getId());
				}
			}
			ps.setInt(21, meso.get());
			ps.setInt(22, hpApUsed);
			ps.setInt(23, mpApUsed);
			if (map == null) {
				ps.setInt(24, 0);
			} else {
				MaplePortal closest = map.findClosestSpawnpoint(getPosition());
				if (closest != null) {
					ps.setInt(24, closest.getId());
				} else {
					ps.setInt(24, 0);
				}
			}
			if (party != null) {
				ps.setInt(25, party.getId());
			} else {
				ps.setInt(25, -1);
			}
			ps.setInt(26, buddylist.getCapacity());

			if (messenger != null) {
				ps.setInt(27, messenger.getId());
				ps.setInt(28, messengerposition);
			} else {
				ps.setInt(27, 0);
				ps.setInt(28, 4);
			}

			if (update) {
				ps.setInt(29, childId);
				ps.setInt(30, reborns);
				ps.setInt(31, achivementpoints);
                                ps.setInt(32, DojoFinished ? 1 : 0);
                                ps.setInt(33, DojoPoints);
                                ps.setInt(34, DojoCompleted);
				ps.setInt(35, id);
			} else {
				ps.setInt(29, accountid);
				ps.setString(30, name);
				ps.setInt(31, world); 
				ps.setInt(32, childId);
				ps.setInt(33, parentId);
				ps.setInt(34, reborns);
				ps.setInt(35, achivementpoints);
				ps.setInt(36, DojoFinished ? 1 : 0);
                                ps.setInt(37, DojoPoints);
                                ps.setInt(38, DojoCompleted);
			}
			int updateRows = ps.executeUpdate();
			if (!update) {
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					this.id = rs.getInt(1);
				} else {
					throw new DatabaseException("Inserting char failed.");
				}
			} else if (updateRows < 1) {
				throw new DatabaseException("Character not in database (" + id + ")");
			}
			ps.close();

			for (int i = 0; i < 3; i++) {
				if (pets[i] != null) {
					pets[i].saveToDb();
				}
			}

			ps = con.prepareStatement("DELETE FROM skillmacros WHERE characterid = ?");
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();

			for (int i = 0; i < 5; i++) {
				SkillMacro macro = skillMacros[i];
				if (macro != null) {
					ps = con.prepareStatement("INSERT INTO skillmacros" + " (characterid, skill1, skill2, skill3, name, shout, position) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");

					ps.setInt(1, id);
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
			PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
					if (rs.next()) {
						itemid = rs.getInt(1);
					} else {
						throw new DatabaseException("Inserting char failed.");
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
						pse.setInt(19, equip.getHammers());
						pse.executeUpdate();
					}
				}
			}
			ps.close();
			pse.close();
			// psl.close();

			deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) " +
					" VALUES (DEFAULT, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
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
			ps.close();

			deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
			ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `group`, `pending`) VALUES (?, ?, ?, 0)");
			ps.setInt(1, id);
			for (BuddylistEntry entry : buddylist.getBuddies()) {
				if (entry.isVisible()) {
					ps.setInt(2, entry.getCharacterId());
					ps.setString(3, entry.getGroup());
					ps.executeUpdate();
				}
			}
			ps.close();

			ps = con.prepareStatement("UPDATE accounts SET `cardNX` = ?, `maplePoints` = ?, `paypalNX` = ?, `points` = ? WHERE id = ?");
			ps.setInt(1, cardNX);
			ps.setInt(2, maplePoints);
			ps.setInt(3, paypalNX);
			ps.setInt(4, points);
			ps.setInt(5, client.getAccID());
			ps.executeUpdate();
			ps.close();

			if (storage != null) {
				storage.saveToDB();
			}
                if (update) {
                ps = con.prepareStatement("DELETE FROM achievements WHERE accountid = ?");
                ps.setInt(1, accountid);
                ps.executeUpdate();
                ps.close();

                for (Integer achid : finishedAchievements) {
                    ps = con.prepareStatement("INSERT INTO achievements(charid, achievementid, accountid) VALUES(?, ?, ?)");
                    ps.setInt(1, id);
                    ps.setInt(2, achid);
                    ps.setInt(3, accountid);
                    ps.executeUpdate();
                    ps.close();
                }
            }   
			con.commit();
		} catch (Exception e) {
			log.error(MapleClient.getLogMessage(this, "[charsave] Error saving character data"), e);
			try {
				con.rollback();
			} catch (SQLException e1) {
				log.error(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back"), e);
			}
		} finally {
			try {
				con.setAutoCommit(true);
				con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			} catch (SQLException e) {
				log.error(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode"), e);
			}
		}
	}

	private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public MapleQuestStatus getQuest(MapleQuest quest) {
		if (!quests.containsKey(quest)) {
			return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
		}
		return quests.get(quest);
	}

	public void updateQuest(MapleQuestStatus quest) {
		quests.put(quest.getQuest(), quest);
		if (!(quest.getQuest() instanceof MapleCustomQuest)) {
			if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
				client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId()));
				client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 8));
			} else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
				client.getSession().write(MaplePacketCreator.completeQuest(this, (short) quest.getQuest().getId()));
			} else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
				client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
			}
		}
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

        public Map<Integer, MapleKeyBinding> getKeymap() {
            getClient().getSession().write(MaplePacketCreator. getKeymap(keymap));
            return keymap;
        }

	public Integer getBuffedValue(MapleBuffStat effect) {
		MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return Integer.valueOf(mbsvh.value);
	}

	public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
		MapleBuffStatValueHolder mbsvh = effects.get(stat);
		if (mbsvh == null) {
			return false;
		}
		return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
	}

	public int getBuffSource(MapleBuffStat stat) {
		MapleBuffStatValueHolder mbsvh = effects.get(stat);
		if (mbsvh == null) {
			return -1;
		}
		return mbsvh.effect.getSourceId();
	}

	public void setBuffedValue(MapleBuffStat effect, int value) {
		MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return;
		}
		mbsvh.value = value;
	}

	public Long getBuffedStarttime(MapleBuffStat effect) {
		MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return Long.valueOf(mbsvh.startTime);
	}

	public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
		MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return mbsvh.effect;
	}

	private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
		if (dragonBloodSchedule != null) {
			dragonBloodSchedule.cancel(false);
		}
		dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {
			@Override
			public void run() {
				addHP(-bloodEffect.getX());
				getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
				getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
			}
		}, 4000, 4000);
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
					getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
				}
			}
		}, 60000, 60000);
		fullnessSchedule[petSlot] = schedule;
	}

	public void cancelFullnessSchedule(int petSlot) {
		fullnessSchedule[petSlot].cancel(false);
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
					if (MapleItemInformationProvider.getInstance().isMiniDungeonMap(from.getId())) {
						pfrom = from.getPortal("MD00");
					} else {
						pfrom = from.getPortal(0);
					}
					if (pfrom != null) {
						chr.changeMap(from, pfrom);
					}
				}
			}, from.getTimeLimit() * 1000, from.getTimeLimit() * 1000);
		}
	}

	public void cancelMapTimeLimitTask() {
		if (mapTimeLimitTask != null) {
			mapTimeLimitTask.cancel(false);
		}
	}

	public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
		if (effect.isHide()) {
			this.hidden = true;
			getClient().getSession().write(MaplePacketCreator.sendGMOperation(16, 1));
			getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
		} else if (effect.isDragonBlood()) {
			prepareDragonBlood(effect);
		}
		for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
			effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
		}

		recalcLocalStats();
	}

	private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
		List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
		for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
			MapleBuffStatValueHolder mbsvh = stateffect.getValue();
			if (mbsvh.effect.sameSource(effect) && (startTime == -1 || startTime == mbsvh.startTime)) {
				stats.add(stateffect.getKey());
			}
		}
		return stats;
	}

	private void deregisterBuffStats(List<MapleBuffStat> stats) {
		List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
		for (MapleBuffStat stat : stats) {
			MapleBuffStatValueHolder mbsvh = effects.get(stat);
			if (mbsvh != null) {
				effects.remove(stat);
				boolean addMbsvh = true;
				for (MapleBuffStatValueHolder contained : effectsToCancel) {
					if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
						addMbsvh = false;
					}
				}
				if (addMbsvh) {
					effectsToCancel.add(mbsvh);
				}
				if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
					int summonId = mbsvh.effect.getSourceId();
					MapleSummon summon = summons.get(summonId);
					if (summon != null) {
						getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true), summon.getPosition());
						getMap().removeMapObject(summon);
						removeVisibleMapObject(summon);
						summons.remove(summonId);
					}
				} else if (stat == MapleBuffStat.DRAGONBLOOD) {
					dragonBloodSchedule.cancel(false);
					dragonBloodSchedule = null;
				}
			}
		}
		for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
			if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0) {
				cancelEffectCancelTasks.schedule.cancel(false);
			}
		}
	}

	/**
	 * @param effect
	 * @param overwrite when overwrite is set no data is sent and all the Buffstats in the StatEffect are deregistered
	 * @param startTime
	 */
	public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
		List<MapleBuffStat> buffstats;
		if (!overwrite) {
			buffstats = getBuffStats(effect, startTime);
		} else {
			List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
			buffstats = new ArrayList<MapleBuffStat>(statups.size());
			for (Pair<MapleBuffStat, Integer> statup : statups) {
				buffstats.add(statup.getLeft());
			}
		}
		deregisterBuffStats(buffstats);
		if (effect.isMagicDoor()) {
			// remove for all on maps
			if (!getDoors().isEmpty()) {
				MapleDoor door = getDoors().iterator().next();
				for (MapleCharacter chr : door.getTarget().getCharacters()) {
					door.sendDestroyData(chr.getClient());
				}
				for (MapleCharacter chr : door.getTown().getCharacters()) {
					door.sendDestroyData(chr.getClient());
				}
				for (MapleDoor destroyDoor : getDoors()) {
					door.getTarget().removeMapObject(destroyDoor);
					door.getTown().removeMapObject(destroyDoor);
				}
				clearDoors();
				silentPartyUpdate();
			}
		}

		if (!overwrite) {
			if (energyChargeSchedule != null) {
				this.energyChargeSchedule.cancel(false);
			}
			this.energyChargeSchedule = null;
			this.energyChargeLevel = 0;
			cancelPlayerBuffs(buffstats);
			if (effect.isHide() && (MapleCharacter) getMap().getMapObject(getObjectId()) != null) {
				this.hidden = false;
				getClient().getSession().write(MaplePacketCreator.sendGMOperation(16, 0));
				getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
				//for (int i = 0; i < 3; i++) {
				//	if (pets[i] != null) {
				//		getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pets[i], false, false), false);
				//	}
				//}
			}
		}
	}

	public void cancelBuffStats(MapleBuffStat... stat) {
		List<MapleBuffStat> buffStatList = Arrays.asList(stat);
		deregisterBuffStats(buffStatList);
		cancelPlayerBuffs(buffStatList);
	}

	public void cancelEffectFromBuffStat(MapleBuffStat stat) {
		cancelEffect(effects.get(stat).effect, false, -1);
	}

	private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
		if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
			recalcLocalStats();
			enforceMaxHpMp();
			getClient().getSession().write(MaplePacketCreator.cancelBuff(buffstats));
			getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
		}
	}

	public void dispel() {
		LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			if (mbsvh.effect.isSkill()) {
				cancelEffect(mbsvh.effect, false, mbsvh.startTime);
			}
		}
	}

	public void cancelAllBuffs() {
		LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			cancelEffect(mbsvh.effect, false, mbsvh.startTime);
		}
	}

	public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
		for (PlayerBuffValueHolder mbsvh : buffs) {
			mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
		}
	}

	public List<PlayerBuffValueHolder> getAllBuffs() {
		List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
		for (MapleBuffStatValueHolder mbsvh : effects.values()) {
			ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
		}
		return ret;
	}

        public void cancelMagicDoor() {
                    LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
                    for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                            if (mbsvh.effect.isMagicDoor()) {
                                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                            }
                    }
            }

        public void handleEnergyChargeGain() {
            ISkill energycharge = SkillFactory.getSkill(5110001);
            int energyChargeSkillLevel = getSkillLevel(energycharge);
            if (energyChargeSkillLevel <= 0) {
                    energycharge = SkillFactory.getSkill(15100004);
                energyChargeSkillLevel = getSkillLevel(energycharge);
            }
            MapleStatEffect ceffect = null;
            ceffect = energycharge.getEffect(energyChargeSkillLevel);
            TimerManager tMan = TimerManager.getInstance();
            if (energyChargeSkillLevel > 0) {
                if (energybar < 10000) {
                    energybar = (energybar + 102);
                    if (energybar > 10000) {
                        energybar = 10000;
                    }

                    getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(energybar));
                    getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
                    getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
                    if (energybar == 10000) {
                        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
                    }
                }
                if (energybar >= 10000 && energybar < 11000) {
                    energybar = 15000;
                    final MapleCharacter chr = this;
                    tMan.schedule(new Runnable() {
                        @Override
                        public void run() {
                            getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(0));
                            getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
                            energybar = 0;
                        }
                    }, ceffect.getDuration());
                }

            }
        }
        
	public void handleOrbGain() {
		int orbcount = getBuffedValue(MapleBuffStat.COMBO);
		ISkill combo = SkillFactory.getSkill(Skills.Crusader.ComboAttack);
		ISkill advcombo = SkillFactory.getSkill(Skills.Hero.AdvancedComboAttack);

		if (getSkillLevel(combo) == 0) {
			combo = SkillFactory.getSkill(Skills.DawnWarrior3.ComboAttack);
			advcombo = SkillFactory.getSkill(Skills.DawnWarrior3.Advancedcombo);
		}

		MapleStatEffect ceffect = null;
		int advComboSkillLevel = getSkillLevel(advcombo);
		if (advComboSkillLevel > 0) {
			ceffect = advcombo.getEffect(advComboSkillLevel);
		} else {
			ceffect = combo.getEffect(getSkillLevel(combo));
		}

		if (orbcount < ceffect.getX() + 1) {
			int neworbcount = orbcount + 1;
			if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
				if (neworbcount < ceffect.getX() + 1) {
					neworbcount++;
				}
			}

			List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
			setBuffedValue(MapleBuffStat.COMBO, neworbcount);
			int duration = ceffect.getDuration();
			duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

			getClient().getSession().write(MaplePacketCreator.giveBuff(combo.getId(), duration, stat));
			getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
		}
	}

	public void handleOrbconsume() {
		ISkill combo = SkillFactory.getSkill(Skills.Crusader.ComboAttack);
		if (getSkillLevel(combo) == 0) {
			combo = SkillFactory.getSkill(Skills.DawnWarrior3.ComboAttack);
		}
		MapleStatEffect ceffect = combo.getEffect(getSkillLevel(combo));
		List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
		setBuffedValue(MapleBuffStat.COMBO, 1);
		int duration = ceffect.getDuration();
		duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));
		getClient().getSession().write(MaplePacketCreator.giveBuff(combo.getId(), duration, stat));
		getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
	}

	private void silentEnforceMaxHpMp() {
		setMp(getMp());
		setHp(getHp(), true);
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
		if (stats.size() > 0) {
			getClient().getSession().write(MaplePacketCreator.updatePlayerStats(stats));
		}
	}

	public MapleMap getMap() {
		return map;
	}

	/**
	 * only for tests
	 * 
	 * @param newmap
	 */
	public void setMap(MapleMap newmap) {
		this.map = newmap;
	}

	public int getMapId() {
		if (map != null) {
			return map.getId();
		}
		return mapid;
	}

	public int getInitialSpawnpoint() {
		return initialSpawnPoint;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

        public void setExp(int exp) {
            this.exp.set(exp);
        }

        public int getPoints() {
            return points;
        }

        public int getDojoPoints(){
            return DojoPoints;
        }

        public boolean getDojoFinished(){
            return DojoFinished;
        }

        public int getDojoEnergy() {
            return dojoEnergy;
        }
        
        public void setDojoEnergy(int x) {
            this.dojoEnergy = x;
        }

        public void setDojoStart() {
            int stage = (map.getId() / 100) % 100;
            this.dojoFinish = System.currentTimeMillis() + ((stage > 36 ? 15 : stage / 6 + 5) | 0) * 60000;
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

        public void changeMap(MapleMap to) {
            changeMap(to, to.getPortal(0));
        }
        
        public int getAchivements() {
            return achivementpoints;
        }

        public int getReborns() {
            return reborns;
        }

        public boolean isPartyLeader() {
            return this.party.getLeader() == this.party.getMemberById(this.getId());
        }
        
        public void addPoints(int addpoints) {
            points += addpoints;
        }

        public void setDojoFinished() {
            this.DojoFinished = true;
        }

        public void addDojoPoints(int points){
            DojoPoints += points;
        }

        public void addDojoCompleted(int points){
            DojoCompleted += points;
        }
        
        public void addAchivements(int points){
            achivementpoints +=achivementpoints;
        }

        public void removePoints(int removepoints) {
            if (points <= 0) {
                points = 0;
            } else if (removepoints >= points) {
                points = 0;
            } else {
                points -= removepoints;
            }
        }
        
        public boolean hasEntered(String script, int mapId) {
            if (entered.containsKey(mapId))
                    if (entered.get(mapId).equals(script))
                                    return true;
            return false;
        }

        public boolean hasEntered(String script) {
            for (int mapId : entered.keySet())
                    if (entered.get(mapId).equals(script))
                                    return true;
            return false;
        }
        
        public void enteredScript(String script, int mapid) {
            if (!entered.containsKey(mapid))
                    entered.put(mapid, script);
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
    
        public void doReborn() {
            setLevel(1);
            setExp(0);
            changeJob(MapleJob.getById(0));
            updateSingleStat(MapleStat.LEVEL, 1);
            updateSingleStat(MapleStat.JOB, 0);
            updateSingleStat(MapleStat.EXP, 0);
            reborns += 1;
        }

        public void maxSkill(int skillid) {
            if (Math.floor(skillid / 10000) == getJob().getId() || isGM() || skillid < 2000) {
                ISkill skill_ = SkillFactory.getSkill(skillid);
                int maxlevel = skill_.getMaxLevel();
                changeSkillLevel(skill_, maxlevel, maxlevel);
            }
        }

        public void maxAllSkills() {
            int[] skillId = {8, /*1000, 1001, 1002,*/ 1003, 1004, 1005, 1000000, 1000001, 1000002, 1001003, 1001004, 1001005, 1100000, 1100001,
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
                    4001344, 4100000, 4100001, 4100002, 4101003, 4101005, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006,
                    4120002, 4120005, 4121000, 4121003, 4121004, 4121006, 4121007, 4121008, 4121009, 4200000, 4200001, 4201002, 4201003, 4201004,
                    4201005, 4210000, 4211001, 4211002, 4211003, 4211004, 4211005, 4211006, 4220002, 4220005, 4221000, 4221001, 4221003, 4221004,
                    4221006, 4221007, 4221008, 5000000, 5001001, 5001002, 5001003, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005,
                    5101006, 5101007, 5110000, 5110001, 5111002, 5111004, 5111005, 5111006, 5121000, 5121001, 5121002, 5121003, 5121004, 5121005,
                    5121007, 5121008, 5121009, 5121010, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5210000, 5211001, 5211002,
                    5211004, 5211005, 5211006, 5220001, 5220002, 5220011, 5221000, 5221003, 5221004, 5221006, 5221007, 5221008, 5221009, 5221010,
                    10000012, 10001000, 10001001, 10001002, 10001003, 10001004, 10001005, 11000000, 11001001, 11001002, 11001003, 11001004, 12000000,
                    12001001, 12001002, 12001003, 12001004, 13000000, 13000001, 13001002, 13001003, 13001004, 14000000, 14000001, 14001002, 14001003,
                    14001004, 14001005, 15000000, 15001001, 15001002, 15001003, 15001004, 11100000, 11101001, 11101002, 11101003, 11101004, 11101005,
                    12101000, 12101001, 12101002, 12101003, 12101004, 12101005, 12101006, 13100000, 13100004, 13101001, 13101002, 13101003, 13101005,
                    13101006, 14100000, 14100001, 14100005, 14101002, 14101003, 14101006, 15100000, 15100001, 15100004, 15101002, 15101003,
                    15101005, 15101006, 11110000, 11110005, 11111001, 11111002, 11111003, 11111004, 11111006, 11111007, 12110000, 12110001, 12111002,
                    12111003, 12111004, 12111005, 12111006, 13110003, 13111000, 13111001, 13111002, 13111004, 13111005, 13111006, 13111007, 14110003,
                    14110004, 14111000, 14111001, 14111002, 14111005, 14111006, 15110000, 15111001, 15111002, 15111003, 15111004, 15111005, 15111006,
                    15111007
            };
            for (int skillzors_ : skillId) {
                maxSkill(skillzors_);
            }
        }
    
	public int getRank() {
		return rank;
	}

	public int getRankMove() {
		return rankMove;
	}

	public int getJobRank() {
		return jobRank;
	}

	public int getJobRankMove() {
		return jobRankMove;
	}

	public int getFame() {
		return fame;
	}

	public int getStr() {
		return str;
	}

	public int getDex() {
		return dex;
	}

	public int getLuk() {
		return luk;
	}

	public int getInt() {
		return int_;
	}

	public MapleClient getClient() {
		return client;
	}

	public int getExp() {
		return exp.get();
	}

	public int getHp() {
		return hp;
	}

	public int getMaxHp() {
		return maxhp;
	}

	public int getMp() {
		return mp;
	}

	public int getMaxMp() {
		return maxmp;
	}

	public int getRemainingAp() {
		return remainingAp;
	}

	public int getRemainingSp() {
		return remainingSp;
	}

	public int getMpApUsed() {
		return mpApUsed;
	}

	public void setMpApUsed(int mpApUsed) {
		this.mpApUsed = mpApUsed;
	}

	public int getHpApUsed() {
		return hpApUsed;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHpApUsed(int hpApUsed) {
		this.hpApUsed = hpApUsed;
	}

	public MapleSkinColor getSkinColor() {
		return skinColor;
	}

	public MapleJob getJob() {
		return job;
	}

	public int getGender() {
		return gender;
	}

	public int getHair() {
		return hair;
	}

	public int getFace() {
		return face;
	}

	public void setJob(MapleJob job) {
		this.job = job;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStr(int str) {
		this.str = str;
		recalcLocalStats();
	}

	public void setDex(int dex) {
		this.dex = dex;
		recalcLocalStats();
	}

	public void setLuk(int luk) {
		this.luk = luk;
		recalcLocalStats();
	}

	public void setInt(int int_) {
		this.int_ = int_;
		recalcLocalStats();
	}

	public void setMaxHp(int hp) {
		this.maxhp = hp;
		recalcLocalStats();
	}

	public void setMaxMp(int mp) {
		this.maxmp = mp;
		recalcLocalStats();
	}

	public void setHair(int hair) {
		this.hair = hair;
	}

	public void setFace(int face) {
		this.face = face;
	}

	public void setRemainingAp(int remainingAp) {
		this.remainingAp = remainingAp;
	}

	public void setRemainingSp(int remainingSp) {
		this.remainingSp = remainingSp;
	}

	public void setSkinColor(MapleSkinColor skinColor) {
		this.skinColor = skinColor;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public void setGM(int gmlevel) {
		this.gmLevel = gmlevel;
	}

	public CheatTracker getCheatTracker() {
		return anticheat;
	}

	public BuddyList getBuddylist() {
		return buddylist;
	}

	public void addFame(int famechange) {
		this.fame += famechange;
	}

	public void changeMap(final MapleMap to, final Point pos) {
		/*getClient().getSession().write(MaplePacketCreator.spawnPortal(map.getId(), to.getId(), pos));
		if (getParty() != null) {
		getClient().getSession().write(MaplePacketCreator.partyPortal(map.getId(), to.getId(), pos));
		}*/
		MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, 0x80, this);
		changeMapInternal(to, pos, warpPacket);
	}

	public void changeMap(final MapleMap to, final MaplePortal pto) {
		MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, pto.getId(), this);
		changeMapInternal(to, pto.getPosition(), warpPacket);
	}

	private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
		warpPacket.setOnSend(new Runnable() {
			@Override
			public void run() {
				map.removePlayer(MapleCharacter.this);
				if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
					map = to;
					setPosition(pos);
					to.addPlayer(MapleCharacter.this);
					if (party != null) {
						silentPartyUpdate();
						getClient().getSession().write(MaplePacketCreator.updateParty(getClient().getChannel(), party, PartyOperation.SILENT_UPDATE, null));
						updatePartyMemberHP();
					}
				}
			}
		});
		getClient().getSession().write(warpPacket);
	}

	public void leaveMap() {
		controlled.clear();
		visibleMapObjects.clear();
		if (chair != 0) {
			chair = 0;
		}
	}

	public void changeJob(MapleJob newJob) {
		this.job = newJob;
		this.remainingSp++;
		updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
		updateSingleStat(MapleStat.JOB, newJob.getId());
		getMap().broadcastMessage(this, MaplePacketCreator.showJobChange(getId()), false);
		silentPartyUpdate();
		guildUpdate();
	}

	public void gainAp(int ap) {
		this.remainingAp += ap;
		updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
	}

	public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
		skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
		this.getClient().getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
	}

	public void setHp(int newhp) {
		setHp(newhp, false);
	}

	public void setHp(int newhp, boolean silent) {
		int oldHp = hp;
		int thp = newhp;
		if (thp < 0) {
			thp = 0;
		}
		if (thp > localmaxhp) {
			thp = localmaxhp;
		}
		this.hp = thp;

		if (!silent) {
			updatePartyMemberHP();
		}
		if (oldHp > hp && !isAlive()) {
			playerDead();
		}
	}

	private void playerDead() {
		if (getEventInstance() != null) {
			getEventInstance().playerKilled(this);
		}
		cancelAllBuffs();
		getClient().getSession().write(MaplePacketCreator.enableActions());
	}

	public void updatePartyMemberHP() {
		if (party != null) {
			int channel = client.getChannel();
			for (MaplePartyCharacter partychar : party.getMembers()) {
				if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
					MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
					if (other != null) {
						other.getClient().getSession().write(
								MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
					}
				}
			}
		}
	}

	public void receivePartyMemberHP() {
		if (party != null) {
			int channel = client.getChannel();
			for (MaplePartyCharacter partychar : party.getMembers()) {
				if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
					MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
					if (other != null) {
						getClient().getSession().write(
								MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
					}
				}
			}
		}
	}

	public void setMp(int newmp) {
		int tmp = newmp;
		if (tmp < 0) {
			tmp = 0;
		}
		if (tmp > localmaxmp) {
			tmp = localmaxmp;
		}
		this.mp = tmp;
	}

	/**
	 * Convenience function which adds the supplied parameter to the current hp then directly does a updateSingleStat.
	 * 
	 * @see MapleCharacter#setHp(int)
	 * @param delta
	 */
	public void addHP(int delta) {
		setHp(hp + delta);
		updateSingleStat(MapleStat.HP, hp);
	}

	/**
	 * Convenience function which adds the supplied parameter to the current mp then directly does a updateSingleStat.
	 * 
	 * @see MapleCharacter#setMp(int)
	 * @param delta
	 */
	public void addMP(int delta) {
		setMp(mp + delta);
		updateSingleStat(MapleStat.MP, mp);
	}

	public void addMPHP(int hpDiff, int mpDiff) {
		setHp(hp + hpDiff);
		setMp(mp + mpDiff);
		List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
		stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(hp)));
		stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(mp)));
		MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(stats);
		client.getSession().write(updatePacket);
	}

	/**
	 * Updates a single stat of this MapleCharacter for the client. This method only creates and sends an update packet,
	 * it does not update the stat stored in this MapleCharacter instance.
	 * 
	 * @param stat
	 * @param newval
	 * @param itemReaction
	 */
	public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
		Pair<MapleStat, Integer> statpair = new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval));
		MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(Collections.singletonList(statpair), itemReaction);
		client.getSession().write(updatePacket);
	}

	public void updateSingleStat(MapleStat stat, int newval) {
		updateSingleStat(stat, newval, false);
	}

	public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
		if (getLevel() < 200) {
			int newexp = this.exp.addAndGet(gain);
			updateSingleStat(MapleStat.EXP, newexp);
		}
		if (show) {
			client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white));
		}
		while (level < getMaxLevel() && exp.get() >= ExpTable.getExpNeededForLevel(level + 1)) {
			levelUp();
		}
	}

	public int getMaxLevel() {
		if (job.isA(MapleJob.NOBLESSE)) {
			return 120;
		}
		return 200;
	}

	public void silentPartyUpdate() {
		if (party != null) {
			try {
				getClient().getChannelServer().getWorldInterface().updateParty(party.getId(),
						PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(MapleCharacter.this));
			} catch (RemoteException e) {
				log.error("REMOTE THROW", e);
				getClient().getChannelServer().reconnectWorld();
			}
		}
	}

	public void gainExp(int gain, boolean show, boolean inChat) {
		gainExp(gain, show, inChat, true);
	}

	public boolean isGM() {
		return gmLevel > 0;
	}

	public int getGMLevel() {
		return gmLevel;
	}

	public boolean hasGmLevel(int level) {
		return gmLevel >= level;
	}

	public MapleInventory getInventory(MapleInventoryType type) {
		return inventory[type.ordinal()];
	}

	public MapleShop getShop() {
		return shop;
	}

	public void setShop(MapleShop shop) {
		this.shop = shop;
	}

	public int getMeso() {
		return meso.get();
	}

        public int getSavedLocation(String type) {
            int m = savedLocations[SavedLocationType.fromString(type).ordinal()].getMapId();
            clearSavedLocation(SavedLocationType.fromString(type));
            return m;
        }

        public void saveLocation(String type) {
            MaplePortal closest = map.findClosestPortal(getPosition());
            savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
        }
        
        public void clearSavedLocation(SavedLocationType type) {
            savedLocations[type.ordinal()] = null;
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
		int newVal = meso.addAndGet(gain);
		updateSingleStat(MapleStat.MESO, newVal, enableActions);
		if (show) {
			client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, inChat));
		}
	}

	/**
	 * Adds this monster to the controlled list. The monster must exist on the Map.
	 * 
	 * @param monster
	 */
	public void controlMonster(MapleMonster monster, boolean aggro) {
		monster.setController(this);
		controlled.add(monster);
		client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
	}

	public void stopControllingMonster(MapleMonster monster) {
		controlled.remove(monster);
	}

	public Collection<MapleMonster> getControlledMonsters() {
		return Collections.unmodifiableCollection(controlled);
	}

	public int getNumControlledMonsters() {
		return controlled.size();
	}

	@Override
	public String toString() {
		return "Character: " + this.name;
	}

	public int getAccountID() {
		return accountid;
	}

	public void mobKilled(int id) {
		for (MapleQuestStatus q : quests.values()) {
			if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
				continue;
			}
			if (q.mobKilled(id) && !(q.getQuest() instanceof MapleCustomQuest)) {
				client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
				if (q.getQuest().canComplete(this, null)) {
					client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
				}
			}
		}
	}

	public final List<MapleQuestStatus> getStartedQuests() {
		List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
		for (MapleQuestStatus q : quests.values()) {
			if (q.getStatus().equals(MapleQuestStatus.Status.STARTED) && !(q.getQuest() instanceof MapleCustomQuest)) {
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public final List<MapleQuestStatus> getCompletedQuests() {
		List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
		for (MapleQuestStatus q : quests.values()) {
			if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED) && !(q.getQuest() instanceof MapleCustomQuest)) {
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public MaplePlayerShop getPlayerShop() {
		return playerShop;
	}

	public void setPlayerShop(MaplePlayerShop playerShop) {
		this.playerShop = playerShop;
	}

	public Map<ISkill, SkillEntry> getSkills() {
		return Collections.unmodifiableMap(skills);
	}

	public int getSkillLevel(ISkill skill) {
		SkillEntry ret = skills.get(skill);
		if (ret == null) {
			return 0;
		}
		return ret.skillevel;
	}

	public int getMasterLevel(ISkill skill) {
		SkillEntry ret = skills.get(skill);
		if (ret == null) {
			return 0;
		}
		return ret.masterlevel;
	}

	// the equipped inventory only contains equip... I hope
	public int getTotalDex() {
		return localdex;
	}

	public int getTotalInt() {
		return localint_;
	}

	public int getTotalStr() {
		return localstr;
	}

	public int getTotalLuk() {
		return localluk;
	}

	public int getTotalMagic() {
		return magic;
	}

	public double getSpeedMod() {
		return speedMod;
	}

	public double getJumpMod() {
		return jumpMod;
	}

	public int getTotalWatk() {
		return watk;
	}

	private static int rand(int lbound, int ubound) {
		return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
	}

	public void levelUp() {
		if (job.getId() >= 1000 && job.getId() <= 1511 && getLevel() < 70) {
			remainingAp += 1;
		}
		remainingAp += 5;
		if (job == MapleJob.BEGINNER) {
			maxhp += rand(14, 16);
			maxmp += rand(10, 12);
		} else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.GM) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)) {
			maxhp += rand(20, 24);
			maxmp += rand(14, 16);
		} else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
			int mpskill = job.isA(MapleJob.BLAZEWIZARD1) ? 12000000 : 2000001;
			ISkill improvingMaxMP = SkillFactory.getSkill(mpskill);
			int improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
			if (improvingMaxMPLevel > 0) {
				maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
			}
			maxhp += rand(10, 14);
			maxmp += rand(20, 24);
		} else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
			int hpskill = job.isA(MapleJob.DAWNWARRIOR1) ? 11000000 : 1000001;
			ISkill improvingMaxHP = SkillFactory.getSkill(hpskill);
			int improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
			if (improvingMaxHPLevel > 0) {
				maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
			}
			maxhp += rand(22, 26);
			maxmp += rand(4, 7);
		} else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
			int hpskill = job.isA(MapleJob.THUNDERBREAKER1) ? 15100000 : 5100000;
			ISkill improvingMaxHP = SkillFactory.getSkill(hpskill);
			int improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
			if (improvingMaxHPLevel > 0) {
				maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
			}
			maxhp += rand(22, 28);
			maxmp += rand(18, 23);
		}

		maxmp += getTotalInt() / 10;
		exp.addAndGet(-ExpTable.getExpNeededForLevel(level + 1));
		level += 1;
		if (level == getMaxLevel()) {
			exp.set(0);
			MaplePacket packet = MaplePacketCreator.serverNotice(0, "Congratulations to " + getName() + " for reaching level 200!");
			try {
				getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), packet.getBytes());
			} catch (RemoteException e) {
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
        if (level == 200 && !isGM()) {
            exp.set(0);
            MaplePacket packet = MaplePacketCreator.serverNotice(6, "[Congratulations] " + getName() + " has reached Level 200! Congratulate " + getName() + " on such an amazing achievement!");
            try {
                getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), packet.getBytes());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
                if (level == 200 && !isGM()) {
            exp.set(0);
            MaplePacket packet = MaplePacketCreator.serverNotice(6, "[Congratulations] " + getName() + " has reached Level 200! Congratulate " + getName() + " on such an amazing achievement!");
            try {
                getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), packet.getBytes());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }  
		setHp(maxhp);
		setMp(maxmp);
		getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
		getMap().broadcastMessage(this, MaplePacketCreator.showLevelup(getId()), false);
		recalcLocalStats();
		silentPartyUpdate();
		guildUpdate();
	}

	public void changeKeybinding(int key, MapleKeyBinding keybinding) {
		if (keybinding.getType() != 0) {
			keymap.put(Integer.valueOf(key), keybinding);
		} else {
			keymap.remove(Integer.valueOf(key));
		}
	}
public void setAchievementFinished(int id) {
        finishedAchievements.add(id);
    }

    public boolean achievementFinished(int achievementid) {
        return finishedAchievements.contains(achievementid);
    }

    public void finishAchievement(int id) {
        if (!achievementFinished(id)) {
            if (isAlive()) {
                MapleAchievements.getInstance().getById(id).finishAchievement(this);
            }
        }
    }

    public List<Integer> getFinishedAchievements() {
        return finishedAchievements;
    }  
	public void sendKeymap() {
		getClient().getSession().write(MaplePacketCreator.getKeymap(keymap));
	}

	public void sendMacros() {
		boolean macros = false;
		for (int i = 0; i < 5; i++) {
			if (skillMacros[i] != null) {
				macros = true;
			}
		}
		if (macros) {
			getClient().getSession().write(MaplePacketCreator.getMacros(skillMacros));
		}
	}

	public void updateMacros(int position, SkillMacro updateMacro) {
		skillMacros[position] = updateMacro;
	}

	public void tempban(String reason, Calendar duration, int greason) {
		if (lastmonthfameids == null) {
			throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
		}
		tempban(reason, duration, greason, client.getAccID());
		client.getSession().close();
	}

	public static boolean tempban(String reason, Calendar duration, int greason, int accountid) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
			Timestamp TS = new Timestamp(duration.getTimeInMillis());
			ps.setTimestamp(1, TS);
			ps.setString(2, reason);
			ps.setInt(3, greason);
			ps.setInt(4, accountid);
			ps.executeUpdate();
			ps.close();
			return true;
		} catch (SQLException ex) {
			log.error("Error while tempbanning", ex);
		}
		return false;
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
			log.error("Error while banning", ex);
		}
		client.getSession().close();
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
			if (accountId) {
				ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
			} else {
				ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
			}
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
			log.error("Error while banning", ex);
		}
		return false;
	}

	/**
	 * Oid of players is always = the cid
	 */
	@Override
	public int getObjectId() {
		return getId();
	}

	/**
	 * Throws unsupported operation exception, oid of players is read only
	 */
	@Override
	public void setObjectId(int id) {
		throw new UnsupportedOperationException();
	}

	public MapleStorage getStorage() {
		return storage;
	}

	public int getCurrentMaxHp() {
		return localmaxhp;
	}

	public int getCurrentMaxMp() {
		return localmaxmp;
	}

	public int getCurrentMaxBaseDamage() {
		return localmaxbasedamage;
	}

	public int calculateMaxBaseDamage(int watk) {
		int maxbasedamage;
		if (watk == 0) {
			maxbasedamage = 1;
		} else {
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
				maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
				//just some saveguard against rounding errors, we want to a/b for this
				maxbasedamage += 10;
			} else {
				maxbasedamage = 0;
			}
		}
		return maxbasedamage;
	}

	public void addVisibleMapObject(MapleMapObject mo) {
		visibleMapObjects.add(mo);
	}

	public void removeVisibleMapObject(MapleMapObject mo) {
		visibleMapObjects.remove(mo);
	}

	public boolean isMapObjectVisible(MapleMapObject mo) {
		return visibleMapObjects.contains(mo);
	}

	public Collection<MapleMapObject> getVisibleMapObjects() {
		return Collections.unmodifiableCollection(visibleMapObjects);
	}

	public boolean isAlive() {
		return this.hp > 0;
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		if (!this.isHidden()) {
			client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
			for (int i = 0; i < 3; i++) {
				if (pets[i] != null) {
					client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
				}
			}
		}
	}

	private void recalcLocalStats() {
		int oldmaxhp = localmaxhp;
		localmaxhp = getMaxHp();
		localmaxmp = getMaxMp();
		localdex = getDex();
		localint_ = getInt();
		localstr = getStr();
		localluk = getLuk();
		int speed = 100;
		int jump = 100;
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
		if (hbhp != null) {
			localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
		}
		Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
		if (hbmp != null) {
			localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
		}
		localmaxhp = Math.min(30000, localmaxhp);
		localmaxmp = Math.min(30000, localmaxmp);
		Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
		if (watkbuff != null) {
			watk += watkbuff.intValue();
		}
		if (job.isA(MapleJob.BOWMAN)) {
			ISkill expert = null;
			if (job.isA(MapleJob.CROSSBOWMASTER)) {
				expert = SkillFactory.getSkill(Skills.Marksman.MarksmanBoost);
			} else if (job.isA(MapleJob.BOWMASTER)) {
				expert = SkillFactory.getSkill(Skills.Bowmaster.BowExpert);
			}
			if (expert != null) {
				int boostLevel = getSkillLevel(expert);
				if (boostLevel > 0) {
					watk += expert.getEffect(boostLevel).getX();
				}
			}
		}
		Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
		if (matkbuff != null) {
			magic += matkbuff.intValue();
		}
		Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
		if (speedbuff != null) {
			speed += speedbuff.intValue();
		}
		Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
		if (jumpbuff != null) {
			jump += jumpbuff.intValue();
		}
		if (speed > 140) {
			speed = 140;
		}
		if (jump > 123) {
			jump = 123;
		}
		speedMod = speed / 100.0;
		jumpMod = jump / 100.0;
		Integer mount = getBuffedValue(MapleBuffStat.MONSTER_RIDING);
		if (mount != null) {
			switch (mount.intValue()) {
				case 1004:
					int mountId = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
					switch (mountId) {
						case 1902000:
							speedMod = 1.5;
							break;
						case 1902001:
							speedMod = 1.7;
							break;
						case 1902002:
							speedMod = 1.8;
							break;
					}
					jumpMod = 1.23;
					break;
				case 5221006:
					// AFAIK Battleship doesn't give any speed/jump bonuses
					break;
				default:
					log.warn("Unhandeled monster riding level");
			}
		}
		localmaxbasedamage = calculateMaxBaseDamage(watk);
		if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
			updatePartyMemberHP();
		}
	}

	public void equipChanged() {
		getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
		recalcLocalStats();
		enforceMaxHpMp();
		if (getClient().getPlayer().getMessenger() != null) {
			WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
			try {
				wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
			} catch (RemoteException e) {
				getClient().getChannelServer().reconnectWorld();
			}
		}
	}

	public MaplePet getPet(int index) {
		return pets[index];
	}

	public void addPet(MaplePet pet) {
		for (int i = 0; i < 3; i++) {
			if (pets[i] == null) {
				pets[i] = pet;
				return;
			}
		}
	}

	public void removePet(MaplePet pet, boolean shift_left) {
		int slot = -1;
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				if (pets[i].getUniqueId() == pet.getUniqueId()) {
					pets[i] = null;
					slot = i;
					break;
				}
			}
		}
		if (shift_left) {
			if (slot > -1) {
				for (int i = slot; i < 3; i++) {
					if (i != 2) {
						pets[i] = pets[i + 1];
					} else {
						pets[i] = null;
					}
				}
			}
		}
	}

	public int getNoPets() {
		int ret = 0;
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				ret++;
			}
		}
		return ret;
	}

	public int getPetIndex(MaplePet pet) {
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				if (pets[i].getUniqueId() == pet.getUniqueId()) {
					return i;
				}
			}
		}
		return -1;
	}

	public int getPetIndex(int petId) {
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				if (pets[i].getUniqueId() == petId) {
					return i;
				}
			}
		}
		return -1;
	}

	public int getNextEmptyPetIndex() {
		if (pets[0] == null) {
			return 0;
		}
		if (pets[1] == null) {
			return 1;
		}
		if (pets[2] == null) {
			return 2;
		}
		return 3;
	}

	public MaplePet[] getPets() {
		return pets;
	}

	public void unequipAllPets() {
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				unequipPet(pets[i], true);
			}
		}
	}

	public void unequipPet(MaplePet pet, boolean shift_left) {
		unequipPet(pet, shift_left, false);
	}

	public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
		cancelFullnessSchedule(getPetIndex(pet));

		pet.saveToDb();

		// Broadcast the packet to the map - with null instead of MaplePet
		getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);

		// Make a new list for the stat updates
		List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
		stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));

		// Write the stat update to the player...
		getClient().getSession().write(MaplePacketCreator.petStatUpdate(this));
		getClient().getSession().write(MaplePacketCreator.enableActions());

		// Un-assign the pet set to the player
		removePet(pet, shift_left);
	}

	public void shiftPetsRight() {
		if (pets[2] == null) {
			pets[2] = pets[1];
			pets[1] = pets[0];
			pets[0] = null;
		}
	}

	public FameStatus canGiveFame(MapleCharacter from) {
		if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
			return FameStatus.NOT_TODAY;
		} else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
			return FameStatus.NOT_THIS_MONTH;
		} else {
			return FameStatus.OK;
		}
	}

	public void hasGivenFame(MapleCharacter to) {
		lastfametime = System.currentTimeMillis();
		lastmonthfameids.add(Integer.valueOf(to.getId()));
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
			ps.setInt(1, getId());
			ps.setInt(2, to.getId());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			log.error("ERROR writing famelog for char " + getName() + " to " + to.getName(), e);
		}
	}

	public MapleParty getParty() {
		return party;
	}

	public int getPartyId() {
		return (party != null ? party.getId() : -1);
	}

	public int getWorld() {
		return world;
	}

	public void setWorld(int world) {
		this.world = world;
	}

	public void setParty(MapleParty party) {
		this.party = party;
	}

	public MapleTrade getTrade() {
		return trade;
	}

	public void setTrade(MapleTrade trade) {
		this.trade = trade;
	}

	public EventInstanceManager getEventInstance() {
		return eventInstance;
	}

	public void setEventInstance(EventInstanceManager eventInstance) {
		this.eventInstance = eventInstance;
	}

	public void addDoor(MapleDoor door) {
		doors.add(door);
	}

	public void clearDoors() {
		doors.clear();
	}

	public List<MapleDoor> getDoors() {
		return new ArrayList<MapleDoor>(doors);
	}

	public boolean canDoor() {
		return canDoor;
	}

	public void disableDoor() {
		canDoor = false;
		TimerManager tMan = TimerManager.getInstance();
		tMan.schedule(new Runnable() {
			@Override
			public void run() {
				canDoor = true;
			}
		}, 5000);
	}

	public Map<Integer, MapleSummon> getSummons() {
		return summons;
	}

	public int getChair() {
		return chair;
	}

	public int getItemEffect() {
		return itemEffect;
	}

	public void setChair(int chair) {
		this.chair = chair;
	}

	public void setItemEffect(int itemEffect) {
		this.itemEffect = itemEffect;
	}

	@Override
	public Collection<MapleInventory> allInventories() {
		return Arrays.asList(inventory);
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.PLAYER;
	}

	public int getGuildId() {
		return guildid;
	}

	public int getGuildRank() {
		return guildrank;
	}

	public void setGuildId(int _id) {
		guildid = _id;
		if (guildid > 0) {
			if (mgc == null) {
				mgc = new MapleGuildCharacter(this);
			} else {
				mgc.setGuildId(guildid);
			}
		} else {
			mgc = null;
		}
	}

	public void setGuildRank(int _rank) {
		guildrank = _rank;
		if (mgc != null) {
			mgc.setGuildRank(_rank);
		}
	}

	public MapleGuildCharacter getMGC() {
		return mgc;
	}

	public void guildUpdate() {
		if (this.guildid <= 0) {
			return;
		}
		mgc.setLevel(this.level);
		mgc.setJobId(this.job.getId());

		try {
			this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
		} catch (RemoteException re) {
			log.error("RemoteExcept while trying to update level/job in guild.", re);
		}
	}
	private NumberFormat nf = new DecimalFormat("#,###,###,###");

	public String guildCost() {
		return nf.format(MapleGuild.CREATE_GUILD_COST);
	}

	public String emblemCost() {
		return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
	}

	public String capacityCost() {
		return nf.format(MapleGuild.INCREASE_CAPACITY_COST);
	}

	public void genericGuildMessage(int code) {
		this.client.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
	}

	public void disbandGuild() {
		if (guildid <= 0 || guildrank != 1) {
			log.warn(this.name + " tried to disband and s/he is either not in a guild or not leader.");
			return;
		}

		try {
			client.getChannelServer().getWorldInterface().disbandGuild(this.guildid);
		} catch (Exception e) {
			log.error("Error while disbanding guild.", e);
		}
	}

	public void increaseGuildCapacity() {
		if (this.getMeso() < MapleGuild.INCREASE_CAPACITY_COST) {
			client.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
			return;
		}

		if (this.guildid <= 0) {
			log.info(this.name + " is trying to increase guild capacity without being in the guild.");
			return;
		}

		try {
			client.getChannelServer().getWorldInterface().increaseGuildCapacity(this.guildid);
		} catch (Exception e) {
			log.error("Error while increasing capacity.", e);
			return;
		}

		this.gainMeso(-MapleGuild.INCREASE_CAPACITY_COST, true, false, true);
	}

	public void saveGuildStatus() {
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?");
			ps.setInt(1, this.guildid);
			ps.setInt(2, this.guildrank);
			ps.setInt(3, this.id);
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			log.error("SQL error: " + se.getLocalizedMessage(), se);
		}
	}

	/**
	 * Allows you to change someone's NXCash, Maple Points, and Gift Tokens!
	 * 
	 * Created by Acrylic/Penguins
	 * 
	 * @param type: 0 = NX, 1 = MP, 2 = GT
	 * @param quantity: how much to modify it by. Negatives subtract points, Positives add points. 
	 */
	public void addCSPoints(int type, int quantity) {
		if (type == 1) {
			this.paypalNX += quantity;
		} else if (type == 2) {
			this.maplePoints += quantity;
		} else if (type == 4) {
			this.cardNX += quantity;
		}
	}

        public void modifyCSPoints(int type, int dx) {
            if (type == 1)
                this.paypalNX += dx;
            else if (type == 2)
                this.maplePoints += dx;
            else if (type == 4)
                this.cardNX += dx;
        }
        
	public int getCSPoints(int type) {
		if (type == 0) {
			return this.cardNX;
		} else if (type == 1) {
			return this.maplePoints;
		} else if (type == 2) {
			return this.paypalNX;
		} else {
			return 0;
		}
	}

        public boolean haveItem(int itemid) {
            return haveItem(itemid, 1, false, true);
        }
        
	public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
		MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
		MapleInventory iv = inventory[type.ordinal()];
		int possesed = iv.countById(itemid);
		if (checkEquipped) {
			possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
		}
		if (greaterOrEquals) {
			return possesed >= quantity;
		} else {
			return possesed == quantity;
		}
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
		public long startTime;
		public long length;
		public ScheduledFuture<?> timer;

		public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
			super();
			this.skillId = skillId;
			this.startTime = startTime;
			this.length = length;
			this.timer = timer;
		}
	}

	public static class SkillEntry {
		public int skillevel;
		public int masterlevel;

		public SkillEntry(int skillevel, int masterlevel) {
			this.skillevel = skillevel;
			this.masterlevel = masterlevel;
		}

		@Override
		public String toString() {
			return skillevel + ":" + masterlevel;
		}
	}

	public enum FameStatus {
		OK, NOT_TODAY, NOT_THIS_MONTH
	}

	public int getBuddyCapacity() {
		return buddylist.getCapacity();
	}

	public void setBuddyCapacity(int capacity) {
		buddylist.setCapacity(capacity);
		client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
	}

	public MapleMessenger getMessenger() {
		return messenger;
	}

	public void setMessenger(MapleMessenger messenger) {
		this.messenger = messenger;
	}

	public void checkMessenger() {
		if (messenger != null && messengerposition < 4 && messengerposition > -1) {
			try {
				WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
				MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(client.getPlayer(), messengerposition);
				wci.silentJoinMessenger(messenger.getId(), messengerplayer, messengerposition);
				wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
			} catch (RemoteException e) {
				client.getChannelServer().reconnectWorld();
			}
		}
	}

	public int getMessengerPosition() {
		return messengerposition;
	}

	public void setMessengerPosition(int position) {
		this.messengerposition = position;
	}

	public int hasEXPCard() {
		return 1;
	}

	public boolean getNXCodeValid(String code, boolean validcode) throws SQLException {

		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
		ps.setString(1, code);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			validcode = rs.getInt("valid") == 0 ? false : true;
		}

		rs.close();
		ps.close();

		return validcode;
	}

	public int getNXCodeType(String code) throws SQLException {

		int type = -1;
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT `type` FROM nxcode WHERE code = ?");
		ps.setString(1, code);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			type = rs.getInt("type");
		}

		rs.close();
		ps.close();

		return type;
	}

	public int getNXCodeItem(String code) throws SQLException {

		int item = -1;
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT `item` FROM nxcode WHERE code = ?");
		ps.setString(1, code);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			item = rs.getInt("item");
		}

		rs.close();
		ps.close();

		return item;
	}

	public void setNXCodeUsed(String code) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = ?");
		ps.setString(1, code);
		ps.executeUpdate();
		ps.close();
		ps = con.prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
		ps.setString(1, this.getName());
		ps.setString(2, code);
		ps.executeUpdate();
		ps.close();
	}

	public void setInCS(boolean yesno) {
		this.incs = yesno;
	}

	public boolean inCS() {
		return this.incs;
	}

        public int getEnergy() {
            return energybar;
        }
        
	public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
		if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
			this.coolDowns.remove(skillId);
		}
		this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
	}

	public void removeCooldown(int skillId) {
		if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
			this.coolDowns.remove(Integer.valueOf(skillId));
		}
	}

	public void giveCoolDowns(final List<PlayerCoolDownValueHolder> cooldowns) {
		for (PlayerCoolDownValueHolder cooldown : cooldowns) {
			int time = (int) ((cooldown.length + cooldown.startTime) - System.currentTimeMillis());
			ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, cooldown.skillId), time);
			addCooldown(cooldown.skillId, System.currentTimeMillis(), time, timer);
		}
	}

	public boolean isOnCoolDown(int skillId) {
		for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
			if (mcdvh.skillId == skillId) {
				return true;
			}
		}
		return false;
	}

	public List<PlayerCoolDownValueHolder> getAllCoolDowns() {
		List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
		for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
			ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
		}
		return ret;
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

	public void addDisease(MapleDisease disease) {
		this.diseases.add(disease);
	}

	public List<MapleDisease> getDiseases() {
		return Collections.unmodifiableList(diseases);
	}

	public void removeDisease(MapleDisease disease) {
		if (diseases.contains(disease)) {
			diseases.remove(disease);
		}
	}

	public void removeDiseases() {
		diseases.clear();
	}

	public void giveDebuff(MapleDisease disease, MobSkill skill) {
		List<Pair<MapleDisease, Integer>> disease_ = new ArrayList<Pair<MapleDisease, Integer>>();
		disease_.add(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
		this.diseases.add(disease);
		getClient().getSession().write(MaplePacketCreator.giveDebuff(disease_, skill));
		getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, disease_, skill), false);
	}

	public void dispelDebuffs() {
		List<MapleDisease> toDispel = new ArrayList<MapleDisease>();
		for (MapleDisease disease : diseases) {
			if (disease != MapleDisease.SEDUCE && disease != MapleDisease.SLOW) {
				toDispel.add(disease);
			}
		}
		getClient().getSession().write(MaplePacketCreator.cancelDebuff(toDispel));
		getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, toDispel), false);
		toDispel.clear();
		this.diseases.clear();
	}

	public void dispelAllDebuffs() {
		getClient().getSession().write(MaplePacketCreator.cancelDebuff(diseases));
		getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, diseases), false);
		this.diseases.clear();
	}

	public void setLevel(int level) {
		this.level = level - 1;
	}

	//public boolean canWear(IEquip equip) {
	//	if (equip.)
	//}
	public void sendNote(String to, String msg) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
		ps.setString(1, to);
		ps.setString(2, this.getName());
		ps.setString(3, msg);
		ps.setLong(4, System.currentTimeMillis());
		ps.executeUpdate();
		ps.close();
	}

	public void showNote() throws SQLException {
		Connection con = DatabaseConnection.getConnection();

		PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ps.setString(1, this.getName());
		ResultSet rs = ps.executeQuery();

		rs.last();
		int count = rs.getRow();
		rs.first();

		client.getSession().write(MaplePacketCreator.showNotes(rs, count));
		rs.close();
		ps.close();
	}

	public void deleteNote(int id) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public int getCashSlots() {
		return cashSlots;
	}

	public void setCashSlots(int cashSlots) {
		this.cashSlots = cashSlots;
	}

	public int getEquipSlots() {
		return equipSlots;
	}

	public void setEquipSlots(int equipSlots) {
		this.equipSlots = equipSlots;
	}

	public int getEtcSlots() {
		return etcSlots;
	}

	public void setEtcSlots(int etcSlots) {
		this.etcSlots = etcSlots;
	}

	public int getSetupSlots() {
		return setupSlots;
	}

	public void setSetupSlots(int setupSlots) {
		this.setupSlots = setupSlots;
	}

	public int getUseSlots() {
		return useSlots;
	}

	public void setUseSlots(int useSlots) {
		this.useSlots = useSlots;
	}

	public int getMarkedMonster() {
		return markedMonster;
	}

	public void setMarkedMonster(int markedMonster) {
		this.markedMonster = markedMonster;
	}

	public Byte getHammerSlot() {
		return hammerSlot;
	}

	public void setHammerSlot(Byte hammerSlot) {
		this.hammerSlot = hammerSlot;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int id) {
		this.parentId = id;
	}

	public int getChildId() {
		return childId;
	}

	public void setChildId(int id) {
		this.childId = id;
	}

	public boolean hasEnergyCharge() {
		int skillId = 0;
		if (getJob().isA(MapleJob.MARAUDER)) {
			skillId = Skills.Marauder.EnergyCharge;
		} else if (getJob().isA(MapleJob.THUNDERBREAKER2)) {
			skillId = Skills.ThunderBreaker2.EnergyCharge;
		} else {
			return false;
		}
		ISkill skill = SkillFactory.getSkill(skillId);
		if (getSkillLevel(skill) > 0) {
			return true;
		}
		return false;
	}

	public int getEnergyCharge() {
		return energyChargeLevel;
	}

	public void resetBattleshipHp() {
		ISkill skill = SkillFactory.getSkill(Skills.Corsair.Battleship);
		this.battleshipHp = (4000 * getSkillLevel(skill)) + ((getLevel() - 120) * 2000);
	}

	public int getBattleshipHp() {
		return battleshipHp;
	}

	public void setBattleshipHp(int battleshipHp) {
		this.battleshipHp = battleshipHp;
	}

	public void decreaseBattleshipHp(int decrease) {
		this.battleshipHp -= decrease;
		if (battleshipHp <= 0) {
			this.battleshipHp = 0;
			ISkill battleship = SkillFactory.getSkill(Skills.Corsair.Battleship);
			int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
			getClient().getSession().write(MaplePacketCreator.skillCooldown(Skills.Corsair.Battleship, cooldown));
			ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, Skills.Corsair.Battleship), cooldown * 1000);
			addCooldown(Skills.Corsair.Battleship, System.currentTimeMillis(), cooldown * 1000, timer);
			cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
			resetBattleshipHp();
		}
	}

	public void increaseEnergyCharge(int numMonsters) {
		if (energyChargeLevel < 10000 && numMonsters > 0) {
			if (energyChargeSchedule != null) {
				this.energyChargeSchedule.cancel(false);
				this.energyChargeSchedule = null;
			}
			int skillId = 0;
			if (getJob().isA(MapleJob.MARAUDER)) {
				skillId = Skills.Marauder.EnergyCharge;
			} else if (getJob().isA(MapleJob.THUNDERBREAKER2)) {
				skillId = Skills.ThunderBreaker2.EnergyCharge;
			} else {
				return;
			}
			ISkill skill = SkillFactory.getSkill(skillId);
			int skillLevel = getSkillLevel(skill);
			int x = 0;
			if (skillLevel > 0) {
				x = skill.getEffect(skillLevel).getX();
			}
			int toAdd = x * numMonsters;
			this.energyChargeLevel += toAdd;
			if (energyChargeLevel >= 10000) {
				this.energyChargeLevel = 10000;
				skill.getEffect(skillLevel).applyTo(this);
				return;
			} else {
				List<Pair<MapleBuffStat, Integer>> statups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, energyChargeLevel));
				getClient().getSession().write(MaplePacketCreator.givePirateBuff(0, 0, statups));
				getMap().broadcastMessage(this, MaplePacketCreator.showPirateBuff(id, 0, 0, statups), false);
			}
			this.energyChargeSchedule = TimerManager.getInstance().register(new ReduceEnergyChargeAction(this), 10000, 10000);
		}
	}

	public static class ReduceEnergyChargeAction implements Runnable {
		private WeakReference<MapleCharacter> target;

		public ReduceEnergyChargeAction(MapleCharacter target) {
			this.target = new WeakReference<MapleCharacter>(target);
		}

		@Override
		public void run() {
			MapleCharacter realTarget = target.get();
			realTarget.energyChargeLevel -= 200;
			if (realTarget.energyChargeLevel <= 0) {
				realTarget.energyChargeLevel = 0;
				realTarget.energyChargeSchedule.cancel(false);
			}
		}
	}
}
