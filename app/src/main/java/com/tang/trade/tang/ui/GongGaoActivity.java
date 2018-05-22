package com.tang.trade.tang.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.GongGaoModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.MyProgressDialog;

import java.util.HashMap;

import butterknife.BindView;

public class GongGaoActivity extends BaseActivity {

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.loading)
    LoadingView loadingView;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_gong_gao;
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);


        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                //设定加载结束的操作\\
                loadingView.setVisibility(View.GONE);
            }
        });


        webView.loadUrl(MyApp.get("gaonGaoUrl",""));

    }



    @Override
    public void initData() {


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
