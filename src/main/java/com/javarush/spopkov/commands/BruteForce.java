package com.javarush.spopkov.commands;

import com.javarush.spopkov.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class BruteForce {

    private static final String DICT_PATH = "text/dict.txt";

    // A small built-in fallback dictionary (common Russian words/particles)
    private static final String[] FALLBACK_WORDS = new String[]{
            "И","В","НЕ","НА","Я","ОН","ОНА","ОНИ","ЭТО","К","С","У","ПО","А","НО","ДА",
            "ЧТО","КАК","ЕСЛИ","ТО","ДЛЯ","БЫЛ","БЫЛА","БЫЛО","БЫТЬ","МОЖЕТ","КОТОРЫЙ",
            "ЖЕ","ЛИ","ЕГО","ЕЕ","ИХ","ТАК","УЖЕ","ТЕ","МЫ","ТЫ","МНЕ","ТЕБЕ","НАС",
            "ОТ","ДО","ИЗ","ЗА","ПРИ","ПОСЛЕ","ПЕРЕД","ТУТ","ТАМ","ТОЛЬКО","ЕЩЕ"
    };

    private static final Set<Character> TOP_RUS_LETTERS = new HashSet<>(
            Arrays.asList('О','Е','А','И','Н','Т','С','Р','В','Л','К','М','Д','П','У')
    );

    private static final String[] BAD_BIGRAMS = new String[]{
            "ЪЪ","ЬЬ","ЙЙ","ЫЫ","ЪЬ","ЬЪ","ЪЫ","ЬЫ","ЙЪ","ЙЬ","ЪЙ","ЬЙ"
    };

    // Roman numeral letters (both cases) — we must NOT touch these
    private static final Set<Character> ROMAN_CHARS = new HashSet<>(
            Arrays.asList('I','V','X','L','C','D','M','i','v','x','l','c','d','m')
    );

    public void decrypt(String inputPathStr, String outputPathStr) {
        try {
            Path projectDir = Path.of(System.getProperty("user.dir"));
            Path inputPath  = projectDir.resolve(inputPathStr);
            Path outputPath = projectDir.resolve(outputPathStr);

            if (!Files.exists(inputPath)) {
                System.out.println("Encrypted file not found: " + inputPath.toAbsolutePath());
                return;
            }

            if (outputPath.getParent() != null) Files.createDirectories(outputPath.getParent());

            String encrypted = Files.readString(inputPath);
            Set<String> dict = loadDictionaryOrFallback(projectDir.resolve(DICT_PATH));

            String bestText = "";
            int bestScore = Integer.MIN_VALUE;
            int bestKey = -1;

            int alphabetSize = Constants.RUS_ALPHABET.length();
            int totalKeys = alphabetSize;

            for (int key = 0; key < alphabetSize; key++) {
                String candidate = shiftLikeDecoder(encrypted, key);
                int score = evaluateCandidate(candidate, dict);

                if (score > bestScore) {
                    bestScore = score;
                    bestText = candidate;
                    bestKey = key;
                }
            }

            Files.writeString(outputPath, bestText);

            // Print summary: only total count + best key (as requested)
            System.out.println("Total keys: " + totalKeys);
            System.out.println("Best key: " + bestKey);
            System.out.println("Decrypted text saved to: " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    private String shiftLikeDecoder(String text, int key) {
        StringBuilder sb = new StringBuilder();
        String RUS = Constants.RUS_ALPHABET;
        String SYM = Constants.NUMBERS_SYMBOLS;

        for (char c : text.toCharArray()) {
            // 1a) Always leave Latin letters untouched (keeps Roman numerals and other Latin text intact)
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                sb.append(c);
                continue;
            }

            // 1b) Additionally, explicitly protect Roman-numeral characters (just in case)
            if (ROMAN_CHARS.contains(c)) {
                sb.append(c);
                continue;
            }

            // 2) Russian letter? (lookup in uppercase alphabet)
            char up = Character.toUpperCase(c);
            int idxRus = RUS.indexOf(up);
            if (idxRus != -1) {
                int newIndex = (idxRus - key + RUS.length()) % RUS.length();
                char mapped = RUS.charAt(newIndex);
                sb.append(Character.isLowerCase(c) ? Character.toLowerCase(mapped) : mapped);
                continue;
            }

            // 3) Number/symbol ring — use the raw char index (Decoder used raw char for symbols)
            int idxSym = SYM.indexOf(c);
            if (idxSym != -1) {
                int newIndex = (idxSym - key + SYM.length()) % SYM.length();
                sb.append(SYM.charAt(newIndex));
                continue;
            }

            // 4) Unknown char — keep as is
            sb.append(c);
        }
        return sb.toString();
    }


    private int evaluateCandidate(String text, Set<String> dict) {
        int score = 0;
        score += scoreDictionary(text, dict);
        score += scoreFrequentLetters(text);
        score += scorePunctuation(text);
        score -= penaltyBadBigrams(text);
        score -= penaltyConsonantRuns(text);
        return score;
    }

    private int scoreDictionary(String text, Set<String> dict) {
        int points = 0;
        String[] words = text.split("[^А-ЯЁа-яё]+");
        for (String w : words) {
            if (w.isEmpty()) continue;
            String up = w.toUpperCase(Locale.ROOT);
            if (dict.contains(up)) {
                if (up.length() == 1) points += 2;
                else if (up.length() <= 3) points += 4;
                else points += 6 + up.length() / 2;
            }
        }
        return points;
    }

    private int scoreFrequentLetters(String text) {
        int rusCount = 0, topCount = 0;
        for (char ch : text.toCharArray()) {
            char up = Character.toUpperCase(ch);
            boolean isRus = (up >= 'А' && up <= 'Я') || up == 'Ё';
            if (isRus) {
                rusCount++;
                if (TOP_RUS_LETTERS.contains(up)) topCount++;
            }
        }
        if (rusCount == 0) return -50;
        int ratio = (int) Math.round(100.0 * topCount / rusCount);
        return ratio;
    }

    private int scorePunctuation(String text) {
        int good = 0, bad = 0;
        good += countOccurrences(text, ". ");
        good += countOccurrences(text, ", ");
        good += countOccurrences(text, "! ");
        good += countOccurrences(text, "? ");
        good += countOccurrences(text, " — ");

        bad += countOccurrences(text, " ,");
        bad += countOccurrences(text, " .");
        bad += countOccurrences(text, " !");
        bad += countOccurrences(text, " ?");
        bad += countOccurrences(text, "  ");

        return good * 2 - bad * 3;
    }

    private int penaltyBadBigrams(String text) {
        String up = text.toUpperCase(Locale.ROOT);
        int penalty = 0;
        for (String bg : BAD_BIGRAMS) {
            penalty += 10 * countOccurrences(up, bg);
        }
        return penalty;
    }

    private int penaltyConsonantRuns(String text) {
        String up = text.toUpperCase(Locale.ROOT);
        Set<Character> vowels = new HashSet<>(Arrays.asList('А','О','У','Э','Ы','Е','Ё','И','Ю','Я'));
        int run = 0, penalty = 0;
        for (int i = 0; i < up.length(); i++) {
            char ch = up.charAt(i);
            boolean isRus = (ch >= 'А' && ch <= 'Я') || ch == 'Ё';
            if (!isRus) { run = 0; continue; }
            if (!vowels.contains(ch)) {
                run++;
                if (run >= 5) penalty += 3;
            } else {
                run = 0;
            }
        }
        return penalty;
    }

    private int countOccurrences(String text, String needle) {
        if (needle.isEmpty()) return 0;
        int idx = 0, cnt = 0;
        while ((idx = text.indexOf(needle, idx)) != -1) {
            cnt++;
            idx += needle.length();
        }
        return cnt;
    }

    private Set<String> loadDictionaryOrFallback(Path dictPath) {
        Set<String> dict = new HashSet<>();
        try {
            if (Files.exists(dictPath)) {
                for (String line : Files.readAllLines(dictPath)) {
                    String w = line.trim();
                    if (!w.isEmpty()) dict.add(w.toUpperCase(Locale.ROOT));
                }
            }
        } catch (IOException ignored) {}
        if (dict.isEmpty()) {
            for (String w : FALLBACK_WORDS) dict.add(w.toUpperCase(Locale.ROOT));
        }
        return dict;
    }
}
