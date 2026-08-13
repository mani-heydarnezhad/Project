package vigenere;

import org.junit.jupiter.api.Test;
import util.Alphabet;
import util.FrequencyTable;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های ماژول رمز ویژنر: صحت رمزگذاری/رمزگشایی، شاخص تطابق، آزمون کاسیسکی
 * و بازیابی کامل کلید روی متن بلند.
 */
class VigenereCipherTest {

    private static final Alphabet ALPHABET = Alphabet.english();
    private final VigenereCipher cipher = new VigenereCipher(ALPHABET);

    @Test
    void encryptThenDecryptReturnsOriginalText() {
        String plaintext = "Attack at Dawn, Discrete Mathematics!";
        String key = "lemon";
        String encrypted = cipher.encrypt(plaintext, key);
        String decrypted = cipher.decrypt(encrypted, key);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void nonAlphabeticCharactersPassThroughAndKeyDoesNotAdvance() {
        // مطابق طراحی: فقط حروف الفبا شمارنده‌ی کلید را پیش می‌برند
        String plaintext = "AB CD";
        String encrypted = cipher.encrypt(plaintext, "xy");
        // A(0)+x(23)=X , B(1)+y(24)=Z , C(2)+x(23)=Z , D(3)+y(24)=B
        assertEquals("XZ ZB", encrypted);
    }

    @Test
    void keySpaceSizeFollowsNPowerL() {
        assertEquals(BigInteger.valueOf(26), cipher.keySpaceSize(1));
        assertEquals(BigInteger.valueOf(26L * 26L), cipher.keySpaceSize(2));
        assertEquals(BigInteger.valueOf(26).pow(5), cipher.keySpaceSize(5));
    }

    @Test
    void indexOfCoincidenceIsHigherForNaturalTextThanRandomText() {
        IndexOfCoincidence icCalc = new IndexOfCoincidence(ALPHABET);
        String naturalText = "the quick brown fox jumps over the lazy dog and discrete mathematics "
                + "provides the foundation for modern cryptography and computer science theory";
        double icNatural = icCalc.compute(naturalText);

        // متن رمزشده با کلید بلند (نزدیک به تصادفی) باید IC پایین‌تری داشته باشد
        String pseudoRandomKey = "qwertyuiopasdfghjklzxcvbnm"; // طول کلید = اندازه الفبا
        String scrambled = cipher.encrypt(naturalText, pseudoRandomKey);
        double icScrambled = icCalc.compute(scrambled);

        assertTrue(icNatural > icScrambled,
                "IC متن طبیعی باید به‌طور محسوس بالاتر از IC متن رمزشده با کلید بلند باشد");
        assertTrue(icNatural > 0.06, "IC متن انگلیسی طبیعی باید نزدیک بازه ۰.۰۶۵-۰.۰۷۰ باشد");
    }

    @Test
    void kasiskiFindsRepeatedSequencesAndComputesDistances() {
        KasiskiExamination kasiski = new KasiskiExamination(ALPHABET, 3);
        // متن با تکرار عمدی "ABCABC" برای ایجاد دنباله‌های تکراری در رمزشده
        String plaintext = ("crypto crypto crypto crypto crypto crypto crypto crypto").repeat(1);
        String ciphertext = cipher.encrypt(plaintext, "key");
        KasiskiExamination.Distances distances = kasiski.findRepeatedSequenceDistances(ciphertext);
        assertFalse(distances.distances.isEmpty(), "باید حداقل یک فاصله‌ی تکراری یافت شود");
        // همه فاصله‌ها باید مضرب طول کلید (3) باشند چون الگو با دوره‌ی کلید هم‌فاز است
        for (int d : distances.distances) {
            assertEquals(0, d % 3, "فاصله باید مضرب طول کلید باشد: " + d);
        }
    }

    @Test
    void fullKeyRecoveryOnLongTextWithKnownKeyLength() {
        String plaintext = "the quick brown fox jumps over the lazy dog and discrete mathematics provides "
                + "the foundation for modern cryptography and computer science theory including graph "
                + "theory number theory and combinatorics which are essential for algorithm design and "
                + "analysis in modern computing systems where efficient algorithms play a critical role "
                + "in practice especially when dealing with large scale data processing and real time "
                + "constraints that arise in distributed systems network protocols and secure communication";
        String trueKey = "mathkey";
        String ciphertext = cipher.encrypt(plaintext, trueKey);

        VigenereCryptanalyzer analyzer = new VigenereCryptanalyzer(ALPHABET, FrequencyTable.englishReference());
        VigenereCryptanalyzer.Result result = analyzer.breakCipherWithKnownLength(ciphertext, trueKey.length());

        assertEquals(trueKey, result.recoveredKey);
        assertEquals(plaintext, result.plaintext);
    }

    @Test
    void fullKeyRecoveryWithAutomaticKeyLengthEstimation() {
        String plaintext = "the quick brown fox jumps over the lazy dog and discrete mathematics provides "
                + "the foundation for modern cryptography and computer science theory including graph "
                + "theory number theory and combinatorics which are essential for algorithm design and "
                + "analysis in modern computing systems where efficient algorithms play a critical role "
                + "in practice especially when dealing with large scale data processing and real time "
                + "constraints that arise in distributed systems network protocols and secure communication";
        String trueKey = "mathkey";
        String ciphertext = cipher.encrypt(plaintext, trueKey);

        VigenereCryptanalyzer analyzer = new VigenereCryptanalyzer(ALPHABET, FrequencyTable.englishReference());
        VigenereCryptanalyzer.Result result = analyzer.breakCipher(ciphertext, 15);

        // طول برآوردشده ممکن است خودِ L یا مضربی از آن باشد (مثلاً 2L)؛
        // در هر دو حالت متن اصلی باید به‌درستی بازیابی شود.
        assertEquals(0, result.estimatedKeyLength % trueKey.length(),
                "طول برآوردشده باید مضرب طول کلید واقعی باشد");
        assertEquals(plaintext, result.plaintext);
    }
}
