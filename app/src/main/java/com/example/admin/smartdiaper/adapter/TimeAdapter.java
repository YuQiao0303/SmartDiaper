package com.example.admin.smartdiaper.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.smartdiaper.R;
import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.utils.DensityUtil;
import com.example.admin.smartdiaper.utils.TimeFormat;

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

        private TextView txtDateTime;

        private RelativeLayout rlTitle;
        private View vLine;
        private int position;
        private TimelineItem timelineItem;
        private ImageView dotImage;

        public ViewHolder(View itemView) {
            super(itemView);
            rlTitle = (RelativeLayout) itemView.findViewById(R.id.rl_title);
            vLine = itemView.findViewById(R.id.v_line);
            txtDateTime = (TextView) itemView.findViewById(R.id.txt_date_time);

            dotImage = (ImageView)itemView.findViewById(R.id.dot_image);
        }

        public void setPosition(int position) {
            this.position = position;
            timelineItem = data.get(position);



            //时间节点图片
            if(timelineItem.isPredicted())
                dotImage.setImageResource(R.mipmap.time_node_red);

            //显示时间
            txtDateTime.setText(TimeFormat.format("yyyy.MM.dd", timelineItem.getTime()) );


            //竖线
            //时间轴竖线的layoutParams,用来动态的添加竖线
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vLine.getLayoutParams();
            //上下边缘均与rl_title 对齐
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.rl_title);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.rl_title);
            //设置margin
            int marginLeft = 20;
            if (position == 0) {  //第一条数据，去掉上方线段
                //打印信息-----------------------
                String message = "1 = "+ 1 + "\n"
                +"rlTitle.getHeight()/2" + rlTitle.getHeight()/2 + "\n"+
                        "DensityUtil.dip2px(vLine.getContext(), rlTitle.getHeight()/2)" +DensityUtil.dip2px(vLine.getContext(), rlTitle.getHeight()/2)+"\n";
                Log.d(TAG, message);
                //----------------------
                layoutParams.setMargins(DensityUtil.dip2px(vLine.getContext(), marginLeft), DensityUtil.dip2px(vLine.getContext(), 25), 0, 0);
            } else if (position == data.size() - 1) {  //最后一条数据，去掉下方线段
                layoutParams.setMargins(DensityUtil.dip2px(vLine.getContext(), marginLeft), 0, 0, DensityUtil.dip2px(vLine.getContext(), 25));
            }
            vLine.setLayoutParams(layoutParams);
        }
    }
}
