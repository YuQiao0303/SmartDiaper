package com.example.admin.smartdiaper.bean;



public class TimelineItem {

    private  int id;
    private long time;
    private String title;

    private boolean predicted = false;

    public TimelineItem(int id, long time, boolean predicted, String title) {
        this.id = id;
        this.title = title;
        this.predicted = predicted;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
