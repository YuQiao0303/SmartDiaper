package com.example.admin.smartdiaper.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.smartdiaper.R;

import me.zhanghai.android.seekbarpreference.SeekBarPreference;

public class SetupFragment extends PreferenceFragmentCompat {

    public SetupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,String rootKey) {
        getPreferenceManager().setSharedPreferencesName("mysetting");
        addPreferencesFromResource(R.xml.preference_settings);

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!SeekBarPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference);
        }
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_setup, container, false);
//    }


}
