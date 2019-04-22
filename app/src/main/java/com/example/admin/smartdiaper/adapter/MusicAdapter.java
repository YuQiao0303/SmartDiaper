package com.example.admin.smartdiaper.adapter;

import android.content.SharedPreferences;
import android.os.TestLooperManager;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RadioButton;


import com.example.admin.smartdiaper.MyApplication;
import com.example.admin.smartdiaper.R;

import com.example.admin.smartdiaper.activity.ChooseMusicActivity;
import com.example.admin.smartdiaper.constant.Constant;


import java.util.List;

/**
 * Created by wen on 2017/6/14.
 */

public class MusicAdapter extends RecyclerView.Adapter{
    private static final String TAG = "TimeAdapter";
    private List<Integer> data;
    int musicId;


    public MusicAdapter(List<Integer> list) {
        this.data = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final int mPosition = position;
        //显示铃声名称
        ((ViewHolder) holder).setPosition(position);
        //鼠标点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "onClick: position = " + position);
                ChooseMusicActivity.setChosenMusicId(position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RadioButton musicName;
//        private TextView rightTxt;
//        private RelativeLayout rlTitle;
//        private View vLine;
//        private int position;
//        private TimelineItem timelineItem;
//        private ImageView dotImage;
//        private TextView leftTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            musicName =  itemView.findViewById(R.id.music_name);

        }

        public void setPosition(int position) {
            musicId = data.get(position);
            musicName.setText(Constant.musicNames[musicId]);
            Log.d(TAG, "setPosition: musicId = "+ musicId + "musicName = " + Constant.musicNames[musicId]);
        }
    }
}
