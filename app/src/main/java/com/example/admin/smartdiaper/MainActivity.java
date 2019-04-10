package com.example.admin.smartdiaper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private BleDevice bleDevice;//蓝牙设备

    private View homeView, timelineView, setupView;
    private long mExitTime;
    private BleStatusReceiver bleStatusReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置三个Fragment
        setView();
    }

    /**-----------------------------------------------------------------------
     *                           蓝牙相关
     *----------------------------------------------------------------------*/

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        //开启蓝牙服务
        if(getIntent().getParcelableExtra("bleDevice")!=null) {
            //获得bleDevice对象
            bleDevice = getIntent().getParcelableExtra("bleDevice");

            //把bleDevice传给BleConnectService，这样该service之后才能实现蓝牙连接
            Intent intent1=new Intent(this,BleConnectService.class);
            intent1.putExtra("type","transBleDevice");
            intent1.putExtra("bleDevice", bleDevice);
            startService(intent1);
            Log.d(TAG, "onResume: start connect service in main activity");

            //蓝牙期间执行的服务，读取温湿度数据等
            Intent intent = new Intent(this, BleService.class);
            intent.putExtra("bleDevice", bleDevice);
            startService(intent);
            Log.d(TAG, "Connect bleDevice: "+bleDevice.getName());

        }

        //注册蓝牙连接状态监听广播
        //实现断开自动重连
        bleStatusReceiver=new BleStatusReceiver();
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
}
