package com.example.admin.smartdiaper.activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.admin.smartdiaper.R;

import com.example.admin.smartdiaper.adapter.TimeAdapter;

import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.admin.smartdiaper.constant.Constant.DB_PREDICTION_NAME;
import static com.example.admin.smartdiaper.constant.Constant.DB_RECORD_NAME;


public class TimeLineFragment extends Fragment{
    private static final String TAG="TimeLineFragment";

    //存储列表数据
    List<TimelineItem> list = new ArrayList<>();
    TimeAdapter adapter;

    //数据库
    private MyDatabaseHelper dbHelper;
    public TimeLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //初始化数据库:建表/添加数据
        initDatabase();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_timeline, container, false);

        //time line
        RecyclerView rlView = view.findViewById(R.id.activity_rlview);
        //初始化数据
        initData();

        // recyclerview绑定适配器
        rlView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = new TimeAdapter(list);
        rlView.setAdapter(adapter);
        return view;

    }


    /**-----------------------------------------------------------------------
     *                       数据库相关
     *----------------------------------------------------------------------*/
    private void initDatabase(){
        //创建数据库
        dbHelper = new MyDatabaseHelper(this.getContext(), "SmartDiaper.db", null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了
        //添加测试数据
        //addTestData();
    }
    private void addTestData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();   //获得该数据库实例
        ContentValues values = new ContentValues();
        //历史记录
        int i;
        for(i = 0;i<10;i++)
        {
            values.put("time", i*1000*3600);
            db.insert(DB_RECORD_NAME,null,values);
            values.clear();
        }
        //预测数据
        for(i = 10;i<13;i++)
        {
            values.put("time", i*1000*3600);
            db.insert(DB_PREDICTION_NAME,null,values);
            values.clear();
        }

        Log.d(TAG, "addTestData: 成功添加数据！");
    }

    /**-----------------------------------------------------------------------
     *                       数据相关
     *----------------------------------------------------------------------*/
    private void initData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // DB_PREDICTION_NAME
        Cursor cursorPrediction = db.query(DB_PREDICTION_NAME, null, null, null, null, null, "time desc");
//        final String selPrediction = "select * from "+ DB_PREDICTION_NAME + "order by time desc";
//        Cursor cursorPrediction = db.rawQuery(selPrediction,null);
        if (cursorPrediction.moveToFirst()) {
            do {
                // 遍历Cursor对象，将每条数据加入list，并打印到控制台
                int id = cursorPrediction.getInt(cursorPrediction.getColumnIndex
                        ("id"));
                long time = cursorPrediction.getLong(cursorPrediction.getColumnIndex
                        ("time"));
                Log.d(TAG, "time is " + time);
                Log.d(TAG, "id is " + id);
                list.add(new TimelineItem(time, true,""));
            } while (cursorPrediction.moveToNext());
        }
        cursorPrediction.close();

        // 查询DB_RECORD_NAME表中所有的数据
        Cursor cursor = db.query(DB_RECORD_NAME, null, null, null, null, null, "time desc");
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，将每条数据加入list，并打印到控制台
                int id = cursor.getInt(cursor.getColumnIndex
                        ("id"));
                long time = cursor.getLong(cursor.getColumnIndex
                        ("time"));
                Log.d(TAG, "time is " + time);
                Log.d(TAG, "id is " + id);
                list.add(new TimelineItem(time, false,""));
            } while (cursor.moveToNext());
        }
        cursor.close();


    }

}
