package com.raincat.unblockmusicpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限代理，坑爹的安卓6.0
 *
 * @author 躲雨的猫
 */
public class PermissionProxyActivity extends AppCompatActivity {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static AlertDialog dialog;
    /**
     * 存储卡访问
     */
    public static String PERMISSION_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    /**
     * 存储卡访问
     */
    public static String PERMISSION_EXTERNAL_STORAGE2 = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public OnPermissionResultListener back;
    private Context context;

    public interface OnPermissionResultListener {
        void onResult(boolean get);
    }

    /**
     * 检查权限
     *
     * @param permissions 权限列表
     * @param b
     */
    public void checkPermission(final Context c, final String[] permissions, OnPermissionResultListener b) {
        back = b;
        context = c;
        // Android 6.0以上才用询问权限
        if (Build.VERSION.SDK_INT < 23) {
            PermissionsRequest(back, true);
            return;
        }

        List<String> permissionsNeed = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED)
                permissionsNeed.add(permissions[i]);
        }

        if (permissionsNeed.size() != 0) {
            ActivityCompat.requestPermissions((Activity) context, permissionsNeed.toArray(new String[permissionsNeed.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        } else
            PermissionsRequest(back, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            PermissionsRequest(back, true);
        }
    }

    @SuppressLint("Override")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length <= 0) {
                    PermissionsRequest(back, true);
                    break;
                }
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel(i, permissions);
                        return;
                    }
                }
                PermissionsRequest(back, true);
                break;
        }
    }

    private void showMessageOKCancel(int i, String[] permissions) {
        String permissionText = getString(R.string.app_name)+"需要“存储卡访问”权限释放必要文件，请点击“前往设置”按钮，在“权限”选项中打开“存储”权限后重试！";
        if (dialog != null)
            dialog.dismiss();

        dialog = new AlertDialog.Builder(context).setCancelable(false).setTitle("提示").setMessage(permissionText).setPositiveButton("前往设置", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri packageURI = Uri.parse("package:" + context.getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                context.startActivity(intent);
            }
        }).setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PermissionsRequest(back, false);
            }
        }).create();
        dialog.show();
    }

    private static void PermissionsRequest(OnPermissionResultListener b, boolean get) {
        if (b != null)
            b.onResult(get);
    }
}
