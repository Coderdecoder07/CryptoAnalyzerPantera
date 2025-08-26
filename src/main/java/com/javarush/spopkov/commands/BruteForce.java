package com.javarush.spopkov.commands;

import com.javarush.spopkov.CaesarCipherException;
import com.javarush.spopkov.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class BruteForce {

    public void decrypt(String encryptedPathStr, String outputPathStr) throws CaesarCipherException {
        try {
            Path projectDir = Path.of(System.getProperty("user.dir"));
            Path encryptedPath = projectDir.resolve(encryptedPathStr);
            Path dictPath = projectDir.resolve("text/dict.txt");
            Path outputPath = projectDir.resolve(outputPathStr);

            if (!Files.exists(encryptedPath)) {
                throw new CaesarCipherException("File for decryption not found: " + encryptedPath.toAbsolutePath());
            }

            String encryptedText = Files.readString(encryptedPath);
            Set<String> dictionary = loadDictionary(dictPath);

            // Try all shifts for Russian letters
            int bestShiftRus = findBestShift(encryptedText, Constants.RUS_ALPHABET, dictionary);
            // Try all shifts for English letters
            int bestShiftEng = findBestShift(encryptedText, Constants.ENG_ALPHABET, dictionary);

            // Decrypt text using the best shifts
            String decryptedText = decryptText(encryptedText, bestShiftRus, bestShiftEng);

            Files.writeString(outputPath, decryptedText);
            System.out.println("Brute force completed -> " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            throw new CaesarCipherException("Error while working with files: " + e.getMessage());
        }
    }

    private Set<String> loadDictionary(Path dictPath) throws IOException {
        Set<String> dictionary = new HashSet<>();
        if (Files.exists(dictPath)) {
            for (String word : Files.readString(dictPath).split("\\W+")) {
                if (!word.isEmpty()) dictionary.add(word.toLowerCase());
            }
        }
        return dictionary;
    }

    private String decryptText(String text, int keyRus, int keyEng) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);

            int indexRus = Constants.RUS_ALPHABET.indexOf(upperC);
            if (indexRus != -1) {
                int newIndex = (indexRus - keyRus + Constants.RUS_ALPHABET.length()) % Constants.RUS_ALPHABET.length();
                char newChar = Constants.RUS_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            int indexEng = Constants.ENG_ALPHABET.indexOf(upperC);
            if (indexEng != -1) {
                int newIndex = (indexEng - keyEng + Constants.ENG_ALPHABET.length()) % Constants.ENG_ALPHABET.length();
                char newChar = Constants.ENG_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Numbers and symbols remain unchanged
            result.append(c);
        }
        return result.toString();
    }

    private int findBestShift(String text, String alphabet, Set<String> dictionary) {
        int bestShift = 0;
        double bestScore = -1;

        for (int shift = 0; shift < alphabet.length(); shift++) {
            String shifted = shiftText(text, shift, alphabet);
            double score = evaluateText(shifted, dictionary);

            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }

        return bestShift;
    }

    private String shiftText(String text, int key, String alphabet) {
        StringBuilder result = new StringBuilder();
        int len = alphabet.length();

        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);
            int index = alphabet.indexOf(upperC);

            if (index != -1) {
                int newIndex = (index - key + len) % len; // shift backward
                char newChar = alphabet.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
            } else {
                // Non-letter characters remain unchanged
                result.append(c);
            }
        }

        return result.toString();
    }

    private double evaluateText(String text, Set<String> dictionary) {
        if (dictionary.isEmpty()) return 0;

        double score = 0;
        for (String word : text.split("\\W+")) {
            if (!word.isEmpty() && dictionary.contains(word.toLowerCase())) score += 1;
        }
        return score;
    }
}