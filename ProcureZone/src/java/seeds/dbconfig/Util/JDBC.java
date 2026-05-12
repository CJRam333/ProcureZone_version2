package seeds.dbconfig.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import seeds.login.daoImpl.Test1;
import seeds.login.daoImpl.Test2;

/**
 *
 * @author ramesh.avv
 */
public class JDBC {
    private static ConnPool pool = new ConnPool();  // Shared pool instance

    public static Connection getConn() throws SQLException, IOException {
        return getConnection();
    }

    public static Connection getConnection() throws SQLException, IOException {
        Connection conn = null;
        try {
            conn = pool.getConnection();  // Use shared pool

            // Start threads only once (move these to a static block or main app context if needed)
            // Test1 test1 = new Test1(); test1.start();
            // Test2 test2 = new Test2(); test2.start();
        } catch (Exception e) {
            System.err.println("Initial connection failed, retrying once...");
            conn = pool.getConnection();  // Retry using same pool
        }
        return conn;
    }
}
