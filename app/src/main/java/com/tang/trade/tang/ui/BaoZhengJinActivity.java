package com.tang.trade.tang.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;

import butterknife.BindView;

public class BaoZhengJinActivity extends BaseActivity {

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.tv_quate_num)
    TextView tv_quate_num;

    @BindView(R.id.et_revalued_currency)
    EditText et_revalued_currency;

    @BindView(R.id.btn_submit)
    Button btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bao_zheng_jin;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
            finish();
        }else if (view.getId()==R.id.btn_submit){

        }

    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        btn_submit.setOnClickListener(this);

    }

    @Override
    public void initData() {

    }
}
