package com.example.admin.smartdiaper.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.smartdiaper.R;

import me.zhanghai.android.seekbarpreference.SeekBarPreference;

public class SetupFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SetupFragment";
    SwitchPreference ring;
    ListPreference ringMusic;
    SeekBarPreference ringVolume;
    public SetupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,String rootKey) {
        Log.d(TAG, "onCreatePreferences: ");
        addPreferencesFromResource(R.xml.preference_settings);

        ring = (SwitchPreference)findPreference("ring");
        ringMusic = (ListPreference)findPreference("ring_music");
        ringVolume = (SeekBarPreference)findPreference("ring_volume");

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
     * listview可以播放音乐
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference){
        switch (preference.getKey())
        {

            case "ring" :{
                //播放音乐
                break;
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

    }

    /**
     * 设置铃声和音量是否可选
     */
    public void setAll(){
        //setEnable
        ringMusic.setEnabled(ring.isChecked());
        ringVolume.setEnabled(ring.isChecked());

        //setSummary
        ringMusic.setSummary(ringMusic.getEntry());
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
