package it.unipi.COAP_Resources;

import it.unipi.Database_manager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;


public class Measurement_Resource extends CoapResource {

    int value;

    {
        try {
            value = initialize();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Measurement_Resource(String name) {
        super(name);
        setObservable(true);
    }
    public void handleGET(CoapExchange exchange) {
        JSONObject jsonObject = new JSONObject();
        if(Objects.equals(this.getName(), "co2")){
            value=value/10;
        }
        jsonObject.put("value", value);
        exchange.respond(CoAP.ResponseCode.CONTENT,jsonObject.toJSONString(), MediaTypeRegistry.APPLICATION_JSON);
        if(Objects.equals(this.getName(), "co2")){
            value=value*10;
        }
        System.out.println("richiesta GET ricevuta!\n");
        System.out.println("Richiesta ricevuta da: "+exchange.getSourceAddress()+"\n");
    }

    public void handlePUT(CoapExchange exchange) {
        //method called by the Collector to update the resource state

        byte[] payload = exchange.getRequestPayload();

        // Convert the payload into a string
        String payloadString = new String(payload);
        JSONParser parser = new JSONParser();
        try {

            JSONObject jsonPayload = (JSONObject) parser.parse(new String(payloadString.getBytes(), StandardCharsets.UTF_8));

            Long value_long = (Long) jsonPayload.get("value");
            value = value_long.intValue();

           // Send a response to the client
            exchange.respond(CoAP.ResponseCode.CREATED);

        } catch (ParseException e) {
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Errore nella richiesta JSON");
        }

    }

    int initialize() throws SQLException {
        String name = this.getName();
        Connection connection = Database_manager.db_connection();
        String query = "SELECT value FROM iotproject.sensors WHERE topic='"+name+"' ORDER BY timestamp DESC LIMIT 1;";
        //System.out.println("name:"+name+"\n"+query+"\n");
        float query_value = 0;
        if(connection==null){
            System.out.println("Failed to connect to DB!\n");
            System.exit(1);
        }
        ResultSet resultSet = Database_manager.query_executor(connection, query);
        if(resultSet != null) {
            resultSet.next();
            query_value = resultSet.getFloat("value");
            //System.out.println("value : "+query_value);
            // Chiudi il ResultSet
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Empty resultset");
        }
        if(!Database_manager.close_connection(connection)) {
            System.out.println("Errore in chiusura connessione col database\n");
            System.exit(1);
        }
        return ((int) query_value);
    }
}

