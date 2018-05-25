package com.tang.trade.tang.socket;


import com.google.common.primitives.UnsignedInteger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.fc.io.base_encoder;
import com.tang.trade.tang.socket.fc.io.raw_type;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class authority {

    public static class authority_type_deserializer implements JsonDeserializer<authority> {
        @Override
        public authority deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            authority authority = new authority();

            ArrayList account_auths = new ArrayList();

            try{
                JsonArray json_array = object.getAsJsonArray("account_auths");

                for (JsonElement element:json_array) {
                    JsonArray json_item = element.getAsJsonArray();

                    ArrayList account = new ArrayList();

                    account.add(object_id.create_from_string(json_item.get(0).getAsString()));

                    account.add(Integer.valueOf(json_item.get(1).getAsInt()));

                    account_auths.add(account);
                }
            }catch (Exception e) {
                JsonObject json_item = object.getAsJsonObject("account_auths");

                for (Map.Entry<String, JsonElement> entry : json_item.entrySet()) {
                    ArrayList account = new ArrayList();

                    account.add(object_id.create_from_string(entry.getKey()));

                    account.add(Integer.valueOf(entry.getValue().getAsInt()));

                    account_auths.add(account);
                }
            }

            ArrayList key_auths = new ArrayList();

            try{
                JsonArray json_array = object.getAsJsonArray("key_auths");

                for (JsonElement element:json_array) {
                    JsonArray json_item = element.getAsJsonArray();

                    ArrayList key = new ArrayList();

                    key.add(new types.public_key_type(json_item.get(0).getAsString()));

                    key.add(Integer.valueOf(json_item.get(1).getAsInt()));

                    key_auths.add(key);
                }
            }catch (Exception e) {
                JsonObject json_item = object.getAsJsonObject("key_auths");

                for (Map.Entry<String, JsonElement> entry : json_item.entrySet()) {
                    ArrayList key = new ArrayList();

                    try {
                        key.add(new types.public_key_type(entry.getKey()));
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    }

                    key.add(Integer.valueOf(entry.getValue().getAsInt()));

                    key_auths.add(key);
                }

            }
            authority.key_auths = key_auths;

            return authority;
        }
    }

    private Integer weight_threshold;
    private ArrayList<ArrayList> account_auths = new ArrayList();
    private ArrayList<ArrayList> key_auths = new ArrayList();
    private ArrayList<ArrayList> address_auths = new ArrayList();

//    public HashMap<object_id<account_object>, Integer> account_auths = new HashMap<>()
//    private HashMap<types.public_key_type, Integer> key_auths = new HashMap<>();
//    private HashMap<address, Integer> address_auths = new HashMap<>();

    public HashMap<object_id<account_object>, Integer> account_auths() {
        HashMap<object_id<account_object>, Integer> auths = new HashMap<>();

        for (ArrayList key : account_auths) {
            auths.put((object_id<account_object>)key.get(0),(Integer) key.get(1));
        }
        return auths;
    }
    public authority() {

    }

    public authority(int nWeightThreshold, types.public_key_type publicKeyType, int nWeightType) {
        weight_threshold = nWeightThreshold;
        ArrayList key = new ArrayList();
        key.add(publicKeyType);
        key.add(nWeightType);
        key_auths.add(key);

//        key_auths.put(publicKeyType, nWeightType);
    }

    public void addAuthority( types.public_key_type publicKeyType, int nWeightType) {
        if (is_public_key_type_exist(publicKeyType)) return;
        ArrayList key = new ArrayList();
        key.add(publicKeyType);
        key.add(nWeightType);
        key_auths.add(key);
    }

    public boolean is_public_key_type_exist(types.public_key_type publicKeyType) {
        for (ArrayList key : key_auths) {


            if (key.get(0).getClass() != types.public_key_type.class) {
                String pub = (String)key.get(0);

                try {
                    key.set(0,new types.public_key_type(pub));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            if (key.get(0).equals(publicKeyType)) {
                return true;
            }
        }
        return false;

//        return key_auths.containsKey(publicKeyType);
    }

    public List<types.public_key_type> get_keys() {
        List<types.public_key_type> listKeyType = new ArrayList<>();

        for (ArrayList key : key_auths) {
            if (key.get(0).getClass() != types.public_key_type.class) {
                String pub = (String)key.get(0);

                try {
                    key.set(0,new types.public_key_type(pub));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            types.public_key_type pub = (types.public_key_type) key.get(0);

            listKeyType.add(pub);
        }

//        listKeyType.addAll(key_auths.keySet());
        return listKeyType;
    }

    public void write_to_endcode(base_encoder baseEncoder) {
        raw_type rawObject = new raw_type();

        baseEncoder.write(rawObject.get_byte_array(weight_threshold));

        rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(account_auths.size()));

        for (ArrayList key : account_auths) {
            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(((object_id<account_object>)key.get(0)).get_instance()));

            Integer weight = (Integer) key.get(1);

            baseEncoder.write(rawObject.get_byte_array(weight.shortValue()));
        }

        rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(key_auths.size()));
        for (ArrayList key : key_auths) {
            //通过Gson 未定义转换方式,所以调用时需先判断类型
            if (key.get(0).getClass() != types.public_key_type.class) {
                String pub = (String)key.get(0);

                try {
                    key.set(0,new types.public_key_type(pub));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            types.public_key_type pub = (types.public_key_type) key.get(0);
            //Gson 转int 有可能转成Double 所以在这里转换回来
            if (key.get(1).getClass() != Integer.class) {
                key.set(1,Integer.valueOf(((Double)key.get(1)).intValue()));
            }

            Integer weight = (Integer) key.get(1);

//            byte[] data = pub.key_data;

            baseEncoder.write(pub.key_data);
//            rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits((int)key.get(1)));
            System.out.println("authority  public_key_weight");
            baseEncoder.write(rawObject.get_byte_array(weight.shortValue()));
        }

        System.out.println("authority address_auths");
        rawObject.pack(baseEncoder,UnsignedInteger.fromIntBits(address_auths.size()));
    }
}
