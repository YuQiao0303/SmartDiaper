package com.example.admin.smartdiaper.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
     * 将long类型的time转换为“刚刚”，“2分钟前”，“昨天”等描述的字符串
     * @param time long 类型，从1970年1.1的0点，到某一时刻的毫秒数
     * @return
     */
    public static String time2ShowString(long time){
        String str = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String timestr = format.format(time);

        long currentTime = System.currentTimeMillis();
        //目标时间
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(time);
        //现在时间
        Calendar nowCalendar=Calendar.getInstance();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());
        //今天
        Date dateToday = new Date();
        Calendar today = Calendar.getInstance();
        today.setTime(dateToday);
        //昨天
        Calendar yesterday  =Calendar.getInstance();
        yesterday.setTime(dateToday);
        yesterday.add(Calendar.DATE,-1);
        //前天
        Calendar oneDayAgo  =Calendar.getInstance();
        oneDayAgo.setTime(dateToday);
        yesterday.add(Calendar.DATE,-2);
        //明天
        Calendar tomorrow  =Calendar.getInstance();
        oneDayAgo.setTime(dateToday);
        yesterday.add(Calendar.DATE,1);
        //后天
        Calendar oneDayLater  =Calendar.getInstance();
        oneDayAgo.setTime(dateToday);
        yesterday.add(Calendar.DATE,2);

        if(targetCalendar.get(Calendar.YEAR) != nowCalendar.get(Calendar.YEAR) )
        {
            //不是同一年，直接显示
            return time2ShowString(time);
        }
        if(targetCalendar.get(Calendar.DATE) == today.get(Calendar.DATE))
        {
            return "今天 " + timestr;
        }
        else if(targetCalendar.get(Calendar.DATE) == yesterday.get(Calendar.DATE))
        {
            return "昨天 " + timestr;
        }
        else if(targetCalendar.get(Calendar.DATE) == oneDayAgo.get(Calendar.DATE))
        {
            return "前天 " + timestr;
        }
        else if(targetCalendar.get(Calendar.DATE) == tomorrow.get(Calendar.DATE))
        {
            return "明天 " + timestr;
        }
        else if(targetCalendar.get(Calendar.DATE) == oneDayLater.get(Calendar.DATE))
        {
            return "后天 " + timestr;
        }



//
//        if(targetCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) ){
//            //今天
//            str +="今天 " + timestr;
//            return str;
//        }
//        //是今年但不是今天
//        //判断是否是昨天
//        nowCalendar.add(Calendar.DATE,-1);
//        if(targetCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) ){
//            //昨天
//            str +="昨天 "+ timestr;
//            return str;
//        }
//        //是今年但不是今天也不是昨天
//        //判断是否是前天
//        nowCalendar.add(Calendar.DATE,-1);
//        if(targetCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) ){
//            //前天
//            str +="前天 "+ timestr;
//            return str;
//        }
//        //是今年，但不是今天、昨天、前天
//
//        Calendar yesterdayCalendar = Calendar.getInstance();
//        yesterdayCalendar.setTimeInMillis(System.currentTimeMillis());
//        yesterdayCalendar.add(Calendar.DATE,-1);



//
//        long difference = (currentTime - time)/1000; //time到现在的秒数
//        if(difference < 60 )   //1分钟以内显示为：“刚刚”
//        {
//            str = "刚刚";
//        }
//        else if (difference<3600)  //一小时以内显示为：“x分钟前”
//        {
//            str = difference/60 + "分钟前";
//        }
//        else if(difference <3600 * 24)  //一天之内，显示为“x小时x分钟前”
//        {
//            long  differenceInminutes = difference/60;
//            long minutes = differenceInminutes % 60;
//            long hours = differenceInminutes /60;
//            str = hours + "小时" + minutes +"分钟";
//        }
//        else if(difference < 3600 * 48) //昨天
//        {
//
//        }
        return str;
    }

//    /**
//     * 如果是今天，昨，前天，返回今天昨天前天；否则返回yyyy-mm-dd
//     * @param time
//     * @return
//     */
//    public static String getDate(long time){
//        String str = "";
//        long currentTime = System.currentTimeMillis();
//
//        Calendar nowCalendar=Calendar.getInstance();
//        nowCalendar.setTimeInMillis(currentTime);
//
//        long difference = (currentTime - time)/1000; //time到现在的秒数
//        if(difference < 60 )   //1分钟以内显示为：“刚刚”
//        {
//            str = "刚刚";
//        }
//        else if (difference<3600)  //一小时以内显示为：“x分钟前”
//        {
//            str = difference/60 + "分钟前";
//        }
//        else if(difference <3600 * 24)  //一天之内，显示为“x小时x分钟前”
//        {
//            long  differenceInminutes = difference/60;
//            long minutes = differenceInminutes % 60;
//            long hours = differenceInminutes /60;
//            str = hours + "小时" + minutes +"分钟";
//        }
//        else if(difference < 3600 * 48) //昨天
//        {
//
//        }
//        return str;
//    }

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
    public static String timestamp2whole(long timestamp){
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
