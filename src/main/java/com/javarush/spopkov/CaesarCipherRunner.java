package com.javarush.spopkov;

public class CaesarCipherRunner {
    public static void main(String[] args) {
        String RusAlphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        String EngAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String symbols = "~!@#$%^&*()_+{}|:<>?-=[];',./`";
        String alphabet = RusAlphabet + EngAlphabet + RusAlphabet.toLowerCase() + EngAlphabet.toLowerCase() + numbers + symbols;
    }
}
