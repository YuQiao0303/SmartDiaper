package com.example.admin.smartdiaper.utils;

import com.example.admin.smartdiaper.bean.TimelineItem;

import java.util.Comparator;

/**
 * Created by wen on 2017/6/14.
 */

public class TimeComparator implements Comparator<TimelineItem> {
    @Override
    public int compare(TimelineItem td1, TimelineItem td2) {
        return td2.getTime().compareTo(td1.getTime());
    }
}
