

/* @Author Lerk
 * 
 * 2201004.js: Papulatus Reactor: Performs the Papulatus commands
*/

function act(){
	rm.mapMessage(5, "The dimensional hole has been filled by the <Piece of Cracked Dimension>.");
	rm.changeMusic("Bgm09/TimeAttack");
	rm.spawnMonster(8500000, -410, -400);
}