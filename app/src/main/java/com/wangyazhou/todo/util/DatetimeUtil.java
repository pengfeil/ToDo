package com.wangyazhou.todo.util;

import java.util.Date;

public class DatetimeUtil {
    public static final long TWO_DAY_MILLI_SEC = 2 * 24 * 3600 * 1000;

    public static long getNowDatetime(){
        return new Date().getTime();
    }
}
