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

            // Loading dictionary, if available
            Set<String> dictionary = getStrings(dictPath);

            // Selecting the best shift
            int bestShiftRus = findBestShift(encryptedText, Constants.RUS_ALPHABET, dictionary);
            int bestShiftEng = findBestShift(encryptedText, Constants.ENG_ALPHABET, dictionary);

            // Decrypting with the found keys
            String decryptedText = decryptText(encryptedText, bestShiftRus, bestShiftEng);

            Files.writeString(outputPath, decryptedText);
            System.out.println("Brute force completed, best result -> " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            throw new CaesarCipherException("Error while working with files: " + e.getMessage());
        }
    }

    private static Set<String> getStrings(Path dictPath) throws IOException {
        Set<String> dictionary = new HashSet<>();
        if (Files.exists(dictPath)) {
            for (String word : Files.readString(dictPath).split("\\W+")) {
                dictionary.add(word.toLowerCase());
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

            result.append(c);
        }

        return result.toString();
    }

    private int findBestShift(String text, String alphabet, Set<String> dictionary) {
        int bestShift = 0;
        double bestScore = -1;

        for (int shift = 0; shift < alphabet.length(); shift++) {
            String shifted = shiftText(text, shift, alphabet);
            double score = evaluateText(shifted, dictionary, alphabet);

            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }

        return bestShift;
    }

    private String shiftText(String text, int key, String alphabet) {
        StringBuilder result = new StringBuilder();
        int size = alphabet.length();

        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);
            int index = alphabet.indexOf(upperC);

            if (index != -1) {
                int newIndex = (index + key) % size;
                char newChar = alphabet.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    // Evaluating the decrypted text
    private double evaluateText(String text, Set<String> dictionary, String alphabet) {
        String[] words = text.split("\\W+");
        double score = 0;

        for (String word : words) {
            if (word.isEmpty()) continue;
            String lower = word.toLowerCase();

            // Dictionary found — checking for matches
            if (!dictionary.isEmpty() && dictionary.contains(lower)) {
                score += 1.0;
            }

            // For an unknown dictionary, using word length
            score += Math.min(word.length(), 5);
        }

        return score;
    }
}