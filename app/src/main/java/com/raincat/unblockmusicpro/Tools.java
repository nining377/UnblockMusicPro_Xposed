package com.raincat.unblockmusicpro;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

public class Tools {
    final static String HOOK_NAME = "com.netease.cloudmusic";

    final static String SDCardPath = Environment.getExternalStorageDirectory() + "/UnblockMusicPro";
    final static String Start = "./node app.js -o ";
    final static String origin[] = new String[]{"kuwo migu kugou qq", "migu kuwo kugou qq", "kugou kuwo migu qq", "qq kuwo migu kugou"};
    final static int originResId[] = new int[]{R.id.rb_a, R.id.rb_b, R.id.rb_c, R.id.rb_d};
    final static int originString[] = new int[]{R.string.kuwo, R.string.migu, R.string.kugou, R.string.qq};
    final static String State = "[ \"`pgrep node`\" != \"\" ] && echo YES";
    final static String Stop = "killall -9 node >/dev/null 2>&1";
    final static String nowVersion = "0.19.1";
    final static String message = "酷我：音质高，部分可下载无损\n" +
            "咪咕：酷我没有的歌用咪咕试试\n" +
            "酷狗：同上\n" +
            "ＱＱ：叫爸爸，歌曲多但音质较差。";

    /**
     * 获取线程名称
     *
     * @param context
     * @return
     */
    static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        throw new RuntimeException("can't get current process name");
    }

    static void copyFilesAssets(Context context, String oldPath, String codePath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(codePath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesAssets(context, oldPath + "/" + fileName, codePath + "/" + fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(codePath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void copyFilesFromSD(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            newFile.mkdirs();
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFilesFromSD(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String loadFileFromSD(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(path);
        if (!file.isDirectory()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                inputStream.close();
            } catch (Exception e) {
                Log.d("Tools", e.getMessage());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 判断是否为64bit手机
     *
     * @return true是64位
     */
    static boolean is64BitImpl() {
        try {
            Class<?> clzVMRuntime = Class.forName("dalvik.system.VMRuntime");
            if (clzVMRuntime == null) {
                return false;
            }
            Method mthVMRuntimeGet = clzVMRuntime.getDeclaredMethod("getRuntime");
            if (mthVMRuntimeGet == null) {
                return false;
            }
            Object objVMRuntime = mthVMRuntimeGet.invoke(null);
            if (objVMRuntime == null) {
                return false;
            }
            Method sVMRuntimeIs64BitMethod = clzVMRuntime.getDeclaredMethod("is64Bit");
            if (sVMRuntimeIs64BitMethod == null) {
                return false;
            }
            Object objIs64Bit = sVMRuntimeIs64BitMethod.invoke(objVMRuntime);
            if (objIs64Bit instanceof Boolean) {
                return (boolean) objIs64Bit;
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }

    static void shell(Command command) {
        try {
            RootTools.closeAllShells();
            RootTools.getShell(false).add(command);
        } catch (TimeoutException | RootDeniedException | IOException e) {
            e.printStackTrace();
        }
    }

    static List<String> filterList(List<String> list, Pattern pattern) {
        return Stream.of(list)
                .filter(s -> pattern.matcher(s).find())
                .toList();
    }

    static List<String> filterList(List<String> list, String start, String end) {
        return Stream.of(list)
                .filter(s -> TextUtils.isEmpty(start) || s.startsWith(start))
                .filter(s -> TextUtils.isEmpty(end) || s.endsWith(end))
                .toList();
    }
}