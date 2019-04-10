package com.example.admin.smartdiaper.ble;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;
import com.example.admin.smartdiaper.constant.Constant;
//import com.example.admin.smartdiaper.utils.DateUtil;
//import com.example.admin.smartdiaper.utils.SharedPreferencesUtils;

/**
 * 蓝牙连接状态监听
 * 连上了：发通知
 * 断开了：启动BleConnectedService自动重连
 * 传来device：启动BleConnectedService
 */
public class BleStatusReceiver extends BroadcastReceiver {
    BleDevice device;
    String name;
    private static String TAG="BleStatusReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(device!=null){
            name=device.getName();
        }
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            //连接上了
            Log.d(TAG, "onReceive: "+"蓝牙已连接");
            Toast.makeText(context,"蓝牙已连接", Toast.LENGTH_LONG).show();
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            //蓝牙连接被切断
            //SharedPreferencesUtils.setLong("lastTime", DateUtil.currentTimeFrom2000InSeconds(),context);//存储蓝牙断开的时间
            Log.d(TAG, "onReceive: "+"蓝牙已断开");
            Toast.makeText(context,"蓝牙的连接已断开，正在尝试重新连接", Toast.LENGTH_LONG).show();
            Intent intent2=new Intent(context, BleConnectService.class);
            intent2.putExtra("type","sysbroadcast");
            context.startService(intent2);

        }
//        else if(action.equals(Constant.BLE_CON_ACTION)){
//            Log.d(TAG, "onReceive: BLE_CON_ACTION");
//            device=intent.getParcelableExtra("bleDevice");//传递连接的蓝牙
//            Intent intent1=new Intent(context, BleConnectService.class);
//            intent1.putExtra("bleDevice",device);
//            intent1.putExtra("type","transBleDevice");
//            context.startService(intent1);
//            Log.d(TAG, "onReceive: start connect service in receiver");
//        }

    }
}
