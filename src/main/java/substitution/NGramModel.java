package substitution;

import util.Alphabet;

import java.util.HashMap;
import java.util.Map;

/**
 * مدل زبانی n-gram (دوگرام/سه‌گرام) ساخته‌شده از یک پیکره‌ی متنی، برای
 * محاسبه‌ی log-likelihood در الگوریتم تپه‌نوردی (بخش ۳.۲ گزارش):
 *
 *   Score(π) = Σ log P( π(Ci), π(Ci+1) )     [حالت دوگرام]
 *
 * برای جلوگیری از log(0)، احتمال هر n-gram دیده‌نشده با هموارسازی
 * افزایشی (add-one / Laplace smoothing) محاسبه می‌شود:
 *
 *   P(a,b) = (count(a,b) + 1) / (Σ_{x,y} count(x,y) + n²)
 */
public final class NGramModel {

    private final Alphabet alphabet;
    private final int n;                 // اندازه الفبا
    private final int order;             // 2 = bigram , 3 = trigram
    private final Map<String, Long> counts = new HashMap<>();
    private long totalCount = 0;

    public NGramModel(Alphabet alphabet, int order) {
        if (order != 2 && order != 3) {
            throw new IllegalArgumentException("فقط دوگرام (2) یا سه‌گرام (3) پشتیبانی می‌شود.");
        }
        this.alphabet = alphabet;
        this.order = order;
        this.n = alphabet.size();
    }

    /** یادگیری مدل از یک پیکره‌ی متنی (کامیت الزامی نفر B) */
    public void train(String corpus) {
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < corpus.length(); i++) {
            if (alphabet.indexOf(corpus.charAt(i)) >= 0) {
                cleaned.append(Character.toLowerCase(corpus.charAt(i)));
            }
        }
        String text = cleaned.toString();
        for (int i = 0; i + order <= text.length(); i++) {
            String gram = text.substring(i, i + order);
            counts.merge(gram, 1L, Long::sum);
            totalCount++;
        }
    }

    /** لگاریتم احتمال هموارشده‌ی یک n-gram (add-one smoothing) */
    public double logProbability(String gram) {
        long c = counts.getOrDefault(gram, 0L);
        double denomStates = Math.pow(n, order);
        double p = (c + 1.0) / (totalCount + denomStates);
        return Math.log(p);
    }

    /**
     * امتیاز log-likelihood کل یک متن (مجموع لگاریتم احتمال تمام n-gram های آن)؛
     * دقیقاً معادل فرمول Score(π) بخش ۳.۲ گزارش وقتی متن، خروجیِ رمزگشاییِ
     * فرضی با جایگشت π است.
     */
    public double scoreText(String text) {
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (alphabet.indexOf(text.charAt(i)) >= 0) {
                cleaned.append(Character.toLowerCase(text.charAt(i)));
            }
        }
        String t = cleaned.toString();
        double score = 0.0;
        for (int i = 0; i + order <= t.length(); i++) {
            score += logProbability(t.substring(i, i + order));
        }
        return score;
    }

    public int order() {
        return order;
    }
}
