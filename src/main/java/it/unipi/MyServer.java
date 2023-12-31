package it.unipi;

import it.unipi.COAP_Resources.Registration_Resource;
import it.unipi.frontend.Frontend;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.californium.core.CoapServer;

import java.io.IOException;
import java.sql.SQLException;
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
        server.add(new Registration_Resource("registration"));
        System.out.println("COAP server started\n");
        //The following lines are only there to not mess up the command line interface
        Logger californiumLogger = Logger.getLogger("org.eclipse.californium");
        californiumLogger.setLevel(Level.SEVERE);
        server.start();

        // Start Loop for Actuators management
        Thread actuatorThread = new Thread(new Actuators_controller.Control_Loop());
        actuatorThread.start();

        // Start Command Line Interface
        Thread cliThread = new Thread(() -> {
            try {
                Frontend.start();
            } catch (SQLException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        cliThread.start();

        // Wait for completion of the cli thread
        try {
            cliThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(exit){
            System.exit(0);
        }
    }
}