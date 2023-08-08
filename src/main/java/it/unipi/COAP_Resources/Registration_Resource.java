package it.unipi.COAP_Resources;
import it.unipi.Actuators_controller;
import it.unipi.Database_manager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.sql.Connection;
import java.util.Objects;


public class Registration_Resource extends CoapResource {
    public Registration_Resource(String name) {
        super(name);
        setObservable(true);
    }

    public void handlePOST(CoapExchange exchange){
        //method called by the Collector to update the resource state
        String payload = new String(exchange.getRequestPayload());

        if(payload==null){
            System.out.println("NULL payload");
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Error in JSON request");
            return;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonPayload = (JSONObject) parser.parse(payload);
            String topic = jsonPayload.get("topic").toString();
            Long value  = (Long) jsonPayload.get("value");
            if(value == null || topic== null){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Error in JSON request");
                return;
            }

            Connection connection = Database_manager.db_connection();
            String ip = exchange.getSourceAddress().getHostAddress();
            String query= "INSERT into iotproject.actuators (ip, topic, state) VALUES ('"+ip+"', '"+topic+"', "+ value +");";
            assert connection != null;
            Database_manager.insert_executor(connection, query);
            if(!Database_manager.close_connection(connection)) {
                System.out.println("Error closing connection with database\n");
                System.exit(1);
            }

            if(Objects.equals(topic, "temperature")){
                Actuators_controller.client_temp = new CoapClient("coap://["+ip+"]/"+topic+"_actuator");
            }else if(Objects.equals(topic, "co2")){
                Actuators_controller.client_co2 = new CoapClient("coap://["+ip+"]/"+topic+"_actuator");
            }else if(Objects.equals(topic, "humidity")){
                Actuators_controller.client_humidity = new CoapClient("coap://["+ip+"]/"+topic+"_actuator");
            }

            // Send a response to the client
            exchange.respond(CoAP.ResponseCode.CREATED);
        } catch (ParseException e) {
            System.out.println("errore in json request");
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Error in JSON request");
        }
    }
}
