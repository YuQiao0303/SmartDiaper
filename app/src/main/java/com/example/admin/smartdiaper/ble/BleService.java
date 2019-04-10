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
//import com.example.admin.smartdiaper.activity.ClockAlarmActivity;
//import com.example.admin.smartdiaper.bean.Data;
//import com.example.admin.smartdiaper.bean.OperatingLat;
import com.example.admin.smartdiaper.constant.Constant;
//import com.example.admin.smartdiaper.db.DataHelper;
//import com.example.admin.smartdiaper.db.DoseRecordHelper;
//import com.example.admin.smartdiaper.utils.ByteUtil;
//import com.example.admin.smartdiaper.utils.DateUtil;
import com.example.admin.smartdiaper.activity.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class BleService extends Service {

    private static final String TAG ="BleService" ;

    private BluetoothGattCharacteristic characteristic;

//    private String str2,str2_old;//二进制字符串
//    private List<OperatingLat> operatingLatList=new ArrayList<>();
//    //BoxStateHelper boxStateHelper;
//    private DataHelper dataHelper;
//    private DoseRecordHelper doseRecordHelper;
//    private Handler mHandler;
//    private Runnable mRunnable;
//    private int emptyLatticesCount=0;

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

//        dataHelper=new DataHelper(getApplicationContext());
//        doseRecordHelper=new DoseRecordHelper(getApplicationContext());
//
//        mHandler=new Handler();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
//        final BleDevice bleDevice=intent.getParcelableExtra("bleDevice");
//        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
//        for (BluetoothGattService mService : gatt.getServices()) {
//            // 获取对应的服务
//            if (mService.getUuid().toString().equals(Constant.UUID_KEY_SERVICE)) {
//                // 获取对应的特征字
//                for (BluetoothGattCharacteristic mCharacteristic : mService.getCharacteristics()) {
//                    characteristic=mCharacteristic;
//                    if (mCharacteristic.getUuid().toString().equals(Constant.UUID_KEY_CHARACTERISTIC)) {
//
//                        //写数据 时间同步
//                        long currentTimeInSeconds;
//                        currentTimeInSeconds=DateUtil.currentTimeFrom2000InSeconds(); //从2000年1月1日距离现在的时间，以秒为单位（配合主控时间格式）
//                        String hexStr="ffffff"+ Long.toHexString(currentTimeInSeconds);
//                        BleManager.getInstance().write(bleDevice,
//                                characteristic.getService().getUuid().toString(),
//                                characteristic.getUuid().toString(),
//                                HexUtil.hexStringToBytes(hexStr),
//                                new BleWriteCallback() {
//                                    @Override
//                                    public void onWriteSuccess(int i, int i1, byte[] bytes) {
//                                        Log.d(TAG, "写入时间: "+ HexUtil.encodeHexStr(bytes));
//                                    }
//
//                                    @Override
//                                    public void onWriteFailure(BleException e) {
//                                        Log.e(TAG, "onWriteFailure: 写入时间失败！");
//                                        Log.e(TAG, "onWriteFailure: "+e.getDescription());
//                                    }
//                                });
//
//
//                        // 等待100毫秒打开通知
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() { }
//                        },100);
//                        BleManager.getInstance().notify(
//                                bleDevice,
//                                characteristic.getService().getUuid().toString(),
//                                characteristic.getUuid().toString(),
//                                new BleNotifyCallback() {
//
//                                    @Override
//                                    public void onNotifySuccess() {
//                                        Log.d(TAG, "onNotifySuccess: success");
//                                    }
//
//                                    @Override
//                                    public void onNotifyFailure(final BleException exception) {
//                                        Log.d(TAG, "onNotifyFailure: fail");
//                                    }
//
//                                    @Override
//                                    public void onCharacteristicChanged(final byte[] data) {
//                                        // 打开通知后，设备发过来的数据将在这里处理
//                                        if(data.length==0) return;
//                                        Log.d(TAG, "recData: "+ HexUtil.encodeHexStr(data));
//                                        if(data.length%7==0){
//                                            //收到数据，响应硬件设备
//                                            new Thread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    try {
//                                                        Thread.sleep(100);//等待100毫秒
//                                                        response(bleDevice,data,characteristic);
//                                                    }catch (InterruptedException e){
//                                                        Log.e(TAG, e.toString() );
//                                                    }
//                                                }
//                                            }).start();
//
//                                            handleData(data);
//                                        }else{
//                                            Log.e(TAG, "onCharacteristicChanged: 数据传输出错",null );
//                                        }
//
//                                    }
//                                });
//                        break;
//                    }
//
//                }
//            }
//        }
        return super.onStartCommand(intent, flags, startId);


    }


    //进行数据处理
    private void handleData(final byte[] data){
//
//
//        mRunnable=new Runnable() {
//            @Override
//            public void run() {
//                synchronized (this) {
//                    int dataCount=data.length/7;//一组数据7字节，一次可能收到多组
//                    final List<Data> strList = new ArrayList<>();
//                    for(int i=0;i<dataCount;i++) {
//                        byte[] rcvBytes = new byte[7];
//                        for(int j=0;j<7;j++) {
//                            rcvBytes[j]=data[i*7+j];
//                        }
//                        strList.add(parseDate(rcvBytes));//解析数据
//                    }
//                    Data data1=dataHelper.queryStr();
//                    if (data1.isEmpty()){
//                        data1.setData("000000000000000000000000");
//                        data1.setDate("1970-01-01");
//                        data1.setTime("00:00:00");
//                    }
//                    str2_old = data1.getData();//旧药盒状态
//                    boolean isWarning=false;
//                    //判断是否生成服药记录，若由1->0则生成，若由0->0则忽略
//                    for (int i = 0; i < strList.size(); i++) {
//                        str2 = strList.get(i).getData();//当前药盒操作
//                        //生成服药记录
//                        str2_old=isDose(str2_old, str2,strList.get(i).getDate(), strList.get(i).getTime());
//                        if(DateUtil.stringToLong(data1.getDate(),data1.getTime())+10*60000<DateUtil.stringToLong(strList.get(i).getDate(),strList.get(i).getTime())){
//                            isWarning=true;
//                        }
//                    }
//                    //如果空药盒数占0.8以上，重置提醒
//                    Log.d(TAG, "空药格数: "+emptyLatticesCount+",isWarning:"+isWarning);
//                    if(emptyLatticesCount>Constant.BOX_LATTICE_NUM*0.8&&isWarning) {
//                        Intent resetIntent = new Intent(getApplicationContext(), ClockAlarmActivity.class);
//                        resetIntent.putExtra("ResetAlarm", "ResetAlarm");
//                        resetIntent.putExtra("msg", "药快吃完了，请及时装药！");
//                        startActivity(resetIntent);
//                    }
//                }
//            }
//        };
//        mHandler.post(mRunnable);
    }

//    //解析数据
//    private Data parseDate(byte[] rcvData){
//        Long DateTime=ByteUtil.bytes2Long(rcvData,3,rcvData.length-1);//解析日期
//        String Str_Binary= ByteUtil.byteArrToBinStr(rcvData,0,0);//二进制字符串
//        Log.e(TAG, "parseData: " + Str_Binary+",Date:"+DateUtil.getDateFrom2000Long(DateTime)+",Time:"+DateUtil.getTimeFrom2000Long(DateTime));
//        return new Data(Str_Binary,DateUtil.getDateFrom2000Long(DateTime),DateUtil.getTimeFrom2000Long(DateTime));
//    }
//
//
//    /**
//     * 是否吃药，如果吃药则往数据库添加吃药记录
//     * @param str_old 上一次药盒状态码
//     * @param str 本次药盒状态码
//     * @param Date 本次日期
//     * @param Time 本次时间
//     * @return 更新的药盒状态码
//     */
//    private String isDose(String str_old, String str, String Date, String Time){
//        emptyLatticesCount=0;
//        StringBuilder str_buf=new StringBuilder();
//        str_buf.append(str_old);
//        //找出0，做异或运算，判断是否吃药
//        for (int i = 0; i < Constant.BOX_LATTICE_NUM; i++){
//            int digit_old=str_old.charAt(i)-'0';
//            int digit=str.charAt(i)-'0';
//            if(digit_old==0){
//                emptyLatticesCount++;//空药格计数
//            }
//            if(digit==0){
//                Log.d(TAG, "用户操作："+(i+1)+"号 打开药盒");
//                if((digit^digit_old)==1) {
//                    //由1变0，吃药
//                    emptyLatticesCount++;
//                    OperatingLat operatingLat = new OperatingLat();
//                    operatingLat.setDate(Date);
//                    operatingLat.setTime(Time);
//                    operatingLat.setNumber(i+1);
//                    operatingLat.setMeal(getMeal(i, 0));
//                    operatingLat.setOperating(0);
//                    doseRecordHelper.insert(operatingLat,getExpectedNumber());//添加服药记录
//                    operatingLatList.add(operatingLat);
//                    Log.d(TAG, (i+1)+"号 打开药盒并吃药");
//                }
//                str_buf.replace(i,i+1,"0");
//            }
//        }
//        String currentStatus=str_buf.toString();
//        dataHelper.insert(currentStatus,Date,Time);
//        return str_buf.toString();
//    }
//
//    //预期药格序号
//    private int getExpectedNumber(){
//        int i=0;
//        return i;
//    }
//
//
//    /**
//     * @param i 输入序号
//     * @param flag 0输出中文“早”，1输出数字
//     * @return
//     */
//    private String getMeal(int i, int flag){
//        String mealStr="";
//        String mealDig="";
//        if(Constant.BOX_MEALS_NUM==3) {
//            switch (i % 3) {
//                case 0:
//                    mealStr = "早";
//                    mealDig = "0";
//                    break;
//                case 1:
//                    mealStr = "中";
//                    mealDig = "1";
//                    break;
//                case 2:
//                    mealStr = "晚";
//                    mealDig = "2";
//                    break;
//            }
//        }else if(Constant.BOX_MEALS_NUM==2){
//            switch (i % 2) {
//                case 0:
//                    mealStr = "早";
//                    mealDig = "0";
//                    break;
//                case 1:
//                    mealStr = "晚";
//                    mealDig = "2";
//                    break;
//            }
//        }
//        return flag==0?mealStr:mealDig;
//    }
//
//    /**
//     * 响应函数
//     * @param bleDevice 蓝牙
//     * @param data 响应数据
//     */
//    private void response(BleDevice bleDevice, byte[] data, BluetoothGattCharacteristic characteristic){
//        BleManager.getInstance().write(bleDevice,
//                characteristic.getService().getUuid().toString(),
//                characteristic.getUuid().toString(),
//                data,
//                new BleWriteCallback() {
//                    @Override
//                    public void onWriteSuccess(int i, int i1, byte[] bytes) {
//                        Log.d(TAG, "响应数据发送成功:"+ HexUtil.encodeHexStr(bytes));
//                    }
//
//                    @Override
//                    public void onWriteFailure(BleException e) {
//                        Log.e(TAG, "响应数据发送失败！");
//                    }
//                });
//
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mHandler.removeCallbacks(mRunnable);
//    }
}
