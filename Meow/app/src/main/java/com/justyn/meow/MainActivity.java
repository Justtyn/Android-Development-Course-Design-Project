package com.justyn.meow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.justyn.meow.auth.LoginActivity;
import com.justyn.meow.cat.CatFmActivity;
import com.justyn.meow.cat.CatPicActivity;
import com.justyn.meow.cat.CatProfileActivity;
import com.justyn.meow.cat.CatWikiActivity;
import com.justyn.meow.util.MeowPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 主入口页面：展示功能卡片、当前用户信息与打卡状态。
 * <p>
 * 这里负责：
 * 1) 读取登录态并在未登录时跳转到登录页
 * 2) 展示用户昵称/用户名
 * 3) 处理功能入口跳转
 * 4) 记录并显示“撸猫打卡”连续天数
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    // 首页顶部“当前用户”文案
    private TextView tvCurrentUserName;

    // 打卡卡片副标题（展示连续天数）
    private TextView tvCheckInSub;

    // 打卡相关常量
    private static final String PREFS_NAME = "meow_checkin_prefs";
    private static final String KEY_LAST_CHECKIN_DATE = "last_checkin_date";
    private static final String KEY_STREAK = "checkin_streak";

    // 保存打卡状态的 SharedPreferences
    private SharedPreferences checkInPrefs;
    private String currentUsername;
    private String keyLastCheckInDate;
    private String keyStreak;

    /**
     * 初始化主界面：校验登录态、绑定控件与注册点击事件。
     * <p>
     * 主要流程：
     * - 未登录则直接跳转登录页
     * - 绑定 UI 控件与功能卡片点击
     * - 恢复打卡连续天数并展示
     * </p>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 没登陆，踢回登陆页
        if (!MeowPreferences.isLoggedIn(this)) {
            goLoginAndFinish();
            return;
        }

        // 基本控件
        tvCurrentUserName = findViewById(R.id.tvCurrentUserName);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        MaterialCardView cardCatProfile = findViewById(R.id.cardCatProfile);
        MaterialCardView cardMeowFM = findViewById(R.id.cardMeowFM);
        MaterialCardView cardCatWiki = findViewById(R.id.cardCatWiki);
        MaterialCardView cardMeowPic = findViewById(R.id.cardMeowPic);

        // 打卡相关控件
        MaterialCardView cardCheckIn = findViewById(R.id.cardCheckIn);
        tvCheckInSub = findViewById(R.id.tvCheckInSub);

        // 打卡持久化
        initCheckInPrefs();

        // 绑定当前用户
        bindCurrentUser();

        // 初始化打卡显示
        int streak = checkInPrefs.getInt(keyStreak, 0);
        updateCheckInText(streak);

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            MeowPreferences.clearLogin(MainActivity.this);
            goLoginAndFinish();
        });

        // 功能卡片点击
        cardCatProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatProfileActivity.class))
        );

        cardMeowFM.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatFmActivity.class))
        );

        cardCatWiki.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatWikiActivity.class))
        );

        cardMeowPic.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatPicActivity.class))
        );

        // 撸猫打卡卡片点击
        cardCheckIn.setOnClickListener(v -> handleCheckIn());
    }

    /**
     * 跳转到登录页并清空返回栈，避免返回到主界面。
     */
    private void goLoginAndFinish() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // 清空栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // 结束当前页面
        finish();
    }

    /**
     * 绑定“当前用户”显示文本。
     * 优先显示昵称，其次显示用户名，最后使用兜底文案。
     */
    private void bindCurrentUser() {
        // 优先显示昵称，没有昵称就显示账号
        String nickname = MeowPreferences.getNickname(this);
        String username = MeowPreferences.getUsername(this);

        String displayName;
        if (!TextUtils.isEmpty(nickname)) {
            displayName = nickname;
        } else if (!TextUtils.isEmpty(username)) {
            displayName = username;
        } else {
            displayName = "神秘铲屎官";
        }
        tvCurrentUserName.setText(displayName);
    }

    /**
     * 打卡逻辑：
     * - 若今天已打卡则直接提示
     * - 若是首次或断档则从 1 重新开始
     * - 若昨天打卡过则连续天数 +1
     */
    private void handleCheckIn() {
        String today = getTodayString(); // 例如 2025-12-08
        String lastDate = checkInPrefs.getString(keyLastCheckInDate, null);
        int streak = checkInPrefs.getInt(keyStreak, 0);

        // 如果今天已经打卡了
        if (today.equals(lastDate)) {
            Toast.makeText(this, "今天已经撸猫打卡过啦！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 第一次打卡 或者 lastDate 无效
        if (lastDate == null) {
            streak = 1;
        } else if (isYesterday(lastDate, today)) {
            // 昨天打过卡，今天接着打 ⇒ 连续 +1
            streak = streak + 1;
        } else {
            // 中间断了，从 1 重新开始
            streak = 1;
        }

        // 保存新数据
        checkInPrefs.edit()
                .putString(keyLastCheckInDate, today)
                .putInt(keyStreak, streak)
                .apply();

        // 更新 UI
        updateCheckInText(streak);
        Toast.makeText(this, "撸猫打卡成功！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新打卡卡片副标题显示。
     */
    private void updateCheckInText(int streak) {
        String text = "已连续打卡 " + streak + " 天";
        tvCheckInSub.setText(text);
    }

    private void initCheckInPrefs() {
        currentUsername = MeowPreferences.getUsername(this);
        checkInPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        keyLastCheckInDate = buildCheckInKey(KEY_LAST_CHECKIN_DATE);
        keyStreak = buildCheckInKey(KEY_STREAK);
        migrateLegacyCheckInIfNeeded();
    }

    private String buildCheckInKey(String baseKey) {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            return baseKey;
        }
        return baseKey + "_" + currentUsername;
    }

    private void migrateLegacyCheckInIfNeeded() {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            return;
        }
        if (checkInPrefs.contains(keyStreak) || checkInPrefs.contains(keyLastCheckInDate)) {
            return;
        }
        SharedPreferences.Editor editor = checkInPrefs.edit();
        if (checkInPrefs.contains(KEY_STREAK)) {
            editor.putInt(keyStreak, checkInPrefs.getInt(KEY_STREAK, 0));
        }
        if (checkInPrefs.contains(KEY_LAST_CHECKIN_DATE)) {
            editor.putString(keyLastCheckInDate, checkInPrefs.getString(KEY_LAST_CHECKIN_DATE, null));
        }
        editor.apply();
    }

    /**
     * 获取今天的日期字符串（yyyy-MM-dd）。
     */
    private String getTodayString() {
        // 格式：yyyy-MM-dd，比如 2025-12-08
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 判断 lastDate 是否为 today 的前一天。
     *
     * @param lastDateStr 上次打卡日期字符串
     * @param todayStr    今天日期字符串
     * @return true 表示连续，false 表示不连续或解析失败
     */
    private boolean isYesterday(String lastDateStr, String todayStr) {
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date last = sdf.parse(lastDateStr);
            Date today = sdf.parse(todayStr);
            if (last == null || today == null) return false;

            long diffMs = today.getTime() - last.getTime();
            long days = diffMs / (1000 * 60 * 60 * 24);
            return days == 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
