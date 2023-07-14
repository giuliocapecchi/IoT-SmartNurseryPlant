package it.unipi;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Objects;

public class COAP_Client {
    CoapClient client_temp = new CoapClient("coap://127.0.0.1/temperature_actuator");
    CoapClient client_humidity = new CoapClient("coap://127.0.0.1/humidity_actuator");
    CoapClient client_co2 = new CoapClient("coap://127.0.0.1/co2_actuator");

    public void getResponse() {
        ArrayList<String> topics=new ArrayList<>();
        topics.add("temperature_actuator");
        topics.add("humidity_actuator");
        topics.add("co2_actuator");

        while(true){
            for(String t:topics){
                CoapResponse response;
                if(Objects.equals(t, "temperature_actuator")){
                    response = client_temp.get();
                } else if(Objects.equals(t, "humidity_actuator")){
                    response = client_humidity.get();
                } else{
                    response = client_co2.get();
                }

                byte[] payload = response.getPayload();
                String payloadString = new String(payload);
                JSONParser parser = new JSONParser();
                try {

                    JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));

                    Long value_long = (Long) jsonPayload.get("value");
                    int value = value_long.intValue();

                    /*   IP     TOPIC    STATE   ACTIVE     TIMESTAMP
                    *
                    * */

                    Connection connection = Database_manager.db_connection();

                    String query = "INSERT INTO iotproject.actuators (ip, topic, state, active) VALUES ('"+topic+"',"+value+ ");";
                    Database_manager.insert_executor(connection, query);
                    if(!Database_manager.close_connection(connection)) {
                        System.out.println("Errore in chiusura connessione col database\n");
                        System.exit(1);
                    }

                    CoapClient client = new CoapClient("coap://127.0.0.1/"+topic);
                    client.put(jsonObject.toJSONString(), MediaTypeRegistry.APPLICATION_JSON);


                } catch (ParseException e) {
                    System.out.println("Errore nel parsing JSON");
                }
            }
        }
    }
}
