var setupTask;

function init() {
    scheduleNew();
}
function scheduleNew() {
    var cal = java.util.Calendar.getInstance();
    cal.set(java.util.Calendar.HOUR, 3);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    var nextTime = cal.getTimeInMillis();
    while (nextTime <= java.lang.System.currentTimeMillis())
        nextTime += 1000*300;
    setupTask = em.scheduleAtTimestamp("start", nextTime);
}
function cancelSchedule() {
    setupTask.cancel(true);
}
function start() {
    scheduleNew(); //not working
    var Message = new Array("Welcome to SonarMS! We hope you enjoy your stay here. If you have any questions, don't be afraid to ask any of our user-friendly GM!");
    em.getChannelServer().yellowWorldMessage("[SonarMS Tip] " + Message[Math.floor(Math.random() * Message.length)]);
}