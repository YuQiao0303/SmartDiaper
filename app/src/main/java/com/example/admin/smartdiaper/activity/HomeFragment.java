package com.example.admin.smartdiaper.activity;

import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.constant.Constant;


public class HomeFragment extends Fragment{
    private static final String TAG="HomeFragment";
    public static Handler handler;  //处理BleService传来的更新温湿度ui消息

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
        Button rdmindTest = view.findViewById(R.id.send_notice);
        final TextView lastTime = view.findViewById(R.id.last_time);
        final TextView currentTemperature = view.findViewById(R.id.current_temperature);
        final TextView currentHumidity = view.findViewById(R.id.current_humidity);
        //更新温湿度ui的handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == Constant.MSG_UPDATE_TEMPERATURE_HUMIDITY)
                {
                    currentTemperature.setText("当前温度： "+ msg.arg1 + " ℃");
                    currentHumidity.setText("当前湿度： "+ msg.arg2 );
                }
            }
        };

        //省电模式
        final Switch savePower = view.findViewById(R.id.save_power_home);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        savePower.setChecked(preferences.getBoolean("save_power",false));
        Log.d(TAG, "onCreateView: save_Power from preference+" + preferences.getBoolean("save_power",false));
        savePower.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                if(savePower.isChecked())
                {
                    editor.putBoolean("save_power",true);
                }
                else{
                    editor.putBoolean("save_power",false);
                }
                editor.commit();
                Log.d(TAG, "onClick: save_power from home " + savePower.isChecked());
            }
        });
        return view;
    }
}
