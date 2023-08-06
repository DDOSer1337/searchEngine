package searchengine.Busines.LinkHandling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private Connection connection;
    private String dbName = "skillbox";
    private String dbUser = "root";
    private String dbPass = "Server.2002.name";

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/" + dbName +
                                "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" +
                                "&user=" + dbUser +
                                "&password=" + dbPass);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
