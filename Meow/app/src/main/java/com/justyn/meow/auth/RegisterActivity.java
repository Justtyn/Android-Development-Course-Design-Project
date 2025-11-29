package com.justyn.meow.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.meow.R;
import com.justyn.meow.data.MeowDbHelper;

public class RegisterActivity extends AppCompatActivity {

    private MeowDbHelper dbHelper;
    private TextInputEditText etUsername;
    private TextInputEditText etNickname;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MeowDbHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        MaterialButton btnDoRegister = findViewById(R.id.btnDoRegister);
        MaterialButton btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnDoRegister.setOnClickListener(v -> doRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String nickname = etNickname.getText() == null ? "" : etNickname.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String confirm = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "喵～，账号和密码不能为空喔", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "喵～，两次输入的密码不一致喔", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "喵～，该账号已存在了喔，请换一个", Toast.LENGTH_SHORT).show();
            return;
        }

        long rowId = dbHelper.registerUser(username, password, nickname);
        if (rowId == -1) {
            Toast.makeText(this, "喵～，注册失败啦，请重试喔", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "喵～，注册成功啦，请返回登录喔", Toast.LENGTH_SHORT).show();
            finish();  // 关闭注册页，回到登录页
        }
    }
}