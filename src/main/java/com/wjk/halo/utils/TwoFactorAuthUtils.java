package com.wjk.halo.utils;

public class TwoFactorAuthUtils {

    public static void validateTFACode(String tfaKey, String tfaCode){
        try {
            int validCode = Integer.parseInt(tfaCode);
            boolean result = TimeBasedO
        }
    }

}


class TimeBasedOneTimePasswordUtil{
    public static boolean validateCurrentNumber(String base32Secret, int authNumber, int windowMillis){
        return validateCurrentNumber()
    }

    public static boolean validateCurrentNumber(String base32Secret, int authNumber, int windowMillis, long timeMillis, int timeStepSeconds){
        long fromTimeMillis = timeMillis;
        long toTimeMillis = timeMillis;
        if (windowMillis > 0){
            fromTimeMillis -= windowMillis;
            toTimeMillis += windowMillis;
        }
        long timeStepMillis = timeStepSeconds * 1000;
        for (long millis=fromTimeMillis; millis<=toTimeMillis;millis+=timeStepMillis){
            int generatedNumber =
        }
    }

    public static int generateNumber(String base32Secret, long timeMillis, int timeStepSeconds){
        byte[] key =
    }

    static byte[] decodeBase32(String str){
        int numBytes = ((str.length() * 5) + 7) / 8;
        byte[] result = new byte[numBytes];
        int resultIndex = 0;
        int which = 0;
        int working = 0;
    }

}
