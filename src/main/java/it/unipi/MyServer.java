package it.unipi;

import it.unipi.COAP_Resources.Measurement_Resource;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.californium.core.CoapServer;

public class MyServer extends CoapServer{
    public static void main(String[] args) throws MqttException{

        // Start MQTT collector
        Mqtt_collector.MyClient mc = new Mqtt_collector.MyClient();
        mc.add_client();

        // Start COAP server
        MyServer server = new MyServer();
        server.add(new Measurement_Resource("temperature"));
        server.add(new Measurement_Resource("humidity"));
        server.add(new Measurement_Resource("co2"));
        System.out.println("Server started\n");
        server.start();
    }
}