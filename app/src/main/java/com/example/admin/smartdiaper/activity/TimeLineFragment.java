package com.example.admin.smartdiaper.activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;

import com.example.admin.smartdiaper.adapter.TimeAdapter;

import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;
import com.example.admin.smartdiaper.utils.DateTimeUtil;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;




public class TimeLineFragment extends Fragment{
    private static final String TAG="TimeLineFragment";

    //存储列表数据
    static List<TimelineItem> list = new ArrayList<>();
    static TimeAdapter adapter;
    public static TimeAdapter getAdapter() {
        return adapter;
    }

    //数据库
    private static MyDatabaseHelper dbHelper;

    //UI 控件
    private static TextView noRecordYet;
    //
    private Calendar calendar;

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


        // recyclerview绑定适配器
        rlView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new TimeAdapter(list,getActivity());
        rlView.setAdapter(adapter);

        //初始化数据
        getData();

        //判断是否有数据
        noRecordYet = view.findViewById(R.id.no_record_yet);
        if(list.size()==0) //若无数据，就显示暂无数据
            noRecordYet.setVisibility(View.VISIBLE);
        else
            noRecordYet.setVisibility(View.GONE);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出DatePickerDialog
                //注意第一个参数得是Activity哦
                calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //获取新的日期
                                calendar.set(Calendar.YEAR,year);
                                calendar.set(Calendar.MONTH,month);
                                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                                //弹出timePickerDialog
                                //注意第一个参数得是Activity哦
                                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                                //获取新的时间
                                                calendar.set(Calendar.HOUR_OF_DAY,hour);
                                                calendar.set(Calendar.MINUTE,minute);
                                                long newTime = calendar.getTimeInMillis();
                                                //发消息给MainActivity
                                                Message msg = new Message();
                                                msg.what = Constant.MSG_STORE;
                                                msg.obj = newTime;
                                                MainActivity.handler.sendMessage(msg);
                                            }
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
                                if(!getActivity().isFinishing())
                                    timePickerDialog.show();
                            }
                        },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                if(!getActivity().isFinishing())
                {
                    datePickerDialog.show();
                }
            }
        });
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


    /**-----------------------------------------------------------------------
     *                       数据相关
     *----------------------------------------------------------------------*/

    public static void getData() {
        Log.d(TAG, "getData: ");
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
                Log.d(TAG, "从数据库得到"+ id +"的预测时间： " + DateTimeUtil.time2ShowString(time));
                Log.d(TAG, "id is " + id);
                list.add(new TimelineItem(id,time, true,""));

            } while (cursorPrediction.moveToNext());
        }
        else
            Log.d(TAG, "getData: 预测数据为空！");
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
                list.add(new TimelineItem(id,time, false,""));
            } while (cursor.moveToNext());
        }
        cursor.close();
        //更新listView
        adapter.notifyDataSetChanged();
        //判断是否有数据
        if(noRecordYet != null) {
            if (list.size() == 0) //若无数据，就显示暂无数据
                noRecordYet.setVisibility(View.VISIBLE);
            else
                noRecordYet.setVisibility(View.GONE);
        }
    }

}
