package it.unipi;

import it.unipi.COAP_Resources.Measurement_Resource;
import it.unipi.frontend.Frontend;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.californium.core.CoapServer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyServer extends CoapServer {

    public static void setExit(boolean exit) {
        MyServer.exit = exit;
    }

    static volatile boolean exit = false;
    public static void main(String[] args) throws MqttException {
        // Start the MQTT collector
        Thread mqttThread = new Thread(() -> {
            try {
                Mqtt_collector.MyClient mc = new Mqtt_collector.MyClient();
                mc.add_client();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        mqttThread.start();

        // Start the COAP server
        MyServer server = new MyServer();
        server.add(new Measurement_Resource("temperature"));
        server.add(new Measurement_Resource("humidity"));
        server.add(new Measurement_Resource("co2"));
        System.out.println("COAP server started\n");
        Logger californiumLogger = Logger.getLogger("org.eclipse.californium");
        californiumLogger.setLevel(Level.SEVERE);
        server.start();

        // Start Loop for Actuators management
        Thread actuatorThread = new Thread(new Actuators_controller.Control_Loop());
        actuatorThread.start();

        // Start Command Line Interface (to be implemented)
        Thread cliThread = new Thread(() -> {
            // Implementazione della Command Line Interface
            Frontend.start();
        });
        cliThread.start();

        // Wait for completion of threads
        try {
            cliThread.join();
            //mqttThread.join();
            //actuatorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(exit){
            System.exit(0);
        }

    }
}