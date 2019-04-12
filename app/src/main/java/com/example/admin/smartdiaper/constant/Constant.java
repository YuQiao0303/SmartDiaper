package com.example.admin.smartdiaper.constant;

public interface Constant {


    //蓝牙通信相关常量
    String UUID_KEY_SERVICE="0000fff0-0000-1000-8000-00805f9b34fb";
    String UUID_KEY_CHARACTERISTIC ="0000fff6-0000-1000-8000-00805f9b34fb";
    int DATA_SIZE_NO_TIME = 2;
    int BINARY_DATA_DIGITS=24;
    String LAST_BLE_DISCON_TIME="lastDisconnectTime";
    //String BLE_CON_ACTION="com.example.admin.smartdiaper.ble";

    //Sqlite数据库相关的常量
    String DB_RECORD_NAME = "tbl_record";
    String DB_PREDICTION_NAME = "tbl_prediction";


    //handler & msg 定义
    int MSG_UPDATE_TEMPERATURE_HUMIDITY = 1;
    int MSG_PEE = 2;

    //notification id
    int NOTIFICATION_PEE = 1;
}
