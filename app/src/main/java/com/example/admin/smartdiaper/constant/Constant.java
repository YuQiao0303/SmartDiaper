package com.example.admin.smartdiaper.constant;

public interface Constant {


    //蓝牙通信相关常量
    String UUID_KEY_SERVICE="0000fff0-0000-1000-8000-00805f9b34fb";
    String UUID_KEY_CHARACTERISTIC ="0000fff6-0000-1000-8000-00805f9b34fb";
    int BINARY_DATA_DIGITS=24;
    String LAST_BLE_DISCON_TIME="lastDisconnectTime";
    //String BLE_CON_ACTION="com.example.admin.smartdiaper.ble";

    //Sqlite数据库相关的常量
    int DB_VERSION = 1;
    String DB_RECORD_NAME = "tbl_record";
    String DB_PREDICTION_NAME = "tbl_prediction";
    String DB_CREATE_FAILURE = "数据库创建失败";
    String DB_CREATE_SUCCESS = "数据库创建成功";
    String DB_CREATING = "正在创建数据库";

    //数据库表相关常量
    String DB_DATARECORD_TABLENAME="data";
    String DB_OPERATINGRECORD_TABLENAME = "operatingrecord";
    String DB_DOSERECORD_TABLENAME = "doserecord";
    String DB_BOXSTATE_TABLENAME = "boxstate";
    String DB_REMIND_TABLENAME="remind";
    String DB_CREATE_TABLE_FAILURE = "数据表创建失败";
    String DB_CREATE_TABLE_SUCCESS = "数据表创建成功";
    String DB_CREATE_TABLE_EXIST = "数据表已经存在，无需再次创建";
    //数据库操作相关常量
    String DB_OPEN_READ_CONNECTION = "打开读数据库连接";
    String DB_OPEN_WRITE_CONNECTION = "打开写数据库连接";
    String DB_CLOSE_READ_CONNECTION = "关闭读数据库连接";
    String DB_CLOSE_WRITE_CONNECTION = "关闭写数据库连接";
    String DB_INSERT = "插入数据";
    String DB_UPDATE = "更新数据";
    String DB_DELETE = "删除数据";
    String DB_UPDATE_EXCEPTIOIN = "更新数据异常";
    String DB_INSERT_EXCEPTIOIN = "插入数据异常";
    String DB_DELETE_EXCEPTIOIN = "删除数据异常";
    String DB_UPDATE_COMMIT = "确定数据提交到后台";
    String DB_UPDATE_COMMIT_EXCEPTION = "确定数据提交到后台异常";
    String DB_QUERY_EXCEPTIOIN = "查询数据异常";


}
