

/*
 * MapleShopFactory.java
 *
 * Created on 28. November 2007, 18:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matze
 */
public class MapleShopFactory {
	private Map<Integer,MapleShop> shops = new HashMap<Integer,MapleShop>();
	private Map<Integer,MapleShop> npcShops = new HashMap<Integer,MapleShop>();
	
	private static MapleShopFactory instance = new MapleShopFactory();
	
	public static MapleShopFactory getInstance() {
		return instance;
	}
	
	public void clear() {
		shops.clear();
		npcShops.clear();
	}
	
	private MapleShop loadShop(int id, boolean isShopId) {
		MapleShop ret = MapleShop.createFromDB(id, isShopId);
		if (ret != null) {
			shops.put(ret.getId(), ret);
			npcShops.put(ret.getNpcId(), ret);			
		} else if (isShopId) {
			shops.put(id, null);
		} else {
			npcShops.put(id, null);
		}

		return ret;
	}

	public void loadAllShops() {
		List<Integer> npcs = MapleShop.getShopNpcIds();
		for (Integer npc : npcs) {
			MapleShop ret = MapleShop.createFromDB(npc.intValue(), false);
			shops.put(ret.getId(), ret);
			npcShops.put(ret.getNpcId(), ret);
		}
	}
	
	public MapleShop getShop(int shopId) {
		if (shops.containsKey(shopId)) {
			return shops.get(shopId);
		}
		return loadShop(shopId, true);
	}
	
	public MapleShop getShopForNPC(int npcId) {
		if (npcShops.containsKey(npcId)) {
			npcShops.get(npcId);
		}
		return loadShop(npcId, false);
	}
	
}
