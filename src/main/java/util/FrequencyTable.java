package util;

import java.util.LinkedHashMap;
import java.util.Map;


public final class FrequencyTable {

    private final Alphabet alphabet;
    private final double[] relativeFrequency; 

    private FrequencyTable(Alphabet alphabet, double[] relativeFrequency) {
        this.alphabet = alphabet;
        this.relativeFrequency = relativeFrequency;
    }

    // جدول فراوانی مرجع زبان انگلیسی (منابع کلاسیک تحلیل رمز) 
    public static FrequencyTable englishReference() {
        // ترتیب مطابق Alphabet.ENGLISH: a b c d e f g h i j k l m n o p q r s t u v w x y z
        double[] freq = {
                8.2, 1.5, 2.8, 4.3, 12.7, 2.2, 2.0, 6.1, 7.0, 0.15, 0.77, 4.0, 2.4,
                6.7, 7.5, 1.9, 0.095, 6.0, 6.3, 9.1, 2.8, 0.98, 2.4, 0.15, 2.0, 0.074
        };
        return new FrequencyTable(Alphabet.english(), freq);
    }

    public static FrequencyTable persianApproximate() {
        int n = Alphabet.persian().size();
        double[] freq = new double[n];
        java.util.Arrays.fill(freq, 100.0 / n); // یکنواخت -- جای‌گزین شود با داده واقعی
        return new FrequencyTable(Alphabet.persian(), freq);
    }

    
    public static FrequencyTable fromCorpus(String corpus, Alphabet alphabet) {
        long[] counts = new long[alphabet.size()];
        long total = 0;
        for (int i = 0; i < corpus.length(); i++) {
            int idx = alphabet.indexOf(corpus.charAt(i));
            if (idx >= 0) {
                counts[idx]++;
                total++;
            }
        }
        double[] freq = new double[alphabet.size()];
        if (total == 0) {
            throw new IllegalArgumentException("The corpus does not contain any characters from the given alphabet.");
        }
        for (int i = 0; i < counts.length; i++) {
            freq[i] = 100.0 * counts[i] / total;
        }
        return new FrequencyTable(alphabet, freq);
    }

    public Alphabet alphabet() {
        return alphabet;
    }

    public double frequencyOf(int letterIndex) {
        return relativeFrequency[letterIndex];
    }

    public double[] toArray() {
        return relativeFrequency.clone();
    }

    // نمایش خوانا به صورت map حرف -> درصد، مرتب‌شده بر اساس فراوانی نزولی 
    public Map<Character, Double> sortedByFrequencyDescending() {
        Map<Character, Double> map = new LinkedHashMap<>();
        Integer[] order = new Integer[relativeFrequency.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) -> Double.compare(relativeFrequency[b], relativeFrequency[a]));
        for (int idx : order) {
            map.put(alphabet.charAt(idx, false), relativeFrequency[idx]);
        }
        return map;
    }
}
