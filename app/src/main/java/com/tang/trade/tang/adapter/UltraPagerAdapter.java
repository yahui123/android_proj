package com.tang.trade.tang.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tang.trade.tang.AppConfig;
import com.tang.trade.tang.ui.LeadActivity;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;

/**
 * Created by dagou on 2017/9/12.
 */

public class UltraPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    private int [] src ;
    Activity activity;
    public UltraPagerAdapter(boolean isMultiScr, int[] src, LeadActivity leadActivity) {
        this.isMultiScr = isMultiScr;
        this.src= src;
        this.activity = leadActivity;
    }

    @Override
    public int getCount() {
        return src.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        RelativeLayout relativeLayout = new RelativeLayout(container.getContext());
        ImageView mImageView = new ImageView(container.getContext());
        mImageView.setImageResource(src[position]);
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
        if (position == src.length - 1) {
            ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(container.getContext()).inflate(R.layout.activity_launch_pager,null);
            viewGroup.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    container.getContext().startActivity(new Intent(container.getContext(), MainActivity.class));
                    MyApp.set(AppConfig.IS_FIRST_COMING,false);
                    activity.finish();
                }
            });
            relativeLayout.addView(viewGroup,layoutParams);
        } else {

            relativeLayout.addView(mImageView,layoutParams);

        }


        container.addView(relativeLayout);


        return relativeLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        RelativeLayout view = (RelativeLayout) object;
        container.removeView(view);
    }
}
