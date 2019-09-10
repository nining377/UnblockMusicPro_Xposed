package com.raincat.unblockmusicpro;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * <pre>
 *     author : RainCat
 *     org    : Shenzhen JingYu Network Technology Co., Ltd.
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends PermissionProxyActivity {
    private Context context;
    private RelativeLayout rela_enable, rela_hide, rela_log;
    private CheckBox cb_enable, cb_hide, cb_log;
    private TextView tv_update, tv_faq, tv_version, tv_script, tv_perfect[];
    private ImageView iv_question;
    private RadioGroup rg_origin;

    private int originIndex = 0;
    private SharedPreferences share;

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
                if (get)
                    initData();
                else
                    finish();
            }
        });
    }

    private void initData() {
        //比对版本
        String version = Tools.loadFileFromSD(Tools.SDCardPath + File.separator + "package.json");
        String localVersion = "";
        try {
            if (version.length() != 0) {
                JSONObject jsonObject = new JSONObject(version);
                localVersion = jsonObject.getString("version");
            }
            if (!localVersion.equals(Tools.nowVersion)) {
                //复制核心文件文件到SD卡
                Tools.copyFilesAssets(context, "UnblockNeteaseMusic-" + Tools.nowVersion, Tools.SDCardPath);
                Tools.copyFilesAssets(context, "shell", Tools.SDCardPath);
                if (Tools.is64BitImpl())
                    Tools.copyFilesAssets(context, "node-64bit", Tools.SDCardPath);
                else
                    Tools.copyFilesAssets(context, "node-32bit", Tools.SDCardPath);
            }
            Tools.copyFilesAssets(context, "log", Tools.SDCardPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tv_script.setText(localVersion);
        int app_version = share.getInt("app_version", 0);
        if (app_version < BuildConfig.VERSION_CODE) {
            String update = Tools.loadFileFromSD(Tools.SDCardPath + File.separator + "update.txt");
            showMessageDialog(getString(R.string.menu_update), update, false);
        }
        share.edit().putInt("app_version", BuildConfig.VERSION_CODE).apply();
    }

    private void initView() {
        rela_enable = (RelativeLayout) findViewById(R.id.rela_enable);
        rela_hide = (RelativeLayout) findViewById(R.id.rela_hide);
        rela_log = (RelativeLayout) findViewById(R.id.rela_log);
        tv_update = (TextView) findViewById(R.id.tv_update);
        tv_faq = (TextView) findViewById(R.id.tv_faq);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_script = (TextView) findViewById(R.id.tv_script);
        tv_perfect = new TextView[2];
        tv_perfect[0] = (TextView) findViewById(R.id.tv_perfect1);
        tv_perfect[1] = (TextView) findViewById(R.id.tv_perfect2);
        iv_question = (ImageView) findViewById(R.id.iv_question);
        rg_origin = (RadioGroup) findViewById(R.id.rg_origin);
        cb_enable = (CheckBox) findViewById(R.id.cb_enable);
        cb_hide = (CheckBox) findViewById(R.id.cb_hide);
        cb_log = (CheckBox) findViewById(R.id.cb_log);

        tv_version.setText(BuildConfig.VERSION_NAME);
        tv_perfect[0].setText("Google：4.3.1");
        tv_perfect[1].setText("Global：6.0.0～6.4.0");

        share = getSharedPreferences("share", Context.MODE_WORLD_READABLE);
        originIndex = share.getInt("origin", 0);
        rg_origin.check(Tools.originResId[originIndex]);
        cb_enable.setChecked(share.getBoolean("enable", true));
        cb_hide.setChecked(share.getBoolean("hide", false));
        cb_log.setChecked(share.getBoolean("log", false));
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
                String update = Tools.loadFileFromSD(Tools.SDCardPath + File.separator + "update.txt");
                showMessageDialog(getString(R.string.menu_update), update, false);
            }
        });

        tv_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FAQ = Tools.loadFileFromSD(Tools.SDCardPath + File.separator + "FAQ.txt");
                showMessageDialog(getString(R.string.menu_faq), FAQ, false);
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
}
