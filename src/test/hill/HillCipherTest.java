package hill;

import org.junit.jupiter.api.Test;
import util.Alphabet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های رمز هیل و حمله‌ی متن آشکار شناخته‌شده (KPA) — بخش امتیازی.
 */
class HillCipherTest {

    private static final Alphabet ALPHABET = Alphabet.english();
    private final HillCipher cipher = new HillCipher(ALPHABET, 2);

    // ماتریس کلید نمونه با det=9 که gcd(9,26)=1 است (معکوس‌پذیر در Z_26)
    private static final int[][] KEY = {{3, 3}, {2, 5}};

    @Test
    void encryptThenDecryptReturnsOriginalPaddedText() {
        String plaintext = "helloworld"; // طول ۱۰، زوج -> بدون padding اضافه
        String ciphertext = cipher.encrypt(plaintext, KEY);
        String decrypted = cipher.decrypt(ciphertext, KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void oddLengthTextIsPaddedWithX() {
        String plaintext = "abc"; // طول فرد -> باید یک 'x' اضافه شود
        String ciphertext = cipher.encrypt(plaintext, KEY);
        String decrypted = cipher.decrypt(ciphertext, KEY);
        assertEquals("abcx", decrypted);
    }

    @Test
    void nonInvertibleKeyThrowsException() {
        // det = 2*4 - 1*3 = 5 ; gcd(5,26)=1 در واقع معکوس‌پذیر است؛
        // برای تست خطا، ماتریسی با det زوج (مثلاً 13، که با 26 مشترک دارد) می‌سازیم
        int[][] singularKey = {{2, 4}, {1, 3}}; // det = 6-4=2 ; gcd(2,26)=2 != 1
        assertThrows(IllegalArgumentException.class, () -> cipher.encrypt("test", singularKey));
    }

    @Test
    void modularInverseIsCorrect() {
        // 3 * 9 = 27 = 1 (mod 26) => modInverse(3,26) = 9
        assertEquals(9, HillCipher.modInverse(3, 26));
    }

    @Test
    void knownPlaintextAttackRecoversKeyExactly() {
        String knownPlaintext = "hillcipherde"; // 6 بلوک، X (اولین بلوک) معکوس‌پذیر است
        String knownCiphertext = cipher.encrypt(knownPlaintext, KEY);

        HillKnownPlaintextAttack kpa = new HillKnownPlaintextAttack(ALPHABET, 2);
        int[][][] matrices = kpa.extractBlockMatrices(knownPlaintext, knownCiphertext);
        int[][] recoveredKey = kpa.recoverKey(matrices[0], matrices[1]);

        assertArrayEquals(KEY[0], recoveredKey[0]);
        assertArrayEquals(KEY[1], recoveredKey[1]);
    }

    @Test
    void recoveredKeyDecryptsNewCiphertextCorrectly() {
        String knownPlaintext = "hillcipherde";
        String knownCiphertext = cipher.encrypt(knownPlaintext, KEY);

        HillKnownPlaintextAttack kpa = new HillKnownPlaintextAttack(ALPHABET, 2);
        int[][][] matrices = kpa.extractBlockMatrices(knownPlaintext, knownCiphertext);
        int[][] recoveredKey = kpa.recoverKey(matrices[0], matrices[1]);

        // با کلید بازیابی‌شده، یک پیام *جدید* (که مهاجم قبلاً ندیده) هم باید درست رمزگشایی شود
        String newPlaintext = "secretxx";
        String newCiphertext = cipher.encrypt(newPlaintext, KEY);
        String decryptedWithRecoveredKey = cipher.decrypt(newCiphertext, recoveredKey);

        assertEquals(newPlaintext, decryptedWithRecoveredKey,
                "نشان می‌دهد KPA کل امنیت رمز هیل را برای پیام‌های آینده نیز از بین می‌برد");
    }
}
