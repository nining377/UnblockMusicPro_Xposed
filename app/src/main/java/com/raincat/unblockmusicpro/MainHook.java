package com.raincat.unblockmusicpro;

import net.androidwing.hotxposed.HotXposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/10/23
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(Tools.HOOK_NAME)) {
            HotXposed.hook(HTTPHook.class, lpparam);
        } else if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }
    }
}
