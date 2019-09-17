package com.raincat.unblockmusicpro;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.widget.Toast;

import com.stericson.RootShell.execution.Command;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/07
 *     desc   : Hook
 *     version: 1.0
 * </pre>
 */

public class HTTPHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final Pattern REX_MD5 = Pattern.compile("[a-f0-9]{32}", Pattern.CASE_INSENSITIVE);
    private static int versionCode = 0;
    private static String codePath = "";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            findAndHookMethod(findClass(MainActivity.class.getName(), lpparam.classLoader),
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }

        //Hook入口
        if (lpparam.packageName.equals(Tools.HOOK_NAME)) {
            findAndHookMethod(findClass("com.netease.cloudmusic.NeteaseMusicApplication", lpparam.classLoader),
                    "attachBaseContext", Context.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!Setting.getEnable()) {
                                Command stop = new Command(0, Tools.Stop);
                                Tools.shell(stop);
                                return;
                            }

                            Context context = (Context) param.thisObject;
                            try {
                                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                                versionCode = info.versionCode;
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            final String processName = Tools.getCurrentProcessName(context);
                            //主进程脚本注入
                            if (processName.equals(Tools.HOOK_NAME)) {
                                if (!initData(context))
                                    return;
                                if (Setting.getAd())
                                    Tools.deleteDirectory(Tools.neteaseCachePath);
                            } else if (processName.equals(Tools.HOOK_NAME + ":play")) {
                                if (initData(context)) {
                                    Command start;
                                    String port = " -p 23338";
                                    if (Setting.getSSL())
                                        port = port + ":23339";
                                    if (!Setting.getLog())
                                        start = new Command(0, Tools.Stop, "cd " + codePath, Setting.getNodejs() + port);
                                    else
                                        start = new Command(0, Tools.Stop, "cd " + codePath, Setting.getNodejs() + port) {
                                            @Override
                                            public void commandOutput(int id, String line) {
                                                XposedBridge.log(line);
                                            }
                                        };
                                    Tools.shell(start);
                                    Toast.makeText(context, "成功运行，当前优先选择" + Setting.getOriginString() + "音源", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "文件完整性校验失败，请打开UnblockMusic Pro并同意存储卡访问权限!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            if (processName.equals(Tools.HOOK_NAME) || processName.equals(Tools.HOOK_NAME + ":play")) {
                                final SSLSocketFactory socketFactory = Tools.getSLLContext(codePath + File.separator + "ca.crt").getSocketFactory();
                                final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 23338));
                                if (versionCode == 110) {
                                    //强制HTTP走本地代理
                                    hookAllConstructors(findClass("okhttp3.a", context.getClassLoader()), new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            if (param.args.length >= 9) {
                                                param.args[8] = proxy;
                                                if (Setting.getSSL())
                                                    param.args[4] = socketFactory;
                                            }
                                        }
                                    });
                                } else if (versionCode >= 138) {
                                    //强制返回正确MD5
                                    CloudMusicPackage.init(context);
                                    hookMethod(CloudMusicPackage.Transfer.getCalcMd5Method(), new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            Object file = param.args[0];
                                            if (file instanceof File) {
                                                String path = param.args[0].toString();
                                                Matcher matcher = REX_MD5.matcher(path);
                                                if (matcher.find()) {
                                                    param.setResult(matcher.group());
                                                }
                                            }
                                        }
                                    });

                                    //解决有版权歌曲无法缓冲
                                    hookAllMethods(findClass("okhttp3.RealCall", context.getClassLoader()), "newRealCall", new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            if (param.args.length == 3) {
                                                Object client = param.args[0];
                                                Object request = param.args[1];
                                                Field httpUrl = request.getClass().getDeclaredField("url");
                                                httpUrl.setAccessible(true);
                                                Object urlObj = httpUrl.get(request);

                                                Field proxyField = client.getClass().getDeclaredField("proxy");
                                                boolean proxyFlag = proxyField.isAccessible();
                                                proxyField.setAccessible(true);
                                                if (urlObj.toString().contains("jdyyaac")) {
                                                    proxyField.set(client, null);
                                                } else {
                                                    proxyField.set(client, proxy);
                                                }
                                                proxyField.setAccessible(proxyFlag);
                                                param.args[0] = client;
                                            }
                                        }
                                    });

                                    //去广告
                                    if (Setting.getAd()) {
                                        hookAllMethods(findClass("okhttp3.OkHttpClient", context.getClassLoader()), "newCall", new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                if (param.args.length == 1) {
                                                    Object request = param.args[0];
                                                    Field httpUrl = request.getClass().getDeclaredField("url");
                                                    httpUrl.setAccessible(true);
                                                    Object urlObj = httpUrl.get(request);
                                                    if (urlObj.toString().contains("eapi/ad")) {
                                                        Field url = urlObj.getClass().getDeclaredField("url");
                                                        url.setAccessible(true);
                                                        url.set(urlObj, "https://33.123.321.14/");
                                                        param.args[0] = request;
                                                    }
                                                }
                                            }
                                        });
                                    }

                                    //强制HTTP走本地代理
                                    if (Setting.getSSL()) {
                                        hookAllConstructors(findClass("okhttp3.OkHttpClient", context.getClassLoader()), new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                if (param.args.length == 1) {
                                                    Object okHttpClientBuilder = param.args[0];
//                                                  Field proxyField = okHttpClientBuilder.getClass().getDeclaredField("proxy");
//                                                  boolean proxyFlag = proxyField.isAccessible();
//                                                  proxyField.setAccessible(true);
//                                                  proxyField.set(okHttpClientBuilder, proxy);
//                                                  proxyField.setAccessible(proxyFlag);
//                                                  param.args[0] = okHttpClientBuilder;

                                                    Field sslSocketFactoryField = okHttpClientBuilder.getClass().getDeclaredField("sslSocketFactory");
                                                    sslSocketFactoryField.setAccessible(true);
                                                    sslSocketFactoryField.set(okHttpClientBuilder, socketFactory);
                                                    param.args[0] = okHttpClientBuilder;
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
        }
    }

    //释放脚本文件
    private boolean initData(Context context) {
        codePath = context.getFilesDir().getAbsolutePath();
        //比对版本
        String sdCartVersionString = Tools.loadFileFromSD(Tools.SDCardPath + File.separator + "package.json");
        String codeVersionString = Tools.loadFileFromSD(codePath + File.separator + "package.json");
        int nowVersion = Integer.parseInt(Tools.nowVersion.replace(".", "00"));
        int sdCartVersion = 0;
        int codeVersion = 0;
        try {
            //对比SD卡与安装包的脚本版本，不对则返回错误
            if (sdCartVersionString.length() != 0) {
                JSONObject jsonObject = new JSONObject(sdCartVersionString);
                sdCartVersion = Integer.parseInt(jsonObject.getString("version").replace(".", "00"));
            }
            if (sdCartVersion < nowVersion) {
                return false;
            }

            //对比SD卡与网易云内部脚本版本，不对则拷贝SD卡脚本文件到网易云内部
            if (codeVersionString.length() != 0) {
                JSONObject jsonObject = new JSONObject(codeVersionString);
                codeVersion = Integer.parseInt(jsonObject.getString("version").replace(".", "00"));
            }
            if (codeVersion < sdCartVersion) {
                Tools.copyFilesFromSD(Tools.SDCardPath, codePath);
            }
            Command command = new Command(0, "cd " + codePath, "chmod 700 *");
            Tools.shell(command);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        codeVersionString = Tools.loadFileFromSD(codePath + File.separator + "package.json");
        return codeVersionString.length() != 0;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    }
}
