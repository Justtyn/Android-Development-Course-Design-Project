package com.justyn.meow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.justyn.meow.auth.LoginActivity;
import com.justyn.meow.cat.CatFmActivity;
import com.justyn.meow.cat.CatPicActivity;
import com.justyn.meow.cat.CatProfileActivity;
import com.justyn.meow.cat.CatWikiActivity;
import com.justyn.meow.checkin.CheckInCalendarActivity;
import com.justyn.meow.checkin.CheckInStore;
import com.justyn.meow.util.MeowPreferences;

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

        // 绑定当前用户
        bindCurrentUser();

        // 初始化打卡显示
        int streak = CheckInStore.getStreak(this);
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
        cardCheckIn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CheckInCalendarActivity.class))
        );
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
     * 更新打卡卡片副标题显示。
     */
    private void updateCheckInText(int streak) {
        String text = "已连续打卡 " + streak + " 天";
        tvCheckInSub.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCheckInText(CheckInStore.getStreak(this));
    }
}
