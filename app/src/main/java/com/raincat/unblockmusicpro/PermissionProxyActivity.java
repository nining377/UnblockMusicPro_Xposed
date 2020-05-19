package com.raincat.unblockmusicpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 说明
 *     version: 1.0
 * </pre>
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
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                permissionsNeed.add(permission);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length <= 0) {
                PermissionsRequest(back, true);
                return;
            }
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    showMessageOKCancel(i, permissions);
                    return;
                }
            }
            PermissionsRequest(back, true);
        }
    }

    private void showMessageOKCancel(int i, String[] permissions) {
        String permissionText = getString(R.string.app_name) + "需要“存储卡访问”权限释放必要文件，请点击“前往设置”按钮，在“权限”选项中打开“存储”权限后重试！";
        if (dialog != null)
            dialog.dismiss();

        dialog = new AlertDialog.Builder(context).setCancelable(false).setTitle("提示").setMessage(permissionText).setPositiveButton("前往设置", (dialog, which) -> {
            Uri packageURI = Uri.parse("package:" + context.getPackageName());
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            context.startActivity(intent);
        }).setNegativeButton("取消", (dialog, which) -> PermissionsRequest(back, false)).create();
        dialog.show();
    }

    private static void PermissionsRequest(OnPermissionResultListener b, boolean get) {
        if (b != null)
            b.onResult(get);
    }
}
