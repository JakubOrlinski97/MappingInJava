package GPS;


import java.sql.*;
import java.util.ArrayList;

public class TransactionHandler {
    private final static String DBURL = "jdbc:mysql://localhost:3306/MappingInJava" + "?verifyServerCertificate=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
            + "&useSSL=true";
    private final static String username = "GeoJake";
    private final static String password = "HP*aeVE@3G69Ta!xB5lfJg2Xh2";


    private synchronized Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DBURL, username, password);
        } catch (SQLException e) {
            System.out.println("DATABASE NOT FOUND");
        }
        return conn;
    }

    public synchronized void addNewID(String ID, String hash) {
        String sqlInsert = "INSERT INTO users(client_id, hash, update_time) VALUES(?, ?, ?)";
        String sqlCreate = String.format("CREATE TABLE IF NOT EXISTS %s (\n" +
                "client_id text, \n" +
                "latitude text, \n" +
                "longitude text, \n" +
                "update_time bigint primary key \n" +
                ")", ID);

        PreparedStatement p1 = null;
        PreparedStatement p2 = null;
        Connection conn = connect();



        if (conn == null) {
            System.out.println("No connection to db!");
            return;
        }

        try {

            p1 = conn.prepareStatement(sqlInsert);
            p1.setString(1, ID);
            p1.setString(2, hash);
            p1.setLong(3, System.currentTimeMillis());

            p2 = conn.prepareStatement(sqlCreate);

            conn.setAutoCommit(false);

            int rowsAffected = p1.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Didn't succeed!");
                conn.rollback();
            }

            p2.executeUpdate();

            conn.commit();
            System.out.println("User added and personal table created!");
        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn, p1, p2);
        }
    }

    public synchronized void removeID(String clientId) {
        //TODO
    }

    public synchronized ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<>();

        String sqlGet = "SELECT client_id FROM users";

        Connection conn = connect();

        if (conn == null) {
            return null;
        }

        try (Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sqlGet)) {

            while (rs.next()) {
                users.add(rs.getString("client_id"));
            }


        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn);
        }

        return users;
    }

    public synchronized String getHash(String ID) {
        String sqlGet = "SELECT hash FROM users WHERE client_id = ?";

        PreparedStatement p1 = null;
        Connection conn = connect();
        String hash = "";

        if (conn == null) {
            return null;
        }

        try {
            p1 = conn.prepareStatement(sqlGet);
            p1.setString(1, ID);

            ResultSet rs = p1.executeQuery();

            if (rs.next()) {
                hash = rs.getString("hash");
                System.out.println("Hash obtained! " + hash);
            }

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

    public synchronized void addNewLocation(String clientId, String latitude, String longitude, String time) {

        String sqlInsert = String.format("INSERT INTO %s(client_id, latitude, longitude, update_time) VALUES(?, ?, ?, ?)", clientId);

        PreparedStatement p1 = null;
        Connection conn = connect();

        if (conn == null) {
            return;
        }

        try {
            p1 = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            p1.setString(1, clientId);
            p1.setString(2, latitude);
            p1.setString(3, longitude);
            p1.setLong(4, Long.parseLong(time));

            int rowsAffected = p1.executeUpdate();
            System.out.println("New location added!");

            if (rowsAffected != 1) {
                conn.rollback();
            }

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

    public ArrayList<ArrayList<String>> getLocation(String id) {
        ArrayList<ArrayList<String>> location = new ArrayList<>();

        String sqlGet = String.format("SELECT latitude, longitude, update_time FROM %s", id);

        if (!getUsers().contains(id)) {
            System.out.println("The user doesn't exist!");
            return null;
        }

        Connection conn = connect();
        Statement p1;

        if (conn == null) {
            return null;
        }

        try {
            p1 = conn.createStatement();

            ResultSet rs = p1.executeQuery(sqlGet);

            while (rs.next()) {
                ArrayList<String> placeholder = new ArrayList<>();
                placeholder.add(rs.getString("latitude"));
                placeholder.add(rs.getString("longitude"));
                placeholder.add(rs.getString("update_time"));

                location.add(placeholder);
            }


        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("COULDN'T ROLL BACK");
            }

        } finally {
            closeDBConnection(conn);
        }

        return location;
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



    public synchronized void deploy() {
        String sqlTable = "CREATE TABLE IF NOT EXISTS users (\n" +
                "client_id varchar(30) PRIMARY KEY , \n" +
                "hash text, \n" +
                "update_time bigint\n" +
                ")";

        String sqlDrop = "DROP TABLE IF EXISTS users";

        Connection conn = null;
        PreparedStatement p1 = null;
        PreparedStatement p2 = null;

        try {
            conn = DriverManager.getConnection(DBURL, username, password);
            p1 = conn.prepareStatement(sqlTable);
            p2 = conn.prepareStatement(sqlDrop);

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("The driver's name is: " + meta.getDriverName());
            System.out.println("A new database has been created!");

            p2.execute();
            p1.execute();
            System.out.println("The user table has been created!");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDBConnection(conn, p1, p2);
        }
    }
}
