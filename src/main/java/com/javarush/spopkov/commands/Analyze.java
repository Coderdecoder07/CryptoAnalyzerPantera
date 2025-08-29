package com.javarush.spopkov.commands;

import com.javarush.spopkov.constants.Constants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Analyze {

    // Decrypt text using frequency analysis (without external reference)
    public void decrypt(String inputFile, String outputFile) {
        try {
            Path inputPath = Path.of(System.getProperty("user.dir")).resolve(inputFile);
            Path outputPath = Path.of(System.getProperty("user.dir")).resolve(outputFile);

            if (!Files.exists(inputPath)) {
                System.out.println("Encrypted file not found: " + inputPath.toAbsolutePath());
                return;
            }

            String encryptedText = Files.readString(inputPath);

            // Count frequencies of Russian letters and symbols
            Map<Character, Integer> freqMap = countFrequencies(encryptedText);

            // Guess the most frequent character (likely space or 'О')
            char mostFrequent = getMostFrequent(freqMap);

            // Assume the most frequent letter corresponds to Russian 'О' (most common letter)
            int guessedKey = (Constants.RUS_ALPHABET.indexOf(mostFrequent) - Constants.RUS_ALPHABET.indexOf('О') + Constants.RUS_ALPHABET.length()) % Constants.RUS_ALPHABET.length();

            // Decrypt with the guessed key
            String decryptedText = decryptWithKey(encryptedText, guessedKey);

            // Save to file
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write(decryptedText);
            }

            System.out.println("Statistical analysis finished. Guessed key: " + guessedKey);
            System.out.println("Decrypted text saved -> " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    // Count frequency of Russian letters and symbols in text
    private Map<Character, Integer> countFrequencies(String text) {
        Map<Character, Integer> map = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (Constants.RUS_ALPHABET.indexOf(Character.toUpperCase(c)) != -1 ||
                    Constants.NUMBERS_SYMBOLS.indexOf(c) != -1) {
                char key = Character.toUpperCase(c);
                map.put(key, map.getOrDefault(key, 0) + 1);
            }
        }
        return map;
    }

    // Return the most frequent character
    private char getMostFrequent(Map<Character, Integer> freqMap) {
        int max = -1;
        char mostFreq = 'О'; // default fallback
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                mostFreq = entry.getKey();
            }
        }
        return mostFreq;
    }

    // Decrypt text with a given key
    private String decryptWithKey(String text, int key) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            // Keep Latin letters unchanged (including Roman numerals)
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                result.append(c);
                continue;
            }

            char upperC = Character.toUpperCase(c);

            // Russian letters
            int indexRus = Constants.RUS_ALPHABET.indexOf(upperC);
            if (indexRus != -1) {
                int newIndex = (indexRus - key + Constants.RUS_ALPHABET.length()) % Constants.RUS_ALPHABET.length();
                char newChar = Constants.RUS_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Numbers & symbols
            int indexNumSymb = Constants.NUMBERS_SYMBOLS.indexOf(c);
            if (indexNumSymb != -1) {
                int newIndex = (indexNumSymb - key + Constants.NUMBERS_SYMBOLS.length()) % Constants.NUMBERS_SYMBOLS.length();
                result.append(Constants.NUMBERS_SYMBOLS.charAt(newIndex));
                continue;
            }

            // Other characters (punctuation etc.)
            result.append(c);
        }

        return result.toString();
    }
}
