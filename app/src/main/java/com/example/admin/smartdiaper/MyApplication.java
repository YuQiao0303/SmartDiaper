package com.example.admin.smartdiaper;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;
    /**
     * Application单例
     */
    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sInstance = this;
    }

    public static Context getContext(){
        return context;
    }

    /**
     * @return Application实例
     */
    public static MyApplication getInstance() {
        return sInstance;
    }


}
