package com.example.admin.smartdiaper.ble;

import android.app.Service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.utils.DateTimeUtil;


public class BleService extends Service {

    private static final String TAG ="BleService" ;
    //特征
    private BluetoothGattCharacteristic characteristic;
    private BleDevice bleDevice;

    public BleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        //获得传来的蓝牙设备
        bleDevice=intent.getParcelableExtra("bleDevice");
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        for (BluetoothGattService mService : gatt.getServices()) {
            // 获取对应的服务
            if (mService.getUuid().toString().equals(Constant.UUID_KEY_SERVICE)) {
                // 获取对应的特征字
                for (BluetoothGattCharacteristic mCharacteristic : mService.getCharacteristics()) {
                    characteristic = mCharacteristic;
                    if (mCharacteristic.getUuid().toString().equals(Constant.UUID_KEY_CHARACTERISTIC)) {
                        // 等待100毫秒
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() { }
                        },2000);

                        //写入时间
                        setDiaperTime();
                        //打开通知
                        BleManager.getInstance().notify(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                new BleNotifyCallback(){
                                    @Override
                                    public void onNotifySuccess() {
                                        Log.d(TAG, "onNotifySuccess: 打开通知成功");
                                    }

                                    @Override
                                    public void onNotifyFailure(final BleException exception) {
                                        Log.d(TAG, "onNotifyFailure: 打开通知失败");
                                    }

                                    @Override
                                    public void onCharacteristicChanged(byte[] data) {
                                        Log.d(TAG, "recData: "+HexUtil.encodeHexStr(data));
                                        handleData(data);
                                    }

                                });


                    }
                }
            }
         }

        return super.onStartCommand(intent, flags, startId);
    }


    private void setDiaperTime(){
        //写入时间
        //获取从2000年到现在的时间（配合硬件主控），以秒为单位
        long currentTimeInSeconds = DateTimeUtil.currentTimeFrom2000InSeconds();
        String hexStr="ffffff"+Long.toHexString(currentTimeInSeconds);// 写入的内容，前三个字节是全1，后四个字节是这个long表示的时间
        BleManager.getInstance().write(bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(hexStr),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int i, int i1, byte[] bytes) {
                        Log.d(TAG, "写入时间: "+ HexUtil.encodeHexStr(bytes));
                    }

                    @Override
                    public void onWriteFailure(BleException e) {
                        Log.e(TAG, "onWriteFailure: 写入时间失败！");
                        Log.e(TAG, "onWriteFailure: "+e.getDescription());
                    }
                });
    }
    //进行数据处理
    private void handleData(final byte[] data){
        //处理数据
        /*
         * 判断数据是否有效，
         * 如果是省电模式，响应，数据加入数据库，并在TimeLineFragment 中显示
         * 如果是非省电模式，数据显示到HomeFragment，并判断是否提醒
         * */

        //判断数据是否有效，有效的话要响应
        // 可能会收到多组数据，每组数据5个字节
        if (data.length == 0) {
            return;
        }
        if(data.length %Constant.DATA_SIZE !=0){
            //Log.d(TAG, "handleData: 收到notification数据出错：");
        }

        if(data.length %Constant.DATA_SIZE ==0){

        }
    }

}
