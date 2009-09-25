//Kippieeej for the base of the script, Mikethemak for editing it for this function.
var status = 0;
 
function start() {
    status = -1;
    action(1, 0, 0);
}
 
function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
                cm.sendSimple("Hello #h #, Welcome to #rTommyMS#k\r\nAre your mesos maxed out? Want to get more? Well I can help!\r\nI give you rare money sacks that you can trade in at any time for mesos! You can hold up to 50 of each sack. Come to me at any time to give me one, and ill give you your money back!\r\n \r\nWhat would you like to do? \r\n#L1##bTrade 1.5bil for 1 #v5200002##k#l \r\n \r\n#L2##bTrade 1 #v5200002# for 1.5bil#k#l \r\n \r\n#L3#Trade 1bil Mesos for 1 #v5200001##l \r\n#L4#Trade 1 #v5200001# For 1bil mesos.#l \r\n#L5#Trade 500m mesos for 1 #v5200000##l \r\n#L6#Trade 1 #v5200000# For 500m mesos.#l");
        //cm.dispose();
            } else if (status == 1) {
            if (selection == 1) {
    if (cm.itemQuantity(5200002) >= 50) {
    cm.sendOk(" I Think you have enough #v5200002# saved up, try spending some of your mesos, and cashing in a few #v5200002# before you buy more.");
    cm.dispose();
        }  else if (cm.getMeso() >= 1500000000) {
                    cm.gainMeso(-1500000000);                
                    cm.gainItem(5200002, 1); 
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have enough #bMesos#k, are you trying to #eScam#k me!?");
                    cm.dispose();
                }                                
            } else if (selection == 2) {
		if (cm.getMeso() >= 647000000) {
		cm.sendOk("Dont get too carried away, if you trade in your money sack now, you wont get any money! You need to spend some, if you have then the meso limit after u trade in your money sack, you wont get mesos!");
                cm.dispose();
		} else if (cm.itemQuantity(5200002) >= 1) {
                    cm.gainMeso(1500000000);                
                    cm.gainItem(5200002, -1); 
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have any #v5200002#. Please come back when you have some to cash in.");
                    cm.dispose();
                }    
            } else if (selection == 3) {
    if (cm.itemQuantity(5200000) >= 50) {
    cm.sendOk(" I Think you have enough #v5200001# saved up, try spending some of your mesos, and cashing in a few #v5200001# before you buy more.");
    cm.dispose();
    } else if (cm.getMeso() >= 1000000000) {
                    cm.gainMeso(-1000000000);                
                    cm.gainItem(5200001, 1); 
                    cm.dispose();
                   }
        } else if (selection == 4) {
		if (cm.getMeso() >= 1147000000) {
		cm.sendOk("Dont get too carried away, if you trade in your money sack now, you wont get any money! You need to spend some, if you have then the meso limit after u trade in your money sack, you wont get mesos!");
                cm.dispose();
                } else if (cm.itemQuantity(5200001) >= 1) {
                    cm.gainMeso(1000000000);                
                    cm.gainItem(5200001, -1); 
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have any #v5200001#. Please come back when you have some to cash in.");
                    cm.dispose();
               }    
            } else if (selection == 5) {
    if (cm.itemQuantity(5200000) >= 50) {
    cm.sendOk(" I Think you have enough #v5200000# saved up, try spending some of your mesos, and cashing in a few #v5200000# before you buy more.");
    cm.dispose();
    } else if (cm.getMeso() >= 500000000) {
                    cm.gainMeso(-500000000);                
                    cm.gainItem(5200000, 1); 
                    cm.dispose();
            }
                } else if (selection == 6) {
		if (cm.getMeso() >= 1647000000) {
		cm.sendOk("Dont get too carried away, if you trade in your money sack now, you wont get any money! You need to spend some, if you have then the meso limit after u trade in your money sack, you wont get mesos!");
                cm.dispose();
                } else if (cm.itemQuantity(5200000) >= 1) {
                    cm.gainMeso(500000000);                
                    cm.gainItem(5200000, -1); 
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have any #v5200000#. Please come back when you have some to cash in.");
                    cm.dispose();
                    }    
                }
            }
        }
    }
