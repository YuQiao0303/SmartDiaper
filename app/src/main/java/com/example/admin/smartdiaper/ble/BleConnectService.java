package com.example.admin.smartdiaper.ble;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
//import com.example.admin.smartdiaper.utils.DateUtil;
//import com.example.admin.smartdiaper.utils.SharedPreferencesUtils;

/**
 * 蓝牙连接服务
 */
public class BleConnectService extends Service {
    public static final String KEY_DATA = "key_data";
    public BleDevice bleDevice;

    private String TAG="BleConnectService";

    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate BleConnectService");
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        //如果是首次连接传bleDevice的，就传完直接return了
        if(intent.getStringExtra("type").equals("transBleDevice")){
            bleDevice = intent.getParcelableExtra("bleDevice");
            return super.onStartCommand(intent, flags, startId);
        }
        //否则就连接
        connect(bleDevice);

        return super.onStartCommand(intent, flags, startId);

    }

    //只有断开重连时才会调用此方法
    //首次连接时不会调用
    private void connect(final BleDevice bleDevice) {
        Log.d(TAG, "connect: ");

        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(3)//设置重连次数
                .setConnectOverTime(10000)
                .setOperateTimeout(5000)
                .connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect: ");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.e(TAG, "onConnectFail:蓝牙连接失败！" );
                    Toast.makeText(getApplicationContext(),"蓝牙连接失败！", Toast.LENGTH_SHORT).show();
                    stopSelf();//关闭服务
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess: ");
//                Toast.makeText(getApplicationContext(),"蓝牙连接成功", Toast.LENGTH_LONG).show();
                Intent intentBleService=new Intent(getApplicationContext(),BleService.class);
                intentBleService.putExtra("bleDevice",bleDevice);
                startService(intentBleService);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                //蓝牙连接被切断
                //无需操作，Broadcast会调用重新启动该service
                //SharedPreferencesUtils.setLong("lastTime", DateUtil.currentTimeFrom2000InSeconds(),getApplicationContext());//存储蓝牙断开的时间
//                Log.d(TAG, "onDisConnected: "+"蓝牙已断开");
//                Toast.makeText(getApplicationContext(),"service蓝牙的连接已断开，正在尝试重新连接", Toast.LENGTH_LONG).show();
//                connect(bleDevice);//重连
            }
        });
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        System.out.println("onDestroy invoke");
        super.onDestroy();
    }

}
