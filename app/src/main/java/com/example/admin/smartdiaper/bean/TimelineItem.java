package com.example.admin.smartdiaper.bean;

/**
 * 存储数据bena类
 * Created by wen on 2017/6/14.
 */

public class TimelineItem {

    private long time;
    private String title;

    private boolean predicted = false;

    public TimelineItem(long time, boolean predicted, String title) {
        this.title = title;
        this.predicted = predicted;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPredicted() {
        return predicted;
    }

    public void setPredicted(boolean predicted) {
        this.predicted = predicted;
    }

}
