package com.javarush.spopkov.сommands;

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

    private String shift(String text, int key) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            char upperC = Character.toUpperCase(c);

            // Check: Russian alphabet
            int indexRus = Constants.RusAlphabet.indexOf(upperC);
            if (indexRus != -1) {
                int newIndex = (indexRus - key + Constants.RusAlphabet.length()) % Constants.RusAlphabet.length();
                char newChar = Constants.RusAlphabet.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // Check: English alphabet
            int indexEng = Constants.EngAlphabet.indexOf(upperC);
            if (indexEng != -1) {
                int newIndex = (indexEng - key + Constants.EngAlphabet.length()) % Constants.EngAlphabet.length();
                char newChar = Constants.EngAlphabet.charAt(newIndex);
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(newChar) : newChar);
                continue;
            }

            // If not a letter
            result.append(c);
        }
        return result.toString();
    }
}