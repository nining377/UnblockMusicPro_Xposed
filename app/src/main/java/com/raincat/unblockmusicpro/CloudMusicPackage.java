package com.raincat.unblockmusicpro;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.annimon.stream.Stream;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.XposedHelpers;

import static com.raincat.unblockmusicpro.CloudMusicPackage.ClassHelper.getFilteredClasses;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/09
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */

public class CloudMusicPackage {
    private static WeakReference<List<String>> allClassList = new WeakReference<>(null);

    static void init(Context context) throws PackageManager.NameNotFoundException {
        NeteaseMusicApplication.init(context);
    }

    public static ClassLoader getClassLoader() {
        try {
            return NeteaseMusicApplication.getApplication().getClassLoader();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ClassHelper {
        private static File getApkPath() throws PackageManager.NameNotFoundException, IllegalAccessException {
            ApplicationInfo applicationInfo = CloudMusicPackage.NeteaseMusicApplication.getApplication().getPackageManager().getApplicationInfo(Tools.HOOK_NAME, 0);
            return new File(applicationInfo.sourceDir);
        }


        static List<String> getAllClasses() {
            List<String> list = allClassList.get();
            if (list == null) {
                list = new ArrayList<>();

                try {
                    File apkFile = getApkPath();
                    // 不用 ZipDexContainer 因为会验证zip里面的文件是不是dex，会慢一点
                    Enumeration zip = new ZipFile(apkFile).entries();
                    while (zip.hasMoreElements()) {
                        ZipEntry dexInZip = (ZipEntry) zip.nextElement();
                        if (dexInZip.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile = DexFileFactory.loadDexEntry(apkFile, dexInZip.getName(), true, null);
                            for (DexBackedClassDef classDef : dexFile.getClasses()) {
                                String classType = classDef.getType();
                                classType = classType.substring(1, classType.length() - 1).replace("/", ".");
                                list.add(classType);
                            }
                        }
                    }

                    allClassList = new WeakReference<>(list);

                } catch (Throwable t) {
                }
            }
            return list;
        }

        public static List<String> getFilteredClasses(Pattern pattern, Comparator<String> comparator) {
            List<String> list = Tools.filterList(getAllClasses(), pattern);
            Collections.sort(list, comparator);
            return list;
        }

        public static List<String> getFilteredClasses(String start, String end, Comparator<String> comparator) {
            List<String> list = Tools.filterList(getAllClasses(), start, end);
            Collections.sort(list, comparator);
            return list;
        }
    }

    public static class Transfer {
        private static Method calcMd5Method;

        public static Method getCalcMd5Method() {
            if (calcMd5Method == null) {
                Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.transfer\\.[a-z]\\.[a-z]$");
                List<String> list = getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    calcMd5Method = Stream.of(list)
                            .map(c -> findClass(c, getClassLoader()).getDeclaredMethods())
                            .flatMap(Stream::of)
                            .filter(m -> m.getReturnType() == String.class)
                            .filter(m -> m.getParameterTypes().length == 2)
                            .filter(m -> m.getParameterTypes()[0] == File.class)
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("can't find getCalcMd5Method");
                }
            }
            return calcMd5Method;
        }
    }

    static class NeteaseMusicApplication {
        private static Class clazz;
        private static Field singletonField;

        static void init(Context context) {
            clazz = findClass("com.netease.cloudmusic.NeteaseMusicApplication", context.getClassLoader());
            singletonField = XposedHelpers.findFirstFieldByExactType(getClazz(), getClazz());
        }

        static Class getClazz() {
            return clazz;
        }

        static Application getApplication() throws IllegalAccessException {
            return (Application) singletonField.get(null);
        }
    }
}
