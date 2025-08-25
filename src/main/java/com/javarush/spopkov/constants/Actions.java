package com.javarush.spopkov.constants;


import com.javarush.spopkov.CaesarCipherException;

public interface Actions {
    void encrypt(String inputPath, String outputPath, int key) throws CaesarCipherException;
    void decrypt(String inputPath, String outputPath, int key) throws CaesarCipherException;
}