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
import com.justyn.meow.cat.CatProfileActivity;
import com.justyn.meow.cat.CatWikiActivity;
import com.justyn.meow.cat.FeedLogActivity;
import com.justyn.meow.util.MeowPreferences;

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentUserName;
    private MaterialButton btnLogout;

    private MaterialCardView cardCatProfile;
    private MaterialCardView cardFeedLog;
    private MaterialCardView cardMeowFM;
    private MaterialCardView cardCatWiki;

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

        tvCurrentUserName = findViewById(R.id.tvCurrentUserName);
        btnLogout = findViewById(R.id.btnLogout);

        cardCatProfile = findViewById(R.id.cardCatProfile);
        cardFeedLog = findViewById(R.id.cardFeedLog);
        cardMeowFM = findViewById(R.id.cardMeowFM);
        cardCatWiki = findViewById(R.id.cardCatWiki);

        // 绑定当前用户
        bindCurrentUser();

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            MeowPreferences.clearLogin(MainActivity.this);
            goLoginAndFinish();
        });

        cardCatProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatProfileActivity.class))
        );

        cardFeedLog.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FeedLogActivity.class))
        );

        cardMeowFM.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatFmActivity.class))
        );

        cardCatWiki.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CatWikiActivity.class))
        );
    }

    private void goLoginAndFinish() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // 清空栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // 保险，finish 当前页面
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
}
