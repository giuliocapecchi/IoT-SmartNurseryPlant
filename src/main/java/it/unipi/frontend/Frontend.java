package it.unipi.frontend;

import java.util.Scanner;
import java.io.IOException;

public class Frontend {
    static int i=0;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            // Stampa il menu
            System.out.println("Welcome into Smart Plant Nursery application. Choose an option:");
            System.out.println("1. Help");
            System.out.println("2. Status of your system");
            System.out.println("3. Measurements of sampled values");
            System.out.println("4. Send commands to actuators");


            // Leggi l'input dell'utente
            command = scanner.nextLine();

            // Esegui l'azione corrispondente al comando inserito
            switch (command) {
                case "1":
                    System.out.println("**Help menu**\n1. Help: Show this menu\n2. Show which sensors and actuators are " +
                            "connected to the system and their status.\n3.Shows a list of latest measurement values for " +
                            "each sensor\n4.Show a submenu with the state of the actuator and handle their behaviour");
                    break;
                case "2":
                    clearConsole();
                    System.out.println("**Sensors and actuators status panel**");
                    while (true) { 
                        // Aggiorna e mostra lo stato del sistema dal database 
                        refreshSystemStatus(); 
                        System.out.println("Type 'exit' to quit.");
                        String input = scanner.nextLine(); 
                        if (input.equalsIgnoreCase("exit")){ 
                                break; 
                            }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            }
                        }
                    break;
                case "3":
                    System.out.println("Uscita...");
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private static void refreshSystemStatus() {
        // Aggiorna lo stato del sistema dal database e mostra i risultati
        System.out.println("Refreshing system status "+i);
        i++;
        // Esegui le operazioni per mostrare lo stato del database

        
    }


    public static void clearConsole() {
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during clear console function");
        }
    }
}
