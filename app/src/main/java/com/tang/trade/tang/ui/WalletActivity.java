package com.tang.trade.tang.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.WalletListAdapter;
import com.tang.trade.tang.net.model.BlockResponModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.block_chain_info;
import com.tang.trade.tang.socket.chain.block_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by dagou on 2017/9/22.
 */

public class WalletActivity extends BaseActivity {

    private TextView tvBlock;
    private TextView tvHeadBlock;
    private TextView tvActive;
    private TextView tvNexƒtMaintance;
    private TextView tvAtiveCommitee;
    private ListView listView;

    private  ArrayList<BlockResponModel.DataBean> dataList = new ArrayList<BlockResponModel.DataBean>();

//    TimerTask timerTask = new TimerTask() {
//        @Override
//        public void run() {
//
//            //查询区块基础区块信息
//
//
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            block_chain_info blockChainInfo = null;
//                            try {
//                                blockChainInfo = BitsharesWalletWraper.getInstance().info();
//                            } catch (NetworkStatusException e) {
//                                e.printStackTrace();
//                            }
//                            if (blockChainInfo != null) {
//                                tvBlock.setText("#"+String.valueOf(blockChainInfo.head_block_num));
//                                tvHeadBlock.setText(String.valueOf(blockChainInfo.head_block_age));
//                                tvAtiveCommitee.setText(String.valueOf(blockChainInfo.active_committee_memberts_count));
//                                tvActive.setText(String.valueOf(blockChainInfo.active_witnesses_count));
//                                tvNexƒtMaintance.setText(blockChainInfo.next_maintenance_time);
//
//                                try {
//                                    Integer nextBlockNum = Integer.parseInt(blockChainInfo.head_block_num);
//                                    for (int i=0; i< 10; ++i) {
//                                        block_object blockobj =  BitsharesWalletWraper.getInstance().get_block(nextBlockNum);
//                                        if (blockobj == null) {
//                                            continue;
//                                        }
//
//                                        BlockResponModel.DataBean dataBean= new BlockResponModel.DataBean();
//                                        dataBean.setBlocknumber("#"+blockobj.blockNumber);
//                                        dataBean.setTime(blockobj.timeStame);
//                                        dataBean.setWitness(blockobj.witnessId);
//                                        dataBean.setExchangecount(blockobj.transactionCount+"");
//                                        dataList.add(dataBean);
//                                        nextBlockNum --;
//                                    }
//
//                                    listView.setAdapter(new WalletListAdapter(dataList));
//                                } catch (NetworkStatusException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
//
//
//
//        }
//    };


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dataList.clear();
            block_chain_info blockChainInfo = null;
            try {
                blockChainInfo = BitsharesWalletWraper.getInstance().info();
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
            if (blockChainInfo != null) {
                tvBlock.setText("#"+String.valueOf(blockChainInfo.head_block_num));
                tvHeadBlock.setText(String.valueOf(blockChainInfo.head_block_age));
                tvAtiveCommitee.setText(String.valueOf(blockChainInfo.active_committee_memberts_count));
                tvActive.setText(String.valueOf(blockChainInfo.active_witnesses_count));
                tvNexƒtMaintance.setText(blockChainInfo.next_maintenance_time);

                try {
                    Integer nextBlockNum = Integer.parseInt(blockChainInfo.head_block_num);
                    for (int i=0; i< 10; ++i) {
                        block_object blockObj =  BitsharesWalletWraper.getInstance().get_block(nextBlockNum);
                        if (blockObj == null) {
                            continue;
                        }

                        BlockResponModel.DataBean dataBean= new BlockResponModel.DataBean();
                        dataBean.setBlocknumber("#"+blockObj.blockNumber);
                        dataBean.setTime(blockObj.timeStame);
                        dataBean.setWitness(blockObj.witnessId);
                        dataBean.setExchangecount(blockObj.transactionCount+"");
                        dataList.add(dataBean);
                        nextBlockNum --;
                    }

                    listView.setAdapter(new WalletListAdapter(dataList));

                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
            handler.postDelayed(thread,5000);
        }

    };


    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    });
    private Timer timer;
    private ImageView ivBack;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backwallet:
                this.finish();
                break;
        }
    }

    @Override
    public void initView() {
        ivBack = (ImageView) findViewById(R.id.backwallet);
        tvBlock = (TextView) findViewById(R.id.currentblock);
        tvHeadBlock = (TextView) findViewById(R.id.headblockage);
        tvActive = (TextView) findViewById(R.id.activewitnesses);
        tvNexƒtMaintance = (TextView) findViewById(R.id.nextmaintance);
        tvAtiveCommitee = (TextView) findViewById(R.id.activecommitmember);
        listView = (ListView) findViewById(R.id.listview_wallet);

        ivBack.setOnClickListener(this);
    }

    @Override
    public void initData() {
//        timer = new Timer();
//        timer.schedule(timerTask, 0, 5000);

    }


    @Override
    protected void onResume() {
        super.onResume();
//        if (timer == null){
//            timer = new Timer();
//            timer.schedule(timerTask, 0, 5000);
//        }
        handler.post(thread);
        Log.e("My", "onRestart: "+"Wa");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
//        timer.cancel();
//        timer = null;

        handler.removeCallbacks(thread);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

    }
}
