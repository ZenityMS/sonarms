/*
 * @Name         NimaKIN
 * @Author:      Signalize
 * @NPC:         9900001
 * @Purpose:     Hair/Face/Eye Changer -- May set job as GM too
 * @GMSPurpose:  Sets one's job as a GM. Fixed by Tommy of SonarMS
 * @Map:         180000000
 */
var status = 0;
var beauty = 0;
var haircolor = Array();
var skin = Array(0, 1, 2, 3, 4, 9);
var hair = Array(30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30840, 31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31400, 31410, 31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31780, 31790, 31800, 31810 );
var hairnew = Array();
var face = Array(20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024, 20026, 21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21021, 21022, 21024, 21025);
var facenew = Array();
var colors = Array();

function start() {
    cm.sendSimple("Hey there, I can change your look. What would you like to change?\r\n#L0#Skin#l\r\n#L1#Hair#l\r\n#L2#Hair Color#l\r\n#L3#Eye#l\r\n#L4#Eye Color#l\r\n#L5#GM Job#l");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            beauty = selection + 1;
            if (selection == 0) 
                cm.sendStyle("Pick one?", skin);
            else if (selection == 1) {
                hairnew = Array();
                for(var i = 0; i < hair.length; i++) {
                    hairnew.push(hair[i] + parseInt(cm.getPlayer().getHair() % 10));
                }
                cm.sendStyle("Pick one?", hairnew);
            } else if (selection == 2) {
                haircolor = Array();
                var current = parseInt(cm.getPlayer().getHair()/10)*10;
                for(var k = 0; k < 8; k++) {
                    haircolor.push(current + k);
                }
                cm.sendStyle("Pick one?", haircolor);
            } else if (selection == 3) {
                facenew = Array();
                for(var j = 0; j < face.length; j++) {
                    facenew.push(face[j] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
                cm.sendStyle("Pick one?", facenew);
            } else if (selection == 4) {
                var current = cm.getPlayer().getFace() % 100 + 21000;
                colors = Array();
                colors = Array(current , current + 100, current + 200, current + 300, current +400, current + 500, current + 600, current + 700, current + 800);
                cm.sendStyle("Pick one?", colors);
            } else if (selection == 5)
                if (cm.getPlayer().gmLevel() > 0) {
				cm.getPlayer().changeJobById(910);
				cm.dispose();
				return;
				}
        } else if (status == 2){
            if (beauty == 1)
                cm.setSkin(skin[selection]);
            if (beauty == 2)
                cm.setHair(hairnew[selection]);
            if (beauty == 3)
                cm.setHair(haircolor[selection]);
            if (beauty == 4)
                cm.setFace(facenew[selection]);
            if (beauty == 5)
                cm.setFace(colors[selection]);
            cm.dispose();
        }
    }
}