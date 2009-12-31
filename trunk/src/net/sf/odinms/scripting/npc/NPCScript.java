package net.sf.odinms.scripting.npc;

public interface NPCScript {
	
	public void start();
	public void action(byte mode, byte type, byte selection);

}
