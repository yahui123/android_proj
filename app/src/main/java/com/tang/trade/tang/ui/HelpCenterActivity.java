package com.tang.trade.tang.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.data.model.entity.HelpCenter;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.GongGaoModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.widget.LoadingView;

import java.util.HashMap;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HelpCenterActivity extends BaseActivity {
    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.loading)
    LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_help_center;
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
                super.onPageFinished(view, url);
                loadingView.setVisibility(View.GONE);
            }

        });


        getURl();

    }

    private void getURl(){

        HttpDataManager.getInstance().getHelpCenter(new Observer<HelpCenter>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(HelpCenter helpCenter) {
                webView.loadUrl(helpCenter.getValue());
            }

            @Override
            public void onError(Throwable e) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
