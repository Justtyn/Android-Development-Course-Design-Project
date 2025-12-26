package com.justyn.meow.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.meow.R;
import com.justyn.meow.data.MeowDbHelper;

/**
 * 注册页面：创建新用户并写入本地数据库。
 * <p>
 * 主要流程：
 * - 输入校验（必填、两次密码一致）
 * - 检查用户名是否重复
 * - 写入数据库并提示结果
 * </p>
 */
public class RegisterActivity extends AppCompatActivity {

    // 数据库帮助类（注册时需要查重和写入）
    private MeowDbHelper dbHelper;
    // 用户名输入框
    private TextInputEditText etUsername;
    // 昵称输入框
    private TextInputEditText etNickname;
    // 密码输入框
    private TextInputEditText etPassword;
    // 再次确认密码输入框
    private TextInputEditText etConfirmPassword;

    /**
     * 初始化注册表单并绑定按钮事件。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 处理沉浸式边距
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 注册需要查重和写库，先把 DB helper 准备好
        dbHelper = new MeowDbHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        MaterialButton btnDoRegister = findViewById(R.id.btnDoRegister);
        MaterialButton btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // 一个走注册流程，一个返回登录页
        btnDoRegister.setOnClickListener(v -> doRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    /**
     * 执行注册逻辑：
     * - 读取输入内容
     * - 判空、确认密码
     * - 用户名查重
     * - 写入数据库并提示结果
     */
    private void doRegister() {
        // 先把输入框内容取出来，用户名/昵称顺便 trim
        // 密码保留原样，避免把用户输入的空格删掉
        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String nickname = etNickname.getText() == null ? "" : etNickname.getText().toString().trim();
        // 密码不做 trim
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String confirm = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

        // 任一失败就 Toast 提示并 return
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "喵～，账号和密码不能为空喔", Toast.LENGTH_SHORT).show();
            return;
        }

        // 两次密码不一致提示
        if (!password.equals(confirm)) {
            Toast.makeText(this, "喵～，两次输入的密码不一致喔", Toast.LENGTH_SHORT).show();
            return;
        }

        // 账号已存在处理
        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "喵～，该账号已存在了喔，请换一个", Toast.LENGTH_SHORT).show();
            return;
        }

        // 同步写入数据库，rowId == -1 表示失败
        long rowId = dbHelper.registerUser(username, password, nickname);
        if (rowId == -1) {
            Toast.makeText(this, "喵～，注册失败啦，请重试喔", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "喵～，注册成功啦，请返回登录喔", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
