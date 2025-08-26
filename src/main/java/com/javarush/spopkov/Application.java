package com.javarush.spopkov;

import com.javarush.spopkov.commands.Analyze;
import com.javarush.spopkov.commands.BruteForce;
import com.javarush.spopkov.commands.Decoder;
import com.javarush.spopkov.commands.Encoder;

import java.util.Scanner;

public class Application {
    private final Encoder encoder = new Encoder();
    private final Decoder decoder = new Decoder();
    private final BruteForce bruteForce = new BruteForce();
    private final Analyze analyze = new Analyze();

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nChoose an operation mode:");
        System.out.println("1 - Encrypt text");
        System.out.println("2 - Decrypt with a key");
        System.out.println("3 - Brute force");
        System.out.println("4 - Statistical analysis");
        System.out.println("0 - Exit");
        System.out.print("Your choice: ");

        int choice = scanner.nextInt();
        try {
            switch (choice) {
                case 1 -> encoder.encrypt("text/text.txt", "text/encrypt.txt", getKey(scanner));
                case 2 -> decoder.decrypt("text/encrypt.txt", "text/decrypt.txt", getKey(scanner));
                case 3 -> bruteForce.decrypt("text/encrypt.txt", "text/bruteforce.txt");
                case 4 -> analyze.decrypt("text/encrypt.txt", "text/analyze.txt");
                case 0 -> System.out.println("Exit the program.");
                default -> System.out.println("Invalid choice.");
            }
        } catch (CaesarCipherException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("The program has terminated.");
    }

    private int getKey(Scanner scanner) {
        System.out.print("Enter the key (shift): ");
        return scanner.nextInt();
    }
}