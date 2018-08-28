package com.ywl5320.wlivefm.http.converter;

import com.google.gson.Gson;
import com.ywl5320.wlivefm.log.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by ywl5320 on 2017/5/23.
 */

public class UnGsonResponseBodyConverter<T extends Object> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final Type type;

    public UnGsonResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody valuer) throws IOException {


        String json = valuer.string();
        String result;
        MyLog.d("response content: " + json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iterator = jsonObject.keys();
            String data = "";
            String message = "";
            String status = "";
            while(iterator.hasNext())
            {
                String key = iterator.next();
                if(!key.equals("message") && !key.equals("status"))
                {
                    String value = jsonObject.getString(key);
                    if(value.startsWith("[") || value.startsWith("{")) {
                        data += "\"" + key + "\":" + value + ",";
                    }
                    else
                    {
                        data += "\"" + key + "\":\"" + value + "\",";
                    }
                }
                else if(key.equals("message"))
                {
                    message = "\"message\":\"" + jsonObject.getString(key) + "\"";
                }
                else if(key.equals("status"))
                {
                    status = "\"status\":" + jsonObject.getString(key);
                }
            }
            data = "\"data\":{" + data.substring(0, data.length() - 1) + "},";
            result = "{" + data + message + "," + status + "}";
            MyLog.d( "result:" + result);

        } catch (JSONException e) {
            e.printStackTrace();
            result = "{\n" +
                    "    \"status\": 400,\n" +
                    "    \"message\": \"gson format wrong\"\n" +
                    "}";
        }
        T t = gson.fromJson(result, type);
        return t;
    }
}
