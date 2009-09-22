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
    var Message = new Array("At a certain conference, every pair of people are either friends or strangers. At mealtime, every participant eats in one of two large dining rooms. Each person insists upon eating in a room which contains an even number of his or her friends. Prove that the number of ways that the people may be split between the two rooms is a power of two.");
    em.getChannelServer().yellowWorldMessage("[MapleTip] " + Message[Math.floor(Math.random() * Message.length)]);
}