package com.raincat.unblockmusicpro;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 工具类
 *     version: 1.0
 * </pre>
 */

public class Tools {
    static String nowVersion = "0.20.0";

    final static String HOOK_NAME = "com.netease.cloudmusic";
    final static String SDCardPath = Environment.getExternalStorageDirectory() + "/UnblockMusicPro";
    final static String neteaseCachePath = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Ad";
    final static String Start = "./node app.js -o ";
    final static String origin[] = new String[]{"kuwo migu kugou qq", "migu kuwo kugou qq", "kugou kuwo migu qq", "qq kuwo migu kugou"};
    final static int originResId[] = new int[]{R.id.rb_a, R.id.rb_b, R.id.rb_c, R.id.rb_d};
    final static int originString[] = new int[]{R.string.kuwo, R.string.migu, R.string.kugou, R.string.qq};
    final static String State = "[ \"`pgrep node`\" != \"\" ] && echo YES";
    final static String Stop = "killall -9 node >/dev/null 2>&1";
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

    /**
     * 从assets中拷贝文件
     *
     * @param context
     * @param oldPath
     * @param codePath
     */
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

    /**
     * 从SD卡中拷贝文件
     *
     * @param oldPath
     * @param newPath
     */
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

    /**
     * 从SD卡中读取一个文件
     *
     * @param path
     * @return
     */
    static String readFileFromSD(String path) {
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
     * 写入内容到一个文件
     *
     * @param path
     * @param content
     * @return
     */
    static boolean writeFileFromSD(String path, String content) {
        BufferedWriter out = null;
        try {
            File file = new File(path);
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "utf-8"));
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    static boolean unzipFile(String zipFileString, String outPathString) {
        try {
            // 创建解压目标目录
            File outPath = new File(outPathString);
            // 如果目标目录不存在，则创建
            if (!outPath.exists()) {
                outPath.mkdirs();
            }

            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
            ZipEntry zipEntry;
            String folderName = "";
            String szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // 读取（字节）字节到缓冲区
                    while ((len = inZip.read(buffer)) != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            inZip.close();
            szName = szName.substring(0, szName.lastIndexOf(File.separator));
            copyFilesFromSD(outPathString + File.separator + szName, outPathString);
            deleteDirectory(outPathString + File.separator + szName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    /**
     * 弹窗
     *
     * @param context
     * @param message
     */
    static void showToastOnLooper(final Context context, String message) {
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    /**
     * ADB命令
     *
     * @param context
     * @param command
     */
    static void shell(Context context, Command command) {
        try {
            RootTools.closeAllShells();
            RootTools.getShell(false).add(command);
        } catch (TimeoutException | RootDeniedException | IOException e) {
            e.printStackTrace();
            showToastOnLooper(context, e.getMessage());
        }
    }

    /**
     * 删除指定文件夹下所有文件及文件夹本身
     *
     * @param path
     */
    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取CA证书
     *
     * @param caPath
     * @return
     */
    static SSLContext getSLLContext(String caPath) {
        SSLContext sslContext = null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            File ca = new File(caPath);
            InputStream certificate = new FileInputStream(ca);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
            sslContext = SSLContext.getInstance("TLS");
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext;
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