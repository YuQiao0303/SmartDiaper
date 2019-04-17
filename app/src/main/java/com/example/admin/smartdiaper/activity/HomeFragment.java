package com.example.admin.smartdiaper.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.constant.Constant;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;
import com.example.admin.smartdiaper.utils.DateTimeUtil;


public class HomeFragment extends Fragment{
    private static final String TAG="HomeFragment";
    public static Handler handler;  //处理BleService传来的更新温湿度ui消息
    //数据库
    private MyDatabaseHelper dbHelper;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        final TextView lastTime = view.findViewById(R.id.last_time);
        final TextView currentTemperature = view.findViewById(R.id.current_temperature);
        final TextView currentHumidity = view.findViewById(R.id.current_humidity);
        //-------------查数据库--------------
        //创建数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Cursor cursorPrediction = db.query(Constant.DB_PREDICTION_TABLE_NAME, null, null, null, null, null, "time desc");
        Cursor cursor = db.rawQuery("select MAX(time) from " + Constant.DB_RECORD_TABLE_NAME ,null);
        if (cursor.moveToFirst()) {
            long time = cursor.getLong(0);
            //long time = cursor.getLong(cursor.getColumnIndex("time"));
            Log.d(TAG, "time is " + time);
            lastTime.setText(DateTimeUtil.toymdhms(time));
        }
        else
            Log.d(TAG, "初始化“上次排尿时间”时，查询数据库失败");
        cursor.close();
        //---------------------------------------------
        //更新温湿度ui的handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case (Constant.MSG_UPDATE_TEMPERATURE_HUMIDITY):{
                        currentTemperature.setText("当前温度： "+ msg.arg1 + " ℃");
                        currentHumidity.setText("当前湿度： "+ msg.arg2 );
                        break;
                    }
                    case(Constant.MSG_PEE_HOME):{
                        lastTime.setText(DateTimeUtil.toymdhms((long)msg.obj));
                        break;
                    }
                    default:break;
                }

            }
        };

        //省电模式
        final Switch savePower = view.findViewById(R.id.save_power_home);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        savePower.setChecked(preferences.getBoolean("save_power",false));
        Log.d(TAG, "onCreateView: save_Power from preference+" + preferences.getBoolean("save_power",false));
        if(!savePower.isChecked())
        {
            //显示当前温湿度
            currentHumidity.setVisibility(View.VISIBLE);
            currentTemperature.setVisibility(View.VISIBLE);
        }
        else
        {
            //不显示当前温湿度
            currentHumidity.setVisibility(View.GONE);
            currentTemperature.setVisibility(View.GONE);
        }


        savePower.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                if(savePower.isChecked())
                {
                    editor.putBoolean("save_power",true);
                    //不显示当前温湿度
                    currentHumidity.setVisibility(View.GONE);
                    currentTemperature.setVisibility(View.GONE);
                }
                else{
                    editor.putBoolean("save_power",false);
                    //显示当前温湿度
                    currentHumidity.setVisibility(View.VISIBLE);
                    currentTemperature.setVisibility(View.VISIBLE);

                }
                editor.commit();
                Log.d(TAG, "onClick: save_power from home " + savePower.isChecked());
                //发消息让MainActivity 调用BleService的方法来设置硬件模式
                Message msg = new Message();
                msg.what = Constant.MSG_SET_MODE;
                MainActivity.handler.sendMessage(msg);
            }
        });
        return view;
    }
}
