package com.example.admin.smartdiaper.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.admin.smartdiaper.constant.Constant.DB_PREDICTION_TABLE_NAME;
import static com.example.admin.smartdiaper.constant.Constant.DB_RECORD_TABLE_NAME;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyDatabaseHelper";
    //建表的SQL语句
    public static final String CREATE_TABLE_RECORD = "create table "+ DB_RECORD_TABLE_NAME +" ("
            + "id integer primary key autoincrement, "
            + "time integer) ";  //1970.1.1  0点至今的毫秒数

    public static final String CREATE_TABLE_PREDICTION = "create table "+ DB_PREDICTION_TABLE_NAME +" ("
            + "id integer primary key autoincrement, "
            + "time integer) ";  //1970.1.1  0点至今的毫秒数

    //删表语句
    public static final String DROP_TABLE_RECORD ="drop table if exists "+ DB_RECORD_TABLE_NAME;
    public static final String DROP_TABLE_PREDICTION = "drop table if exists " + DB_PREDICTION_TABLE_NAME;
    private Context mContext;
    //构造方法
    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }


    /**
     * 该函数在数据库创建时执行
     * 尚未创建数据库是，调用以下方法就会创建数据库：dbHelper.getWritableDatabase();
     * 在onCreate中执行建表操作
     * 也就是创建数据库的同时，建表
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        db.execSQL(CREATE_TABLE_RECORD);  //
        db.execSQL(CREATE_TABLE_PREDICTION);
        Log.d(TAG, "onCreate: 建表成功！");
//        //insert 数据
//        ContentValues values = new ContentValues();
//        //insert 历史记录
//        int i;
//        for(i = 0;i<2;i++)
//        {
//            values.put("time", i*1000*3600);
//            db.insert(DB_RECORD_TABLE_NAME,null,values);
//            values.clear();
//        }
//        //insert 预测数据
//        for(i = 2;i<5;i++)
//        {
//            values.put("time", i*1000*3600);
//            db.insert(DB_PREDICTION_TABLE_NAME,null,values);
//            values.clear();
//        }
//
//        Log.d(TAG, "addTestData: 成功添加数据！");
    }

    /**
     * 该函数在版本号比已存在的数据库更大时调用
     * 可以在里面重新建表
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_RECORD);
        db.execSQL(DROP_TABLE_PREDICTION);
        onCreate(db);
    }

}
