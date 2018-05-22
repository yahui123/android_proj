package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/22.
 */

public class LanguageAdapter extends BaseAdapter {

    private Context context;
    private List<String> data = new ArrayList<String>();

    public LanguageAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        if (null!=data)
            return data.size();
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView==null)
        {
            convertView = View.inflate(context, R.layout.item_language_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvLanguage.setText(data.get(position));
        if (MyApp.get(BuildConfig.LANGUAGE, "").equals("")){
            String locale = context.getResources().getConfiguration().locale.getCountry();
            //繁体
            if (locale.equalsIgnoreCase("TW") || locale.equalsIgnoreCase("HK")){
                if (data.get(position).contains("繁體中文")){
                    viewHolder.ivLanguage.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.ivLanguage.setVisibility(View.GONE);
                }
            //简体
            }else if (locale.equalsIgnoreCase("CN")){
                if (data.get(position).contains("简体中文")){
                    viewHolder.ivLanguage.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.ivLanguage.setVisibility(View.GONE);
                }
            //英文
            }else{
                if (data.get(position).contains("English")){
                    viewHolder.ivLanguage.setVisibility(View.VISIBLE);

                }else{
                    viewHolder.ivLanguage.setVisibility(View.GONE);
                }
            }

        }else{
            if (MyApp.get(BuildConfig.LANGUAGE, "").equalsIgnoreCase(data.get(position))){
                viewHolder.ivLanguage.setVisibility(View.VISIBLE);
            }else{
                viewHolder.ivLanguage.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.iv_language)
        ImageView ivLanguage;

        @BindView(R.id.tv_language)
        TextView tvLanguage;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
