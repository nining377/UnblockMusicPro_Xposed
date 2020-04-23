package com.raincat.unblockmusicpro;

import de.robv.android.xposed.XSharedPreferences;

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

    static boolean getAd() {
        return getModuleSharedPreferences().getBoolean("ad", true);
    }

    static boolean getUpdate() {
        return getModuleSharedPreferences().getBoolean("update", true);
    }

    static boolean getSSL() {
        return getModuleSharedPreferences().getBoolean("ssl", true);
    }

    static boolean getLog() {
        return getModuleSharedPreferences().getBoolean("log", false);
    }
}
