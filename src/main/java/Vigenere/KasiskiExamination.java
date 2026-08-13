package vigenere;

import util.Alphabet;

import java.util.*;


public final class KasiskiExamination {

    private final Alphabet alphabet;
    private final int minSequenceLength;

    public KasiskiExamination(Alphabet alphabet, int minSequenceLength) {
        this.alphabet = alphabet;
        this.minSequenceLength = Math.max(3, minSequenceLength);
    }

    public static final class Distances {
        public final Map<String, List<Integer>> repeatedSequencePositions;
        public final List<Integer> distances;

        Distances(Map<String, List<Integer>> positions, List<Integer> distances) {
            this.repeatedSequencePositions = positions;
            this.distances = distances;
        }
    }

   
    public Distances findRepeatedSequenceDistances(String ciphertext) {
        
        StringBuilder cleanedBuilder = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            if (alphabet.indexOf(ciphertext.charAt(i)) >= 0) {
                cleanedBuilder.append(Character.toLowerCase(ciphertext.charAt(i)));
            }
        }
        String cleaned = cleanedBuilder.toString();

        Map<String, List<Integer>> positions = new LinkedHashMap<>();
        for (int i = 0; i + minSequenceLength <= cleaned.length(); i++) {
            String seq = cleaned.substring(i, i + minSequenceLength);
            positions.computeIfAbsent(seq, s -> new ArrayList<>()).add(i);
        }

        List<Integer> distances = new ArrayList<>();
        Iterator<Map.Entry<String, List<Integer>>> it = positions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Integer>> entry = it.next();
            List<Integer> pos = entry.getValue();
            if (pos.size() < 2) {
                it.remove(); 
                continue;
            }
            for (int i = 1; i < pos.size(); i++) {
                distances.add(pos.get(i) - pos.get(i - 1));
            }
        }
        return new Distances(positions, distances);
    }

   
    public List<Integer> estimateKeyLengthCandidates(List<Integer> distances, int maxKeyLength) {
        int[] votes = new int[maxKeyLength + 1];
        for (int d : distances) {
            for (int L = 2; L <= maxKeyLength; L++) {
                if (d % L == 0) votes[L]++;
            }
        }
        List<Integer> candidates = new ArrayList<>();
        for (int L = 2; L <= maxKeyLength; L++) candidates.add(L);
        candidates.sort((a, b) -> Integer.compare(votes[b], votes[a]));
        return candidates;
    }

    public static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static int gcdOfList(List<Integer> numbers) {
        int result = 0;
        for (int num : numbers) result = gcd(result, num);
        return result;
    }
}
