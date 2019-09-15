package com.raincat.unblockmusicpro;

import android.content.Context;

import com.raincat.netutils.HTTPS_GET;
import com.raincat.netutils.HTTP_POST;
import com.raincat.netutils.NetCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/16
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

public class Update extends BaseObject {
    String downloadUrl = "";
    String version = "";
    String log = "";
    String zipUrl = "";

    private final static String appUrl = "https://api.github.com/repos/nining377/UnblockMusicPro_Xposed/releases/latest";
    private final static String scriptUrl = "https://api.github.com/repos/nondanee/UnblockNeteaseMusic/releases/latest";

    public static void getAppVersion(Context context, NetCallBack back) {
        new HTTPS_GET(context, appUrl, null, true, false, back);
    }

    public static void getScriptVersion(Context context, NetCallBack back) {
        new HTTPS_GET(context, scriptUrl, null, true, false, back);
    }

    public static Update getUpdate(Context context, JSONObject jsonObject) throws JSONException {
        Update update = new Update();
        update.version = getJsonString(jsonObject, "tag_name").replaceAll("[^0-9.]", "");
        JSONArray array = jsonObject.getJSONArray("assets");
        if (array.length() > 0)
            update.downloadUrl = getJsonString(array.getJSONObject(0), "browser_download_url");
        update.log = getJsonString(jsonObject, "body");
        update.zipUrl = getJsonString(jsonObject, "zipball_url");
        return update;
    }
}
