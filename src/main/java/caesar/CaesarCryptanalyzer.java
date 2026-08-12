package caesar;

import util.Alphabet;
import util.FrequencyTable;

/**
 * شکست رمز سزار با جستجوی فراگیر (Brute-force) روی تمام n کلید ممکن،
 * و انتخاب محتمل‌ترین کلید بر اساس کمینه‌سازی آماره‌ی chi-squared
 * نسبت به جدول فراوانی مرجع زبان (بخش ۲.۲ گزارش):
 *
 *   χ²(k) = Σ_{i=0}^{n-1} (Oi(k) − Ei)² / Ei
 *
 * پیچیدگی زمانی: O(n · m) که m طول متن رمزشده است.
 */
public final class CaesarCryptanalyzer {

    private final CaesarCipher cipher;
    private final FrequencyTable reference;

    public CaesarCryptanalyzer(CaesarCipher cipher, FrequencyTable reference) {
        this.cipher = cipher;
        this.reference = reference;
    }

    public static final class Result {
        public final int key;
        public final double chiSquared;
        public final String plaintext;

        public Result(int key, double chiSquared, String plaintext) {
            this.key = key;
            this.chiSquared = chiSquared;
            this.plaintext = plaintext;
        }

        @Override
        public String toString() {
            return String.format("key=%d, chi^2=%.4f, plaintext=\"%s\"", key, chiSquared, plaintext);
        }
    }

    /** اجرای حمله‌ی brute-force؛ نتیجه با کمترین χ² به عنوان بهترین حدس برگردانده می‌شود. */
    public Result breakCipher(String ciphertext) {
        Alphabet alphabet = cipher.alphabet();
        int n = alphabet.size();

        Result best = null;
        for (int k = 0; k < n; k++) {
            String candidate = cipher.decrypt(ciphertext, k);
            double chi2 = chiSquared(candidate, alphabet);
            if (best == null || chi2 < best.chiSquared) {
                best = new Result(k, chi2, candidate);
            }
        }
        return best;
    }

    /** بازگرداندن تمام n نتیجه به ترتیب کلید، برای بررسی دستی/گزارش‌گیری */
    public Result[] tryAllKeys(String ciphertext) {
        Alphabet alphabet = cipher.alphabet();
        int n = alphabet.size();
        Result[] results = new Result[n];
        for (int k = 0; k < n; k++) {
            String candidate = cipher.decrypt(ciphertext, k);
            double chi2 = chiSquared(candidate, alphabet);
            results[k] = new Result(k, chi2, candidate);
        }
        return results;
    }

    private double chiSquared(String text, Alphabet alphabet) {
        int n = alphabet.size();
        long[] observedCounts = new long[n];
        long total = 0;
        for (int i = 0; i < text.length(); i++) {
            int idx = alphabet.indexOf(text.charAt(i));
            if (idx >= 0) {
                observedCounts[idx]++;
                total++;
            }
        }
        if (total == 0) return Double.MAX_VALUE;

        double chi2 = 0.0;
        for (int i = 0; i < n; i++) {
            double observedPercent = 100.0 * observedCounts[i] / total;
            double expectedPercent = reference.frequencyOf(i);
            if (expectedPercent <= 0) continue;
            double diff = observedPercent - expectedPercent;
            chi2 += (diff * diff) / expectedPercent;
        }
        return chi2;
    }
}
