package util;

/**
 * محاسبه‌ی آنتروپی شانون — بخش امتیازی (بخش ۶ گزارش اصلی).
 *
 *   H(X) = − Σ_i P(xi) · log2( P(xi) )
 *
 * برای مقایسه‌ی آنتروپی متن اصلی و متن رمزشده با رمزهای مختلف: از آنجا که
 * رمزهای سزار و جانشینی فقط جایگشت (برچسب‌گذاری مجدد) حروف را انجام می‌دهند
 * و توزیع آماری فراوانی حروف را تغییر نمی‌دهند، آنتروپی متن رمزشده با این
 * رمزها دقیقاً برابر با آنتروپی متن اصلی است. رمز ویژنر با کلید بلند، توزیع
 * را به سمت یکنواخت‌تر (و آنتروپی را به سمت log2(n)) نزدیک می‌کند.
 */
public final class ShannonEntropy {

    private ShannonEntropy() {
    }

    /** آنتروپی شانون بر حسب بیت بر نویسه، بر پایه‌ی فراوانی حروف الفبای داده‌شده */
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

    /** حداکثر آنتروپی نظری برای الفبای اندازه n (توزیع کاملاً یکنواخت): log2(n) */
    public static double maxEntropy(int alphabetSize) {
        return Math.log(alphabetSize) / Math.log(2);
    }

    /**
     * درصد بهره‌وری آنتروپی نسبت به حداکثر ممکن — معیاری از میزان
     * «تصادفی‌نمایی» (randomness) ظاهری متن. مقدار پایین یعنی افزونگی بالا
     * (مانند متن زبان طبیعی)، مقدار نزدیک ۱۰۰٪ یعنی توزیع نزدیک یکنواخت.
     */
    public static double entropyEfficiencyPercent(String text, Alphabet alphabet) {
        double h = compute(text, alphabet);
        double hMax = maxEntropy(alphabet.size());
        return 100.0 * h / hMax;
    }
}
