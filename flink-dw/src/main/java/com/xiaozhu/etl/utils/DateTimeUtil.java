package com.xiaozhu.etl.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期转换的工具类
 */
public class DateTimeUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//    public static Long toTs(String dateStr){
//        Long ts = null;
//        try {
//            ts = sdf.parse(dateStr).getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return ts;
//    }

    // SimpleDateFormat是线程不安全的，在JDK1.8之后，使用DateTimeFormatter替换。
    // 将日期对象转换为字符串
    public static String toYMDHMS(Date date){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        String dateStr = dtf.format(localDateTime);
        return dateStr;
    }

    // 将字符串日期转换为毫秒数
    public static Long toTs(String dateStr){
        //Date == LocalDateTime   Calendar == Instant
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, dtf);
        long ts = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return ts;
    }

}

