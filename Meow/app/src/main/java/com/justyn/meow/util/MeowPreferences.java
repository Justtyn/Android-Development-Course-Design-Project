package com.justyn.meow.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类：统一管理登录态与首次数据初始化标记。
 */
public class MeowPreferences {

    // SharedPreferences 文件名
    private static final String PREF_NAME = "meow_prefs";
    // 登录态与用户信息 key
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "current_username";
    private static final String KEY_NICKNAME = "current_nickname";
    // 初始化数据标记 key
    private static final String KEY_SEEDED_FM = "seeded_fm";
    private static final String KEY_SEEDED_CAT_PROFILE = "seeded_cat_profile";

    /**
     * 获取 SharedPreferences 实例。
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 登录成功时保存用户信息
     */
    public static void saveLogin(Context context, String username, String nickname) {
        SharedPreferences sp = getPrefs(context);
        SharedPreferences.Editor editor = sp.edit();
        // 标记为已登录并保存账号信息
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
        // 仅清理登录相关字段
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

    /**
     * 获取当前登录用户名。
     */
    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_USERNAME, null);
    }

    /**
     * 获取当前登录昵称。
     */
    public static String getNickname(Context context) {
        return getPrefs(context).getString(KEY_NICKNAME, null);
    }

    /**
     * 判断 FM 默认数据是否已初始化。
     */
    public static boolean isFmSeeded(Context context) {
        return getPrefs(context).getBoolean(KEY_SEEDED_FM, false);
    }

    /**
     * 标记 FM 默认数据已初始化。
     */
    public static void markFmSeeded(Context context) {
        getPrefs(context).edit().putBoolean(KEY_SEEDED_FM, true).apply();
    }

    /**
     * 判断猫咪档案默认数据是否已初始化。
     */
    public static boolean isCatProfileSeeded(Context context) {
        return getPrefs(context).getBoolean(KEY_SEEDED_CAT_PROFILE, false);
    }

    /**
     * 标记猫咪档案默认数据已初始化。
     */
    public static void markCatProfileSeeded(Context context) {
        getPrefs(context).edit().putBoolean(KEY_SEEDED_CAT_PROFILE, true).apply();
    }
}
