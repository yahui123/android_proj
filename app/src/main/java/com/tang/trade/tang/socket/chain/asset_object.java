package com.tang.trade.tang.socket.chain;


import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.utils.CalculateUtils;

public class asset_object {
    public class asset_object_legible {
        public String count;
        public String decimal;
        public String symbol;

        public long lCount;
        public long lDecimal;
        public long scaled_precision;
        public long lcount;
    }

    //static const uint8_t space_id = protocol_ids;
    //static const uint8_t type_id  = asset_object_type;
    /// Ticker symbol for this asset, i.e. "USD"
    public object_id<asset_object> id;
    public String symbol;
    /// Maximum number of digits after the decimal point (must be <= 12)
    public int precision = 0;
    /// ID of the account which issued this asset.
    object_id<account_object> issuer;

    public asset_options options;

    /// Current supply, fee pool, and collected fees are stored in a separate object as they change frequently.
    object_id<asset_dynamic_data_object>  dynamic_asset_data_id;

    public boolean is_base_asset_object() {
        asset base = options.core_exchange_rate.base;
        asset quote = options.core_exchange_rate.quote;

        return base.asset_id.equals(quote.asset_id);
    }

    public asset_object_legible get_legible_asset_object(long amount) {
        long scaled_precision = get_scaled_precision();

        asset_object_legible assetObjectLegible = new asset_object_legible();
        assetObjectLegible.count = CalculateUtils.div(amount , scaled_precision , precision)+"";
        assetObjectLegible.decimal = ((Long)(amount % scaled_precision + scaled_precision)).toString().substring(1);
        assetObjectLegible.symbol = symbol;

        assetObjectLegible.lDecimal = amount % scaled_precision;
        assetObjectLegible.scaled_precision = scaled_precision;
        assetObjectLegible.lCount = amount / scaled_precision;

        return assetObjectLegible;
    }

    public long convert_exchange_to_base(long amount) {
        asset base = options.core_exchange_rate.base;
        asset quote = options.core_exchange_rate.quote;
        if (base.asset_id.equals(quote.asset_id)) {
            return amount;
        }

        long lBaseAmount = (long)(amount * ((double)quote.amount / base.amount));
        return lBaseAmount;
    }

    public long convert_exchange_from_base(long amount) {
        asset base = options.core_exchange_rate.base;
        asset quote = options.core_exchange_rate.quote;
        if (base.asset_id.equals(quote.asset_id)) {
            return amount;
        }

        long lQuoteAmount = (long)(amount * ((double)base.amount / quote.amount));
        return lQuoteAmount;
    }

    public asset amount_from_string(String strAmount) {
        //strAmount.matches();
        // // TODO: 07/09/2017 需要正则表达处理


        long lCount = 0;
        long lDecimal = 0;
        Long scaled_precision = get_scaled_precision();

        strAmount = CalculateUtils.div(Double.parseDouble(strAmount),1,precision);

        int nIndex = strAmount.indexOf('.');
        if (nIndex == -1) {
            lCount = Long.valueOf(strAmount);
        } else {
            lCount = Long.valueOf(strAmount.substring(0, nIndex));

            int nDecMaxLen = scaled_precision.toString().substring(1).length();

            StringBuilder strDecimal = new StringBuilder(strAmount.substring(nIndex + 1));
            for (int i = strDecimal.length(); i < nDecMaxLen; ++i) {
                strDecimal.append("0");
            }

            lDecimal = Long.valueOf(strDecimal.toString());
        }

        asset assetObject = new asset(lCount * scaled_precision + lDecimal, id);

        return assetObject;
    }

    public long get_scaled_precision() {
        long scaled_precision = 1;
        for (int i = 0; i < precision; ++i) {
            scaled_precision *= 10;
        }
        return scaled_precision;
    }
}
