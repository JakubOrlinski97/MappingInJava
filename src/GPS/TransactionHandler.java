package GPS;

import java.sql.*;

public class TransactionHandler {
    private final static String DBURL = "jdbc:sqlite:/home/jakethesnake/Projects/MappingInJava/src/";
    private String dbName = "test.db";


    public synchronized Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DBURL + dbName);
        } catch (SQLException e) {
            System.out.println("DATABASE NOT FOUND");
        }
        return conn;
    }

    public synchronized void addNewID(String ID, String hash) {
        String sqlInsert = "INSERT INTO users(id, hash) VALUES(?, ?)";
        String sqlCreate = "CREATE TABLE IF NOT EXISTS ? (\n" +
                "client_id integer, \n" +
                "latitude double, \n" +
                "longitude double, \n" +
                "update_time integer PRIMARY KEY\n" +
                ")";

        PreparedStatement p1 = null;
        PreparedStatement p2 = null;
        Connection conn = connect();

        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(false);

            p1 = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            p1.setString(1, ID);
            p1.setString(2, hash);

            p2 = conn.prepareStatement(sqlCreate, Statement.RETURN_GENERATED_KEYS);
            p2.setString(1, ID);

            int rowsAffected = p1.executeUpdate();

            if (rowsAffected != 1) {
                conn.rollback();
            }

            rowsAffected = p2.executeUpdate();

            if (rowsAffected != 1) {
                conn.rollback();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn, p1, p2);
        }
    }

    public void removeID(String clientId) {

    }

    public synchronized String getHash(String ID) {
        String sqlGet = "SELECT id FROM users WHERE id = ?";

        PreparedStatement p1 = null;
        Connection conn = connect();
        String hash = "";

        if (conn == null) {
            return null;
        }

        try {
            p1 = conn.prepareStatement(sqlGet, Statement.RETURN_GENERATED_KEYS);
            p1.setString(1, ID);

            p1.execute();

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn, p1);
        }
        return hash;
    }

    public void addNewLocation(String clientId, String latitude, String longitude, String time) {

        String sqlInsert = "INSERT INTO ?(id, latitude, longitude, update_time) VALUES(?, ?, ?, ?)";

        PreparedStatement p1 = null;
        Connection conn = connect();

        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(false);

            p1 = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            p1.setString(1, clientId);
            p1.setString(2, clientId);
            p1.setString(3, latitude);
            p1.setString(4, longitude);
            p1.setString(5, time);

            int rowsAffected = p1.executeUpdate();

            if (rowsAffected != 1) {
                conn.rollback();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn, p1);
        }
    }

    private synchronized void closeDBConnection(Connection conn, PreparedStatement... p) {
        try {
            for (PreparedStatement statement: p) {
                if (statement != null) {
                    statement.close();
                }
            }
            conn.close();
        } catch (SQLException e3) {
            System.out.println(e3.getMessage());
        }
    }



    public synchronized void deploy(String filename) {
        String url = DBURL + filename + ".db";
        dbName = filename + ".db";

        try (Connection conn = DriverManager.getConnection(url)) {

            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver's name is: " + meta.getDriverName());
                System.out.println("A new database - " + filename + " - has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
