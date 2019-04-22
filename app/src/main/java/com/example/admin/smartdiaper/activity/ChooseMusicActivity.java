package com.example.admin.smartdiaper.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.adapter.MusicAdapter;
import com.example.admin.smartdiaper.adapter.TimeAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseMusicActivity extends AppCompatActivity {

    private static final String TAG = "ChooseMusicActivity";
    //存储列表数据
    static List<Integer> list = new ArrayList<>();
    static MusicAdapter adapter;
    static int chosenMusicId;

    public static void setChosenMusicId(int id){chosenMusicId = id;}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_music);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去除黑边儿

        //time line
        RecyclerView recyclerView = findViewById(R.id.music_recycler_view);
        //初始化数据
        list.clear();
        list.add(0);
        list.add(1);
        list.add(2);

        // recyclerview绑定适配器
        recyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new MusicAdapter(list);
        recyclerView.setAdapter(adapter);


        //确认和取消按钮
        //ok
        TextView okButton = (TextView) findViewById(R.id.ok_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: chosen id = "+ chosenMusicId);
                //更改preference
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("my_ring_music",chosenMusicId);
                editor.commit();
                finish();

            }
        });

        //cancel
        TextView cancleButton = (TextView) findViewById(R.id.cancel_btn);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
