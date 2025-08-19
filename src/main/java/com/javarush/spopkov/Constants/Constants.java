package com.javarush.spopkov.Constants;

public class Constants {
    private static final String RusAlphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final String EngAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String numbers = "0123456789";
    private static final String symbols = "~!@#$%^&*()_+{}|:<>?-=[];',./`";

    public static final String ALPHABET = RusAlphabet + EngAlphabet + RusAlphabet.toLowerCase() + EngAlphabet.toLowerCase() + numbers + symbols;

}
