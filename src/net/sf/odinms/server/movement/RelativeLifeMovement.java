

package net.sf.odinms.server.movement;

import java.awt.Point;

import net.sf.odinms.tools.data.output.LittleEndianWriter;

public class RelativeLifeMovement extends AbstractLifeMovement {
	public RelativeLifeMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	@Override
	public void serialize(LittleEndianWriter lew) {
		lew.write(getType());
		lew.writeShort(getPosition().x);
		lew.writeShort(getPosition().y);
		lew.write(getNewstate());
		lew.writeShort(getDuration());
	}
}
