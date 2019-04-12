package com.example.admin.smartdiaper.activity;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.example.admin.smartdiaper.MainActivity;
import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.constant.Constant;

import static android.content.Context.NOTIFICATION_SERVICE;


public class HomeFragment extends Fragment{
    private static final String TAG="HomeFragment";

    //音频相关
    private SoundPool soundPool;
    private int soundId[] = {0,0,0};  //sound[0] 是test_music0 加载后在sound pool中的id
    private int playingId;  //正在播放的音频在音频池中的id
    private boolean[] loadFinished = {false,false,false};  //loadFinished[0]是test_music0.mp3是否加载完毕
    private boolean playSuccess = false;
    private float volumeF;
    public static Handler handler;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //初始化铃声
        initSoundPool();

        //创建通知通道
        //这部分代码可以写在任何位置，只需要保证在通知弹出之前调用就可以了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            * 渠道ID可以随便定义，只要保证全局唯一性就可以。
            * 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
            * 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的。这里设成low是为了不响铃
            * */
            createNotificationChannel("remind", "排尿提醒", NotificationManager.IMPORTANCE_LOW);
            createNotificationChannel("other", "其他消息", NotificationManager.IMPORTANCE_LOW);
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button rdmindTest = view.findViewById(R.id.send_notice);
        final TextView lastTime = view.findViewById(R.id.last_time);
        final TextView currentTemperature = view.findViewById(R.id.current_temperature);
        final TextView currentHumidity = view.findViewById(R.id.current_humidity);
        //更新ui的handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == Constant.UPDATE_TEMPERATURE_HUMIDITY)
                {
                    currentTemperature.setText("当前温度： "+ msg.arg1 + " ℃");
                    currentHumidity.setText("当前湿度： "+ msg.arg2 );
                }
            }
        };
        //提醒按钮
        rdmindTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendNotification();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

                if(preferences.getBoolean("vibrate",false)){
                    vibrate();
                }
                if(preferences.getBoolean("ring",false)){
                    int volume = preferences.getInt("ring_volume",100);
                    Log.d(TAG, "onClick: get volume from preference:" + volume);
                    int index = Integer.valueOf(preferences.getString("ring_music","0")).intValue();
                    Log.d(TAG, "onClick: get musicId from preference:" + index);
                    ring(index,volume);
                }

            }
        });
        //省电模式
        final Switch savePower = view.findViewById(R.id.save_power_home);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        savePower.setChecked(preferences.getBoolean("save_power",false));
        Log.d(TAG, "onCreateView: save_Power from preference+" + preferences.getBoolean("save_power",false));
        savePower.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                if(savePower.isChecked())
                {
                    editor.putBoolean("save_power",true);
                }
                else{
                    editor.putBoolean("save_power",false);
                }
                editor.commit();
                Log.d(TAG, "onClick: save_power from home " + savePower.isChecked());
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
        Intent intent = new Intent(MyApplication.getContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(MyApplication.getContext(),0,intent,0);

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(MyApplication.getContext(), "remind")  //注意了这里需要一个channelId
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
        mVibrator=(Vibrator)MyApplication.getContext().getSystemService(Service.VIBRATOR_SERVICE);
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

        //加载全部音频
        //priority 目前没用
        soundId[0] = soundPool.load(MyApplication.getContext(), R.raw.test_music0, 1);
        soundId[1] = soundPool.load(MyApplication.getContext(), R.raw.test_music1, 1);
        soundId[2] = soundPool.load(MyApplication.getContext(), R.raw.test_music2, 1);

        //设置音频加载完毕监听事件
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                int i;
                for(i=0;i<3;i++)
                {
                    if(soundId[i] == sampleId)
                        break;
                }
                loadFinished[i] = true;
                Log.d(TAG, "onLoadComplete: 音频池资源id为："+ sampleId + "铃声编号为 "+ i +" 的资源加载完成");
                //如果应该播放当前音乐，但刚才还没加载完，就播放
                if(sampleId == playingId && playSuccess == false)
                {
                    soundPool.play(playingId, volumeF, volumeF, 0, -1, 1.0f);
                    playSuccess = true;
                }
            }
        });

    }
    /**
     * 手机响铃
     */
    public void ring(int index,int volume){

        //soundID参数为资源ID；
        // leftVolume和rightVolume个参数为左右声道的音量，从大到小取0.0f~1.0f之间的值；
        // priority为音频质量，暂时没有实际意义，传0即可；
        // loop为循环次数，0为播放一次，-1为无线循环，其他正数+1为播放次数，如传递3，循环播放4次；
        // rate为播放速率，从大到小取0.0f~2.0f，1.0f为正常速率播放。
        volumeF = ((float)volume)/100;
        playingId = soundId[index];   //在音频池中的id
        Log.d(TAG, "ring: volume "+volume +"volumeF = " + volumeF);
        Log.d(TAG, "ring: index" + index);
        if(loadFinished[index]) {
            playingId = soundPool.play(soundId[index], volumeF, volumeF, 0, -1, 1.0f);
            playSuccess = true;
        }
        else
            playSuccess = false;
    }

    /**
     * 销毁时释放音频池资源
     * 因为每次onCreate 都要重新load
     */
    @Override
    public void onDestroy(){

        if(soundPool!=null) {
            soundPool.release();
            soundPool = null;
        }
//        soundPool.stop(playingId);
//        Log.d(TAG, "onDestroy: stop  " + playingId);
        super.onDestroy();
    }
}
