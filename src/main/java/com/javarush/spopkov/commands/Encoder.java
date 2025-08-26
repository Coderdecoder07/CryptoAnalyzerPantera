package com.javarush.spopkov.commands;

import com.javarush.spopkov.CaesarCipherException;
import com.javarush.spopkov.constants.Actions;
import com.javarush.spopkov.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Encoder implements Actions {

    @Override
    public void encrypt(String inputPathStr, String outputPathStr, int key) throws CaesarCipherException {
        try {
            Path projectDir = Path.of(System.getProperty("user.dir"));
            Path inputPath = projectDir.resolve(inputPathStr);
            Path outputPath = projectDir.resolve(outputPathStr);

            if (!Files.exists(inputPath)) {
                throw new CaesarCipherException("File for encryption not found: " + inputPath.toAbsolutePath());
            }

            String text = Files.readString(inputPath);
            String encrypted = shift(text, key);

            Files.writeString(outputPath, encrypted);

            System.out.println("File encrypted -> " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            throw new CaesarCipherException("Encryption error: " + e.getMessage());
        }
    }

    @Override
    public void decrypt(String inputPath, String outputPath, int key) throws CaesarCipherException {
        throw new CaesarCipherException("Use Decoder for decryption.");
    }

    private String shift(String text, int key) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);

            // Check: Russian alphabet
            int indexRus = Constants.RUS_ALPHABET.indexOf(upperC);
            if (indexRus != -1) {
                int newIndex = (indexRus + key) % Constants.RUS_ALPHABET.length();
                char newChar = Constants.RUS_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Check: English alphabet
            int indexEng = Constants.ENG_ALPHABET.indexOf(upperC);
            if (indexEng != -1) {
                int newIndex = (indexEng + key) % Constants.ENG_ALPHABET.length();
                char newChar = Constants.ENG_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Check: Numbers and Symbols
            int indexNumSymb = Constants.NUMBERS_SYMBOLS.indexOf(upperC);
            if (indexNumSymb != -1) {
                int newIndex = (indexNumSymb + key) % Constants.NUMBERS_SYMBOLS.length();
                char newChar = Constants.NUMBERS_SYMBOLS.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // If not a letter
            result.append(c);
        }
        return result.toString();
    }
}