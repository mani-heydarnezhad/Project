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

    private static final Alphabet ALPHABET = Alphabet.english();
    private static final String DEFAULT_CORPUS_PATH = "data/corpus_english_sample.txt";
    private static final String FALLBACK_CORPUS =
            "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG WHILE THE FIVE BOXING WIZARDS "
            + "JUMP QUICKLY PACK MY BOX WITH FIVE DOZEN LIQUOR JUGS THIS SENTENCE IS USED "
            + "ONLY AS A FALLBACK LANGUAGE MODEL CORPUS WHEN THE DATA FILE IS NOT FOUND";

    private final Scanner in;

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
        System.out.println("========================================");
        System.out.println("1) Caesar Cipher");
        System.out.println("2) Substitution Cipher");
        System.out.println("3) Vigenere Cipher");
        System.out.println("4) Hill Cipher");
        System.out.println("5) Utilities (entropy / frequency table)");
        System.out.println("0) Exit");
    }


    private void caesarMenu() {
        CaesarCipher cipher = new CaesarCipher(ALPHABET);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Caesar Cipher ---");
            System.out.println("1) Encrypt");
            System.out.println("2) Decrypt");
            System.out.println("3) Automatic break (Brute-force + Chi-squared)");
            System.out.println("4) Show key space");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readLine("Enter the plaintext: ");
                    int key = readInt("Key (integer): ");
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readLine("Enter the ciphertext: ");
                    int key = readInt("Key (integer): ");
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 3 -> {
                    String text = readLine("Enter the ciphertext: ");
                    FrequencyTable reference = FrequencyTable.englishReference();
                    CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(cipher, reference);
                    CaesarCryptanalyzer.Result best = analyzer.breakCipher(text);
                    System.out.println("Best guess: " + best);
                }
                case 4 -> {
                    BigInteger keySpace = KeySpaceCalculator.caesarKeySpace(ALPHABET.size());
                    System.out.println("|K| = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }


    private void substitutionMenu() {
        SubstitutionCipher cipher = new SubstitutionCipher(ALPHABET);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Substitution Cipher ---");
            System.out.println("1) Encrypt with a random key");
            System.out.println("2) Encrypt with a custom key");
            System.out.println("3) Decrypt with a custom key");
            System.out.println("4) Automatic break (frequency analysis + hill climbing)");
            System.out.println("5) Show key space");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readLine("Enter the plaintext: ");
                    int[] key = SubstitutionCipher.randomKey(ALPHABET.size(), new Random());
                    System.out.println("Generated key: " + keyToLetters(key));
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readLine("Enter the plaintext: ");
                    int[] key = readSubstitutionKey();
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 3 -> {
                    String text = readLine("Enter the ciphertext: ");
                    int[] key = readSubstitutionKey();
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 4 -> breakSubstitution();
                case 5 -> {
                    BigInteger keySpace = KeySpaceCalculator.substitutionKeySpace(ALPHABET.size());
                    System.out.println("|K| = n! = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void breakSubstitution() {
        String text = readLine("Enter the ciphertext: ");
        int iterations = readInt("Hill-climbing iterations per restart (suggested: 5000): ");
        int restarts = readInt("Number of independent restarts (suggested: 10): ");

        NGramModel model = new NGramModel(ALPHABET, 2);
        model.train(loadCorpus());

        FrequencyAnalysisAttack freqAttack = new FrequencyAnalysisAttack(ALPHABET, FrequencyTable.englishReference());
        int[] initialGuess = freqAttack.initialGuess(text);

        HillClimbingAttack attack = new HillClimbingAttack(ALPHABET, model, System.nanoTime());
        HillClimbingAttack.Result result = attack.attack(text, initialGuess, iterations, restarts);

        System.out.println("Recovered key: " + keyToLetters(result.key));
        System.out.println("Log-likelihood score: " + result.score);
        System.out.println("Decrypted text: " + result.plaintext);
    }

    private int[] readSubstitutionKey() {
        int n = ALPHABET.size();
        String prompt = "Enter the key as a " + n + "-letter permutation of the English alphabet "
                + "(the i-th letter is what 'a'+i maps to, example of a valid key: "
                + "qwertyuiopasdfghjklzxcvbnm): ";
        String keyText = readLine(prompt).trim().toLowerCase(java.util.Locale.ROOT);
        if (keyText.length() != n) {
            throw new IllegalArgumentException("The key must be exactly " + n + " letters long (length entered: "
                    + keyText.length() + ").");
        }
        int[] key = new int[n];
        for (int i = 0; i < n; i++) {
            int idx = ALPHABET.indexOf(keyText.charAt(i));
            if (idx < 0) {
                throw new IllegalArgumentException("Character '" + keyText.charAt(i) + "' is not part of the alphabet.");
            }
            key[i] = idx;
        }
        return key;
    }

    private String keyToLetters(int[] key) {
        StringBuilder sb = new StringBuilder(key.length);
        for (int idx : key) {
            sb.append(ALPHABET.charAt(idx, false));
        }
        return sb.toString();
    }

    private String loadCorpus() {
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
        return FALLBACK_CORPUS;
    }


    private void vigenereMenu() {
        VigenereCipher cipher = new VigenereCipher(ALPHABET);
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Vigenere Cipher ---");
            System.out.println("1) Encrypt");
            System.out.println("2) Decrypt");
            System.out.println("3) Automatic break (Kasiski + IC + Chi-squared)");
            System.out.println("4) Show key space for a given length");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readLine("Enter the plaintext: ");
                    String key = readLine("Key (a string of letters): ");
                    System.out.println("Ciphertext: " + cipher.encrypt(text, key));
                }
                case 2 -> {
                    String text = readLine("Enter the ciphertext: ");
                    String key = readLine("Key (a string of letters): ");
                    System.out.println("Decrypted text: " + cipher.decrypt(text, key));
                }
                case 3 -> breakVigenere();
                case 4 -> {
                    int len = readInt("Key length: ");
                    BigInteger keySpace = KeySpaceCalculator.vigenereKeySpace(ALPHABET.size(), len);
                    System.out.println("|K| = n^L = " + keySpace);
                    printBruteForceEstimate(keySpace);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void breakVigenere() {
        String text = readLine("Enter the ciphertext: ");
        int maxKeyLength = readInt("Maximum key length to search (suggested: 20): ");

        VigenereCryptanalyzer analyzer = new VigenereCryptanalyzer(ALPHABET, FrequencyTable.englishReference());
        VigenereCryptanalyzer.Result result = analyzer.breakCipher(text, maxKeyLength);

        System.out.println("Estimated key length: " + result.estimatedKeyLength);
        System.out.println("Recovered key: " + result.recoveredKey);
        System.out.println("Decrypted text: " + result.plaintext);

        System.out.print("Show IC/Kasiski test details? (y/n): ");
        String answer = in.hasNextLine() ? in.nextLine().trim().toLowerCase(java.util.Locale.ROOT) : "n";
        if (answer.startsWith("y")) {
            IndexOfCoincidence ic = new IndexOfCoincidence(ALPHABET);
            double[] avgIc = ic.averageICForKeyLengths(text, maxKeyLength);
            for (int len = 1; len <= maxKeyLength; len++) {
                System.out.printf(java.util.Locale.ROOT, "L=%2d  average IC=%.4f%n", len, avgIc[len]);
            }
            KasiskiExamination kasiski = new KasiskiExamination(ALPHABET, 3);
            KasiskiExamination.Distances distances = kasiski.findRepeatedSequenceDistances(text);
            System.out.println("Number of repeated sequences found: " + distances.repeatedSequencePositions.size());
        }
    }


    private void hillMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Hill Cipher ---");
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
        String text = readLine(encrypt ? "Enter the plaintext: " : "Enter the ciphertext: ");

        HillCipher cipher = new HillCipher(ALPHABET, m);
        if (encrypt) {
            System.out.println("Ciphertext: " + cipher.encrypt(text, key));
        } else {
            System.out.println("Decrypted text: " + cipher.decrypt(text, key));
        }
    }

    private void hillKnownPlaintextAttack() {
        int m = readInt("Block size (m): ");
        String plaintext = readLine("Enter the known plaintext (at least " + (m * m) + " letters): ");
        String ciphertext = readLine("Enter the corresponding ciphertext (same length): ");

        HillKnownPlaintextAttack attack = new HillKnownPlaintextAttack(ALPHABET, m);
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
                matrix[row][col] = Integer.parseInt(parts[col]);
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
            System.out.println("\n--- Utilities ---");
            System.out.println("1) Shannon entropy of a text");
            System.out.println("2) Letter frequency table of a text");
            System.out.println("0) Back");
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1 -> {
                    String text = readLine("Enter the text: ");
                    double entropy = ShannonEntropy.compute(text, ALPHABET);
                    double maxEntropy = ShannonEntropy.maxEntropy(ALPHABET.size());
                    double efficiency = ShannonEntropy.entropyEfficiencyPercent(text, ALPHABET);
                    System.out.printf(java.util.Locale.ROOT, "H(X) = %.4f bits/char%n", entropy);
                    System.out.printf(java.util.Locale.ROOT, "Theoretical max = %.4f bits/char%n", maxEntropy);
                    System.out.printf(java.util.Locale.ROOT, "Entropy efficiency = %.2f%%%n", efficiency);
                }
                case 2 -> {
                    String text = readLine("Enter the text: ");
                    FrequencyTable table = FrequencyTable.fromCorpus(text, ALPHABET);
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


    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                throw new IllegalStateException("End of input reached.");
            }
            String line = in.nextLine().trim();
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
}