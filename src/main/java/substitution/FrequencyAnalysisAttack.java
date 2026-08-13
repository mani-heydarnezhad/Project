package substitution;

import util.Alphabet;
import util.FrequencyTable;

import java.util.*;


public final class FrequencyAnalysisAttack {

    private final Alphabet alphabet;
    private final FrequencyTable reference;

    public FrequencyAnalysisAttack(Alphabet alphabet, FrequencyTable reference) {
        this.alphabet = alphabet;
        this.reference = reference;
    }

    
    //تولید حدس اولیه‌ی جایگشت رمزگشایی (decryption key) بر اساس تطبیق رتبه‌ی
    //فراوانی. خروجی: آرایه‌ی key به‌طوری‌که plaintextIndex = key[ciphertextIndex].
     
    public int[] initialGuess(String ciphertext) {
        int n = alphabet.size();
        long[] counts = new long[n];
        for (int i = 0; i < ciphertext.length(); i++) {
            int idx = alphabet.indexOf(ciphertext.charAt(i));
            if (idx >= 0) counts[idx]++;
        }

        Integer[] cipherOrder = sortedIndicesDescending(counts);
        Double[] refFreq = new Double[n];
        for (int i = 0; i < n; i++) refFreq[i] = reference.frequencyOf(i);
        Integer[] refOrder = sortedIndicesDescendingD(refFreq);

        int[] key = new int[n]; // key[cipherLetterIdx] = plaintextLetterIdx
        for (int rank = 0; rank < n; rank++) {
            key[cipherOrder[rank]] = refOrder[rank];
        }
        return key;
    }

    private Integer[] sortedIndicesDescending(long[] values) {
        Integer[] idx = new Integer[values.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Long.compare(values[b], values[a]));
        return idx;
    }

    private Integer[] sortedIndicesDescendingD(Double[] values) {
        Integer[] idx = new Integer[values.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare(values[b], values[a]));
        return idx;
    }
}
