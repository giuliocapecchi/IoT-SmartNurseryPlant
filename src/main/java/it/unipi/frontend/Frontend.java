package it.unipi.frontend;

import it.unipi.Database_manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Scanner;
import java.io.IOException;

import static it.unipi.MyServer.setExit;

public class Frontend {
    private static boolean exit_control_panel = false;
    public static void start() {
        Scanner scanner = new Scanner(System.in);
        String command= null;
        clearConsole();
        System.out.println("Welcome into Smart Plant Nursery application.");

        while (true) {
            // Stampa il menu
            if (!Objects.equals(command, "1")) {
                System.out.println("Choose an option:");
                System.out.println("1. Help");
                System.out.println("2. Status of your system");
                System.out.println("3. Measurements of sampled values");
                System.out.println("4. Send commands to actuators");
                System.out.println("5. Open Grafana");
                System.out.println("6. Exit");
            }


            // Leggi l'input dell'utente
            System.out.print("Input->");
            command = scanner.nextLine();

            // Esegui l'azione corrispondente al comando inserito
            switch (command) {
                case "1":
                    clearConsole();
                    System.out.println("**Help menu**\n1. Help: Show this menu\n2. Show which sensors and actuators are " +
                            "connected to the system and their status\n3.Shows a list of latest measurement values for " +
                            "each sensor\n4.Show a submenu with the state of the actuator and handle their behaviour\n" +
                            "5. Open the default browser on the Grafana page for this system.\n6. Exit from this application");
                    break;
                case "2":
                    clearConsole();
                    System.out.println("**Sensors and actuators status panel**");
                    exit_control_panel=false;

                    Thread refreshThread = new Thread(() -> {

                        while (!exit_control_panel) {
                            try {
                                refreshSystemStatus();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    refreshThread.start();

                    while (true) {
                        String input = scanner.nextLine();
                        if (input.equalsIgnoreCase("exit")) {
                            exit_control_panel = true;
                            clearConsole();
                            break;
                        }
                    }
                    break;
                case "3":
                    System.out.println("Misure...");

                    break;
                case "4":
                    System.out.println("Comandi per gli attuatori...");
                    break;
                case "5":
                    System.out.println("Grafana...");
                    break;
                case "6":
                    System.out.println("Exit...");
                    setExit(true);
                    return;
                default:
                    clearConsole();
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void refreshSystemStatus() throws SQLException {
        clearConsole();
        // Get Database information about sensors and actuators
        Connection connection = Database_manager.db_connection();
        String query = "SELECT s.topic, s.value, s.timestamp\n" +
                "FROM sensors s\n" +
                "INNER JOIN (\n" +
                "    SELECT topic, MAX(timestamp) AS max_timestamp\n" +
                "    FROM sensors\n" +
                "    GROUP BY topic\n" +
                ") t ON s.topic = t.topic AND s.timestamp = t.max_timestamp;\n";
        //System.out.println("query\n");
        if(connection==null){
            System.out.println("Failed to connect to DB!\n");
            System.exit(1);
        }
        ResultSet resultSet = Database_manager.query_executor(connection, query);
        if(resultSet != null) {
            System.out.println("SYSTEM STATUS\n+-------------+-------+---------------------+");
            while(resultSet.next()){
                Timestamp ts = resultSet.getTimestamp("timestamp");
                Timestamp actual_timestamp = new Timestamp(System.currentTimeMillis());
                String topic = resultSet.getString("topic");
                if ((actual_timestamp.getTime() - ts.getTime()) > 20000) {
                    System.out.println("| "+topic+" sensor : DISCONNECTED;\t|");
                } else {
                    System.out.println("| "+topic+" sensor : CONNECTED;\t|");
                }
                System.out.println("+-------------+-------+---------------------+");
            }
            // Close the ResultSet
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Empty resultset");
        }

        query = "SELECT a.topic, a.state, a.timestamp\n" +
                "FROM actuators a\n" +
                "INNER JOIN (\n" +
                "    SELECT topic, MAX(timestamp) AS max_timestamp\n" +
                "    FROM actuators\n" +
                "    GROUP BY topic\n" +
                ") t ON a.topic = t.topic AND a.timestamp = t.max_timestamp;\n";
        resultSet = Database_manager.query_executor(connection, query);
        if(resultSet != null) {
            System.out.println("+-------------+-------+---------------------+");
            while(resultSet.next()){
                Timestamp ts = resultSet.getTimestamp("timestamp");
                Timestamp actual_timestamp = new Timestamp(System.currentTimeMillis());
                String topic = resultSet.getString("topic");
                if(Objects.equals(topic, "temperature_actuator")){
                    topic = "temperature";
                }else if(Objects.equals(topic, "humidity_actuator")){
                    topic = "humidity";
                }else if(Objects.equals(topic, "co2_actuator")){
                    topic = "co2";
                }
                if ((actual_timestamp.getTime() - ts.getTime()) > 20000) {
                    System.out.println("| "+topic+" actuator : DISCONNECTED;\t|");
                } else {
                    String state = resultSet.getString("state");
                    System.out.println("| "+topic+" actuator : CONNECTED, with STATE: "+state+"\t|");
                }
                System.out.println("+-------------+-------+---------------------+");
            }
            // Close the ResultSet
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Empty resultset");
        }

        if(!Database_manager.close_connection(connection)) {
            System.out.println("Error closing connection with database\n");
            System.exit(1);
        }

        System.out.println("Type 'exit' to quit.");
    }


    public static void clearConsole() {
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during clear console function");
        }
    }
}
