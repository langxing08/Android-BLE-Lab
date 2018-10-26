package com.example.bletest.tool;

import android.content.Context;
import android.widget.Toast;

public class Utils {

    private static Toast toast;

    /**
     * 多滴调用Toast时, 只显示最后一个Toast
     * @param context  上下文对象
     * @param msg  内容
     */
    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }

        toast.show();
    }
}
