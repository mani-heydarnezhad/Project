package substitution;

import util.Alphabet;

/**
 * رمز جانشینی تک‌حرفی (Monoalphabetic Substitution Cipher).
 * مدل ریاضی (بخش ۳.۱ گزارش):
 *   E(x) = π(x) ,  D(y) = π⁻¹(y) ,  π ∈ S_n
 * که π یک جایگشت دلخواه روی الفبا (کلید) است.
 *
 * کلید در این پیاده‌سازی با یک آرایه‌ی permutation از طول n نمایش داده می‌شود:
 * key[i] = اندیس حرفی که حرف i به آن نگاشت می‌شود.
 */
public final class SubstitutionCipher {

    private final Alphabet alphabet;

    public SubstitutionCipher(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    /** بررسی معتبر بودن کلید: باید یک جایگشت کامل از {0,...,n-1} باشد */
    public void validateKey(int[] key) {
        int n = alphabet.size();
        if (key.length != n) {
            throw new IllegalArgumentException("طول کلید باید برابر اندازه الفبا (" + n + ") باشد.");
        }
        boolean[] seen = new boolean[n];
        for (int v : key) {
            if (v < 0 || v >= n || seen[v]) {
                throw new IllegalArgumentException("کلید یک جایگشت معتبر نیست (مقدار تکراری یا خارج از بازه).");
            }
            seen[v] = true;
        }
    }

    public String encrypt(String plaintext, int[] key) {
        validateKey(key);
        return applyPermutation(plaintext, key);
    }

    public String decrypt(String ciphertext, int[] key) {
        validateKey(key);
        int[] inverse = invert(key);
        return applyPermutation(ciphertext, inverse);
    }

    private String applyPermutation(String text, int[] permutation) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx < 0) {
                sb.append(c);
                continue;
            }
            int newIdx = permutation[idx];
            boolean upper = Character.isUpperCase(c);
            sb.append(alphabet.charAt(newIdx, upper));
        }
        return sb.toString();
    }

    /** محاسبه‌ی جایگشت معکوس π⁻¹ برای رمزگشایی */
    public static int[] invert(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            inverse[permutation[i]] = i;
        }
        return inverse;
    }

    /** تولید کلید تصادفی (جایگشت تصادفی) — برای تست و شبیه‌سازی */
    public static int[] randomKey(int n, java.util.Random random) {
        int[] key = new int[n];
        for (int i = 0; i < n; i++) key[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
        }
        return key;
    }

    public Alphabet alphabet() {
        return alphabet;
    }
}
