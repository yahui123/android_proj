package com.tang.trade.tang.ui.service;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import com.tang.trade.tang.MyApp;

import java.util.ArrayList;
import java.util.List;


public class MyService extends Service {
    private String TAG="MyService";
    PowerManager.WakeLock mWakeLock;// 电源锁
    public List<Activity> activityList=new ArrayList<>();
    public static boolean isFirst=true;

    public static boolean needStopService;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
            }
        }
    };
    MyApp myApp=null;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          //  Log.e(TAG, "Thread");
//            Log.e("Myservice", "Runnable");
            myApp= (MyApp) MyService.this.getApplication();
            myApp.finishAllActivity();
            releaseWakeLock();
            MyService.this.stopSelf();
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e("Myservice", "onCreate");
        if (isFirst){
            myRegisterReceiver();
            handler.postDelayed(thread, 60000*5 );
            acquireWakeLock();
        }
        isFirst=false;

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //Log.e(TAG, "onStart");
    }

        @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY ;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.e(TAG, "onDestroy");
        releaseWakeLock();
        if (null!=oBaseActiviy_Broad){
            unregisterReceiver(oBaseActiviy_Broad);
        }


    }

    protected void myRegisterReceiver() {
        oBaseActiviy_Broad = new MyBaseActiviy_Broad();
        IntentFilter intentFilter = new IntentFilter("com.tang.trade.tang.ui.service.MyService");
      /*  intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);*/
        registerReceiver(oBaseActiviy_Broad, intentFilter);
    }


    private MyBaseActiviy_Broad oBaseActiviy_Broad;

    public class MyBaseActiviy_Broad extends BroadcastReceiver {

        public void onReceive(Context arg0, Intent intent) {
            //接收发送过来的广播内容
            handler.removeCallbacks(thread);
                MyService.this.stopSelf();
           // Log.e(TAG, "onReceive: ");
//            }
        }

    }

    private void acquireWakeLock()
    {
        if (null == mWakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"");
            if (null != mWakeLock)
            {
                mWakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
