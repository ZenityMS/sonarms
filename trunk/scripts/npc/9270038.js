/* 	
Shalon - Warp to Kerning (From Singapore)
@Author Moogra
*/
function start() {
    cm.sendYesNo("Hello, I am Shalon from Singapore Airport. I can assist you in getting back to Kerning City in no time. Do you want to go back to Kerning City?#k#l");
}
function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(103000000, 0);
    cm.dispose();
}