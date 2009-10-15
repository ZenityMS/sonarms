/* Door of Dimension
	Enter 3rd job event
*/

function start() {
    if (cm.isQuestStarted(100101) && !cm.haveItem(4031059)) {
        var em = cm.getEventManager("3rdjob");
        if (em == null)
            cm.sendOk("Sorry, but 3rd job advancement is closed.");
        else
        em.newInstance(cm.getPlayer().getName()).registerPlayer(cm.getPlayer());
    }
    cm.dispose();
}