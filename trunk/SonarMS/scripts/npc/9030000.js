/* Fredrick NPC (9030000)
 * By Moogra
 */

/**
     public void removeHiredMerchantItem(int id) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM hiredmerchant WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
    }
 */

var status=0;
var choice;

function start() {
    cm.sendNext("Hi, I'm the store banker.");
} 

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 1)
        cm.sendSimple("Would you like to withdraw\r\n#b#L0#Mesos#l");//    #L1#Items#l");
    else if (status == 2) {
        cm.sendNext("Let me check if you have any....");
        choice = selection;
    } else {
        if (choice == 0) {
            if (status == 3) {
                var mesoEarnt = cm.getHiredMerchantMesos(false);
                if (mesoEarnt > 0)
                    cm.sendYesNo("You have made "+mesoEarnt+" mesos in your store so far. Would you like to withdraw them?");
                else {
                    cm.sendNext("You have not made any mesos");
                    cm.dispose();
                }
            } else if (status == 4) {
                cm.sendNext("Thank you for using my services, your mesos has been recieved");
                cm.gainMeso(cm.getHiredMerchantMesos(true));
                cm.dispose();
            }
        } else {
            if (status == 3) {
//                var items = cm.getHiredMerchantItems();
//                if (items.size() > 0) {
//                    var text = "Please select an item\r\n";
//                    for (var i = 0; i < items.size(); i++)
//                        text += "#L"+i+"##i"+items.get(i).getRight().getItemId()+"##l ";
//                    cm.sendSimple(text);
//                } else {
//                    cm.sendNext("You do not have any items from your store");
//                    cm.dispose();
//                }
            } else if (status == 4) {
                //				var items = cm.getHiredMerchantItems();
                //				MapleInventoryManipulator.addFromDrop(cm.getClient(), items.get(selection).getRight());
                //				cm.sendNext("Thank you for using my services, your item has been recieved");
                //				cm.removeHiredMerchantItem(items.get(selection).getLeft());
                //				cm.dispose();
                cm.sendOk("This is not yet implemented.");
            }
        }
    }
}