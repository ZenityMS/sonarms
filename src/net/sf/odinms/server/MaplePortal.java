

package net.sf.odinms.server;

import java.awt.Point;

import net.sf.odinms.client.MapleClient;

public interface MaplePortal {
	public final int MAP_PORTAL = 2;
	public final int DOOR_PORTAL = 6;
	
	int getType();
	int getId();
	Point getPosition();
	String getName();
	String getTarget();
	String getScriptName();
	void setScriptName(String newName);
	int getTargetMapId();
	void enterPortal(MapleClient c);
}
