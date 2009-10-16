package net.login.handler;

//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import client.LoginCrypto;
import client.MapleCharacter;
import client.MapleClient;
import net.MaplePacketHandler;
import net.login.LoginWorker;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
//import tools.DatabaseConnection;
//import net.login.LoginServer;

public class LoginPasswordHandler implements MaplePacketHandler {
//    private boolean success;
//
//    private boolean getAccountExists(String name) {
//        boolean accountExists = false;
//        try {
//            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM accounts WHERE name = ?");
//            ps.setString(1, name);
//            if (ps.executeQuery().first())
//                accountExists = true;
//            ps.close();
//        } catch (Exception e) {
//        }
//        return accountExists;
//    }
//
//    private void createAccount(String login, String pwd, String eip) {
//        try {
//            PreparedStatement ipc = DatabaseConnection.getConnection().prepareStatement("SELECT lastknownip FROM accounts WHERE lastknownip = ?");
//            ipc.setString(1, eip.substring(1, eip.lastIndexOf(':')));
//            ResultSet rs = ipc.executeQuery();
//            if (!rs.first() || rs.last() && rs.getRow() < 5)//MAX 5
//                try {
//                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, lastknownip) VALUES (?, ?, ?, ?, ?, ?)");
//                    ps.setString(1, login);
//                    ps.setString(2, LoginCrypto.hexSha1(pwd));
//                    ps.setString(3, "");
//                    ps.setString(4, "0000-00-00");
//                    ps.setString(5, "00-00-00-00-00-00");
//                    ps.setString(6, eip.substring(1, eip.lastIndexOf(':')));
//                    ps.executeUpdate();
//                    ps.close();
//                    success = true;
//                } catch (Exception e) {
//                }
//            ipc.close();
//            rs.close();
//        } catch (Exception ex) {
//        }
//    }
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int loginok = 0;
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        c.setAccountName(login);
        boolean ipBan = c.hasBannedIP();
        boolean macBan = c.hasBannedMac();
//        if (LoginServer.getInstance().getAutoRegister()) {
//            if (getAccountExists(login))
//                loginok = c.login(login, pwd, ipBan || macBan);
//            else if (!ipBan || !macBan) {
//                createAccount(login, pwd, c.getSession().getRemoteAddress().toString());
//                if (success)
//                    loginok = c.login(login, pwd, ipBan || macBan);
//            }
//        } else
        loginok = c.login(login, pwd, ipBan || macBan);
        if (loginok == 0 && (ipBan || macBan)) {
            loginok = 3;
            if (macBan)
                MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false);
        }
        if (loginok != 0) {
            c.getSession().write(MaplePacketCreator.getLoginFailed(loginok));
            return;
        }
        LoginWorker.getInstance().registerClient(c);
    }
}