package com.example.admin.smartdiaper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.clj.fastble.data.BleDevice;

import com.example.admin.smartdiaper.activity.HomeFragment;
import com.example.admin.smartdiaper.activity.SetupFragment;
import com.example.admin.smartdiaper.activity.TimeLineFragment;
import com.example.admin.smartdiaper.ble.BleConnectService;
//import com.example.admin.smartdiaper.ble.BleService;
import com.example.admin.smartdiaper.ble.BleService;
import com.example.admin.smartdiaper.ble.BleStatusReceiver;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.remind.Reminder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private BleDevice bleDevice;//蓝牙设备
    private View homeView, timelineView, setupView; //bottom bar中的三个图标
    private BleStatusReceiver bleStatusReceiver;
    public static Handler handler;  //处理BleService传来的提醒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置三个Fragment
        setView();

        //初始化铃声
        Reminder.initSoundPool();

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
        //处理排尿提醒的handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Constant.PEE) {
                    sendNotification();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

                    if (preferences.getBoolean("vibrate", false)) {
                        Reminder.vibrate();
                    }
                    if (preferences.getBoolean("ring", false)) {
                        int volume = preferences.getInt("ring_volume", 100);
                        Log.d(TAG, "handleMessage: get volume from preference:" + volume);
                        int index = Integer.valueOf(preferences.getString("ring_music", "0")).intValue();
                        Log.d(TAG, "handleMessage: get musicId from preference:" + index);
                        Reminder.ring(index, volume);
                    }
                }
            }
        };
    }

    /**
     * -----------------------------------------------------------------------
     * 通知相关
     * ----------------------------------------------------------------------
     */
    /**
     * 创建通知通道
     * 全部设置成不要震动和响铃，因为设置后不好更改，因此将震动响铃和notification分开写
     *
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
        channel.setSound(null, null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);//设置是否在锁屏中显示

        /*
        for fragment:  (NotificationManager) getActivity().getSystemService
        for activity:  (NotificationManager) getSystemService
         */
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }


    /**
     * 发送通知
     */
    public void sendNotification() {
        //设置跳转到MainActivity 的 pendingintent，作为这条通知的点击事件
        Intent intent = new Intent(MyApplication.getContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(MyApplication.getContext(), 0, intent, 0);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
     * -----------------------------------------------------------------------
     * 蓝牙相关
     * ----------------------------------------------------------------------
     */

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        //开启蓝牙服务
        if (getIntent().getParcelableExtra("bleDevice") != null) {
            //获得bleDevice对象
            bleDevice = getIntent().getParcelableExtra("bleDevice");

            //把bleDevice传给BleConnectService，这样该service之后才能实现蓝牙连接
            Intent intent1 = new Intent(this, BleConnectService.class);
            intent1.putExtra("type", "transBleDevice");
            intent1.putExtra("bleDevice", bleDevice);
            startService(intent1);
            Log.d(TAG, "onResume: start connect service in main activity");

            //蓝牙期间执行的服务，读取温湿度数据等
            Intent intent = new Intent(this, BleService.class);
            intent.putExtra("bleDevice", bleDevice);
            startService(intent);
            Log.d(TAG, "Connect bleDevice: " + bleDevice.getName());

        }

        //注册蓝牙连接状态监听广播
        //实现断开自动重连
        bleStatusReceiver = new BleStatusReceiver();
//        IntentFilter stateChangeFilter = new IntentFilter(
//                BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constant.BLE_CON_ACTION);
        //registerReceiver(bleStatusReceiver,filter);
        //registerReceiver(bleStatusReceiver, stateChangeFilter);
        registerReceiver(bleStatusReceiver, connectedFilter);
        registerReceiver(bleStatusReceiver, disConnectedFilter);

        //发送BLE_CON_ACTION连接广播
//        Intent intent=new Intent();
//        if(bleDevice!=null) {
//            intent.putExtra("bleDevice", bleDevice);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setAction(Constant.BLE_CON_ACTION);
//            MainActivity.this.sendBroadcast(intent);
//        }else{
//            Log.e(TAG, "bleDevice is null");
//        }

    }

    /**-----------------------------------------------------------------------
     *                       设置Fragment相关
     *----------------------------------------------------------------------*/

    private void setView(){
        addFragment(new HomeFragment());//默认是homefragment
        homeView = findViewById(R.id.bottombar_home);
        timelineView = findViewById(R.id.bottombar_timeline);
        setupView = findViewById(R.id.bottombar_setup);
        homeView.setSelected(true);
        //点击事件：如果点击任何一个view,addFragment 并将其设为selected，其余设为非selected
        homeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: homeView");
                selected();
                homeView.setSelected(true);
                addFragment(new HomeFragment());
            }
        });

        timelineView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: timelineView");
                selected();
                timelineView.setSelected(true);
                addFragment(new TimeLineFragment());
            }
        });

        setupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: setupView");
                selected();
                setupView.setSelected(true);
                addFragment(new SetupFragment());
            }
        });
    }
    private void selected(){
        homeView.setSelected(false);
        timelineView.setSelected(false);
        setupView.setSelected(false);
    }

    public void addFragment(Fragment fragment) {
        //获取到FragmentManager，在V4包中通过getSupportFragmentManager，
        //在系统中原生的Fragment是通过getFragmentManager获得的。
        FragmentManager FMs = getSupportFragmentManager();
        //fig2.开启一个事务，通过调用beginTransaction方法开启。
        FragmentTransaction MfragmentTransactions = FMs.beginTransaction();
        //把自己创建好的fragment创建一个对象
        //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
        MfragmentTransactions.replace(R.id.main_content,fragment);
        //提交事务，调用commit方法提交。
        MfragmentTransactions.commit();
    }

    @Override
    protected void onDestroy(){
        Reminder.releaseSoundPool();
        super.onDestroy();
    }
}
