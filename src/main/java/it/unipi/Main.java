package it.unipi;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) throws MqttException {
        Mqtt_collector.MyClient mc = new Mqtt_collector.MyClient();
        mc.add_client();
    }
}