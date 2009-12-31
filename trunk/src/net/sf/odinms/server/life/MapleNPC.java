

package net.sf.odinms.server.life;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapleNPC extends AbstractLoadedMapleLife {
	private MapleNPCStats stats;
	private boolean custom = false;
	
	public MapleNPC(int id, MapleNPCStats stats) {
		super(id);
		this.stats = stats;
	}
	
	public boolean hasShop() {
		return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
	}
	
	public void sendShop(MapleClient c) {
		MapleShop shop = MapleShopFactory.getInstance().getShopForNPC(getId());
		shop.sendShop(c);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
                if (getName().contains("Maple TV"))
                return;
		client.getSession().write(MaplePacketCreator.spawnNPC(this, false));
		client.getSession().write(MaplePacketCreator.spawnNPC(this, true));//Show NPC Text
	}
	
	@Override
	public void sendDestroyData(MapleClient client) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.NPC;
	}
	
	public String getName() {
		return stats.getName();
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}
}
