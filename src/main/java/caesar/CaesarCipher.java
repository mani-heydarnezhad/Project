package caesar;

import util.Alphabet;

/**
 * پیاده‌سازی رمز سزار طبق مدل ریاضی بخش ۲.۱ گزارش:
 *   E(x) = (x + k) mod n
 *   D(y) = (y - k) mod n
 *
 * حروف غیرالفبایی (فاصله، علائم نگارشی، اعداد) بدون تغییر عبور داده می‌شوند
 * تا خوانایی متن حفظ شود؛ حالت حروف بزرگ/کوچک لاتین نیز حفظ می‌شود.
 */
public final class CaesarCipher {

    private final Alphabet alphabet;

    public CaesarCipher(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    /** فضای کلید |K| = n (بخش ۲.۲ گزارش) */
    public int keySpaceSize() {
        return alphabet.size();
    }

    public String encrypt(String plaintext, int key) {
        return shift(plaintext, key);
    }

    public String decrypt(String ciphertext, int key) {
        return shift(ciphertext, -key);
    }

    private String shift(String text, int key) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx < 0) {
                sb.append(c); // کاراکتر خارج از الفبا بدون تغییر
                continue;
            }
            int newIdx = Math.floorMod(idx + key, alphabet.size());
            boolean upper = Character.isUpperCase(c);
            sb.append(alphabet.charAt(newIdx, upper));
        }
        return sb.toString();
    }

    public Alphabet alphabet() {
        return alphabet;
    }
}
