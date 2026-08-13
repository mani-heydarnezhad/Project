package vigenere;

import util.Alphabet;

import java.util.ArrayList;
import java.util.List;

/**
 * شاخص تطابق (Index of Coincidence – IC) — بخش ۴.۳ گزارش.
 *
 *   IC = [ Σ_{i=0}^{n-1} fi (fi − 1) ] / [ N (N − 1) ]
 *
 * که fi فراوانی مطلق حرف i و N طول کل متن است. برای زبان انگلیسی طبیعی
 * IC ≈ 0.065–0.070 و برای متن تصادفی/رمز جانشینی با کلید بلند IC ≈ 1/n ≈ 0.038.
 */
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

    /**
     * برای هر طول کلید فرضی L در بازه‌ی [1, maxKeyLength]، متن رمزشده به L
     * زیرمتن (بر اساس موقعیت i mod L) تقسیم شده و میانگین IC این زیرمتن‌ها
     * محاسبه می‌شود. طولی که میانگین IC آن به بازه‌ی طبیعی زبان (~0.065-0.070
     * برای انگلیسی) نزدیک‌تر باشد، به عنوان طول کلید محتمل انتخاب می‌شود.
     */
    public double[] averageICForKeyLengths(String ciphertext, int maxKeyLength) {
        // فقط حروف الفبا برای صحت شاخص‌گذاری i mod L در نظر گرفته می‌شود
        StringBuilder cleanedBuilder = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            if (alphabet.indexOf(ciphertext.charAt(i)) >= 0) {
                cleanedBuilder.append(ciphertext.charAt(i));
            }
        }
        String cleaned = cleanedBuilder.toString();

        double[] avgIC = new double[maxKeyLength + 1]; // ایندکس 0 استفاده نمی‌شود
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

    /** مرجع تقریبی IC زبان انگلیسی طبیعی، برای مقایسه در گزارش و انتخاب بهترین L */
    public static final double ENGLISH_NATURAL_IC = 0.0667;
    public static final double RANDOM_TEXT_IC_ENGLISH = 1.0 / 26.0; // ≈ 0.0385
}
