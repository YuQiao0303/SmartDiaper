package com.example.admin.smartdiaper.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.adapter.MyFragmentAdapter;
import com.example.admin.smartdiaper.ble.BleConnectService;
//import com.example.admin.smartdiaper.ble.BleService;
import com.example.admin.smartdiaper.ble.BleService;
import com.example.admin.smartdiaper.ble.BleStatusReceiver;

import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;
import com.example.admin.smartdiaper.remind.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //tab layout 管理/控件
    public static final String []sTitle = new String[]{"首页","时间轴","设置"};
    private int icons []= {R.drawable.btm_home,R.drawable.btm_service,R.drawable.btm_person};
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TextView disconnect;
    //蓝牙
    private BleDevice bleDevice;//蓝牙设备
    private BleStatusReceiver bleStatusReceiver;
    public static Handler handler;  //处理BleService传来的提醒
    //数据库
    private static MyDatabaseHelper dbHelper;


    //绑定BleService

    public BleService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            myBinder = (BleService.MyBinder) service;
            //更新home fragment 中当前温湿度
            Message msg = new Message();
            msg.what = Constant.MSG_UPDATE_TEMPERATURE_HUMIDITY;
            msg.arg1 = myBinder.getTemperature();
            msg.arg2 = myBinder.getHumidity();
            HomeFragment.handler.sendMessage(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disconnect = findViewById(R.id.not_connected);

        //数据库初始化
        initDatabase();
        //bind service
        Intent bindIntent = new Intent(MainActivity.this, BleService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        //设置三个Fragment
        initView();



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
                switch (msg.what)
                {
                    //排尿：数据库增加数据，发提醒，弹框，震动，响铃
                    case (Constant.MSG_PEE_MAIN):{
                        addRecord((long)msg.obj);//增加一条数据（数据库 & adapter 的list中 同步增加） //此处时间是1970开始至今的时间
                        long nextTime = predict();//添加或更新预测数据

                        sendNotification();   //发通知
                        showAlertDialog();   //弹框提醒
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

                        if (preferences.getBoolean("vibrate", true)) {
                            Reminder.vibrate();  //震动
                        }
                        if (preferences.getBoolean("ring", true)) {
                            int volume = preferences.getInt("ring_volume", 100);
                            Log.d(TAG, "handleMessage: get volume from preference:" + volume);
//                            int index = Integer.valueOf(preferences.getString("my_ring_music", "0")).intValue();
                            int index =preferences.getInt("my_ring_music", 0);
                            Log.d(TAG, "handleMessage: get musicId from preference:" + index);
                            Reminder.ring(index, volume);  //响铃
                        }

                        //更改HomeFragment ui
                        Message msg1 = new Message();
                        msg1.what = Constant.MSG_UPDATE_TIMES_IN_HOME;
                        long[] times ={(long)msg.obj,nextTime};

                        msg1.obj = times;
                        HomeFragment.handler.sendMessage(msg1);
                        //更新Timeline fragment 的ui
                        TimeLineFragment.getData();
                        break;
                    }
                    case (Constant.MSG_STORE):{
                        addRecord((long)msg.obj);//增加一条数据（数据库 & adapter 的list中 同步增加） //此处时间是1970开始至今的时间
                        long nextTime = predict();//添加或更新预测数据
                        TimeLineFragment.getData(); //更新Timeline fragment 的ui
                        //更改HomeFragment ui
                        Message msg1 = new Message();
                        msg1.what = Constant.MSG_UPDATE_TIMES_IN_HOME;
                        long[] times ={(long)msg.obj,nextTime};

                        msg1.obj = times;
                        HomeFragment.handler.sendMessage(msg1);
                        break;
                    }
                    case(Constant.MSG_UPDATE_RECORD):{

                    }
                    case (Constant.MSG_SET_MODE): {
                        Log.d(TAG, "handleMessage: MainActivity收到设置模式的消息");
                        if (myBinder == null) {
                            Intent bindIntent = new Intent(MainActivity.this, BleService.class);
                            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
                            Log.d(TAG, "handleMessage: myBinder == null");
                            myBinder.setSavePowerMode();
                        }
                        else{
                            Log.d(TAG, "handleMessage: myBinder is fine");
                            myBinder.setSavePowerMode();
                        }
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

                        break;
                    }
                    case(Constant.MSG_CONNECTION):{
                        disconnect.setVisibility(View.GONE);
                        break;
                    }
                    case(Constant.MSG_DISCONNECTION):{
                        disconnect.setVisibility(View.VISIBLE);
                        break;
                    }
                    case(Constant.MSG_RECONNET):{
                        BleManager.getInstance().disconnectAllDevice();
                        Intent intent=new Intent(MainActivity.this,FindDiaperActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }


                    default :break;
                }

            }
        };
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        ble();
        //rebind bleService
        if (myBinder == null) {
            Intent bindIntent = new Intent(MainActivity.this, BleService.class);
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "onResume: myBinder == null ,rebind");
        }
        else{
            Log.d(TAG, "onResume: myBinder is fine");

        }

        //如果蓝牙连接上了，隐藏"蓝牙尚未连接"的提示语
        if(bleDevice != null) {
            Log.d(TAG, "onCreate: bleDevice != null");
            if (BleManager.getInstance().isConnected(bleDevice)) {

                disconnect.setVisibility(View.GONE);
            }
            else
                disconnect.setVisibility(View.VISIBLE);
        }
        else
            disconnect.setVisibility(View.VISIBLE);

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
                .setAutoCancel(false)    //点击通知无法自动取消
                .setContentIntent(pi)   //点击跳转

                .build();
        manager.notify(Constant.NOTIFICATION_PEE, notification);
        Log.d(TAG, "sendNotification: ");
    }
    private void showAlertDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder (MainActivity.
                this);
        dialog.setTitle("宝宝尿了！");
        dialog.setMessage("可以更换纸尿裤了哦~");
        dialog.setCancelable(false);
        dialog.setPositiveButton("好的", new DialogInterface.
                OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //关闭通知
                NotificationManager manager = (NotificationManager) getSystemService
                        (NOTIFICATION_SERVICE);
                manager.cancel(Constant.NOTIFICATION_PEE);
                //铃声停止
                Reminder.stopRing();
            }});
        dialog.setNegativeButton("取消", new DialogInterface.
                OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    /**
     * -----------------------------------------------------------------------
     * 蓝牙相关
     * ----------------------------------------------------------------------
     */

    private void ble(){
        //
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

        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(bleStatusReceiver, connectedFilter);
        registerReceiver(bleStatusReceiver, disConnectedFilter);


        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bleStatusReceiver, filter);
    }


    /**-----------------------------------------------------------------------
     *                       数据库相关
     *----------------------------------------------------------------------*/
    private void initDatabase(){
        //创建数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了
    }
    private void addRecord(long time){
        //数据库操作
        SQLiteDatabase db = dbHelper.getWritableDatabase();   //获得该数据库实例
        ContentValues values = new ContentValues();
        //添加这条历史记录
        values.put("time", time);
        db.insert(Constant.DB_RECORD_TABLE_NAME,null,values);
        values.clear();
        //list操作
        //如果此时timelineFratment 正在显示中，就手动增加一条数据，否则，下次进入HomeFragment的时候会自动调用OnCreateView 方法中的initData


        Log.d(TAG, "addTestData: 成功添加数据！");

    }

    /**
     * 预测算法
     * @return  返回预测的下次排尿时间
     */
    public static long predict(){
        Log.d(TAG, "predict: ");
        List<Long> prediction = new ArrayList<Long>();
        prediction.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();   //获得该数据库实例
        ContentValues values = new ContentValues();
        long diff = 0;
        //获得历史记录数据
        Cursor cursor = db.rawQuery("select count(id) , MAX(time) , MIN(time) from " + Constant.DB_RECORD_TABLE_NAME ,null);
        if (cursor.moveToFirst())
        {
            int count = cursor.getInt(0);
            if(count ==0)   // 如果还没数据
            {
                return -1;
            }
            else if (count == 1)  //如果只有一条数据，也先return试试吧
            {
                return -1;
            }
            else
            {  //数据大于等于两条，可以预测，算出每次排尿的平均间隔diff
                long maxTime = cursor.getLong(1);
                long minTime = cursor.getLong(2);
                diff = (maxTime - minTime)/(count - 1);
                Log.d(TAG, "predict: diff = " + diff);

                //加入预测数据库
                Cursor cursorPrediction = db.rawQuery("select count(id)  from " + Constant.DB_PREDICTION_TABLE_NAME ,null);
                if (cursorPrediction.moveToFirst())
                {
                    if(cursorPrediction.getInt(0) == 0)  //预测数据库暂无数据，需要insert新数据
                    {
                        for(int i=0;i<Constant.PREDICTION_NUM;i++) {
                            values.put("time", maxTime + (i + 1) * diff);
                            db.insert(Constant.DB_PREDICTION_TABLE_NAME, null, values);
                            values.clear();
                            Log.d(TAG, "addRecord: 添加 " + i + "条预测数据");
                        }
                    }
                    else    //update
                    {
                        for(int i=0;i<Constant.PREDICTION_NUM;i++) {
                            values.put("time", maxTime + (i + 1) * diff);
                            db.update(Constant.DB_PREDICTION_TABLE_NAME,  values,"id= ?",new String[] {""+(i+1)}); //不要漏了问号
                            values.clear();
                            Log.d(TAG, "addRecord: 更新 " +i + "条预测数据");
                        }
                    }
                    return maxTime + diff;
                }
                else
                {
                    Log.d(TAG, "predict: 读取预测数据个数失败");
                }
            }
        }
        else{   //读取数据库失败
            Log.d(TAG, "predict: 预测时，读取record table 失败");
        }
        return -1;
    }
    /**-----------------------------------------------------------------------
     *                       设置Fragment相关
     *----------------------------------------------------------------------*/

    private void initView(){
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setText(sTitle[0]));
        mTabLayout.addTab(mTabLayout.newTab().setText(sTitle[1]));
        mTabLayout.addTab(mTabLayout.newTab().setText(sTitle[2]));

        mTabLayout.setupWithViewPager(mViewPager);

        //add icons : must do it after setupWithViewPager
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(icons[i]);
        }
        List<Fragment> fragments = new ArrayList<>();

        //homeFragment
        HomeFragment homeFragment = new HomeFragment();
        if(myBinder!=null)
        {
            Bundle bundle = new Bundle();
            bundle.putInt("temperature" ,myBinder.getTemperature());
            bundle.putInt("humidity" ,myBinder.getHumidity());
            homeFragment.setArguments(bundle);
        }
        fragments.add(homeFragment);
        //timeline fragment
        fragments.add(new TimeLineFragment());


        //setup fragment
        SetupFragment setupFragment = new SetupFragment();
        Bundle bundle = new Bundle();
        if(bleDevice == null)
            bundle.putInt("connect",0);
        else if( !BleManager.getInstance().isConnected(bleDevice))
            bundle.putInt("connect",0);
        else
            bundle.putInt("connect",1);
        setupFragment.setArguments(bundle);
        fragments.add(setupFragment);

        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(),fragments, Arrays.asList(sTitle));
        mViewPager.setAdapter(adapter);
    }


    /**
     * 按返回键，回到home而不销毁当前activity
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy(){
        Reminder.releaseSoundPool();
        BleManager.getInstance().disconnectAllDevice();
        unregisterReceiver(bleStatusReceiver);
        super.onDestroy();
    }
}
