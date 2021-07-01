package com.wjk.halo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@Slf4j
public class HaloUtils {

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

}
