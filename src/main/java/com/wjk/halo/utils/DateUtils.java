package com.wjk.halo.utils;

import com.sun.istack.NotNull;

import java.util.Date;

public class DateUtils {
    private DateUtils(){
    }

    @NotNull
    public static Date now(){
        return new Date();
    }

}
