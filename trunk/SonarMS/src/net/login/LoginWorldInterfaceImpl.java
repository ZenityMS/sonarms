package net.login;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import net.login.remote.LoginWorldInterface;

/**
 *
 * @author Matze
 */
public class LoginWorldInterfaceImpl extends UnicastRemoteObject implements LoginWorldInterface {
    public LoginWorldInterfaceImpl() throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public void channelOnline(int channel, String ip) throws RemoteException {
        LoginServer.getInstance().addChannel(channel, ip);
    }

    public void channelOffline(int channel) throws RemoteException {
        LoginServer.getInstance().removeChannel(channel);
    }

    public void shutdown() throws RemoteException {
        LoginServer.getInstance().shutdown();
    }

    public boolean isAvailable() throws RemoteException {
        return true;
    }
}
