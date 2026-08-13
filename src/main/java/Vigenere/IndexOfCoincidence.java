package vigenere;

import util.Alphabet;

import java.util.ArrayList;
import java.util.List;


public final class IndexOfCoincidence {

    private final Alphabet alphabet;

    public IndexOfCoincidence(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public double compute(String text) {
        int n = alphabet.size();
        long[] counts = new long[n];
        long total = 0;
        for (int i = 0; i < text.length(); i++) {
            int idx = alphabet.indexOf(text.charAt(i));
            if (idx >= 0) {
                counts[idx]++;
                total++;
            }
        }
        if (total < 2) return 0.0;

        double numerator = 0.0;
        for (long f : counts) {
            numerator += f * (f - 1);
        }
        double denominator = (double) total * (total - 1);
        return numerator / denominator;
    }

    public double[] averageICForKeyLengths(String ciphertext, int maxKeyLength) {
        
        StringBuilder cleanedBuilder = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            if (alphabet.indexOf(ciphertext.charAt(i)) >= 0) {
                cleanedBuilder.append(ciphertext.charAt(i));
            }
        }
        String cleaned = cleanedBuilder.toString();

        double[] avgIC = new double[maxKeyLength + 1]; 
        for (int L = 1; L <= maxKeyLength; L++) {
            List<StringBuilder> columns = new ArrayList<>();
            for (int c = 0; c < L; c++) columns.add(new StringBuilder());
            for (int i = 0; i < cleaned.length(); i++) {
                columns.get(i % L).append(cleaned.charAt(i));
            }
            double sum = 0.0;
            for (StringBuilder col : columns) {
                sum += compute(col.toString());
            }
            avgIC[L] = sum / L;
        }
        return avgIC;
    }

    
    public static final double ENGLISH_NATURAL_IC = 0.0667;
    public static final double RANDOM_TEXT_IC_ENGLISH = 1.0 / 26.0; 
}
