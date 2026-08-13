package substitution;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.Alphabet;
import util.FrequencyTable;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های ماژول رمز جانشینی: صحت رمزگذاری/رمزگشایی، اعتبارسنجی کلید،
 * حدس اولیه بر پایه فراوانی، و بهبود امتیاز با تپه‌نوردی.
 *
 * نکته‌ی مهم درباره‌ی تست تپه‌نوردی: بر خلاف رمز سزار که فضای کلید کوچک (n)
 * دارد و شکست آن قطعی است، شکست رمز جانشینی با تپه‌نوردی یک الگوریتم
 * ابتکاری (heuristic) است و *تضمینی* برای بازیابی کامل کلید روی متن‌های
 * کوتاه یا با پیکره‌ی آموزشی کوچک وجود ندارد (این موضوع دقیقاً در بخش ۷.۲
 * گزارش، نمودار نرخ موفقیت بر حسب طول متن، مستند شده است). بنابراین تست
 * زیر *بهبود امتیاز* را بررسی می‌کند، نه تساوی دقیق با متن اصلی —
 * رویکردی واقع‌بینانه و غیرشکننده (non-brittle) برای این نوع الگوریتم.
 */
class SubstitutionCipherTest {

    private static final Alphabet ALPHABET = Alphabet.english();
    private final SubstitutionCipher cipher = new SubstitutionCipher(ALPHABET);

    @Test
    void encryptThenDecryptReturnsOriginalText() {
        int[] key = SubstitutionCipher.randomKey(26, new Random(123));
        String original = "Discrete Mathematics and Cryptography";
        String encrypted = cipher.encrypt(original, key);
        String decrypted = cipher.decrypt(encrypted, key);
        assertEquals(original, decrypted);
    }

    @Test
    void invalidKeyWithDuplicateThrows() {
        int[] badKey = new int[26];
        // دو حرف به یک مقصد نگاشت شده‌اند -> جایگشت نامعتبر
        for (int i = 0; i < 26; i++) badKey[i] = 0;
        assertThrows(IllegalArgumentException.class, () -> cipher.encrypt("test", badKey));
    }

    @Test
    void invalidKeyWithWrongLengthThrows() {
        int[] shortKey = {0, 1, 2};
        assertThrows(IllegalArgumentException.class, () -> cipher.encrypt("test", shortKey));
    }

    @Test
    void invertProducesCorrectInversePermutation() {
        int[] key = {2, 0, 1}; // 0->2, 1->0, 2->1
        int[] inverse = SubstitutionCipher.invert(key);
        // بررسی: key[inverse[i]] == i برای هر i
        for (int i = 0; i < key.length; i++) {
            assertEquals(i, key[inverse[i]]);
        }
    }

    @Test
    void frequencyAnalysisInitialGuessIsValidPermutation() {
        int[] trueKey = SubstitutionCipher.randomKey(26, new Random(7));
        String plaintext = "the quick brown fox jumps over the lazy dog and discrete mathematics "
                + "provides the foundation for modern cryptography and computer science theory";
        String ciphertext = cipher.encrypt(plaintext, trueKey);

        FrequencyAnalysisAttack attack = new FrequencyAnalysisAttack(ALPHABET, FrequencyTable.englishReference());
        int[] guess = attack.initialGuess(ciphertext);

        // بررسی معتبر بودن حدس اولیه به عنوان جایگشت (نه لزوماً صحت کامل)
        assertDoesNotThrow(() -> cipher.validateKey(guess));
    }

    @Test
    void nGramModelAssignsHigherScoreToRealisticText() {
        NGramModel bigramModel = new NGramModel(ALPHABET, 2);
        bigramModel.train("the quick brown fox jumps over the lazy dog the discrete mathematics "
                + "provides the foundation for modern cryptography the study of number theory");

        double scoreReal = bigramModel.scoreText("the quick brown fox jumps over the lazy dog");
        double scoreRandom = bigramModel.scoreText("xzq vjkw plmr fbnt hgds qzxw vjpl mrbnt");

        assertTrue(scoreReal > scoreRandom,
                "متن نزدیک به زبان طبیعی باید امتیاز log-likelihood بالاتری نسبت به متن تصادفی داشته باشد");
    }

    @Test
    void hillClimbingImprovesScoreOverInitialFrequencyGuess() {
        int[] trueKey = SubstitutionCipher.randomKey(26, new Random(42));
        String plaintext = "the quick brown fox jumps over the lazy dog and discrete mathematics "
                + "provides the foundation for modern cryptography and computer science theory "
                + "including graph theory number theory and combinatorics which are essential "
                + "for algorithm design and analysis in modern computing systems";
        String ciphertext = cipher.encrypt(plaintext, trueKey);

        NGramModel bigramModel = new NGramModel(ALPHABET, 2);
        bigramModel.train(TRAINING_CORPUS);

        FrequencyAnalysisAttack freqAttack = new FrequencyAnalysisAttack(ALPHABET, FrequencyTable.englishReference());
        int[] initialGuess = freqAttack.initialGuess(ciphertext);
        double initialScore = bigramModel.scoreText(cipher.decrypt(ciphertext, initialGuess));

        HillClimbingAttack hillClimbing = new HillClimbingAttack(ALPHABET, bigramModel, 7L);
        HillClimbingAttack.Result result = hillClimbing.attack(ciphertext, initialGuess, 3000, 5);

        assertTrue(result.score >= initialScore,
                "امتیاز تپه‌نوردی نباید هرگز از حدس اولیه‌ی فراوانی بدتر شود (خاصیت hill climbing)");
    }

    private static final String TRAINING_CORPUS = String.join(" ",
            java.util.Collections.nCopies(10,
                    "the quick brown fox jumps over the lazy dog discrete mathematics is the branch "
                    + "of mathematics dealing with objects that can assume only distinct separated "
                    + "values it includes graph theory combinatorics number theory and mathematical "
                    + "logic cryptography relies heavily on discrete mathematics especially number "
                    + "theory and combinatorics classical ciphers such as the caesar cipher the "
                    + "substitution cipher and the vigenere cipher are simple historical examples"));
}
