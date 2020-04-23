package com.raincat.unblockmusicpro;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.raincat.netutils.FILE_DOWNLOAD;
import com.raincat.netutils.NetCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends PermissionProxyActivity {
    private Context context;
    private RelativeLayout rela_enable, rela_ad, rela_update, rela_high, rela_ssl, rela_hide, rela_log;
    private CheckBox cb_enable, cb_ad, cb_update, cb_high, cb_ssl, cb_hide, cb_log;
    private TextView tv_update, tv_faq, tv_version, tv_script, tv_perfect[];
    private ImageView iv_question, iv_version, iv_script;
    private RadioGroup rg_origin;
    private LinearLayout linear_version, linear_script;
    private EditText et_proxy;

    private int originIndex = 0;
    private SharedPreferences share;
    private Update versionUpdate, scriptUpdate;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                getPackageManager().setComponentEnabledSetting(new ComponentName(MainActivity.this, "com.raincat.unblockmusicpro.MainAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else if (msg.what == 1) {
                getPackageManager().setComponentEnabledSetting(new ComponentName(MainActivity.this, "com.raincat.unblockmusicpro.MainAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;

        checkState();
        initView();
        listener();
        checkPermission(context, new String[]{PERMISSION_EXTERNAL_STORAGE, PERMISSION_EXTERNAL_STORAGE2}, new OnPermissionResultListener() {
            @Override
            public void onResult(boolean get) {
                if (get) {
                    initData();
                    checkUpdate();
                } else
                    finish();
            }
        });
    }

    private void initData() {
        //比对版本
        String version = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "package.json");
        String localVersionString = "";
        int localVersion = 0;
        try {
            if (version.length() != 0) {
                JSONObject jsonObject = new JSONObject(version);
                localVersionString = jsonObject.getString("version");
                localVersion = Integer.parseInt(localVersionString.replace(".", "00").replace("-high", ""));
            }
            int nowVersion = Integer.parseInt(Tools.nowVersion.replace(".", "00").replace("-high", ""));
            if (nowVersion > localVersion) {
                //复制核心文件文件到SD卡
                Tools.copyFilesAssets(context, "UnblockNeteaseMusic-" + Tools.nowVersion, Tools.SDCardPath);
                Tools.copyFilesAssets(context, "shell", Tools.SDCardPath);
                if (Tools.is64BitImpl())
                    Tools.copyFilesAssets(context, "node-64bit", Tools.SDCardPath);
                else
                    Tools.copyFilesAssets(context, "node-32bit", Tools.SDCardPath);
                changeQuality(cb_high.isChecked());
            } else {
                Tools.nowVersion = localVersionString;
                Tools.copyFilesAssets(context, "UnblockNeteaseMusic-" + Tools.nowVersion.replace("-high", "") + "/node_modules", Tools.SDCardPath + "/node_modules");
            }
            Tools.copyFilesAssets(context, "log", Tools.SDCardPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tv_script.setText(Tools.nowVersion);
        int app_version = share.getInt("app_version", 0);
        if (app_version < BuildConfig.VERSION_CODE) {
            String update = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "update.txt");
            showMessageDialog(getString(R.string.menu_update), update, false);
        }
        share.edit().putInt("app_version", BuildConfig.VERSION_CODE).apply();
    }

    private void initView() {
        rela_enable = (RelativeLayout) findViewById(R.id.rela_enable);
        rela_ad = (RelativeLayout) findViewById(R.id.rela_ad);
        rela_update = (RelativeLayout) findViewById(R.id.rela_update);
        rela_high = (RelativeLayout) findViewById(R.id.rela_high);
        rela_ssl = (RelativeLayout) findViewById(R.id.rela_ssl);
        rela_hide = (RelativeLayout) findViewById(R.id.rela_hide);
        rela_log = (RelativeLayout) findViewById(R.id.rela_log);
        cb_enable = (CheckBox) findViewById(R.id.cb_enable);
        cb_ad = (CheckBox) findViewById(R.id.cb_ad);
        cb_update = (CheckBox) findViewById(R.id.cb_update);
        cb_high = (CheckBox) findViewById(R.id.cb_high);
        cb_ssl = (CheckBox) findViewById(R.id.cb_ssl);
        cb_hide = (CheckBox) findViewById(R.id.cb_hide);
        cb_log = (CheckBox) findViewById(R.id.cb_log);

        tv_update = (TextView) findViewById(R.id.tv_update);
        tv_faq = (TextView) findViewById(R.id.tv_faq);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_script = (TextView) findViewById(R.id.tv_script);
        tv_perfect = new TextView[2];
        tv_perfect[0] = (TextView) findViewById(R.id.tv_perfect1);
        tv_perfect[1] = (TextView) findViewById(R.id.tv_perfect2);

        iv_question = (ImageView) findViewById(R.id.iv_question);
        iv_version = (ImageView) findViewById(R.id.iv_version);
        iv_script = (ImageView) findViewById(R.id.iv_script);
        rg_origin = (RadioGroup) findViewById(R.id.rg_origin);
        linear_version = (LinearLayout) findViewById(R.id.linear_version);
        linear_script = (LinearLayout) findViewById(R.id.linear_script);

        et_proxy = (EditText) findViewById(R.id.et_proxy);

        tv_version.setText(BuildConfig.VERSION_NAME);
        tv_perfect[0].setText("Google：4.3.1");
        tv_perfect[1].setText("Global：6.0.0～7.0.20");

        share = getSharedPreferences("share", Context.MODE_WORLD_READABLE);
        originIndex = share.getInt("origin", 0);
        rg_origin.check(Tools.originResId[originIndex]);
        cb_enable.setChecked(share.getBoolean("enable", true));
        cb_ad.setChecked(share.getBoolean("ad", true));
        cb_update.setChecked(share.getBoolean("update", true));
        cb_ssl.setChecked(share.getBoolean("ssl", true));
        cb_high.setChecked(share.getBoolean("high", false));
        cb_hide.setChecked(share.getBoolean("hide", false));
        cb_log.setChecked(share.getBoolean("log", false));
        String proxyContent = share.getString("proxy", "127.0.0.1:23338");
        et_proxy.setText(proxyContent);
        et_proxy.setSelection(proxyContent.length());
    }

    private void listener() {
        rg_origin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_a:
                        originIndex = 0;
                        break;
                    case R.id.rb_b:
                        originIndex = 1;
                        break;
                    case R.id.rb_c:
                        originIndex = 2;
                        break;
                    case R.id.rb_d:
                        originIndex = 3;
                        break;
                }
                Toast.makeText(context, "切换成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
                share.edit().putInt("origin", originIndex).putString("nodejs", Tools.Start + Tools.origin[originIndex]).
                        putString("originString", getString(Tools.originString[originIndex])).apply();
            }
        });

        rela_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_enable.isChecked();
                cb_enable.setChecked(isChecked);
                share.edit().putBoolean("enable", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_ad.isChecked();
                cb_ad.setChecked(isChecked);
                share.edit().putBoolean("ad", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_update.isChecked();
                cb_update.setChecked(isChecked);
                share.edit().putBoolean("update", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_ssl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_ssl.isChecked();
                cb_ssl.setChecked(isChecked);
                share.edit().putBoolean("ssl", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_high.isChecked();
                changeQuality(isChecked);
                cb_high.setChecked(isChecked);
                share.edit().putBoolean("high", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_hide.isChecked();
                cb_hide.setChecked(isChecked);
                share.edit().putBoolean("hide", isChecked).apply();
                handler.sendEmptyMessageDelayed(isChecked ? 0 : 1, 1000);
                Toast.makeText(context, "操作成功！", Toast.LENGTH_SHORT).show();
            }
        });

        rela_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !cb_log.isChecked();
                cb_log.setChecked(isChecked);
                share.edit().putBoolean("log", isChecked).apply();
                Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            }
        });

        iv_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessageDialog("高亮项目将优先代理", Tools.message, false);
            }
        });

        tv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String update = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "update.txt");
                showMessageDialog(getString(R.string.menu_update), update, false);
            }
        });

        tv_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FAQ = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "FAQ.txt");
                showMessageDialog(getString(R.string.menu_faq), FAQ, false);
            }
        });

        linear_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (versionUpdate == null)
                    return;
                if (!versionUpdate.version.equals(BuildConfig.VERSION_NAME)) {
                    showUpdateDialog("更新APP - " + versionUpdate.version, "注意：更新后请重启手机！", versionUpdate);
                } else
                    Toast.makeText(context, "APP已是最新版本！", Toast.LENGTH_SHORT).show();
            }
        });

        linear_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scriptUpdate == null)
                    return;
                if (!scriptUpdate.version.equals(Tools.nowVersion.replace("-high", ""))) {
                    showUpdateDialog("更新脚本 - " + scriptUpdate.version, "注意：下载不了多试几次，相信自己是最胖的！", scriptUpdate);
                } else
                    Toast.makeText(context, "脚本已是最新版本！", Toast.LENGTH_SHORT).show();
            }
        });

        et_proxy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".contentEquals(s)) {
                    share.edit().remove("proxy").apply();
                    return;
                }
                share.edit().putString("proxy", s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkUpdate() {
        Update.getAppVersion(context, new NetCallBack() {
            @Override
            public void finish(JSONObject jsonObject) throws JSONException {
                versionUpdate = Update.getUpdate(context, jsonObject);
                if (!versionUpdate.version.equals(BuildConfig.VERSION_NAME))
                    iv_version.setVisibility(View.VISIBLE);
            }

            @Override
            public void error(int i, String s) {
                ErrorCode.showError(context, i);
            }

            @Override
            public void init() {

            }
        });

        Update.getScriptVersion(context, new NetCallBack() {
            @Override
            public void finish(JSONObject jsonObject) throws JSONException {
                scriptUpdate = Update.getUpdate(context, jsonObject);
                if (!scriptUpdate.version.equals(Tools.nowVersion.replace("-high", "")))
                    iv_script.setVisibility(View.VISIBLE);
            }

            @Override
            public void error(int i, String s) {
                ErrorCode.showError(context, i);
            }

            @Override
            public void init() {

            }
        });
    }

    private void checkState() {
        String method = null;

        if (isModuleActive()) {
            method = "Xposed / EdXposed";
        } else if (isVXP()) {
            method = "VirtualXposed";
        } else if (isExpModuleActive()) {
            method = "太极";
        }

        if (method == null)
            showMessageDialog("引导", getString(R.string.Module_is_Not_Active), false);
    }

    /**
     * 改变音质
     *
     * @param high
     */
    private void changeQuality(boolean high) {
        String packageJson = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "package.json");
        String kuwo = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "provider" + File.separator + "select.js");
//        String migu = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "provider" + File.separator + "migu.js");
        String hook = Tools.readFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "hook.js");
        String localVersionString = "";
        try {
            JSONObject jsonObject = new JSONObject(packageJson);
            localVersionString = jsonObject.getString("version");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (high) {
            if (!packageJson.contains("-high"))
                packageJson = packageJson.replace(localVersionString, localVersionString + "-high");
            if (!kuwo.contains("\nmodule.exports.ENABLE_FLAC = 'true'"))
                kuwo = kuwo + "\nmodule.exports.ENABLE_FLAC = 'true'";
//            migu = migu.replace("/*'sqPlayInfo'*/,", "'sqPlayInfo',");
            hook = hook.replace("(item.code != 200 || item.freeTrialInfo)", "(item.code != 200 || item.freeTrialInfo || item.br <= 128000)");
        } else {
            packageJson = packageJson.replace(localVersionString, localVersionString.replace("-high", ""));
            kuwo = kuwo.replace("\n\nmodule.exports.ENABLE_FLAC = 'true'", "");
//            migu = migu.replace("'sqPlayInfo',", "/*'sqPlayInfo'*/,");
            hook = hook.replace("(item.code != 200 || item.freeTrialInfo || item.br <= 128000)", "(item.code != 200 || item.freeTrialInfo)");
        }
        Tools.writeFileFromSD(Tools.SDCardPath + File.separator + "package.json", packageJson);
        Tools.writeFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "provider" + File.separator + "select.js", kuwo);
//        Tools.writeFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "provider" + File.separator + "migu.js", migu);
        Tools.writeFileFromSD(Tools.SDCardPath + File.separator + "src" + File.separator + "hook.js", hook);
        initData();
    }

    private boolean isExpModuleActive() {
        boolean isExp = false;

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = contentResolver.call(uri, "active", null, null);
            if (result == null) {
                return false;
            }
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    private static boolean isModuleActive() {
        return false;
    }

    private boolean isVXP() {
        return System.getProperty("vxp") != null;
    }

    private void showMessageDialog(final String title, final String message, final boolean finish) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (finish)
                            finish();
                    }
                })
                .show();
    }

    private void showUpdateDialog(final String title, final String extra, final Update update) {
        String log = update.log;
        if (extra != null && extra.length() != 0)
            log = log + "\n\n" + extra;
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(log)
                .setNegativeButton("取消", null)
                .setNeutralButton("无法下载，访问蓝奏云", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, "23fj");
                        Toast.makeText(context, "密码已复制到剪切板", Toast.LENGTH_SHORT).show();
                        clipboard.setPrimaryClip(clipData);
                        Uri uri = Uri.parse("https://www.lanzous.com/b957345");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = update.downloadUrl;
                        if (url.length() == 0)
                            url = update.zipUrl;
                        new FILE_DOWNLOAD(context, url, R.mipmap.ic_launcher, true, true, new NetCallBack() {
                            @Override
                            public void finish(JSONObject jsonObject) throws JSONException {
                                if (update.downloadUrl.length() == 0) {
                                    iv_script.setVisibility(View.GONE);
                                    String path = jsonObject.getString("path");
                                    if (Tools.unzipFile(path, Tools.SDCardPath))
                                        Toast.makeText(context, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show();
                                    changeQuality(cb_high.isChecked());
                                }
                            }

                            @Override
                            public void error(int i, String s) {
                                ErrorCode.showError(context, i);
                            }

                            @Override
                            public void init() {

                            }
                        });
                    }
                })
                .show();
    }
}
