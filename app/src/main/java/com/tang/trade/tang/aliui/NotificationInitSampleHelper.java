package com.tang.trade.tang.aliui;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMNotification;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;

/**
 * 通知栏的一些自定义设置
 * @author zhaoxu
 *
 */
public class NotificationInitSampleHelper extends IMNotification {

    private static boolean mNeedQuiet;
    private static boolean mNeedVibrator = true;
    private static boolean mNeedSound = true;


    public NotificationInitSampleHelper(Pointcut pointcut) {
        super(pointcut);
    }


    public void setNeedQuiet(boolean needQuiet){
        mNeedQuiet = needQuiet;
    }

    public void setNeedVibrator(boolean needVibrator){
        mNeedVibrator = needVibrator;
    }

    public void setNeedSound(boolean needSound){
        mNeedSound = needSound;
    }

    /**
     * 是否开启免打扰模式，若开启免打扰模式则收到新消息时不发送通知栏提醒，只在会话列表页面显示未读数
     * 若开启免打扰模式，则声音提醒和震动提醒会失效，即收到消息时不会有震动和提示音
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true:开启， false：不开启
     */
    @Override
    public boolean needQuiet(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return true;
//        }
        return false;
    }

    /**
     * 收到通知栏消息时是否震动提醒，该设置在没有开启免打扰模式的情况下才有效
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true：震动，false：不震动
     */
    @Override
    public boolean needVibrator(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return false;
//        }
        return false;
    }


    /**
     * 收到通知栏消息时是否有声音提醒，该设置在没有开启免打扰模式的情况下才有效
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true：有提示音，false：没有提示音
     */
    @Override
    public boolean needSound(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return false;
//        }
        return mNeedSound;
    }


    /**
     * 返回自定义提示音资源Id
     * @return  提示音资源Id，返回0则使用SDK默认的提示音
     */
    @Override
    public int getNotificationSoundResId() {
        return 0;
    }

}
