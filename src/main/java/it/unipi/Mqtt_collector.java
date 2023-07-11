package it.unipi;
//import SmartSuit.unipi.it.DatabaseAccess;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;


public class Mqtt_collector {
       public static class MyClient implements MqttCallback {
        public void add_client() throws MqttException {
            String broker = "tcp://127.0.0.1:1883";
            String clientId = "Mqtt_Collector";
            ArrayList <String> topics = new ArrayList<>();
            topics.add("temperature");
            topics.add("humidity");
            topics.add("co2");
            MqttClient mqttClient = new MqttClient(broker, clientId);
            mqttClient.setCallback(this);
            mqttClient.connect();
            for (String topic : topics){
                mqttClient.subscribe(topic);
            }
            System.out.println("Collector started and subscribed to all the topics\n");
        }

        @Override
        public void connectionLost(Throwable throwable) {
            System.out.println("Connection with broker lost!\n");
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            JSONParser parser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject) parser.parse(new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
                Long value_long = (Long) jsonObject.get("value");
                Integer value = value_long.intValue();

                if(Objects.equals(topic, "co2")) {
                    value *= 10;
                }

                System.out.println("nuovo valore registrato sul topic:"+topic+", value:"+value+"\n");

            }catch(ParseException e) {
                throw new RuntimeException(e);
            }

            //System.out.println(String.format("[%s] %s" , topic, new String(mqttMessage.getPayload())));


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            System.out.println("Completed delivery\n");
        }
    }


}
