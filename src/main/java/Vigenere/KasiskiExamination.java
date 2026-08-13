package vigenere;

import util.Alphabet;

import java.util.*;

/**
 * آزمون کاسیسکی (Kasiski Examination) برای برآورد طول کلید رمز ویژنر
 * (بخش ۴.۲ گزارش).
 *
 * ایده: اگر یک زیررشته‌ی تکراری (معمولاً طول ۳ یا بیشتر) در متن اصلی، در
 * فاصله‌ای برابر با مضرب طول کلید L ظاهر شود، رمزشده‌ی آن دو رخداد نیز
 * (چون هر دو با همان بخش از کلید رمز شده‌اند) یکسان خواهد بود. با یافتن
 * فاصله‌ی میان تکرارهای یکسان در متن رمزشده و محاسبه‌ی gcd این فاصله‌ها،
 * می‌توان L را برآورد کرد:
 *
 *   L ≈ gcd(d1, d2, ..., dm)
 */
public final class KasiskiExamination {

    private final Alphabet alphabet;
    private final int minSequenceLength;

    public KasiskiExamination(Alphabet alphabet, int minSequenceLength) {
        this.alphabet = alphabet;
        this.minSequenceLength = Math.max(3, minSequenceLength);
    }

    public static final class Distances {
        public final Map<String, List<Integer>> repeatedSequencePositions;
        public final List<Integer> distances;

        Distances(Map<String, List<Integer>> positions, List<Integer> distances) {
            this.repeatedSequencePositions = positions;
            this.distances = distances;
        }
    }

    /** یافتن تمام دنباله‌های تکراری با طول >= minSequenceLength و فاصله‌ی آن‌ها */
    public Distances findRepeatedSequenceDistances(String ciphertext) {
        // فقط حروف الفبا (بدون فاصله/علائم) در نظر گرفته می‌شود تا فاصله‌ها معنادار باشند
        StringBuilder cleanedBuilder = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            if (alphabet.indexOf(ciphertext.charAt(i)) >= 0) {
                cleanedBuilder.append(Character.toLowerCase(ciphertext.charAt(i)));
            }
        }
        String cleaned = cleanedBuilder.toString();

        Map<String, List<Integer>> positions = new LinkedHashMap<>();
        for (int i = 0; i + minSequenceLength <= cleaned.length(); i++) {
            String seq = cleaned.substring(i, i + minSequenceLength);
            positions.computeIfAbsent(seq, s -> new ArrayList<>()).add(i);
        }

        List<Integer> distances = new ArrayList<>();
        Iterator<Map.Entry<String, List<Integer>>> it = positions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Integer>> entry = it.next();
            List<Integer> pos = entry.getValue();
            if (pos.size() < 2) {
                it.remove(); // دنباله‌های غیرتکراری حذف می‌شوند
                continue;
            }
            for (int i = 1; i < pos.size(); i++) {
                distances.add(pos.get(i) - pos.get(i - 1));
            }
        }
        return new Distances(positions, distances);
    }

    /**
     * برآورد طول کلید محتمل با محاسبه‌ی gcd همه‌ی فاصله‌ها.
     * در صورت وجود نویز آماری، رایج‌ترین مقسوم‌علیه‌های کوچک (2 تا maxKeyLength)
     * که بیشترین تعداد فاصله را می‌شمارند به عنوان کاندید برگردانده می‌شوند.
     */
    public List<Integer> estimateKeyLengthCandidates(List<Integer> distances, int maxKeyLength) {
        int[] votes = new int[maxKeyLength + 1];
        for (int d : distances) {
            for (int L = 2; L <= maxKeyLength; L++) {
                if (d % L == 0) votes[L]++;
            }
        }
        List<Integer> candidates = new ArrayList<>();
        for (int L = 2; L <= maxKeyLength; L++) candidates.add(L);
        candidates.sort((a, b) -> Integer.compare(votes[b], votes[a]));
        return candidates;
    }

    public static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static int gcdOfList(List<Integer> numbers) {
        int result = 0;
        for (int num : numbers) result = gcd(result, num);
        return result;
    }
}
