package com.tang.trade.tang.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;

import butterknife.BindView;

public class ContactWayActivity extends BaseActivity {

    @BindView(R.id.et_contact_way)
    EditText et_contact_way;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_save)
    TextView tv_save;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_contact_way;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:
                if (TextUtils.isEmpty(et_name.getText().toString())){
                    MyApp.showToast(getString(R.string.name_err));
                    return;
                }

                if (TextUtils.isEmpty(et_contact_way.getText().toString())){
                    MyApp.showToast(getString(R.string.contact_err));
                    return;
                }
                break;

            case R.id.iv_back:
                finish();
                break;

        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        tv_save.setOnClickListener(this);

    }

    @Override
    public void initData() {

    }
}
