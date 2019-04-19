package com.example.admin.smartdiaper.constant;

public interface Constant {


    //蓝牙通信相关常量
    String UUID_KEY_SERVICE="0000fff0-0000-1000-8000-00805f9b34fb";
    String UUID_KEY_CHARACTERISTIC ="0000fff6-0000-1000-8000-00805f9b34fb";
    int DATA_SIZE_NO_TIME = 2;
    int DATA_SIZE_WITH_TIME = 6;
    String LAST_BLE_DISCON_TIME="lastDisconnectTime";
    //String BLE_CON_ACTION="com.example.admin.smartdiaper.ble";
    String savePowerHex = "22";
    String noSavePowerHex = "33";

    //Sqlite数据库相关的常量
    String DB_RECORD_TABLE_NAME = "tbl_record";
    String DB_PREDICTION_TABLE_NAME = "tbl_prediction";
    String DB_NAME = "SmartDiaper.db";
    int PREDICTION_NUM = 3;

    //handler & msg 定义
    int MSG_UPDATE_TEMPERATURE_HUMIDITY = 1;
    int MSG_PEE_MAIN = 2;
    int MSG_SET_MODE = 3;
    int MSG_STORE = 4;
    int MSG_PEE_HOME = 5;
    int MSG_DISCONNECTION = 6;
    int MSG_CONNECTION = 7;
    int MSG_RECONNET = 8;

    //notification id
    int NOTIFICATION_PEE = 1;

    //some params
    int TIME = 0;
    int MODE = 1;


}
