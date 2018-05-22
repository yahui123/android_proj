package com.tang.trade.tang.utils;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Administrator on 2018/2/26.
 */

public class ActivityManagerUrils {
    private static final String TAG = "ActivityManagerUrils";
    private static Stack<Activity> activityStack;
    private static ActivityManagerUrils instance;
    private Activity currActivity;
    private ActivityManagerUrils() {
    }

    public static ActivityManagerUrils getActivityManager() {
        if (instance == null) {
            instance = new ActivityManagerUrils();
        }
        return instance;
    }


    //退出栈顶Activity
    public void popActivity(Activity activity) {
        if(activity == null || activityStack == null) {
            return;
        }
        if(activityStack.contains(activity)) {
            activityStack.remove(activity);
        }
        currActivity = activity;
        //activity.finish();

        //activity = null;
    }

    public void destoryActivity(Activity activity) {
        if(activity == null ) {
            return;
        }
        activity.finish();
        if(activityStack.contains(activity)) {
            activityStack.remove(activity);
        }
        activity = null;
    }

    //获得当前栈顶Activity
    public Activity currentActivity() {
        if(activityStack == null ||activityStack.empty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    //将当前Activity推入栈中
    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    //退出栈中除指定的Activity外的所有
    public void popAllActivityExceptOne(Class cls) {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(cls)) {
                break;
            }
            destoryActivity(activity);
        }
    }

    //退出栈中所有Activity
    public void popAllActivity() {
        popAllActivityExceptOne(null);
    }

    public Activity getCurrentActivity()
    {
        return currActivity;
    }

    public int getActivityStackSize()
    {
        int size = 0;
        if (activityStack != null)
        {
            size = activityStack.size();
        }
        return size;
    }
}