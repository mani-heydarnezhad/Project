package caesar;

import util.Alphabet;
import util.FrequencyTable;
import util.KeySpaceCalculator;

import java.math.BigInteger;

/**
 * اجرای نمایشی ماژول رمز سزار: رمزگذاری، رمزگشایی، شکست با brute-force + chi^2،
 * و محاسبه‌ی فضای کلید. خروجی این کلاس مستقیماً در بخش ۷ گزارش (نتایج تجربی)
 * قابل استفاده است.
 */
public final class CaesarDemo {

    public static void main(String[] args) {
        Alphabet english = Alphabet.english();
        CaesarCipher cipher = new CaesarCipher(english);

        String plaintext = "DISCRETE MATHEMATICS IS THE FOUNDATION OF COMPUTER SCIENCE AND CRYPTOGRAPHY";
        int key = 7;

        String ciphertext = cipher.encrypt(plaintext, key);
        String decrypted = cipher.decrypt(ciphertext, key);

        System.out.println("=== رمزگذاری/رمزگشایی ساده ===");
        System.out.println("متن اصلی   : " + plaintext);
        System.out.println("کلید       : " + key);
        System.out.println("متن رمزشده : " + ciphertext);
        System.out.println("رمزگشایی   : " + decrypted);
        System.out.println("صحت رمزگشایی: " + decrypted.equals(plaintext));

        System.out.println();
        System.out.println("=== شکست خودکار با Brute-force + Chi-squared ===");
        FrequencyTable reference = FrequencyTable.englishReference();
        CaesarCryptanalyzer analyzer = new CaesarCryptanalyzer(cipher, reference);
        CaesarCryptanalyzer.Result best = analyzer.breakCipher(ciphertext);
        System.out.println("بهترین حدس: " + best);
        System.out.println("کلید واقعی بود: " + key + "  |  کلید یافت‌شده: " + best.key
                + "  |  موفقیت: " + (best.key == key));

        System.out.println();
        System.out.println("=== فضای کلید ===");
        BigInteger keySpace = KeySpaceCalculator.caesarKeySpace(english.size());
        System.out.println("|K| = " + keySpace);
        double seconds = KeySpaceCalculator.estimatedBruteForceSeconds(keySpace, 1_000_000_000.0);
        System.out.println("زمان تخمینی جستجوی فراگیر با 10^9 کلید/ثانیه: "
                + KeySpaceCalculator.humanReadableDuration(seconds));
    }
}
