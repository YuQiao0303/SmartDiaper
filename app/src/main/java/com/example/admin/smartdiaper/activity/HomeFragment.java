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
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

        final View lastTimeTimeLine = view.findViewById(R.id.last_time_time_line);
        final View nextTimeTimeLine = view.findViewById(R.id.next_time_time_line);

        final TextView lastTime = lastTimeTimeLine.findViewById(R.id.right_txt);
        final TextView nextTime = nextTimeTimeLine.findViewById(R.id.right_txt);

        final TextView lastTimeTxt = lastTimeTimeLine.findViewById(R.id.left_txt);
        final TextView nextTimeTxt = nextTimeTimeLine.findViewById(R.id.left_txt);
        final ImageView nextTimeDot = nextTimeTimeLine.findViewById(R.id.dot_image);
        final TextView currentTemperature = view.findViewById(R.id.current_temperature);
        final TextView currentHumidity = view.findViewById(R.id.current_humidity);
        final RelativeLayout currentState = view.findViewById(R.id.current_state);

        //当前温湿度
        Bundle bundle = getArguments();
        if(bundle!= null)
        {
            currentHumidity.setText(""+bundle.getInt("humidity"));  //注意，如果直接传int会被当成resource ID 来用！
            currentTemperature.setText(""+bundle.getInt("temperature") + " ℃");
        }
        //设置时间轴：点的颜色，txt显示的文字，背景颜色
        nextTimeDot.setImageResource(R.drawable.prediction_dot);
        lastTimeTxt.setText("上次排尿时间：");
        nextTimeTxt.setText("预计下次排尿时间：");
        lastTimeTxt.setBackground(null);
        nextTimeTxt.setBackground(null);
        lastTime.setBackground(null);
        nextTime.setBackground(null);
        //-------------查数据库，显示上次和预计下次排尿时间--------------
        //创建数据库
        dbHelper = new MyDatabaseHelper(MyApplication.getContext(), Constant.DB_NAME, null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了
        //上次
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Cursor cursorPrediction = db.query(Constant.DB_PREDICTION_TABLE_NAME, null, null, null, null, null, "time desc");
        Cursor cursor = db.rawQuery("select count(id) , MAX(time) from " + Constant.DB_RECORD_TABLE_NAME ,null);
        if (cursor.moveToFirst()) {
            if(cursor.getInt(0) != 0) //如果有记录
            {
                long time = cursor.getLong(1);
                //long time = cursor.getLong(cursor.getColumnIndex("time"));  //这句话不行原因是 columeName 是MAX(time) 而不是 time
                Log.d(TAG, "time is " + time);
                Log.d(TAG, "onCreateView: column name: "+ cursor.getColumnName(0));
                lastTime.setText(DateTimeUtil.time2ShowString(time));
            }
            else
            {
                Log.d(TAG, "onCreateView: count is "+ cursor.getInt(0));
                Log.d(TAG, "onCreateView: max time is "+ cursor.getInt(1));
                lastTime.setText(R.string.no_data_yet_short);
            }

        }
        else
            Log.d(TAG, "初始化“上次排尿时间”时，查询数据库失败");
        cursor.close();
        //下次
        Cursor cursor1 = db.rawQuery("select count(id), MIN(time) from " + Constant.DB_PREDICTION_TABLE_NAME ,null);
        if (cursor1.moveToFirst()) {
            if(cursor1.getInt(1) !=0)  //如果有数据
            {
                long time = cursor1.getLong(1);
                Log.d(TAG, "onCreateView: column name: "+ cursor.getColumnName(0));
                Log.d(TAG, "time is " + time);
                nextTime.setText(DateTimeUtil.time2ShowString(time));
            }
            else
            {
                nextTime.setText(R.string.no_data_yet_short);
            }

        }
        else
            Log.d(TAG, "初始化“预计下次排尿时间”时，查询数据库失败");
        cursor1.close();
        //---------------------------------------------
        //更新温湿度ui的handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case (Constant.MSG_UPDATE_TEMPERATURE_HUMIDITY):{
                        currentTemperature.setText(""+ msg.arg1 + " ℃");
                        currentHumidity.setText(""+ msg.arg2 );
                        break;
                    }
                    case(Constant.MSG_PEE_HOME):{
                        lastTime.setText(DateTimeUtil.time2ShowString(((long[])msg.obj)[0]));
                        nextTime.setText(DateTimeUtil.time2ShowString(((long[])msg.obj)[1]));
                        break;
                    }
                    default:break;
                }

            }
        };

        //根据是否是省电模式来决定是否显示当前温湿度
        final Switch savePower = view.findViewById(R.id.save_power_home);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        savePower.setChecked(preferences.getBoolean("save_power",false));
        Log.d(TAG, "onCreateView: save_Power from preference+" + preferences.getBoolean("save_power",false));
        if(!savePower.isChecked())
        {
            //显示当前温湿度
            currentState.setVisibility(View.VISIBLE);
        }
        else
        {
            //不显示当前温湿度
            currentState.setVisibility(View.GONE);
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
                    currentState.setVisibility(View.GONE);

                }
                else{
                    editor.putBoolean("save_power",false);
                    //显示当前温湿度
                    currentState.setVisibility(View.VISIBLE);

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
