package com.tang.trade.tang.aliui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingPageOperateion;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.fundamental.widget.WxAlertDialog;
import com.tang.trade.tang.R;

import java.util.ArrayList;

/**
 * 聊天界面(单聊和群聊界面)的定制点(根据需要实现相应的接口来达到自定义聊天界面)，不设置则使用openIM默认的实现
 * 1.CustomChattingTitleAdvice 自定义聊天窗口标题 2. OnUrlClickChattingAdvice 自定义聊天窗口中
 * 当消息是url是点击的回调。用于isv处理url的打开处理。不处理则用第三方浏览器打开 如果需要定制更多功能，需要实现更多开放的接口
 * 需要.继承BaseAdvice .实现相应的接口
 * <p/>
 * 另外需要在Application中绑定
 * AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT,
 * ChattingOperationCustomSample.class);
 *
 * @author jing.huai
 */
public class ChattingOperationCustomSample extends IMChattingPageOperateion {

    public ChattingOperationCustomSample(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 单聊ui界面，点击url的事件拦截 返回true;表示自定义处理，返回false，由默认处理
     *
     * @param fragment 可以通过 fragment.getActivity拿到Context
     * @param message  点击的url所属的message
     * @param url      点击的url
     */
    @Override
    public boolean onUrlClick(Fragment fragment, YWMessage message, String url,
                              YWConversation conversation) {
        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        fragment.getActivity().startActivity(intent);

        return true;
    }


    /**
     * 数字字符串点击事件,开发者可以根据自己的需求定制
     * @param activity
     * @param clickString 被点击的数字string
     * @param widget 被点击的TextView
     * @return false:不处理
     *         true:需要开发者在return前添加自己实现的响应逻辑代码
     */
    @Override
    public boolean onNumberClick(final Activity activity, final String clickString, final View widget) {

       if (clickString.length() == 11){
           ArrayList<String> menuList = new ArrayList<String>();
//           menuList.add("呼叫");
//           menuList.add("添加到手机通讯录");
//           menuList.add("复制到剪贴板");

           menuList.add(activity.getString(R.string.bds_call));
           menuList.add(activity.getString(R.string.bds_add_contact));
           menuList.add(activity.getString(R.string.bds_copy));

           final String[] items = new String[menuList.size()];
           menuList.toArray(items);
           Dialog alertDialog = new WxAlertDialog.Builder(activity)
                   .setItems(items, new DialogInterface.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialog,
                                           int which) {
                           if (TextUtils.equals(items[which], activity.getString(R.string.bds_call))) {
                               Intent intent = new Intent(Intent.ACTION_DIAL);
                               intent.setData(Uri.parse("tel:" + clickString));
                               activity.startActivity(intent);
                           } else if (TextUtils.equals(items[which], activity.getString(R.string.bds_add_contact))) {
                               Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                               intent.setType("vnd.android.cursor.item/person");
                               intent.setType("vnd.android.cursor.item/contact");
                               intent.setType("vnd.android.cursor.item/raw_contact");
                               intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, clickString);
                               activity.startActivity(intent);

                           } else if (TextUtils.equals(items[which], activity.getString(R.string.bds_copy))) {
                               ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                               clipboardManager.setText(clickString);
                           }
                       }
                   }).create();
           alertDialog.setCanceledOnTouchOutside(true);
           alertDialog.setCancelable(true);
           alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
               @Override
               public void onDismiss(DialogInterface dialog) {
                   widget.invalidate();
               }
           });
           if (!alertDialog.isShowing()) {
               alertDialog.show();
           }

           return true;
       }

      return false;
    }



    @Override
    public boolean onMessageLongClick(Fragment fragment, YWMessage message) {
        return true;
    }
}