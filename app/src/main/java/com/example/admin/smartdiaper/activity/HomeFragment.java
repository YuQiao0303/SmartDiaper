package com.example.admin.smartdiaper.activity;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.admin.smartdiaper.MainActivity;
import com.example.admin.smartdiaper.R;

import static android.content.Context.NOTIFICATION_SERVICE;


public class HomeFragment extends Fragment{
    private static final String TAG="HomeFragment";

    //音频相关
    private SoundPool soundPool;
    private int soundId;
    private boolean loadFinished = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        initSoundPool();

        //创建通知通道
        //这部分代码可以写在任何位置，只需要保证在通知弹出之前调用就可以了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            * 渠道ID可以随便定义，只要保证全局唯一性就可以。
            * 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
            * 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的。这里设成low是为了不响铃
            * */
            String channelId = "remind";
            String channelName = "排尿提醒";
            int importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "other";
            channelName = "其他消息";
            importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button sendNotice = view.findViewById(R.id.send_notice);
        sendNotice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendNotification();
                vibrate();
                ring();
            }
        });
        return view;
    }

    /**
     * 创建通知通道
     * 全部设置成不要震动和响铃，因为设置后不好更改，因此将震动响铃和notification分开写
     * @param channelId
     * @param channelName
     * @param importance
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        //设置震动响铃等，高版本仅在此处设置有效，notification中设置无效
        //全部设置成不要震动和响铃，因为设置后不好更改，因此将震动响铃和notification分开写
        channel.enableLights(true);
        channel.enableVibration(false);
        channel.setSound(null,null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);//设置是否在锁屏中显示


        /*
        for fragment:  (NotificationManager) getActivity().getSystemService
        for activity:  (NotificationManager) getSystemService
         */
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 发送通知
     */
    public void sendNotification() {
        //设置跳转到MainActivity 的 pendingintent，作为这条通知的点击事件
        Intent intent = new Intent(this.getContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this.getContext(),0,intent,0);

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this.getContext(), "remind")  //注意了这里需要一个channelId
                .setContentTitle("宝宝尿了~")
                .setContentText("可以更换纸尿裤了哦~")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis())
                /*
                这些内容加了也没用，得在channel中设置；channel中设置了不能改，因此决定用其他方式实现
                .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")))  //设置铃声,
                .setVibrate(new long[]{0, 1000, 1000, 1000})  //设置震动：静止0ms，震动1000ms，静止1000毫秒，震动1000毫秒，需要声明权限
                .setLights(Color.GREEN,1000,1000)  //设置前置led等：绿色，亮1000ms，灭1000ms
                */
                .setAutoCancel(true)    //点击通知自动取消
                .setContentIntent(pi)   //点击跳转

                .build();
        manager.notify(1, notification);
        Log.d(TAG, "sendNotification: ");
    }

    /**
     * 手机震动
     */
    public void vibrate(){
        //创建震动服务对象
        Vibrator mVibrator;
        //获取手机震动服务
        mVibrator=(Vibrator)this.getContext().getSystemService(Service.VIBRATOR_SERVICE);
        long[] patter = {0, 500, 500, 500, 500, 500};  //静止xxms，震动xxms，静止xxms，震动xxms
        mVibrator.vibrate(patter, -1);  //循环，表示从数组的哪个下标开始循环，如果是-1表示不循环

        //停止震动
        //mVibrator.cancel();
    }

    public void initSoundPool(){
        //实例化soundPool
        if (Build.VERSION.SDK_INT >= 21) {
            //设置描述音频流信息的属性
            AudioAttributes abs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build() ;
            soundPool =  new SoundPool.Builder()
                    .setMaxStreams(100)   //设置允许同时播放的流的最大值
                    .setAudioAttributes(abs)   //完全可以设置为null
                    .build() ;
        }
        else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,0);
        }

        //加载音频
        soundId = soundPool.load(this.getContext(), R.raw.test_music, 1);  //priority 目前没用
        //设置音频加载完毕监听事件
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(TAG, "onLoadComplete: 音频池资源id为："+ soundId + "的资源加载完成");
                loadFinished = true;
            }
        });

    }
    /**
     * 手机响铃
     */
    public void ring(){

        //soundID参数为资源ID；
        // leftVolume和rightVolume个参数为左右声道的音量，从大到小取0.0f~1.0f之间的值；
        // priority为音频质量，暂时没有实际意义，传0即可；
        // loop为循环次数，0为播放一次，-1为无线循环，其他正数+1为播放次数，如传递3，循环播放4次；
        // rate为播放速率，从大到小取0.0f~2.0f，1.0f为正常速率播放。
        while(!loadFinished);
            soundPool.play(soundId,1.0f,1.0f,0,-1,1.0f);
    }

    /**
     * 销毁时释放音频池资源
     */
    @Override
    public void onDestroy(){

        if(soundPool!=null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }
}
