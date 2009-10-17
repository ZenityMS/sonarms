package client.command;

public interface CommandProcessorMBean {
	String processCommandJMX(int cserver, int mapid, String command);
}