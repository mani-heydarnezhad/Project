package hill;

import util.Alphabet;

/**
 * رمز هیل (Hill Cipher) — بخش امتیازی (بخش ۵ گزارش).
 * مبتنی بر جبر خطی روی حلقه‌ی پیمانه‌ای Z_n.
 *
 * مدل ریاضی:
 *   y = K·x (mod n)      [رمزگذاری بلوکی با ماتریس کلید K اندازه m×m]
 *   x = K⁻¹·y (mod n)    [رمزگشایی]
 *
 * برای وجود K⁻¹ در Z_n لازم و کافی است gcd(det(K), n) = 1 باشد.
 */
public final class HillCipher {

    private final Alphabet alphabet;
    private final int blockSize; // m

    public HillCipher(Alphabet alphabet, int blockSize) {
        this.alphabet = alphabet;
        this.blockSize = blockSize;
    }

    public int blockSize() {
        return blockSize;
    }

    /** رمزگذاری متن با ماتریس کلید K (اندازه m×m)؛ متن با پرکردن (padding) به مضرب m می‌رسد. */
    public String encrypt(String plaintext, int[][] key) {
        validateKeyInvertible(key);
        int[] vector = textToVector(plaintext, true);
        int[] result = new int[vector.length];
        int n = alphabet.size();

        for (int block = 0; block < vector.length; block += blockSize) {
            for (int row = 0; row < blockSize; row++) {
                long sum = 0;
                for (int col = 0; col < blockSize; col++) {
                    sum += (long) key[row][col] * vector[block + col];
                }
                result[block + row] = (int) Math.floorMod(sum, n);
            }
        }
        return vectorToText(result);
    }

    public String decrypt(String ciphertext, int[][] key) {
        int[][] inverseKey = invertMatrixModN(key, alphabet.size());
        int[] vector = textToVector(ciphertext, false);
        int[] result = new int[vector.length];
        int n = alphabet.size();

        for (int block = 0; block < vector.length; block += blockSize) {
            for (int row = 0; row < blockSize; row++) {
                long sum = 0;
                for (int col = 0; col < blockSize; col++) {
                    sum += (long) inverseKey[row][col] * vector[block + col];
                }
                result[block + row] = (int) Math.floorMod(sum, n);
            }
        }
        return vectorToText(result);
    }

    private int[] textToVector(String text, boolean pad) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int idx = alphabet.indexOf(text.charAt(i));
            if (idx >= 0) list.add(idx);
        }
        if (pad) {
            while (list.size() % blockSize != 0) {
                list.add(alphabet.indexOf('x') >= 0 ? alphabet.indexOf('x') : 0); // padding استاندارد با 'x'
            }
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    private String vectorToText(int[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int idx : vector) sb.append(alphabet.charAt(idx, false));
        return sb.toString();
    }

    public void validateKeyInvertible(int[][] key) {
        int det = determinantModN(key, alphabet.size());
        int g = gcd(Math.floorMod(det, alphabet.size()), alphabet.size());
        if (g != 1) {
            throw new IllegalArgumentException(
                    "ماتریس کلید معکوس‌پذیر نیست: gcd(det=" + det + ", n=" + alphabet.size() + ") = " + g);
        }
    }

    // ---- عملیات جبر خطی روی Z_n ----

    public static int determinantModN(int[][] matrix, int n) {
        int size = matrix.length;
        if (size == 1) return Math.floorMod(matrix[0][0], n);
        if (size == 2) {
            return Math.floorMod(matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0], n);
        }
        // بسط لاپلاس برای اندازه‌های بزرگ‌تر (کافی برای بلوک‌های کوچک آموزشی)
        int det = 0;
        for (int col = 0; col < size; col++) {
            int minorDet = determinantModN(minor(matrix, 0, col), n);
            int sign = (col % 2 == 0) ? 1 : -1;
            det = Math.floorMod(det + sign * matrix[0][col] * minorDet, n);
        }
        return det;
    }

    private static int[][] minor(int[][] matrix, int skipRow, int skipCol) {
        int size = matrix.length;
        int[][] result = new int[size - 1][size - 1];
        int ri = 0;
        for (int r = 0; r < size; r++) {
            if (r == skipRow) continue;
            int ci = 0;
            for (int c = 0; c < size; c++) {
                if (c == skipCol) continue;
                result[ri][ci] = matrix[r][c];
                ci++;
            }
            ri++;
        }
        return result;
    }

    /** معکوس ضرب‌پذیر a در Z_n با الگوریتم اقلیدسی توسعه‌یافته */
    public static int modInverse(int a, int n) {
        a = Math.floorMod(a, n);
        int[] r = extendedGCD(a, n);
        if (r[0] != 1) {
            throw new IllegalArgumentException("معکوس ضرب‌پذیر وجود ندارد: gcd(" + a + "," + n + ")=" + r[0]);
        }
        return Math.floorMod(r[1], n);
    }

    /** بازگرداندن [gcd, x, y] به‌طوری‌که a*x + b*y = gcd(a,b) */
    private static int[] extendedGCD(int a, int b) {
        if (b == 0) return new int[]{a, 1, 0};
        int[] r = extendedGCD(b, a % b);
        int g = r[0], x1 = r[1], y1 = r[2];
        return new int[]{g, y1, x1 - (a / b) * y1};
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    /** معکوس ماتریس K در Z_n: K⁻¹ = det(K)⁻¹ · adj(K) (mod n) */
    public static int[][] invertMatrixModN(int[][] key, int n) {
        int size = key.length;
        int det = determinantModN(key, n);
        int detInv = modInverse(det, n);

        int[][] adjugate = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int minorDet = determinantModN(minor(key, r, c), n);
                int sign = ((r + c) % 2 == 0) ? 1 : -1;
                int cofactor = Math.floorMod(sign * minorDet, n);
                adjugate[c][r] = cofactor; // ترانهاده برای adjugate
            }
        }

        int[][] inverse = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                inverse[r][c] = Math.floorMod(detInv * adjugate[r][c], n);
            }
        }
        return inverse;
    }
}
