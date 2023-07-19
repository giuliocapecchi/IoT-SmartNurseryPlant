package it.unipi.frontend;

import it.unipi.Database_manager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static it.unipi.Actuators_controller.*;
import static it.unipi.MyServer.setExit;


public class Frontend {
    private static boolean exit_control_panel = false;
    private static boolean exit_control_panel_actuators = false;
    private static boolean exit_control_panel_mes = false;

    static Scanner scanner = new Scanner(System.in);
    public static void start() throws SQLException, InterruptedException, IOException {
        String command= null;
        clearConsole();
        Logger.getLogger(Desktop.class.getName()).setLevel(Level.OFF);
        System.out.println("Welcome into Smart Plant Nursery application.");
        while (true) {
            // Print the menu
            if (!Objects.equals(command, "1")) {
                System.out.println("Choose an option:");
                System.out.println("1. Help");
                System.out.println("2. Status of the system");
                System.out.println("3. Measurements of sampled values");
                System.out.println("4. Send commands to actuators");
                System.out.println("5. Open Grafana");
                System.out.println("6. Exit");
            }


            //Read input from user
            System.out.print("Input->");
            command = scanner.nextLine();

            // Execute the action relative to the command inserted
            switch (command) {
                case "1":
                    clearConsole();
                    System.out.println("HELP MENU\n+-----------------------------------------------+\n" +
                            "1. Help: Show this menu\n2. Show which sensors and actuators are " +
                            "connected to the system and their status\n3. Shows a list of latest measurement values for " +
                            "each sensor\n4. Show a submenu with the state of the actuator and handle their behaviour\n" +
                            "5. Open the default browser on the Grafana page for this system.\n6. Exit from this application");
                    break;
                case "2":
                    clearConsole();
                    System.out.println("**Sensors and actuators status panel**");
                    exit_control_panel=false;

                    Thread refreshActuatorsThread = new Thread(() -> {

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
                    refreshActuatorsThread.start();

                    while (true) {
                        String input = scanner.nextLine();
                        if (input.equalsIgnoreCase("q")) {
                            exit_control_panel = true;
                            clearConsole();
                            break;
                        }else{
                            System.out.println("Invalid input!");
                            System.out.print("Input->");
                        }
                    }
                    break;
                case "3":
                    Thread refreshMeasurementsThread = new Thread(() -> {
                        exit_control_panel_mes = false;
                        while (!exit_control_panel_mes) {
                            try {
                                refreshMeasurements();
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
                    refreshMeasurementsThread.start();

                    while (true) {
                        String input = scanner.nextLine();
                        if (input.equalsIgnoreCase("q")) {
                            exit_control_panel_mes = true;
                            clearConsole();
                            break;
                        }else{
                            System.out.println("Invalid input!");
                            System.out.print("Input->");
                        }
                    }
                    break;
                case "4":
                    clearConsole();
                    Thread refreshActuatorThread = new Thread(() -> {
                        exit_control_panel_actuators = false;
                        while (!exit_control_panel_actuators) {
                            get_actuators_commands();
                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    refreshActuatorThread.start();

                    while (true) {

                        String input = scanner.nextLine();
                        String[] commands = input.split(" ");

                        if (commands.length == 2) {
                            try {
                                int actuator = Integer.parseInt(commands[0]);
                                int state = Integer.parseInt(commands[1]);
                                int co2_status = coapResponse(client_co2);
                                int humidity_status = coapResponse(client_humidity);
                                int temperature_status = coapResponse(client_temp);
                                // Check if the values are correct
                                if (actuator >= 1 && actuator <= 3 && state >= 0 && state <= 3) {
                                    System.out.println("Setting actuator " + actuator + " to state " + state+"...");
                                    CoapResponse response;
                                    JSONObject payload = new JSONObject();
                                    payload.put("value",state);
                                    if(actuator==1 && co2_status!=-1){
                                        response = client_co2.put("status="+payload.toJSONString(),MediaTypeRegistry.APPLICATION_JSON);
                                    }else if (actuator==2 && humidity_status!=-1){
                                        response = client_humidity.put("status="+payload.toJSONString(),MediaTypeRegistry.APPLICATION_JSON);
                                    }else if(actuator==3 && temperature_status!=-1){
                                        response = client_temp.put("status="+payload.toJSONString(),MediaTypeRegistry.APPLICATION_JSON);
                                    }else{
                                        clearConsole();
                                        System.out.println("---->Actuator disconnected! Updating table...");
                                        continue;
                                    }
                                    // Check the answer
                                    if (response != null) {
                                        clearConsole();
                                        System.out.println("---->Set. Updating table...");
                                        //System.out.println("Response Code: " + response.getCode());
                                        //System.out.println("Response Payload: " + response.getResponseText());
                                    } else {
                                        System.out.println("No response received.");
                                    }
                                } else {
                                    System.out.println("Invalid actuator or state.");
                                    System.out.print("Command->");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input format.");
                            }
                        } else if (input.equalsIgnoreCase("q")) {
                            exit_control_panel_actuators = true;
                            clearConsole();
                            break;
                        } else {
                            System.out.println("Invalid command format.");
                            System.out.print("Command->");
                        }
                    }

                    break;
                case "5":
                    System.out.println("Opening browser on the Grafana homepage...");
                    try {
                        Desktop.getDesktop().browse(new URI("http://localhost:3000/?orgId=1&viewPanel=3"));
                    } catch (IOException | URISyntaxException e) {
                        System.out.println("Failed to open browser.");
                    }
                    Thread.sleep(4000);
                    clearConsole();
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

    private static void get_actuators_commands(){
        clearConsole();

        System.out.println("ACTUATORS MANAGEMENT MENU");
        System.out.println("Send a command with this format : 'ACTUATOR_ID STATE'. Avaiable commands are: 0: off state, 1: force into state 1, 2: force into state 2, 3: force into state 3. Type 'q' to exit this menu.");

        int co2_status = coapResponse(client_co2);
        int humidity_status = coapResponse(client_humidity);
        int temperature_status = coapResponse(client_temp);

        System.out.println("+-----------------------------------------------+");
        System.out.println("| ID\t| ACTUATOR\t| CONNECTED\t|STATE\t|");
        System.out.println("+-----------------------------------------------+");
        System.out.println("| 1\t| co2\t\t| "+((co2_status==-1)? "NO":"YES")+"\t\t| "+((co2_status==-1)?"":co2_status)+"\t|");
        System.out.println("| 2\t| humidity\t| "+((humidity_status==-1)? "NO":"YES")+"\t\t| "+((humidity_status==-1)?"":humidity_status)+"\t|");
        System.out.println("| 3\t| temperature\t| "+((temperature_status==-1)? "NO":"YES")+"\t\t| "+((temperature_status==-1)?"":temperature_status)+"\t|");
        System.out.println("+-----------------------------------------------+");
        System.out.print("Command->");
    }





    private static int coapResponse(CoapClient client) {
        if(client==null)
            return -1;
        client.setTimeout(2000);
        CoapResponse response = client.get();
        if(response!=null){
            byte[] payload = response.getPayload();
            String payloadString = new String(payload);
            JSONParser parser = new JSONParser();
            try {
                JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));
                Long value_long = (Long) jsonPayload.get("value");
                return value_long.intValue();
            } catch (ParseException e) {
                System.out.println("Error parsing JSON");
            }
        }

        return -1;
    }

    private static void refreshMeasurements() throws SQLException {
        clearConsole();
        System.out.println("MEASUREMENTS");
        Connection connection = Database_manager.db_connection();
        String query = "SELECT s.topic, s.value, s.timestamp\n" +
                "FROM sensors s\n" +
                "INNER JOIN (\n" +
                "    SELECT topic, MAX(timestamp) AS max_timestamp\n" +
                "    FROM sensors\n" +
                "    GROUP BY topic\n" +
                ") t ON s.topic = t.topic AND s.timestamp = t.max_timestamp;\n";
        //System.out.println("query:"+ query+"\n");
        if(connection==null){
            System.out.println("Failed to connect to DB!\n");
            System.exit(1);
        }
        ResultSet resultSet = Database_manager.query_executor(connection, query);
        if(resultSet != null) {
            System.out.println("+-----------------------------------------------+");
            System.out.println("| ID\t| SENSOR\t| CONNECTED\t| VALUE\t|");
            System.out.println("+-----------------------------------------------+");
            int i = 0 ;
            while(resultSet.next()){
                Timestamp ts = resultSet.getTimestamp("timestamp");
                Timestamp actual_timestamp = new Timestamp(System.currentTimeMillis());
                String topic = resultSet.getString("topic");
                float value =  resultSet.getFloat("value");
                if ((actual_timestamp.getTime() - ts.getTime()) > 20000) {
                    System.out.println("| "+i+"\t| "+topic+(Objects.equals(topic, "co2") ?"\t":"")+"\t|  NO\t\t| "+value+"\t|");
                } else {
                    System.out.println("| "+i+"\t| "+topic+(Objects.equals(topic, "co2") ?"\t":"")+"\t| YES\t\t| "+value+"\t|");
                }
                System.out.println("+-----------------------------------------------+");
                i++;
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

        System.out.println("Type 'q' to quit.");
        System.out.print("Input->");
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
            System.out.println("SYSTEM STATUS\n+-------------------------------------------------------+");
            System.out.println("| TYPE\t\t| TOPIC\t\t| CONNECTED\t| STATE\t|");
            System.out.println("+-------------------------------------------------------+");
            while(resultSet.next()){
                Timestamp ts = resultSet.getTimestamp("timestamp");
                Timestamp actual_timestamp = new Timestamp(System.currentTimeMillis());
                String topic = resultSet.getString("topic");
                if ((actual_timestamp.getTime() - ts.getTime()) > 20000) {
                    System.out.println("| sensor\t| "+topic+(Objects.equals(topic, "co2") ?"\t":"")+"\t| no\t\t|   /\t|");
                } else {
                    System.out.println("| sensor\t| "+topic+(Objects.equals(topic, "co2") ?"\t":"")+"\t| yes\t\t|   /\t|");
                }
                System.out.println("+-------------------------------------------------------+");
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

        int co2_status = coapResponse(client_co2);
        int humidity_status = coapResponse(client_humidity);
        int temperature_status = coapResponse(client_temp);

        System.out.println("| actuator\t| co2\t\t| "+((co2_status==-1)? "no":"yes")+"\t\t|   "+((co2_status==-1)?"":co2_status)+"\t|");
        System.out.println("+-------------------------------------------------------+");
        System.out.println("| actuator\t| humidity\t| "+((humidity_status==-1)? "no":"yes")+"\t\t|   "+((humidity_status==-1)?"":humidity_status)+"\t|");
        System.out.println("+-------------------------------------------------------+");
        System.out.println("| actuator\t| temperature\t| "+((temperature_status==-1)? "no":"yes")+"\t\t|   "+((temperature_status==-1)?"":temperature_status)+"\t|");
        System.out.println("+-------------------------------------------------------+");

        System.out.println("Type 'q' to quit.");
        System.out.print("Input->");
    }


    public static void clearConsole() {
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during clear console function");
        }
    }
}
