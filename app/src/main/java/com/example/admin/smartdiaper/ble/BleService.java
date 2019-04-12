package com.example.admin.smartdiaper.ble;

import android.app.Service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.example.admin.smartdiaper.MainActivity;
import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.activity.HomeFragment;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.utils.DateTimeUtil;

import static com.example.admin.smartdiaper.MyApplication.getContext;


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

    /**
     * 处理硬件传来的温湿度数据
     * @param data
     */
    private void handleData(final byte[] data){
        //判断数据是否有效
        if (data.length == 0) {
            Log.d(TAG, "handleData: 收到notification长度为0");
            return;
        }
        if(data.length !=Constant.DATA_SIZE_NO_TIME){
            Log.d(TAG, "handleData: 收到notification长度有误：");
            return;
        }
        //过滤震动传感器数据
        if(HexUtil.encodeHexStr(data).equals("ffff"))
            return;


        int temperature = data[0];
        int humidity = data[1];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

        //普通模式
        if(!preferences.getBoolean("save_power",false)){
            Log.d(TAG, "handleData: 普通模式");
            //更新ui : handler & messa
            Message msg = new Message();
            msg.what = Constant.UPDATE_TEMPERATURE_HUMIDITY;
            msg.arg1 = temperature;
            msg.arg2 = humidity;
            HomeFragment.handler.sendMessage(msg);
            //判断是否提醒
            //如果提醒
            //数据加入数据库
            //在TimeLineFragment 中显示

        }
        //省电模式
        else{
            Log.d(TAG, "handleData: 省电模式");
            //响应
            //提醒
            //数据加入数据库
            // 在TimeLineFragment 中显示

        }
    }
}
