function enter(pi) {
	var returnMap = pi.getPlayer().getFM();//clears too!
	if (returnMap < 0)
		returnMap = 100000000;
	pi.warp(returnMap);
    return true;
}