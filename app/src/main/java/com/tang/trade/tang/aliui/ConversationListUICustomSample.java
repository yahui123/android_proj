package com.tang.trade.tang.aliui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMConversationListUI;
import com.alibaba.mobileim.channel.util.AccountUtils;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.alibaba.mobileim.conversation.YWTribeConversationBody;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.mobileim.utility.IMSmilyCache;
import com.alibaba.mobileim.utility.IMUtil;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.SPUtils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.mobileim.YWChannel.getResources;


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
public class ConversationListUICustomSample extends IMConversationListUI {

    private ImageView tv_into_ContactListActivity,iv_add;
    private RelativeLayout customView;
    private View popupView;
    private PopupWindow window;
    private TextView tvAdd,tvSao,tvYaoqing,tvCode;
    private ImageView iv_dian;

    public YWIMKit mIMKit;
    public String userid;

    private Map<String, CharSequence> mSmilyContentCache = new HashMap<String, CharSequence>();  //表情的本地缓存，加速读取速度用
    IMSmilyCache smilyManager;
    int defaultSmilySize = 0;
    private int contentWidth;

    public ConversationListUICustomSample(Pointcut pointcut) {
        super(pointcut);
    }


    /**
     * 返回会话列表的自定义标题
     *
     * @param fragment
     * @param context
     * @param inflater
     * @return
     */
    @Override
    public View getCustomConversationListTitle(final Fragment fragment,
                                               final Context context, LayoutInflater inflater) {
        //TODO 重要：必须以该形式初始化customView---［inflate(R.layout.**, new RelativeLayout(context),false)］------，以让inflater知道父布局的类型，否则布局xml**中定义的高度和宽度无效，均被默认的wrap_content替代
        customView = (RelativeLayout) inflater
                .inflate(R.layout.demo_custom_conversation_title_bar, new RelativeLayout(context),false);
//        init();
        ImageView iv_back = (ImageView) customView.findViewById(R.id.iv_back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.getActivity().finish();
            }
        });
        return customView;
    }

    private void init()
    {

//        popupView = View.inflate(activity,R.layout.popupwindow_layout1,null);
//        tv_into_ContactListActivity = (ImageView) customView.findViewById(R.id.tv_into_ContactListActivity);
//        iv_dian = (ImageView) customView.findViewById(R.id.iv_dian);
//        iv_add = (ImageView) customView.findViewById(R.id.iv_add);
//        tvAdd = (TextView) popupView.findViewById(R.id.tv_add);
//        tvSao = (TextView) popupView.findViewById(R.id.tv_saoyisao);
//        tvYaoqing = (TextView) popupView.findViewById(R.id.tv_yaoqing);
//        tvCode = (TextView) popupView.findViewById(R.id.tv_mycode);


//        tv_into_ContactListActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                activity.startActivity(new Intent(activity,ContactListActivity.class));
//            }
//        });
//
//        iv_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                show();
//
//            }
//        });
//
//        tvAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                activity.startActivity(new Intent(activity,AddContactActivity.class));
//                window.dismiss();
//
//            }
//        });
//        tvSao.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                activity.startActivityForResult(new Intent(activity, CaptureActivity.class), 0);
//                window.dismiss();
//            }
//        });
//        tvYaoqing.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                activity.startActivity(new Intent(activity,NewShareActivity.class));
//                window.dismiss();
//
//            }
//        });
//
//        tvCode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                activity.startActivity(new Intent(activity,MyQrCode.class));
//                window.dismiss();
//
//            }
//        });
    }


//    private void show() {
//        //创建PopupWindow对象，指定宽度和高度
//        window = new PopupWindow(popupView,  WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT);
//        //设置动画
//        window.setAnimationStyle(R.style.popup_window_anim1);
//        //设置背景颜色
//        Drawable drawable = getResources().getDrawable(R.mipmap.xiala1);
//        window.setBackgroundDrawable(drawable);
//        // 设置可以获取焦点
//        window.setFocusable(true);
//        //设置可以触摸弹出框以外的区域
//        window.setOutsideTouchable(true);
//        // 更新popupwindow的状态
//        window.update();
//
//        //以下拉的方式显示，并且可以设置显示的位置
//        window.showAsDropDown(iv_add, -20,0);
//
//    }


    /**
     * 申请数据
     */
//    public void getAskFors() {
//
//        HttpConnect.post(activity,"member_friends_list_apply" , null,
//                new Callback() {
//
//                    @Override
//                    public void onResponse(Response arg0)
//                            throws IOException {
//
//                        final JSONObject object = JSONObject.fromObject(arg0
//                                .body().string());
//                        if (object.get("status").equals("success")) {
//
//                            final JSONArray array = object
//                                    .optJSONArray("data");
//                            activity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (array.size() > 0) {
//
//                                        for (int i = 0; i < array.size(); i++) {
//                                            if (array.getJSONObject(i).optString("status").equals("接受")){
//                                                iv_dian.setVisibility(View.VISIBLE);
//                                                return;
//                                            }else{
//                                                iv_dian.setVisibility(View.GONE);
//                                                return;
//                                            }
//                                        }
//
//                                    }else{
//                                        iv_dian.setVisibility(View.GONE);
//                                    }
//                                }
//                            });
//
//                        }else{
//                            activity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    iv_dian.setVisibility(View.GONE);
//                                }
//                            });
//                        }
//
//                    }
//
//
//                    @Override
//                    public void onFailure(Request arg0, IOException arg1) {
//
//
//                        Handler dataHandler = new Handler(
//                                activity.getContext().getMainLooper()) {
//
//                            @Override
//                            public void handleMessage(
//                                    final Message msg) {
//                                iv_dian.setVisibility(View.GONE);
////                                activity.sToast("链接超时！");
//                            }
//                        };
//                        dataHandler.sendEmptyMessage(0);
//                    }
//                });
//
//    }


    /**
     * 该方法可以构造一个会话列表为空时的展示View
     * @return
     *      empty view
     */
    @Override
    public View getCustomEmptyViewInConversationUI(Context context) {
        /** 以下为示例代码，开发者可以按需返回任何view*/
        TextView textView = new TextView(context);
        textView.setText(context.getString(R.string.bds_previous_messages));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        return textView;
    }

    /**
     * 搜索框
     * @param fragment
     * @return
     */
    @Override
    public boolean enableSearchConversations(Fragment fragment){
        return true;
    }



    private final int[] viewTypeArray = {0,1,2}; //这样就有2种自定义View
    /**
     * 自定义item view的种类数
     * @return 种类数
     */
    @Override
    public int getCustomItemViewTypeCount() {
        return viewTypeArray.length;
    }

    /**
     * 自定义item的viewType
     * @param conversation
     * @return viewType
     */
    @Override
    public int getCustomItemViewType(YWConversation conversation) {
        if (conversation.getConversationType() == YWConversationType.Tribe) {
            return viewTypeArray[0];
        } else if (conversation.getConversationType() == YWConversationType.P2P){
            return viewTypeArray[1];
        } else if (conversation.getConversationType() == YWConversationType.SHOP){
            return viewTypeArray[2];
        }
        //这里必须调用基类方法返回！！
        return super.getCustomItemViewType(conversation);
    }


    /**
     * 根据viewType自定义item的view
     * @param fragment
     * @param conversation      当前item对应的会话
     * @param convertView       convertView
     * @param viewType          当前itemView的viewType
     * @param headLoadHelper    加载头像管理器，用户可以使用该管理器设置头像
     * @param parent            getView中的ViewGroup参数
     * @return
     */
    @Override
    public View getCustomItemView(Fragment fragment, YWConversation conversation, View convertView, int viewType, YWContactHeadLoadHelper headLoadHelper, ViewGroup parent) {
            ViewHolder2 holder = null;
            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
                holder = new ViewHolder2();
                convertView = inflater.inflate(R.layout.demo_custom_conversation_item_1, parent, false);
                holder.head = (ImageView) convertView.findViewById(R.id.head);
                holder.unread = (TextView) convertView.findViewById(R.id.unread);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.atMsgNotify = (TextView) convertView.findViewById(R.id.at_msg_notify);
                holder.draftNotify = (TextView) convertView.findViewById(R.id.at_msg_notify);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder2)convertView.getTag();
            }

        userid = SPUtils.getString(Const.USERNAME,"");
        mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);
        /**
         * 群
         */
        if (viewType == viewTypeArray[0]) {
            holder.unread.setVisibility(View.GONE);
            int unreadCount = conversation.getUnreadCount();
            if (unreadCount > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                if (unreadCount > 99){
                    holder.unread.setText("99+");
                }else {
                    holder.unread.setText(String.valueOf(unreadCount));
                }
            }

            //昵称
            YWTribeConversationBody body = (YWTribeConversationBody) conversation.getConversationBody();
            YWTribe tribe = body.getTribe();
            String name = tribe.getTribeName();
            holder.name.setText(name);
            /**
             * !!!注:这里是给群回话设置头像,这里直接设置的默认头像
             * 如果想自由设置可以使用{@link YWContactHeadLoadHelper#setCustomHeadView(ImageView, int, String)},
             * 该方法的第三个参数为加载头像的地址:可以是资源Id或者是sdcard上的file绝对路径以及网络url
             */
            if (viewType == viewTypeArray[0]) {
                headLoadHelper.setTribeDefaultHeadView(holder.head);
            } else {
                headLoadHelper.setRoomDefaultHeadView(holder.head);
            }

            //是否支持群@消息提醒
            boolean isAtEnalbe = YWAPI.getYWSDKGlobalConfig().enableTheTribeAtRelatedCharacteristic();
            if (isAtEnalbe){
                //文案修改为@消息提醒
                holder.atMsgNotify.setText(R.string.aliwx_at_msg_notify);
                if (conversation.hasUnreadAtMsg()) {
                    holder.atMsgNotify.setVisibility(View.VISIBLE);
                } else {
                    holder.atMsgNotify.setVisibility(View.GONE);
                }
            } else {
                holder.atMsgNotify.setVisibility(View.GONE);
            }

            //内容
            String content = conversation.getLatestContent();
            boolean isDraftEnable = YWAPI.getYWSDKGlobalConfig().enableConversationDraft();
            //没有开启@消息开关或者@消息提醒不可见,说明此时没有@消息,检查是否有草稿
            if(isDraftEnable) {
                if (!isAtEnalbe || (holder.atMsgNotify != null && holder.atMsgNotify.getVisibility() != View.VISIBLE)) {
                    if (conversation.getConversationDraft() != null
                            && !TextUtils.isEmpty(conversation.getConversationDraft().getContent())) {
                        //文案修改为草稿提醒
                        holder.draftNotify.setText(R.string.aliwx_draft_notify);
                        content = conversation.getConversationDraft().getContent();
                        holder.draftNotify.setVisibility(View.VISIBLE);
                    } else {
                        holder.draftNotify.setVisibility(View.GONE);
                    }
                }
            }
            holder.content.setText(content);
            setSmilyContent(fragment.getActivity(), content, holder);
            holder.time.setText(IMUtil.getFormatTime(conversation.getLatestTimeInMillisecond(), mIMKit.getIMCore().getServerTime()));
            /**
             * 单聊
             */
        }else if (viewType == viewTypeArray[1]){
            /**
             * 头像
             */
            headLoadHelper.setHeadView(holder.head, conversation);

            //未读消息
            holder.unread.setVisibility(View.GONE);
            int unreadCount = conversation.getUnreadCount();
            if (unreadCount > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                if (unreadCount > 99){
                    holder.unread.setText("99+");
                }else {
                    holder.unread.setText(String.valueOf(unreadCount));
                }
            }

            holder.atMsgNotify.setVisibility(View.GONE);


            String content = conversation.getLatestContent();

            String title = null;


            YWP2PConversationBody conversationBody = (YWP2PConversationBody) conversation
                    .getConversationBody();

            if (!TextUtils.isEmpty(conversationBody.getContact().getShowName())) {
                title = conversationBody.getContact().getShowName();
            } else {

                IYWContact contact = mIMKit.getContactService().getContactProfileInfo(conversationBody.getContact().getUserId(), conversationBody.getContact().getAppKey());
                //生成showName，According to id。
                if (contact != null && !TextUtils.isEmpty(contact.getShowName())) {
                    title = contact.getShowName();
                }
            }
            //如果标题为空，那么直接使用Id
            if (TextUtils.isEmpty(title)) {
                title = conversationBody.getContact().getUserId();
            }


            holder.name.setText(title);

            boolean isDraftEnable = YWAPI.getYWSDKGlobalConfig().enableConversationDraft();
            //没有开启@消息开关或者@消息提醒不可见,说明此时没有@消息,检查是否有草稿
            if(isDraftEnable) {

                if (conversation.getConversationDraft() != null
                        && !TextUtils.isEmpty(conversation.getConversationDraft().getContent())) {
                    //文案修改为草稿提醒
                    holder.draftNotify.setText(R.string.aliwx_draft_notify);
                    content = conversation.getConversationDraft().getContent();
                    holder.draftNotify.setVisibility(View.VISIBLE);
                } else {
                    holder.draftNotify.setVisibility(View.GONE);
                }

            }
            //最后一条消息
            holder.content.setText(content);
            setSmilyContent(fragment.getActivity(), content, holder);
            //时间
            holder.time.setText(IMUtil.getFormatTime(conversation.getLatestTimeInMillisecond(), mIMKit.getIMCore().getServerTime()));
            /**
             * 客服
             */
        }else if (viewType == viewTypeArray[2]){

            headLoadHelper.setHeadView(holder.head, conversation);
            holder.name.setText(AccountUtils.getShortUserID(conversation.getConversationId()));

            holder.unread.setVisibility(View.GONE);
            int unreadCount = conversation.getUnreadCount();
            if (unreadCount > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                if (unreadCount > 99){
                    holder.unread.setText("99+");
                }else {
                    holder.unread.setText(String.valueOf(unreadCount));
                }
            }

            holder.atMsgNotify.setVisibility(View.GONE);

            String content = conversation.getLatestContent();
            boolean isDraftEnable = YWAPI.getYWSDKGlobalConfig().enableConversationDraft();
            //没有开启@消息开关或者@消息提醒不可见,说明此时没有@消息,检查是否有草稿
            if(isDraftEnable) {

                if (conversation.getConversationDraft() != null
                        && !TextUtils.isEmpty(conversation.getConversationDraft().getContent())) {
                    //文案修改为草稿提醒
                    holder.draftNotify.setText(R.string.aliwx_draft_notify);
                    content = conversation.getConversationDraft().getContent();
                    holder.draftNotify.setVisibility(View.VISIBLE);
                } else {
                    holder.draftNotify.setVisibility(View.GONE);
                }

            }
            holder.content.setText(content);
            setSmilyContent(fragment.getActivity(), content, holder);
            holder.time.setText(IMUtil.getFormatTime(conversation.getLatestTimeInMillisecond(), mIMKit.getIMCore().getServerTime()));
        }


            return convertView;
    }


    private void setSmilyContent(Context context, String content, ViewHolder2 holder){
        initSmilyManager(context);
        if (content == null || holder.content.getPaint() == null) {
            CharSequence charSequence = mSmilyContentCache.get(content);
            if (charSequence != null) {
                holder.content.setText(charSequence);
            } else {
                CharSequence smilySpanStr = smilyManager.getSmilySpan(context,
                        content, defaultSmilySize, false);
                mSmilyContentCache.put(content, smilySpanStr);
                holder.content.setText(smilySpanStr);
            }
        } else {
            CharSequence charSequence = mSmilyContentCache.get(content);
            if (charSequence != null) {
                holder.content.setText(charSequence);
            } else {
                CharSequence text = TextUtils.ellipsize(content,
                        holder.content.getPaint(), contentWidth,
                        holder.content.getEllipsize());
                CharSequence smilySpanStr = smilyManager.getSmilySpan(context,
                        String.valueOf(text), defaultSmilySize, false);
                mSmilyContentCache.put(content, smilySpanStr);
                holder.content.setText(smilySpanStr);
            }
        }
    }



    private void initSmilyManager(Context context){
        if (smilyManager == null){
            smilyManager = IMSmilyCache.getInstance();
            defaultSmilySize = (int) context.getResources().getDimension(R.dimen.aliwx_smily_column_width);
            int width = context.getResources().getDisplayMetrics().widthPixels;
            contentWidth = width
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_column_up_unit_margin)*2
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_common_head_size)
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_message_content_margin_right);
        }
    }

    public class ViewHolder2{
        ImageView head;
        TextView unread;
        TextView name;
        TextView content;
        TextView atMsgNotify;
        TextView draftNotify;
        TextView time;
    }
}
