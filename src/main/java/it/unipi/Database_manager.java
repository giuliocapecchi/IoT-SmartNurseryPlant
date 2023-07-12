package it.unipi;
import java.sql.*;


public class Database_manager {
    public static Connection db_connection() {
        Connection connection = null;
        try {
            String url = "jdbc:mysql://localhost:3306/iotproject";
            String username = "root";
            String password = "PASSWORD";
            connection = DriverManager.getConnection(url, username, password);
            //System.out.println("Connessione al database stabilita!");
            return connection;
        } catch (SQLException e) {
            System.out.println("Errore durante la connessione al database: " + e.getMessage());
            return null;
        }
    }
    public static ResultSet query_executor(Connection connection, String query){
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            return resultSet;

        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }
        return null;
    }

        public static void insert_executor(Connection connection, String query){
            Statement statement = null;
            try {
                statement = connection.createStatement();
               statement.executeUpdate(query);

            } catch (SQLException e) {
                System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
            } finally {
                // Chiudi lo Statement
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public static boolean close_connection(Connection connection) {
            if (connection != null) {
                try {
                    connection.close();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

}
