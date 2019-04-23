package com.example.admin.smartdiaper.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.ble.BleConnectService;
import com.example.admin.smartdiaper.constant.Constant;

import me.zhanghai.android.seekbarpreference.SeekBarPreference;

public class SetupFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SetupFragment";
    SwitchPreference ring;
    Preference ringMusic;
    SeekBarPreference ringVolume;
    SwitchPreference savePower;
    Preference bleStatus;
    Preference bleReconnect;

    public static Handler handler;

    public SetupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,String rootKey) {
        Log.d(TAG, "onCreatePreferences: ");
        addPreferencesFromResource(R.xml.preference_settings);

        ring = (SwitchPreference)findPreference("ring");
        ringMusic = (Preference)findPreference("my_ring_music");
        ringVolume = (SeekBarPreference)findPreference("ring_volume");
        savePower = (SwitchPreference)findPreference("save_power");
        bleStatus = findPreference("ble_status");
        bleReconnect = findPreference("ble_reconnect");


    }

    /**
     * 针对seekBarPreference 做专门处理
     * @param preference
     */
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!SeekBarPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    /**
     * 鼠标单击事件
     * 重连蓝牙
     * listview可以播放音乐
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference){
        switch (preference.getKey())
        {

            case "ble_reconnect" :{
                Message msg = new Message();
                msg.what = Constant.MSG_RECONNET;
                MainActivity.handler.sendMessage(msg);
                break;
            }
            case "my_ring_music" :{
                //弹出选择铃声对话框
                Intent intent = new Intent(MyApplication.getContext(), ChooseMusicActivity.class);
                startActivity(intent);
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    /**
     * 设置更改事件
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: key = " + key);
        setAll();

        if(key.equals("save_power") )
        {

            Message msg = new Message();
            msg.what = Constant.MSG_SET_MODE;
            MainActivity.handler.sendMessage(msg);
        }

    }

    /**
     * 设置铃声和音量是否可选
     */
    public void setAll(){
        //setEnable
        ringMusic.setEnabled(ring.isChecked());
        ringVolume.setEnabled(ring.isChecked());

        //setSummary

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        switch(preferences.getInt("my_ring_music",0)){
            case(0):{
                ringMusic.setSummary(R.string.ring_music1);
                break;
            }
            case(1):{
                ringMusic.setSummary(R.string.ring_music2);
                break;
            }
            case(2):{
                ringMusic.setSummary(R.string.ring_music3);
                break;
            }
        }

        ringVolume.setSummary(ringVolume.getProgress()+"%");
    }

    @Override
    public void onResume() {
        super.onResume();
        setAll();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
