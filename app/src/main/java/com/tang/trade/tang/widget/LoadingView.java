package com.tang.trade.tang.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tang.trade.tang.R;

import static com.tang.trade.tang.R.id.rl_load;
import static com.tang.trade.tang.R.id.tv_no_more_data;

/**
 * Created by Administrator on 2017/6/8.
 */

public class LoadingView extends LinearLayout {

    private View view;
    private LinearLayout relativeLayout;
    private TextView textView;
    public LoadingView(Context context) {
        super(context);

        view = View.inflate(context, R.layout.loading_more_layout,null);
        relativeLayout = view.findViewById(rl_load);
        textView = view.findViewById(tv_no_more_data);
        addView(view);

    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        view = View.inflate(context, R.layout.loading_more_layout,null);
        relativeLayout = view.findViewById(rl_load);
        textView = view.findViewById(tv_no_more_data);
        addView(view);
    }

    public void loadMore(){
        relativeLayout.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
    }

    public void noMoreData(String str){
        relativeLayout.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText(str);
    }
}
