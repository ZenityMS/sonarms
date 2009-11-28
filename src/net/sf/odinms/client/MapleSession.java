package net.sf.odinms.client;

import java.net.SocketAddress;

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.support.BaseIoSession;

public class MapleSession extends BaseIoSession {

	@Override
	protected void updateTrafficMask() {
		
	}

	@Override
	public IoSessionConfig getConfig() {
		return null;
	}

	@Override
	public IoFilterChain getFilterChain() {
		return null;
	}

	@Override
	public IoHandler getHandler() {
		return null;
	}

	@Override
	public SocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public IoService getService() {
		return null;
	}

	@Override
	public SocketAddress getServiceAddress() {
		return null;
	}

	@Override
	public IoServiceConfig getServiceConfig() {
		return null;
	}

	@Override
	public TransportType getTransportType() {
		return null;
	}

}
