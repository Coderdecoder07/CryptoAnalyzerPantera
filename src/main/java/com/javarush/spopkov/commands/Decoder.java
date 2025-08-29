package com.javarush.spopkov.commands;

import com.javarush.spopkov.CaesarCipherException;
import com.javarush.spopkov.constants.Actions;
import com.javarush.spopkov.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Decoder implements Actions {

    @Override
    public void encrypt(String inputPath, String outputPath, int key) throws CaesarCipherException {
        throw new CaesarCipherException("Use Encoder for encryption.");
    }

    @Override
    public void decrypt(String inputPathStr, String outputPathStr, int key) throws CaesarCipherException {
        try {
            Path projectDir = Path.of(System.getProperty("user.dir"));
            Path inputPath = projectDir.resolve(inputPathStr);
            Path outputPath = projectDir.resolve(outputPathStr);

            if (!Files.exists(inputPath)) {
                throw new CaesarCipherException("File for encryption not found: " + inputPath.toAbsolutePath());
            }

            String encryptedText = Files.readString(inputPath);
            String decrypted = shift(encryptedText, key);

            Files.writeString(outputPath, decrypted);

            System.out.println("File decrypted -> " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            throw new CaesarCipherException("Error while working with files: " + e.getMessage());
        }
    }

    String shift(String text, int key) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);

            // Check: Russian alphabet
            int indexRus = Constants.RUS_ALPHABET.indexOf(upperC);
            if (indexRus != -1) {
                int newIndex = (indexRus - key + Constants.RUS_ALPHABET.length()) % Constants.RUS_ALPHABET.length();
                char newChar = Constants.RUS_ALPHABET.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Check: Numbers and Symbols
            int indexNumSymb = Constants.NUMBERS_SYMBOLS.indexOf(upperC);
            if (indexNumSymb != -1) {
                int newIndex = (indexNumSymb - key + Constants.NUMBERS_SYMBOLS.length()) % Constants.NUMBERS_SYMBOLS.length();
                char newChar = Constants.NUMBERS_SYMBOLS.charAt(newIndex);
                result.append(newChar);
                continue;
            }

            // If not found in alphabets, just append the original character
            result.append(c);
        }
        return result.toString();
    }
}