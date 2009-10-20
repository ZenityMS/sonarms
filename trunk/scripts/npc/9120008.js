/*
    Made by Tommy 
    Pet name changer  
*/ 

var status = 0;
var price = 1000000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) 
{
    if (mode == -1) {
        cm.dispose();
    } else {
    if (mode == 0 && status == 0) {
      cm.dispose();
      return;
    } else if (mode == 1) {
            status++;
        } else {
            status--;
        } if (status == 0) {
        cm.sendYesNo(" Hello #b#h ##k, do you want to change your pet's name? ");
    } else if (status == 1) {
        cm.sendGetText(" Please enter your desired pet name in the blank below. You will then be gifted a pet name tag. ");
    } else if (status == 2) {
        name = cm.getText();
            if (name.length() < 2 || name.length() > 12) {
                cm.sendOk("Either the name you entered is too long, too short, or you didn't enter a name.");
                cm.dispose();
            } else {
                cm.sendYesNo("Are you sure that you want to change your pet's name to #d " + name + "#k?");
            }
    } else if (status == 3) {
            if (cm.getPlayer().getPet() == null) {
                cm.sendOk(" Please make sure your pet is equipped or you have one. ");
                cm.dispose();
            } else if (cm.getMeso() < price) {
                cm.sendOk("You do not have enough mesos to change. Come back to me again after you have collected 1 million mesos.");
                cm.dispose();
            } else {
                cm.getPlayer().getPet().setName(name);
                cm.gainMeso(-price);
                cm.sendOk(" Your pet's name has been changed.");
                cm.dispose();
                cm.reloadChar();
            }
        }
    }
}  