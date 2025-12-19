package com.justyn.meow.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.meow.MainActivity;
import com.justyn.meow.R;
import com.justyn.meow.data.MeowDbHelper;
import com.justyn.meow.util.MeowPreferences;

/**
 * 登录页面：校验本地账号密码并保存登录态。
 * <p>
 * 逻辑要点：
 * - 启动时先检查是否已登录
 * - 输入校验通过后查询本地数据库
 * - 登录成功则持久化登录态并跳转主界面
 * </p>
 */
public class LoginActivity extends AppCompatActivity {

    // 数据库帮助类，用于校验账号密码
    private MeowDbHelper dbHelper;
    // 用户名输入框
    private TextInputEditText etUsername;
    // 密码输入框
    private TextInputEditText etPassword;

    /**
     * 初始化登录页：
     * - 已登录则直接跳转主界面并结束当前页
     * - 未登录则初始化布局与事件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从偏好里读登录态，已登录就跳转并 finish，避免回退到登录页
        if (MeowPreferences.isLoggedIn(this)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // DB helper 用 Activity 的 Context 初始化，后面校验账号密码会用到
        dbHelper = new MeowDbHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        // 登录走校验流程，注册直接跳注册页
        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    /**
     * 执行登录流程：
     * - 从输入框读取用户名/密码
     * - 判空后查询数据库校验
     * - 成功则保存登录态并进入主界面
     * - 失败则提示错误
     */
    private void doLogin() {
        // 用户名去掉前后空格，密码保留原样避免改变用户输入
        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        // 判空先拦截，减少无意义的 DB 查询
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "喵～，请输入账号和密码喔", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbHelper.checkLogin(username, password);
        if (ok) {
            // 登录通过后补一份昵称，没有就用空串
            String nickname = dbHelper.getNicknameByUsername(username);
            if (nickname == null) {
                nickname = "";
            }
            // 保存到 SharedPreferences，作为下次启动的登录态依据
            MeowPreferences.saveLogin(this, username, nickname);
            // 成功提示后跳转并 finish，避免回退到登录页
            Toast.makeText(this, "登录成功，喵~！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 校验失败就提示错误
            Toast.makeText(this, "喵～，账号或密码错误了喔", Toast.LENGTH_SHORT).show();
        }
    }

}
