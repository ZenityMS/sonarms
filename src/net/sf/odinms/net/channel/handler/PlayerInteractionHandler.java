

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePlayerShop;
import net.sf.odinms.server.MaplePlayerShopItem;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class PlayerInteractionHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerInteractionHandler.class);	
	
	private enum Action {
		CREATE(0x00),
		//1
		INVITE(0x02),
		DECLINE(0x03),
		VISIT(0x04),
		//5
		CHAT(0x06),
		//7
		//8
		//9
		EXIT(0x0A),
		OPEN(0x0B),
		//C
		//D
		SET_ITEMS(0x0E),
		SET_MESO(0x0F),
		CONFIRM(0x10),
		//11
		//12
		ADD_ITEM(0x13),
		BUY(0x14),
		//15
		//16
		//17
		REMOVE_ITEM(0x18); //slot(byte) bundlecount(short)
		
		final byte code;
		
		private Action(int code) {
			this.code = (byte) code;
		}
		
		public byte getCode() {
			return code;
		}
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte mode = slea.readByte();
		if (mode == Action.CREATE.getCode()) {
			byte createType = slea.readByte();
			if (createType == 3) { // trade
				MapleTrade.startTrade(c.getPlayer());
			} else if (createType == 4) { // shop
				String desc = slea.readMapleAsciiString();
				@SuppressWarnings("unused")
				byte uk1 = slea.readByte(); // maybe public/private 00
				@SuppressWarnings("unused")
				short uk2 = slea.readShort(); // 01 00
				@SuppressWarnings("unused")
				int uk3 = slea.readInt(); // 20 6E 4E
				MaplePlayerShop shop = new MaplePlayerShop(c.getPlayer(), desc);
				c.getPlayer().setPlayerShop(shop);
				c.getPlayer().getMap().addMapObject(shop);
				shop.sendShop(c);
			}
		} else if (mode == Action.INVITE.getCode()) {
			int otherPlayer = slea.readInt();
			MapleCharacter otherChar = c.getPlayer().getMap().getCharacterById(otherPlayer);
			MapleTrade.inviteTrade(c.getPlayer(), otherChar);
		} else if (mode == Action.DECLINE.getCode()) {
			MapleTrade.declineTrade(c.getPlayer());
		} else if (mode == Action.VISIT.getCode()) {
			// we will ignore the trade oids for now
			if (c.getPlayer().getTrade() != null && c.getPlayer().getTrade().getPartner() != null) {
				MapleTrade.visitTrade(c.getPlayer(), c.getPlayer().getTrade().getPartner().getChr());
			} else {
				int oid = slea.readInt();
				MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
				if (ob instanceof MaplePlayerShop) {
					MaplePlayerShop shop = (MaplePlayerShop) ob;
					if (shop.hasFreeSlot() && !shop.isVisitor(c.getPlayer())) {
						shop.addVisitor(c.getPlayer());
						c.getPlayer().setPlayerShop(shop);
						shop.sendShop(c);
					}
				}
			}
		} else if (mode == Action.CHAT.getCode()) { // chat lol
			if (c.getPlayer().getTrade() != null) {
				c.getPlayer().getTrade().chat(slea.readMapleAsciiString());
			} else {
				MaplePlayerShop shop = c.getPlayer().getPlayerShop();
				if (shop != null) {
					shop.chat(c, slea.readMapleAsciiString());
				}
			}
		} else if (mode == Action.EXIT.getCode()) {
			if (c.getPlayer().getTrade() != null) {
				MapleTrade.cancelTrade(c.getPlayer());
			} else {
/*				MaplePlayerShop shop = c.getPlayer().getPlayerShop();
				if (shop != null) {
					c.getPlayer().setPlayerShop(null);
					if (shop.isOwner(c.getPlayer())) {
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateCharBox(c.getPlayer()));
						// return the items not sold
						for (MaplePlayerShopItem item : shop.getItems()) {
							IItem iItem = item.getItem().copy();
							iItem.setQuantity((short) (item.getBundles() * iItem.getQuantity()));
							StringBuilder logInfo = new StringBuilder("Returning items not sold in ");
							logInfo.append(c.getPlayer().getName());
							logInfo.append("'s shop");
							MapleInventoryManipulator.addFromDrop(c, iItem, logInfo.toString());
						}
					} else {
						shop.removeVisitor(c.getPlayer());
					}
				}*/
			}
		} else if (mode == Action.OPEN.getCode()) {
			MaplePlayerShop shop = c.getPlayer().getPlayerShop();
			if (shop != null && shop.isOwner(c.getPlayer())) {
				@SuppressWarnings("unused")
				byte uk1 = slea.readByte(); // 01
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateCharBox(c.getPlayer()));
			}
		} else if (mode == Action.SET_MESO.getCode()) {
			c.getPlayer().getTrade().setMeso(slea.readInt());
		} else if (mode == Action.SET_ITEMS.getCode()) {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
			IItem item = c.getPlayer().getInventory(ivType).getItem((byte) slea.readShort());
			short quantity = slea.readShort();
			byte targetSlot = slea.readByte();
			if (c.getPlayer().getTrade() != null) {
				if ((quantity <= item.getQuantity() && quantity >= 0) || ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
					if (!c.getChannelServer().allowUndroppablesDrop() &&
							ii.isDropRestricted(item.getItemId()))
						//ensure that undroppable items do not make it to the trade window
					{
						c.getSession().write(MaplePacketCreator.enableActions());
						return;
					}
					IItem tradeItem = item.copy();
					if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
						tradeItem.setQuantity(item.getQuantity());
						MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), item.getQuantity(), true);
					} else {
						tradeItem.setQuantity(quantity);
						MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);
					}
					tradeItem.setPosition(targetSlot);
					c.getPlayer().getTrade().addItem(tradeItem);
				} else if (quantity < 0) {
					log.info("[h4x] {} Trading negative amounts of an item", c.getPlayer().getName());
				}
			}
		} else if (mode == Action.CONFIRM.getCode()) {
			MapleTrade.completeTrade(c.getPlayer());
		} else if (mode == Action.ADD_ITEM.getCode()) {
			MaplePlayerShop shop = c.getPlayer().getPlayerShop();
			if (shop != null && shop.isOwner(c.getPlayer())) {
				MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
				byte slot = (byte) slea.readShort();
				short bundles = slea.readShort();
				short perBundle = slea.readShort();
				int price = slea.readInt();
				IItem ivItem = c.getPlayer().getInventory(type).getItem(slot);
				if (ivItem != null && ivItem.getQuantity() >= bundles * perBundle) {
					IItem sellItem = ivItem.copy();
					sellItem.setQuantity(perBundle);
					MaplePlayerShopItem item = new MaplePlayerShopItem(
						shop, sellItem, bundles, price);
					shop.addItem(item);
					// can be put in addItem without faek o.o
					MapleInventoryManipulator.removeFromSlot(c, type, slot, 
						(short) (bundles * perBundle), true);
					c.getSession().write(MaplePacketCreator.getPlayerShopItemUpdate(shop));
				}
				
			}			
		} else if (mode == Action.REMOVE_ITEM.getCode()) {
			/*MaplePlayerShop shop = c.getPlayer().getPlayerShop();
			if (shop != null && shop.isOwner(c.getPlayer())) {
				int slot = slea.readShort();
				MaplePlayerShopItem item = shop.getItems().get(slot);
				IItem ivItem = item.getItem().copy();
				shop.removeItem(slot);
				ivItem.setQuantity((short) (item.getBundles() * ivItem.getQuantity()));
				StringBuilder logInfo = new StringBuilder("Taken out from player shop by ");
				logInfo.append(c.getPlayer().getName());
				MapleInventoryManipulator.addFromDrop(c, ivItem, logInfo.toString());
				c.getSession().write(MaplePacketCreator.getPlayerShopItemUpdate(shop));
			}*/			
		}
	}

}
