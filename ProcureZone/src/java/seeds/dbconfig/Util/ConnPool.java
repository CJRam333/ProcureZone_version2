package seeds.dbconfig.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

public class ConnPool {
    private int initialConnections = 5;
    private int maxConnections = 10;

    private Vector<Connection> connectionsAvailable = new Vector<Connection>();
    private Vector<Connection> connectionsUsed = new Vector<Connection>();

    private String connectionUrl = "jdbc:mysql://localhost:3306/seeds_indent?autoReconnect=true";
    private String userName = "root";
    private String userPassword = "password";
    // private String userPassword = "ezone160@172169160"; // Replace with your actual database password
    private String driver = "com.mysql.cj.jdbc.Driver";

    public ConnPool() {
        try {
            Class.forName(driver);
            initializeConnections();
            System.out.println("Connection pool initialized with " + initialConnections + " connections.");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver class not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error initializing connections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConnections() throws SQLException {
        for (int i = 0; i < initialConnections; i++) {
            Connection connection = createNewConnection();
            if (connection != null) {
                connectionsAvailable.addElement(connection);
            }
        }
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, userName, userPassword);
    }

    public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;

        int retryCount = 3;
        while (retryCount > 0) {
            try {
                connection = getConnectionFromPool();
                if (connection != null && !connection.isClosed() && connection.isValid(2)) {
                    break;
                } else {
                    System.err.println("Got invalid connection, retrying...");
                    retryCount--;
                }
            } catch (SQLException e) {
                System.err.println("Failed to get a connection. Retrying... (" + retryCount + ")");
                e.printStackTrace();
                retryCount--;
            }
        }

        if (connection == null) {
            throw new SQLException("Failed to get a connection after multiple retries.");
        }

        return connection;
    }

    private Connection getConnectionFromPool() throws SQLException {
        if (connectionsAvailable.isEmpty()) {
            // Check if we can create a new connection without exceeding maxConnections
            if (connectionsUsed.size() < maxConnections) {
                return createNewConnection();
            } else {
                throw new SQLException("Maximum pool size reached, no available connections.");
            }
        } else {
            Connection connection = connectionsAvailable.lastElement();
            connectionsAvailable.removeElement(connection);

            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = createNewConnection();
            }

            connectionsUsed.addElement(connection);
            return connection;
        }
    }

    public synchronized void releaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed() && connection.isValid(2)) {
                connectionsUsed.removeElement(connection);
                connectionsAvailable.addElement(connection);
            } else {
                connectionsUsed.removeElement(connection);
                // Do not add invalid/closed connections back to pool
                System.err.println("Released connection is invalid or closed; discarding.");
            }
        } catch (SQLException e) {
            System.err.println("Error releasing connection: " + e.getMessage());
        }
    }

    public synchronized void closeAllConnections() {
        for (Connection connection : connectionsAvailable) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        for (Connection connection : connectionsUsed) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        connectionsAvailable.clear();
        connectionsUsed.clear();
        System.out.println("All connections closed and pool cleared.");
    }

    public synchronized int availableCount() {
        return connectionsAvailable.size();
    }

    public synchronized int usedCount() {
        return connectionsUsed.size();
    }
}
