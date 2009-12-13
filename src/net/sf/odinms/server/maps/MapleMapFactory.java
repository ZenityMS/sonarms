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
package net.sf.odinms.server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.PortalFactory;
import net.sf.odinms.server.life.AbstractLoadedMapleLife;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.tools.MockIOSession;
import net.sf.odinms.tools.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleMapFactory {

	private static Logger log = LoggerFactory.getLogger(MapleMapFactory.class);
	private MapleDataProvider source;
	private MapleData nameData;
	private Map<Integer, MapleMap> maps = new HashMap<Integer, MapleMap>();
	private int channel;

	public MapleMapFactory(MapleDataProvider source, MapleDataProvider stringSource) {
		this.source = source;
		this.nameData = stringSource.getData("Map.img");
	}

	public MapleMap getMap(int mapid) {
		return getMap(mapid, true, true, true);
	}

	//backwards-compatible
	public MapleMap getMap(int mapid, boolean respawns, boolean npcs) {
		return getMap(mapid, respawns, npcs, true);
	}

	public MapleMap getMap(int mapid, boolean respawns, boolean npcs, boolean reactors) {
		Integer omapid = Integer.valueOf(mapid);
		MapleMap map = maps.get(omapid);
		if (map == null) {
			synchronized (this) {
				// check if someone else who was also synchronized has loaded the map already
				map = maps.get(omapid);
				if (map != null) {
					return map;
				}

				String mapName = getMapName(mapid);
				MapleData mapData = source.getData(mapName);
				float monsterRate = 0;
				if (respawns) {
					MapleData mobRate = mapData.getChildByPath("info/mobRate");
					if (mobRate != null) {
						monsterRate = ((Float) mobRate.getData()).floatValue();
					}
				}
				map = new MapleMap(mapid, channel, MapleDataTool.getInt("info/returnMap", mapData), monsterRate);
				PortalFactory portalFactory = new PortalFactory();
				for (MapleData portal : mapData.getChildByPath("portal")) {
					int type = MapleDataTool.getInt(portal.getChildByPath("pt"));
					MaplePortal myPortal = portalFactory.makePortal(type, portal);
					map.addPortal(myPortal);
				}
				List<MapleFoothold> allFootholds = new LinkedList<MapleFoothold>();
				Point lBound = new Point();
				Point uBound = new Point();
				for (MapleData footRoot : mapData.getChildByPath("foothold")) {
					for (MapleData footCat : footRoot) {
						for (MapleData footHold : footCat) {
							int x1 = MapleDataTool.getInt(footHold.getChildByPath("x1"));
							int y1 = MapleDataTool.getInt(footHold.getChildByPath("y1"));
							int x2 = MapleDataTool.getInt(footHold.getChildByPath("x2"));
							int y2 = MapleDataTool.getInt(footHold.getChildByPath("y2"));
							MapleFoothold fh = new MapleFoothold(new Point(x1, y1), new Point(x2, y2), Integer.parseInt(footHold.getName()));
							fh.setPrev(MapleDataTool.getInt(footHold.getChildByPath("prev")));
							fh.setNext(MapleDataTool.getInt(footHold.getChildByPath("next")));

							if (fh.getX1() < lBound.x) {
								lBound.x = fh.getX1();
							}
							if (fh.getX2() > uBound.x) {
								uBound.x = fh.getX2();
							}
							if (fh.getY1() < lBound.y) {
								lBound.y = fh.getY1();
							}
							if (fh.getY2() > uBound.y) {
								uBound.y = fh.getY2();
							}
							allFootholds.add(fh);
						}
					}
				}
				MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
				for (MapleFoothold fh : allFootholds) {
					fTree.insert(fh);
				}
				map.setFootholds(fTree);


				// load areas (EG PQ platforms)
				if (mapData.getChildByPath("area") != null) {
					for (MapleData area : mapData.getChildByPath("area")) {
						int x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
						int y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
						int x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
						int y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
						Rectangle mapArea = new Rectangle(x1, y1, (x2 - x1), (y2 - y1));
						map.addMapleArea(mapArea);
					}
				}

				// load life data (npc, monsters)
				for (MapleData life : mapData.getChildByPath("life")) {
					String id = MapleDataTool.getString(life.getChildByPath("id"));
					String type = MapleDataTool.getString(life.getChildByPath("type"));
					if (npcs || !type.equals("n")) {
						AbstractLoadedMapleLife myLife = loadLife(life, id, type);
						if (myLife instanceof MapleMonster) {
							// ((MapleMonster) myLife).calcFhBounds(allFootholds);
							MapleMonster monster = (MapleMonster) myLife;
							int mobTime = MapleDataTool.getInt("mobTime", life, 0);

							if (mobTime == -1 && respawns) { //does not respawn, force spawn once

								map.spawnMonster(monster);
							} else {
								map.addMonsterSpawn(monster, mobTime);
							}
						} else if (myLife instanceof MapleNPC) {
							map.addMapObject(myLife);
						} else {
							map.addMapObject(myLife);
						}
					}
				}

				//load reactor data
				if (reactors && mapData.getChildByPath("reactor") != null) {
					for (MapleData reactor : mapData.getChildByPath("reactor")) {
						String id = MapleDataTool.getString(reactor.getChildByPath("id"));
						if (id != null) {
							MapleReactor newReactor = loadReactor(reactor, id);
							map.spawnReactor(newReactor);
						}
					}
				}

				try {
					map.setMapName(MapleDataTool.getString("mapName", nameData.getChildByPath(getMapStringName(omapid)), ""));
					map.setStreetName(MapleDataTool.getString("streetName", nameData.getChildByPath(getMapStringName(omapid)), ""));
				} catch (Exception e) {
					map.setMapName("");
					map.setStreetName("");
				}
				map.setClock(mapData.getChildByPath("clock") != null);
				map.setEverlast(mapData.getChildByPath("info/everlast") != null);
				map.setForcedReturnMap(MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
				map.setTimeLimit(MapleDataTool.getIntConvert("timeLimit", mapData.getChildByPath("info"), -1));
				map.setFieldType(MapleDataTool.getIntConvert("info/fieldType", mapData, 0));
				
				maps.put(omapid, map);

				if (channel > 0 && Boolean.parseBoolean(ChannelServer.getInstance(channel).getProperty("net.sf.odinms.world.faekchar"))) {
					MapleClient faek = new MapleClient(null, null, new MockIOSession());
					try {
						MapleCharacter faekchar = MapleCharacter.loadCharFromDB(30000, faek, true);
						faek.setPlayer(faekchar);
						faekchar.setPosition(new Point(0, 0));
						faekchar.setMap(map);
						map.addPlayer(faekchar);
					} catch (SQLException e) {
						log.error("Loading FAEK failed", e);
					}
				}
			}
		}
		return map;
	}

	public int getLoadedMaps() {
		return maps.size();
	}

	public boolean isMapLoaded(int mapId) {
		return maps.containsKey(mapId);
	}

	private AbstractLoadedMapleLife loadLife(MapleData life, String id, String type) {
		AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);
		myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
		MapleData dF = life.getChildByPath("f");
		if (dF != null) {
			myLife.setF(MapleDataTool.getInt(dF));
		}
		myLife.setFh(MapleDataTool.getInt(life.getChildByPath("fh")));
		myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
		myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
		int x = MapleDataTool.getInt(life.getChildByPath("x"));
		int y = MapleDataTool.getInt(life.getChildByPath("y"));
		myLife.setPosition(new Point(x, y));

		int hide = MapleDataTool.getInt("hide", life, 0);
		if (hide == 1) {
			myLife.setHide(true);
		} else if (hide > 1) {
			log.warn("Hide > 1 ({})", hide);
		}
		return myLife;
	}

	private MapleReactor loadReactor(MapleData reactor, String id) {
		MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)), Integer.parseInt(id));

		int x = MapleDataTool.getInt(reactor.getChildByPath("x"));
		int y = MapleDataTool.getInt(reactor.getChildByPath("y"));
		myReactor.setPosition(new Point(x, y));

		myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
		myReactor.setState((byte) 0);
		myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));

		return myReactor;
	}

	private String getMapName(int mapid) {
		String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
		StringBuilder builder = new StringBuilder("Map/Map");
		int area = mapid / 100000000;
		builder.append(area);
		builder.append("/");
		builder.append(mapName);
		builder.append(".img");

		mapName = builder.toString();
		return mapName;
	}

	private String getMapStringName(int mapid) {
		StringBuilder builder = new StringBuilder();
		if (mapid < 100000000) {
			builder.append("maple");
		} else if (mapid >= 100000000 && mapid < 200000000) {
			builder.append("victoria");
		} else if (mapid >= 200000000 && mapid < 300000000) {
			builder.append("ossyria");
		} else if (mapid >= 600000000 && mapid < 620000000) {
			builder.append("MasteriaGL");
		} else if (mapid >= 670000000 && mapid < 682000000) {
			builder.append("weddingGL");
		} else if (mapid >= 682000000 && mapid < 683000000) {
			builder.append("HalloweenGL");
		} else if (mapid >= 800000000 && mapid < 900000000) {
			builder.append("jp");
		} else {
			builder.append("etc");
		}
		builder.append("/");
		builder.append(mapid);

		String mapName = builder.toString();
		return mapName;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
