package com.tang.trade.tang.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.DownloadAddressModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.QRCodeUtil;

import java.util.HashMap;

import butterknife.BindView;

public class WeCharPublicNumberActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.image_vr)
    ImageView imageVr;


    @BindView(R.id.tv_node)
    TextView tvNode;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_dowmload_address;
    }

    @Override
    public void onClick(View v) {
        finish();

    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
    }

    @Override
    public void initData() {
        acceptantList();

    }

    private void acceptantList() {

        AcceptorApi.acceptantHttp(new HashMap<String, String>(), "url_weixin_mp", new JsonCallBack<DownloadAddressModel>(DownloadAddressModel.class) {
            @Override
            public void onSuccess(Response<DownloadAddressModel> response) {
                if (response.body().getStatus().equals("success")) {

                    Resources res = getResources();
                    Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.mine_borderless_vr_icon);
                    String strURl=response.body().getData().get(0).getUrl();
                    imageVr.setImageBitmap(QRCodeUtil.getLogoQRBitMap(strURl,bmp));

                }

                progressDialog.dismiss();
            }

            @Override
            public void onStart(Request<DownloadAddressModel, ? extends Request> request) {
                super.onStart(request);
                progressDialog.show();


            }

            @Override
            public void onError(Response<DownloadAddressModel> response) {
                super.onError(response);
              MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
            }
        });
    }
}
