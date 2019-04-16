package com.example.admin.smartdiaper.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 日期转换 wen
 */
public class DateTimeUtil {
    private static final String TAG = "DateTimeUtil";

    private static long duration;

    //从2000年1月1日距离现在的时间，以秒为单位（配合主控时间格式）
    public static long currentTimeFrom2000InSeconds(){
        long currentTimeInSeconds;
        Calendar ca=Calendar.getInstance();
        ca.set(2000,1,1,0,0,0);
        currentTimeInSeconds=System.currentTimeMillis()/1000-ca.getTimeInMillis()/1000;
        return currentTimeInSeconds;
    }

    //把4字节的时间2000开始的时间，单位是s  转换为  8字节   1970 开始的时间，单位是mm
    public static long mcuTimeToAndroidTime(long mcuTime){
        long androidTime = 0;
        Calendar ca2000=Calendar.getInstance();
        ca2000.set(2000,1,1,0,0,0);
        Calendar ca1970=Calendar.getInstance();
        ca1970.set(1970,1,1,0,0,0);
        long diff = ca2000.getTimeInMillis() - ca1970.getTimeInMillis();
        androidTime = mcuTime * 1000  + ca2000.getTimeInMillis();
        Log.d(TAG, "mcuTimeToAndroidTime: diff = "+ diff);
        Log.d(TAG, "mcuTimeToAndroidTime: ca2000.getTimeInMillis() = " + ca2000.getTimeInMillis());
        Log.d(TAG, "mcuTimeToAndroidTime: ca1970.getTimeInMillis() = " + ca1970.getTimeInMillis());
        return androidTime;
    }
    /**
     * byte[]转Long
     */
    public static long bytes2Long(byte[] byteNum,int start,int end) {
        long num = 0;
        for (int ix = start; ix <= end; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }
    /**
     * 系统时间转换为年月日
     * @param timestamp
     * @return
     */

    public static String timestamp2y(long timestamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        return format.format(timestamp);
    }

    public static String timestamp2ymd(long timestamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(timestamp);
    }

    //只有这一个用到
    public static String toymdhms(long timestamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timestamp);
    }

    /**
     * 将long类型的时间转为01:22:38形式
     * @param duration1
     * @return
     */
    public static String formateDuration(long duration1) {
        DateTimeUtil.duration = duration1;
        //定义常量
        long HOUR = 1000*60*60;//1小时
        long MINUTE = 1000*60;//1分钟
        long SECOND = 1000;//1秒钟

        //1.先计算小时
        long hour = duration / HOUR;//得到多少小时
        //再拿算完小时后的余数去算分钟
        long remain = duration % HOUR;
        //2.计算分钟
        long minute = remain / MINUTE;//得到了多少分钟
        remain = remain%MINUTE;
        //3.计算秒
        long second = remain / SECOND;

        if(hour==0){
            //说明不足一个小时，那么就不要显示小时了
            return String.format("%02d:%02d",minute,second);
        }else {
            return String.format("%02d:%02d:%02d",hour,minute,second);
        }
    }
}
