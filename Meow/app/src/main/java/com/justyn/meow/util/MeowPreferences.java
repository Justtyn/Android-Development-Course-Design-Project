package com.justyn.meow.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MeowPreferences {

    private static final String PREF_NAME = "meow_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "current_username";
    private static final String KEY_NICKNAME = "current_nickname";

    // 获取 SharedPreferences 实例
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 登录成功时保存用户信息
     */
    public static void saveLogin(Context context, String username, String nickname) {
        SharedPreferences sp = getPrefs(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NICKNAME, nickname);
        editor.apply();  // 异步提交
    }

    /**
     * 退出登录时清除
     */
    public static void clearLogin(Context context) {
        SharedPreferences sp = getPrefs(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_NICKNAME);
        editor.apply();
    }

    /**
     * 是否已经登录
     */
    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_USERNAME, null);
    }

    public static String getNickname(Context context) {
        return getPrefs(context).getString(KEY_NICKNAME, null);
    }
}
