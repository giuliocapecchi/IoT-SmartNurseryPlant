package it.unipi.COAP_Resources;

import it.unipi.Actuators_controller;
import it.unipi.Database_manager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;


public class Registration_Resource extends CoapResource {
    public Registration_Resource(String name) {
        super(name);
        setObservable(true);
    }

    public void handlePOST(CoapExchange exchange) {
        //method called by the Collector to update the resource state

        byte[] payload = exchange.getRequestPayload();

        // Convert the payload into a string
        String payloadString = new String(payload);
        JSONParser parser = new JSONParser();
        try {

            JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));

            Long value = (Long) jsonPayload.get("value");
            String topic = jsonPayload.get("topic").toString();

            Connection connection = Database_manager.db_connection();
            String query= "INSTER into iotproject.actuators (uri, topic, state) VALUES ('"+exchange.getSourceAddress()+
                    "', '"+topic+"', "+value.toString()+");";
            // Send a response to the client
            exchange.respond(CoAP.ResponseCode.CREATED);
        } catch (ParseException e) {
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Error in JSON request");
        }
    }
}
