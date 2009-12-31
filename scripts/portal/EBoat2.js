/**
-- Odin JavaScript --------------------------------------------------------------------------------
	During The Ride To Ellinia
-- By ---------------------------------------------------------------------------------------------
	Information
-- Description ------------------------------------------------------------------------------------
	Temporary map music fix
-- Version Info -----------------------------------------------------------------------------------
	1.4 - Code clean up [Information]
	1.3 - Typo >.< [Information]
	1.2 - Update to support latest script [Information]
	1.1 - The right statement to bring out the music [Information]
	1.0 - First Version by Information
-- Additional Comments ----------------------------------------------------------------------------
	None
---------------------------------------------------------------------------------------------------
**/

function enter(pi) {
	pi.warp(200090000, 5);
	if(pi.getPlayer().getClient().getChannelServer().getEventSM().getEventManager("Boats").getProperty("haveBalrog").equals("true")) {
		pi.changeMusic("Bgm04/ArabPirate");
	}
	return true;
}
