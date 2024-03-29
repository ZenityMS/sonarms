

package net.sf.odinms.server.maps;

import java.awt.Point;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.scripting.portal.PortalScriptManager;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapleGenericPortal implements MaplePortal {
	private String name;
	private String target;
	private Point position;
	private int targetmap;
	private int type;
	private int id;
	private String scriptName;

	public MapleGenericPortal(int type) {
		this.type = type;
	}
	
	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id  = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Point getPosition() {
		return position;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public int getTargetMapId() {
		return targetmap;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getScriptName() {
		return scriptName;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setTargetMapId(int targetmapid) {
		this.targetmap = targetmapid;
	}

	@Override
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	
	@Override
	public void enterPortal(MapleClient c) {
		MapleCharacter player = c.getPlayer();
		double distanceSq = getPosition().distanceSq(player.getPosition());
		if (distanceSq > 22500) {
			player.getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL, "D" + Math.sqrt(distanceSq));
		}
		
		boolean changed = false;
		if (getScriptName() != null) {
			changed = PortalScriptManager.getInstance().executePortalScript(this, c);
		} else if (getTargetMapId() != 999999999) {
			MapleMap to;
			if (player.getEventInstance() == null) {
				to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(getTargetMapId());
			} else {
				to = player.getEventInstance().getMapInstance(getTargetMapId());
			}
			MaplePortal pto = to.getPortal(getTarget());
			if (pto == null) {
				pto = to.getPortal(0);
			}
			c.getPlayer().changeMap(to, pto);
			changed = true;
		}
		if (!changed) {
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}
}
