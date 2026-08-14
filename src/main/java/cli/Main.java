package cli;

import caesar.CaesarCipher;
import caesar.CaesarCryptanalyzer;
import hill.HillCipher;
import hill.HillKnownPlaintextAttack;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import substitution.FrequencyAnalysisAttack;
import substitution.HillClimbingAttack;
import substitution.NGramModel;
import substitution.SubstitutionCipher;
import util.Alphabet;
import util.FrequencyTable;
import util.KeySpaceCalculator;
import util.ShannonEntropy;
import vigenere.IndexOfCoincidence;
import vigenere.KasiskiExamination;
import vigenere.VigenereCipher;
import vigenere.VigenereCryptanalyzer;


public final class Main {

    private static final String DEFAULT_CORPUS_PATH = "data/corpus_english_sample.txt";

    private static final String FALLBACK_CORPUS_ENGLISH =
            "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG WHILE THE FIVE BOXING WIZARDS "
            + "JUMP QUICKLY PACK MY BOX WITH FIVE DOZEN LIQUOR JUGS THIS SENTENCE IS USED "
            + "ONLY AS A FALLBACK LANGUAGE MODEL CORPUS WHEN THE DATA FILE IS NOT FOUND";

    
    private static final String FALLBACK_CORPUS_PERSIAN =
            "رمزنگاری کلاسیک یکی از قدیمی ترین شاخه های علم رمزنگاری است که پیش از ظهور "
            + "رایانه های امروزی توسعه یافت در این روش ها معمولا از جایگشت یا جابجایی حروف "
            + "یک الفبای مشخص برای پنهان کردن معنای پیام استفاده می شود رمز سزار یکی از "
            + "ساده ترین نمونه های این نوع رمزنگاری است که در آن هر حرف متن اصلی با انتقال "
            + "ثابتی در الفبا جایگزین می شود رمز جانشینی تک الفبایی نیز با استفاده از یک "
            + "جایگشت دلخواه از حروف الفبا امنیت بیشتری نسبت به رمز سزار فراهم می کند اما "
            + "همچنان در برابر تحلیل فراوانی حروف آسیب پذیر است رمز ویژنر با استفاده از یک "
            + "کلید چند حرفی و رمزگذاری چند الفبایی تحلیل فراوانی ساده را دشوارتر می کند "
            + "رمز هیل نیز با استفاده از جبر خطی و ماتریس های کلید بلوک هایی از حروف را "
            + "به طور همزمان رمزگذاری می کند مطالعه این روش های کلاسیک پایه ای مناسب برای "
            + "درک اصول رمزنگاری نوین و امنیت اطلاعات فراهم می آورد";

    private final Scanner in;
    private Alphabet alphabet = Alphabet.english();
    private boolean persianMode = false;

    private Main(Scanner in) {
        this.in = in;
    }

    public static void main(String[] args) {
        
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new Main(scanner).run();
        }
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Your choice: ");
            try {
                switch (choice) {
                    case 1 -> caesarMenu();
                    case 2 -> substitutionMenu();
                    case 3 -> vigenereMenu();
                    case 4 -> hillMenu();
                    case 5 -> utilityMenu();
                    case 6 -> selectAlphabetMenu();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option, please try again.\n");
                }
            } catch (RuntimeException e) {
                
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMainMenu() {
        System.out.println("========================================");
        System.out.println(" Classical Cryptography & Cryptanalysis - Main Menu");
        System.out.println(" Current alphabet: " + alphabetLabel() + " (" + alphabet.size() + " letters)");
        System.out.println("========================================");
        System.out.println("1) Caesar Cipher");
        System.out.println("2) Substitution Cipher");
        System.out.println("3) Vigenere Cipher");
        System.out.println("4) Hill Cipher");
        System.out.println("5) Utilities (entropy / frequency table)");
        System.out.println("6) Change alphabet / language (English <-> Persian)");
        System.out.println("0) Exit");
    }

    private String alphabetLabel() {
        return persianMode ? "Persian" : "English";
    }


    private void selectAlphabetMenu() {
        System.out.println("\n--- Select alphabet / language ---");
        System.out.println("1) English (26 letters)");
        System.out.println("2) Persian (32 letters)");
        System.out.println("0) Back (keep current: " + alphabetLabel() + ")");
        int choice = readInt("Your choice: ");
        switch (choice) {
            case 1 -> {
                alphabet = Alphabet.english();
                persianMode = false;
                System.out.println("Alphabet set to English.");
            }
            case 2 -> {
                alphabet = Alphabet.persian();
                persianMode = true;
                System.out.println("Alphabet set to Persian.");
                System.out.println("Note: automatic cryptanalysis (frequency analysis / hill climbing) "
                        + "for Persian relies on a small built-in reference corpus, so its accuracy is "
                        + "lower than for English. Plain encryption and decryption work at full accuracy.");
            }
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private FrequencyTable referenceFrequencyTable() {
        return persianMode ? FrequencyTable.persianApproximate() : FrequencyTable.englishReference();
    }


    private void caesarMenu() {
        CaesarCipher cipher = new CaesarCipher(alphabet);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Caesar Cipher (" + alphabetLabel() + ") ---");
            System.out.println("1) Encrypt");
            System.out.println("2) Decrypt");
            System.out.println("3) Automatic break (Brute-force + Chi-squared)");
            System.out.println("4) Show key space");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readAlphabetText("Enter the plaintext: ");
                    int key = readInt("Key (integer): ");
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readAlphabetText("Enter the ciphertext: ");
                    int key = readInt("Key (integer): ");
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 3 -> {
                    String text = readAlphabetText("Enter the ciphertext: ");
                    CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(cipher, referenceFrequencyTable());
                    CaesarCryptanalyzer.Result best = analyzer.breakCipher(text);
                    System.out.println("Best guess: " + best);
                }
                case 4 -> {
                    BigInteger keySpace = KeySpaceCalculator.caesarKeySpace(alphabet.size());
                    System.out.println("|K| = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }


    private void substitutionMenu() {
        SubstitutionCipher cipher = new SubstitutionCipher(alphabet);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Substitution Cipher (" + alphabetLabel() + ") ---");
            System.out.println("1) Encrypt with a random key");
            System.out.println("2) Encrypt with a custom key");
            System.out.println("3) Decrypt with a custom key");
            System.out.println("4) Automatic break (frequency analysis + hill climbing)");
            System.out.println("5) Show key space");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readAlphabetText("Enter the plaintext: ");
                    int[] key = SubstitutionCipher.randomKey(alphabet.size(), new Random());
                    System.out.println("Generated key: " + keyToLetters(key));
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readAlphabetText("Enter the plaintext: ");
                    int[] key = readSubstitutionKey();
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 3 -> {
                    String text = readAlphabetText("Enter the ciphertext: ");
                    int[] key = readSubstitutionKey();
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 4 -> breakSubstitution();
                case 5 -> {
                    BigInteger keySpace = KeySpaceCalculator.substitutionKeySpace(alphabet.size());
                    System.out.println("|K| = n! = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void breakSubstitution() {
        String text = readAlphabetText("Enter the ciphertext: ");
        int iterations = readInt("Hill-climbing iterations per restart (suggested: 5000): ");
        int restarts = readInt("Number of independent restarts (suggested: 10): ");

        NGramModel model = new NGramModel(alphabet, 2);
        model.train(loadCorpus());

        FrequencyAnalysisAttack freqAttack = new FrequencyAnalysisAttack(alphabet, referenceFrequencyTable());
        int[] initialGuess = freqAttack.initialGuess(text);

        HillClimbingAttack attack = new HillClimbingAttack(alphabet, model, System.nanoTime());
        HillClimbingAttack.Result result = attack.attack(text, initialGuess, iterations, restarts);

        System.out.println("Recovered key: " + keyToLetters(result.key));
        System.out.println("Log-likelihood score: " + result.score);
        System.out.println("Decrypted text: " + result.plaintext);
    }

    private int[] readSubstitutionKey() {
        int n = alphabet.size();
        String prompt = "Enter the key as a " + n + "-character permutation of the current alphabet's letters "
                + "(the i-th character is what the i-th letter of the alphabet, in the order shown below, maps to).\n"
                + "Current alphabet letters, in order: " + alphabet.letters() + "\n"
                + "Key: ";
        String keyText = readLine(prompt).trim().toLowerCase(java.util.Locale.ROOT);
        if (keyText.length() != n) {
            throw new IllegalArgumentException("The key must be exactly " + n + " characters long (length entered: "
                    + keyText.length() + ").");
        }
        int[] key = new int[n];
        for (int i = 0; i < n; i++) {
            int idx = alphabet.indexOf(keyText.charAt(i));
            if (idx < 0) {
                throw new IllegalArgumentException("Character '" + keyText.charAt(i)
                        + "' is not part of the current (" + alphabetLabel() + ") alphabet.");
            }
            key[i] = idx;
        }
        return key;
    }

    private String keyToLetters(int[] key) {
        StringBuilder sb = new StringBuilder(key.length);
        for (int idx : key) {
            sb.append(alphabet.charAt(idx, false));
        }
        return sb.toString();
    }

    private String loadCorpus() {
        if (persianMode) {
            System.out.println("Note: no Persian corpus file is bundled with the project; "
                    + "using the small built-in Persian text as the language model source.");
            return FALLBACK_CORPUS_PERSIAN;
        }
        Path path = Path.of(DEFAULT_CORPUS_PATH);
        try {
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (java.io.IOException e) {
            System.out.println("Warning: failed to read the corpus file (" + e.getMessage()
                    + "); falling back to the built-in corpus.");
        }
        System.out.println("Warning: corpus file '" + DEFAULT_CORPUS_PATH
                + "' was not found; falling back to a small built-in corpus "
                + "(automatic-break accuracy may be lower).");
        return FALLBACK_CORPUS_ENGLISH;
    }


    private void vigenereMenu() {
        VigenereCipher cipher = new VigenereCipher(alphabet);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Vigenere Cipher (" + alphabetLabel() + ") ---");
            System.out.println("1) Encrypt");
            System.out.println("2) Decrypt");
            System.out.println("3) Automatic break (Kasiski + IC + Chi-squared)");
            System.out.println("4) Show key space for a given length");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readAlphabetText("Enter the plaintext: ");
                    String key = readLine("Key (a string of letters): ");
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readAlphabetText("Enter the ciphertext: ");
                    String key = readLine("Key (a string of letters): ");
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 3 -> breakVigenere();
                case 4 -> {
                    int len = readInt("Key length: ");
                    BigInteger keySpace = KeySpaceCalculator.vigenereKeySpace(alphabet.size(), len);
                    System.out.println("|K| = n^L = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void breakVigenere() {
        String text = readAlphabetText("Enter the ciphertext: ");
        int maxKeyLength = readInt("Maximum key length to search (suggested: 20): ");

        VigenereCryptanalyzer analyzer = new VigenereCryptanalyzer(alphabet, referenceFrequencyTable());
        VigenereCryptanalyzer.Result result = analyzer.breakCipher(text, maxKeyLength);

        System.out.println("Estimated key length: " + result.estimatedKeyLength);
        System.out.println("Recovered key: " + result.recoveredKey);
        System.out.println("Decrypted text: " + result.plaintext);

        System.out.print("Show IC/Kasiski test details? (y/n): ");
        String answer = in.hasNextLine() ? in.nextLine().trim().toLowerCase(java.util.Locale.ROOT) : "n";
        if (answer.startsWith("y")) {
            IndexOfCoincidence ic = new IndexOfCoincidence(alphabet);
            double[] avgIc = ic.averageICForKeyLengths(text, maxKeyLength);
            for (int len = 1; len <= maxKeyLength; len++) {
                System.out.printf(java.util.Locale.ROOT, "L=%2d  average IC=%.4f%n", len, avgIc[len]);
            }
            KasiskiExamination kasiski = new KasiskiExamination(alphabet, 3);
            KasiskiExamination.Distances distances = kasiski.findRepeatedSequenceDistances(text);
            System.out.println("Number of repeated sequences found: " + distances.repeatedSequencePositions.size());
        }
    }


    private void hillMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Hill Cipher (" + alphabetLabel() + ") ---");
            System.out.println("1) Encrypt");
            System.out.println("2) Decrypt");
            System.out.println("3) Known-plaintext attack");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> hillEncryptOrDecrypt(true);
                case 2 -> hillEncryptOrDecrypt(false);
                case 3 -> hillKnownPlaintextAttack();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void hillEncryptOrDecrypt(boolean encrypt) {
        int m = readInt("Key matrix block size (e.g. 2 or 3): ");
        int[][] key = readMatrix(m);
        String text = readAlphabetText(encrypt ? "Enter the plaintext: " : "Enter the ciphertext: ");

        HillCipher cipher = new HillCipher(alphabet, m);
        if (encrypt) {
            System.out.println("Ciphertext: " + cipher.encrypt(text, key));
        } else {
            System.out.println("Decrypted text: " + cipher.decrypt(text, key));
        }
    }

    private void hillKnownPlaintextAttack() {
        int m = readInt("Block size (m): ");
        String plaintext = readAlphabetText("Enter the known plaintext (at least " + (m * m) + " letters): ");
        String ciphertext = readAlphabetText("Enter the corresponding ciphertext (same length): ");

        HillKnownPlaintextAttack attack = new HillKnownPlaintextAttack(alphabet, m);
        int[][][] blocks = attack.extractBlockMatrices(plaintext, ciphertext);
        int[][] recoveredKey = attack.recoverKey(blocks[0], blocks[1]);

        System.out.println("Recovered key matrix:");
        printMatrix(recoveredKey);
    }

    private int[][] readMatrix(int m) {
        System.out.println("Enter the entries of the " + m + "x" + m + " key matrix row by row"
                + " (numbers within a row separated by spaces):");
        int[][] matrix = new int[m][m];
        for (int row = 0; row < m; row++) {
            String[] parts = readLine("Row " + (row + 1) + ": ").trim().split("\\s+");
            if (parts.length != m) {
                throw new IllegalArgumentException("Each row must have exactly " + m + " numbers.");
            }
            for (int col = 0; col < m; col++) {
                matrix[row][col] = Integer.parseInt(normalizeDigits(parts[col]));
            }
        }
        return matrix;
    }

    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            StringBuilder sb = new StringBuilder();
            for (int v : row) {
                sb.append(String.format(java.util.Locale.ROOT, "%4d", v));
            }
            System.out.println(sb);
        }
    }


    private void utilityMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Utilities (" + alphabetLabel() + ") ---");
            System.out.println("1) Shannon entropy of a text");
            System.out.println("2) Letter frequency table of a text");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readAlphabetText("Enter the text: ");
                    double entropy = ShannonEntropy.compute(text, alphabet);
                    double maxEntropy = ShannonEntropy.maxEntropy(alphabet.size());
                    double efficiency = ShannonEntropy.entropyEfficiencyPercent(text, alphabet);
                    System.out.printf(java.util.Locale.ROOT, "H(X) = %.4f bits/char%n", entropy);
                    System.out.printf(java.util.Locale.ROOT, "Theoretical max = %.4f bits/char%n", maxEntropy);
                    System.out.printf(java.util.Locale.ROOT, "Entropy efficiency = %.2f%%%n", efficiency);
                }
                case 2 -> {
                    String text = readAlphabetText("Enter the text: ");
                    FrequencyTable table = FrequencyTable.fromCorpus(text, alphabet);
                    Map<Character, Double> sorted = table.sortedByFrequencyDescending();
                    for (Map.Entry<Character, Double> entry : sorted.entrySet()) {
                        System.out.printf(java.util.Locale.ROOT, "%c : %5.2f%%%n", entry.getKey(), entry.getValue());
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void printBruteForceEstimate(BigInteger keySpace) {
        double seconds = KeySpaceCalculator.estimatedBruteForceSeconds(keySpace, 1_000_000_000.0);
        System.out.println("Estimated brute-force search time at 10^9 keys/sec: "
                + KeySpaceCalculator.humanReadableDuration(seconds));
    }

    private static String normalizeDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u06F0' && c <= '\u06F9') {
                sb.append((char) ('0' + (c - '\u06F0')));
            } else if (c >= '\u0660' && c <= '\u0669') {
                sb.append((char) ('0' + (c - '\u0660')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                throw new IllegalStateException("End of input reached.");
            }
            String line = normalizeDigits(in.nextLine().trim());
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            throw new IllegalStateException("End of input reached.");
        }
        return in.nextLine();
    }

    
    private String readAlphabetText(String prompt) {
        String text = readLine(prompt);
        boolean anyMatch = false;
        for (int i = 0; i < text.length(); i++) {
            if (alphabet.indexOf(text.charAt(i)) >= 0) {
                anyMatch = true;
                break;
            }
        }
        if (!text.isEmpty() && !anyMatch) {
            throw new IllegalArgumentException("None of the characters you entered belong to the current ("
                    + alphabetLabel() + ") alphabet. If your text is in the other language, switch the "
                    + "alphabet first from the main menu (option 6).");
        }
        return text;
    }
}