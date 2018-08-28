package com.ywl5320.wlivefm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 存储类
 *
 */
public class SharedpreferencesUtil {

    /**
     * 存储int类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     * @param v        存储的值
     */
    public static void write(Context context, String fileName, String k, int v) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = preference.edit();
        editor.putInt(k, v);
        editor.apply();
    }

    /**
     * 存储boolean类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     * @param v        存储的值
     */
    public static void write(Context context, String fileName, String k,
                             boolean v) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = preference.edit();
        editor.putBoolean(k, v);
        editor.apply();
    }

    /**
     * 存储String类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     * @param v        存储的值
     */
    public static void write(Context context, String fileName, String k,
                             String v) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = preference.edit();
        editor.putString(k, v);
        editor.apply();
    }

    /**
     * 读取int类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     */
    public static int readInt(Context context, String fileName, String k) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getInt(k, 0);
    }

    public static int readInt(Context context, String fileName, String k,
                              int defv) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getInt(k, defv);
    }

    /**
     * 读取boolean类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     */
    public static boolean readBoolean(Context context, String fileName, String k) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getBoolean(k, false);
    }

    public static boolean readBoolean(Context context, String fileName,
                                      String k, boolean defBool) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getBoolean(k, defBool);
    }

    /**
     * 读取String类型
     *
     * @param context  上下文
     * @param fileName 文件名
     * @param k        keyID
     */
    public static String readString(Context context, String fileName, String k) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getString(k, null);
    }

    public static String readString(Context context, String fileName, String k,
                                    String defV) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return preference.getString(k, defV);
    }

    /**
     * 删除SharedPreferences中Editor值
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    public static void remove(Context context, String fileName, String k) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = preference.edit();
        editor.remove(k);
        editor.apply();
    }

    /**
     * 清除SharedPreferences中Editor值
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    public static void clean(Context context, String fileName) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = preference.edit();
        editor.clear();
        editor.apply();
    }
}
