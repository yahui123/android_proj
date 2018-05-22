package com.tang.trade.tang.aliui;

import android.support.v4.app.Fragment;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMConversationListOperation;
import com.alibaba.mobileim.conversation.YWConversation;
import com.tang.trade.tang.R;


/**
 * 最近会话界面的定制点(根据需要实现相应的接口来达到自定义会话列表界面)，不设置则使用openIM默认的实现
 * 调用方设置的回调，必须继承BaseAdvice 根据不同的需求实现 不同的 开放的 Advice
 * com.alibaba.mobileim.aop.pointcuts包下开放了不同的Advice.通过实现多个接口，组合成对不同的ui界面的定制
 * 这里设置了自定义会话的定制
 * 1.CustomConversationAdvice 实现自定义会话的ui定制
 * 2.CustomConversationTitleBarAdvice 实现自定义会话列表的标题的ui定制
 * <p/>
 * 另外需要在application中将这个Advice绑定。设置以下代码
 * AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_POINTCUT, CustomChattingAdviceDemo.class);
 *
 * @author jing.huai
 */
public class ConversationListOperationCustomSample extends IMConversationListOperation {

    public ConversationListOperationCustomSample(Pointcut pointcut) {
        super(pointcut);
    }


    /**
     * 返回自定义会话和群会话的默认头像 如返回本地的 R.drawable.test
     * 该方法只适用设置自定义会话和群会话的头像
     *
     * @param fragment
     * @param conversation
     * @return
     */
    @Override
    public int getConversationDefaultHead(Fragment fragment,
                                          YWConversation conversation) {
//        if (conversation.getConversationType() == YWConversationType.Custom) {
//            YWCustomConversationBody body = (YWCustomConversationBody) conversation.getConversationBody();
//            String conversationId = body.getIdentity();
//            if (conversationId.equals("sysTribe")) {
//                return R.mipmap.icon;
//            } else if (conversationId.equals("sysfrdreq")) {
//                return R.mipmap.icon;
//            } else {
//                return R.mipmap.icon;
//            }
//        } else if (conversation.getConversationType() == YWConversationType.Tribe){
//            return R.mipmap.icon;
//        }
        return  R.mipmap.icon;
    }



}
