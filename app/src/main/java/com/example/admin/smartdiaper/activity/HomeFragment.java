package com.example.admin.smartdiaper.activity;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.admin.smartdiaper.MainActivity;
import com.example.admin.smartdiaper.R;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;


public class HomeFragment extends Fragment{
    private static final String TAG="HomeFragment";
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //创建通知通道
        //这部分代码可以写在任何位置，只需要保证在通知弹出之前调用就可以了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            * 渠道ID可以随便定义，只要保证全局唯一性就可以。
            * 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
            * 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的。
            * */
            String channelId = "remind";
            String channelName = "排尿提醒";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "other";
            channelName = "其他消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button sendNotice = view.findViewById(R.id.send_notice);
        sendNotice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendNotification();
            }
        });
        return view;
    }

    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @param importance
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

        /*
        for fragment:  (NotificationManager) getActivity().getSystemService
        for activity:  (NotificationManager) getSystemService
         */
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 发送通知的代码
     */
    public void sendNotification() {
        //设置跳转到MainActivity 的 pendingintent，作为这条通知的点击事件
        Intent intent = new Intent(this.getContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this.getContext(),0,intent,0);

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this.getContext(), "remind")  //注意了这里需要一个channelId
                .setContentTitle("宝宝尿了~")
                .setContentText("可以更换纸尿裤了哦~")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis())
                /*
                这些内容加了也没用，得在channel中设置；channel中设置了不能改，因此决定用其他方式实现
                .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")))  //设置铃声,
                .setVibrate(new long[]{0, 1000, 1000, 1000})  //设置震动：静止0ms，震动1000ms，静止1000毫秒，震动1000毫秒，需要声明权限
                .setLights(Color.GREEN,1000,1000)  //设置前置led等：绿色，亮1000ms，灭1000ms
                */
                .setAutoCancel(true)    //点击通知自动取消
                .setContentIntent(pi)   //点击跳转

                .build();
        manager.notify(1, notification);
        Log.d(TAG, "sendNotification: ");
    }

}
