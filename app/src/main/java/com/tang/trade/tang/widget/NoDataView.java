package com.tang.trade.tang.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.tang.trade.tang.R;


/**
 * Created by Administrator on 2017/6/8.
 */

public class NoDataView extends LinearLayout {

    private View view;
    public NoDataView(Context context) {
        super(context);

        view = View.inflate(context, R.layout.no_data_layout,null);

        addView(view);

    }

    public NoDataView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        view = View.inflate(context, R.layout.no_data_layout,null);

        addView(view);
    }


}
