package com.raincat.unblockmusicpro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Map;

import de.robv.android.xposed.XSharedPreferences;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

class Setting {
    private static XSharedPreferences preferences;

    private static XSharedPreferences getModuleSharedPreferences() {
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, "share");
            preferences.makeWorldReadable();
        } else
            preferences.reload();
        return preferences;
    }

    static String getNodejs() {
        return getModuleSharedPreferences().getString("nodejs", Tools.Start + Tools.origin[0]);
    }

    static String getOriginString() {
        return getModuleSharedPreferences().getString("originString", "酷我");
    }

    static boolean getEnable() {
        return getModuleSharedPreferences().getBoolean("enable", true);
    }

    static boolean getLog() {
        return getModuleSharedPreferences().getBoolean("log", false);
    }
}
