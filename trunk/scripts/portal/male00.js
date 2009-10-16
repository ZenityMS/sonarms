function enter(pi) {
    /**
	 *Male00.js
	 */
    var gender = pi.getPlayer().getGender();
    if (gender == 0) {
        pi.warp(670010200, 3);
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "You cannot proceed past here.");
        return false;
    }
}