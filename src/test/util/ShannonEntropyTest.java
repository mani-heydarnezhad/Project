package util;

import org.junit.jupiter.api.Test;
import vigenere.VigenereCipher;
import caesar.CaesarCipher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست آنتروپی شانون: بررسی ادعاهای نظری بخش ۶ گزارش اصلی.
 */
class ShannonEntropyTest {

    private static final Alphabet ALPHABET = Alphabet.english();
    private static final String TEXT =
            "the quick brown fox jumps over the lazy dog and discrete mathematics provides "
            + "the foundation for modern cryptography and computer science theory";

    @Test
    void caesarCipherPreservesEntropyExactly() {
        double hPlain = ShannonEntropy.compute(TEXT, ALPHABET);
        CaesarCipher caesar = new CaesarCipher(ALPHABET);
        String encrypted = caesar.encrypt(TEXT, 7);
        double hEncrypted = ShannonEntropy.compute(encrypted, ALPHABET);

        assertEquals(hPlain, hEncrypted, 1e-9,
                "رمز سزار فقط جایگشت است؛ توزیع فراوانی و در نتیجه آنتروپی باید دقیقاً حفظ شود");
    }

    @Test
    void vigenereWithLongKeyIncreasesEntropyTowardMaximum() {
        double hPlain = ShannonEntropy.compute(TEXT, ALPHABET);
        VigenereCipher vigenere = new VigenereCipher(ALPHABET);
        String encrypted = vigenere.encrypt(TEXT, "qwertyuiopasdfghjklzxcvbnm"); // کلید به طول الفبا
        double hEncrypted = ShannonEntropy.compute(encrypted, ALPHABET);

        assertTrue(hEncrypted > hPlain,
                "ویژنر با کلید بلند باید توزیع را یکنواخت‌تر و آنتروپی را بالاتر ببرد");
        assertTrue(hEncrypted <= ShannonEntropy.maxEntropy(26) + 1e-9,
                "آنتروپی هرگز نباید از سقف نظری log2(n) بیشتر شود");
    }

    @Test
    void maxEntropyMatchesLog2OfAlphabetSize() {
        assertEquals(Math.log(26) / Math.log(2), ShannonEntropy.maxEntropy(26), 1e-9);
    }

    @Test
    void naturalEnglishTextHasEntropyBelowMaximum() {
        double h = ShannonEntropy.compute(TEXT, ALPHABET);
        double hMax = ShannonEntropy.maxEntropy(26);
        assertTrue(h < hMax, "افزونگی زبان طبیعی باید آنتروپی را زیر سقف نظری نگه دارد");
        assertTrue(h > 3.5 && h < 4.7, "آنتروپی متن انگلیسی طبیعی معمولاً در بازه ۴.۰-۴.۲ بیت است");
    }
}
