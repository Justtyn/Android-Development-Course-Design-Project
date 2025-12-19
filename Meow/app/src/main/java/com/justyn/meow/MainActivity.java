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

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentUserName;

    private TextView tvCheckInSub;

    // 打卡相关常量
    private static final String PREFS_NAME = "meow_checkin_prefs";
    private static final String KEY_LAST_CHECKIN_DATE = "last_checkin_date";
    private static final String KEY_STREAK = "checkin_streak";

    private SharedPreferences checkInPrefs;

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

        // 打卡控件
        // 打卡相关控件
        MaterialCardView cardCheckIn = findViewById(R.id.cardCheckIn);
        tvCheckInSub = findViewById(R.id.tvCheckInSub);

        // 打卡持久化
        checkInPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 绑定当前用户
        bindCurrentUser();

        // 初始化打卡显示
        int streak = checkInPrefs.getInt(KEY_STREAK, 0);
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

    private void goLoginAndFinish() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // 清空栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // 结束当前页面
        finish();
    }

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

    //   打卡逻辑
    private void handleCheckIn() {
        String today = getTodayString(); // 例如 2025-12-08
        String lastDate = checkInPrefs.getString(KEY_LAST_CHECKIN_DATE, null);
        int streak = checkInPrefs.getInt(KEY_STREAK, 0);

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
                .putString(KEY_LAST_CHECKIN_DATE, today)
                .putInt(KEY_STREAK, streak)
                .apply();

        // 更新 UI
        updateCheckInText(streak);
        Toast.makeText(this, "撸猫打卡成功！", Toast.LENGTH_SHORT).show();
    }

    private void updateCheckInText(int streak) {
        String text = "已连续打卡 " + streak + " 天";
        tvCheckInSub.setText(text);
    }

    private String getTodayString() {
        // 格式：yyyy-MM-dd，比如 2025-12-08
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

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
