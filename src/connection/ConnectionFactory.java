package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public static Connection conectar(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha) throws SQLException {

        String url = "jdbc:postgresql://"
                + host + ":"
                + porta + "/"
                + banco
                + "?sslmode=prefer";

        return DriverManager.getConnection(
                url,
                usuario,
                senha
        );
    }
}
