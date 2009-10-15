/*
	Gachapon Script
	Created by Crovax for FlowsionMS
*/

function start() {
    if (cm.haveItem(5220000, 1)) {
        var prizes = new Array();
        var odds = new Array();
        var globalOdds = Array(20, 20, 20, 20, 20);
        //Potions, other stackable junk
        prizes[0] = Array(2000000, 2000001, 2000002);
        odds[0] = Array(100, 50, 50);
        //Quantity is only used for the stackable junk category.
        var quantity = Array(30, 50, 40);
        //Don't forget to add the items. Using an itemid of 0 will probably cause some errors.
        //FMS items are secret.
        //Scrolls
        prizes[1] = Array(0, 0, 0);
        odds[1] = Array(1, 1, 1);
        //Weapons
        prizes[2] = Array(0, 0, 0);
        odds[2] = Array(1, 1, 1);
        //Armors
        prizes[3] = Array(0, 0, 0);
        odds[3] = Array(1, 1, 1);
        //NX equips prizes
        prizes[1] = Array(0, 0, 0);
        odds[1] = Array(1, 1, 1);
        var totalodds = 0;
        var catChoice = 0;
        var itemChoice = 0;
        var itemQuantity = 1;
        for (var i = 0; i < globalOdds.length; i++)
            totalodds += globalOdds[i];
        var randomPick = Math.floor(Math.random()*totalodds)+1;
        for (var i = 0; i < globalOdds.length; i++) {
            randomPick -= globalOdds[i];
            if (randomPick <= 0) {
                catChoice = i;
                randomPick = totalodds + 100;
                i = globalOdds.length;
            }
        }
        totalodds = 0;
        if ((catChoice == 3) || (catChoice == 4)) {
            for (var i = 0; i < odds[catChoice].length; i++) {
                var itemGender = (Math.floor(prizes[catChoice][i]/1000)%10);
                if ((cm.getPlayer().getGender() != itemGender) && (itemGender != 2)) {
                    odds[catChoice][i] = 0;
                }
            }
        }
        for (var i = 0; i < odds[catChoice].length; i++)
            totalodds += odds[catChoice][i];
        randomPick = Math.floor(Math.random()*totalodds)+1;
        for (var i = 0; i < odds[catChoice].length; i++) {
            randomPick -= odds[catChoice][i];
            if (randomPick <= 0) {
                itemChoice = i;
                randomPick = totalodds + 100;
                i = odds[catChoice].length;
            }
        }
        if (catChoice == 0)
            itemQuantity = quantity[itemChoice];
        if ((catChoice == 2) || (catChoice == 3)) {
            cm.addRandomItem(prizes[catChoice][itemChoice]);
        } else
            cm.gainItem(prizes[catChoice][itemChoice], itemQuantity);
        cm.gainItem(5220000, -1);
        cm.dispose();
    } else {
        cm.sendOk("Here is Gachapon.");
        cm.dispose();
    }
}