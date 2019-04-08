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

public class SetupFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SetupFragment";
    public SetupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,String rootKey) {
        //getPreferenceManager().setSharedPreferencesName("mysetting");
        addPreferencesFromResource(R.xml.preference_settings);

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!SeekBarPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    //监听事件
    @Override
    public boolean onPreferenceTreeClick(Preference preference){
        switch (preference.getKey())
        {
            //设置铃声和音量是否可选
            case "ring" :{
                SwitchPreference ring = (SwitchPreference)findPreference("ring");
                ListPreference ringMusic = (ListPreference)findPreference("ring_music");
                SeekBarPreference ringVolume = (SeekBarPreference)findPreference("ring_volume");

                ringMusic.setEnabled(ring.isChecked());
                ringVolume.setEnabled(ring.isChecked());
                break;
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_setup, container, false);
//    }


}
