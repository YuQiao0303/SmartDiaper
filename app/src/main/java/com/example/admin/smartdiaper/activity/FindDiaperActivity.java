package com.example.admin.smartdiaper.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.permission.PermissionListener;

import java.util.List;

public class FindDiaperActivity extends BaseActivity {

    private TextView deviceScan,deviceMac;
    private Button skip;

    private static final int REQUEST_CODE_OPEN_BLE=1;
    private static final int REQUEST_CODE_OPEN_GPS=2;

   // BleDevice bleDevice;//蓝牙设备信息

    private static String LOG_TAG="FindBoxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_diaper);

        deviceScan=findViewById(R.id.deviceResult);
        deviceMac=findViewById(R.id.deviceResult_mac);
        skip=findViewById(R.id.skipFind);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FindDiaperActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnScan=findViewById(R.id.scan);
        //重点是下面这两句话
        //点击扫描按钮，开始扫描
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();//扫描并建立连接
            }
        });

        initBLE();
      //  doScan();//扫描并建立连接
    }

    private void initBLE(){
        //初始化以及全局配置
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance().enableLog(true);
        //如果蓝牙没打开，引导用户打开
        if(!BleManager.getInstance().isBlueEnable()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_CODE_OPEN_BLE);
        }

        //6.0以上机型需要动态获取位置权限 permission-group.LOCATION
        requestPermissions();

    }

    private void doScan(){
        //配置扫描规则
        //这里要把附近所有蓝牙设备都显示出来，所以不配置规则
        //参考： https://github.com/Jasonchenlijian/FastBle/wiki/%E6%89%AB%E6%8F%8F%E5%8F%8A%E8%BF%9E%E6%8E%A5#%E9%85%8D%E7%BD%AE%E6%89%AB%E6%8F%8F%E8%A7%84%E5%88%99
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
             //   .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
             //   .setDeviceName(true, names)         // 只扫描指定广播名的设备，可选
             //   .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
             //   .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
             //   .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        //开始扫描
        //https://github.com/Jasonchenlijian/FastBle/wiki/%E6%89%AB%E6%8F%8F%E5%8F%8A%E8%BF%9E%E6%8E%A5#%E6%89%AB%E6%8F%8F
        BleManager.getInstance().scan(new BleScanCallback() {

            /**
             * 扫描是否成功开启（主线程）
             * 由于蓝牙没有打开或者上次扫描没有结束等原因会造成扫描开启失败
             * @param success
             */
            @Override
            public void onScanStarted(boolean success) {
                if(success){
                    Log.d(LOG_TAG, "onScanStarted: 开始扫描");
                    deviceScan.setText("正在扫描...");
                }else {
                    Log.d(LOG_TAG, "onScanStarted: 失败");
                }
            }

            /**
             * 所有被扫描到的结果回调（工作线程）
             * 同一个设备可能多次调用，调用次数取决于周围设备量以及外围设备的广播间隔
             * @param bleDevice
             */
            @Override
            public void onLeScan(BleDevice bleDevice) {
            }

            /**
             * 所有过滤后的扫描结果回调（主线程）
             * 同一个设备只出现一次
             * @param bleDevice
             */
            @Override
            public void onScanning(BleDevice bleDevice) {
                String targetBleName=getResources().getString(R.string.ble_name);
                Log.d(LOG_TAG, "onScanning: targetBleName = " + targetBleName);
                String name = bleDevice.getName();
                Log.d(LOG_TAG, "扫描到新设备：" + name);
                deviceScan.setText("扫描到新设备：" + name);
                deviceMac.setText("mac: "+bleDevice.getMac());
                if(name!=null && name.equals(targetBleName)){
                    BleManager.getInstance().cancelScan();
                    getConnect(bleDevice);//扫描到设备，开始连接
                }
            }

            /**
             * 本次扫描时间段内所有过滤后的扫描设备集合（主线程）
             * @param scanResultList
             */
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
            }
        });
    }
    //连接设备
    //https://github.com/Jasonchenlijian/FastBle/wiki/%E6%89%AB%E6%8F%8F%E5%8F%8A%E8%BF%9E%E6%8E%A5#%E9%80%9A%E8%BF%87%E8%AE%BE%E5%A4%87%E5%AF%B9%E8%B1%A1%E8%BF%9E%E6%8E%A5
    private void getConnect(BleDevice bleDevice){

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(LOG_TAG, "onStartConnect: 开始连接设备...");
                deviceMac.setText("开始连接...");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                Log.d(LOG_TAG,"连接失败");
                deviceMac.setText("连接失败");
                Toast.makeText(getApplicationContext(),"连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                Log.d(LOG_TAG, "onConnectSuccess: 连接成功");
                deviceMac.setText("连接成功");
                Intent intent=new Intent(FindDiaperActivity.this,MainActivity.class);
                intent.putExtra("bleDevice",bleDevice);  //把bleDevice 传给MainActivity
                startActivity(intent);
            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                 //断开后延迟一段时间再重连
                Log.d(LOG_TAG, "onDisConnected: 蓝牙连接断开");
                //通常，连接断开前提是之前连接成功，已经进入了MainActivity，故这句toast应该不会显示
                //Toast.makeText(getApplicationContext(),"连接断开", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //权限申请
    private void requestPermissions(){
        requestRunPermisssion(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new PermissionListener() {
            @Override
            public void onGranted() {
                doScan();
            }

            @Override
            public void onDenied(List<String> deniedPermission) {

                //权限被用户拒绝
                //当拒绝了授权后，为提升用户体验，可以以弹窗的方式引导用户到设置中去进行设置
                new android.support.v7.app.AlertDialog.Builder(FindDiaperActivity.this)
                        .setMessage("需要开启定位权限才能使用此功能")
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //引导用户到设置中打开蓝牙定位
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivityForResult(intent,REQUEST_CODE_OPEN_GPS);

                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }

        });

    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_BLE) {
            requestPermissions();
        }
        if(requestCode==REQUEST_CODE_OPEN_GPS){
            if(checkGPSIsOpen()){
                doScan();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
