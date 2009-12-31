importPackage(net.sf.odinms.client);
var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
    var cal = java.util.Calendar.getInstance();
    cal.set(java.util.Calendar.HOUR, 0);
    cal.set(java.util.Calendar.MINUTE, 10);
    cal.set(java.util.Calendar.SECOND, 0);
    var nextTime = cal.getTimeInMillis();
    while (nextTime <= java.lang.System.currentTimeMillis())
        nextTime += 1000*600; //10 min
    setupTask = em.scheduleAtTimestamp("start", nextTime);
}

function cancelSchedule() {
    setupTask.cancel(true);
}

function start() {
    scheduleNew();
    var Message = new Array("Welcome to SonarMS! Visit our forum @ sonarms.forumotion.com", " Please report all bugs found to Tommy or post them on the forum.");
    em.getChannelServer().broadcastPacket(net.sf.odinms.tools.MaplePacketCreator.serverNotice(5, "[Tip] : " + Message[Math.floor(Math.random() * Message.length)]));
    var iter = em.getInstances().iterator();
    while (iter.hasNext())
        var eim = iter.next();
}