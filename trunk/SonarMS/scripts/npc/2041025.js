//By Moogra

function start() {
    cm.sendYesNo("Beep... beep... you can make your escape to a safer place through me. Beep... beep... would you like to leave this place?");
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(220080000);
    cm.dispose();
}