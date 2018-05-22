package com.tang.trade.tang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tang.trade.tang.R;

/**
 * Created by Administrator on 2017/11/17.view_loading_layout
 */

public class MyListView extends ListView implements AbsListView.OnScrollListener {




    private View footer;
    // ListView中的list总数量
    private int totalItemCount=0;
    // ListView中最后一个list
    private int lastVisibleItem;
    private boolean isLoading;
    private TextView textView;
    private ProgressBar progressBar;
    private boolean isFlay=true;
    private Context context;
    private int indexSum=10;//分页加载每页的数量
    private int flag=0;    //并发控制标志位


    private ILoadListener iLoadListener;

    public MyListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context=context;
        initview(context);
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setIndexSum(int indexSum) {
        this.indexSum = indexSum;
    }

    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        this.context=context;
        initview(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initview(context);
        this.context=context;
    }

    // 用于加载底部布局页面
    public void initview(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        footer = inflater.inflate(R.layout.view_loading_layout,null );
        textView=(TextView) footer.findViewById(R.id.loading_tv_text);
        progressBar=(ProgressBar) footer.findViewById(R.id.loading_pb_loading);
        // 起始状态为隐藏
//        footer.findViewById(R.id.lsit).setVisibility(View.GONE);
//        this.addFooterView(footer);
        // 设置滚动监听
        this.setOnScrollListener(this);

    }

    // 加载数据完毕，isloading设为false，并且隐藏底部加载页面
    public void LoadComplete() {
        if (!isFlay){
            this.removeFooterView(footer);
            isFlay=true;
        }

    }
    public void LoadComplete2() {
        progressBar.setVisibility(GONE);
        textView.setText(context.getString(R.string.bds_nodate_data));
    }

    // 滚动状态发生变化
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        // 滚动状态停止，且最后一个item等于总共的item数量 item 全屏 总数量是index的整数倍
        if (lastVisibleItem == totalItemCount&&totalItemCount>0 && scrollState == SCROLL_STATE_IDLE&&isLoading&&flag==0) {

            if (isFlay){
                this.addFooterView(footer);
            }
            if (totalItemCount%indexSum==0){
                flag=1;
                progressBar.setVisibility(VISIBLE);
                textView.setText(context.getText(R.string.loading1));
                isFlay=false;
                iLoadListener.onLoad();
            }else {
                LoadComplete2();
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        this.lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
        Log.e("item2", "onScrollStateChanged: "+totalItemCount );
        if(totalItemCount > visibleItemCount){
            isLoading=true;

        }else{
            isLoading=false;
            //不满一屏
            LoadComplete();

        }



    }

    public void setInterface(ILoadListener iLoadlistener) {
        this.iLoadListener = iLoadlistener;
    }

    // 用于获得更多数据、更新ListView内容
    public interface ILoadListener {
        void onLoad();
    }

}

