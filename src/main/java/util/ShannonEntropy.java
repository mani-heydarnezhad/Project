package util;


public final class ShannonEntropy {

    private ShannonEntropy() {
    }

    
    public static double compute(String text, Alphabet alphabet) {
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
        if (total == 0) return 0.0;

        double entropy = 0.0;
        for (long c : counts) {
            if (c == 0) continue;
            double p = (double) c / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    
    public static double maxEntropy(int alphabetSize) {
        return Math.log(alphabetSize) / Math.log(2);
    }

    
    public static double entropyEfficiencyPercent(String text, Alphabet alphabet) {
        double h = compute(text, alphabet);
        double hMax = maxEntropy(alphabet.size());
        return 100.0 * h / hMax;
    }
}
