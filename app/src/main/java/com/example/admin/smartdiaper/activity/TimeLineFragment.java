package com.example.admin.smartdiaper.activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.admin.smartdiaper.R;

import com.example.admin.smartdiaper.adapter.TimeAdapter;

import com.example.admin.smartdiaper.bean.TimelineItem;
import com.example.admin.smartdiaper.utils.TimeComparator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TimeLineFragment extends Fragment{
    private static final String TAG="TimeLineFragment";

    //存储列表数据
    List<TimelineItem> list = new ArrayList<>();
    TimeAdapter adapter;

    public TimeLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_timeline, container, false);
        //vivianTimeline(view);
        //time line
        RecyclerView rlView = view.findViewById(R.id.activity_rlview);

        //初始化数据
        initData();
        // 将数据按照时间排序
        TimeComparator comparator = new TimeComparator();
        Collections.sort(list, comparator);
        // recyclerview绑定适配器
        rlView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = new TimeAdapter(list);
        rlView.setAdapter(adapter);
        return view;

    }
    private void initData() {
        list.add(new TimelineItem("20170710", true,""));
        list.add(new TimelineItem("20140709", true,""));
        list.add(new TimelineItem("20140708", false,""));
        list.add(new TimelineItem("20140706", false,""));
    }

}
