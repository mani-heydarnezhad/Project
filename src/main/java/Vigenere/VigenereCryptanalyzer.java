package vigenere;

import caesar.CaesarCipher;
import caesar.CaesarCryptanalyzer;
import util.Alphabet;
import util.FrequencyTable;

import java.util.ArrayList;
import java.util.List;

/**
 * بازیابی کامل کلید رمز ویژنر (بخش ۴.۴ گزارش).
 *
 * الگوریتم:
 *  ۱) برآورد طول کلید L با ترکیب آزمون کاسیسکی و شاخص تطابق (IC)
 *  ۲) تقسیم متن رمزشده به L زیرمتن مستقل (بر اساس i mod L)
 *  ۳) هر زیرمتن در واقع با یک رمز سزار ساده رمزگذاری شده؛ با استفاده از
 *     همان الگوریتم χ² کلاس {@link CaesarCryptanalyzer} (بخش ۲.۲ گزارش)،
 *     تک‌تک حروف کلید بازیابی می‌شوند.
 */
public final class VigenereCryptanalyzer {

    private final Alphabet alphabet;
    private final FrequencyTable reference;
    private final IndexOfCoincidence icCalculator;
    private final KasiskiExamination kasiski;

    public VigenereCryptanalyzer(Alphabet alphabet, FrequencyTable reference) {
        this.alphabet = alphabet;
        this.reference = reference;
        this.icCalculator = new IndexOfCoincidence(alphabet);
        this.kasiski = new KasiskiExamination(alphabet, 3);
    }

    public static final class Result {
        public final int estimatedKeyLength;
        public final String recoveredKey;
        public final String plaintext;
        public final double[] icByKeyLength;

        Result(int estimatedKeyLength, String recoveredKey, String plaintext, double[] icByKeyLength) {
            this.estimatedKeyLength = estimatedKeyLength;
            this.recoveredKey = recoveredKey;
            this.plaintext = plaintext;
            this.icByKeyLength = icByKeyLength;
        }
    }

    /**
     * برآورد طول کلید با انتخاب طولی که میانگین IC آن به IC طبیعی زبان
     * (بخش ۴.۳ گزارش) نزدیک‌ترین است، در میان کاندیدهای پیشنهادی کاسیسکی.
     */
    public int estimateKeyLength(String ciphertext, int maxKeyLength) {
        double[] avgIC = icCalculator.averageICForKeyLengths(ciphertext, maxKeyLength);

        KasiskiExamination.Distances distances = kasiski.findRepeatedSequenceDistances(ciphertext);
        List<Integer> kasiskiCandidates;
        if (!distances.distances.isEmpty()) {
            kasiskiCandidates = kasiski.estimateKeyLengthCandidates(distances.distances, maxKeyLength);
        } else {
            kasiskiCandidates = new ArrayList<>();
            for (int L = 1; L <= maxKeyLength; L++) kasiskiCandidates.add(L);
        }

        // از میان چند کاندید برتر کاسیسکی، طولی را انتخاب می‌کنیم که IC آن
        // به IC طبیعی زبان نزدیک‌تر است (ترکیب دو روش، مطابق بخش ۴.۳-۴.۴ گزارش)
        int bestL = kasiskiCandidates.get(0);
        double bestDiff = Double.MAX_VALUE;
        int topN = Math.min(5, kasiskiCandidates.size());
        for (int i = 0; i < topN; i++) {
            int L = kasiskiCandidates.get(i);
            double diff = Math.abs(avgIC[L] - IndexOfCoincidence.ENGLISH_NATURAL_IC);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestL = L;
            }
        }
        return bestL;
    }

    /** بازیابی کامل کلید و متن اصلی، با برآورد خودکار طول کلید */
    public Result breakCipher(String ciphertext, int maxKeyLength) {
        int L = estimateKeyLength(ciphertext, maxKeyLength);
        double[] icByLength = icCalculator.averageICForKeyLengths(ciphertext, maxKeyLength);
        return breakCipherWithKnownLength(ciphertext, L, icByLength);
    }

    /** بازیابی کلید وقتی طول کلید از قبل مشخص است (برای تست و مقایسه) */
    public Result breakCipherWithKnownLength(String ciphertext, int keyLength) {
        return breakCipherWithKnownLength(ciphertext, keyLength,
                icCalculator.averageICForKeyLengths(ciphertext, keyLength));
    }

    private Result breakCipherWithKnownLength(String ciphertext, int keyLength, double[] icByLength) {
        // فقط حروف الفبا برای صحت شاخص‌گذاری ستون‌ها در نظر گرفته می‌شود
        StringBuilder cleanedBuilder = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            if (alphabet.indexOf(ciphertext.charAt(i)) >= 0) {
                cleanedBuilder.append(ciphertext.charAt(i));
            }
        }
        String cleaned = cleanedBuilder.toString();

        StringBuilder[] columns = new StringBuilder[keyLength];
        for (int c = 0; c < keyLength; c++) columns[c] = new StringBuilder();
        for (int i = 0; i < cleaned.length(); i++) {
            columns[i % keyLength].append(cleaned.charAt(i));
        }

        CaesarCipher caesar = new CaesarCipher(alphabet);
        CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(caesar, reference);

        StringBuilder keyBuilder = new StringBuilder();
        for (StringBuilder col : columns) {
            CaesarCryptanalyzer.Result r = analyzer.breakCipher(col.toString());
            keyBuilder.append(alphabet.charAt(r.key, false));
        }
        String recoveredKey = keyBuilder.toString();

        VigenereCipher vigenere = new VigenereCipher(alphabet);
        String plaintext = vigenere.decrypt(ciphertext, recoveredKey);

        return new Result(keyLength, recoveredKey, plaintext, icByLength);
    }
}
