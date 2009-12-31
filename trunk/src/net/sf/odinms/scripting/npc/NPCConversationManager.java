package net.sf.odinms.scripting.npc;

import java.util.LinkedList;
import java.util.List;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.scripting.AbstractPlayerInteraction;
import net.sf.odinms.scripting.event.EventManager;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.server.MapleSquad;
import net.sf.odinms.server.MapleSquadType;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.net.world.MaplePartyCharacter;

public class NPCConversationManager extends AbstractPlayerInteraction {
	private MapleClient c;
	private int npc;
	private String getText;

	public NPCConversationManager(MapleClient c, int npc) {
		super(c);
		this.c = c;
		this.npc = npc;
	}

	public void dispose() {
		NPCScriptManager.getInstance().dispose(this);
	}

	public void sendNext(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01"));
	}

	public void sendPrev(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00"));
	}

	public void sendNextPrev(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01"));
	}

	public void sendOk(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00"));
	}

	public void sendYesNo(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, ""));
	}

	public void sendAcceptDecline(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, ""));
	}

	public void sendSimple(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, ""));
	}

	public void sendStyle(String text, int styles[]) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
	}

	public void sendGetNumber(String text, int def, int min, int max) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
	}

	public void sendGetText(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
	}

	public void setGetText(String text) {
		this.getText = text;
	}

	public String getText() {
		return this.getText;
	}
        
        public void modifyNX(int amount, int type) {
            getPlayer().addCSPoints(type, amount);
            if (amount > 0) {
                getClient().getSession().write(MaplePacketCreator. serverNotice(5, "You have gained NX Cash (+" + amount +")."));
            } else {
                getClient().getSession().write(MaplePacketCreator. serverNotice(5, "You have lost NX Cash (" + (amount) +")."));
            }
        }

	public void openShop(int id) {
		MapleShopFactory.getInstance().getShop(id).sendShop(getClient());
	}

	public void changeJob(MapleJob job) {
		getPlayer().changeJob(job);
	}

	public MapleJob getJob() {
		return getPlayer().getJob();
	}

	public void startQuest(int id) {
		MapleQuest.getInstance(id).start(getPlayer(), npc);
	}

	public void completeQuest(int id) {
		MapleQuest.getInstance(id).complete(getPlayer(), npc);
	}

	public void forfeitQuest(int id) {
		MapleQuest.getInstance(id).forfeit(getPlayer());
	}

        public void finishAchievement(int id) {
        getClient().getPlayer().finishAchievement(id);
        }
        
	@Deprecated
	public int getMeso() {
		return getPlayer().getMeso();
	}

	public void gainMeso(int gain) {
		getPlayer().gainMeso(gain, true, false, true);
	}

	public void gainExp(int gain) {
		getPlayer().gainExp(gain, true, true);
	}

	public int getNpc() {
		return npc;
	}
        
        public int getPoints() {
            return getPlayer().getPoints();
        }

        public void addPoints(int points) {
            getPlayer().addPoints(points);
        }

        public void removePoints(int points) {
            getPlayer().removePoints(points);
        }

        public boolean isPartyLeader() {
            return getPlayer().isPartyLeader();
        }

        public void warpParty(int mapId) {
                warpParty(mapId, 0, 0);
        }

        public void warpParty(int mapId, int exp, int meso) {
            MapleMap target = getMap(mapId);
            for (MaplePartyCharacter chr_ : getPlayer().getParty().getMembers()) {
                MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr_.getName());
                if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                    curChar.changeMap(target, target.getPortal(0));
                    if (exp > 0) {
                        curChar.gainExp(exp, true, false, true);
                    }
                    if (meso > 0) {
                        curChar.gainMeso(meso, true);
                    }
                }
            }
        }

    	public void warpPartyWithExp(int mapId, int exp) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chrs : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chrs.getName());
			if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
				curChar.gainExp(exp, true, false, true);
			}
		}
	}
        
        public List<MapleCharacter> getPartyMembers() {
            if (getPlayer().getParty() == null) {
                return null;
            }
            List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of shit..
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
                    if (chr != null) { // double check <3
                        chars.add(chr);
                    }
                }
            }
            return chars;
        }


	/**
	 * use getPlayer().getLevel() instead
	 * @return
	 */
	@Deprecated
	public int getLevel() {
		return getPlayer().getLevel();
	}

	public void unequipEverything() {
		MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
		MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<Byte> ids = new LinkedList<Byte>();
		for (IItem item : equipped.list()) {
			ids.add(item.getPosition());
		}
		for (byte id : ids) {
			MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
		}
	}

	public void teachSkill(int id, int level, int masterlevel) {
		getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
	}

	/**
	 * Use getPlayer() instead (for consistency with MapleClient)
	 * @return
	 */
	@Deprecated
	public MapleCharacter getChar() {
		return getPlayer();
	}

	public MapleClient getC() {
		return getClient();
	}

	public void rechargeStars() {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		IItem stars = getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) 1);
		if (ii.isThrowingStar(stars.getItemId()) || ii.isBullet(stars.getItemId())) {
			stars.setQuantity(ii.getSlotMax(stars.getItemId()));
			getC().getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
		}
	}

	public EventManager getEventManager(String event) {
		return getClient().getChannelServer().getEventSM().getEventManager(event);
	}

	public void showEffect(String effect) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
	}

	public void playSound(String sound) {
		getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
	}

	@Override
	public String toString() {
		return "Conversation with NPC: " + npc;
	}

	public void updateBuddyCapacity(int capacity) {
		getPlayer().setBuddyCapacity(capacity);
	}

	public int getBuddyCapacity() {
		return getPlayer().getBuddyCapacity();
	}

	public void setHair(int hair) {
		c.getPlayer().setHair(hair);
		c.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
		c.getPlayer().equipChanged();
	}

	public void setFace(int face) {
		c.getPlayer().setFace(face);
		c.getPlayer().updateSingleStat(MapleStat.FACE, face);
		c.getPlayer().equipChanged();
	}

	@SuppressWarnings("static-access")
	public void setSkin(int color) {
		c.getPlayer().setSkinColor(c.getPlayer().getSkinColor().getById(color));
		c.getPlayer().updateSingleStat(MapleStat.SKIN, color);
		c.getPlayer().equipChanged();
	}

	public MapleSquad createMapleSquad(MapleSquadType type) {
		MapleSquad squad = new MapleSquad(c.getChannel(), getPlayer());
		if (getSquadState(type) == 0) {
			c.getChannelServer().addMapleSquad(squad, type);
		} else {
			return null;
		}
		return squad;
	}

	public MapleCharacter getSquadMember(MapleSquadType type, int index) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		MapleCharacter ret = null;
		if (squad != null) {
			ret = squad.getMembers().get(index);
		}
		return ret;
	}

	public int getSquadState(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			return squad.getStatus();
		} else {
			return 0;
		}
	}

	public void setSquadState(MapleSquadType type, int state) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.setStatus(state);
		}
	}

	public boolean checkSquadLeader(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.getLeader().getId() == getPlayer().getId()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void removeMapleSquad(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.getLeader().getId() == getPlayer().getId()) {
				squad.clear();
				c.getChannelServer().removeMapleSquad(squad, type);
			}
		}
	}

	public int numSquadMembers(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		int ret = 0;
		if (squad != null) {
			ret = squad.getSquadSize();
		}
		return ret;
	}

	public boolean isSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		boolean ret = false;
		if (squad.containsMember(getPlayer())) {
			ret = true;
		}
		return ret;
	}

	public void addSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.addMember(getPlayer());
		}
	}

	public void removeSquadMember(MapleSquadType type, MapleCharacter chr, boolean ban) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.banMember(chr, ban);
		}
	}

	public void removeSquadMember(MapleSquadType type, int index, boolean ban) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			MapleCharacter chr = squad.getMembers().get(index);
			squad.banMember(chr, ban);
		}
	}

	public boolean canAddSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.isBanned(getPlayer())) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public void warpSquadMembers(MapleSquadType type, int mapId) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
		if (squad != null) {
			if (checkSquadLeader(type)) {
				for (MapleCharacter chr : squad.getMembers()) {
					chr.changeMap(map, map.getPortal(0));
				}
			}
		}
	}

        @Override
	public void gainItem(int id, short quantity) {
		if (quantity >= 0) {
			StringBuilder logInfo = new StringBuilder(c.getPlayer().getName());
			logInfo.append(" received ");
			logInfo.append(quantity);
			logInfo.append(" from a scripted PlayerInteraction (");
			logInfo.append(this.toString());
			logInfo.append(")");
			MapleInventoryManipulator.addById(c, id, quantity, logInfo.toString());
		} else {
			MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
		}
		c.getSession().write(MaplePacketCreator.getShowItemGain(id,quantity, true));
	}

	public void resetReactors() {
		c.getPlayer().getMap().resetReactors();
	}

        public void openNpc(int npcid){
            	NPCScriptManager npc = NPCScriptManager.getInstance();
		npc.start(c, npcid);
        }
	public void displayGuildRanks() {
		MapleGuild.displayGuildRanks(getClient(), npc);
	}

	public void sendCygnusCreation() {
		c.getSession().write(MaplePacketCreator.sendCygnusCreation());
	}
	
	public MapleStatEffect getItemEffect(int itemId) {
		return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
	}
}
