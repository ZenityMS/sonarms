
package net.sf.odinms.server.maps;

import java.awt.Point;
import java.awt.Rectangle;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapleMist extends AbstractMapleMapObject {

	private Rectangle mistPosition;
	private MapleCharacter owner = null;
	private MapleMonster mob = null;
	private MapleStatEffect source;
	private MobSkill skill;
	private boolean isMobMist,  isPoisonMist;
	private int skillDelay;

	public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
		this.mistPosition = mistPosition;
		this.mob = mob;
		this.skill = skill;

		isMobMist = true;
		isPoisonMist = true;
		skillDelay = 0;
	}

	public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
		this.mistPosition = mistPosition;
		this.owner = owner;
		this.source = source;

		switch (source.getSourceId()) {
			case 4221006:
				isMobMist = false;
				isPoisonMist = false;
				skillDelay = 8;
				break;
			case 2111003:
			case 12111005:
				isMobMist = false;
				isPoisonMist = true;
				skillDelay = 8;
				break;
		}
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.MIST;
	}

	@Override
	public Point getPosition() {
		return mistPosition.getLocation();
	}

	public ISkill getSourceSkill() {
		return SkillFactory.getSkill(source.getSourceId());
	}

	public boolean isMobMist() {
		return isMobMist;
	}

	public boolean isPoisonMist() {
		return isPoisonMist;
	}

	public int getSkillDelay() {
		return skillDelay;
	}

	public MapleMonster getMobOwner() {
		return mob;
	}

	public MapleCharacter getOwner() {
		return owner;
	}

	public Rectangle getBox() {
		return mistPosition;
	}

	@Override
	public void setPosition(Point position) {
		throw new UnsupportedOperationException();
	}

	public MaplePacket makeDestroyData() {
		return MaplePacketCreator.removeMist(getObjectId());
	}

	public MaplePacket makeSpawnData() {
		if (owner != null) {
			return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
		}
		return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
	}

	public MaplePacket makeFakeSpawnData(int level) {
		if (owner != null) {
			return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
		}
		return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		client.getSession().write(makeSpawnData());
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(makeDestroyData());
	}

	public boolean makeChanceResult() {
		return source.makeChanceResult();
	}
}