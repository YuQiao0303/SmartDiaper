package com.example.admin.smartdiaper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.utils.DateTimeUtil;


import java.util.List;

/**
 * Created by wen on 2017/6/14.
 */

public class TimeAdapter extends RecyclerView.Adapter{
    private static final String TAG = "TimeAdapter";
    private List<TimelineItem> data;

    public TimeAdapter( List<TimelineItem> list) {
        this.data = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setPosition(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView rightTxt;

        private RelativeLayout rlTitle;
//        private View vLine;
//        private int position;
        private TimelineItem timelineItem;
        private ImageView dotImage;
        private TextView leftTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            rlTitle = (RelativeLayout) itemView.findViewById(R.id.rl_title);
//            vLine = itemView.findViewById(R.id.v_line);
            rightTxt = (TextView) itemView.findViewById(R.id.right_txt);
            leftTxt = itemView.findViewById(R.id.left_txt);
            dotImage = (ImageView)itemView.findViewById(R.id.dot_image);
        }


        public void setPosition(int position) {
//            this.position = position;
            timelineItem = data.get(position);

            //显示在左边还是右边
            if(position % 2 ==0)
            {
                leftTxt.setVisibility(View.GONE);
                rightTxt.setVisibility(View.VISIBLE);
                rightTxt.setText(DateTimeUtil.time2ShowString(timelineItem.getTime()));
            }
            else{
                rightTxt.setVisibility(View.GONE);
                leftTxt.setVisibility(View.VISIBLE);
                leftTxt.setText(DateTimeUtil.time2ShowString(timelineItem.getTime()));
            }


            //时间节点图片
            if(timelineItem.isPredicted())
                dotImage.setImageResource(R.drawable.prediction_dot);

        }
    }
}
