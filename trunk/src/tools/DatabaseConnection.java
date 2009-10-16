package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class DatabaseConnection {
    private static ThreadLocal<Connection> con = new ThreadLocalConnection();
    private static Properties props = null;

    public static Connection getConnection() {
        if (props == null)
            throw new RuntimeException("DatabaseConnection not initialized");
        return con.get();
    }

    public static boolean isInitialized() {
        return props != null;
    }

    public static void setProps(Properties aProps) {
        props = aProps;
    }

    public static void closeAll() throws SQLException {
        for (Connection con : ThreadLocalConnection.allConnections)
            con.close();
    }

    private static class ThreadLocalConnection extends ThreadLocal<Connection> {
        public static Collection<Connection> allConnections = new LinkedList<Connection>();

        @Override
        protected Connection initialValue() {
            String driver = props.getProperty("driver");
            String url = props.getProperty("url");
            String user = props.getProperty("user");
            String password = props.getProperty("password");
            try {
                Class.forName(driver); // touch the mysql driver
            } catch (ClassNotFoundException e) {
            }
            try {
                Connection con = DriverManager.getConnection(url, user, password);
                allConnections.add(con);
                return con;
            } catch (SQLException e) {
                return null;
            }
        }
    }
}