package dev.arttention.influencers.bungee.provider;

import dev.arttention.influencers.bungee.configuration.MySQLDefaultConfig;
import dev.arttention.libraries.api.mysql.MySQL;
import dev.arttention.libraries.api.mysql.MySQLAPI;
import dev.arttention.libraries.api.mysql.entity.MySQLAuthentication;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class MySQLProvider {

    private final MySQLDefaultConfig mySQLDefaultConfig;
    private static MySQLProvider mySQLProvider;

    public MySQLProvider(MySQLDefaultConfig mySQLDefaultConfig) {
        mySQLProvider = this;
        this.mySQLDefaultConfig = mySQLDefaultConfig;
    }

    private static final HashMap<String, MySQL> connections = new HashMap<>();

    public void connect(String database) {
        MySQLAuthentication mySQLAuthentication = new MySQLAuthentication(mySQLDefaultConfig.getUrl() + "/" + database,
                mySQLDefaultConfig.getUser(), mySQLDefaultConfig.getPassword());
        connections.put(database, new MySQLAPI(mySQLAuthentication));
        System.out.print("[YoutuberManager] Verbindung mit Datenbank '" + database + "' aufgebaut!");
    }

    public void disconnect(String database) throws SQLException {
        Connection connection = (Connection) connections.get(database);
        connection.close();
    }

    public MySQL getConnection(String database) {
        if (connections.containsKey(database)) {
            return connections.get(database);
        }
        return null;
    }

    public static HashMap<String, MySQL> getConnections() {
        return connections;
    }

    public void closeConnections() {
        if (!connections.isEmpty()) {
            connections.forEach((database, mySQL) -> {
                try {
                    Connection connection = (Connection) mySQL;
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            connections.clear();
        }
    }

    public static MySQLProvider getInstance() {
        return mySQLProvider;
    }


}
