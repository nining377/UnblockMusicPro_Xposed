package com.raincat.unblockmusicpro;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * @author 躲雨的猫 序列化根
 */
public class BaseObject implements Serializable {
    private static final long serialVersionUID = 1L;
    public static String tag;

    public BaseObject() {
        tag = super.getClass().toString();
    }

    protected static String getJsonString(JSONObject obj, String key) throws JSONException {
        if (obj != null && obj.length() != 0) {
            if (!obj.isNull(key)) {
                return obj.getString(key);
            }
        }
        Log.d(tag, "The key '" + key + "' has no value");
        return "";
    }

    protected static int getJsonInt(JSONObject obj, String key) throws JSONException {
        if (obj != null && obj.length() != 0) {
            if (!obj.isNull(key)) {
                return obj.getInt(key);
            }
        }
        Log.d(tag, "The key '" + key + "' has no value");
        return -1;
    }

    protected static long getJsonLong(JSONObject obj, String key) throws JSONException {
        if (obj != null && obj.length() != 0) {
            if (!obj.isNull(key)) {
                return obj.getLong(key);
            }
        }
        Log.d(tag, "The key '" + key + "' has no value");
        return -1;
    }

    protected static boolean getJsonBoolean(JSONObject obj, String key) throws JSONException {
        if (obj != null && obj.length() != 0) {
            if (!obj.isNull(key)) {
                return obj.getBoolean(key);
            }
        }
        Log.d(tag, "The key '" + key + "' has no value");
        return false;
    }

    protected static double getJsonDouble(JSONObject obj, String key) {
        if (obj != null && obj.length() != 0) {
            if (!obj.isNull(key)) {
                try {
                    return obj.getDouble(key);
                } catch (JSONException e) {
                    return 0.0;
                }
            }
        }
        Log.d(tag, "The key '" + key + "' has no value");
        return 0.0;
    }
}
