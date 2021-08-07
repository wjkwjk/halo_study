package com.wjk.halo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.UUID;

import static com.wjk.halo.model.support.HaloConst.FILE_SEPARATOR;

@Slf4j
public class HaloUtils {

    private static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    @NonNull
    public static String pluralize(long times, @NonNull String label, @NonNull String pluralLabel){
        if (times<=0){
            return "no " + pluralLabel;
        }

        if (times == 1){
            return times + " " + label;
        }

        return times + " " + pluralLabel;

    }

    @NonNull
    public static String timeFormat(long totalSeconds){
        if (totalSeconds <= 0){
            return "0 second";
        }

        StringBuilder timeBuilder = new StringBuilder();

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 3600 % 60;

        if (hours > 0){
            if (StringUtils.isNotBlank(timeBuilder)){
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(hours, "hour", "hours"));
        }

        if (minutes > 0){
            if (StringUtils.isNotBlank(timeBuilder)){
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(minutes, "minute", "minutes"));
        }

        if (seconds > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(seconds, "second", "seconds"));
        }

        return timeBuilder.toString();

    }

    //生成唯一识别码作为AccessToken以及RefreshToken
    @NonNull
    public static String randomUUIDWithoutDash(){
        return StringUtils.remove(UUID.randomUUID().toString(),'-');
    }

    //删除html语言的标签
    public static String cleanHtmlTag(String content){
        if (StringUtils.isEmpty(content)){
            return StringUtils.EMPTY;
        }
        return content.replaceAll(RE_HTML_MARK, StringUtils.EMPTY);
    }

    @NonNull
    public static String ensureSuffix(@NonNull String string, @NonNull String suffix){
        return StringUtils.removeEnd(string, suffix) + suffix;
    }

    public static String desensitize(@NonNull String plainText, int leftSize, int rightSize){
        if (leftSize < 0){
            leftSize = 0;
        }
        if (leftSize > plainText.length()){
            leftSize = plainText.length();
        }
        if (rightSize < 0){
            rightSize = 0;
        }
        if (rightSize > plainText.length()){
            rightSize = plainText.length();
        }
        if (plainText.length() < leftSize + rightSize){
            rightSize = plainText.length() - leftSize;
        }
        int remainSize = plainText.length() - rightSize - leftSize;

        String left = StringUtils.left(plainText, leftSize);
        String right = StringUtils.right(plainText, rightSize);
        return StringUtils.rightPad(left, remainSize + leftSize, '*') + right;
    }

    public static String changeFileSeparatorToUrlSeparator(@NonNull String pathname){
        return pathname.replace(FILE_SEPARATOR, "/");
    }

    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String bothfix){
        return ensureBoth(string, bothfix, bothfix);
    }

    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String prefix, @NonNull String suffix){
        return ensureSuffix(ensurePrefix(string, prefix), suffix);
    }

    @NonNull
    public static String ensurePrefix(@NonNull String string, @NonNull String prefix){
        return prefix + StringUtils.removeStart(string, prefix);
    }

}
