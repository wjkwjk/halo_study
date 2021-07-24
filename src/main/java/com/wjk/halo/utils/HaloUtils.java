package com.wjk.halo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.UUID;

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

}
