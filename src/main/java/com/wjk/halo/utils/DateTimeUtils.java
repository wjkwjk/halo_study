package com.wjk.halo.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    /**
     * 上海时区格式
     */
    public static final String CTT = ZoneId.SHORT_IDS.get("CTT");

    /**
     * 上海时区
     */
    public static final ZoneId CTT_ZONE_ID = ZoneId.of(CTT);

    private DateTimeUtils(){}

    public static Instant toInstant(LocalDateTime localDateTime){
        return toInstant(localDateTime, CTT_ZONE_ID);
    }

    public static Instant toInstant(LocalDateTime localDateTime, ZoneId zoneId){
        return localDateTime.atZone(zoneId).toInstant();
    }

    public static long toEpochMilli(LocalDateTime localDateTime){
        return toInstant(localDateTime).toEpochMilli();
    }

}
