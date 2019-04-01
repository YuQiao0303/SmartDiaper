package com.example.admin.smartdiaper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import com.example.admin.smartdiaper.activity.HomeFragment;
import com.example.admin.smartdiaper.activity.SetupFragment;
import com.example.admin.smartdiaper.activity.TimeLineFragment;
import com.example.admin.smartdiaper.db.MyDatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MyDatabaseHelper dbHelper;
    private BleDevice bleDevice;//蓝牙设备
    private final static String LOG_TAG = "MainActivity";
    private View homeView, timelineView, setupView;
    private long mExitTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化数据库
        //initDatabase();
        //设置三个Fragment
        setView();
    }



    /**-----------------------------------------------------------------------
     *                       数据库相关
     *----------------------------------------------------------------------*/
    private void initDatabase(){
        //创建数据库
        dbHelper = new MyDatabaseHelper(this, "SmartDiaper.db", null, 1);
        dbHelper.getWritableDatabase();   //检测有没有该名字的数据库，若没有则创建，同时调用dbHelper 的 onCreate 方法；若有就不会再创建了
        //添加测试数据
        addTestData();
    }
    private void addTestData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();   //获得该数据库实例
        ContentValues values = new ContentValues();

//        values.put("name", "The Da Vinci Code");
//        values.put("author", "Dan Brown");
        db.insert("tbl_record",null,values);
        Log.d(TAG, "addTestData: 成功添加数据！");
    }

    /**-----------------------------------------------------------------------
     *                       设置Fragment相关
     *----------------------------------------------------------------------*/

    private void setView(){
        addFragment(new HomeFragment());//默认是homefragment
        homeView = findViewById(R.id.bottombar_home);
        timelineView = findViewById(R.id.bottombar_timeline);
        setupView = findViewById(R.id.bottombar_setup);
        homeView.setSelected(true);
        //点击事件：如果点击任何一个view,addFragment 并将其设为selected，其余设为非selected
        homeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: homeView");
                selected();
                homeView.setSelected(true);
                addFragment(new HomeFragment());
            }
        });

        timelineView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: timelineView");
                selected();
                timelineView.setSelected(true);
                addFragment(new TimeLineFragment());
            }
        });

        setupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: setupView");
                selected();
                setupView.setSelected(true);
                addFragment(new SetupFragment());
            }
        });
    }
    private void selected(){
        homeView.setSelected(false);
        timelineView.setSelected(false);
        setupView.setSelected(false);
    }

    public void addFragment(Fragment fragment) {
        //获取到FragmentManager，在V4包中通过getSupportFragmentManager，
        //在系统中原生的Fragment是通过getFragmentManager获得的。
        FragmentManager FMs = getSupportFragmentManager();
        //fig2.开启一个事务，通过调用beginTransaction方法开启。
        FragmentTransaction MfragmentTransactions = FMs.beginTransaction();
        //把自己创建好的fragment创建一个对象
        //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
        MfragmentTransactions.replace(R.id.main_content,fragment);
        //提交事务，调用commit方法提交。
        MfragmentTransactions.commit();
    }
}
