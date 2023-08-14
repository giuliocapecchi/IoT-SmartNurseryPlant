package it.unipi;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.crypto.Data;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;

public class Actuators_controller {

    public static CoapClient client_temp;
    public static CoapClient client_humidity;
    public static CoapClient client_co2;

    static Connection connection;

    public static void getValues() throws SQLException, ParseException {
        connection = Database_manager.db_connection();
        ArrayList<String> topics=new ArrayList<>();
        topics.add("temperature");
        topics.add("humidity");
        topics.add("co2");
        for(String t:topics){
            String query = "SELECT value FROM iotproject.sensors WHERE topic = '"+t+"' ORDER BY timestamp DESC LIMIT 1;";
            assert connection != null;
            ResultSet resultSet = Database_manager.query_executor(connection, query);
            CoapResponse response = null;
            String uri = null;
            if(resultSet != null) {
                resultSet.next();
                float value =  resultSet.getFloat("value");

                if(Objects.equals(t, "temperature")){
                    temperature_control(value);
                    if(client_temp!=null){
                        response = client_temp.get();
                        if(response!=null) {
                            uri = client_temp.getURI();
                        }else{
                            client_temp = null;
                            continue;
                        }
                    }else{
                        continue;
                    }
                }else if(Objects.equals(t, "co2")){
                    co2_control(value);
                    if(client_co2!=null){
                        response = client_co2.get();
                        if(response!=null) {
                            uri = client_co2.getURI();
                        }else{
                            client_co2 = null;
                            continue;
                        }
                    }else{
                        continue;
                    }
                }else if(Objects.equals(t, "humidity")){
                    humidity_control(value);
                    if(client_humidity!=null) {
                        response = client_humidity.get();
                        if (response != null) {
                            uri = client_humidity.getURI();
                        } else {
                            client_humidity = null;
                            continue;
                        }
                    }else{
                        continue;
                    }
                }

                int startIndex = uri.indexOf("[") + 1;
                int endIndex = uri.indexOf("]");
                String ip = uri.substring(startIndex, endIndex);

                byte[] payload = response.getPayload();
                if (payload==null)
                    continue;
                String payloadString = new String(payload);
                JSONParser parser = new JSONParser();
                JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));
                Long value_long = (Long) jsonPayload.get("value");
                int state = value_long.intValue();

                query= "INSERT into iotproject.actuators (ip, topic, state) VALUES ('"+ip+"', '"+t+"', "+ state +");";
                Database_manager.insert_executor(connection,query);

            }
        }

        if(!Database_manager.close_connection(connection)) {
            System.out.println("Error closing connection with database\n");
            System.exit(1);
        }

    }

    static void temperature_control(float value){
        CoapResponse response;
        JSONObject payload = new JSONObject();
        int state;
        payload.put("forced",0);
        if(value < 18){ // turn on heating system
            state = 1;
        }else if(value > 28){ //turn on refrigerator system
            state = 3;
        }else{ // situation is stable
            state = 2;
        }
        payload.put("value",state);
        if(client_temp!= null){
            response = client_temp.put(payload.toJSONString(), MediaTypeRegistry.APPLICATION_JSON);
            if(response==null)
                client_temp = null;
        }

    }

    static void co2_control(float value){

        if(value < 18){ // turn on heating system

        }else if(value > 28){ //turn on refrigerator system

        }else{ // situation is stable

        }
    }

    static void humidity_control(float value){

        if(value < 18){ // turn on heating system

        }else if(value > 28){ //turn on refrigerator system

        }else{ // situation is stable

        }
    }

    static class Control_Loop implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    getValues();
                } catch (SQLException | ParseException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
