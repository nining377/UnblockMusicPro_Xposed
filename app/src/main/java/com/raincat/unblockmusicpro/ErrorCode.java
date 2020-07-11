package com.raincat.unblockmusicpro;

import android.content.Context;
import android.widget.Toast;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/16
 *     desc   : 错误代码
 *     version: 1.0
 * </pre>
 */

public class ErrorCode {
    public static void showError(Context context, int errorCode) {
            switch (errorCode) {
                case 1:
                    Toast.makeText(context, "网络访问已取消",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                case 3:
                case 4:
                case 7:
//                    Toast.makeText(context, "网络连接失败，请查看你的网络状态",Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "检查更新失败，请确认github是否被墙",Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(context, "数据解析错误，请重试",Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(context, "未知错误",Toast.LENGTH_SHORT).show();
                    break;
        }
    }
}
