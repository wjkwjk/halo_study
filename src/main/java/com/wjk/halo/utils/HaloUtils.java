package com.wjk.halo.utils;

import cn.hutool.core.util.URLUtil;
import com.wjk.halo.model.support.HaloConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.UUID;

import static com.wjk.halo.model.support.HaloConst.FILE_SEPARATOR;

@Slf4j
public class HaloUtils {

    public static final String URL_SEPARATOR = "/";
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

    /**
     * 给字符串加后缀，若字符串已经有了该后缀，则删除掉该后缀再加一个一样的
     */
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

    /**
     * 往字符串string两端都加上字符串bothfix
     *  分别加前缀和后缀
     * @return
     */
    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String bothfix){
        return ensureBoth(string, bothfix, bothfix);
    }

    /**
     * 往字符串两端加前后缀
     *  先加前缀，再加后缀
     */
    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String prefix, @NonNull String suffix){
        return ensureSuffix(ensurePrefix(string, prefix), suffix);
    }

    /**
     * 给字符串加前缀，若字符串已经有了该前缀，则删除掉该前缀再加一个一样的
     */
    @NonNull
    public static String ensurePrefix(@NonNull String string, @NonNull String prefix){
        return prefix + StringUtils.removeStart(string, prefix);
    }

    @NonNull
    public static String normalizeUrl(@NonNull String originalUrl){
        if (StringUtils.startsWithAny(originalUrl, URL_SEPARATOR, HaloConst.PROTOCOL_HTTPS, HaloConst.PROTOCOL_HTTP)
        && !StringUtils.startsWith(originalUrl, "//")){
            return originalUrl;
        }
        return URLUtil.normalize(originalUrl);
    }

}
