package net.sf.odinms.client.messages.commands;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.odinms.database.DatabaseConnection;
import java.util.Collection;
import java.util.List;
import static net.sf.odinms.client.messages.CommandProcessor.getOptionalIntArg;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import java.util.Arrays;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;

public class CharCommands implements Command {

	private MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

	@SuppressWarnings("static-access")
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("!lowhp")) {
			player.setHp(1);
			player.setMp(500);
			player.updateSingleStat(MapleStat.HP, 1);
			player.updateSingleStat(MapleStat.MP, 500);
		} else if (splitted[0].equals("!fullhp")) {
			player.addMPHP(player.getMaxHp() - player.getHp(), player.getMaxMp() - player.getMp());
		} else if (splitted[0].equals("!skill")) {
			int skill = Integer.parseInt(splitted[1]);
			int level = getOptionalIntArg(splitted, 2, 1);
			int masterlevel = getOptionalIntArg(splitted, 3, 1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
		} else if (splitted[0].equals("!ap")) {
			player.setRemainingAp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
		} else if (splitted[0].equals("!sp")) {
			player.setRemainingSp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
		} else if (splitted[0].equals("!mesos")){
                        player.gainMeso(Integer.parseInt(splitted[1]), true);
                } else if (splitted[0].equals("!job")) {
			int jobId = Integer.parseInt(splitted[1]);
			if (MapleJob.getById(jobId) != null) {
				player.changeJob(MapleJob.getById(jobId));
			}
		} else if (splitted[0].equals("!shop")) {
			MapleShopFactory sfact = MapleShopFactory.getInstance();
			MapleShop shop = sfact.getShop(getOptionalIntArg(splitted, 1, 1));
			shop.sendShop(c);
		} else if (splitted[0].equals("!levelup")) {
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
		} else if (splitted[0].equals("!cleardrops")) {
                    List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
                    for (MapleMapObject i : items) {
                        player.getMap().removeMapObject(i);
                        player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
                    }
                    mc.dropMessage("Items Destroyed: " + items.size());
                } else if (splitted[0].equals("!item")) {
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			int itemId = Integer.parseInt(splitted[1]);
			if (ii.getSlotMax(itemId) > 0) {
				if (itemId >= 5000000 && itemId <= 5000100) {
					if (quantity > 1) {
						quantity = 1;
					}
					int petId = MaplePet.createPet(itemId);
					MapleInventoryManipulator.addById(c, itemId, quantity, c.getPlayer().getName() + "used !item with quantity " + quantity, player.getName(), petId);
					return;
				}
				MapleInventoryManipulator.addById(c, itemId, quantity, c.getPlayer().getName() + "used !item with quantity " + quantity, player.getName());
			} else {
				mc.dropMessage("Item " + itemId + " not found.");
			}
		} else if (splitted[0].equals("!drop")) {
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			IItem toDrop;
			if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
				toDrop = ii.getEquipById(itemId);
			} else {
				toDrop = new Item(itemId, (byte) 0, (short) quantity);
			}
			StringBuilder logMsg = new StringBuilder("Created by ");
			logMsg.append(c.getPlayer().getName());
			logMsg.append(" using !drop. Quantity: ");
			logMsg.append(quantity);
			toDrop.log(logMsg.toString(), false);
			toDrop.setOwner(player.getName());
			c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
		} else if (splitted[0].equals("!level")) {
			int quantity = Integer.parseInt(splitted[1]);
			c.getPlayer().setLevel(quantity);
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
		} else if (splitted[0].equals("!online")) {
			mc.dropMessage("Characters connected to channel " + c.getChannel() + ":");
			Collection<MapleCharacter> chrs = c.getChannelServer().getInstance(c.getChannel()).getPlayerStorage().getAllCharacters();
			for (MapleCharacter chr : chrs) {
				mc.dropMessage(chr.getName() + " at map ID: " + chr.getMapId());
			}
			mc.dropMessage("Total characters on channel " + c.getChannel() + ": " + chrs.size());
		}  else if (splitted[0].equals("!pnpc")) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x + 50);
                npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(false);

                                Connection con = DatabaseConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                                ps.setInt(1, npcId);
                                ps.setInt(2, 0);
                                ps.setInt(3, c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                                ps.setInt(4, c.getPlayer().getPosition().y);
                                ps.setInt(5, c.getPlayer().getPosition().x + 50);
                                ps.setInt(6, c.getPlayer().getPosition().x - 50);
                                ps.setString(7, "n");
                                ps.setInt(8, c.getPlayer().getPosition().x);
                                ps.setInt(9, c.getPlayer().getPosition().y);
                                ps.setInt(10, c.getPlayer().getMapId());
                                ps.executeUpdate();

                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, false));
                // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!pmob")) {
            int npcId = Integer.parseInt(splitted[1]);
                        int mobTime = Integer.parseInt(splitted[2]);
            MapleMonster mob = MapleLifeFactory.getMonster(npcId);
            if (mob != null && !mob.getName().equals("MISSINGNO")) {
                mob.setPosition(c.getPlayer().getPosition());
                mob.setCy(c.getPlayer().getPosition().y);
                mob.setRx0(c.getPlayer().getPosition().x + 50);
                mob.setRx1(c.getPlayer().getPosition().x - 50);
                mob.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());

                                Connection con = DatabaseConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                                ps.setInt(1, npcId);
                                ps.setInt(2, 0);
                                ps.setInt(3, c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                                ps.setInt(4, c.getPlayer().getPosition().y);
                                ps.setInt(5, c.getPlayer().getPosition().x + 50);
                                ps.setInt(6, c.getPlayer().getPosition().x - 50);
                                ps.setString(7, "m");
                                ps.setInt(8, c.getPlayer().getPosition().x);
                                ps.setInt(9, c.getPlayer().getPosition().y);
                                ps.setInt(10, c.getPlayer().getMapId());
                                ps.setInt(11, mobTime);
                                ps.executeUpdate();

                c.getPlayer().getMap().addMonsterSpawn(mob, mobTime);
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!statreset")) {
			int str = c.getPlayer().getStr();
			int dex = c.getPlayer().getDex();
			int int_ = c.getPlayer().getInt();
			int luk = c.getPlayer().getLuk();
			int newap = c.getPlayer().getRemainingAp() + (str - 4) + (dex - 4) + (int_ - 4) + (luk - 4);
			c.getPlayer().setStr(4);
			c.getPlayer().setDex(4);
			c.getPlayer().setInt(4);
			c.getPlayer().setLuk(4);
			c.getPlayer().setRemainingAp(newap);
			List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
			stats.add(new Pair<MapleStat, Integer>(MapleStat.STR, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.DEX, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.INT, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.LUK, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(newap)));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(stats));
		} else if (splitted[0].equals("!gmpacket")) {
			int type = Integer.parseInt(splitted[1]);
			int mode = Integer.parseInt(splitted[2]);
			c.getSession().write(MaplePacketCreator.sendGMOperation(type, mode));
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[]{
					new CommandDefinition("lowhp", "", "", 100),
					new CommandDefinition("fullhp", "", "", 100),
					new CommandDefinition("skill", "", "", 100),
					new CommandDefinition("ap", "", "", 100),
					new CommandDefinition("sp", "", "", 100),
					new CommandDefinition("job", "", "", 100),
					new CommandDefinition("whereami", "", "", 100),
					new CommandDefinition("shop", "", "", 100),
					new CommandDefinition("levelup", "", "", 100),
					new CommandDefinition("cleardrops", "", "", 100),
					new CommandDefinition("pnpc", "", "", 100),
					new CommandDefinition("pmob", "", "", 100),
					new CommandDefinition("mesos", "", "", 100),
					new CommandDefinition("item", "", "", 100),
					new CommandDefinition("drop", "", "", 100),
					new CommandDefinition("level", "", "", 100),
					new CommandDefinition("online", "", "", 100),
					new CommandDefinition("ring", "", "", 100),
					new CommandDefinition("statreset", "", "", 100),
					new CommandDefinition("gmpacket", "", "", 100)
				};
	}
}
