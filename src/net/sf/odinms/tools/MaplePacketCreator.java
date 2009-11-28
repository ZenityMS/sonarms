/*
	This file is part of the OdinMS Maple Story Server
	Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
			   Matthias Butz <matze@odinms.de>
			   Jan Christian Meyer <vimes@odinms.de>

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU Affero General Public License version 3
	as published by the Free Software Foundation. You may not use, modify
	or distribute this program under any other version of the
	GNU Affero General Public License.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Affero General Public License for more details.

	You should have received a copy of the GNU Affero General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sf.odinms.tools;

import java.awt.Point;
import java.awt.Rectangle;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.odinms.client.BuddylistEntry;
import net.sf.odinms.client.IEquip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleKeyBinding;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.IEquip.ScrollResult;
import net.sf.odinms.client.MapleDisease;
import net.sf.odinms.client.MapleRing;
import net.sf.odinms.client.SkillMacro;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.ByteArrayMaplePacket;
import net.sf.odinms.net.LongValueHolder;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.SendPacketOpcode;
import net.sf.odinms.net.channel.handler.SummonDamageHandler.SummonAttackEntry;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePlayerShop;
import net.sf.odinms.server.MaplePlayerShopItem;
import net.sf.odinms.server.MapleShopItem;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleReactor;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.tools.data.output.LittleEndianWriter;
import net.sf.odinms.tools.data.output.MaplePacketLittleEndianWriter;
import net.sf.odinms.net.world.guild.*;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.maps.MapleSummon;


/**
 * Provides all MapleStory packets needed in one place.
 * 
 * @author Frz
 * @since Revision 259
 * @version 1.0
 */
public class MaplePacketCreator {
	private static Logger log = LoggerFactory.getLogger(MaplePacketCreator.class);

	private final static byte[] CHAR_INFO_MAGIC = new byte[] { (byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b };
	private final static byte[] ITEM_MAGIC = new byte[] { (byte) 0x80, 5 };
	public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
	private static Random rand = new Random();
	
	/**
	 * Sends a hello packet.
	 * 
	 * @param mapleVersion The maple client version.
	 * @param sendIv the IV used by the server for sending
	 * @param recvIv the IV used by the server for receiving
	 */
	public static MaplePacket getHello(short mapleVersion, byte[] sendIv, byte[] recvIv, boolean testServer) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		mplew.writeShort(0x0d);
		mplew.writeShort(mapleVersion);
		mplew.write(new byte[] { 0, 0 });
		mplew.write(recvIv);
		mplew.write(sendIv);
		mplew.write(testServer ? 5 : 8);
		return mplew.getPacket();
	}

	/**
	 * Sends a ping packet.
	 * 
	 * @return The packet.
	 */
	public static MaplePacket getPing() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		mplew.writeShort(SendPacketOpcode.PING.getValue());
		return mplew.getPacket();
	}

	/**
	 * Gets a login failed packet.
	 * 
	 * Possible values for <code>reason</code>:<br>
	 * 3: ID deleted or blocked<br>
	 * 4: Incorrect password<br>
	 * 5: Not a registered id<br>
	 * 6: System error<br>
	 * 7: Already logged in<br>
	 * 8: System error<br>
	 * 9: System error<br>
	 * 10: Cannot process so many connections<br>
	 * 11: Only users older than 20 can use this channel
	 * 
	 * @param reason The reason logging in failed.
	 * @return The login failed packet.
	 */
	public static MaplePacket getLoginFailed(int reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.writeInt(reason);
		mplew.writeShort(0);

		return mplew.getPacket();
	}

	public static MaplePacket getPermBan(byte reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		// Response.WriteHexString("00 00 02 00 01 01 01 01 01 00");
		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.writeShort(0x02); // Account is banned
		mplew.write(0x0);
		mplew.write(reason);
		mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
		return mplew.getPacket();
	}

	public static MaplePacket getTempBan(long timestampTill, byte reason) {

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.write(0x02);
		mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00")); // Account is banned
		mplew.write(reason);
		mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since
										// 1/1/1601. Lulz.
		return mplew.getPacket();
	}

	/**
	 * Gets a successful authentication and PIN Request packet.
	 * 
	 * @param account The account name.
	 * @return The PIN request packet.
	 */
	public static MaplePacket getAuthSuccessRequestPin(String account, boolean admin) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(0); //user id
        mplew.write(0); //gender (0x0a == gender select, 0x0b == pin select)
        mplew.write(admin ? 1 : 0);
        mplew.write(0); //admin, doesn't let them chat
        mplew.write(0);
        mplew.writeMapleAsciiString(account);
        mplew.write(0);
        mplew.write(0); //isquietbanned
        mplew.writeLong(0);
        mplew.writeLong(0); //creation time
        mplew.writeInt(0);
        
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a PIN operation.
	 * 
	 * Possible values for <code>mode</code>:<br>
	 * 0 - PIN was accepted<br>
	 * 1 - Register a new PIN<br>
	 * 2 - Invalid pin / Reenter<br>
	 * 3 - Connection failed due to system error<br>
	 * 4 - Enter the pin
	 * 
	 * @param mode The mode.
	 */
	public static MaplePacket pinOperation(byte mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendPacketOpcode.PIN_OPERATION.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet requesting the client enter a PIN.
	 * 
	 * @return The request PIN packet.
	 */
	public static MaplePacket requestPin() {
		return pinOperation((byte) 4);
	}

	/**
	 * Gets a packet requesting the PIN after a failed attempt.
	 * 
	 * @return The failed PIN packet.
	 */
	public static MaplePacket requestPinAfterFailure() {
		return pinOperation((byte) 2);
	}

	/**
	 * Gets a packet saying the PIN has been accepted.
	 * 
	 * @return The PIN accepted packet.
	 */
	public static MaplePacket pinAccepted() {
		return pinOperation((byte) 0);
	}

	/**
	 * Gets a packet detailing a server and its channels.
	 * 
	 * @param serverIndex The index of the server to create information about.
	 * @param serverName The name of the server.
	 * @param channelLoad Load of the channel - 1200 seems to be max.
	 * @return The server info packet.
	 */
	public static MaplePacket getServerList(int serverIndex, String serverName, Map<Integer, Integer> channelLoad) {
		/*
		 * 0B 00 00 06 00 53 63 61 6E 69 61 00 00 00 64 00 64 00 00 13 08 00 53 63 61 6E 69 61 2D 31 5E 04 00 00 00 00
		 * 00 08 00 53 63 61 6E 69 61 2D 32 25 01 00 00 00 01 00 08 00 53 63 61 6E 69 61 2D 33 F6 00 00 00 00 02 00 08
		 * 00 53 63 61 6E 69 61 2D 34 BC 00 00 00 00 03 00 08 00 53 63 61 6E 69 61 2D 35 E7 00 00 00 00 04 00 08 00 53
		 * 63 61 6E 69 61 2D 36 BC 00 00 00 00 05 00 08 00 53 63 61 6E 69 61 2D 37 C2 00 00 00 00 06 00 08 00 53 63 61
		 * 6E 69 61 2D 38 BB 00 00 00 00 07 00 08 00 53 63 61 6E 69 61 2D 39 C0 00 00 00 00 08 00 09 00 53 63 61 6E 69
		 * 61 2D 31 30 C3 00 00 00 00 09 00 09 00 53 63 61 6E 69 61 2D 31 31 BB 00 00 00 00 0A 00 09 00 53 63 61 6E 69
		 * 61 2D 31 32 AB 00 00 00 00 0B 00 09 00 53 63 61 6E 69 61 2D 31 33 C7 00 00 00 00 0C 00 09 00 53 63 61 6E 69
		 * 61 2D 31 34 B9 00 00 00 00 0D 00 09 00 53 63 61 6E 69 61 2D 31 35 AE 00 00 00 00 0E 00 09 00 53 63 61 6E 69
		 * 61 2D 31 36 B6 00 00 00 00 0F 00 09 00 53 63 61 6E 69 61 2D 31 37 DB 00 00 00 00 10 00 09 00 53 63 61 6E 69
		 * 61 2D 31 38 C7 00 00 00 00 11 00 09 00 53 63 61 6E 69 61 2D 31 39 EF 00 00 00 00 12 00
		 */

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
		mplew.write(serverIndex);
		mplew.writeMapleAsciiString(serverName);
		mplew.write(3); // 1: E 2: N 3: H

		mplew.writeMapleAsciiString("");
		mplew.write(0x64); // rate modifier, don't ask O.O!
		mplew.write(0x0); // event xp * 2.6 O.O!
		mplew.write(0x64); // rate modifier, don't ask O.O!
		mplew.write(0x0); // drop rate * 2.6
		mplew.write(0x0);
		int lastChannel = 1;
		Set<Integer> channels = channelLoad.keySet();
		for (int i = 30; i > 0; i--) {
			if (channels.contains(i)) {
				lastChannel = i;
				break;
			}
		}
		mplew.write(lastChannel);

		int load;
		for (int i = 1; i <= lastChannel; i++) {
			if (channels.contains(i)) {
				load = channelLoad.get(i);
			} else {
				load = 1200;
			}
			mplew.writeMapleAsciiString(serverName + "-" + i);
			mplew.writeInt(load);
			mplew.write(serverIndex);
			mplew.writeShort(i - 1);
		}
		mplew.writeShort(0); // ver 0.56

		return mplew.getPacket();
	}

	/**
	 * Gets a packet saying that the server list is over.
	 * 
	 * @return The end of server list packet.
	 */
	public static MaplePacket getEndOfServerList() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
		mplew.write(0xFF);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a server status message.
	 * 
	 * Possible values for <code>status</code>:<br>
	 * 0 - Normal<br>
	 * 1 - Highly populated<br>
	 * 2 - Full
	 * 
	 * @param status The server status.
	 * @return The server status packet.
	 */
	public static MaplePacket getServerStatus(int status) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
		mplew.writeShort(status);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the channel server.
	 * 
	 * @param inetAddr The InetAddress of the requested channel server.
	 * @param port The port the channel is on.
	 * @param clientId The ID of the client.
	 * @return The server IP packet.
	 */
	public static MaplePacket getServerIP(InetAddress inetAddr, int port, int clientId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
		mplew.writeShort(0);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		// 0x13 = numchannels?
		mplew.writeInt(clientId); // this gets repeated to the channel server
		// leos.write(new byte[] { (byte) 0x13, (byte) 0x37, 0x42, 1, 0, 0, 0,
		// 0, 0 });

		mplew.write(new byte[] { 0, 0, 0, 0, 0 });
		// 0D 00 00 00 3F FB D9 0D 8A 21 CB A8 13 00 00 00 00 00 00
		// ....?....!.........
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the new channel.
	 * 
	 * @param inetAddr The InetAddress of the requested channel server.
	 * @param port The port the channel is on.
	 * @return The server IP packet.
	 */
	public static MaplePacket getChannelChange(InetAddress inetAddr, int port) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
		mplew.write(1);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		return mplew.getPacket();
	}
	
	/**
	 * Gets a packet with a list of characters.
	 * 
	 * @param c The MapleClient to load characters of.
	 * @param serverId The ID of the server requested.
	 * @return The character list packet.
	 */
	public static MaplePacket getCharList(MapleClient c, int serverId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
		mplew.write(0);
		List<MapleCharacter> chars = c.loadCharacters(serverId);
		mplew.write((byte) chars.size());

		for (MapleCharacter chr : chars) {
			MaplePacketHelper.addCharEntry(mplew, chr);
		}
		
		mplew.writeInt(6);

		return mplew.getPacket();
	}

	/**
	 * Gets character info for a character.
	 * 
	 * @param chr The character to get info about.
	 * @return The character info packet.
	 */
	public static MaplePacket getCharInfo(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue()); // 0x49

		mplew.writeInt(chr.getClient().getChannel() - 1);
		mplew.write(1);
		mplew.write(1);
		mplew.writeShort(0);
		mplew.writeInt(rand.nextInt()); // seed the maplestory rng with a random number <3
		mplew.writeInt(rand.nextInt()); // seed the maplestory rng with a random number <3
		mplew.writeInt(rand.nextInt()); // seed the maplestory rng with a random number <3
		
		mplew.writeLong(-1);
		MaplePacketHelper.addCharStats(mplew, chr);
		mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
		mplew.writeInt(chr.getMeso()); // mesos
		MaplePacketHelper.addInventoryInfo(mplew, chr);
		MaplePacketHelper.addSkillInfo(mplew, chr);
		MaplePacketHelper.addQuestInfo(mplew, chr);
		
		/*List<MapleRing> rings = new ArrayList<MapleRing>();
		
		for (Item item : equipped) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		iv = chr.getInventory(MapleInventoryType.EQUIP);
		for (IItem item : iv.list()) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		
		Collections.sort(rings);
		boolean FR_last = false;
		for (MapleRing ring : rings) {
			if (ring.getItemId() >= 1112800 && ring.getItemId() <= 1112810 && rings.indexOf(ring) == 0) {
				mplew.writeShort(0);
			}
			mplew.writeShort(0);
			mplew.writeShort(1);
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(ring.getPartnerName(), '\0', 13));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			if (ring.getItemId() >= 1112800 && ring.getItemId() <= 1112810) {
				FR_last = true;
				mplew.writeInt(0);
				mplew.writeInt(ring.getItemId());
				mplew.writeShort(0);
			} else {
				if (rings.size() > 1) {
					mplew.writeShort(0);
				}
				FR_last = false;
			}
		}
		
		if (!FR_last) {*/
			mplew.writeLong(0);
		//}
			
		// 00 00
		// 01 00
		// 77 6B 2E 00
		// 67 66 64 67 68 67 68 66 67 00 00 00 BC
		// 3C 13 32 00
		// 00 00 00 00
		// 3B 13 32 00
		// 00 00 00 00
		// E0 FA 10 00
		// 00 00
		// FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 A0 52 03 F5 98 0B C9 01
		for (int x = 0; x < 15; x++) {
		    //TeleportMaps(5)
		    //VIPTeleportMaps(10)
		    //NoMap->999999999->CHAR_INFO_MAGIC
			mplew.write(CHAR_INFO_MAGIC);
		}
		mplew.writeInt(0);
		MaplePacketHelper.addMonsterBookInfo(mplew, chr);
		mplew.write(0);
		
		// Party Quest data (quest needs to be added in the quests list)
		mplew.writeShort(0); //number of quests
		mplew.writeShort(0); //questid, string
		//thx vana (Diamondo)
		
        mplew.writeLong(MaplePacketHelper.getTime((long) System.currentTimeMillis()));

		return mplew.getPacket();
	}

	/**
	 * Gets an empty stat update.
	 * 
	 * @return The empy stat update packet.
	 */
	public static MaplePacket enableActions() {
		return updatePlayerStats(EMPTY_STATUPDATE, true);
	}

	/**
	 * Gets an update for specified stats.
	 * 
	 * @param stats The stats to update.
	 * @return The stat update packet.
	 */
	public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats) {
		return updatePlayerStats(stats, false);
	}
	
	/**
	 * Gets an update for specified stats.
	 * 
	 * @param stats The list of stats to update.
	 * @param itemReaction Result of an item reaction(?)
	 * @param pet Result of spawning a pet(?)
	 * @return The stat update packet.
	 */
	public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
		if (itemReaction) {
			mplew.write(1);
		} else {
			mplew.write(0);
		}
		int updateMask = 0;
		for (Pair<MapleStat, Integer> statupdate : stats) {
			updateMask |= statupdate.getLeft().getValue();
		}
		List<Pair<MapleStat, Integer>> mystats = stats;
		if (mystats.size() > 1) {
			Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

				@Override
				public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
					int val1 = o1.getLeft().getValue();
					int val2 = o2.getLeft().getValue();
					return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
				}
			});
		}
		mplew.writeInt(updateMask);
		for (Pair<MapleStat, Integer> statupdate : mystats) {
			if (statupdate.getLeft().getValue() >= 1) {
				if (statupdate.getLeft().getValue() == 0x1) {
					mplew.writeShort(statupdate.getRight().shortValue());
				} else if (statupdate.getLeft().getValue() <= 0x4) {
					mplew.writeInt(statupdate.getRight());
				} else if (statupdate.getLeft().getValue() < 0x20) {
					mplew.write(statupdate.getRight().shortValue());
				} else if (statupdate.getLeft().getValue() < 0xFFFF) {
					mplew.writeShort(statupdate.getRight().shortValue());
				} else {
					mplew.writeInt(statupdate.getRight().intValue());
				}
			}
		}

		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to change maps.
	 * 
	 * @param to The <code>MapleMap</code> to warp to.
	 * @param spawnPoint The spawn portal number to spawn at.
	 * @param chr The character warping to <code>to</code>
	 * @return The map change packet.
	 */
    public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    	mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
    	mplew.writeInt(chr.getClient().getChannel() - 1);
    	mplew.writeShort(0x2); // Count
    	mplew.writeShort(0);
    	mplew.writeInt(to.getId());
    	mplew.write(spawnPoint);
    	mplew.writeShort(chr.getHp());
    	mplew.write(0);
    	mplew.writeLong(MaplePacketHelper.getTime((long) System.currentTimeMillis()));

    	return mplew.getPacket();
    }
	/*public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue()); // 0x49

		mplew.writeInt(chr.getClient().getChannel() - 1);
		mplew.writeShort(0x2);
		mplew.writeShort(0);
		mplew.writeInt(to.getId());
		mplew.write(spawnPoint);
		mplew.writeShort(chr.getHp()); // hp (???)

		mplew.write(0);
		long questMask = 0x1ffffffffffffffL;
		mplew.writeLong(questMask);

		return mplew.getPacket();
	}*/

	/**
	 * Gets a packet to spawn a portal.
	 * 
	 * @param townId The ID of the town the portal goes to.
	 * @param targetId The ID of the target.
	 * @param pos Where to put the portal.
	 * @return The portal spawn packet.
	 */
	public static MaplePacket spawnPortal(int townId, int targetId, Point pos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		if (pos != null) {
			mplew.writeShort(pos.x);
			mplew.writeShort(pos.y);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a door.
	 * 
	 * @param oid The door's object ID.
	 * @param pos The position of the door.
	 * @param town
	 * @return The remove door packet.
	 */
	public static MaplePacket spawnDoor(int oid, Point pos, boolean town) {
		// [D3 00] [01] [93 AC 00 00] [6B 05] [37 03]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());

		mplew.write(town ? 1 : 0);
		mplew.writeInt(oid);
		mplew.writeShort(pos.x);
		mplew.writeShort(pos.y);
		
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a door.
	 * 
	 * @param oid The door's ID.
	 * @param town
	 * @return The remove door packet.
	 */
	public static MaplePacket removeDoor(int oid, boolean town) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		if (town) {
			mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
			mplew.writeInt(999999999);
			mplew.writeInt(999999999);
		} else {
			mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
			mplew.write(/*town ? 1 : */0);
			mplew.writeInt(oid);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a special map object.
	 * 
	 * @param chr The MapleCharacter who spawned the object.
	 * @param skill The skill used.
	 * @param skillLevel The level of the skill used.
	 * @param pos Where the object was spawned.
	 * @param movementType Movement type of the object.
	 * @param animated Animated spawn?
	 * @return The spawn packet for the map object.
	 */
	public static MaplePacket spawnSpecialMapObject(MapleSummon summon, int skillLevel, boolean animated) {
		// 72 00 29 1D 02 00 FD FE 30 00 19 7D FF BA 00 04 01 00 03 01 00
		// 85 00 [6A 4D 27 00] [35 1F 00 00] [2D 5D 20 00] [0C] [8C 16] [CA 01] [03] [00] [00] [01] [01] [00]

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());

		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId()); // Supposed to be Object ID, but this works too! <3
		mplew.writeInt(summon.getSkill());
		mplew.write(skillLevel);
		mplew.writeShort(summon.getPosition().x);
		mplew.writeShort(summon.getPosition().y);
		mplew.write(3); // test
		mplew.write(0); // test
		mplew.write(0); // test

		mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow
												// (4th mage summons?), 2/4 =
												// only tele follow, 3 = bird
												// follow

		mplew.write(1); // 0 and the summon can't attack - but puppets don't
						// attack with 1 either ^.-
		
		mplew.write(animated ? 0 : 1);

		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a special map object.
	 * 
	 * @param chr The MapleCharacter who removed the object.
	 * @param skill The skill used to create the object.
	 * @param animated Animated removal?
	 * @return The packet removing the object.
	 */
	public static MaplePacket removeSpecialMapObject(MapleSummon summon, boolean animated) {
		// [86 00] [6A 4D 27 00] 33 1F 00 00 02
		// 92 00 36 1F 00 00 0F 65 85 01 84 02 06 46 28 00 06 81 02 01 D9 00 BD FB D9 00 BD FB 38 04 2F 21 00 00 10 C1 2A 00 06 00 06 01 00 01 BD FB FC 00 BD FB 6A 04 88 1D 00 00 7D 01 AF FB
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());

		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());

		mplew.write(animated ? 4 : 1); // ?

		return mplew.getPacket();
	}

	/**
	 * Gets the response to a relog request.
	 * 
	 * @return The relog response packet.
	 */
	public static MaplePacket getRelogResponse() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
		mplew.write(1);
		return mplew.getPacket();
	}

	/**
	 * Gets a server message packet.
	 * 
	 * @param message The message to convey.
	 * @return The server message packet.
	 */
	public static MaplePacket serverMessage(String message) {
		return serverMessage(4, 0, message, true);
	}

	/**
	 * Gets a server notice packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Light blue background and lolwhut<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type The type of the notice.
	 * @param message The message to convey.
	 * @return The server notice packet.
	 */
	public static MaplePacket serverNotice(int type, String message) {
		return serverMessage(type, 0, message, false);
	}

	/**
	 * Gets a server notice packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Light blue background and lolwhut<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type The type of the notice.
	 * @param channel The channel this notice was sent on.
	 * @param message The message to convey.
	 * @return The server notice packet.
	 */
	public static MaplePacket serverNotice(int type, int channel, String message) {
		return serverMessage(type, channel, message, false);
	}

	/**
	 * Gets a server message packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Light blue background and lolwhut<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type The type of the notice.
	 * @param channel The channel this notice was sent on.
	 * @param message The message to convey.
	 * @param servermessage Is this a scrolling ticker?
	 * @return The server notice packet.
	 */
	private static MaplePacket serverMessage(int type, int channel, String message, boolean servermessage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());

		mplew.write(type);
		if (servermessage) {
			mplew.write(1);
		}
		mplew.writeMapleAsciiString(message);

		if (type == 3) {
			mplew.write(channel - 1); // channel
			mplew.write(0); // 0 = graues ohr, 1 = lulz?
		}
		else if (type == 6)
			mplew.writeInt(0);

		return mplew.getPacket();
	}
	
	public static MaplePacket getItemMegaphone(String message, IItem item, int channel, boolean showEar) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		//41 00 
		//08 
		//25 00 
		//4A 30 62 70 6C 7A 20 3A 20 50 41 43 4B 45 54 20 53 4E 49 46 46 49 4E 20 34 20 53 56 52 20 53 4F 20 50 52 41 4F 
		//0C 
		//01 
		//01 01 
		//25 53 14 00 00 00 80 05 BB 46 E6 17 02 07 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 4A 00 00 50 00 00 24 00 40 E0 FD 3B 37 4F 01 
		//FF FF FF FF
		mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x08);
		mplew.writeMapleAsciiString(message);
		mplew.write(channel - 1);
		mplew.write(showEar ? 1 : 0); //ear
		if (item != null)
			MaplePacketHelper.addItemInfo(mplew, item);
		else
			mplew.write(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket getMultiMegaphone(String[] messages, int channel, boolean showEar) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		//41 00 
		//0A 
		//45 00 
		//4A 30 62 70 6C 7A 20 3A 20 49 20 54 48 49 4E 4B 20 4E 45 58 4F 4E 20 49 53 20 45 48 20 50 52 45 54 54 59 20 43 4F 4F 4C 20 47 55 59 2C 20 45 4E 43 52 59 50 54 53 20 42 41 44 20 41 4E 44 20 44 4F 45 53 4E 54 
		//03 
		//42 00 
		//4A 30 62 70 6C 7A 20 3A 20 41 46 52 41 49 44 20 4F 46 20 41 4E 59 54 48 49 4E 2C 20 4F 48 20 47 4F 44 20 48 4F 57 20 44 49 44 20 49 20 47 45 54 20 48 45 52 45 20 49 4D 20 4E 4F 54 20 56 45 52 59 20 
		//45 00 
		//4A 30 62 70 6C 7A 20 3A 20 47 4F 4F 44 20 57 49 54 48 20 43 4F 4D 50 55 54 45 52 2C 20 53 4E 49 46 46 20 53 4E 49 46 46 20 59 55 4D 4D 20 59 55 4D 4D 20 50 41 43 4B 45 54 53 20 
		//4D 4D 4D 4D 4D 4D 4D 4D 4D 4D 
		//0C 
		//01
		
		mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x0A);
		if (messages[0] != null)
			mplew.writeMapleAsciiString(messages[0]);
		mplew.write(messages.length);
		for (int i = 1; i < messages.length; i++)
			if (messages[i] != null)
				mplew.writeMapleAsciiString(messages[i]);
		for (int i = 0; i < 10; i++)
			mplew.write(channel - 1); //appears to be the channel... I have no idea.
		mplew.write(showEar ? 1 : 0); //ear
		mplew.write(1); //?
		
		return mplew.getPacket();
	}
	
	public static MaplePacket getGachaponItemEarned(String name, String town, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x0B);
		mplew.writeMapleAsciiString(name + " : got a(n)");
		mplew.writeInt(4); //?
		mplew.writeMapleAsciiString(town);
		MaplePacketHelper.addItemInfo(mplew, item, true, true); //should be right.
		
		return mplew.getPacket();
	}
	
	/**
	 * Gets an avatar megaphone packet.
	 * 
	 * @param chr The character using the avatar megaphone.
	 * @param channel The channel the character is on.
	 * @param medal The name of the medal the player has equipped.
	 * @param itemId The ID of the avatar-mega.
	 * @param message The message that is sent.
	 * @return The avatar mega packet.
	 */
	public static MaplePacket getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
		mplew.writeInt(itemId);
		mplew.writeMapleAsciiString(chr.getMedal() + chr.getName());
		for (String s : message) {
			mplew.writeMapleAsciiString(s);
		}
		mplew.writeInt(channel - 1); // channel

		mplew.write(0);
		MaplePacketHelper.addCharLook(mplew, chr, true);

		return mplew.getPacket();
	}

	/**
	 * Gets a NPC spawn packet.
	 * 
	 * @param life The NPC to spawn.
	 * @param requestController Does the NPC want a controller?
	 * @return The NPC spawn packet.
	 */
	public static MaplePacket spawnNPC(MapleNPC life, boolean requestController) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (requestController) {
			mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
			mplew.write(1); // ?

		} else {
			mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
		}
		mplew.writeInt(life.getObjectId());
		mplew.writeInt(life.getId());
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		mplew.write(1); //Facing Left
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getRx0());
		mplew.writeShort(life.getRx1());
		
		mplew.write(1);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket removeNPC(int objid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
		mplew.writeInt(objid);
		
		return mplew.getPacket();
	}

	/**
	 * Gets a spawn monster packet.
	 * 
	 * @param life The monster to spawn.
	 * @param newSpawn Is it a new spawn?
	 * @return The spawn monster packet.
	 */
	public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn) {
		return spawnMonsterInternal(life, false, newSpawn, false, 0);
	}

	/**
	 * Gets a spawn monster packet.
	 * 
	 * @param life The monster to spawn.
	 * @param newSpawn Is it a new spawn?
	 * @param effect The spawn effect.
	 * @return The spawn monster packet.
	 */
	public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
		return spawnMonsterInternal(life, false, newSpawn, false, effect);
	}

	/**
	 * Gets a control monster packet.
	 * 
	 * @param life The monster to give control to.
	 * @param newSpawn Is it a new spawn?
	 * @param aggro Aggressive monster?
	 * @return The monster control packet.
	 */
	public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
		return spawnMonsterInternal(life, true, newSpawn, aggro, 0);
	}

	/**
	 * Internal function to handler monster spawning and controlling.
	 * 
	 * @param life The mob to perform operations with.
	 * @param requestController Requesting control of mob?
	 * @param newSpawn New spawn (fade in?)
	 * @param aggro Aggressive mob?
	 * @param effect The spawn effect to use.
	 * @return The spawn/control packet.
	 */
	private static MaplePacket spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn,
			boolean aggro, int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// 95 00 DA 33 37 00 01 58 CC 6C 00 00 00 00 00 B7 FF F3 FB 02 1A 00 1A
		// 00 02 0E 06 00 00 FF
		// OP OBJID MOBID NULL PX PY ST 00 00 FH
		// 95 00 7A 00 00 00 01 58 CC 6C 00 00 00 00 00 56 FF 3D FA 05 00 00 00
		// 00 FE FF
		
		// [AE 00] [1D 43 3C 00] [01] [21 B3 81 00] [00 00 00 08] [00 00 00 00] [63 FE] [7E FE] [02] [14 00] [14 00] [FD] [B3 42 3C 00] [FF] [00 00 00 00]
		// [AE 00] [EA 77 2A 00] [01] [DB 70 8F 00] [00 00 00 08] [00 00 00 00] [04 01] [91 FA] [02] [81 00] [8A 00] [FF FF] [00 00 00 00]
		
		// [B0 00] [01] [1D 43 3C 00] [01] [21 B3 81 00] [00 00 00 08] [00 00 00 00] [63 FE] [7E FE] [02] [14 00] [14 00] [FF FF] [00 00 00 00]
		
		if (requestController) {
			mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
			// mplew.writeShort(0xA0); // 47 9e
			if (aggro) {
				mplew.write(2);
			} else {
				mplew.write(1);
			}
		} else {
			mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
		}
		mplew.writeInt(life.getObjectId());
		mplew.write(5); // ????!? either 5 or 1?
		mplew.writeInt(life.getId());
		mplew.writeInt(0);
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getPosition().y);
		mplew.write(life.getStance()); // or 5? o.O"
		mplew.writeShort(0);
		mplew.writeShort(life.getFh());
		if (effect > 0) {
			mplew.write(effect);
			mplew.write(0); 
			mplew.writeShort(0);
			if (effect == 15) //Might be for all effects, but for now it'll just be like this.
				mplew.write(0); //this delays the spawn.
		}
		
		if (newSpawn) {
			mplew.writeShort(-2);
		} else {
			mplew.writeShort(-1);
		}
		
		mplew.writeInt(0);
		
		//D0 00 
		//C3 35 A5 01 
		//01 
		//DD E8 8D 00 
		//00 00 00 08 
		//00 00 00 00 
		//8C 00 00 00 
		//02 06 00 06 00 
		//0F 
		//00 
		//00 00 
		//00 
		//FF 00 00 00 00
		
		return mplew.getPacket();
	}

	/**
	 * Handles monsters not being targettable, such as Zakum's first body.
	 * @param life The mob to spawn as non-targettable.
	 * @param effect The effect to show when spawning.
	 * @return The packet to spawn the mob as non-targettable.
	 */
	public static MaplePacket spawnFakeMonster(MapleMonster life, int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.write(5);
		mplew.writeInt(life.getId());
		mplew.writeInt(0);
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getPosition().y);
		mplew.write(life.getStance());
		mplew.writeShort(life.getStartFh());
		mplew.writeShort(life.getFh());
		
		if (effect > 0) {
			mplew.write(effect);
			mplew.write(0);
			mplew.writeShort(0);
		}
		
		mplew.writeShort(-2);
		
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}

	/**
	 * Makes a monster previously spawned as non-targettable, targettable.
	 * @param life The mob to make targettable.
	 * @return The packet to make the mob targettable.
	 */
	public static MaplePacket makeMonsterReal(MapleMonster life) {													
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());

		mplew.writeInt(life.getObjectId());
		mplew.write(5);
		mplew.writeInt(life.getId());
		mplew.writeInt(0);
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getPosition().y);
		mplew.write(life.getStance());
		mplew.writeShort(life.getStartFh());
		mplew.writeShort(life.getFh());
		mplew.writeShort(-1);
		
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}

	/**
	 * Gets a stop control monster packet.
	 * 
	 * @param oid The ObjectID of the monster to stop controlling.
	 * @return The stop control monster packet.
	 */
	public static MaplePacket stopControllingMonster(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(0);
		mplew.writeInt(oid);

		return mplew.getPacket();
	}
	/**
	 * Gets a response to a move monster packet.
	 * 
	 * @param objectid The ObjectID of the monster being moved.
	 * @param moveid The movement ID.
	 * @param currentMp The current MP of the monster.
	 * @param useSkills Can the monster use skills?
	 * @return The move response packet.
	 */
	public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
		return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
	}

	/**
	 * Gets a response to a move monster packet.
	 * 
	 * @param objectid The ObjectID of the monster being moved.
	 * @param moveid The movement ID.
	 * @param currentMp The current MP of the monster.
	 * @param useSkills Can the monster use skills?
	 * @param skillId The skill ID for the monster to use.
	 * @param skillLevel The level of the skill to use.
	 * @return The move response packet.
	 */
	public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
		// A1 00 18 DC 41 00 01 00 00 1E 00 00 00
		// A1 00 22 22 22 22 01 00 00 00 00 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
		mplew.writeInt(objectid);
		mplew.writeShort(moveid);
		mplew.write(useSkills ? 1 : 0);
		mplew.writeShort(currentMp);
		mplew.write(skillId);
		mplew.write(skillLevel);

		return mplew.getPacket();
	}

	/**
	 * Gets a general chat packet.
	 * 
	 * @param cidfrom The character ID who sent the chat.
	 * @param text The text of the chat.
	 * @return The general chat packet.
	 */
	public static MaplePacket getChatText(int cidfrom, String text, boolean whiteBG, int show) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
		mplew.writeInt(cidfrom);
		mplew.write(whiteBG ? 1 : 0);
		mplew.writeMapleAsciiString(text);
		mplew.write(show);

		return mplew.getPacket();
	}

	/**
	 * For testing only! Gets a packet from a hexadecimal string.
	 * 
	 * @param hex The hexadecimal packet to create.
	 * @return The MaplePacket representing the hex string.
	 */
	public static MaplePacket getPacketFromHexString(String hex) {
		byte[] b = HexTool.getByteArrayFromHexString(hex);
		return new ByteArrayMaplePacket(b);
	}

	/**
	 * Gets a packet telling the client to show an EXP increase.
	 * 
	 * @param gain The amount of EXP gained.
	 * @param inChat In the chat box?
	 * @param white White text or yellow?
	 * @return The exp gained packet.
	 */
	public static MaplePacket getShowExpGain(int gain, boolean inChat, boolean white) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
		mplew.write(white ? 1 : 0);
		mplew.writeInt(gain);
		mplew.writeInt(inChat ? 1 : 0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		if (inChat)
			mplew.write(0);
			
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show a meso gain.
	 * 
	 * @param gain How many mesos gained.
	 * @return The meso gain packet.
	 */
	public static MaplePacket getShowMesoGain(int gain) {
		return getShowMesoGain(gain, false);
	}

	/**
	 * Gets a packet telling the client to show a meso gain.
	 * 
	 * @param gain How many mesos gained.
	 * @param inChat Show in the chat window?
	 * @return The meso gain packet.
	 */
	public static MaplePacket getShowMesoGain(int gain, boolean inChat) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		if (!inChat) {
			mplew.write(0);
			mplew.write(1);
		} else {
			mplew.write(5);
		}
		mplew.writeInt(gain);
		mplew.writeShort(0); // inet cafe meso gain ?.o

		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show a item gain.
	 * 
	 * @param itemId The ID of the item gained.
	 * @param quantity How many items gained.
	 * @return The item gain packet.
	 */
	public static MaplePacket getShowItemGain(int itemId, short quantity) {
		return getShowItemGain(itemId, quantity, false);
	}

	/**
	 * Gets a packet telling the client to show an item gain.
	 * 
	 * @param itemId The ID of the item gained.
	 * @param quantity The number of items gained.
	 * @param inChat Show in the chat window?
	 * @return The item gain packet.
	 */
	public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		if (inChat) {
			mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
			mplew.write(3);
			mplew.write(1);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
		} else {
			mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
			mplew.writeShort(0);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client that a monster was killed.
	 * 
	 * @param oid The objectID of the killed monster.
	 * @param animation Show killed animation?
	 * @return The kill monster packet.
	 */
	public static MaplePacket killMonster(int oid, boolean animation) {
		return killMonster(oid, animation ? 1 : 0);
	}
	
	public static MaplePacket killMonster(int oid, int animation) {
		// 9D 00 45 2B 67 00 01
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(animation);//Not a boolean, really an int type

		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show mesos coming out of a map
	 * object.
	 * 
	 * @param amount The amount of mesos.
	 * @param itemoid The ObjectID of the dropped mesos.
	 * @param dropperoid The OID of the dropper.
	 * @param ownerid The ID of the drop owner.
	 * @param dropfrom Where to drop from.
	 * @param dropto Where the drop lands.
	 * @param mod ?
	 * @return The drop mesos packet.
	 */
	public static MaplePacket dropMesoFromMapObject(int amount, int itemoid, int dropperoid, int ownerid,
													Point dropfrom, Point dropto, byte mod) {
		return dropItemFromMapObjectInternal(amount, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, true);
	}

	/**
	 * Gets a packet telling the client to show an item coming out of a map
	 * object.
	 * 
	 * @param itemid The ID of the dropped item.
	 * @param itemoid The ObjectID of the dropped item.
	 * @param dropperoid The OID of the dropper.
	 * @param ownerid The ID of the drop owner.
	 * @param dropfrom Where to drop from.
	 * @param dropto Where the drop lands.
	 * @param mod ?
	 * @return The drop mesos packet.
	 */
	public static MaplePacket dropItemFromMapObject(int itemid, int itemoid, int dropperoid, int ownerid,
													Point dropfrom, Point dropto, byte mod) {
		return dropItemFromMapObjectInternal(itemid, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, false);
	}

	/**
	 * Internal function to get a packet to tell the client to drop an item onto
	 * the map.
	 * 
	 * @param itemid The ID of the item to drop.
	 * @param itemoid The ObjectID of the dropped item.
	 * @param dropperoid The OID of the dropper.
	 * @param ownerid The ID of the drop owner.
	 * @param dropfrom Where to drop from.
	 * @param dropto Where the drop lands.
	 * @param mod ?
	 * @param mesos Is the drop mesos?
	 * @return The item drop packet.
	 */
	public static MaplePacket dropItemFromMapObjectInternal(int itemid, int itemoid, int dropperoid, int ownerid,
								Point dropfrom, Point dropto, byte mod, boolean mesos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// dropping mesos
		// BF 00 01 01 00 00 00 01 0A 00 00 00 24 46 32 00 00 84 FF 70 00 00 00
		// 00 00 84 FF 70 00 00 00 00
		// dropping maple stars
		// BF 00 00 02 00 00 00 00 FB 95 1F 00 24 46 32 00 00 84 FF 70 00 00 00
		// 00 00 84 FF 70 00 00 00 00 80 05 BB 46 E6 17 02 00
		// killing monster (0F 2C 67 00)
		// BF 00 01 2C 03 00 00 00 6D 09 3D 00 24 46 32 00 00 A3 02 6C FF 0F 2C
		// 67 00 A3 02 94 FF 89 01 00 80 05 BB 46 E6 17 02 01

		// 4000109
		mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		// mplew.write(1); // 1 with animation, 2 without o.o
		mplew.write(mod);
		mplew.writeInt(itemoid);
		mplew.write(mesos ? 1 : 0); // 1 = mesos, 0 =item

		mplew.writeInt(itemid);
		mplew.writeInt(ownerid); // owner charid

		mplew.write(0);
		mplew.writeShort(dropto.x);
		mplew.writeShort(dropto.y);
		if (mod != 2) {
			mplew.writeInt(ownerid);
			mplew.writeShort(dropfrom.x);
			mplew.writeShort(dropfrom.y);
		} else {
			mplew.writeInt(dropperoid);
		}
		mplew.write(0);
		if (mod != 2) {
			mplew.writeShort(0);
		}
		if (!mesos) {
			mplew.write(ITEM_MAGIC);
			// TODO getTheExpirationTimeFromSomewhere o.o
			MaplePacketHelper.addExpirationTime(mplew, System.currentTimeMillis(), false);
			// mplew.write(1);
			mplew.write(0);
		}

		return mplew.getPacket();
	}

	/* (non-javadoc)
	 * TODO: make MapleCharacter a mapobject, remove the need for passing oid
	 * here.
	 */
	/**
	 * Gets a packet spawning a player as a mapobject to other clients.
	 * 
	 * @param chr The character to spawn to other clients.
	 * @return The spawn player packet.
	 */
	public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
		// 62 00 24 46 32 00 05 00 42 65 79 61 6E 00 00 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 00 00 20 4E 00 00 00 44 75 00 00 01 2A 4A 0F 00 04
		// 60 BF 0F 00 05 A2 05 10 00 07 2B 5C 10 00 09 E7 D0 10 00 0B 39 53 14
		// 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		// DE 01 73 FF 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00
		// 00 00 00 00
		
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
		mplew.writeInt(chr.getId());
		mplew.writeMapleAsciiString(chr.getName());
		
		if (chr.getGuildId() <= 0)
		{
			mplew.writeMapleAsciiString("");
			mplew.write(new byte[6]);
		}
		else
		{
			MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(
					chr.getGuildId());
			
			if (gs != null)
			{
				mplew.writeMapleAsciiString(gs.getName());
				mplew.writeShort(gs.getLogoBG());
				mplew.write(gs.getLogoBGColor());
				mplew.writeShort(gs.getLogo());
				mplew.write(gs.getLogoColor());
			}
			
			else
			{
				mplew.writeMapleAsciiString("");
				mplew.write(new byte[6]);
			}
		}
        mplew.writeInt(0); //Not sure anymore this should be an int.
        mplew.write(0xf8);
        mplew.write(3);
        mplew.writeShort(0);
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null)
            mplew.writeInt(2);
        else
            mplew.writeInt(0);
        long buffmask = 0;
        Integer buffvalue = null;
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden())
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null)
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null)
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null)
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
        mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffvalue != null)
            if (chr.getBuffedValue(MapleBuffStat.MORPH) != null)
                mplew.writeShort(buffvalue);
            else
                mplew.write(buffvalue.byteValue());
        mplew.writeInt((int) (buffmask & 0xffffffffL));
        int CHAR_MAGIC_SPAWN = new Random().nextInt();
		mplew.writeInt(0);
		mplew.writeShort(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0); //v74
		mplew.write(0); //v74
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);//v74
		mplew.writeShort(0);
		mplew.write(0); //v74
		mplew.writeInt(0);
		mplew.writeInt(0);
		
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.write(0);
		
		IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
		if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && mount != null) {
			mplew.writeInt(mount.getItemId());
			mplew.writeInt(1004);
		} else {
			mplew.writeLong(0);
		}
		
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.write(0);
		mplew.writeShort(412); //chr.getJob().getId());
		
		MaplePacketHelper.addCharLook(mplew, chr, false);
		mplew.writeInt(0);
		mplew.writeInt(chr.getItemEffect());
		mplew.writeInt(chr.getChair());
		mplew.writeShort(chr.getPosition().x);
		mplew.writeShort(chr.getPosition().y);
		mplew.write(chr.getStance());
		mplew.writeInt(0);
		mplew.writeInt(1);
		
		mplew.writeLong(0);
		mplew.writeShort(0);
		
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> equippedC = iv.list();
		List<Item> equipped = new ArrayList<Item>(equippedC.size());
		for (IItem item : equippedC) {
			equipped.add((Item) item);
		}
		Collections.sort(equipped);
		List<MapleRing> rings = new ArrayList<MapleRing>();
		for (Item item : equipped) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		
		Collections.sort(rings);
		// [01] [07 6C 31 00] [00 00 00 00] [08 6C 31 00] [00 00 00 00] [C1 F7 10 00] [01] [3B 13 32 00] [00 00 00 00] [3C 13 32 00] [00 00 00 00] [E0 FA 10 00] 00 00
		if (rings.size() > 0) {
			mplew.write(0);
			for (MapleRing ring : rings) {
				mplew.write(1);
				mplew.writeInt(ring.getRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getPartnerRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getItemId());
			}
			mplew.writeShort(0);
		} else {
			mplew.writeInt(0);
		}
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeShort(0);
		
		log.info(mplew.toString());

		return mplew.getPacket();
	}

	/**
	 * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWriter to add an announcement box
	 *            to.
	 * @param shop The shop to announce.
	 */
	private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop) {
		// 00: no game
		// 01: omok game
		// 02: card game
		// 04: shop
		mplew.write(4);
		mplew.writeInt(shop.getObjectId()); // gameid/shopid

		mplew.writeMapleAsciiString(shop.getDescription()); // desc
		// 00: public
		// 01: private

		mplew.write(0);
		// 00: red 4x3
		// 01: green 5x4
		// 02: blue 6x5
		// omok:
		// 00: normal
		mplew.write(0);
		// first slot: 1/2/3/4
		// second slot: 1/2/3/4
		mplew.write(1);
		mplew.write(4);
		// 0: open
		// 1: in progress
		mplew.write(0);
	}

	public static MaplePacket facialExpression(MapleCharacter from, int expression) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());

		mplew.writeInt(from.getId());
		mplew.writeInt(expression);
		
		return mplew.getPacket();
	}

	private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
		lew.write(moves.size());
		for (LifeMovementFragment move : moves) {
			move.serialize(lew);
		}
	}

	public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
		mplew.writeInt(cid);

		mplew.writeInt(0);

		serializeMovementList(mplew, moves);
		
		return mplew.getPacket();
	}

	public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.writeShort(startPos.x);
		mplew.writeShort(startPos.y);

		serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static MaplePacket moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int oid, Point startPos,
											List<LifeMovementFragment> moves) {
		/*
		 * A0 00 C8 00 00 00 00 FF 00 00 00 00 48 02 7D FE 02 00 1C 02 7D FE 9C FF 00 00 2A 00 03 BD 01 00 DC 01 7D FE
		 * 9C FF 00 00 2B 00 03 7B 02
		 */
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
		// mplew.writeShort(0xA2); // 47 a0
		mplew.writeInt(oid);
		mplew.write(useskill);
		mplew.write(skill);
		mplew.write(skill_1);
		mplew.write(skill_2);
		mplew.write(skill_3);
		mplew.write(0);
		mplew.writeShort(startPos.x);
		mplew.writeShort(startPos.y);

		serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static MaplePacket summonAttack(int cid, int summonSkillId, int newStance, List<SummonAttackEntry> allDamage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(newStance);
		mplew.write(allDamage.size());
		for (SummonAttackEntry attackEntry : allDamage) {
			mplew.writeInt(attackEntry.getMonsterOid()); // oid

			mplew.write(6); // who knows

			mplew.writeInt(attackEntry.getDamage()); // damage

		}

		return mplew.getPacket();
	}

	public static MaplePacket closeRangeAttack(int cid, int skill, int stance, int numAttackedAndDamage,
								List<Pair<Integer, List<Integer>>> damage, int speed) {
		// 7D 00 #30 75 00 00# 12 00 06 02 0A 00 00 00 00 01 00 00 00 00 97 02
		// 00 00 97 02 00 00
		// 7D 00 #30 75 00 00# 11 00 06 02 0A 00 00 00 00 20 00 00 00 49 06 00
		// 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
		// mplew.writeShort(0x7F); // 47 7D
		if (skill == 4211006) { // meso explosion
			addMesoExplosion(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
		} else {
			addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
		}
		return mplew.getPacket();
	}

	public static MaplePacket rangedAttack(int cid, int skill, int stance, int numAttackedAndDamage, int projectile,
								List<Pair<Integer, List<Integer>>> damage, int speed) {
		// 7E 00 30 75 00 00 01 00 97 04 0A CB 72 1F 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
		// mplew.writeShort(0x80); // 47 7E
		addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, projectile, damage, speed);
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}


	public static MaplePacket magicAttack(int cid, int skill, int stance, int numAttackedAndDamage,
								List<Pair<Integer, List<Integer>>> damage, int charge, int speed) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
		// mplew.writeShort(0x81);
		addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
		if (charge != -1) {
			mplew.writeInt(charge);
		}

		return mplew.getPacket();
	}

	private static void addAttackBody(LittleEndianWriter lew, int cid, int skill, int stance, int numAttackedAndDamage,
								int projectile, List<Pair<Integer, List<Integer>>> damage, int speed) {
		lew.writeInt(cid);
		lew.write(numAttackedAndDamage);
		if (skill > 0) {
			lew.write(0xFF); // too low and some skills don't work (?)
			lew.writeInt(skill);
		} else {
			lew.write(0);
		}
		lew.write(0);
		lew.write(stance);
		lew.write(speed);
		lew.write(0x0A);
		//lew.write(0);
		lew.writeInt(projectile);

		for (Pair<Integer, List<Integer>> oned : damage) {
			if (oned.getRight() != null) {
				lew.writeInt(oned.getLeft().intValue());
				lew.write(0xFF);
				for (Integer eachd : oned.getRight()) {
					// highest bit set = crit
					lew.writeInt(eachd.intValue());
				}
			}
		}
	}

	private static void addMesoExplosion(LittleEndianWriter lew, int cid, int skill, int stance,
									int numAttackedAndDamage, int projectile,
									List<Pair<Integer, List<Integer>>> damage, int speed) {
		// 7A 00 6B F4 0C 00 22 1E 3E 41 40 00 38 04 0A 00 00 00 00 44 B0 04 00
		// 06 02 E6 00 00 00 D0 00 00 00 F2 46 0E 00 06 02 D3 00 00 00 3B 01 00
		// 00
		// 7A 00 6B F4 0C 00 00 1E 3E 41 40 00 38 04 0A 00 00 00 00
		lew.writeInt(cid);
		lew.write(numAttackedAndDamage);
		lew.write(0x1E);
		lew.writeInt(skill);
		lew.write(0);
		lew.write(stance);
		lew.write(speed);
		lew.write(0x0A);
		lew.writeInt(projectile);

		for (Pair<Integer, List<Integer>> oned : damage) {
			if (oned.getRight() != null) {
				lew.writeInt(oned.getLeft().intValue());
				lew.write(0xFF);
				lew.write(oned.getRight().size());
				for (Integer eachd : oned.getRight()) {
					lew.writeInt(eachd.intValue());
				}
			}
		}

	}

	public static MaplePacket getNPCShop(int sid, List<MapleShopItem> items) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		//0F 01 
		//38 6D 0F 00 
		//24 00 
		
		//E2 27 16 00 
		//B8 0B 00 00 
		//01 00 
		//01 00 
		
		//E3 27 16 00 
		//70 17 00 00 
		//01 00 
		//01 00 
		
		//E1 27 16 00 
		//10 27 00 00 
		//01 00 
		//01 00 
		
		//E0 27 16 00 
		//20 4E 00 00 
		//01 00 
		//01 00 
		
		//F1 4E 16 00 
		//A0 0F 00 00 
		//01 00 
		//01 00 
		
		//F2 4E 16 00 
		//40 1F 00 00 
		//01 00 
		//01 00 
		
		//F3 4E 16 00 
		//E0 2E 00 00 
		//01 00 
		//01 00 
		
		//F0 4E 16 00 
		//30 75 00 00 
		//01 00 
		//01 00 
		
		//F7 DD 13 00 
		//B8 0B 00 00 
		//01 00 
		//01 00 
		
		//17 2C 14 00 
		//70 17 00 00 
		//01 00 
		//01 00 
		
		//18 2C 14 00 
		//E0 2E 00 00 
		//01 00 
		//01 00 
		
		//B4 B2 15 00 
		//20 4E 00 00 
		//01 00 
		//01 00 
		
		//D4 00 16 00 
		//C0 5D 00 00 
		//01 00 
		//01 00 
		
		//F0 95 1F 00 
		//00 00 00 00 
		//33 33 33 33 33 33 
		
		//D3 3F F4 01 
		//F1 95 1F 00 
		//00 00 00 00 
		//9A 99 99 99 99 99 
		
		//D9 3F F4 01 
		//F2 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//E0 3F BC 02 
		//F3 95 1F 00 
		//00 00 00 00 
		//33 33 33 33 33 33 
		
		//E3 3F F4 01 
		//F4 95 1F 00 
		//00 00 00 00 
		//33 33 33 33 33 33 
		
		//E3 3F E8 03 
		//F5 95 1F 00 
		//00 00 00 00 
		//66 66 66 66 66 66 
		
		//E6 3F E8 03 
		//F6 95 1F 00 
		//00 00 00 00 
		//9A 99 99 99 99 99 
		
		//E9 3F 20 03 
		//F7 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F E8 03 
		//F8 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F 20 03 
		//F9 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F 20 03 
		//FA 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F 20 03 
		//FB 95 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F 20 03 
		//00 96 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//F0 3F 20 03 
		//02 96 1F 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//00 40 20 03 
		//90 8D 23 00 
		//00 00 00 00 
		//33 33 33 33 33 33 
		
		//D3 3F 20 03 
		//91 8D 23 00 
		//00 00 00 00 
		//9A 99 99 99 99 99 
		
		//D9 3F B0 04 
		//92 8D 23 00 
		//00 00 00 00 
		//00 00 00 00 00 00 
		
		//E0 3F 40 06 
		//93 8D 23 00 
		//00 00 
		//00 00 
		//33 33 33 33 33 33 
		
		//E3 3F 98 08 
		//94 8D 23 00 
		//00 00 
		//00 00 
		//66 66 66 66 66 66 
		
		//E6 3F 28 0A 
		//95 8D 23 00 
		//00 00 
		//00 00 
		//9A 99 99 99 99 99 
		
		//E9 3F B8 0B 
		//78 91 23 00 
		//00 00 
		//00 00 
		//66 66 66 66 66 66 
		
		//E6 3F 20 03 
		//60 95 23 00 
		//00 00 
		//00 00 
		//66 66 66 66 66 66 
		
		//E6 3F 20 03 
		//FD 95 1F 00 
		//00 00 
		//00 00 
		//33 33 33 33 33 33 
		
		//E3 3F E8 03
		
		mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(items.size()); // item count

		for (MapleShopItem item : items) {
			mplew.writeInt(item.getItemId());
			mplew.writeInt(item.getPrice());
			if (!ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
					mplew.writeShort(1); // stacksize o.o
					
					mplew.writeShort(item.getBuyable());
			} else {
				mplew.writeShort(0);
				mplew.writeInt(0);
				// o.O getPrice sometimes returns the unitPrice not the price
				mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
				mplew.writeShort(ii.getSlotMax(item.getItemId()));
			}
		}

		return mplew.getPacket();
	}

	/**
	 * code (8 = sell, 0 = buy, 0x20 = due to an error the trade did not happen
	 * o.o)
	 * 
	 * @param code
	 * @return
	 */
	public static MaplePacket confirmShopTransaction(byte code) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
		// mplew.writeShort(0xE6); // 47 E4
		mplew.write(code); // recharge == 8?

		return mplew.getPacket();
	}

	/*
	 * 19 reference 00 01 00 = new while adding 01 01 00 = add from drop 00 01 01 = update count 00 01 03 = clear slot
	 * 01 01 02 = move to empty slot 01 02 03 = move and merge 01 02 01 = move and merge with rest
	 */
	public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item) {
		return addInventorySlot(type, item, false);
	}
	
	public static MaplePacket updateItemInSlot(IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(0); // could be from drop
		mplew.write(2); // always 2
		mplew.write(3); // quantity > 0 (?)
		mplew.write(item.getType()); // inventory type
		mplew.write(item.getPosition()); // item slot
		mplew.writeShort(0);
		mplew.write(1);
		mplew.write(item.getPosition()); // wtf repeat
		MaplePacketHelper.addItemInfo(mplew, item, true, false);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		// mplew.writeShort(0x19);
		if (fromDrop) {
			mplew.write(1);
		} else {
			mplew.write(0);
		}
		mplew.write(HexTool.getByteArrayFromHexString("01 00")); // add mode

		mplew.write(type.getType()); // iv type

		mplew.write(item.getPosition()); // slot id

		MaplePacketHelper.addItemInfo(mplew, item, true, false);
		return mplew.getPacket();
	}

	public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item) {
		return updateInventorySlot(type, item, false);
	}

	public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		if (fromDrop) {
			mplew.write(1);
		} else {
			mplew.write(0);
		}
		mplew.write(HexTool.getByteArrayFromHexString("01 01")); // update
		// mode

		mplew.write(type.getType()); // iv type

		mplew.write(item.getPosition()); // slot id

		mplew.write(0); // ?

		mplew.writeShort(item.getQuantity());
		return mplew.getPacket();
	}

	public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
		return moveInventoryItem(type, src, dst, (byte) -1);
	}

	public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("01 01 02"));
		mplew.write(type.getType());
		mplew.writeShort(src);
		mplew.writeShort(dst);
		if (equipIndicator != -1) {
			mplew.write(equipIndicator);
		}
		return mplew.getPacket();
	}

	public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, byte src, byte dst, short total) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("01 02 03"));
		mplew.write(type.getType());
		mplew.writeShort(src);
		mplew.write(1); // merge mode?

		mplew.write(type.getType());
		mplew.writeShort(dst);
		mplew.writeShort(total);
		return mplew.getPacket();
	}

	public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, byte src, byte dst,
																short srcQ, short dstQ) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("01 02 01"));
		mplew.write(type.getType());
		mplew.writeShort(src);
		mplew.writeShort(srcQ);
		mplew.write(HexTool.getByteArrayFromHexString("01"));
		mplew.write(type.getType());
		mplew.writeShort(dst);
		mplew.writeShort(dstQ);
		return mplew.getPacket();
	}

	public static MaplePacket clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(fromDrop ? 1 : 0);
		mplew.write(HexTool.getByteArrayFromHexString("01 03"));
		mplew.write(type.getType());
		mplew.writeShort(slot);
		return mplew.getPacket();
	}

	public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed) {
		// 18 00 01 02 03 02 08 00 03 01 F7 FF 01
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(1); // fromdrop always true

		mplew.write(destroyed ? 2 : 3);
		mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
		mplew.write(MapleInventoryType.USE.getType());
		mplew.writeShort(scroll.getPosition());
		if (scroll.getQuantity() > 0) {
			mplew.writeShort(scroll.getQuantity());
		}
		mplew.write(3);
		if (!destroyed) {
			mplew.write(MapleInventoryType.EQUIP.getType());
			mplew.writeShort(item.getPosition());
			mplew.write(0);
		}
		mplew.write(MapleInventoryType.EQUIP.getType());
		mplew.writeShort(item.getPosition());
		if (!destroyed) {
			MaplePacketHelper.addItemInfo(mplew, item, true, true);
		}
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
		mplew.writeInt(chr);
		switch (scrollSuccess) {
			case SUCCESS:
				mplew.writeShort(1);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
			case FAIL:
				mplew.writeShort(0);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
			case CURSE:
				mplew.write(0);
				mplew.write(1);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
			default:
				throw new IllegalArgumentException("effect in illegal range");
		}

		return mplew.getPacket();
	}

	public static MaplePacket removePlayerFromMap(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
		// mplew.writeShort(0x65); // 47 63
		mplew.writeInt(cid);
		return mplew.getPacket();
	}

	/**
	 * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
	 * 4 - explode<br/> cid is ignored for 0 and 1
	 * 
	 * @param oid
	 * @param animation
	 * @param cid
	 * @return
	 */
	public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
		return removeItemFromMap(oid, animation, cid, false, 0);
	}

	/**
	 * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
	 * 4 - explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet
	 * as true will make a pet pick up the item.
	 * 
	 * @param oid
	 * @param animation
	 * @param cid
	 * @param pet
	 * @param slot
	 * @return
	 */
	public static MaplePacket removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		mplew.write(animation); // expire

		mplew.writeInt(oid);
		if (animation >= 2) {
			mplew.writeInt(cid);
			if (pet) {
				mplew.write(slot);
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateCharLook(MapleCharacter chr) {
		// 88 00 80 74 03 00 01 00 00 19 50 00 00 00 67 75 00 00 02 34 71 0F 00
		// 04 59 BF 0F 00 05 AB 05 10 00 07 8C 5B
		// 10 00 08 F4 82 10 00 09 E7 D0 10 00 0A BE A9 10 00 0B 0C 05 14 00 FF
		// FF 00 00 00 00 00 00 00 00 00 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(1);
		MaplePacketHelper.addCharLook(mplew, chr, false);
		//mplew.writeShort(0);
		
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> equippedC = iv.list();
		List<Item> equipped = new ArrayList<Item>(equippedC.size());
		for (IItem item : equippedC) {
			equipped.add((Item) item);
		}
		Collections.sort(equipped);
		List<MapleRing> rings = new ArrayList<MapleRing>();
		for (Item item : equipped) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		
		Collections.sort(rings);
		/*
		 97 00
		 77 6B 2E 00
		 01 00 00 20 4E 00 00 00 04 76 00 00 05 C8 DE 0F 00 06 DD 2C 10 00 07 B7 5B 10 00 08 92 82 10 00 0B F0 4E 16 00 0C E0 FA 10 00 0D C1 F7 10 00 0E 2E 7F 1B 00 11 D7 1E 11 00 17 20 A6 1B 00 1A 24 A6 1B 00 FF FF 00 00 00 00 57 4B 4C 00 00 00 00 00 00 00 00 00
		 01
		 07 6C 31 00
		 00 00 00 00
		 08 6C 31 00
		 00 00 00 00
		 C1 F7 10 00
		 01
		 3B 13 32 00
		 00 00 00 00
		 3C 13 32 00
		 00 00 00 00
		 E0 FA 10 00
		 00*/
		if (rings.size() > 0) {
			for (MapleRing ring : rings) {
				mplew.write(1);
				mplew.writeInt(ring.getRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getPartnerRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getItemId());
			}
		} else {
			mplew.write(0);
		}
		mplew.writeShort(0);

		return mplew.getPacket();
	}

	public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		// mplew.writeShort(0x19);
		mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
		mplew.write(type.getType());
		mplew.writeShort(src);
		if (src < 0) {
			mplew.write(1);
		}
		return mplew.getPacket();
	}

	public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
		mplew.write(type.getType());
		mplew.writeShort(item.getPosition());
		mplew.writeShort(item.getQuantity());
		return mplew.getPacket();
	}

	public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
		// 82 00 30 C0 23 00 FF 00 00 00 00 B4 34 03 00 01 00 00 00 00 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
		// mplew.writeShort(0x84); // 47 82
		mplew.writeInt(cid);
		mplew.write(skill);
		mplew.writeInt(damage);
		mplew.writeInt(monsteridfrom);
		mplew.write(direction);
		if (pgmr) {
			mplew.write(pgmr_1);
			mplew.write(is_pg ? 1 : 0);
			mplew.writeInt(oid);
			mplew.write(6);
			mplew.writeShort(pos_x);
			mplew.writeShort(pos_y);
			mplew.write(0);
		} else {
			mplew.writeShort(0);
		}

		mplew.writeInt(damage);

		if (fake > 0) {
			mplew.writeInt(fake);
		}
		
		return mplew.getPacket();
	}

	public static MaplePacket charNameResponse(String charname, boolean nameUsed) {
		// 0D 00 0C 00 42 6C 61 62 6C 75 62 62 31 32 33 34 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
		mplew.writeMapleAsciiString(charname);
		mplew.write(nameUsed ? 1 : 0);

		return mplew.getPacket();
	}

	public static MaplePacket addNewCharEntry(MapleCharacter chr, boolean worked) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
		mplew.write(worked ? 0 : 1);
		MaplePacketHelper.addCharEntry(mplew, chr);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param quest
	 * @return
	 */
	public static MaplePacket startQuest(MapleCharacter c, short quest) {
		// [24 00] [01] [69 08] [01 00] [00]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// mplew.writeShort(0x21);
		/*mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.writeShort(1);
		mplew.write(0);*/
		
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(1);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeShort(0);
		
		return mplew.getPacket();
	}

	/**
	 * state 0 = del ok state 12 = invalid bday
	 * 
	 * @param cid
	 * @param state
	 * @return
	 */
	public static MaplePacket deleteCharResponse(int cid, int state) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
		mplew.writeInt(cid);
		mplew.write(state);
		return mplew.getPacket();
	}
	
    public static MaplePacket charInfo(MapleCharacter chr) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    	mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
    	mplew.writeInt(chr.getId());
    	mplew.write(chr.getLevel());
    	mplew.writeShort(chr.getJob().getId());
    	mplew.writeShort(chr.getFame());
    	mplew.write(0); // heart red or gray

    	if (chr.getGuildId() <= 0) {
    	    mplew.writeMapleAsciiString("-");
    	    mplew.writeMapleAsciiString("-");
    	} else {
    	    MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
    	    mplew.writeMapleAsciiString(gs.getName());
    	    //MapleAlliance alliance = chr.getGuild().getAlliance(chr.getClient());
    	    //if (alliance == null) {
    	    	mplew.writeMapleAsciiString("-");
    	    /*} else {
    	    	mplew.writeMapleAsciiString(alliance.getName());
    	    }*/
    	}
    	mplew.write(0);
    	MaplePet[] pets = chr.getPets();
    	IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
    	for (int i = 0; i < 3; i++) {
    	    if (pets[i] != null) {
    		mplew.write(pets[i].getUniqueId());
    		mplew.writeInt(pets[i].getItemId()); // petid
    		mplew.writeMapleAsciiString(pets[i].getName());
    		mplew.write(pets[i].getLevel()); // pet level
    		mplew.writeShort(pets[i].getCloseness()); // pet closeness
    		mplew.write(pets[i].getFullness()); // pet fullness
    		mplew.writeShort(0);
    		mplew.writeInt(inv != null ? inv.getItemId() : 0);
    	    }
    	}
    	mplew.writeShort(0); // Mount here

    	//int wishlistSize = chr.getWishlistSize();
    	mplew.write(0);//wishlistSize);
    	/*if (wishlistSize > 0) {
    	    int[] wishlist = chr.getWishlist();
    	    for (int i = 0; i < wishlistSize; i++) {
    		mplew.writeInt(wishlist[i]);
    	    }
    	}*/

    	int normalcard = chr.getMonsterBook().getNormalCard(),
    	    specialcard = chr.getMonsterBook().getSpecialCard();
    	mplew.writeInt(chr.getMonsterBook().getBookLevel());
    	mplew.writeInt(normalcard);
    	mplew.writeInt(specialcard);
    	mplew.writeInt(normalcard + specialcard);
    	if (chr.getMonsterBookCover() != 0)
    		mplew.writeInt(chr.getMonsterBook().getCardMobID(chr.getMonsterBookCover()));
    	else
    		mplew.writeInt(0);

    	return mplew.getPacket();
    }


	/**
	 * 
	 * @param c
	 * @param quest
	 * @return
	 */
	public static MaplePacket forfeitQuest(MapleCharacter c, short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		/*mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.writeShort(0);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0);*/
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param quest
	 * @return
	 */
	public static MaplePacket completeQuest(MapleCharacter c, short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		/*mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(HexTool.getByteArrayFromHexString("02 A0 67 B9 DA 69 3A C8 01"));
		mplew.writeInt(0);
		mplew.writeInt(0);*/
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(2);
		mplew.writeLong(MaplePacketHelper.getTime((long) System.currentTimeMillis()));
		
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param quest
	 * @param npc
	 * @param progress
	 * @return
	 */
	// frz note, 0.52 transition: this is only used when starting a quest and
	// seems to have no effect, is it needed?
	public static MaplePacket updateQuestInfo(MapleCharacter c, short quest, int npc, byte progress) {
		// [A5 00] [08] [69 08] [86 71 0F 00] [00 00 00 00]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(progress);
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	private static <E extends LongValueHolder> long getLongMask(List<Pair<E, Integer>> statups) {
		long mask = 0;
		for (Pair<E, Integer> statup : statups) {
			mask |= statup.getLeft().getValue();
		}
		return mask;
	}

	private static <E extends LongValueHolder> long getLongMaskFromList(List<E> statups) {
		long mask = 0;
		for (E statup : statups) {
			mask |= statup.getValue();
		}
		return mask;
	}
	
	private static <E extends LongValueHolder> long getLongMaskD(List<Pair<MapleDisease, Integer>> statups) {
		long mask = 0;
		for (Pair<MapleDisease, Integer> statup : statups) {
			mask |= statup.getLeft().getValue();
		}
		return mask;
	}

	private static <E extends LongValueHolder> long getLongMaskFromListD(List<MapleDisease> statups) {
		long mask = 0;
		for (MapleDisease statup : statups) {
			mask |= statup.getValue();
		}
		return mask;
	}
	
	public static MaplePacket giveBuffTest(int buffid, long mask) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
		
		mplew.writeLong(0);
		
		mplew.writeLong(mask);
		mplew.writeShort(1);
		mplew.writeInt(buffid);
		mplew.writeInt(60);
		
		mplew.writeShort(0); // ??? wk charges have 600 here o.o

		mplew.write(0); // combo 600, too

		mplew.write(0); // new in v0.56
		mplew.write(0);

		return mplew.getPacket();
	}
	
	/**
	 * It is important that statups is in the correct order (see decleration
	 * order in MapleBuffStat) since this method doesn't do automagical
	 * reordering.
	 * 
	 * @param buffid
	 * @param bufflength
	 * @param statups
	 * @param morph
	 * @return
	 */
	public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean morph) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
		
		// darksight
		// 1C 00 80 04 00 00 00 00 00 00 F4 FF EB 0C 3D 00 C8 00 01 00 EB 0C 3D
		// 00 C8 00 00 00 01
		// fire charge
		// 1C 00 04 00 40 00 00 00 00 00 26 00 7B 7A 12 00 90 01 01 00 7B 7A 12
		// 00 90 01 58 02
		// ice charge
		// 1C 00 04 00 40 00 00 00 00 00 07 00 7D 7A 12 00 26 00 01 00 7D 7A 12
		// 00 26 00 58 02
		// thunder charge
		// 1C 00 04 00 40 00 00 00 00 00 0B 00 7F 7A 12 00 18 00 01 00 7F 7A 12
		// 00 18 00 58 02

		// incincible 0.49
		// 1B 00 00 80 00 00 00 00 00 00 0F 00 4B 1C 23 00 F8 24 01 00 00 00
		// mguard 0.49
		// 1B 00 00 02 00 00 00 00 00 00 50 00 6A 88 1E 00 C0 27 09 00 00 00
		// bless 0.49

		// 1B 00 3A 00 00 00 00 00 00 00 14 00 4C 1C 23 00 3F 0D 03 00 14 00 4C
		// 1C 23 00 3F 0D 03 00 14 00 4C 1C 23 00 3F 0D 03 00 14 00 4C 1C 23 00
		// 3F 0D 03 00 00 00

		// combo
		// 1B 00 00 00 20 00 00 00 00 00 01 00 DA F3 10 00 C0 D4 01 00 58 02
		// 1B 00 00 00 20 00 00 00 00 00 02 00 DA F3 10 00 57 B7 01 00 00 00
		// 1B 00 00 00 20 00 00 00 00 00 03 00 DA F3 10 00 51 A7 01 00 00 00

		// 01 00
		// 79 00 - monster skill
		// 01 00
		// B4 78 00 00
		// 00 00
		// 84 03
		
		/*
		 1D 00
		 * 
		 00 00 00 00 00 00 00 00
		 * 
		 00 00 00 40 00 00 00 00
		 * 
		 00 00
		 B0 05 1D 00
		 EC 03 00 00
		 * 
		 B9 8D 25 2F
		 * 
		 00 00 02
		 */
		
		// [1D 00] [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 08 00] [01 00] [78 00 0C 00] [38 7C 00 00] [00 00] [08] [07]
		long mask = getLongMask(statups);
		
		//1D 00 
		//00 00 00 00 40 00 00 00 
		//40 00 00 00
		//00 00 00 00 
		//00 00 00 00 
		//00 00 
		//B0 05 1D 00 
		//EC 03 00 00 00 00 00 00 00 00 00 04
		
		mplew.writeLong(0);
		
		mplew.writeLong(mask);
		for (Pair<MapleBuffStat, Integer> statup : statups) {
			mplew.writeShort(statup.getRight().shortValue());
			mplew.writeInt(buffid);
			mplew.writeInt(bufflength);
		}
		
		if (bufflength == 1004) {
			mplew.writeInt(0x2F258DB9);
		} else {
			mplew.writeShort(0); // ??? wk charges have 600 here o.o
		}

		mplew.write(0); // combo 600, too

		mplew.write(0); // new in v0.56

		mplew.write(0);

		return mplew.getPacket();
	}
	
	/*public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean morph, boolean ismount, MapleMount mount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMask(statups);
        if (ismount)
            mplew.writeInt(0);
        else
            mplew.writeLong(0);
        mplew.writeLong(mask);
        if (!ismount) {
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                mplew.writeShort(statup.getRight().shortValue());
                mplew.writeInt(buffid);
                mplew.writeInt(bufflength);
            }
            mplew.writeShort(0); // ??? wk charges have 600 here o.o
            mplew.write(0); // combo 600, too
            mplew.write(0); // new in v0.56
            mplew.write(0);
        } else {
            mplew.writeInt(0);
            mplew.writeShort(0);
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(mount.getSkillId());
            mplew.writeInt(0); //Server Tick value.
            mplew.writeShort(0);
            mplew.write(0);
            mplew.write(0); //Times you have been buffed
        }
        return mplew.getPacket();
    }*/
	
	public static MaplePacket getTestPacketLol() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.write(HexTool.getByteArrayFromHexString("1D 00 00 00 00 00 40 00 00 00 00 00 00 00 00 00 00 00 00 00 B0 05 1D 00 EC 03 00 00 00 00 00 00 00 00 00 04"));
		return mplew.getPacket();
	}
	
	public static MaplePacket giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
		// [1D 00] [00 00 00 00 00 00 00 00] [00 00 02 00 00 00 00 00] [00 00] [7B 00] [04 00] [B8 0B 00 00] [00 00] [84 03] [01]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
		
		long mask = getLongMaskD(statups);
		
		mplew.writeLong(0);
		mplew.writeLong(mask);
		
		for (Pair<MapleDisease, Integer> statup : statups) {
			mplew.writeShort(statup.getRight().shortValue());
			mplew.writeShort(skill.getSkillId());
			mplew.writeShort(skill.getSkillLevel());
			mplew.writeInt((int) skill.getDuration());
		}

		mplew.writeShort(0); // ??? wk charges have 600 here o.o
		mplew.writeShort(900);//Delay
		mplew.write(1);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
		// [99 00] [6A 4D 27 00] [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 00 40] [7A 00] [01 00] [00 00] [84 03]
		// [99 00] [7E 31 50 00] [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 00 80] [7C 00] [01 00] [00 00] [84 03]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());

		mplew.writeInt(cid);
		long mask = getLongMaskD(statups);
		
		mplew.writeLong(0);
		mplew.writeLong(mask);

		for (@SuppressWarnings("unused")
		Pair<MapleDisease, Integer> statup : statups) {
			//mplew.writeShort(statup.getRight().byteValue());
			mplew.writeShort(skill.getSkillId());
			mplew.writeShort(skill.getSkillLevel());
		}
		mplew.writeShort(0); // same as give_buff
		mplew.writeShort(900);//Delay

		return mplew.getPacket();
	}
	
	public static MaplePacket cancelForeignDebuff(int cid, List<MapleDisease> statups) {
		// 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());

		mplew.writeInt(cid);
		long mask = getLongMaskFromListD(statups);
		
		mplew.writeLong(0);
		mplew.writeLong(mask);

		return mplew.getPacket();
	}
	
	public static MaplePacket showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
		// 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());

		mplew.writeInt(cid);
		long mask = getLongMask(statups);
		
		mplew.writeLong(0);
		
		mplew.writeLong(mask);
		mplew.writeShort(0);
		mplew.writeInt(itemId);
		mplew.writeInt(skillId);
		mplew.writeInt(0x2D4DFC2A);
		
		mplew.writeShort(0); // same as give_buff

		return mplew.getPacket();
	}
	
	public static MaplePacket giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups, boolean morph) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleBuffStat, Integer> statup : statups)
            if (morph)
                mplew.writeInt(statup.getRight().intValue());
            else
                mplew.writeShort(statup.getRight().shortValue());
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    } 

	public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
		// 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());

		mplew.writeInt(cid);
		
		long mask = getLongMaskFromList(statups);
		
		mplew.writeLong(0);
		
		mplew.writeLong(mask); 

		return mplew.getPacket();
	}

	public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
		
		long mask = getLongMaskFromList(statups);
		
		mplew.writeLong(0);
		
		mplew.writeLong(mask); 
		
		mplew.write(3); // wtf?
		
		return mplew.getPacket();
	}
	
	public static MaplePacket cancelDebuff(List<MapleDisease> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
		long mask = getLongMaskFromListD(statups);
		
		mplew.writeLong(0);
		mplew.writeLong(mask);
		
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("06 08"));
		mplew.write(owner ? 0 : 1);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("04 02"));
		MaplePacketHelper.addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		return mplew.getPacket();
	}

	public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("04 01"));// 00 04 88 4E
																// 00"));

		MaplePacketHelper.addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		return mplew.getPacket();
	}

	public static MaplePacket getTradeInvite(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("02 03"));
		mplew.writeMapleAsciiString(c.getName());
		mplew.write(HexTool.getByteArrayFromHexString("B7 50 00 00"));
		return mplew.getPacket();
	}

	public static MaplePacket getTradeMesoSet(byte number, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0xF);
		mplew.write(number);
		mplew.writeInt(meso);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeItemAdd(byte number, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0xE);
		mplew.write(number);
		// mplew.write(1);
		MaplePacketHelper.addItemInfo(mplew, item);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopItemUpdate(MaplePlayerShop shop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0x16);
		mplew.write(shop.getItems().size());
		for (MaplePlayerShopItem item : shop.getItems()) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			MaplePacketHelper.addItemInfo(mplew, item.getItem(), true, true);
		}
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param shop
	 * @param owner
	 * @return
	 */
	public static MaplePacket getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("05 04 04"));
		mplew.write(owner ? 0 : 1);
		mplew.write(0);
		MaplePacketHelper.addCharLook(mplew, shop.getOwner(), false);
		mplew.writeMapleAsciiString(shop.getOwner().getName());

		MapleCharacter[] visitors = shop.getVisitors();
		for (int i = 0; i < visitors.length; i++) {
			if (visitors[i] != null) {
				mplew.write(i + 1);
				MaplePacketHelper.addCharLook(mplew, visitors[i], false);
				mplew.writeMapleAsciiString(visitors[i].getName());
			}
		}
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(shop.getDescription());
		List<MaplePlayerShopItem> items = shop.getItems();
		mplew.write(0x10);
		mplew.write(items.size());
		for (MaplePlayerShopItem item : items) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			MaplePacketHelper.addItemInfo(mplew, item.getItem(), true, true);
		}
		// mplew.write(HexTool.getByteArrayFromHexString("01 60 BF 0F 00 00 00
		// 80 05 BB 46 E6 17 02 05 00 00 00 00 00 00
		// 00 00 00 1D 00 16 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 1B 7F 00 00 0D 00 00
		// 40 01 00 01 00 FF 34 0C 00 01 E6 D0 10 00 00 00 80 05 BB 46 E6 17 02
		// 04 01 00 00 00 00 00 00 00 00 0A 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00
		// 63 CF 07 01 00 00 00 7C 01 00 01 00 5F
		// AE 0A 00 01 79 16 15 00 00 00 80 05 BB 46 E6 17 02 07 00 00 00 00 00
		// 00 00 00 00 66 00 00 00 21 00 2F 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A4 82 7A 01 00 00
		// 00 7C 01 00 01 00 5F AE 0A 00 01 79 16
		// 15 00 00 00 80 05 BB 46 E6 17 02 07 00 00 00 00 00 00 00 00 00 66 00
		// 00 00 23 00 2C 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 FE AD 88 01 00 00 00 7C 01 00 01 00
		// DF 67 35 00 01 E5 D0 10 00 00 00 80 05
		// BB 46 E6 17 02 01 03 00 00 00 00 07 00 00 00 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 0A 00 00 00 00 00 00
		// 00 00 00 00 00 CE D4 F1 00 00 00 00 7C 01 00 01 00 7F 1A 06 00 01 4C
		// BF 0F 00 00 00 80 05 BB 46 E6 17 02 05
		// 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 00 38
		// CE AF 00 00 00 00 7C 01 00 01 00 BF 27 09 00 01 07 76 16 00 00 00 80
		// 05 BB 46 E6 17 02 00 07 00 00 00 00 00
		// 00 00 00 00 00 00 00 17 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 7C 02 00 00 1E 00 00
		// 48 01 00 01 00 5F E3 16 00 01 11 05 14 00 00 00 80 05 BB 46 E6 17 02
		// 07 00 00 00 00 00 00 00 00 00 00 00 00
		// 00 21 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		// 1C 8A 00 00 39 00 00 10 01 00 01 00 7F
		// 84 1E 00 01 05 DE 13 00 00 00 80 05 BB 46 E6 17 02 07 00 00 00 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 00 00
		// 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 00 00 00 E5 07 01 00 00
		// 00 7C 2B 00 01 00 AF B3 00 00 02 FC 0C
		// 3D 00 00 00 80 05 BB 46 E6 17 02 2B 00 00 00 00 00 00 00 01 00 0F 27
		// 00 00 02 D1 ED 2D 00 00 00 80 05 BB 46
		// E6 17 02 01 00 00 00 00 00 0A 00 01 00 9F 0F 00 00 02 84 84 1E 00 00
		// 00 80 05 BB 46 E6 17 02 0A 00 00 00 00
		// 00 01 00 01 00 FF 08 3D 00 01 02 05 14 00 00 00 80 05 BB 46 E6 17 02
		// 07 00 00 00 00 00 00 00 00 00 00 00 00
		// 00 25 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		// 78 36 00 00 1D 00 00 14 01 00 01 00 9F
		// 25 26 00 01 2B 2C 14 00 00 00 80 05 BB 46 E6 17 02 07 00 00 00 00 00
		// 00 00 00 00 00 00 00 00 34 00 00 00 06
		// 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E8 76 00 00 1F 00
		// 00 24 01 00 01 00 BF 0E 16 02 01 D9 D0
		// 10 00 00 00 80 05 BB 46 E6 17 02 00 04 00 00 00 00 00 00 07 00 00 00
		// 00 00 02 00 00 00 06 00 08 00 00 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 23 02 00 00 1C 00 00 1C 5A 00 01 00
		// 0F 27 00 00 02 B8 14 3D 00 00 00 80 05
		// BB 46 E6 17 02 5A 00 00 00 00 00"));
		/*
		 * 10 10 01 00 01 00 3F 42 0F 00 01 60 BF 0F 00 00 00 80 05 BB /* ||||||||||| OMG ITS THE PRICE ||||| PROBABLY
		 * THA QUANTITY ||||||||||| itemid
		 * 
		 */
		// mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("05 03 02"));
		mplew.write(number);
		if (number == 1) {
			mplew.write(0);
			MaplePacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
			mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
		}
		mplew.write(number);
		/*if (number == 1) {
		mplew.write(0);
		mplew.writeInt(c.getPlayer().getId());
		}*/
		MaplePacketHelper.addCharLook(mplew, c.getPlayer(), false);
		mplew.writeMapleAsciiString(c.getPlayer().getName());
		mplew.write(0xFF);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeConfirmation() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0x10);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeCompletion(byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0xA);
		mplew.write(number);
		mplew.write(6);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeCancel(byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0xA);
		mplew.write(number);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static MaplePacket updateCharBox(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		if (c.getPlayerShop() != null) {
			addAnnounceBox(mplew, c.getPlayerShop());
		} else {
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?

		mplew.writeInt(npc);
		mplew.write(msgType);
		mplew.writeMapleAsciiString(talk);
		mplew.write(HexTool.getByteArrayFromHexString(endBytes));
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkStyle(int npc, String talk, int styles[]) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?

		mplew.writeInt(npc);
		mplew.write(7);
		mplew.writeMapleAsciiString(talk);
		mplew.write(styles.length);
		for (int i = 0; i < styles.length; i++) {
			mplew.writeInt(styles[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?

		mplew.writeInt(npc);
		mplew.write(3);
		mplew.writeMapleAsciiString(talk);
		mplew.writeInt(def);
		mplew.writeInt(min);
		mplew.writeInt(max);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkText(int npc, String talk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?

		mplew.writeInt(npc);
		mplew.write(2);
		mplew.writeMapleAsciiString(talk);
		mplew.writeInt(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket showLevelup(int cid) {
		return showForeignEffect(cid, 0);
	}

	public static MaplePacket showJobChange(int cid) {
		return showForeignEffect(cid, 8);
	}

	public static MaplePacket showForeignEffect(int cid, int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid); // ?

		mplew.write(effect);
		return mplew.getPacket();
	}

	public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1); // probably buff level but we don't know it and it doesn't really matter
        return mplew.getPacket();
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid); // ?
            mplew.write(effectid); //buff level
            mplew.writeInt(skillid);
            mplew.write(1);
            if (direction != (byte) 3)
                mplew.write(direction);
        return mplew.getPacket();
    }

	public static MaplePacket updateSkill(int skillid, int level, int masterlevel) {
		// 1E 00 01 01 00 E9 03 00 00 01 00 00 00 00 00 00 00 01
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
		mplew.write(1);
		mplew.writeShort(1);
		mplew.writeInt(skillid);
		mplew.writeInt(level);
		mplew.writeInt(masterlevel);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket updateQuestMobKills(MapleQuestStatus status) {
		// 21 00 01 FB 03 01 03 00 30 30 31
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(status.getQuest().getId());
		mplew.write(1);
		StringBuilder killStr = new StringBuilder();
		for (int kills : status.getMobKills().values()) {
			killStr.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
		}
		mplew.writeMapleAsciiString(killStr.toString());
		mplew.writeLong(0);
		return mplew.getPacket();
	}

	public static MaplePacket getShowQuestCompletion(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
		mplew.writeShort(id);
		return mplew.getPacket();
	}

	public static MaplePacket getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
		mplew.write(0);

		for (int x = 0; x < 90; x++) {
			MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
			if (binding != null) {
				mplew.write(binding.getType());
				mplew.writeInt(binding.getAction());
			} else {
				mplew.write(0);
				mplew.writeInt(0);
			}
		}

		return mplew.getPacket();
	}

	public static MaplePacket getWhisper(String sender, int channel, String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(0x12);
		mplew.writeMapleAsciiString(sender);
		mplew.writeShort(channel - 1); // I guess this is the channel

		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param target name of the target character
	 * @param reply error code: 0x0 = cannot find char, 0x1 = success
	 * @return the MaplePacket
	 */
	public static MaplePacket getWhisperReply(String target, byte reply) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(0x0A); // whisper?

		mplew.writeMapleAsciiString(target);
		mplew.write(reply);
		// System.out.println(HexTool.toString(mplew.getPacket().getBytes()));
		return mplew.getPacket();
	}

	public static MaplePacket getFindReplyWithMap(String target, int mapid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(target);
		mplew.write(1);
		mplew.writeInt(mapid);
		// ?? official doesn't send zeros here but whatever
		mplew.write(new byte[8]);
		return mplew.getPacket();
	}

	public static MaplePacket getFindReply(String target, int channel) {
		// Received UNKNOWN (1205941596.79689): (25)
		// 54 00 09 07 00 64 61 76 74 73 61 69 01 86 7F 3D 36 D5 02 00 00 22 00
		// 00 00
		// T....davtsai..=6...."...
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(target);
		mplew.write(3);
		mplew.writeInt(channel - 1);
		return mplew.getPacket();
	}

	public static MaplePacket getInventoryFull() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(1);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getShowInventoryFull() {
		return getShowInventoryStatus(0xff);
	}
	
	public static MaplePacket showItemUnavailable()
	{
		return getShowInventoryStatus(0xfe);
	}

	public static MaplePacket getShowInventoryStatus(int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(0);
		mplew.write(mode);
		mplew.writeInt(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
		mplew.write(0x16);
		mplew.writeInt(npcId);
		mplew.write(slots);
		mplew.writeShort(0x7E);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		mplew.writeShort(0);
		mplew.write((byte) items.size());
		for (IItem item : items) {
			MaplePacketHelper.addItemInfo(mplew, item, true, true);
		}
		mplew.writeShort(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getStorageFull() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
		mplew.write(0x11);
		return mplew.getPacket();
	}

	public static MaplePacket mesoStorage(byte slots, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
		mplew.write(0x13);
		mplew.write(slots);
		mplew.writeShort(2);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		return mplew.getPacket();
	}

	public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
		mplew.write(0xD);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for (IItem item : items) {
			MaplePacketHelper.addItemInfo(mplew, item, true, true);
			// mplew.write(0);
		}

		return mplew.getPacket();
	}

	public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
		mplew.write(0x9);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for (IItem item : items) {
			MaplePacketHelper.addItemInfo(mplew, item, true, true);
			// mplew.write(0);
		}

		return mplew.getPacket();
	}

	/**
	 * 
	 * @param oid
	 * @param remhp in %
	 * @return
	 */
	public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
		mplew.writeInt(oid);
		mplew.write(remhppercentage);

		return mplew.getPacket();
	}

	public static MaplePacket showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		//53 00 05 21 B3 81 00 46 F2 5E 01 C0 F3 5E 01 04 01
		//00 81 B3 21 = 8500001 = Pap monster ID
		//01 5E F3 C0 = 23,000,000 = Pap max HP
		//04, 01 - boss bar color/background color as provided in WZ

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(5);
		mplew.writeInt(oid);
		mplew.writeInt(currHP);
		mplew.writeInt(maxHP);
		mplew.write(tagColor);
		mplew.write(tagBgColor);

		return mplew.getPacket();
	}

	public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());

		mplew.write(0);
		mplew.writeMapleAsciiString(charname);
		mplew.write(mode);
		mplew.writeShort(newfame);
		mplew.writeShort(0);

		return mplew.getPacket();
	}

	/**
	 * status can be: <br>
	 * 0: ok, use giveFameResponse<br>
	 * 1: the username is incorrectly entered<br>
	 * 2: users under level 15 are unable to toggle with fame.<br>
	 * 3: can't raise or drop fame anymore today.<br>
	 * 4: can't raise or drop fame for this character for this month anymore.<br>
	 * 5: received fame, use receiveFame()<br>
	 * 6: level of fame neither has been raised nor dropped due to an unexpected
	 * error
	 * 
	 * @param status
	 * @param mode
	 * @param charname
	 * @param newfame
	 * @return
	 */
	public static MaplePacket giveFameErrorResponse(int status) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());

		mplew.write(status);

		return mplew.getPacket();
	}

	public static MaplePacket receiveFame(int mode, String charnameFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
		mplew.write(5);
		mplew.writeMapleAsciiString(charnameFrom);
		mplew.write(mode);

		return mplew.getPacket();
	}

	public static MaplePacket partyCreated() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		mplew.write(8);
		mplew.writeShort(0x8b);
		mplew.writeShort(2);
		mplew.write(CHAR_INFO_MAGIC);
		mplew.write(CHAR_INFO_MAGIC);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket partyInvite(MapleCharacter from) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		mplew.write(4);
		mplew.writeInt(from.getParty().getId());
		mplew.writeMapleAsciiString(from.getName());
		mplew.write(0);

		return mplew.getPacket();
	}

	/**
	 * 10: a beginner can't create a party<br>
	 * 11/14/19: your request for a party didn't work due to an unexpected error<br>
	 * 13: you have yet to join a party<br>
	 * 16: already have joined a party<br>
	 * 17: the party you are trying to join is already at full capacity<br>
	 * 18: unable to find the requested character in this channel<br>
	 * 
	 * @param message
	 * @return
	 */
	public static MaplePacket partyStatusMessage(int message) {
		// 32 00 08 DA 14 00 00 FF C9 9A 3B FF C9 9A 3B 22 03 6E 67
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		mplew.write(message);

		return mplew.getPacket();
	}

	/**
	 * 22: has denied the invitation<br>
	 * 
	 * @param message
	 * @param charname
	 * @return
	 */
	public static MaplePacket partyStatusMessage(int message, String charname) {
		// 32 00 08 DA 14 00 00 FF C9 9A 3B FF C9 9A 3B 22 03 6E 67
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		mplew.write(message);
		mplew.writeMapleAsciiString(charname);

		return mplew.getPacket();
	}

	private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
		List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
		while (partymembers.size() < 6) {
			partymembers.add(new MaplePartyCharacter());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getId());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeAsciiString(StringUtil.getRightPaddedStr(partychar.getName(), '\0', 13));
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getJobId());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getLevel());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.isOnline()) {
				lew.writeInt(partychar.getChannel() - 1);
			} else {
				lew.writeInt(-2);
			}
		}
		lew.writeInt(party.getLeader().getId());
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.getChannel() == forchannel) {
				lew.writeInt(partychar.getMapid());
			} else {
				lew.writeInt(0);
			}
		}
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.getChannel() == forchannel && !leaving) {
				lew.writeInt(partychar.getDoorTown());
				lew.writeInt(partychar.getDoorTarget());
				lew.writeInt(partychar.getDoorPosition().x);
				lew.writeInt(partychar.getDoorPosition().y);
			} else {
				lew.writeInt(0);
				lew.writeInt(0);
				lew.writeInt(0);
				lew.writeInt(0);
			}
		}
	}

	public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op,
											MaplePartyCharacter target) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		switch (op) {
			case DISBAND:
			case EXPEL:
			case LEAVE:
				mplew.write(0xC);
				mplew.writeInt(40546);
				mplew.writeInt(target.getId());

				if (op == PartyOperation.DISBAND) {
					mplew.write(0);
					mplew.writeInt(party.getId());
				} else {
					mplew.write(1);
					if (op == PartyOperation.EXPEL) {
						mplew.write(1);
					} else {
						mplew.write(0);
					}
					mplew.writeMapleAsciiString(target.getName());
					addPartyStatus(forChannel, party, mplew, false);
					// addLeavePartyTail(mplew);
				}

				break;
			case JOIN:
				mplew.write(0xF);
				mplew.writeInt(40546);
				mplew.writeMapleAsciiString(target.getName());
				addPartyStatus(forChannel, party, mplew, false);
				// addJoinPartyTail(mplew);
				break;
			case SILENT_UPDATE:
			case LOG_ONOFF:
				mplew.write(0x7);
				mplew.writeInt(party.getId());
				addPartyStatus(forChannel, party, mplew, false);
				break;

		}
		return mplew.getPacket();
	}

	public static MaplePacket partyPortal(int townId, int targetId, Point position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
		mplew.writeShort(0x22);
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		mplew.writeShort(position.x);
		mplew.writeShort(position.y);
		return mplew.getPacket();
	}

	public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(curhp);
		mplew.writeInt(maxhp);
		return mplew.getPacket();
	}

	/**
	 * mode: 0 buddychat; 1 partychat; 2 guildchat
	 * 
	 * @param name
	 * @param chattext
	 * @param mode
	 * @return
	 */
	public static MaplePacket multiChat(String name, String chattext, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(name);
		mplew.writeMapleAsciiString(chattext);
		return mplew.getPacket();
	}

	public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill,
									boolean monsterSkill, int delay) {
		return applyMonsterStatus(oid, stats, skill, monsterSkill, delay, null);
	}
	
	public static MaplePacket applyMonsterStatusTest(int oid, int mask, int delay, MobSkill mobskill, int value) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
		// 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
		// B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);

		mplew.writeInt(mask);
		mplew.writeShort(1);
		mplew.writeShort(mobskill.getSkillId());
		mplew.writeShort(mobskill.getSkillLevel());
		mplew.writeShort(0); // as this looks similar to giveBuff this
		// might actually be the buffTime but it's
		// not displayed anywhere

		mplew.writeShort(delay); // delay in ms

		mplew.write(1); // ?

		return mplew.getPacket();
	}
	
	public static MaplePacket applyMonsterStatusTest2(int oid, int mask, int skill, int value) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
		// 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
		// B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);

		mplew.writeInt(mask);

		mplew.writeShort(value);
		mplew.writeInt(skill);
		mplew.writeShort(0); // as this looks similar to giveBuff this
		// might actually be the buffTime but it's
		// not displayed anywhere

		mplew.writeShort(0); // delay in ms

		mplew.write(1); // ?

		return mplew.getPacket();
	}
	
	public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill,
									boolean monsterSkill, int delay, MobSkill mobskill) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
		// 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
		// B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);

		int mask = 0;
		for (MonsterStatus stat : stats.keySet()) {
			mask |= stat.getValue();
		}

		mplew.writeInt(mask);

		for (Integer val : stats.values()) {
			mplew.writeShort(val);
			if (monsterSkill) {
				mplew.writeShort(mobskill.getSkillId());
				mplew.writeShort(mobskill.getSkillLevel());
			} else {
				mplew.writeInt(skill);
			}
			mplew.writeShort(0); // as this looks similar to giveBuff this
			// might actually be the buffTime but it's
			// not displayed anywhere

		}

		mplew.writeShort(delay); // delay in ms

		mplew.write(1); // ?

		return mplew.getPacket();
	}

	public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());

		mplew.writeInt(oid);
		int mask = 0;
		for (MonsterStatus stat : stats.keySet()) {
			mask |= stat.getValue();
		}

		mplew.writeInt(mask);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static MaplePacket getClock(int time) { // time in seconds

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
		mplew.write(2); // clock type. if you send 3 here you have to send
		// another byte (which does not matter at all) before
		// the timestamp

		mplew.writeInt(time);
		return mplew.getPacket();
	}

	public static MaplePacket getClockTime(int hour, int min, int sec) { // Current Time

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
		mplew.write(1); //Clock-Type
		mplew.write(hour);
		mplew.write(min);
		mplew.write(sec);
		return mplew.getPacket();
	}

	public static MaplePacket spawnMist(int oid, int ownerCid, int skillId, Rectangle mistPosition, int level) {
		/*
		 * D1 00
		 * 0E 00 00 00 // OID?
		 * 01 00 00 00 // Mist ID
		 * 6A 4D 27 00 // Char ID?
		 * 1B 36 20 00 // Skill ID
		 * 1E
		 * 08 00
		 * 3D FD FF FF
		 * 71 FC FF FF
		 * CD FE FF FF
		 * 9D FD FF FF
		 * 00 00 00 00
		 */
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(oid); // maybe this should actually be the "mistid" -
		// seems to always be 1 with only one mist in
		// the map...

		mplew.writeInt(ownerCid); // probably only intresting for smokescreen

		mplew.writeInt(skillId);
		mplew.write(level); // who knows

		mplew.writeShort(8); // ???

		mplew.writeInt(mistPosition.x); // left position

		mplew.writeInt(mistPosition.y); // bottom position

		mplew.writeInt(mistPosition.x + mistPosition.width); // left position

		mplew.writeInt(mistPosition.y + mistPosition.height); // upper
																// position

		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket removeMist(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
		// 77 00 29 1D 02 00 FA FE 30 00 00 10 00 00 00 BF 70 8F 00 00
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(unkByte);
		mplew.writeInt(damage);
		mplew.writeInt(monsterIdFrom);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static MaplePacket damageMonster(int oid, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(damage);
		mplew.write(0);
		mplew.write(0);
		mplew.write(0);

		return mplew.getPacket();
	}
	
    public static MaplePacket healMonster(int oid, int heal) {
		return damageMonster(oid, -heal);
    }

    public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7); //0x0a seems to be the one for updating, but this one works fine
        mplew.write(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId()); // cid
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
                mplew.write(0); // opposite status
                mplew.writeInt(buddy.getChannel() - 1);
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\0', 13));
                mplew.writeInt(0);
            }

        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        
        return mplew.getPacket();
    }

	public static MaplePacket requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
		mplew.write(9);
		mplew.writeInt(cidFrom);
		mplew.writeMapleAsciiString(nameFrom);
		mplew.writeInt(cidFrom);
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 11));
		mplew.write(0x09);
		mplew.write(0xf0);
		mplew.write(0x01);
		mplew.writeInt(0x0f);
		mplew.writeNullTerminatedAsciiString("Default Group");
		mplew.writeInt(cid);

		return mplew.getPacket();
	}

	public static MaplePacket updateBuddyChannel(int characterid, int channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
		// 2B 00 14 30 C0 23 00 00 11 00 00 00
		mplew.write(0x14);
		mplew.writeInt(characterid);
		mplew.write(0);
		mplew.writeInt(channel);

		// 2B 00 14 30 C0 23 00 00 0D 00 00 00
		// 2B 00 14 30 75 00 00 00 11 00 00 00
		return mplew.getPacket();
	}

	public static MaplePacket itemEffect(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());

		mplew.writeInt(characterid);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static MaplePacket updateBuddyCapacity(int capacity) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
		mplew.write(0x15);
		mplew.write(capacity);

		return mplew.getPacket();
	}

	public static MaplePacket showChair(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());

		mplew.writeInt(characterid);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static MaplePacket cancelChair() {
		return cancelChair(-1);
	}
	
	public static MaplePacket cancelChair(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
		
		if(id == -1){
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeShort(id);
		}

		return mplew.getPacket();
	}
	
	// is there a way to spawn reactors non-animated?
	public static MaplePacket spawnReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();

		mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writeInt(reactor.getId());
		mplew.write(reactor.getState());
		mplew.writeShort(pos.x);
		mplew.writeShort(pos.y);
		mplew.write(0);

		return mplew.getPacket();
	}
	
	public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();

		mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getState());
		mplew.writeShort(pos.x);
		mplew.writeShort(pos.y);
		mplew.writeShort(stance);
		mplew.write(0);

		//frame delay, set to 5 since there doesn't appear to be a fixed formula for it
		mplew.write(5);

		return mplew.getPacket();
	}
	
	public static MaplePacket destroyReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();

		mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getState());
		mplew.writeShort(pos.x);
		mplew.writeShort(pos.y);

		return mplew.getPacket();
	}

	public static MaplePacket musicChange(String song) {
		return environmentChange(song, 6);
	}

	public static MaplePacket showEffect(String effect) {
		return environmentChange(effect, 3);
	}

	public static MaplePacket playSound(String sound) {
		return environmentChange(sound, 4);
	}

	public static MaplePacket environmentChange(String env, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(env);

		return mplew.getPacket();
	}

	public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
		mplew.write(active ? 0 : 1);

		mplew.writeInt(itemid);
		if (active)
			mplew.writeMapleAsciiString(msg);

		return mplew.getPacket();
	}

	public static MaplePacket removeMapEffect() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
		mplew.write(0);
		mplew.writeInt(0);

		return mplew.getPacket();
	}
	
	public static MaplePacket showGuildInfo(MapleCharacter c) {
		//whatever functions calling this better make sure
		//that the character actually HAS a guild
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x1A); //signature for showing guild info

		if (c == null) //show empty guild (used for leaving, expelled)
		{
			mplew.write(0);
			return mplew.getPacket();
		}

		MapleGuildCharacter initiator = c.getMGC();

		MapleGuild g = c.getClient().getChannelServer().getGuild(initiator);

		if (g == null) //failed to read from DB - don't show a guild
		{
			mplew.write(0);
			log.warn(MapleClient.getLogMessage(c, "Couldn't load a guild"));
			return mplew.getPacket();
		} else {
			//MapleGuild holds the absolute correct value of guild rank
			//after it is initiated
			MapleGuildCharacter mgc = g.getMGC(c.getId());
			c.setGuildRank(mgc.getGuildRank());
		}

		mplew.write(1); //bInGuild
		mplew.writeInt(c.getGuildId()); //not entirely sure about this one

		mplew.writeMapleAsciiString(g.getName());

		for (int i = 1; i <= 5; i++)
			mplew.writeMapleAsciiString(g.getRankTitle(i));

		Collection<MapleGuildCharacter> members = g.getMembers();

		mplew.write(members.size());
		//then it is the size of all the members

		for (MapleGuildCharacter mgc : members)
			//and each of their character ids o_O
			mplew.writeInt(mgc.getId());
		
		for (MapleGuildCharacter mgc : members) {
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(g.getSignature());
			mplew.writeInt(3);
		}

		mplew.writeInt(g.getCapacity());
		mplew.writeShort(g.getLogoBG());
		mplew.write(g.getLogoBGColor());
		mplew.writeShort(g.getLogo());
		mplew.write(g.getLogoColor());
		mplew.writeMapleAsciiString(g.getNotice());
		mplew.writeInt(g.getGP());
		mplew.writeInt(0);

		//System.out.println("DEBUG: showGuildInfo packet:\n" + mplew.toString());

		return mplew.getPacket();
	}

	public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3d);
		mplew.writeInt(gid);
		mplew.writeInt(cid);
		mplew.write(bOnline ? 1 : 0);

		return mplew.getPacket();
	}

	public static MaplePacket guildInvite(int gid, String charName) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x05);
		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(charName);

		return mplew.getPacket();
	}

	public static MaplePacket genericGuildMessage(byte code) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(code);

		return mplew.getPacket();
	}

	public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x27);

		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
		mplew.writeInt(mgc.getJobId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
		mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
		mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
		mplew.writeInt(3);

		return mplew.getPacket();
	}

	//someone leaving, mode == 0x2c for leaving, 0x2f for expelled
	public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(bExpelled ? 0x2f : 0x2c);

		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeMapleAsciiString(mgc.getName());

		return mplew.getPacket();
	}

	//rank change
	public static MaplePacket changeRank(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x40);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.write(mgc.getGuildRank());

		return mplew.getPacket();
	}

	public static MaplePacket guildNotice(int gid, String notice) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x44);

		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(notice);

		return mplew.getPacket();
	}

	public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3C);

		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getJobId());

		return mplew.getPacket();
	}

	public static MaplePacket rankTitleChange(int gid, String[] ranks) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3e);
		mplew.writeInt(gid);

		for (int i = 0; i < 5; i++)
			mplew.writeMapleAsciiString(ranks[i]);

		return mplew.getPacket();
	}

	public static MaplePacket guildDisband(int gid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x32);
		mplew.writeInt(gid);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x42);
		mplew.writeInt(gid);
		mplew.writeShort(bg);
		mplew.write(bgcolor);
		mplew.writeShort(logo);
		mplew.write(logocolor);

		return mplew.getPacket();
	}

	public static MaplePacket guildCapacityChange(int gid, int capacity) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3a);
		mplew.writeInt(gid);
		mplew.write(capacity);

		return mplew.getPacket();
	}

	public static void addThread(MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
		mplew.writeInt(rs.getInt("localthreadid"));
		mplew.writeInt(rs.getInt("postercid"));
		mplew.writeMapleAsciiString(rs.getString("name"));
		mplew.writeLong(MaplePacketHelper.getKoreanTimestamp(rs.getLong("timestamp")));
		mplew.writeInt(rs.getInt("icon"));
		mplew.writeInt(rs.getInt("replycount"));
	}

	public static MaplePacket BBSThreadList(ResultSet rs, int start) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
		mplew.write(0x06);

		if (!rs.last())
		//no result at all
		{
			mplew.write(0);
			mplew.writeInt(0);
			mplew.writeInt(0);
			return mplew.getPacket();
		}

		int threadCount = rs.getRow();
		if (rs.getInt("localthreadid") == 0) //has a notice
		{
			mplew.write(1);
			addThread(mplew, rs);
			threadCount--; //one thread didn't count (because it's a notice)
		} else
			mplew.write(0);

		if (!rs.absolute(start + 1)) //seek to the thread before where we start
		{
			rs.first(); //uh, we're trying to start at a place past possible
			start = 0;
			// System.out.println("Attempting to start past threadCount");
		}

		mplew.writeInt(threadCount);
		mplew.writeInt(Math.min(10, threadCount - start));

		for (int i = 0; i < Math.min(10, threadCount - start); i++) {
			addThread(mplew, rs);
			rs.next();
		}

		return mplew.getPacket();
	}

	public static MaplePacket showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS)
																									throws SQLException,
																									RuntimeException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
		mplew.write(0x07);

		mplew.writeInt(localthreadid);
		mplew.writeInt(threadRS.getInt("postercid"));
		mplew.writeLong(MaplePacketHelper.getKoreanTimestamp(threadRS.getLong("timestamp")));
		mplew.writeMapleAsciiString(threadRS.getString("name"));
		mplew.writeMapleAsciiString(threadRS.getString("startpost"));
		mplew.writeInt(threadRS.getInt("icon"));

		if (repliesRS != null) {
			int replyCount = threadRS.getInt("replycount");
			mplew.writeInt(replyCount);

			int i;
			for (i = 0; i < replyCount && repliesRS.next(); i++) {
				mplew.writeInt(repliesRS.getInt("replyid"));
				mplew.writeInt(repliesRS.getInt("postercid"));
				mplew.writeLong(MaplePacketHelper.getKoreanTimestamp(repliesRS.getLong("timestamp")));
				mplew.writeMapleAsciiString(repliesRS.getString("content"));
			}

			if (i != replyCount || repliesRS.next()) {
				//in the unlikely event that we lost count of replyid
				throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
				//we need to fix the database and stop the packet sending
				//or else it'll probably error 38 whoever tries to read it

				//there is ONE case not checked, and that's when the thread 
				//has a replycount of 0 and there is one or more replies to the
				//thread in bbs_replies 
			}
		} else
			mplew.writeInt(0); //0 replies

		return mplew.getPacket();
	}
	
	public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException
	{
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x49);
		mplew.writeInt(npcid);
		if (!rs.last())		//no guilds o.o
		{
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		
		mplew.writeInt(rs.getRow());		//number of entries
		
		rs.beforeFirst();
		while (rs.next())
		{
			mplew.writeMapleAsciiString(rs.getString("name"));
			mplew.writeInt(rs.getInt("GP"));
			mplew.writeInt(rs.getInt("logo"));
			mplew.writeInt(rs.getInt("logoColor"));
			mplew.writeInt(rs.getInt("logoBG"));
			mplew.writeInt(rs.getInt("logoBGColor"));
		}
		
		return mplew.getPacket();
	}
	
	public static MaplePacket updateGP(int gid, int GP)
	{
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x48);
		mplew.writeInt(gid);
		mplew.writeInt(GP);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket skillEffect(MapleCharacter from, int skillId, byte flags) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		mplew.write(0x01); // unknown at this point
		mplew.write(flags);
		mplew.write(0x04); // unknown at this point
		return mplew.getPacket();
	}
	
	public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		return mplew.getPacket();
	}
	
	public static MaplePacket showMagnet(int mobid, byte success) {  // Monster Magnet
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
		mplew.writeInt(mobid);
		mplew.write(success);
		return mplew.getPacket();
	}
	
	/**
	 * Sends a Player Hint, something that pops up above your character!
	 * 
	 * @return The packet.
	 */
	public static MaplePacket sendHint(String hint) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
		mplew.writeMapleAsciiString(hint);
		mplew.write(HexTool.getByteArrayFromHexString("FA 00 05 00 01"));
		
		return mplew.getPacket();
	}
	
	public static MaplePacket messengerInvite(String from, int messengerid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x03);
		mplew.writeMapleAsciiString(from);
		mplew.write(0x00);
		mplew.writeInt(messengerid);
		mplew.write(0x00);
		return mplew.getPacket();
	}
	 
	public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x00);
		mplew.write(position);
		MaplePacketHelper.addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}
	 
	public static MaplePacket removeMessengerPlayer(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x02);
		mplew.write(position);
		return mplew.getPacket();
	}
	 
	public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x07);
		mplew.write(position);
		MaplePacketHelper.addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static MaplePacket joinMessenger(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x01);
		mplew.write(position);
		return mplew.getPacket();
	}
	 
	public static MaplePacket messengerChat(String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x06);
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}
	 
	public static MaplePacket messengerNote(String text, int mode, int mode2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(text);
		mplew.write(mode2);
		return mplew.getPacket();
	}
	
	public static MaplePacket warpCS(MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		MapleCharacter chr = c.getPlayer();
		
		mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
		mplew.writeLong(-1);
		
		MaplePacketHelper.addCharStats(mplew, chr);
		mplew.write(20); // ???
		mplew.writeInt(chr.getMeso()); // mesos
		MaplePacketHelper.addInventoryInfo(mplew, chr);
		MaplePacketHelper.addSkillInfo(mplew, chr);
		
		mplew.write(HexTool.getByteArrayFromHexString("00 00 03 00 E1 50 00 00 DE 6D 01 00 31 2C 27 03 00 69 6E 67 1A 00 21 4E 80 0E 84 D9 67 1C CA 01 DE 50 80 F2 43 F2 6E 1C CA 01 89 4E 00 6E B9 15 6C 1C CA 01 29 27 80 B7 A3 1C 24 1D CA 01 22 4E 80 89 54 5F 69 1C CA 01 DF 50 80 CC 3C 8D 6F 1C CA 01 2A 27 80 B7 A3 1C 24 1D CA 01 E0 50 00 61 CB 66 70 1C CA 01 23 4E 80 37 A1 8C 69 1C CA 01 2B 27 80 B7 A3 1C 24 1D CA 01 24 4E 00 06 D6 A9 69 1C CA 01 84 4E 00 71 75 01 6C 1C CA 01 2C 27 80 B7 A3 1C 24 1D CA 01 25 4E 00 0F 37 9E 6A 1C CA 01 2D 27 80 B7 A3 1C 24 1D CA 01 26 4E 80 85 40 11 6B 1C CA 01 DD 27 80 01 1D BE 6F 1C CA 01 1E 27 80 B7 A3 1C 24 1D CA 01 DC 50 00 9C 9C DB 6C 1C CA 01 27 4E 80 3E F8 59 6B 1C CA 01 1F 27 80 B7 A3 1C 24 1D CA 01 20 4E 00 F5 6B 23 66 1C CA 01 DD 50 00 59 C2 D4 6D 1C CA 01 28 4E 80 7E 43 A6 6B 1C CA 01 10 27 00 67 BC AA 6C 1C CA 01 28 27 80 B7 A3 1C 24 1D CA 01"));
		
		mplew.writeLong(0);
		
		for (int i = 0; i < 15; i++) {
			mplew.write(CHAR_INFO_MAGIC);
		}
		
		mplew.writeInt(0);
		mplew.writeInt(0);//v72
		mplew.writeInt(0);//v72
		mplew.write(0);
        mplew.write(1);
        
        mplew.writeMapleAsciiString(chr.getClient().getAccountName());
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 38 00 01 9F 98 00 00 04 00 00 00 02 9F 98 00 00 04 00 00 00 03 9F 98 00 00 04 00 00 00 04 9F 98 00 00 04 00 00 00 05 9F 98 00 00 04 00 00 00 06 9F 98 00 00 04 00 00 00 07 9F 98 00 00 04 00 00 00 08 9F 98 00 00 04 00 00 00 09 9F 98 00 00 04 00 00 00 0A 9F 98 00 00 04 00 00 00 0B 9F 98 00 00 04 00 00 00 0C 9F 98 00 00 04 00 00 00 0D 9F 98 00 00 04 00 00 00 0E 9F 98 00 00 04 00 00 00 0F 9F 98 00 00 04 00 00 00 6F 24 9A 00 00 04 00 00 00 70 24 9A 00 00 04 00 00 00 71 24 9A 00 00 04 00 00 00 72 24 9A 00 00 04 00 00 00 73 24 9A 00 00 08 00 00 FF 74 24 9A 00 00 08 00 00 FF 78 24 9A 00 00 04 00 00 00 79 24 9A 00 00 04 00 00 00 53 2F 31 01 10 00 00 00 0D 54 2F 31 01 00 04 00 00 00 9B 3A 34 01 00 04 00 00 00 9C 3A 34 01 00 04 00 00 00 11 C2 35 01 14 08 00 00 0C 92 09 00 00 01 42 C2 35 01 10 00 00 00 0B 44 C2 35 01 00 04 00 00 00 45 C2 35 01 00 0C 00 00 00 03 46 C2 35 01 00 0C 00 00 00 03 69 48 37 01 14 08 00 00 0C 92 09 00 00 01 8A 48 37 01 00 04 00 00 00 D3 CE 38 01 14 08 00 00 0C 04 06 00 00 01 07 CF 38 01 00 04 00 00 00 AD 55 3A 01 00 04 00 00 00 AE 55 3A 01 00 04 00 00 00 AF 55 3A 01 00 04 00 00 00 98 62 3D 01 06 08 00 00 0C 00 4E 0C 00 00 01 1B 63 3D 01 10 08 00 00 0B 02 1E 63 3D 01 00 04 00 00 00 1F 63 3D 01 00 04 00 00 00 20 63 3D 01 00 04 00 00 00 FF F5 41 01 14 08 00 00 0C 92 09 00 00 01 3A F6 41 01 00 04 00 00 00 3B F6 41 01 00 0C 00 00 00 03 AE C3 C9 01 00 04 00 00 00 47 77 FC 02 00 08 00 00 FF 48 77 FC 02 00 08 00 00 FF 3C FE FD 02 00 0C 00 00 00 FF 40 FE FD 02 00 04 00 00 00 E7 91 02 03 00 0C 00 00 00 03 4E 87 93 03 00 04 00 00 01 DB 1E 2C 04 00 04 00 00 00 DC 1E 2C 04 00 04 00 00 00 00 73 00 65 00 63 00 2E 00 00 00 0D 00 DF 01 0E 04 0E 00 0E 00 DE 01 0E 04 5C 00 00 00 48 00 50 00 20 00 2D 00 32 00 34 00 2C 00 20 00 4D 00 50 00 20 00 2D 00 32 00 34 00 3B 00 20 00 49 00 6D 00 70 00 72 00 6F 00 76 00 65 00 73 00 20 00 67 00 75 00 6E 00 20 00 73 00 70 00 65 00 65 00 64 00 20 00 66 00 6F 00 72 00 20 00 36 00 30 00 20 00 73 00 65 00 63 00 2E 00 01 00 00 00 00 00 00 00 E6 91 02 03 01 00 00 00 00 00 00 00 FB E8 3E 01 01 00 00 00 00 00 00 00 4F 2F 31 01 01 00 00 00 00 00 00 00 75 24 9A 00 01 00 00 00 00 00 00 00 76 24 9A 00 01 00 00 00 01 00 00 00 E6 91 02 03 01 00 00 00 01 00 00 00 FB E8 3E 01 01 00 00 00 01 00 00 00 4F 2F 31 01 01 00 00 00 01 00 00 00 75 24 9A 00 01 00 00 00 01 00 00 00 76 24 9A 00 02 00 00 00 00 00 00 00 E6 91 02 03 02 00 00 00 00 00 00 00 FB E8 3E 01 02 00 00 00 00 00 00 00 4F 2F 31 01 02 00 00 00 00 00 00 00 75 24 9A 00 02 00 00 00 00 00 00 00 76 24 9A 00 02 00 00 00 01 00 00 00 E6 91 02 03 02 00 00 00 01 00 00 00 FB E8 3E 01 02 00 00 00 01 00 00 00 4F 2F 31 01 02 00 00 00 01 00 00 00 75 24 9A 00 02 00 00 00 01 00 00 00 76 24 9A 00 03 00 00 00 00 00 00 00 E6 91 02 03 03 00 00 00 00 00 00 00 FB E8 3E 01 03 00 00 00 00 00 00 00 4F 2F 31 01 03 00 00 00 00 00 00 00 75 24 9A 00 03 00 00 00 00 00 00 00 76 24 9A 00 03 00 00 00 01 00 00 00 E6 91 02 03 03 00 00 00 01 00 00 00 FB E8 3E 01 03 00 00 00 01 00 00 00 4F 2F 31 01 03 00 00 00 01 00 00 00 75 24 9A 00 03 00 00 00 01 00 00 00 76 24 9A 00 04 00 00 00 00 00 00 00 E6 91 02 03 04 00 00 00 00 00 00 00 FB E8 3E 01 04 00 00 00 00 00 00 00 4F 2F 31 01 04 00 00 00 00 00 00 00 75 24 9A 00 04 00 00 00 00 00 00 00 76 24 9A 00 04 00 00 00 01 00 00 00 E6 91 02 03 04 00 00 00 01 00 00 00 FB E8 3E 01 04 00 00 00 01 00 00 00 4F 2F 31 01 04 00 00 00 01 00 00 00 75 24 9A 00 04 00 00 00 01 00 00 00 76 24 9A 00 05 00 00 00 00 00 00 00 E6 91 02 03 05 00 00 00 00 00 00 00 FB E8 3E 01 05 00 00 00 00 00 00 00 4F 2F 31 01 05 00 00 00 00 00 00 00 75 24 9A 00 05 00 00 00 00 00 00 00 76 24 9A 00 05 00 00 00 01 00 00 00 E6 91 02 03 05 00 00 00 01 00 00 00 FB E8 3E 01 05 00 00 00 01 00 00 00 4F 2F 31 01 05 00 00 00 01 00 00 00 75 24 9A 00 05 00 00 00 01 00 00 00 76 24 9A 00 06 00 00 00 00 00 00 00 E6 91 02 03 06 00 00 00 00 00 00 00 FB E8 3E 01 06 00 00 00 00 00 00 00 4F 2F 31 01 06 00 00 00 00 00 00 00 75 24 9A 00 06 00 00 00 00 00 00 00 76 24 9A 00 06 00 00 00 01 00 00 00 E6 91 02 03 06 00 00 00 01 00 00 00 FB E8 3E 01 06 00 00 00 01 00 00 00 4F 2F 31 01 06 00 00 00 01 00 00 00 75 24 9A 00 06 00 00 00 01 00 00 00 76 24 9A 00 07 00 00 00 00 00 00 00 E6 91 02 03 07 00 00 00 00 00 00 00 FB E8 3E 01 07 00 00 00 00 00 00 00 4F 2F 31 01 07 00 00 00 00 00 00 00 75 24 9A 00 07 00 00 00 00 00 00 00 76 24 9A 00 07 00 00 00 01 00 00 00 E6 91 02 03 07 00 00 00 01 00 00 00 FB E8 3E 01 07 00 00 00 01 00 00 00 4F 2F 31 01 07 00 00 00 01 00 00 00 75 24 9A 00 07 00 00 00 01 00 00 00 76 24 9A 00 08 00 00 00 00 00 00 00 E6 91 02 03 08 00 00 00 00 00 00 00 FB E8 3E 01 08 00 00 00 00 00 00 00 4F 2F 31 01 08 00 00 00 00 00 00 00 75 24 9A 00 08 00 00 00 00 00 00 00 76 24 9A 00 08 00 00 00 01 00 00 00 E6 91 02 03 08 00 00 00 01 00 00 00 FB E8 3E 01 08 00 00 00 01 00 00 00 4F 2F 31 01 08 00 00 00 01 00 00 00 75 24 9A 00 08 00 00 00 01 00 00 00 76 24 9A 00 00 00 00 00 00 00 00 41 00 00 00"));
		
		return mplew.getPacket();
	}
	
	public static void toCashItem(MaplePacketLittleEndianWriter mplew, int sn, int type1, int type2) {
		// E1 9C 98 00 00 06 00 00 00 - Globe Cap
		mplew.writeInt(sn);
		mplew.write(0);
		mplew.write(type1);
		mplew.writeShort(0);
		mplew.write(type2);
	}

	public static void toCashItem(MaplePacketLittleEndianWriter mplew, int sn, int type0, int type1, int type2) {
		mplew.writeInt(sn);
		mplew.write(type0);
		mplew.write(type1);
		mplew.writeShort(0);
		mplew.write(type2);
	}
	
	public static MaplePacket showNXMapleTokens(MapleCharacter chr) {
		
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
		mplew.writeInt(chr.getCSPoints(1)); // Paypal NX
		mplew.writeInt(chr.getCSPoints(2)); // Maple Points
		mplew.writeInt(chr.getCSPoints(4)); // Card NX
		
		return mplew.getPacket();
	}
	
	public static MaplePacket showBoughtCSItem(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		
		//23 01 
		//4A 
		//FA 96 C1 00 
		//00 00 00 00 
		//28 8A 64 00 
		//00 00 00 00 
		//EC 4A 0F 00 
		//15 2D 31 01 01 00 00 00 50 4C 40 00 B4 F9 78 00 00 00 00 C0 1E CC C5 A4 73 CA 01 00 00 00 00 00 00 00 00
		mplew.write(0x4a); //v75
		//mplew.write(0x49); //v74
		mplew.write(HexTool.getByteArrayFromHexString("FA 96 C1 00"));
		mplew.writeInt(0);
		mplew.write(HexTool.getByteArrayFromHexString("28 8A 64 00"));
		mplew.writeInt(0);
		mplew.writeInt(itemid);
		mplew.write(HexTool.getByteArrayFromHexString("15 2D 31 01 01 00 00 00 50 4C 40 00 B4 F9 78 00 00 00 00 C0 1E CC C5 A4 73 CA 01"));
		mplew.writeLong(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket showBoughtCSItem(int itemid, int sn, int uniqueid, int quantity, int accid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x4a); //use to be 49
		mplew.writeInt(uniqueid);
		mplew.writeInt(0);
		mplew.writeInt(accid);
		mplew.writeInt(0);
		mplew.writeInt(itemid);
		mplew.writeInt(sn);
		mplew.writeInt(quantity);
		mplew.writeInt(4213840); //??!
		mplew.writeInt(7828244); //?!!?!?
		mplew.writeShort(0);
		mplew.write(0);
		mplew.writeLong(MaplePacketHelper.getKoreanTimestamp((long) System.currentTimeMillis()));
		mplew.writeLong(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket showCouponRedeemedItem(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		//mplew.writeShort(0x3A);
		mplew.writeShort(0x49); //v72 (maybe...)
		mplew.writeInt(0);
		mplew.writeInt(1);
		mplew.writeShort(1);
		mplew.writeShort(0x1A);
		mplew.writeInt(itemid);
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket enableCSUse0() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.write(HexTool.getByteArrayFromHexString("12 00 00 00 00 00 00"));
		
		return mplew.getPacket();
	}
	
	public static MaplePacket enableCSUse1() { //enablecsuse1 is cs inventory packet + storageslot/characterslot
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		
		mplew.write(0x40); //v75
		//mplew.write(0x3f); //v74
		mplew.writeShort(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket enableCSUse2() { //enablecsuse2 is cs gift packet ^_^ messages
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// FE 00 2F 00 00 04 00 03 00
		
		//23 01 
		//3E 
		//09 00 
		//C8 A5 9E 00 
		//00 00 00 00 
		//28 8A 64 00 
		//00 00 00 00 
		//99 34 10 00 
		//00 00 00 00 
		//01 
		//00 00 
		//30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 68 CE 38 01 00 00 00 00 C9 A5 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 D1 E6 0F 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 5E 48 37 01 00 00 00 00 62 A6 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 46 4B 4C 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 0E 87 93 03 1E 00 00 00 67 A6 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 23 A6 1B 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 C6 0D 95 03 00 00 00 00 68 A6 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 22 A6 1B 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 C4 0D 95 03 00 00 00 00 82 BA 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 20 6E 4E 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 20 77 FC 02 00 00 00 00 EA BA 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 D3 F8 19 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 53 62 3D 01 00 00 00 00 F5 BA 9E 00 00 00 00 00 28 8A 64 00 00 00 00 00 CA 4A 0F 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 2B 2D 31 01 00 00 00 00 F5 53 B1 00 00 00 00 00 28 8A 64 00 00 00 00 00 A8 69 52 00 00 00 00 00 01 00 00 30 B0 E9 86 0A 5F 54 40 00 90 8E B0 F0 F9 4A 4A 3F E4 C9 01 41 18 04 03 00 00 00 00 04 00 04 00
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.writeShort(0x3e); //v75
		//mplew.writeShort(0x3d); //v74
		mplew.write(0);
		mplew.writeShort(4);
		mplew.writeShort(3);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket enableCSUse3() { //enablecsuse3 is wishlist packet
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x42); //v75
		//mplew.write(0x41); //v74
		mplew.write(new byte[40]);
		
		return mplew.getPacket();
	}
	
	// Decoding : Raz (Snow) | Author : Penguins (Acrylic)
	public static MaplePacket sendWishList(int characterid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(0xEF);
		mplew.write(0x30);

		Connection con = DatabaseConnection.getConnection();
		int i = 10;

		try {
		    PreparedStatement ps = con.prepareStatement("SELECT sn FROM wishlist WHERE charid = ? LIMIT 10");
		    ps.setInt(1, characterid);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
			    mplew.writeInt(rs.getInt("sn"));
		    }
		    rs.close();
		    ps.close();
		} catch (SQLException se) {
		    System.out.println("Sql Error with wishlists");
		}

		while (i > 0) {
			mplew.writeInt(0);
			i--;
		}
		return mplew.getPacket();
	}

	public static MaplePacket wrongCouponCode() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// FE 00 40 87
		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x40);
		mplew.write(0x87);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket getFindReplyWithCS(String target) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(target);
		mplew.write(2);
		mplew.writeInt(-1);
		return mplew.getPacket();
	}
	
	public static MaplePacket updatePet(MaplePet pet, boolean alive) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(0);
		mplew.write(2);
		mplew.write(3);
		mplew.write(5);
		mplew.write(pet.getPosition());
		mplew.writeShort(0);
		mplew.write(5);
		mplew.write(pet.getPosition());
		mplew.write(0);
		mplew.write(3);
		mplew.writeInt(pet.getItemId());
		mplew.write(1);
		mplew.writeInt(pet.getUniqueId());
		mplew.writeInt(0);
		mplew.write(HexTool.getByteArrayFromHexString("00 40 6f e5 0f e7 17 02"));
		String petname = pet.getName();
		if (petname.length() > 13) {
			petname = petname.substring(0, 13);
		}
		mplew.writeAsciiString(petname);
		for (int i = petname.length(); i < 13; i++) {
			mplew.write(0);
		}
		mplew.write(pet.getLevel());
		mplew.writeShort(pet.getCloseness());
		mplew.write(pet.getFullness());
		if(alive) {
			mplew.writeLong(MaplePacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
			mplew.writeInt(0);
		} else {
			mplew.write(0);
			mplew.write(ITEM_MAGIC);
			mplew.write(HexTool.getByteArrayFromHexString("bb 46 e6 17 02 00 00 00 00"));
		}
		mplew.write(HexTool.getByteArrayFromHexString("50 46 00 00")); //wonder what this is
		return mplew.getPacket();
	}
	
	public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove) {
		return showPet(chr, pet, remove, false);
	}
	
	public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			
		mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
			
		mplew.writeInt(chr.getId());
		mplew.write(chr.getPetIndex(pet));
		if (remove) {
			mplew.write(0);
			mplew.write(hunger ? 1 : 0);
		} else {
			mplew.write(1);
			
			mplew.write(0);

			mplew.writeInt(pet.getItemId());
			mplew.writeMapleAsciiString(pet.getName());
			mplew.writeInt(pet.getUniqueId());

			mplew.writeInt(0);

			mplew.writeShort(pet.getPos().x);
			mplew.writeShort(pet.getPos().y);

			mplew.write(pet.getStance());
			mplew.writeInt(pet.getFh());
		}
		return mplew.getPacket();
	}
	
	public static MaplePacket movePet(int cid, int pid, int slot, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_PET.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.writeInt(pid);

		serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}
	
	public static MaplePacket petChat(int cid, int un, String text, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.writeShort(un);
		mplew.writeMapleAsciiString(text);
		mplew.write(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket commandResponse(int cid, byte command, int slot, boolean success, boolean food) {
		// 84 00 09 03 2C 00 00 00 19 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// 84 00 E6 DC 17 00 00 01 00 00
		mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		if (!food) {
			mplew.write(0);
		}
		
		mplew.write(command);
		if (success) {
			mplew.write(1);
		} else {
			mplew.write(0);
		}
		mplew.write(0);
		
		return mplew.getPacket();
	}

	public static MaplePacket showOwnPetLevelUp(int index) {
	    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
	    mplew.write(4);
	    mplew.write(0);
	    mplew.write(index);//Pet Index

	    return mplew.getPacket();
	}
    
        public static MaplePacket showPetLevelUp(MapleCharacter chr, int index) {
	    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
	    mplew.writeInt(chr.getId());
	    mplew.write(4);
	    mplew.write(0);
	    mplew.write(index);
		
	    return mplew.getPacket();
	}
	
	public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
		// 82 00 E6 DC 17 00 00 04 00 4A 65 66 66 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(0);
		mplew.writeMapleAsciiString(newname);
		mplew.write(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket petStatUpdate(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());

		int mask = 0;
		mask |= MapleStat.PET.getValue();

		mplew.write(0);
		mplew.writeInt(mask);

		MaplePet[] pets = chr.getPets();
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				mplew.writeInt(pets[i].getUniqueId());
				mplew.writeInt(0);
			} else {
				mplew.writeLong(0);
			}
		}

		mplew.write(0);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket weirdStatUpdate() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
		mplew.write(0);
		mplew.write(8);
		mplew.write(0);
		mplew.write(0x18);
		mplew.writeLong(0);
		mplew.writeLong(0);
		mplew.writeLong(0);
		mplew.write(0);
		mplew.write(1);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket showApple() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_APPLE.getValue());
		return mplew.getPacket();
	}
	
	public static MaplePacket skillCooldown(int sid, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
		
		mplew.writeInt(sid);
		mplew.writeShort(time);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket skillBookSuccess(MapleCharacter chr, int skillid,
						int maxlevel, boolean canuse, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
		mplew.writeInt(chr.getId()); // character id
		mplew.write(1);
		mplew.writeInt(skillid);
		mplew.writeInt(maxlevel);
		mplew.write(canuse ? 1 : 0);
		mplew.write(success ? 1 : 0);		
		return mplew.getPacket();
	}
	
	public static MaplePacket getMacros(SkillMacro[] macros) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
		int count = 0;
		for (int i = 0; i < 5; i++) {
			if (macros[i] != null) {
				count++;
			}
		}
		mplew.write(count); // number of macros

		for (int i = 0; i < 5; i++) {
			SkillMacro macro = macros[i];
			if (macro != null) {
				mplew.writeMapleAsciiString(macro.getName());
				mplew.write(macro.getShout());
				mplew.writeInt(macro.getSkill1());
				mplew.writeInt(macro.getSkill2());
				mplew.writeInt(macro.getSkill3());
			}
		}

		return mplew.getPacket();
	}
	
	public static MaplePacket getPlayerNPC(int id) {
		/*
		 * Dear LoOpEdd,
         * 
         * Even though you are a die-hard legit, you have
         * just assisted the private server community by
         * letting me packet sniff your player NPC
         * 
         * Sincerely,
         * Acrylic
		 */
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
		
		Connection con = DatabaseConnection.getConnection();
		   
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
         	   mplew.write(rs.getByte("dir"));
         	   mplew.writeShort(rs.getInt("x"));
         	   mplew.writeShort(rs.getInt("y"));
         	   mplew.writeMapleAsciiString(rs.getString("name"));
         	   mplew.write(0);
         	   mplew.write(rs.getByte("skin"));
         	   mplew.writeInt(rs.getInt("face"));
         	   mplew.write(0);
         	   mplew.writeInt(rs.getInt("hair"));
         	   /*
         	    * 01 // hat
				* CA 4A 0F 00 // 1002186 - transparent hat
				* 03 // eye accessory
				* 4F 98 0F 00 // 1022031 - white toy shades
				* 04 // earring
				* 58 BF 0F 00 // 1032024 - transparent earrings
				* 05 // top
				* D1 E6 0F 00 // 1042129 - ? unknown top maybe?
				* 06 // bottom
				* 9F 34 10 00 // 1062047 - brisk (pants)
				* 07 // shoes
				* 82 5C 10 00 // 1072258 - kitty slippers
				* 08 // gloves
				* 01 83 10 00 // 1082113 - hair cutter gloves
				* 09 // cape
				* D7 D0 10 00 // 1102039 - transparent cape
				* 0B // weapon
				* 00 76 16 // ?
				* 00
				* FF FF
				* D3 F8 19 00 // 1702099 - transparent claw
				* 00 00 00 00
				* 00 00 00 00
				* 00 00 00 00
         	    */
         	   //mplew.write(HexTool.getByteArrayFromHexString("01 CA 4A 0F 00 03 4F 98 0F 00 04 58 BF 0F 00 05 D1 E6 0F 00 06 9F 34 10 00 07 82 5C 10 00 08 01 83 10 00 09 D7 D0 10 00 0B 00 76 16 00 FF FF D3 F8 19 00 00 00 00 00 00 00 00 00 00 00 00 00"));//
            }
            rs.close();
            ps.close();
		 } catch (SQLException se) {}
		 
		 try {
				PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE npcid = ? AND type = 0");
	            ps.setInt(1, id);
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	         	   		mplew.write(rs.getByte("equippos"));
	         	   		mplew.writeInt(rs.getInt("equipid"));
	            }
	            rs.close();
	            ps.close();
		 } catch (SQLException se) {}
		 
		 mplew.writeShort(-1);
		 
		 int count = 0;
		 
		 try {
				PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE npcid = ? AND type = 1");
	            ps.setInt(1, id);
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	         	   		mplew.writeInt(rs.getInt("equipid"));
	         	   		count += 1;
	            }
	            rs.close();
	            ps.close();
		 } catch (SQLException se) {}
		 
		 while (count < 4) {
			 mplew.writeInt(0);
			 count += 1;
		 }

		 return mplew.getPacket();
	}
	
	public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());

		mplew.write(2);
		mplew.write(count);
		
		for (int i=0; i<count; i++) {
		      mplew.writeInt(notes.getInt("id"));
		      mplew.writeMapleAsciiString(notes.getString("from"));
		      mplew.writeMapleAsciiString(notes.getString("message"));
		      mplew.writeLong(MaplePacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
		      mplew.write(0);
		      notes.next();
		}
		
		return mplew.getPacket();
	}
	
	public static void sendUnkwnNote(String to, String msg, String from) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
		ps.setString(1, to);
		ps.setString(2, from);
		ps.setString(3, msg);
		ps.setLong(4, System.currentTimeMillis());
		ps.executeUpdate();
		ps.close();
	}
	
    public static MaplePacket getStatusMsg(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(7);
		mplew.writeInt(itemid);
	
		return mplew.getPacket();
    }
    
    public static MaplePacket enableReport(boolean enable) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.ENABLE_REPORT.getValue());
		mplew.write(enable ? 1 : 0);
	
		return mplew.getPacket();
    }
    
    public static MaplePacket reportResponse(byte mode, int remainingReports) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	
    	//[2A 00]
    	//[02]
    	//[01]
    	//[06 00 00 00]
    	
    	//[2A 00]
    	//[43]
    	
		mplew.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
		mplew.write(mode);
		
		if (mode == 2) {
			mplew.write(1); //does this change?
			mplew.writeInt(remainingReports);
		}
	
		return mplew.getPacket();
    }
    
    public static MaplePacket giveDash(List<Pair<MapleBuffStat, Integer>> statups, int skillid, int x, int y, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeInt(0);
        mplew.writeLong(getLongMask(statups));
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(x);
        mplew.writeInt(skillid);
        mplew.write(new byte[5]);
        mplew.writeShort(duration);
        mplew.writeInt(y);
        mplew.writeInt(skillid);
        mplew.write(new byte[5]);
        mplew.writeShort(duration);
        mplew.writeShort(0);
        mplew.write(0x02);
        
        return mplew.getPacket();
    }

    public static MaplePacket showDashEffecttoOthers(int skillid, int cid, int x, int y, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0); //v74
        mplew.write(HexTool.getByteArrayFromHexString("30 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        mplew.writeInt(x);
        mplew.writeInt(skillid);
        mplew.write(new byte[5]);
        mplew.writeShort(duration);
        mplew.writeInt(y);
        mplew.writeInt(skillid);
        mplew.write(new byte[5]);
        mplew.writeShort(duration);
        mplew.writeShort(0);
        
        return mplew.getPacket();
    }
    
    public static MaplePacket giveInfusion(int skillid, int bufflength, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.MORPH.getValue()); //transform buffstat
        mplew.writeShort(speed);
        mplew.writeInt(skillid);
        mplew.writeLong(0);
        mplew.writeShort(bufflength);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static MaplePacket giveForeignInfusion(int skillid, int cid, int speed, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.MORPH.getValue()); //transform buffstat
        mplew.writeShort(0);
        mplew.writeInt(speed);
        mplew.writeInt(skillid);
        mplew.writeLong(0);
        mplew.writeInt(duration);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static MaplePacket giveEnergyCharge(int barammount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeInt(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue()); //energy charge buffstat
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(barammount); // 0=no bar, 10000=full bar
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeInt(50);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignEnergyCharge(int cid, int barammount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue()); //energy charge buffstat
        mplew.writeShort(0);
        mplew.writeShort(barammount); // 0=no bar, 10000=full bar
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    public static MaplePacket addCard(boolean full, int cardid, int level) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());
		mplew.write(full ? 0 : 1);
		mplew.writeInt(cardid);
		mplew.writeInt(level);
	
		return mplew.getPacket();
    }

    public static MaplePacket showGainCard() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x0D);
	
		return mplew.getPacket();
    }

    public static MaplePacket showForeginCardEffect(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(id);
		mplew.write(0x0D);
	
		return mplew.getPacket();
    }

    public static MaplePacket changeCover(int cardid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.MONSTERBOOK_CHANGE_COVER.getValue());
		mplew.writeInt(cardid);
	
		return mplew.getPacket();
    }
    
    public static MaplePacket showCygnusIntro(int id) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    	mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    	mplew.write(0x12);
    	mplew.writeMapleAsciiString("Effect/Direction.img/cygnus/Scene" + id);

    	return mplew.getPacket();
    }
    
    public static MaplePacket hideUI(boolean hide) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	
    	mplew.writeShort(SendPacketOpcode.HIDE_UI.getValue());
    	mplew.write(hide ? (byte)1 : (byte)0);
    	
    	return mplew.getPacket();
    }
    
    public static MaplePacket disableMovement(boolean disable) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	
    	mplew.writeShort(SendPacketOpcode.DISABLE_MOVEMENT.getValue());
    	mplew.write(disable ? (byte)1 : (byte)0);
    	
    	return mplew.getPacket();
    }
    
    public static MaplePacket cygnusCharCreate() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
    	mplew.writeShort(SendPacketOpcode.CREATE_CYGNUS.getValue());
    	
    	return mplew.getPacket();
    }
    
    /**
	 * Gets cygnus character creation confirmation packet
	 * 
	 * Possible values for <code>mode</code>:<br>
	 * 0: The character has been created.<br>
	 * 1: Character name is in use.<br>
	 * 2: You have ran out of character slots, please purchase more from the cash shop.<br>
	 * 3: This name cannot be used.<br>
	 * 
	 * @param mode The mode
	 * @return The cygnus character creation confirmation packet.
	 */
    public static MaplePacket cygnusCharacterCreated(int mode) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	
    	mplew.writeShort(SendPacketOpcode.CYGNUS_CHAR_CREATED.getValue());
    	mplew.writeInt(mode);
    	
    	return mplew.getPacket();
    }
    
    /**
	 * Gets a special effect packet
	 * 
	 * Possible values for <code>value</code>:<br>
	 * 7: Enter portal sound<br>
	 * 8: Job change<br>
	 * 9: Quest complete<br>
	 * 14: Monster book pickup<br>
	 * 16: ??<br>
	 * 17: Equipment levelup<br>
	 * 19: Exp card<br>
	 * 
	 * @param value The value
	 * @return The special effect packet.
	 */
    public static MaplePacket showSpecialEffect(int value) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	
    	mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    	mplew.write(value);
    	
    	return mplew.getPacket();
    }
    
    public static MaplePacket getFamilyData(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAMILY.getValue());
        mplew.writeInt(11); // Number of events

        mplew.write(0);
        mplew.writeInt(300); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("Transfer to Family Member");
        mplew.writeMapleAsciiString("[Target] Myself\n[Effect] Will be transfered directly to the Map where the family member is located in.");

        mplew.write(1);
        mplew.writeInt(500); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("Summon family member");
        mplew.writeMapleAsciiString("[Target] 1 Family member\n[Effect] Summons one of the family member to the map you are located in.");

        mplew.write(2);
        mplew.writeInt(700); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("1.5 X Drop Rate for Me(15min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 15 min\n[Effect]  Drop rate will be #cincreased by 50%#.\nThe effect will be disregarded if overlapped with other drop rate event.");

        mplew.write(3);
        mplew.write(800); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("1.5 X EXP for me(15min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 15min\n[Effect] EXP gained from monsters  will be #cincreased by 50%.#\nThe effect will be disregarded if overlapped with other EXP event.");

        mplew.write(4);
        mplew.write(1000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("Unity of Family(30min)");
        mplew.writeMapleAsciiString("[Condition] 6 juniors online from pedigree\n[Duration] 30min\n[Effect] Drop Rate and EXP gained will be #cincreased by 100%#.\nThe effect will be disregarded if overlapped with other Drop Rate and EXP event.");

        mplew.write(2);
        mplew.write(1200); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X Drop Rate for Me(15min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 15min\n[Effect]  Drop rate will be #cincreased by 100%.# \nThe effect will be disregarded if overlapped with other Drop Rate event.");

        mplew.write(3);
        mplew.write(1500); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X EXP event for Me(15min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 15min\n[Effect] EXP gained from monsters  will be #cincreased by 100%.#\nThe effect will be disregarded if overlapped with other EXP event.");

        mplew.write(2);
        mplew.write(2000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X Drop Rate for Me(30min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 30min\n[Effect]  drop rate will be #cincreased by 100%.# \nThe effect will be disregarded if overlapped with other Drop Rate event");

        mplew.write(3);
        mplew.write(2500); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X EXP event for Me(30min)");
        mplew.writeMapleAsciiString("[Target] Myself\n[Duration] 30min\n[Effect] EXP gained from monsters  will be #cincreased by 100%.#\nThe effect will be disregarded if overlapped with other EXP event.");

        mplew.write(2);
        mplew.write(4000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X Drop Rate for Party(30min)");
        mplew.writeMapleAsciiString("[Target] My Party\n[Duration] 30min\n[Effect]  drop rate will be #cincreased by 100%.# \nThe effect will be disregarded if overlapped with other Drop Rate event.");

        mplew.write(3);
        mplew.write(5000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("2 X EXP event for Party(30min)");
        mplew.writeMapleAsciiString("[Target] My Party\n[Duration] 30min\n[Effect] EXP gained from monsters  will be #cincreased by 100%.#\nThe effect will be disregarded if overlapped with other EXP event.");

        return mplew.getPacket();
    }
    
    /**
	 * Gets a "block" packet (ie. the cash shop is unavailable, etc)
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 1: You cannot move that channel. Please try again later.<br>
	 * 2: You cannot go into the cash shop. Please try again later.<br>
	 * 3: The Item-Trading shop is currently unavailable, please try again later.<br>
	 * 4: You cannot go into the trade shop, due to the limitation of user count.<br>
	 * 5: You do not meet the minimum level requirement to access the Trade Shop.<br>
	 * 
	 * @param type The type
	 * @return The "block" packet.
	 */
	public static MaplePacket blockedMessage(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_BLOCKED.getValue());
		mplew.write(type);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket updateDojoStats(int points, int belt, boolean tut) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(0x0a);
		mplew.write(HexTool.getByteArrayFromHexString("B7 04")); //?
		mplew.writeMapleAsciiString("pt=" + points + ";belt=" + belt + ";tuto=" + (tut ? "1" : "0"));
		
		return mplew.getPacket();
	}
	
	/**
	*
	* @param type - (0:Light&Long 1:Heavy&Short)
	* @param delay - seconds
	* @return
	*/
	public static MaplePacket trembleEffect(int type, int delay) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(1);
		mplew.write(type);
		mplew.writeInt(delay);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket getEnergy(int level) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
		mplew.writeMapleAsciiString("energy");
		mplew.writeMapleAsciiString(Integer.toString(level));
		
		return mplew.getPacket();
	}

    public static MaplePacket Mulung_DojoUp2() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	
		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(7);
	
		return mplew.getPacket();
    }

    public static MaplePacket dojoWarpUp() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.DOJO_WARP_UP.getValue());
		mplew.write(0);
		mplew.write(6);
	
		return mplew.getPacket();
    }
    
    public static MaplePacket yellowNotice(String message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.YELLOW_MESSAGE.getValue());
		mplew.write(5);
		mplew.writeMapleAsciiString(message);
	
		return mplew.getPacket();
    }
    
    public static MaplePacket sendHammerData(int hammerUsed) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x34);
		mplew.writeInt(0);
		mplew.writeInt(hammerUsed);
		
		return mplew.getPacket();
	}
	
	public static MaplePacket sendHammerMessage() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x38);
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}
	
	/**
	* Gets a gm effect packet (ie. hide, banned, etc.)
	* 
	* Possible values for <code>type</code>:<br>
	* 4: You have successfully blocked access.<br>
	* 5: The unblocking has been successful.<br>
	* 6 with Mode 0: You have successfully removed the name from the ranks.<br>
	* 6 with Mode 1: You have entered an invalid character name.<br>
	* 9: You have either entered a wrong NPC name or<br>
	* 16: GM Hide, mode determines whether or not it is on.<br>
	* 19: Hired merchant CH. 1<br>
	* 26: Enables minimap<br>
	* 27: Disables minimap<br>
	* 29 with Mode 0: Unable to send the message. Please enter the user's name before warning.<br>
	* 29 with Mode 1: Your warning has been successfully sent.<br>
	* 
	* @param type The type
	* @param mode The mode
	* @return The gm effect packet
	*/
    public static MaplePacket getGMEffect(int type, int mode)
    {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(type);
        if (type != 9 && type != 13)
            mplew.write(mode);
        else if (type == 13)
            mplew.writeShort(mode);
        else
            mplew.writeInt(mode);

        return mplew.getPacket();
    }
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket spawnHiredMerchant(MapleHiredMerchant merchant) {
		//[CA 00] [31 6B 0F 00] [71 C0 4C 00] 12 03 6E FF 2F 00 [0B 00 64 75 73 74 72 65 6D 6F 76 65 72] [05] [80 03 00 00] [0A 00 46 72 65 65 20 53 74 75 66 66] [01] [01] [04]
		//[CF 00] [31 6B 0F 00] 05 EE 33 00 00 1D 00 4F 68 20 68 61 69 20 67 75 69 65 73 2C 20 69 20 72 20 67 65 74 20 70 61 63 6B 65 74 7A 01 03 04
		//[CA 00] [78 F7 12 00] [71 C0 4C 00] 2D 02 E6 FF 23 00 [0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65] [05] [4A 07 00 00] [03 00 31 32 33] [01] [01] [04]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
		mplew.writeInt(merchant.getMerchantId());
		mplew.writeInt(merchant.getItemId());
		mplew.writeShort(merchant.getX());
		mplew.writeShort(merchant.getY());
		mplew.writeShort(merchant.getFoothold());
		mplew.writeMapleAsciiString(merchant.getOwnerName());
		mplew.write(5);
		mplew.writeInt(merchant.getObjectId());
		mplew.writeMapleAsciiString(merchant.getDescription());
		mplew.write((merchant.getItemId() - 5030000));
		mplew.write(1);
		mplew.write(4);
		
		return mplew.getPacket();
	}*/
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket updateHiredMerchant(MapleHiredMerchant merchant, int status) {
		//[CD 00] [5D E2 40 00] 71 C0 4C 00 02 01 5E 00 0F 00 0B 00 78 6F 78 78 47 6F 64 78 78 6F 78 05 EE 33 00 00 1D 00 4F 68 20 68 61 69 20 67 75 69 65 73 2C 20 69 20 72 20 67 65 74 20 70 61 63 6B 65 74 7A 01 00 04
		//[CF 00] [5D E2 40 00] [05] [EE 33 00 00] [1D 00 4F 68 20 68 61 69 20 67 75 69 65 73 2C 20 69 20 72 20 67 65 74 20 70 61 63 6B 65 74 7A] 01 03 04
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue());
		mplew.writeInt(merchant.getMerchantId());
		mplew.write(5);
		mplew.writeInt(merchant.getObjectId());
		mplew.writeMapleAsciiString(merchant.getDescription());
		mplew.write(1);
		mplew.write(status);
		mplew.write(4);
		
		return mplew.getPacket();
	}*/
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket removeHiredMerchant(MapleHiredMerchant merchant) {
		//[CA 00] [31 6B 0F 00] [71 C0 4C 00] 12 03 6E FF 2F 00 [0B 00 64 75 73 74 72 65 6D 6F 76 65 72] [05] [80 03 00 00] [0A 00 46 72 65 65 20 53 74 75 66 66] [01] [01] [04]
		//[CA 00] [78 F7 12 00] [71 C0 4C 00] 2D 02 E6 FF 23 00 [0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65] [05] [4A 07 00 00] [03 00 31 32 33] [01] [01] [04]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.write(HexTool.getByteArrayFromHexString("CB 00"));
		mplew.writeInt(merchant.getMerchantId());
		
		return mplew.getPacket();
	}*/
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket closeHiredMerchant() {
		//[CA 00] [31 6B 0F 00] [71 C0 4C 00] 12 03 6E FF 2F 00 [0B 00 64 75 73 74 72 65 6D 6F 76 65 72] [05] [80 03 00 00] [0A 00 46 72 65 65 20 53 74 75 66 66] [01] [01] [04]
		//[CA 00] [78 F7 12 00] [71 C0 4C 00] 2D 02 E6 FF 23 00 [0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65] [05] [4A 07 00 00] [03 00 31 32 33] [01] [01] [04]
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.write(HexTool.getByteArrayFromHexString("F5 00 0A 00 10"));
		
		return mplew.getPacket();
	}*/
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket getHiredMerchantMaintenance() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("0A 01 0D"));
		
		return mplew.getPacket();
	}*/
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket getHiredMerchantItemUpdate(MapleHiredMerchant merchant) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0x17);
		mplew.writeInt(0);
		mplew.write(merchant.getItems().size());
		for (MapleHiredMerchantItem item : merchant.getItems()) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			addItemInfo(mplew, item.getItem(), true, true);
		}
		
		return mplew.getPacket();
	}*/
	    
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket getHiredMerchantMaintenance(MapleHiredMerchant merchant) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("0A 02 0D"));
		
		return mplew.getPacket();
	}*/
	
	/*
	 * @author Rob/Xcheater3161 
	 * */
	/*public static MaplePacket getHiredMerchant(MapleClient c, MapleHiredMerchant merchant, boolean owner, int status) {
		//F5 00 05 05 04 00 00 71 C0 4C 00 0E 00 48 69 72 65 64 20 4D 65 72 63 68 61 6E 74 FF 00 00 0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65 32 92 0D 7E 01 00 00 00 00 00 03 00 31 32 33 10 00 00 00 00 00
		//F5 00 05 05 04 00 00 71 C0 4C 00 0E 00 48 69 72 65 64 20 4D 65 72 63 68 61 6E 74 FF 00 00 0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65 10 16 00 00 00 00 00 00 00 00 03 00 31 32 33 10 00 00 00 01 01 01 00 01 00 01 00 00 00 01 00 76 16 00 00 00 80 05 BB 46 E6 17 02 05 01 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 6E 1B 00 00 4C 00 00 00
		//F5 00 05 05 04 00 00 71 C0 4C 00 0E 00 48 69 72 65 64 20 4D 65 72 63 68 61 6E 74 FF 00 00 0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65 E3 13 00 00 00 00 00 00 00 00 03 00 31 32 33 10 00 00 00 00 03 3A 00 01 00 64 00 00 00 02 71 20 3D 00 00 00 80 05 BB 46 E6 17 02 3A 00 00 00 00 00 02 00 01 00 64 00 00 00 02 73 DA 1E 00 00 00 80 05 BB 46 E6 17 02 02 00 00 00 00 00 10 00 01 00 7B 00 00 00 02 10 0A 3D 00 00 00 80
		//F5 00 05 05 04 00 00 71 C0 4C 00 0E 00 48 69 72 65 64 20 4D 65 72 63 68 61 6E 74 FF 00 00 0B 00 64 75 73 74 72 65 6D 6F 76 65 72 E3 13 00 00 00 00 00 00 00 00 0A 00 46 72 65 65 20 53 74 75 66 66 10 00 00 00 00 03 3A 00 01 00 64 00 00 00 02 71 20 3D 00 00 00 80 05 BB 46 E6 17 02 3A 00 00 00 00 00 02 00 01 00 64 00 00 00 02 73 DA 1E 00 00 00 80 05 BB 46 E6 17 02 02 00 00 00 00 00 10 00 01 00 7B 00 00 00 02 10 0A 3D 00 00 00 80
		//F5 00 05 05 04 01 00 7A C0 4C 00 0E 00 48 69 72 65 64 20 4D 65 72 63 68 61 6E 74 01 00 00 20 4E 00 00 00 93 76 00 00 01 2A 4A 0F 00 04 40 BF 0F 00 05 8D DE 0F 00 06 A5 2C 10 00 07 2B 5C 10 00 0B 00 76 16 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 4D 79 44 65 78 49 73 42 72 6F 6B 65 FF 00 00 08 00 54 69 6E 6B 62 61 62 69 04 00 64 66 61 73 10 00 00 00 00 01 0A 00 01 00 FF E0 F5 05 02 12 30 3D 00 00 00 80 05 BB 46 E6 17 02 0A 00 00 00 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(HexTool.getByteArrayFromHexString("05 05 04"));
		mplew.writeShort(owner ? 0 : 1);
		mplew.writeInt(merchant.getItemId());
		mplew.writeMapleAsciiString("Hired Merchant");
		List<MapleHiredMerchantItem> items = merchant.getItems();
		if (owner) {
			mplew.write(0xFF);
			mplew.writeShort(0);
			mplew.writeMapleAsciiString(merchant.getOwnerName());
			mplew.writeInt(0);
			mplew.writeInt(status);
			mplew.writeShort(0);
			mplew.writeMapleAsciiString(merchant.getDescription());
			mplew.write(0x10);
			mplew.writeInt((int)merchant.getMesos());
		} else {
			mplew.write(1);
			MaplePacketHelper.addCharLook(mplew, c.getPlayer(), false);
			mplew.writeMapleAsciiString(c.getPlayer().getName());
			MapleCharacter[] visitors = merchant.getVisitors();
			int i2 = 1;
			for (int i = 0; i < visitors.length; i++) {
				if (visitors[i] != null) {
					if (visitors[i] != c.getPlayer()) {
						mplew.write(i2 + 1);
						MaplePacketHelper.addCharLook(mplew, visitors[i], false);
						mplew.writeMapleAsciiString(visitors[i].getName());
						c.getPlayer().setVisitorSlot((i2 + 1), visitors[i]);
						i2++;
					}
				}
			}
			mplew.write(0xFF);
			mplew.writeShort(0);
			mplew.writeMapleAsciiString(merchant.getOwnerName());
			mplew.writeMapleAsciiString(merchant.getDescription());
			mplew.write(0x10);
			mplew.writeInt(0);
		}
		
		mplew.write(items.size());
		for (MapleHiredMerchantItem item : items) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			addItemInfo(mplew, item.getItem(), true, true);
		}
		
		return mplew.getPacket();
	}*/
}

