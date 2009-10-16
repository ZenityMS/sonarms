/*
@    Creator :  Tommy Of SonarMS
@    Updater :  Tommy Of SonarMS
@    NPC = You Choose
@    Function = Big Shop
@
*/

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
    cm.sendSimple ("Hey! I run the big shop\r\nPick a category" +"#k\r\n#L80#Magician shops" +"#k\r\n#L81#Thief shops" +"#k\r\n#L82#Warrior shops" +"#k\r\n#L83#Archer shops" +"#k\r\n#L84#Pirate shops" +"#k\r\n#L85#Common shops" +"#k\r\n#L86#ETC shops");
      } else if (selection == 80) {
               cm.sendSimple (" Pick a category #e#d" +
            "#k\r\n#L0##rMagician shoes" +
            "#k\r\n#L1##rMagician overalls" +
            "#k\r\n#L2##rMagician gloves" +
            "#k\r\n#L3##rMagician hats" +
            "#k\r\n#L4##rMagician shields" +
            "#k\r\n#L5##rMagician and common wands" +
            "#k\r\n#L6##rMagician and common staffs");
      } else if (selection == 81) {
               cm.sendSimple ("Pick a Category #e#d" +
            "#k\r\n#L7##bThief shoes" +
            "#k\r\n#L8##bThief bottoms" +
            "#k\r\n#L9##bThief tops" +
            "#k\r\n#L10##bThief overalls" +
            "#l\r\n#L11##bThief gloves" +
            "#l\r\n#L12##bThief hats" +
            "#l\r\n#L13##bThief shields" +
            "#l\r\n#L14##bThief and common daggers" +
            "#k\r\n#L15##bThief and common claws" +
            "#k\r\n#L16##bThief throwing stars");
      } else if (selection == 82) {
               cm.sendSimple ("Pick a Category #e#d" +
            "#k\r\n#L17##dWarrior shoes" +
            "#k\r\n#L18##dWarrior bottom" +
            "#k\r\n#L19##dWarrior top" +
            "#k\r\n#L20##dWarrior overalls" +
            "#k\r\n#L21##dWarrior gloves" +
            "#k\r\n#L22##dWarrior hats" +
            "#k\r\n#L23##dWarrior shields" +
            "#k\r\n#L24##dWarrior and common one handed axes" +
            "#k\r\n#L25##dWarrior and common two handed axes" +
            "#k\r\n#L26##dWarrior and common one handed bWs" +
            "#k\r\n#L27##dWarrior and common two handed bWs" +
            "#k\r\n#L28##dWarrior and common one handed swords" +
            "#k\r\n#L29##dWarrior and common two handed swords" +
            "#k\r\n#L30##dWarrior and common spears" +
            "#k\r\n#L31##dWarrior and common pole arms");
      } else if (selection == 83) {
               cm.sendSimple ("Pick a Category #e#d" +
            "#k\r\n#L32##gArcher shoes" +
            "#k\r\n#L33##gArcher overalls" +
            "#k\r\n#L34##gArcher gloves" +
            "#k\r\n#L35##gArcher hats" +
            "#k\r\n#L36##gArcher and common bows" +
            "#k\r\n#L37##gArcher and common crossbows" +
            "#k\r\n#L38##gArcher arrows");
      } else if (selection == 84) {
               cm.sendSimple ("Pick a Category #e#d" +
            "#k\r\n#L76##bPirate knuckler" +
            "#k\r\n#L71##bPirate guns" +
            "#k\r\n#L72##bPirate bullets" +
            "#k\r\n#L73##bPirate hats" +
            "#k\r\n#L74##bPirate overalls" +
            "#k\r\n#L101##bPirate gloves" +
            "#k\r\n#L75##bPirate shoes");
      } else if (selection == 85) {
               cm.sendSimple ("Pick a Category #e#d" +
            "\r\n#L105#Stirgeman's weapons" +
            "\r\n#L104#Stirgeman's bottom" +
            "\r\n#L102#Stirgeman's capes" +
            "#k\r\n#L39#Maple weapons" +
            "\r\n#L40#Earrings" +
            "\r\n#L41#Necklaces and face accessories" +
            "\r\n#L42#Capes" +
            "\r\n#L43#Common shoes" +
            "\r\n#L44#Common hats" +
            "\r\n#L45#Common gloves" +
            "\r\n#L46#Common overalls" +
            "\r\n#L47#Common shields" +
            "\r\n#L48#Level 0 weapons");
      } else if (selection == 86) {
               cm.sendSimple ("Pick a Category #e#d" +
            "\r\n#L94#All scrolls" +
            "\r\n#L50#Megaphones, Gachapon tickets, Rocks, Morphs" +
            "\r\n#L51#Buffs and potions" +
            "\r\n#L52#Boss pieces" +
            "\r\n#L53#Mounts");
      } else if (selection == 87) {
               cm.sendSimple ("Pick a Category #e#d" +
            "\r\n#L56#All pet equips, Pet food, Scrolls, Books" +
            "\r\n#L100#Cash hats" +
            "\r\n#L95#Cash tops" +
            "\r\n#L96#Cash bottoms" +
            "\r\n#L97#Cash overalls" +
            "\r\n#L99#Cash weapons" +
            "\r\n#L69#Cash throwing stars" +
            "\r\n#L60#Cash gloves" +
            "\r\n#L98#Cash shoes" +
            "\r\n#L59#Cash capes" +
            "\r\n#L61#Cash shields" +
            "\r\n#L62#Rings" +
            "\r\n#L64#Emotions" +
            "\r\n#L65#Cash earrings" +
            "\r\n#L66#Eye accessories" +
            "\r\n#L67#All accessories" +
            "\r\n#L68#Effects" +
            "\r\n#L70#Messengers" +
            "\r\n#L103##rCharge cash");
      } else if (selection == 105) {
        cm.openShop (10095);
        cm.dispose();
      } else if (selection == 104) {
        cm.openShop (10094);
        cm.dispose();
      } else if (selection == 102) {
        cm.openShop (10086);
        cm.dispose();
      } else if (selection == 101) {
        cm.openShop (10092);
        cm.dispose();
      } else if (selection == 100) {
        cm.openShop (10081);
        cm.dispose();
      } else if (selection == 99) {
        cm.openShop (10080);
        cm.dispose();
      } else if (selection == 98) {
        cm.openShop (10083);
        cm.dispose();
      } else if (selection == 97) {
        cm.openShop (10082);
        cm.dispose();
      } else if (selection == 96) {
        cm.openShop (10085);
        cm.dispose();
      } else if (selection == 95) {
        cm.openShop (10084);
        cm.dispose();
      } else if (selection == 94) {
        cm.openShop (10053);
        cm.dispose();
      } else if (selection == 0) {
        cm.openShop (10000);
        cm.dispose();
      } else if (selection == 1) {
        cm.openShop (10001);
        cm.dispose();
      } else if (selection == 2) {
        cm.openShop (10002);
        cm.dispose();
      } else if (selection == 3) {
        cm.openShop (10003);
        cm.dispose();
      } else if (selection == 4) {
        cm.openShop (10004);
        cm.dispose();
      } else if (selection == 5) {
        cm.openShop (10005);
        cm.dispose();
      } else if (selection == 6) {
        cm.openShop (10006);
        cm.dispose();
      } else if (selection == 7) {
        cm.openShop (10007);
        cm.dispose();
      } else if (selection == 8) {
        cm.openShop (10008);
        cm.dispose();
      } else if (selection == 9) {
        cm.openShop (10009);
        cm.dispose();
      } else if (selection == 10) {
        cm.openShop (10010);
        cm.dispose();
      } else if (selection == 11) {
        cm.openShop (10011);
        cm.dispose();
      } else if (selection == 12) {
        cm.openShop (10012);
        cm.dispose();
      } else if (selection == 13) {
        cm.openShop (10013);
        cm.dispose();
      } else if (selection == 14) {
        cm.openShop (10014);
        cm.dispose();
      } else if (selection == 15) {
        cm.openShop (10015);
        cm.dispose();
      } else if (selection == 16) {
        cm.openShop (10038);
        cm.dispose();
      } else if (selection == 17) {
        cm.openShop (10016);
        cm.dispose();
      } else if (selection == 18) {
        cm.openShop (10017);
        cm.dispose();
      } else if (selection == 19) {
        cm.openShop (10018);
        cm.dispose();
      } else if (selection == 20) {
        cm.openShop (10019);
        cm.dispose();
      } else if (selection == 21) {
        cm.openShop (10020);
        cm.dispose();
      } else if (selection == 22) {
        cm.openShop (10021);
        cm.dispose();
      } else if (selection == 23) {
        cm.openShop (10022);
        cm.dispose();
      } else if (selection == 24) {
        cm.openShop (10023);
        cm.dispose();
      } else if (selection == 25) {
        cm.openShop (10024);
        cm.dispose();
      } else if (selection == 26) {
        cm.openShop (10025);
        cm.dispose();
      } else if (selection == 27) {
        cm.openShop (10026);
        cm.dispose();
      } else if (selection == 28) {
        cm.openShop (10027);
        cm.dispose();
      } else if (selection == 29) {
        cm.openShop (10028);
        cm.dispose();
      } else if (selection == 30) {
        cm.openShop (10029);
        cm.dispose();
      } else if (selection == 31) {
        cm.openShop (10030);
        cm.dispose();
      } else if (selection == 32) {
        cm.openShop (10031);
        cm.dispose();
      } else if (selection == 33) {
        cm.openShop (10032);
        cm.dispose();
      } else if (selection == 34) {
        cm.openShop (10033);
        cm.dispose();
      } else if (selection == 35) {
        cm.openShop (10034);
        cm.dispose();
      } else if (selection == 36) {
        cm.openShop (10035);
        cm.dispose();
      } else if (selection == 37) {
        cm.openShop (100320);
        cm.dispose();
      } else if (selection == 38) {
        cm.openShop (10037);
        cm.dispose();
      } else if (selection == 39) {
        cm.openShop (10051);
        cm.dispose();
      } else if (selection == 40) {
        cm.openShop (10039);
        cm.dispose();
      } else if (selection == 41) {
        cm.openShop (10040);
        cm.dispose();
      } else if (selection == 42) {
        cm.openShop (10041);
        cm.dispose();
      } else if (selection == 43) {
        cm.openShop (10042);
        cm.dispose();
      } else if (selection == 44) {
        cm.openShop (10043);
        cm.dispose();
      } else if (selection == 45) {
        cm.openShop (10044);
        cm.dispose();
      } else if (selection == 46) {
        cm.openShop (10045);
        cm.dispose();
      } else if (selection == 47) {
        cm.openShop (10046);
        cm.dispose();
      } else if (selection == 48) {
        cm.openShop (10047);
        cm.dispose();
      } else if (selection == 49) {
        cm.openShop (10048);
        cm.dispose();
      } else if (selection == 50) {
        cm.openShop (10048);
        cm.dispose();
      } else if (selection == 51) {
        cm.openShop (10049);
        cm.dispose();
      } else if (selection == 52) {
        cm.openShop (10050);
        cm.dispose();
      } else if (selection == 53) {
        cm.openShop (10052);
        cm.dispose();
      } else if (selection == 54) {
        cm.openShop (10053);
        cm.dispose();
      } else if (selection == 55) {
        cm.openShop (10054);
        cm.dispose();
      } else if (selection == 56) {
        cm.openShop (10055);
        cm.dispose();
      } else if (selection == 57) {
        cm.openShop (10056);
        cm.dispose();
      } else if (selection == 58) {
        cm.openShop (10057);
        cm.dispose();
      } else if (selection == 59) {
        cm.openShop (10058);
        cm.dispose();
      } else if (selection == 60) {
        cm.openShop (10059);
        cm.dispose();
      } else if (selection == 61) {
        cm.openShop (10060);
        cm.dispose();
      } else if (selection == 62) {
        cm.openShop (10061);
        cm.dispose();
      } else if (selection == 63) {
        cm.openShop (10062);
        cm.dispose();
      } else if (selection == 64) {
        cm.openShop (10063);
        cm.dispose();
      } else if (selection == 65) {
        cm.openShop (10064);
        cm.dispose();
      } else if (selection == 66) {
        cm.openShop (10065);
        cm.dispose();
      } else if (selection == 67) {
        cm.openShop (10066);
        cm.dispose();
      } else if (selection == 68) {
        cm.openShop (10067);
        cm.dispose();
      } else if (selection == 69) {
        cm.openShop (10068);
        cm.dispose();
      } else if (selection == 70) {
        cm.openShop (10069);
        cm.dispose();
      } else if (selection == 71) {
        cm.openShop (10088);
        cm.dispose();
      } else if (selection == 72) {
        cm.openShop (10089);
        cm.dispose();
      } else if (selection == 73) {
        cm.openShop (10090);
        cm.dispose();
      } else if (selection == 74) {
        cm.openShop (10091);
        cm.dispose();
      } else if (selection == 75) {
        cm.openShop (10093);
        cm.dispose();
      } else if (selection == 76) {
        cm.openShop (10087);
        cm.dispose();
      } else if (selection == 103) {
                cm.sendSimple ("How much would you like?#e#d" +
                 "#k\r\n#L89##r10k NX cash for 200k mesos" +
                 "#k\r\n#L90##r20k NX cash for 400k mesos" +
                 "#k\r\n#L91##r30k NX cash for 600k mesos" +
                 "#k\r\n#L92##r40k NX cash for 800k mesos" +
                 "#k\r\n#L93##r50k NX cash for 1 million mesos");
      } else if (selection == 89) {
        if (cm.getMeso < 200000) {
                   cm.sendOk ("Please check if you have enough #rmesos#k");
                   cm.dispose();
      } else {
                   cm.gainMeso (-200000);
                   cm.modifyNX(10000,4);
                   cm.dispose();
                   }
      } else if (selection == 90) {
        if (cm.getMeso < 400000) {
                   cm.sendOk ("Please check if you have enough #rmesos#k");
                   cm.dispose();
      } else {
                   cm.gainMeso (-400000);
                   cm.modifyNX(20000,4);
                   cm.dispose();
                   }
      } else if (selection == 91) {
        if (cm.getMeso < 600000) {
                   cm.sendOk ("Please check if you have enough #rmesos#k");
                   cm.dispose();
      } else {
                   cm.gainMeso (-600000);
                   cm.modifyNX(30000,4);
                   cm.dispose();
                   }
      } else if (selection == 92) {
        if (cm.getMeso < 800000) {
                   cm.sendOk ("Please check if you have enough #rmesos#k");
                   cm.dispose();
      } else {
                   cm.gainMeso (-800000);
                   cm.modifyNX(40000,4);
                   cm.dispose();
                   }
      } else if (selection == 93) {
        if (cm.getMeso < 1000000) {
                   cm.sendOk ("Please check if you have enough #rmesos#k");
                   cm.dispose();
      } else {
                   cm.gainMeso (-1000000);
                   cm.modifyNX(50000,4);
                   cm.dispose();
                   }
      } else if (selection == 107) {
               cm.sendSimple ("Hello Im the Bubble Exchanger. You can earn Bubble from Events, Awards, or  Donations#e#d" +
            "\r\n#L176##gTrade Bubble for Yellow Bubble" + 
            "\r\n#L177##gTrade Bubble for Common Equips" +
            "\r\n#L178##gTrade Bubble for GM scrolls" +
            "\r\n#L179##gTrade Bubble for Buff Potions" +
            "\r\n#L180##gTrade Bubble for Ap Points" +
            "\r\n#L181##gTrade Bubble for Goodie Hats" +
            "\r\n#L182##gTrade Bubble for Gachapon Tickets");
      } else if (selection == 177) {
               cm.sendSimple ("What do you want? #e#d" +
            "\r\n#L108##gTrade 10 Bubble for Brown Work Glove" +
            "\r\n#L109##gTrade 20 Bubble for Blue Sauna" +
            "\r\n#L110##gTrade 20 Bubble for Red Sauna" +
            "\r\n#L111##gTrade 25 Bubble for Face Stompers" +
            "\r\n#L112##gTrade 25 Bubble for Icecream Bars" +
            "\r\n#L113##gTrade 40 Bubble for Stormcaster Glove" +
            "\r\n#L114##gTrade 80 Bubble for Horntail Necklace & 3 Dragonstones" +
            "\r\n#L115##gTrade 100 Bubble for Green & Red & Yellow & Pink  & Black & Blue Bow-tie & 7 Dragonstones ");
      } else if (selection == 178) {
               cm.sendSimple ("Pick one#e#d" +
            "\r\n#L116##gTrade 10 Bubble for Bottomwear for DEF" +
            "\r\n#L117##gTrade 10 Bubble for Bow for ATT" +
            "\r\n#L118##gTrade 10 Bubble for Cape for Magic DEF" +
            "\r\n#L119##gTrade 10 Bubble for Claw for ATT" +
            "\r\n#L120##gTrade 10 Bubble for Claw for ATT" +
            "\r\n#L121##gTrade 10 Bubble for Crossbow for ATT" +
            "\r\n#L122##gTrade 10 Bubble for Dagger for ATT" +
            "\r\n#L123##gTrade 10 Bubble for Gloves for ATT" +
            "\r\n#L124##gTrade 10 Bubble for Gloves for DEX" +
            "\r\n#L125##gTrade 10 Bubble for Helmet for DEF" +
            "\r\n#L126##gTrade 10 Bubble for Helmet for HP" +
            "\r\n#L127##gTrade 10 Bubble for One-Handed Axe for ATT" +
            "\r\n#L128##gTrade 10 Bubble for One-Handed BW for ATT" +
            "\r\n#L129##gTrade 10 Bubble for One-Handed Sword for ATT" +
            "\r\n#L130##gTrade 10 Bubble for Overall Armor for DEX" +
            "\r\n#L131##gTrade 10 Bubble for Pole Arm for ATT" +
            "\r\n#L132##gTrade 10 Bubble for Shield for DEF" +
            "\r\n#L133##gTrade 10 Bubble for Shoes for DEX" +
            "\r\n#L134##gTrade 10 Bubble for Shoes for Jump" +
            "\r\n#L135##gTrade 10 Bubble for Shoes for Speed" +
            "\r\n#L136##gTrade 10 Bubble for Spear for ATT" +
            "\r\n#L137##gTrade 10 Bubble for Staff for Magic ATT" +
            "\r\n#L138##gTrade 10 Bubble for Topwear for DEF" +
            "\r\n#L139##gTrade 10 Bubble for Two-Handed Aex for ATT" +
            "\r\n#L140##gTrade 10 Bubble for Two-Handed BW for ATT" +
            "\r\n#L141##gTrade 10 Bubble for Two-Handed Sword for ATT" +
            "\r\n#L142##gTrade 10 Bubble for Wand for Magic ATT");
	  } else if (selection == 179) {
               cm.sendSimple ("Pick one#e#d" +
            "\r\n#L143##gTrade 5 Bubble for 10 Heartstoppers" +
            "\r\n#L144##gTrade 10 Bubble for 10 Onyx Apples" +
            "\r\n#L145##gTrade 20 Bubble for 25 Onyx Apples" +
            "\r\n#L146##gTrade 50 Bubble for 60 Onyx Apples" +
            "\r\n#L147##gTrade 10 Bubble for 5 Demon Elixirs" +
            "\r\n#L148##gTrade 20 Bubble for 13 Demon Elixirs" +
            "\r\n#L149##gTrade 50 Bubble for 30 Demon Elixirs" +
            "\r\n#L150##gTrade 5 Bubble for 5 Swiss Cheese" +
            "\r\n#L151##gTrade 10 Bubble for 13 Swiss Cheese" +
            "\r\n#L152##gTrade 30 Bubble for 40 Swiss Cheese");
	  } else if (selection == 180) {
               cm.sendSimple ("Pick one #e#d" +
            "\r\n#L153##gTrade 5 Bubble for 10 Ap Points" +
            "\r\n#L154##gTrade 10 Bubble for 25 Ap Points" +
            "\r\n#L155##gTrade 20 Bubble for 50 Ap Points" +
            "\r\n#L156##gTrade 50 Bubble for 120 Ap Points" +
            "\r\n#L157##gTrade 100 Bubble for 230 Ap Points" +
            "\r\n#L158##gTrade 300 Bubble for 700 AP Points" +
            "\r\n#L159##gTrade 500 Bubble for 1200 Ap Points");
	  } else if (selection == 181) {
               cm.sendSimple ("Hmm.. GM equips you say... It's pretty hard to get!#e#d" +
            "\r\n#L160##g2000 Bubble for GM Hat" +
            "\r\n#L161##g5000 Bubble for Customized Hat/+5557 Stats" +
            "\r\n#L162##g9000 Bubble for Customizd Hat/+9999 Stats");
	  } else if (selection == 182) {
               cm.sendSimple ("You want Gachapon?? Ok but don't push your luck!!#e#d" +
            "\r\n#L163##g20 Bubble for 5 Gachapon Tickets" +
            "\r\n#L164##g40 Bubble for 10 Gachapon Tickets" +
            "\r\n#L165##g80 Bubble for 20 Gachapon Tickets");
	  } 
 	   else if (selection == 176) {
               cm.sendSimple ("If you have too many Green Bubbles, you can always trade here#e#d" +
            "\r\n#L166##g100 Green Bubble for 1 Yellow Bubble" +
            "\r\n#L167##g500 Green Bubble for 5 Yellow Bubble" +
            "\r\n#L168##g1000 Green Bubble for 10 Yellow Bubble"+
            "\r\n#L169##g2000 Green Bubble for 20 Yellow Bubble" +
            "\r\n#L170##g5000 Green Bubble for 50 Yellow Bubble" +
            "\r\n\r\n\r\n#L171##g1 Yellow Bubble for 100 Green Bubble" +
            "\r\n#L172##g5 Yellow Bubble for 500 Green Bubble" +
            "\r\n#L173##g10 Yellow Bubble for 1000 Green Bubble" +
            "\r\n#L174##g20 Yellow Bubble for 2000 Green Bubble" +
            "\r\n#L175##g50 Yellow Bubble for 5000 Green Bubble");
	  } 
else if (selection == 108) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(1082149, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your Brown Work Glove");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 109) {
		if (cm.haveItem(4000256, 20)){
		cm.gainItem(1050018, 1);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job, Here is your Blue Sauna");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 110) {
		if (cm.haveItem(4000256, 20)){
		cm.gainItem(1051017, 1);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job, Here is your Red Sauna");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 111) {
		if (cm.haveItem(4000256, 25)){
		cm.gainItem(1072344, 1);
		cm.gainItem(4000256, -25);
		cm.sendOk("Nice job, Here is your Face Stompers");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 112) {
		if (cm.haveItem(4000256, 25)){
		cm.gainItem(1012070, 1);
		cm.gainItem(1012071, 1);
		cm.gainItem(1012072, 1);
		cm.gainItem(1012073, 1);
		cm.gainItem(4000256, -25);
		cm.sendOk("Nice job, Here is your Icecream Bars");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 113) {
		if (cm.haveItem(4000256, 40)){
		cm.gainItem(1082223, 1);
		cm.gainItem(4000256, -40);
		cm.sendOk("Nice job, Here is your Stormcaster Glove");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 114) {
		if (cm.haveItem(4000256, 80)){
		cm.gainItem(1122000, 1);
		cm.gainItem(2041200, 3);
		cm.gainItem(4000256, -80);
		cm.sendOk("Nice job, Here is your Horntail Necklace");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 115) {
		if (cm.haveItem(4000256, 100)){
		cm.gainItem(1122001, 1);
		cm.gainItem(1122002, 1);
		cm.gainItem(1122003, 1);
		cm.gainItem(1122004, 1);
		cm.gainItem(1122005, 1);
		cm.gainItem(1122006, 1);
		cm.gainItem(2041200, 7);
		cm.gainItem(4000256, -100);
		cm.sendOk("Nice job, Here is your BowTie!!");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 116) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040603, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 117) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044503, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 118) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2041024, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 119) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2041025, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 120) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044703, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 121) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044603, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 122) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043303, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 123) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040807, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 124) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040806, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 125) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040006, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 126) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040007, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 127) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043103, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 128) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043203, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 129) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043003, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 130) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040506, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 131) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044403, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 132) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040903, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 133) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040709, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 134) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040710, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 135) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040711, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 136) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044303, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 137) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043803, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 138) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2040403, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 139) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044103, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 140) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044203, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 141) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2044003, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 142) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2043703, 1);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your GM Scroll");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 143) {
		if (cm.haveItem(4000256, 5)){
		cm.gainItem(2022245, 10);
		cm.gainItem(4000256, -5);
		cm.sendOk("Nice job, Here is your HeartStopper");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 144) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2022179, 10);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your Onyx Apple");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 145) {
		if (cm.haveItem(4000256, 20)){
		cm.gainItem(2022179, 25);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job, Here is your Onyx Apple");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 146) {
		if (cm.haveItem(4000256, 50)){
		cm.gainItem(2022179, 60);
		cm.gainItem(4000256, -50);
		cm.sendOk("Nice job, Here is your Onyx Apple");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 147) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2022282, 5);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your Demon Elixir");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 148) {
		if (cm.haveItem(4000256, 20)){
		cm.gainItem(2022282, 13);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job, Here is your Demon Elixir");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 149) {
		if (cm.haveItem(4000256, 50)){
		cm.gainItem(2022282, 30);
		cm.gainItem(4000256, -50);
		cm.sendOk("Nice job, Here is your Demon Elixir");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 150) {
		if (cm.haveItem(4000256, 5)){
		cm.gainItem(2022273, 5);
		cm.gainItem(4000256, -5);
		cm.sendOk("Nice job, Here is your Cheese");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 151) {
		if (cm.haveItem(4000256, 10)){
		cm.gainItem(2022273, 13);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your Cheese");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 152) {
		if (cm.haveItem(4000256, 30)){
		cm.gainItem(2022273, 40);
		cm.gainItem(4000256, -30);
		cm.sendOk("Nice job, Here is your Cheese");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 153) {
		if (cm.haveItem(4000256, 5)){
		cm.gainAP(10);
		cm.gainItem(4000256, -5);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 154) {
		if (cm.haveItem(4000256, 10)){
		cm.gainAP(25);
		cm.gainItem(4000256, -10);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 155) {
		if (cm.haveItem(4000256, 20)){
		cm.gainAP(50);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 156) {
		if (cm.haveItem(4000256, 50)){
		cm.gainAP(120);
		cm.gainItem(4000256, -50);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 157) {
		if (cm.haveItem(4000256, 100)){
		cm.gainAP(230);
		cm.gainItem(4000256, -100);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 158) {
		if (cm.haveItem(4000256, 300)){
		cm.gainAP(700);
		cm.gainItem(4000256, -300);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 159) {
		if (cm.haveItem(4000256, 500)){
		cm.gainAP(1200);
		cm.gainItem(4000256, -500);
		cm.sendOk("Nice job, Here is your AP");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 160) {
		if (cm.haveItem(4000256, 2000)){
		cm.gainItem(1002140, 1);
		cm.gainItem(4000256, -2000);
		cm.sendOk("Nice job, Here is your GM Hat. If you Do not wear a NX Hat over it, It will be wiped off your inventory after a warning");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Don't ever try to scam My GM HAT!!")
		cm.dispose();
		}
	}
else if (selection == 161) {
		if (cm.haveItem(4000256, 5000)){
		cm.gainItem(4031167, 1);
		cm.gainItem(4000256, -5000);
		cm.sendOk("Nice job, Here is a Box for it. DO NOT OPEN THEM. Just post Screen Shot of it whit you and post it on Forum at Event Section or show it to Admin, It will be exchanged for 13337 Stat GM Hat");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Don't ever try to scam My GM HAT!!")
		cm.dispose();
		}
	}
else if (selection == 162) {
		if (cm.haveItem(4000256, 9000)){
		cm.gainItem(4031168, 1);
		cm.gainItem(4000256, -9000);
		cm.sendOk("Nice job, Here is a Box for it. DO NOT OPEN THEM. Just post Screen Shot of it whit you and post it on Forum at Event Section or show it to Admin, It will be exchanged for 13337 Stat GM Hat");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Don't ever try to scam My GM HAT!!")
		cm.dispose();
		}
	}
else if (selection == 163) {
		if (cm.haveItem(4000256, 20)){
		cm.gainItem(4031206, 5);
		cm.gainItem(4000256, -20);
		cm.sendOk("Nice job. Here are the list of possibilities. #i4000000#, #i4000015#, #i1002562#, #i1002527#,#i1002063#,#i1102041#,#i1102042#,#i1102043#,#i1102084#,#i1102086#,#i1050018#,#i1051017#,#i1050100#,#i1050127#,#i1051098#,#i1051140#,#i2340000#,#i1072344#,#i1082223#,#i1122000#,#i1012070#,#i1082230#,#i1002430#,and#i4000255#Also, use the Gachapon on 3rd platform in this room");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Try harder.")
		cm.dispose();
		}
	}
else if (selection == 164) {
		if (cm.haveItem(4000256, 40)){
		cm.gainItem(4031206, 10);
		cm.gainItem(4000256, -40);
		cm.sendOk("Nice job. Here are the list of possibilities. #i4000000#, #i4000015#, #i1002562#, #i1002527#,#i1002063#,#i1102041#,#i1102042#,#i1102043#,#i1102084#,#i1102086#,#i1050018#,#i1051017#,#i1050100#,#i1050127#,#i1051098#,#i1051140#,#i2340000#,#i1072344#,#i1082223#,#i1122000#,#i1012070#,#i1082230#,#i1002430#,and#i4000255#Also, use the Gachapon on 3rd platform in this room");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Try harder.")
		cm.dispose();
		}
	}
else if (selection == 165) {
		if (cm.haveItem(4000256, 80)){
		cm.gainItem(4031206, 20);
		cm.gainItem(4000256, -80);
		cm.sendOk("Nice job. Here are the list of possibilities. #i4000256#,#i4000000#, #i4000015#, #i1002562#, #i1002527#,#i1002063#,#i1102041#,#i1102042#,#i1102043#,#i1102084#,#i1102086#,#i1050018#,#i1051017#,#i1050100#,#i1050127#,#i1051098#,#i1051140#,#i2340000#,#i1072344#,#i1082223#,#i1122000#,#i1012070#,#i1082230#,#i1002430#,and#i4000255#Also, use the Gachapon on 3rd platform in this room");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!! Try harder.")
		cm.dispose();
		}
	}
else if (selection == 166) {
		if (cm.haveItem(4000256, 100)){
		cm.gainItem(4000255, 1);
		cm.gainItem(4000256, -100);
		cm.sendOk("Nice job, Here is your Yellow Wish Ticket");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 167) {
		if (cm.haveItem(4000256, 500)){
		cm.gainItem(4000255, 5);
		cm.gainItem(4000256, -500);
		cm.sendOk("Nice job, Here is your Yellow Wish Ticket");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 168) {
		if (cm.haveItem(4000256, 1000)){
		cm.gainItem(4000255, 10);
		cm.gainItem(4000256, -1000);
		cm.sendOk("Nice job, Here is your Yellow Wish Ticket");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 169) {
		if (cm.haveItem(4000256, 2000)){
		cm.gainItem(4000255, 20);
		cm.gainItem(4000256, -2000);
		cm.sendOk("Nice job, Here is your Yellow Wish Ticket");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 170) {
		if (cm.haveItem(4000256, 5000)){
		cm.gainItem(4000255, 50);
		cm.gainItem(4000256, -5000);
		cm.sendOk("Nice job, Here is your Yellow Wish Ticket");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 171) {
		if (cm.haveItem(4000255, 1)){
		cm.gainItem(4000256, 100);
		cm.gainItem(4000255, -1);
		cm.sendOk("Nice job, Here is your Bubble");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 172) {
		if (cm.haveItem(4000255, 5)){
		cm.gainItem(4000256, 500);
		cm.gainItem(4000255, -5);
		cm.sendOk("Nice job, Here is your Bubble");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 173) {
		if (cm.haveItem(4000255, 10)){
		cm.gainItem(4000256, 1000);
		cm.gainItem(4000255, -10);
		cm.sendOk("Nice job, Here is your Bubble");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 174) {
		if (cm.haveItem(4000255, 20)){
		cm.gainItem(4000256, 2000);
		cm.gainItem(4000255, -20);
		cm.sendOk("Nice job, Here is your Bubble");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
else if (selection == 175) {
		if (cm.haveItem(4000255, 50)){
		cm.gainItem(4000256, 5000);
		cm.gainItem(4000255, -50);
		cm.sendOk("Nice job, Here is your Bubble");
		cm.dispose();
	  } else {
		cm.sendOk("You do not have enough Bubbles!!")
		cm.dispose();
		}
	}
}
}