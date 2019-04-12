package com.example.admin.smartdiaper.remind;


import android.app.Service;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;

import android.util.Log;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;

public class Reminder {
    private static final String TAG = "Reminder";
    //铃声音频相关
    private static SoundPool soundPool;
    private static int soundIdInPool[] = {0, 0, 0};  //sound[0] 是test_music0 加载后在sound pool中的id
    private static int playingIdInPool;  //正在播放的音频在音频池中的id
    private static boolean[] loadFinished = {false, false, false};  //loadFinished[0]是test_music0.mp3是否加载完毕
    private static boolean playSuccess = false;
    private static float volumeF;




    /**
     * 手机震动
     */
    public static void vibrate() {
        //创建震动服务对象
        Vibrator mVibrator;
        //获取手机震动服务
        mVibrator = (Vibrator) MyApplication.getContext().getSystemService(Service.VIBRATOR_SERVICE);
        long[] patter = {0, 500, 500, 500, 500, 500};  //静止xxms，震动xxms，静止xxms，震动xxms
        mVibrator.vibrate(patter, -1);  //循环，表示从数组的哪个下标开始循环，如果是-1表示不循环

        //停止震动
        //mVibrator.cancel();
    }

    public static void initSoundPool() {
        //实例化soundPool
        if (Build.VERSION.SDK_INT >= 21) {
            //设置描述音频流信息的属性
            AudioAttributes abs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(100)   //设置允许同时播放的流的最大值
                    .setAudioAttributes(abs)   //完全可以设置为null
                    .build();
        } else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }

        //加载全部音频
        //priority 目前没用
        soundIdInPool[0] = soundPool.load(MyApplication.getContext(), R.raw.test_music0, 1);
        soundIdInPool[1] = soundPool.load(MyApplication.getContext(), R.raw.test_music1, 1);
        soundIdInPool[2] = soundPool.load(MyApplication.getContext(), R.raw.test_music2, 1);

        //设置音频加载完毕监听事件
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                int i;
                for (i = 0; i < 3; i++) {
                    if (soundIdInPool[i] == sampleId)
                        break;
                }
                loadFinished[i] = true;
                Log.d(TAG, "onLoadComplete: 音频池资源id为：" + sampleId + "铃声编号为 " + i + " 的资源加载完成");
                //如果应该播放当前音乐，但刚才还没加载完，就播放
                if (sampleId == playingIdInPool && playSuccess == false) {
                    Log.d(TAG, "onLoadComplete: play when load finished");
                    soundPool.play(playingIdInPool, volumeF, volumeF, 0, -1, 1.0f);
                    playSuccess = true;
                }
            }
        });

    }

    /**
     * 手机响铃
     */
    public static void ring(int index, int volume) {

        //soundID参数为资源ID；
        // leftVolume和rightVolume个参数为左右声道的音量，从大到小取0.0f~1.0f之间的值；
        // priority为音频质量，暂时没有实际意义，传0即可；
        // loop为循环次数，0为播放一次，-1为无线循环，其他正数+1为播放次数，如传递3，循环播放4次；
        // rate为播放速率，从大到小取0.0f~2.0f，1.0f为正常速率播放。
        volumeF = ((float) volume) / 100;
        playingIdInPool = soundIdInPool[index];   //在音频池中的id
        Log.d(TAG, "ring: volume " + volume + "volumeF = " + volumeF);
        Log.d(TAG, "ring: index" + index);
        if (loadFinished[index]) {
            Log.d(TAG, "ring: loadFinished and play");
            playingIdInPool = soundPool.play(soundIdInPool[index], volumeF, volumeF, 0, -1, 1.0f);
            playSuccess = true;
        } else{
            playSuccess = false;
            Log.d(TAG, "ring: play failed ,wait for loading");
        }
    }


    /**
     * 销毁时释放音频池资源
     * 因为每次onCreate 都要重新load
     */
    public static void releaseSoundPool(){
        if(soundPool!=null) {
            soundPool.release();
            soundPool = null;
        }
    }

    /**
     * 停止播放当前音乐
     */
    public static void stopRing(){
        soundPool.stop(playingIdInPool);
        Log.d(TAG, "onDestroy: stop  " + playingIdInPool);
    }
}
