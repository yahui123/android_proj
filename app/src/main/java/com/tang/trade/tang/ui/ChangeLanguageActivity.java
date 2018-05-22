package com.tang.trade.tang.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.LanguageAdapter;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

import static com.tang.trade.tang.MyApp.activityList;

public class ChangeLanguageActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.lv_language)
    ListView lvLanguage;

    private List<String> data = new ArrayList<String>();
    private LanguageAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back){
            finish();
        }
    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        adapter = new LanguageAdapter(this,data);
        lvLanguage.setAdapter(adapter);

        lvLanguage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyApp.set(BuildConfig.LANGUAGE, data.get(position));
                adapter.notifyDataSetChanged();
                if (data.get(position).contains("简体中文")){
                    settingLanguage(Locale.SIMPLIFIED_CHINESE);
                    LaunchActivity.lang = "zh_CN";
                }else if (data.get(position).contains("繁體中文")){
                    settingLanguage(Locale.TRADITIONAL_CHINESE);
                    LaunchActivity.lang = "zh_HK";
                }else if (data.get(position).contains("English")){
                    settingLanguage(Locale.ENGLISH);
                    LaunchActivity.lang = "en";
                }
                if (null!=MainActivity.mainActivity){
                    MainActivity.mainActivity.finish();
                }
                startActivity(new Intent(ChangeLanguageActivity.this,MainActivity.class));
                finish();

            }
        });
    }

    @Override
    public void initData() {
        setData();
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_change_language;
    }

    private void setData(){
        data.add("English");
        data.add("简体中文(Simplified Chinese)");
        data.add("繁體中文(Traditional Chinese)");
    }


    private void settingLanguage(Locale locale){
        Resources resources = getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 英文
        resources.updateConfiguration(config, dm);
    }
}
