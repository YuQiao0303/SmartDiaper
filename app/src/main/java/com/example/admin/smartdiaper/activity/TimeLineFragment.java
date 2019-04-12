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


import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;

import com.example.admin.smartdiaper.adapter.TimeAdapter;

import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;



import java.util.ArrayList;
import java.util.List;




public class TimeLineFragment extends Fragment{
    private static final String TAG="TimeLineFragment";

    //存储列表数据
    static List<TimelineItem> list = new ArrayList<>();
    static TimeAdapter adapter;

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
        rlView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new TimeAdapter(list);
        rlView.setAdapter(adapter);
        return view;

    }


    /**-----------------------------------------------------------------------
     *                       数据库相关
     *----------------------------------------------------------------------*/
    private void initDatabase(){
        //创建数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了
    }
    private void addTestData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();   //获得该数据库实例
        ContentValues values = new ContentValues();
        //历史记录
        int i;
        for(i = 0;i<10;i++)
        {
            values.put("time", i*1000*3600);
            db.insert(Constant.DB_RECORD_TABLE_NAME,null,values);
            values.clear();
        }
        //预测数据
        for(i = 10;i<13;i++)
        {
            values.put("time", i*1000*3600);
            db.insert(Constant.DB_PREDICTION_TABLE_NAME,null,values);
            values.clear();
        }

        Log.d(TAG, "addTestData: 成功添加数据！");
    }

    /**-----------------------------------------------------------------------
     *                       数据相关
     *----------------------------------------------------------------------*/
    public static void addRecordInList(long time)
    {
        //移除最上面的预测数据
        for(int i=0;i<Constant.PREDICTION_NUM;i++)
        {
            list.remove(0);
        }
//        if(list.size()>0)
//        {
//            while(list.get( list.size()-1).isPredicted() == true )
//            {
//                list.remove(list.size()-1);
//                if(list.size()<=0)
//                    break;
//            }
//        }
        //添加新的排尿记录
        list.add(0,new TimelineItem(time, false,""));
        //添加新的预测数据
        for(int i=0;i<Constant.PREDICTION_NUM;i++)
            list.add(0,new TimelineItem(time, true,""));

        //更新listView
        adapter.notifyDataSetChanged();
    }
    public void updatePrediction(long []times){

    }
    private void initData() {
        list.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursorPrediction = db.query(Constant.DB_PREDICTION_TABLE_NAME, null, null, null, null, null, "time desc");
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
        Cursor cursor = db.query(Constant.DB_RECORD_TABLE_NAME, null, null, null, null, null, "time desc");
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
