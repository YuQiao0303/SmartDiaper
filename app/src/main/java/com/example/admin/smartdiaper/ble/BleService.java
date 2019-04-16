package com.example.admin.smartdiaper.ble;

import android.app.Service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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


public class BleService extends Service {

    private static final String TAG ="BleService" ;
    //特征
    private BluetoothGattCharacteristic characteristic;
    private BleDevice bleDevice;

    //上一次的温湿度
    private int lastTemperature;
    private int lastHumidity;

    //binder
    private MyBinder myBinder = new MyBinder();
    public class MyBinder extends Binder{
        public void setSavePowerMode(){
            Log.d(TAG, "setSavePowerMode: 设置模式的方法！");
            if (bleDevice == null)
            {
                Log.d(TAG, "setSavePowerMode: bleDebive == null! 不能设置模式");
                return ;
            }
            setDiaperTimeAndMode(Constant.MODE);
        }
    }

    public BleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
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

                    if (mCharacteristic.getUuid().toString().equals(Constant.UUID_KEY_CHARACTERISTIC)) {
                        characteristic = mCharacteristic;
                        // 等待100毫秒
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() { }
                        },2000);

                        //写入时间
                        setDiaperTimeAndMode(Constant.TIME);
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


    private void setDiaperTimeAndMode(final int timeOrMode){
        //获取模式
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        boolean flag = false;
        String hexStr;
        //省电模式
        if(preferences.getBoolean("save_power",false)){
            // 后四个字节是这个long表示的时间从2000年到现在的时间（配合硬件主控），以秒为单位
            hexStr=Constant.savePowerHex + "ffff"+Long.toHexString(DateTimeUtil.currentTimeFrom2000InSeconds());
        }
        else{
            hexStr=Constant.noSavePowerHex + "ffff"+Long.toHexString(DateTimeUtil.currentTimeFrom2000InSeconds());
        }


        BleManager.getInstance().write(bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(hexStr),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int i, int i1, byte[] bytes) {
                        Log.d(TAG, "写入时间和状态: "+ HexUtil.encodeHexStr(bytes));
                        Toast.makeText(BleService.this, "状态设置成功！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWriteFailure(BleException e) {
                        Log.e(TAG, "onWriteFailure: 写入时间和状态失败！");
                        Log.e(TAG, "onWriteFailure: "+e.getDescription());
                        //此时要重置状态
                        if(timeOrMode == Constant.MODE)
                        {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("save_power",!preferences.getBoolean("save_power",false));

                            editor.commit();
                            Log.d(TAG, "onWriteFailure: 由于向硬件写入状态失败，已经重置模式");
                            Toast.makeText(BleService.this, "向硬件写入状态失败,请确认蓝牙连接后重试", Toast.LENGTH_SHORT).show();
                        }

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
        //过滤温度传感器不响应时的错误码
        if(HexUtil.encodeHexStr(data).equals("5555")){
            return;
        }


        int temperature = data[0];
        int humidity = data[1];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

        //普通模式
        if(!preferences.getBoolean("save_power",false)){
            //Log.d(TAG, "handleData: 普通模式");
            if(data.length !=Constant.DATA_SIZE_NO_TIME){
                Log.d(TAG, "handleData: 收到notification长度有误：");
                return;
            }

            Log.d(TAG, "handleData: 温度："+ temperature + "湿度: "+ humidity);
            //更新ui : handler & messa
            Message msg = new Message();
            msg.what = Constant.MSG_UPDATE_TEMPERATURE_HUMIDITY;
            msg.arg1 = temperature;
            msg.arg2 = humidity;
            HomeFragment.handler.sendMessage(msg);
            //判断是否提醒
            if(humidity >= 40 && lastHumidity<40)
            {
                sendPeeMessage();
                Log.d(TAG, "handleData: humidity:"+humidity +" last humidity :"+ lastHumidity);
            }
            //更新上次温湿度数据
            lastTemperature = temperature;
            lastHumidity = humidity;

        }
        //省电模式
        else{
            Log.d(TAG, "handleData: 省电模式");
            //有效性判断
            if(data.length !=Constant.DATA_SIZE_WITH_TIME){
                Log.d(TAG, "handleData: 收到notification长度有误：");
                return;
            }
            //时间判断


            //提醒
            sendPeeMessage();
            //数据加入数据库
            // 在TimeLineFragment 中显示

        }


    }

    /**
     * 婴儿排尿时调用该函数
     * 提醒用户更换纸尿裤，将数据加入数据库
     */
    private void sendPeeMessage(){
        //提醒
        Message msg = new Message();
        msg.what = Constant.MSG_PEE;
        msg.obj = System.currentTimeMillis();
        MainActivity.handler.sendMessage(msg);
        //加入数据库
    }


}
