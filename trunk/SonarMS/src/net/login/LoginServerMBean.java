package net.login;

public interface LoginServerMBean {
	int getNumberOfSessions();
	int getLoginInterval();
	String getEventMessage();
	int getFlag();
	int getUserLimit();
}
