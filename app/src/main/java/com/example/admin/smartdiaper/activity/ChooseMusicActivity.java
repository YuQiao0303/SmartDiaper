package com.example.admin.smartdiaper.activity;

import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.remind.Reminder;

public class ChooseMusicActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ChooseMusicActivity";

    static int chosenMusicId;
    private RadioGroup radioGroup;
    private boolean [] isPlaying = {false,false,false};

    ImageView play1 ;
    ImageView play2 ;
    ImageView play3 ;
    TextView okButton ;
    TextView cancleButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_music);

        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去除黑边儿
        //默认选中preference对应的音乐铃声
        radioGroup = findViewById(R.id.radio_group);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

        switch (preferences.getInt("my_ring_music", 0)){
            case(0):{
                radioGroup.check(R.id.music_name);
                break;
            }
            case(1):{
                radioGroup.check(R.id.music_name2);
                break;
            }
            case(2):{
                radioGroup.check(R.id.music_name3);
                break;
            }
        }

        //设置点击事件
        play1 = findViewById(R.id.play_pause);
        play1.setOnClickListener(this);

        play2 = findViewById(R.id.play_pause2);
        play2.setOnClickListener(this);

        play3 = findViewById(R.id.play_pause3);
        play3.setOnClickListener(this);

        okButton = (TextView) findViewById(R.id.ok_btn);
        okButton.setOnClickListener(this);

        cancleButton = (TextView) findViewById(R.id.cancel_btn);
        cancleButton.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause: {
                if(isPlaying[0] == false)
                {
                    Reminder.ring(0,100);
                    isPlaying[0] = true;
                    play1.setImageResource(R.mipmap.ic_pause);
                }
                else{
                    Reminder.pauseRing();
                    isPlaying[0] = false;
                    play1.setImageResource(R.mipmap.ic_play);
                }
                break;
            }
            case R.id.play_pause2:{
                if(isPlaying[1] == false)
                {
                    Reminder.ring(1,100);
                    isPlaying[1] = true;
                    play2.setImageResource(R.mipmap.ic_pause);
                }
                else{
                    Reminder.pauseRing();
                    isPlaying[1] = false;
                    play2.setImageResource(R.mipmap.ic_play);
                }
                break;
            }
            case R.id.play_pause3:{
                if(isPlaying[2] == false)
                {
                    Reminder.ring(2,100);
                    isPlaying[2] = true;
                    play3.setImageResource(R.mipmap.ic_pause);
                }
                else{
                    Reminder.pauseRing();
                    isPlaying[2] = false;
                    play3.setImageResource(R.mipmap.ic_play);
                }
                break;
            }
            case R.id.ok_btn: {
                Log.d(TAG, "onClick: chosen id = " + chosenMusicId);
                //获得所选的music id
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case (R.id.music_name): {
                        chosenMusicId = 0;
                        break;
                    }
                    case (R.id.music_name2): {
                        chosenMusicId = 1;
                        break;
                    }
                    case (R.id.music_name3): {
                        chosenMusicId = 2;
                        break;
                    }
                }
                //更改preference
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("my_ring_music", chosenMusicId);
                editor.commit();
                finish();
                Log.d(TAG, "onClick: 更改preferebce");
                Log.d(TAG, "onClick: id = " + preferences.getInt("my_ring_music", 0));

                //关闭音乐
                Reminder.stopRing();
                break;
            }
            case R.id.cancel_btn:
                //关闭音乐
                Reminder.stopRing();
                finish();
                break;
            default:
                break;
        }
    }
}
