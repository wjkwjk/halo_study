package com.wjk.halo.utils;

import com.wjk.halo.exception.BadRequestException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class TwoFactorAuthUtils {

    private final static int VALID_TFA_WINDOW_MILLIS = 60000;

    /**
     *
     * @param tfaKey    用户信息中的MFAKey
     * @param tfaCode   用户输入的验证码
     */
    public static void validateTFACode(String tfaKey, String tfaCode){
        try {
            int validCode = Integer.parseInt(tfaCode);
            boolean result = TimeBasedOneTimePasswordUtil.validateCurrentNumber(tfaKey, validCode, VALID_TFA_WINDOW_MILLIS);
            if (!result)    throw new BadRequestException("两步验证码验证错误，请确认时间是否同步");
        } catch (NumberFormatException e) {
            throw new BadRequestException("两步验证码请输入数字");
        } catch (GeneralSecurityException e){
            throw new BadRequestException("两步验证码验证异常");
        }
    }

}


class TimeBasedOneTimePasswordUtil{

    public static final int DEFAULT_TIME_STEP_SECONDS = 30;

    public static boolean validateCurrentNumber(String base32Secret, int authNumber, int windowMillis) throws GeneralSecurityException {
        return validateCurrentNumber(base32Secret, authNumber, windowMillis, System.currentTimeMillis(),DEFAULT_TIME_STEP_SECONDS);
    }

    public static boolean validateCurrentNumber(String base32Secret, int authNumber, int windowMillis, long timeMillis, int timeStepSeconds) throws GeneralSecurityException {
        long fromTimeMillis = timeMillis;
        long toTimeMillis = timeMillis;
        if (windowMillis > 0){
            fromTimeMillis -= windowMillis;
            toTimeMillis += windowMillis;
        }
        long timeStepMillis = timeStepSeconds * 1000;
        for (long millis=fromTimeMillis; millis<=toTimeMillis;millis+=timeStepMillis){
            int generatedNumber = generateNumber(base32Secret, millis, timeStepSeconds);
            if (generatedNumber == authNumber){
                return true;
            }
        }
        return false;
    }

    public static int generateNumber(String base32Secret, long timeMillis, int timeStepSeconds) throws GeneralSecurityException {
        byte[] key = decodeBase32(base32Secret);
        byte[] data = new byte[8];
        long value = timeMillis / 1000 / timeStepSeconds;
        for (int i=7;value>0;i--){
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);

        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;

        long truncatedHash = 0;
        for (int i=offset;i<offset+4;++i){
            truncatedHash<<=8;
            truncatedHash |= hash[i] & 0xFF;
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash /= 1000000;
        return (int) truncatedHash;
    }


    static byte[] decodeBase32(String str){
        int numBytes = ((str.length() * 5) + 7) / 8;
        byte[] result = new byte[numBytes];
        int resultIndex = 0;
        int which = 0;
        int working = 0;
        for (int i=0;i<str.length();i++){
            char ch = str.charAt(i);
            int val;
            if (ch >= 'a' && ch <= 'z') {
                val = ch - 'a';
            } else if (ch >= 'A' && ch <= 'Z') {
                val = ch - 'A';
            } else if (ch >= '2' && ch <= '7') {
                val = 26 + (ch - '2');
            } else if (ch == '=') {
                // special case
                which = 0;
                break;
            } else {
                throw new IllegalArgumentException("Invalid base-32 character: " + ch);
            }
            switch (which) {
                case 0:
                    // all 5 bits is top 5 bits
                    working = (val & 0x1F) << 3;
                    which = 1;
                    break;
                case 1:
                    // top 3 bits is lower 3 bits
                    working |= (val & 0x1C) >> 2;
                    result[resultIndex++] = (byte) working;
                    // lower 2 bits is upper 2 bits
                    working = (val & 0x03) << 6;
                    which = 2;
                    break;
                case 2:
                    // all 5 bits is mid 5 bits
                    working |= (val & 0x1F) << 1;
                    which = 3;
                    break;
                case 3:
                    // top 1 bit is lowest 1 bit
                    working |= (val & 0x10) >> 4;
                    result[resultIndex++] = (byte) working;
                    // lower 4 bits is top 4 bits
                    working = (val & 0x0F) << 4;
                    which = 4;
                    break;
                case 4:
                    // top 4 bits is lowest 4 bits
                    working |= (val & 0x1E) >> 1;
                    result[resultIndex++] = (byte) working;
                    // lower 1 bit is top 1 bit
                    working = (val & 0x01) << 7;
                    which = 5;
                    break;
                case 5:
                    // all 5 bits is mid 5 bits
                    working |= (val & 0x1F) << 2;
                    which = 6;
                    break;
                case 6:
                    // top 2 bits is lowest 2 bits
                    working |= (val & 0x18) >> 3;
                    result[resultIndex++] = (byte) working;
                    // lower 3 bits of byte 6 is top 3 bits
                    working = (val & 0x07) << 5;
                    which = 7;
                    break;
                case 7:
                    // all 5 bits is lower 5 bits
                    working |= val & 0x1F;
                    result[resultIndex++] = (byte) working;
                    which = 0;
                    break;
            }
        }
        if (which!=0){
            result[resultIndex++] = (byte) working;
        }
        if (resultIndex!=result.length){
            result = Arrays.copyOf(result, resultIndex);
        }
        return result;

    }



}
