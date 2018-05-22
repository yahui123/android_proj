package com.tang.trade.tang.socket.chain;


import com.tang.trade.tang.net.model.MortgageModel;

import java.util.List;

import de.bitsharesmunich.graphenej.LimitOrder;
import de.bitsharesmunich.graphenej.UserAccount;

public class full_account {
        public UserAccount account;
        public List<LimitOrder> limit_orders;
        public List<CallOrder> call_orders;

}
