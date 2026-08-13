package caesar;

import org.junit.jupiter.api.Test;
import util.Alphabet;
import util.FrequencyTable;
import util.KeySpaceCalculator;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های واحد ماژول رمز سزار.
 * پوشش: صحت رمزگذاری/رمزگشایی، حفظ حالت حروف، کاراکترهای خارج از الفبا،
 * صحت شکست خودکار روی متون کوتاه و بلند، و محاسبه‌ی فضای کلید.
 */
class CaesarCipherTest {

    private final CaesarCipher cipher = new CaesarCipher(Alphabet.english());

    @Test
    void encryptThenDecryptReturnsOriginalText() {
        String original = "Hello, Discrete Mathematics!";
        int key = 5;
        String encrypted = cipher.encrypt(original, key);
        String decrypted = cipher.decrypt(encrypted, key);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptShiftsLettersCorrectly() {
        // A(0) + 3 = D(3)
        assertEquals("D", cipher.encrypt("A", 3));
        // Z(25) + 3 = C(2)  -- بررسی سرریز پیمانه‌ای (mod n)
        assertEquals("C", cipher.encrypt("Z", 3));
    }

    @Test
    void decryptHandlesModularUnderflow() {
        // A(0) - 3 = X(23) -- بررسی floorMod برای مقادیر منفی
        assertEquals("X", cipher.decrypt("A", 3));
    }

    @Test
    void nonAlphabeticCharactersPassThroughUnchanged() {
        String text = "Attack at Dawn! 123.";
        String encrypted = cipher.encrypt(text, 4);
        // فاصله، علائم و اعداد باید بدون تغییر بمانند
        assertTrue(encrypted.contains("!"));
        assertTrue(encrypted.contains("123"));
        assertTrue(encrypted.contains(" "));
    }

    @Test
    void caseIsPreserved() {
        String encrypted = cipher.encrypt("Abc", 1);
        assertEquals("Bcd", encrypted);
        assertTrue(Character.isUpperCase(encrypted.charAt(0)));
        assertTrue(Character.isLowerCase(encrypted.charAt(1)));
    }

    @Test
    void keySpaceSizeEqualsAlphabetSize() {
        assertEquals(26, cipher.keySpaceSize());
        assertEquals(BigInteger.valueOf(26), KeySpaceCalculator.caesarKeySpace(26));
    }

    @Test
    void cryptanalysisRecoversCorrectKeyOnLongText() {
        String plaintext = "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG AND DISCRETE "
                + "MATHEMATICS PROVIDES THE FOUNDATION FOR MODERN CRYPTOGRAPHY AND "
                + "COMPUTER SCIENCE THEORY INCLUDING GRAPH THEORY NUMBER THEORY AND "
                + "COMBINATORICS WHICH ARE ESSENTIAL FOR ALGORITHM DESIGN AND ANALYSIS";
        int trueKey = 11;
        String ciphertext = cipher.encrypt(plaintext, trueKey);

        FrequencyTable reference = FrequencyTable.englishReference();
        CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(cipher, reference);
        CaesarCryptanalyzer.Result result = analyzer.breakCipher(ciphertext);

        assertEquals(trueKey, result.key, "روی متن بلند، chi-squared باید کلید صحیح را بیابد");
        assertEquals(plaintext, result.plaintext);
    }

    @Test
    void cryptanalysisTryAllKeysReturnsFullKeySpace() {
        String ciphertext = cipher.encrypt("SECRET MESSAGE FOR TESTING PURPOSES", 9);
        FrequencyTable reference = FrequencyTable.englishReference();
        CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(cipher, reference);
        CaesarCryptanalyzer.Result[] all = analyzer.tryAllKeys(ciphertext);
        assertEquals(26, all.length);
    }

    @Test
    void frequencyTableFromCorpusSumsToApproximatelyHundred() {
        String corpus = "the quick brown fox jumps over the lazy dog";
        FrequencyTable table = FrequencyTable.fromCorpus(corpus, Alphabet.english());
        double sum = 0;
        for (double f : table.toArray()) sum += f;
        assertEquals(100.0, sum, 0.01);
    }
}
