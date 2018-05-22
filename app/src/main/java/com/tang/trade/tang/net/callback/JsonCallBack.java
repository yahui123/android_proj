package com.tang.trade.tang.net.callback;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lzy.okgo.callback.AbsCallback;
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.utils.TLog;

import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by dagou on 2017/9/21.
 */

public abstract class JsonCallBack<T> extends AbsCallback<T> {

    private Type type;
    private Class<T> clazz;

    public JsonCallBack(Type type) {
        this.type = type;
    }

    public JsonCallBack(Class<T> clazz) {
        this.clazz = clazz;
    }



    @Override
    public T convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }

        T data = null;
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(body.charStream());
        if (type != null) {
            data = gson.fromJson(jsonReader, type);
        }
        if (clazz != null) {
            data = gson.fromJson(jsonReader, clazz);
        }
        return data;
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        TLog.log(response.message());
    }
}
