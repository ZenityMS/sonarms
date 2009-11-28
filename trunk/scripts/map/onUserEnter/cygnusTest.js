/*	
	Author: Traitor
	Map(s):	Cygnus Intro Maps
	Desc:   Sends the disable UI packet, etc.
*/
importPackage(net.sf.odinms.scripting.npc);
importPackage(net.sf.odinms.tools);

function start(ms) {
	var mapid = ms.getPlayer().getMap().getId();
	switch (mapid) {
		case 913040000:
			ms.getClient().getSession().write(MaplePacketCreator.hideUI(true));
			ms.getClient().getSession().write(MaplePacketCreator.disableMovement(true));
		case 913040001:
		case 913040002:
		case 913040003:
		case 913040004:
		case 913040005:
			ms.getClient().getSession().write(MaplePacketCreator.showCygnusIntro(mapid - 913040000));
			ms.getPlayer().setAllowWarpToId(mapid + 1);
			break;
		case 913040006:
			ms.getClient().getSession().write(MaplePacketCreator.hideUI(false));
			ms.getClient().getSession().write(MaplePacketCreator.disableMovement(false));
			ms.getPlayer().setAllowWarpToId(-1);
			NPCScriptManager.getInstance().start(ms.getClient(), 1103005);
			break;
	}
}