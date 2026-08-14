package util;

import java.math.BigInteger;


public final class KeySpaceCalculator {

    private KeySpaceCalculator() {
    }

    public static BigInteger caesarKeySpace(int n) {
        return BigInteger.valueOf(n);
    }

    public static BigInteger substitutionKeySpace(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public static BigInteger vigenereKeySpace(int n, int keyLength) {
        return BigInteger.valueOf(n).pow(keyLength);
    }

    
    public static double estimatedBruteForceSeconds(BigInteger keySpaceSize, double keysPerSecond) {
        return keySpaceSize.doubleValue() / keysPerSecond;
    }

    
    public static String humanReadableDuration(double seconds) {
        double years = seconds / (365.25 * 24 * 3600);
        if (years >= 1) return String.format("%.3e year", years);
        double days = seconds / (24 * 3600);
        if (days >= 1) return String.format("%.2f day", days);
        double hours = seconds / 3600;
        if (hours >= 1) return String.format("%.2f hour", hours);
        double minutes = seconds / 60;
        if (minutes >= 1) return String.format("%.2f minut", minutes);
        return String.format("%.2f second", seconds);
    }
}
