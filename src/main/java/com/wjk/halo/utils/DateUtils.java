package com.wjk.halo.utils;

import com.sun.istack.NotNull;
import org.springframework.lang.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private DateUtils(){
    }

    @NonNull
    public static Date now(){
        return new Date();
    }


    public static Date add(@NonNull Date date, long time, @NonNull TimeUnit timeUnit){
        Date result;

        int timeIntValue;

        if (time > Integer.MAX_VALUE){
            timeIntValue = Integer.MAX_VALUE;
        }else {
            timeIntValue = Long.valueOf(time).intValue();
        }

        switch (timeUnit){
            case DAYS:
                result = org.apache.commons.lang3.time.DateUtils.addDays(date, timeIntValue);
                break;
            case HOURS:
                result = org.apache.commons.lang3.time.DateUtils.addHours(date, timeIntValue);
                break;
            case MINUTES:
                result = org.apache.commons.lang3.time.DateUtils.addMinutes(date, timeIntValue);
                break;
            case SECONDS:
                result = org.apache.commons.lang3.time.DateUtils.addSeconds(date, timeIntValue);
                break;
            case MILLISECONDS:
                result = org.apache.commons.lang3.time.DateUtils.addMilliseconds(date, timeIntValue);
                break;
            default:
                result = date;
        }
        return result;
    }

    @NonNull
    public static Calendar convertTo(@NonNull Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

}
