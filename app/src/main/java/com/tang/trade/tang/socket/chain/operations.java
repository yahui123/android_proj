package com.tang.trade.tang.socket.chain;

import com.google.common.primitives.UnsignedInteger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.authority;
import com.tang.trade.tang.socket.fc.io.base_encoder;
import com.tang.trade.tang.socket.fc.io.raw_type;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.tang.trade.tang.socket.chain.config.GRAPHENE_BLOCKCHAIN_PRECISION;


public class operations {
    public static final int ID_TRANSER_OPERATION = 0;
    public static final int ID_CREATE_LIMIT_ORDER_OPERATION = 1;
    public static final int ID_CANCEL_LMMIT_ORDER_OPERATION = 2;
    public static final int ID_UPDATE_LMMIT_ORDER_OPERATION = 3;
    public static final int ID_FILL_LMMIT_ORDER_OPERATION = 4;
    public static final int ID_CREATE_ACCOUNT_OPERATION = 5;
    public static final int ID_ACCOUNT_UPDATE_OPERATION = 6;
    public static final int ID_ACCOUNT_WHITELIST_OPERATION=7;
    public static final int ID_UPGRADE_ACCOUNT_OPERATION = 8;
    public static final int ID_ACCOUNT_TRANSFER_OPERATION = 9;
    public static final int ID_ASSET_CREATE_OPERATION = 10;
    public static final int ID_VESTING_WITHDRAW_OPERATION= 33;


    public static operation_id_map operations_map = new operation_id_map();
    public static class operation_id_map {
        private HashMap<Integer, Type> mHashId2Operation = new HashMap<>();
        private HashMap<Integer, Type> mHashId2OperationFee = new HashMap<>();
        public operation_id_map() {

            mHashId2Operation.put(ID_TRANSER_OPERATION, transfer_operation.class);
            mHashId2Operation.put(ID_CREATE_LIMIT_ORDER_OPERATION, limit_order_create_operation.class);
            mHashId2Operation.put(ID_CANCEL_LMMIT_ORDER_OPERATION, limit_order_cancel_operation.class);
            mHashId2Operation.put(ID_UPDATE_LMMIT_ORDER_OPERATION, call_order_update_operation.class);
            mHashId2Operation.put(ID_FILL_LMMIT_ORDER_OPERATION, fill_order_operation.class);
            mHashId2Operation.put(ID_CREATE_ACCOUNT_OPERATION, account_create_operation.class);
            mHashId2Operation.put(ID_UPGRADE_ACCOUNT_OPERATION,account_upgrade_operation.class);
            mHashId2Operation.put(ID_VESTING_WITHDRAW_OPERATION,withdraw_vesting_operation.class);

            mHashId2OperationFee.put(ID_TRANSER_OPERATION, transfer_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CREATE_LIMIT_ORDER_OPERATION, limit_order_create_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CANCEL_LMMIT_ORDER_OPERATION, limit_order_cancel_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_UPDATE_LMMIT_ORDER_OPERATION, call_order_update_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_FILL_LMMIT_ORDER_OPERATION, fill_order_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CREATE_ACCOUNT_OPERATION, account_create_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_UPGRADE_ACCOUNT_OPERATION,account_upgrade_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_VESTING_WITHDRAW_OPERATION,withdraw_vesting_operation.fee_parameters_type.class);
        }

        public Type getOperationObjectById(int nId) {
            return mHashId2Operation.get(nId);
        }
        public Type getOperationFeeObjectById(int nId) {
            return mHashId2OperationFee.get(nId);
        }
    }

    public static class operation_type {
        public int nOperationType;
        public Object operationContent;

        public static class operation_type_deserializer implements JsonDeserializer<operation_type> {
            @Override
            public operation_type deserialize(JsonElement json,
                                              Type typeOfT,
                                              JsonDeserializationContext context) throws JsonParseException {
                operation_type operationType = new operation_type();
                JsonArray jsonArray = json.getAsJsonArray();

                operationType.nOperationType = jsonArray.get(0).getAsInt();
                Type type = operations_map.getOperationObjectById(operationType.nOperationType);


                if (type != null) {
                    operationType.operationContent = context.deserialize(jsonArray.get(1), type);
                } else {
                    operationType.operationContent = context.deserialize(jsonArray.get(1), Object.class);
                }

                return operationType;
            }
        }

        public static class operation_type_serializer implements JsonSerializer<operation_type> {

            @Override
            public JsonElement serialize(operation_type src, Type typeOfSrc, JsonSerializationContext context) {
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(src.nOperationType);
                Type type = operations_map.getOperationObjectById(src.nOperationType);

                assert(type != null);
                jsonArray.add(context.serialize(src.operationContent, type));

                return jsonArray;
            }
        }
    };

    public interface base_operation {
        List<authority> get_required_authorities();
        List<object_id<account_object>> get_required_active_authorities();
        List<object_id<account_object>> get_required_owner_authorities();

        void write_to_encoder(base_encoder baseEncoder);

        long calculate_fee(Object objectFeeParameter);

        void set_fee(asset fee);

        object_id<account_object> fee_payer();

        List<object_id<account_object>> get_account_id_list();

        List<object_id<asset_object>> get_asset_id_list();
    }


    public static class transfer_operation implements base_operation {
        public static class fee_parameters_type {
            long fee       = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
            long price_per_kbyte = 10 * GRAPHENE_BLOCKCHAIN_PRECISION; /// only required for large memos.
        };

        public asset fee;
        public object_id<account_object> from;
        public object_id<account_object> to;
        public asset amount;
        public memo_data memo;
        //public extensions_type   extensions;
        public Set<types.void_t> extensions;

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            //baseEncoder.write(rawObject.get_byte_array(fee.asset_id.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));
            //baseEncoder.write(rawObject.get_byte_array(from.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(from.get_instance()));
            //baseEncoder.write(rawObject.get_byte_array(to.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(to.get_instance()));
            baseEncoder.write(rawObject.get_byte_array(amount.amount));
            //baseEncoder.write(rawObject.get_byte_array(amount.asset_id.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(amount.asset_id.get_instance()));
            baseEncoder.write(rawObject.get_byte(memo != null));
            if (memo != null) {
                baseEncoder.write(memo.from.key_data);
                baseEncoder.write(memo.to.key_data);
                baseEncoder.write(rawObject.get_byte_array(memo.nonce));
                byte[] byteMessage = memo.message.array();
                rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(byteMessage.length));
                baseEncoder.write(byteMessage);
            }

            //baseEncoder.write(rawObject.get_byte_array(extensions.size()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
             return calculate_fee(feeParametersType);
        }

        @Override
        public void set_fee(asset assetFee) {
            fee = assetFee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return from;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(from);
            listAccountId.add(to);
            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            listAssetId.add(amount.asset_id);
            return listAssetId;
        }

        public long calculate_fee(fee_parameters_type feeParametersType) {
            long lFee = feeParametersType.fee;
            if (memo != null) {
                // 计算数据价格
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();
                BigInteger nSize = BigInteger.valueOf(gson.toJson(memo).length());
                BigInteger nPrice = BigInteger.valueOf(feeParametersType.price_per_kbyte);
                BigInteger nKbyte = BigInteger.valueOf(1024);
                BigInteger nAmount = nPrice.multiply(nSize).divide(nKbyte);

                lFee += nAmount.longValue();
            }

            return lFee;
        }
    }

    public static class limit_order_create_operation implements base_operation {
        static class fee_parameters_type {
            long fee = 5 * GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        public asset                     fee;
        public object_id<account_object> seller;
        public asset                     amount_to_sell;
        public asset                     min_to_receive;

        /// The order will be removed from the books if not filled by expiration
        /// Upon expiration, all unsold asset will be returned to seller
        public Date expiration; // = time_point_sec::maximum();

        /// If this flag is set the entire order must be filled or the operation is rejected
        public boolean fill_or_kill = false;
        public Set<types.void_t>   extensions;

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            // seller
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(seller.get_instance()));

            // amount_to_sell
            baseEncoder.write(rawObject.get_byte_array(amount_to_sell.amount));
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(amount_to_sell.asset_id.get_instance()));

            // min_to_receive
            baseEncoder.write(rawObject.get_byte_array(min_to_receive.amount));
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(min_to_receive.asset_id.get_instance()));

            // expiration
            baseEncoder.write(rawObject.get_byte_array(expiration));

            // fill_or_kill
            baseEncoder.write(rawObject.get_byte(fill_or_kill));

            // extensions
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return seller;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(seller);
            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            listAssetId.add(amount_to_sell.asset_id);
            listAssetId.add(min_to_receive.asset_id);
            return listAssetId;
        }
    }

    public static class limit_order_cancel_operation implements base_operation {
        class fee_parameters_type {
            long fee = 0;
        };

        public asset                         fee;
        public object_id<limit_order_object> order;
        /** must be order->seller */
        public object_id<account_object>     fee_paying_account;
        public Set<types.void_t>             extensions;

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            // fee_paying_account
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(fee_paying_account.get_instance()));

            // order
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(order.get_instance()));

            // extensions
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return fee_paying_account;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(fee_paying_account);
            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            return listAssetId;
        }
    }

    public static class call_order_update_operation implements base_operation {
        /** this is slightly more expensive than limit orders, this pricing impacts prediction markets */
        class fee_parameters_type {
            long fee = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
        };

        public asset                     fee;
        public object_id<account_object> funding_account; ///< pays fee, collateral, and cover
        public asset                     delta_collateral; ///< the amount of collateral to add to the margin position
        public asset                     delta_debt; ///< the amount of the debt to be paid off, may be negative to issue new debt
        public Set<types.void_t>         extensions;

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {

            raw_type rawObject = new raw_type();
            //打包手续费
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));
            //打包账户
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(funding_account.get_instance()));
            //打包借入资产
            baseEncoder.write(rawObject.get_byte_array(delta_collateral.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(delta_collateral.asset_id.get_instance()));
            //打包抵押资产
            baseEncoder.write(rawObject.get_byte_array(delta_debt.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(delta_debt.asset_id.get_instance()));
            //打包extensions
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return funding_account;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(funding_account);
            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            listAssetId.add(delta_collateral.asset_id);
            listAssetId.add(delta_debt.asset_id);
            return listAssetId;
        }
    }

    public static class fill_order_operation implements base_operation {
        class fee_parameters_type {
        }

        public object_id                     order_id;
        public object_id<account_object>     account_id;
        public asset                         pays;
        public asset                         receives;
        public asset                         fee; // paid by receiving account

        @Override
        public List<authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(asset fee) {

        }

        @Override
        public object_id<account_object> fee_payer() {
            return account_id;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(account_id);

            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            listAssetId.add(pays.asset_id);
            listAssetId.add(receives.asset_id);

            return listAssetId;
        }

    }

    public static class account_upgrade_operation implements  base_operation {
        class fee_parameters_type {
            long membershhip_annual_fee       = 2000*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            long membership_lifetime_fee      = 10000*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
        }
        public asset fee;
        public object_id<account_object> account_to_upgrade;
        public boolean upgrade_to_lifetime_member;
        public Set<types.void_t> extensions;


        public long calculate_fee(fee_parameters_type feeParametersType) {
            if (upgrade_to_lifetime_member)
                return feeParametersType.membership_lifetime_fee;
            return feeParametersType.membershhip_annual_fee;
        }

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();
            //打包手续费
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(account_to_upgrade.get_instance()));

            baseEncoder.write(rawObject.get_byte(upgrade_to_lifetime_member));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;

            if (upgrade_to_lifetime_member) {
                return feeParametersType.membership_lifetime_fee;
            }
            return feeParametersType.membershhip_annual_fee;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return account_to_upgrade;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(account_to_upgrade);
            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            return listAssetId;
        }

    }

    public static class withdraw_vesting_operation implements base_operation {
        class fee_parameters_type {
            long fee = 20*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
        }

        public asset fee;
        public object_id<vesting_balance_object> vesting_balance;
        public object_id<account_object> owner;
        public asset amount;

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();
            //打包手续费
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(vesting_balance.get_instance()));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(owner.get_instance()));

            baseEncoder.write(rawObject.get_byte_array(amount.amount));
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(amount.asset_id.get_instance()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert(fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type)objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return owner;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            return null;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            return null;
        }
    }

    public static class account_create_operation implements base_operation {
        class fee_parameters_type {
            long basic_fee       = 5*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            long premium_fee     = 2000*GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            int  price_per_kbyte = GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        public asset fee;
        public object_id<account_object> registrar;
        public object_id<account_object> referrer;
        public int referrer_percent;
        public String name;
        public authority owner;
        public authority active;
        public types.account_options options;
        public HashMap        extensions;

        public long calculate_fee(fee_parameters_type feeParametersType) {
            long lFeeRequired = feeParametersType.basic_fee;
            if (utils.is_cheap_name(name) == false) {
                lFeeRequired = feeParametersType.premium_fee;
            }

            // // TODO: 07/09/2017  未完成
            return lFeeRequired;

        }

        @Override
        public List<authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<object_id<account_object>> get_required_active_authorities() {
            List<object_id<account_object>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<object_id<account_object>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(base_encoder baseEncoder) {
            raw_type rawObject = new raw_type();
            //打包手续费
            baseEncoder.write(rawObject.get_byte_array(fee.amount));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));
            //打包账户

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(registrar.get_instance()));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(referrer.get_instance()));
            //reffer_percent 需要直接写入数据的二进制文件
            Integer reffer = Integer.valueOf(referrer_percent);
            baseEncoder.write(rawObject.get_byte_array(reffer.shortValue()));

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(name.length()));

            baseEncoder.write(name.getBytes());

            owner.write_to_endcode(baseEncoder);

            active.write_to_endcode(baseEncoder);

            options.write_to_encode(baseEncoder);

            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(asset fee) {
            this.fee = fee;
        }

        @Override
        public object_id<account_object> fee_payer() {
            return registrar;
        }

        @Override
        public List<object_id<account_object>> get_account_id_list() {
            List<object_id<account_object>> listAccountId = new ArrayList<>();
            listAccountId.add(registrar);
            listAccountId.add(referrer);

            return listAccountId;
        }

        @Override
        public List<object_id<asset_object>> get_asset_id_list() {
            List<object_id<asset_object>> listAssetId = new ArrayList<>();
            return listAssetId;
        }

    }

}
