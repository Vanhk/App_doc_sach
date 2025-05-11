package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appdocsachv2.R;

public class LoginActivity extends AppCompatActivity {
    Button btndang_nhap, btndang_ky;
    EditText edtTen_dang_nhap, edtmatkhau;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btndang_nhap = findViewById(R.id.btndang_nhap);
        btndang_ky = findViewById(R.id.btndang_ky);
        edtTen_dang_nhap = findViewById(R.id.edtTen_dang_nhap);
        edtmatkhau = findViewById(R.id.edtmatkhau);
        btndang_nhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ten = edtTen_dang_nhap.getText().toString().trim();
                String mk = edtmatkhau.getText().toString().trim();

                if (ten.equals("admin") && mk.equals("123456")) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    // Có thể chuyển sang màn hình khác ở đây
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tên hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btndang_ky.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}