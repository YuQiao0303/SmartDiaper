package com.example.admin.smartdiaper.preference;


import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;

import android.support.v7.preference.ListPreference;

import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.admin.smartdiaper.R;



public class VolumeSettingPreference extends ListPreference{


    private TextView mTitle;
    private String mTitleText;
    private int mDefaultVolume;
    private String mSummaryText;
    private String mKey;
    private TextView mSummary;
    private Dialog mShowDialog;
    private SeekBar mSeekBar;

    /**
     *------------------ constructors-----------------
     *
     */
    public VolumeSettingPreference(Context context) {
        this(context,null);
    }

    public VolumeSettingPreference(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VolumeSettingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //这里可以定一些自定义的属性，这跟自定义View的套路是一样的,比如说获取到title之类的，默认的音量是多少
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.voice_style);
        mTitleText = typedArray.getString(R.styleable.voice_style_title);
        mDefaultVolume = typedArray.getIndex(R.styleable.voice_style_default_voice);
        mSummaryText = typedArray.getString(R.styleable.voice_style_summary);
        mKey = typedArray.getString(R.styleable.voice_style_key);
        //重要，把key给老爸
        super.setKey(mKey);
        typedArray.recycle();
    }


}