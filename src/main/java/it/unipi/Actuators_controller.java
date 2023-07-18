package it.unipi;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Objects;

public class Actuators_controller {
    public static CoapClient client_temp;
    public static boolean new_client_temp=false;
    public static CoapClient client_humidity;
    public static boolean new_client_humidity=false;
    public static CoapClient client_co2;
    public static boolean new_client_co2=false;

    public static void getResponse() {
        ArrayList<String> topics=new ArrayList<>();
        topics.add("temperature_actuator");
        topics.add("humidity_actuator");
        topics.add("co2_actuator");

        while(true){
            for(String t:topics){
                CoapResponse response = null;
                if(Objects.equals(t, "temperature_actuator") && new_client_temp){
                    response = client_temp.get();
                    new_client_temp=false;
                } else if(Objects.equals(t, "humidity_actuator") && new_client_humidity){
                    response = client_humidity.get();
                    new_client_humidity=false;
                } else if(Objects.equals(t, "co2_actuator") && new_client_co2){
                    response = client_co2.get();
                    new_client_co2=false;
                }

                if(response==null) // no get method has been called
                    continue;

                byte[] payload = response.getPayload();
                if (payload==null)
                    continue;
                String payloadString = new String(payload);
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));
                    Long value_long = (Long) jsonPayload.get("value");
                    int value = value_long.intValue();

                    String ip_client = null;
                    if(Objects.equals(t, "temperature_actuator")){
                        ip_client= client_temp.getURI();
                    } else if(Objects.equals(t, "co2_actuator")){
                        ip_client= client_co2.getURI();
                    } else if(Objects.equals(t, "humidity_actuator")){
                        ip_client=client_humidity.getURI();
                    }

                    /*   IP     TOPIC    STATE   TIMESTAMP */
                    Connection connection = Database_manager.db_connection();
                    String query = "INSERT INTO iotproject.actuators (ip, topic, state) VALUES ('"+ip_client+"','"+t+"',"+value+");";
                    assert connection != null;
                    Database_manager.insert_executor(connection, query);
                    if(!Database_manager.close_connection(connection)) {
                        System.out.println("Error closing connection with database\n");
                        System.exit(1);
                    }


                } catch (ParseException e) {
                    System.out.println("Error parsing JSON");
                }
            }
        }
    }

    static class Control_Loop implements Runnable {
        @Override
        public void run() {
            while (true) {
                getResponse();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
