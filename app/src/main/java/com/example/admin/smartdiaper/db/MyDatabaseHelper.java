package com.example.admin.smartdiaper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    //建表的SQL语句
    public static final String CREATE_TABLE_RECORD = "create table tbl_record ("
            + "id integer primary key autoincrement, "
            + "TimeStamp DEFAULT(datetime('now', 'localtime')))";

    //
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
        db.execSQL(CREATE_TABLE_RECORD);  //
        Toast.makeText(mContext, "Create Table Record succeeded", Toast.LENGTH_SHORT).show();
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
        db.execSQL("drop table if exists Record");
        onCreate(db);
    }

}
