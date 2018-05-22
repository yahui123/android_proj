package com.tang.trade.module.register.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.R;

import butterknife.BindView;

/**
 * 用户协议
 */
public class ProtocolActivity extends AbsMVPActivity<CreateUserContract.Presenter> {

    //    private final String URL = Const.HTTP_BASE_URL + "userProtocol.html";
    private final String URL = "http://api2.borderless.vip:8092/v1/protocol/user.html";
    @BindView(R.id.wv_protocol)
    WebView wvProtocol;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ProtocolActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_400);
        setupDefultToolbar(getString(R.string.title_protocol_400));
        setToolbarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));

        wvProtocol.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        wvProtocol.loadUrl(URL);
    }

    @Override
    public void getIntentValue() {

    }

    @Override
    public CreateUserContract.Presenter getPresenter() {
        return null;
    }

    @Override
    public void onError(DataError error) {

    }
}
