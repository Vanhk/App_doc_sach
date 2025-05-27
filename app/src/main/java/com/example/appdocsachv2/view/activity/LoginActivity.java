package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
//    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin, btnregister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        etUsername = findViewById(R.id.edtTen_dang_nhap);
        etPassword = findViewById(R.id.edtmatkhau);
        btnLogin = findViewById(R.id.btndang_nhap);
        btnregister = findViewById(R.id.btndang_ky);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if ("admin".equals(username) && "admin".equals(password)) {
                    // Lưu userId vào SessionManager (giả định userId = 1 cho admin)
                    sessionManager.createLoginSession(1);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Tên người dùng hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Tên người dùng và mật khẩu là admin!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}