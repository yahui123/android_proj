package com.tang.trade.tang.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.AcceptorCZorTXAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AccTranResponseModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.net.model.RechargeResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.MoneyUtil;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.tang.widget.QRDialog;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class AccOrderActivity extends BaseActivity implements AcceptorCZorTXAdapter.MyOnClickListener{

    private String TYPE_CI="CI";//充值记录
    private String TYPE_CO="CO";//提现记录
    private String type="";
    private String bdsAccount="";

    private static final int TYPE_SUCCESS=0;//成功
    private static final int TYPE_FAILURE=1;//failure
    private static final int TYPE_ERROR=2;//error

    private String TYPE_NA="NA";//未激活
    private String TYPE_A="A";//激活
    private String TYPE_PAY="PAY";//已转账
    private String TYPE_REC="REC";//已收款
    private String TYPE_COM="COM";//完成
    private String TYPE_CAN="CAN";//取消

    private boolean isUpdate=false;

    AcceptorCZorTXAdapter adapter;


    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.lv_acceptors)
    ListView lv_acceptors;


    @BindView(R.id.btn_summit)
    Button btn_summit;

    @BindView(R.id.btn_cancel)
    Button btn_cancel;

    @BindView(R.id.loading)
    LoadingView loading;
    private ClipboardManager cm;

    @BindView(R.id.iv_magage)
    ImageView iv_magage;

    @BindView(R.id.bg)
    FrameLayout bg;
    private PopupWindow popWnd;

    private int isCheck=0;
    private ImageView iv_check;
    private TextView tv_submit;

    private TextView info;
    private TextView info1;
    private static AccTranResponseModel.DataBean dataBean=null;
    private static AccountResponseModel.DataBean payDataBean=null;

    private ArrayList<RechargeResponseModel> list = new ArrayList<RechargeResponseModel>();

    MyProgressDialog myProgressDialog;

    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TYPE_SUCCESS:
                    orderXiaoQingDate();
                    break;
                case TYPE_FAILURE:
                    myProgressDialog.dismiss();
                    break;
                case TYPE_ERROR:
                    MyApp.showToast(getString(R.string.network));
                    myProgressDialog.dismiss();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acc_order;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
            finish();
        }else if (view.getId()==R.id.btn_summit){
            if (UtilOnclick.isFastClick()) {

                showPopupConfirm(type);
            }
        }else if (view.getId()==R.id.btn_cancel){
            deLeteDiolog();
        }else if (view.getId()==R.id.iv_magage){
            IMListener.startChatting(this,dataBean.getMemberBdsAccount());
//            Intent intent = mIMKit.getChattingActivityIntent(dataBean.getMemberBdsAccount(), MyApp.APP_KEY);
//            startActivity(intent);
        }

    }

    @Override
    public void initView() {
        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ivBack.setOnClickListener(this);
        adapter=new AcceptorCZorTXAdapter(list,this);
        adapter.setOnClickListener(this);
        lv_acceptors.setAdapter(adapter);
        btn_summit.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);
        btn_cancel.setOnClickListener(this);
        bdsAccount= SPUtils.getString(Const.USERNAME,"");
        iv_magage.setOnClickListener(this);


        lv_acceptors.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                cm.setText(list.get(i).getValue());
                Toast.makeText(AccOrderActivity.this, getString(R.string.bds_copied_to_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public void initData() {

        if (getIntent().getSerializableExtra("dataBean") != null) {
            dataBean = (AccTranResponseModel.DataBean) getIntent().getSerializableExtra("dataBean");
            type = dataBean.getOrderTypeCode();
            setType();
           // initData(dataBean);

            getPayStyle();

        }
    }


    public void upadteInitData(AccTranResponseModel.DataBean dataBean,AccountResponseModel.DataBean payDataBean) {
        if (!isUpdate){

            isUpdate=true;
            list.add(new RechargeResponseModel(getString(R.string.bds_OrderNumber),dataBean.getOrderNo()+""));
            if (type.equals(TYPE_CI)){
                list.add(new RechargeResponseModel(getString(R.string.bds_OrderUniqueID),dataBean.getDealCode()+""));
            }
            list.add(new RechargeResponseModel(getString(R.string.bds_OrderStatus),dataBean.getStatus()+""));
            list.add(new RechargeResponseModel(getString(R.string.bds_collect_method),dataBean.getPayType()+""));

            if (null!=payDataBean){
                this.payDataBean=payDataBean;
                if (dataBean.getPayTypeCode().equals("AP")){
                    list.add(new RechargeResponseModel(getString(R.string.full_name),payDataBean.getName()));
                    list.add(new RechargeResponseModel(getString(R.string.bds_apliay_account),payDataBean.getPayAccountID()));
                    if (type.equals(TYPE_CO)){
                        list.add(new RechargeResponseModel(getString(R.string.bds_qr_shoukuan),""));
                    }

                }else if (dataBean.getPayTypeCode().equals("WC")){
                    list.add(new RechargeResponseModel(getString(R.string.bds_weChat_name),payDataBean.getName()));
                    list.add(new RechargeResponseModel(getString(R.string.bds_weChat_account),payDataBean.getPayAccountID()));
                    if (type.equals(TYPE_CO)){
                        list.add(new RechargeResponseModel(getString(R.string.bds_qr_shoukuan),""));
                    }

                }else {

                    list.add(new RechargeResponseModel(getString(R.string.bds_CardName),payDataBean.getName()));
                    list.add(new RechargeResponseModel(getString(R.string.bankname),payDataBean.getBank()));
                    list.add(new RechargeResponseModel(getString(R.string.banknum),payDataBean.getPayAccountID()));
                }

            }

            list.add(new RechargeResponseModel(getString(R.string.bds_PaymentAmount), NumberUtils.formatNumber2(dataBean.getAmount())+dataBean.getSymbol()));
            String amount = "0";
            String fee = "0";
            if (!TextUtils.isEmpty(dataBean.getAmount())){
                amount = dataBean.getAmount();
            }
            if (!TextUtils.isEmpty(dataBean.getFee())){
                fee = dataBean.getFee();
            }

            String amount1 = NumberUtils.formatNumber2(CalculateUtils.sub(Double.parseDouble(amount),Double.parseDouble(fee))+"");

            list.add(new RechargeResponseModel(getString(R.string.bds_actual_transfer),MoneyUtil.getMontyCurrency(Double.parseDouble(amount1))+" "+dataBean.getSymbol()));

            list.add(new RechargeResponseModel(getString(R.string.bds_actual_transfer_chinese),MoneyUtil.toChinese(amount1,dataBean.getSymbol())));

            list.add(new RechargeResponseModel(getString(R.string.bds_GatewayServiceFee),NumberUtils.formatNumber2(fee)+dataBean.getSymbol()));
            list.add(new RechargeResponseModel(getString(R.string.bds_placement_time),dataBean.getCreateDate()));

        }else{
            for (int i=0;i<list.size();i++){
                if (list.get(i).getKey().equals(getString(R.string.bds_OrderStatus))){
                    list.get(i).setValue(dataBean.getStatus());
                    break;
                }

            }
            setType();


        }

        adapter.notifyDataSetChanged();



    }



    private void setType(){
        adapter.setStatusCode(dataBean.getStatusCode());
        btn_cancel.setVisibility(View.GONE);
        btn_summit.setVisibility(View.GONE);
        if (type.equals(TYPE_CI)){

            tvTitle.setText(getString(R.string.bds_Deposits));
             if(dataBean.getStatusCode().equals(TYPE_PAY)) {
                btn_summit.setVisibility(View.VISIBLE);
                btn_summit.setText(getString(R.string.bds_Transfer));
            }

        }else {
            tvTitle.setText(getString(R.string.bds_Withdrawals));
            if (dataBean.getStatusCode().equals(TYPE_NA)){


            }else if (dataBean.getStatusCode().equals(TYPE_A)){
                btn_summit.setVisibility(View.VISIBLE);
                btn_cancel.setVisibility(View.VISIBLE);
                btn_summit.setText(getString(R.string.bds_accconfirm));

            } else if (dataBean.getStatusCode().equals(TYPE_PAY)){
        }
        }



    }

    /***acceptant_cancel_cashout
     * 承兑商取消提现订单
     *
     * */
    private void orderDelete(){
        myProgressDialog.show();
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("id",dataBean.getId());
        if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"acceptant_cashout_cancel_apply",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equals("success")){
                    MyApp.showToast(getString(R.string.bds_OrderCanceled));
                    finish();
//                    orderXiaoQingDate();

                }else {
                    MyApp.showToast(""+response.body().getMsg());
                    myProgressDialog.dismiss();
                }


            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);

            }

            @Override

            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                handler.sendEmptyMessage(TYPE_ERROR);


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     *
     * 获取订单详情
     3 encmsg

     * */


    private void orderXiaoQingDate(){
        final String username= SPUtils.getString(Const.USERNAME,"");
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("isAcceptant","1");
        hashMap.put("orderNo",dataBean.getOrderNo());
        hashMap.put("orderType",dataBean.getOrderTypeCode());

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"get_order_info",new JsonCallBack<AccTranResponseModel>(AccTranResponseModel.class) {
            @Override
            public void onSuccess(Response<AccTranResponseModel> response) {
                if (response.body().getStatus().equals("success")){
                    AccOrderActivity.this.dataBean=response.body().getData().get(0);
                    upadteInitData(dataBean,payDataBean);

                }else {
                    MyApp.showToast(response.body().getMsg()+"");

                }
                loading.setVisibility(View.GONE);
                myProgressDialog.dismiss();

            }

            @Override
            public void onStart(Request<AccTranResponseModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<AccTranResponseModel> response) {
                super.onError(response);
                handler.sendEmptyMessage(TYPE_ERROR);
                loading.setVisibility(View.GONE);


            }
        });
    }

    /**
     * //承兑商确认提现已支付
     * member_confirm_pay
     * acceptant_confirm_receive
     *
     * */
    private void confirmPay(){
        myProgressDialog.show();
        final String username= SPUtils.getString(Const.USERNAME,"");
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount",bdsAccount);
        hashMap.put("orderNo",dataBean.getOrderNo());


        if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"acceptant_confirm_pay",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equals("success")){
                   orderXiaoQingDate();

                }else {
                    MyApp.showToast(response.body().getMsg());
                    handler.sendEmptyMessage(TYPE_FAILURE);

                }

            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                handler.sendEmptyMessage(TYPE_ERROR);




            }
        });
    }

    /**
     * //承兑商标记充值已完成
     * acceptant_transfer_cashin
     *
     * */
    private void acceptantTransferCashin  (){
        myProgressDialog.show();
        final String username= SPUtils.getString(Const.USERNAME,"");
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",bdsAccount);
//        hashMap.put("encmsg","");
        hashMap.put("id",dataBean.getId());

        if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"acceptant_transfer_cashin",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equals("success")){
                        orderXiaoQingDate();
                }else {
                    MyApp.showToast(response.body().getMsg());
                    myProgressDialog.dismiss();
                }

            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                handler.sendEmptyMessage(TYPE_ERROR);




            }
        });
    }


    /**
     *
     2 回去支付方式
     3 encmsg

     * */
    private void getPayStyle() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        if (type.equals(TYPE_CI)){

            String bdsAccount=SPUtils.getString(Const.USERNAME,"");
            hashMap.put("bdsAccount", bdsAccount);
            hashMap.put("isAcceptantPay","1");
        }else{
            String bdsAccount=dataBean.getMemberBdsAccount();
            hashMap.put("bdsAccount", bdsAccount);
            hashMap.put("isAcceptantPay","");
        }
        hashMap.put("payStyleID",dataBean.getPayStyleID());

        AcceptorApi.acceptantHttp(hashMap, "get_pay_style", new JsonCallBack<AccountResponseModel>(AccountResponseModel.class) {
            @Override
            public void onSuccess(Response<AccountResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    upadteInitData(dataBean,response.body().getData().get(0));


                } else {
                    upadteInitData(dataBean,payDataBean);

                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onStart(Request<AccountResponseModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AccountResponseModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                loading.setVisibility(View.GONE);

            }
        });
    }


    @Override
    public void onChildClick(int posistion) {
        if (list.get(posistion).getKey().equals(getString(R.string.bds_qr_shoukuan))){
                setQRdiolog(payDataBean.getTypeCode(),payDataBean.getPayCode(),payDataBean.getName(),payDataBean.getPayAccountID());

        }

    }

    private void setQRdiolog(String typeCode,String payCode ,String name,final String accountID){
        Drawable drawable=null;
        if (typeCode.equals("WC")){
            drawable = getResources().getDrawable(R.mipmap.accept_wx_icon);
        }else {
            drawable = getResources().getDrawable(R.mipmap.acaept_alipay_icon);
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        final Bitmap logo = bd.getBitmap();
        final QRDialog qrDialog=new QRDialog(this);
        qrDialog.setQrImageBitmap(payCode,logo);
        qrDialog.setMessage(name,accountID);
        qrDialog.setOnDialogOnClick(new QRDialog.OnDialogOnClick() {
            @Override
            public void OnSumbmitListener() {
                cm.setText(accountID);
                Toast.makeText(AccOrderActivity.this, getString(R.string.bds_copied_to_clipboard), Toast.LENGTH_LONG).show();
                qrDialog.dismiss();
            }
        });
        qrDialog.isVisible();
    }

    private void deLeteDiolog(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(getString(R.string.bds_delete_order));
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderDelete();
                dialog.dismiss();


            }
        });
        builder.show();
    }
    /**
     * //承兑商确认提现已支付diolog
     * member_confirm_pay
     * acceptant_confirm_receive
     *
     * */
    private void showPopupConfirm(final String index) {

        final AlertDialog customizeDialog = new AlertDialog.Builder(AccOrderActivity.this, AlertDialog.THEME_HOLO_LIGHT).create();
        customizeDialog.setInverseBackgroundForced(false);
        customizeDialog.show();
        Window window = customizeDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.confirm_operation_alert);
        customizeDialog.setCanceledOnTouchOutside(false);

        customizeDialog.setCancelable(false);


        info = (TextView) window.findViewById(R.id.tv_info);
        info1 = (TextView) window.findViewById(R.id.tv_info1);
        ImageView iv_cancel = (ImageView) window.findViewById(R.id.iv_cancel);
        tv_submit = (TextView) window.findViewById(R.id.tv_submit);
        LinearLayout ll_check = (LinearLayout) window.findViewById(R.id.ll_confirm);
        iv_check = (ImageView) window.findViewById(R.id.iv_check);

        TextView tv_transfer_money_count = window.findViewById(R.id.tv_transfer_money_count);
        TextView tv_transfer_money_count1 = window.findViewById(R.id.tv_transfer_money_count1);

        String amount = "0";
        String fee = "0";
        if (!TextUtils.isEmpty(dataBean.getAmount())){
            amount = dataBean.getAmount();
        }
        if (!TextUtils.isEmpty(dataBean.getFee())){
            fee = dataBean.getFee();
        }

        if (index.equals(TYPE_CI)){
            String amount1 = NumberUtils.formatNumber2(amount);
            tv_transfer_money_count.setText(" "+MoneyUtil.getMontyCurrency(Double.parseDouble(amount1))+" "+dataBean.getSymbol());
            tv_transfer_money_count1.setText( " "+MoneyUtil.toChinese(amount1,dataBean.getSymbol()));

            info.setText(getString(R.string.bds_QuestionAsk2));
            info1.setText(getString(R.string.bds_ConfirmReceived2));
        }else {

            String amount1 = NumberUtils.formatNumber2(CalculateUtils.sub(Double.parseDouble(amount),Double.parseDouble(fee))+"");
            tv_transfer_money_count.setText(" "+MoneyUtil.getMontyCurrency(Double.parseDouble(amount1))+" "+dataBean.getSymbol());
            tv_transfer_money_count1.setText( " "+MoneyUtil.toChinese(amount1,dataBean.getSymbol()));

            info.setText(getString(R.string.bds_QuestionAsk));
            info1.setText(getString(R.string.bds_QuestionConf));
        }

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeDialog.dismiss();
            }
        });
        isCheck=0;

        ll_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCheck == 0){
                    isCheck = 1;
                    iv_check.setImageResource(R.mipmap.iv_checked);
                    tv_submit.setBackgroundResource(R.drawable.btn_sendout);
                }else if (isCheck == 1){
                    isCheck = 0;
                    iv_check.setImageResource(R.mipmap.iv_check);
                    tv_submit.setBackgroundResource(R.drawable.bg_shape_adadad);

                }
            }
        });



        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Device.pingIpAddress()){
                    MyApp.showToast(getString(R.string.network));
                    customizeDialog.dismiss();
                }else{
                    tv_submit.setClickable(false);

                    if (isCheck==1){
                        customizeDialog.dismiss();
                        if (type.equals(TYPE_CI)) {
                            acceptantTransferCashin();
                        } else {
                            confirmPay();
                        }
                    }else {
                        tv_submit.setClickable(true);
                    }

                }

            }
        });


    }


}
