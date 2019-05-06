package com.example.admin.smartdiaper.adapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.activity.HomeFragment;
import com.example.admin.smartdiaper.activity.MainActivity;
import com.example.admin.smartdiaper.activity.TimeLineFragment;
import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;
import com.example.admin.smartdiaper.utils.DateTimeUtil;
import com.example.admin.smartdiaper.view.PopupWindowList;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wen on 2017/6/14.
 */

public class TimeAdapter extends RecyclerView.Adapter{
    private static final String TAG = "TimeAdapter";
    //数据
    private List<TimelineItem> list;
    //长按弹框 popup window list
    private PopupWindowList mPopupWindowList;
    //context
    private Context context;
    //database
    private static MyDatabaseHelper dbHelper;
    SQLiteDatabase db;
    //variables
    private int mHour;
    private int mMinute;
    Calendar calendar;

    public TimeAdapter( List<TimelineItem> list,Context context) {
        this.list = list;
        this.context =context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RecyclerView.ViewHolder mHolder =holder;
        final int itemPsition = position;
        ((ViewHolder) holder).setPosition(position);
        //长按的监听事件：弹框
        mHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int layoutPosition = mHolder.getLayoutPosition();
                showPopWindows(v,itemPsition);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView rightTxt;
        private TimelineItem timelineItem;
        private ImageView dotImage;
        private TextView leftTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            rightTxt = (TextView) itemView.findViewById(R.id.right_txt);
            leftTxt = itemView.findViewById(R.id.left_txt);
            dotImage = (ImageView)itemView.findViewById(R.id.dot_image);
        }


        public void setPosition(int position) {
            timelineItem = list.get(position);

            //显示在左边还是右边
            if(position % 2 ==0)
            {
                leftTxt.setVisibility(View.GONE);
                rightTxt.setVisibility(View.VISIBLE);
                rightTxt.setText(DateTimeUtil.time2ShowString(timelineItem.getTime()));
            }
            else{
                rightTxt.setVisibility(View.GONE);
                leftTxt.setVisibility(View.VISIBLE);
                leftTxt.setText(DateTimeUtil.time2ShowString(timelineItem.getTime()));
            }


            //时间节点图片
            if(timelineItem.isPredicted())
                dotImage.setImageResource(R.drawable.prediction_dot);
            else
                dotImage.setImageResource(R.drawable.record_dot);

        }
    }
    /**
     * 长按弹框方法的实现
     * @param view  itemView，被长按的条目
     * @param itemPosition   被长按的条目的position
     */
    private void showPopWindows(View view , final int itemPosition){
        if(itemPosition <3) return;
        final View itemView = view;

        List<String> dataList = new ArrayList<>();
        dataList.add("修改");
        dataList.add("删除");


        if (mPopupWindowList == null){
            mPopupWindowList = new PopupWindowList(view.getContext());
        }
        mPopupWindowList.setAnchorView(view);
        mPopupWindowList.setItemData(dataList);
        mPopupWindowList.setModal(true);
        mPopupWindowList.show();
        mPopupWindowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "click position="+position);
                switch (position){
                    //修改
                    case (0):{
                        onClickUpdate(itemView,itemPosition);
                        break;
                    }
                    //删除
                    case (1):{
                        onClickDelete(itemView,itemPosition);
                        break;
                    }

                }
                mPopupWindowList.hide();
            }
        });
    }

    /**
     * 删除条目的实现
     * @param itemView
     * @param itemPosition
     */
    private void onClickDelete(View itemView,final int itemPosition){
        //获取实体类对象
        final TimelineItem timelineItem = list.get(itemPosition);
        //创建/获取数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        db = dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；
        //在数据库中删除
        dbHelper.deleteById(db,timelineItem.getId());
        //在list中删除
        list.remove(itemPosition);
        //更新预测数据
        long nextTime = MainActivity.predict();
        //在list中修改时间
        TimeLineFragment.getData();
        //发送message
        Message msg = new Message();
        msg.what = Constant.MSG_UPDATE_TIMES_IN_HOME;
        long[] times ={list.get(3).getTime(),nextTime};  //第三个数据是上次排尿时间
        msg.obj = times;
        HomeFragment.handler.sendMessage(msg);

        //弹出snackBar
        Log.d(TAG, "onItemClick: 删除");
        Snackbar.make(itemView, "您删除了一条数据", Snackbar.LENGTH_SHORT)
                .setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //重新保存数据到数据库
                        dbHelper.addRecordInDB(db,timelineItem.getTime());
                        //重新保存数据到list
                        list.add(itemPosition,timelineItem);
                        //更新预测数据
                        long nextTime = MainActivity.predict();
                        //更新TimeLineFragment的ui
                        TimeLineFragment.getData();
                        //发送message
                        Message msg = new Message();
                        msg.what = Constant.MSG_UPDATE_TIMES_IN_HOME;
                        long[] times ={list.get(3).getTime(),nextTime};  //第三个数据是上次排尿时间
                        msg.obj = times;
                        HomeFragment.handler.sendMessage(msg);
                    }
                })
                .show();
    }

    /**
     * 修改
     * @param itemView
     * @param itemPosition
     */
    private void onClickUpdate(View itemView,final int itemPosition){
        //获取实体类对象
        final TimelineItem timelineItem = list.get(itemPosition);
        //创建/获取数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        db = dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；
        //弹出timePickerDialog
        //注意第一个参数得是Activity哦
        calendar= Calendar.getInstance();
        //获得修改前的时间
        calendar.setTimeInMillis(timelineItem.getTime());
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {


                        //获取新的时间
                        calendar.set(Calendar.HOUR_OF_DAY,hour);
                        calendar.set(Calendar.MINUTE,minute);
                        long newTime = calendar.getTimeInMillis();
                        //创建/获取数据库
                        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
                        db = dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；
                        //在数据库中修改时间
                        dbHelper.updateTime(db,timelineItem.getId(),newTime);
                        timelineItem.setTime(newTime);
                        //更新预测数据
                        long nextTime = MainActivity.predict();
                        //在list中修改时间
                        TimeLineFragment.getData();
                        //发送message
                        Message msg = new Message();
                        msg.what = Constant.MSG_UPDATE_TIMES_IN_HOME;
                        long[] times ={list.get(3).getTime(),nextTime};  //第三个数据是上次排尿时间
                        msg.obj = times;
                        HomeFragment.handler.sendMessage(msg);
                    }
                },
                mHour, mMinute,true);
        if(!((MainActivity)context).isFinishing())
            timePickerDialog.show();
    }
}
