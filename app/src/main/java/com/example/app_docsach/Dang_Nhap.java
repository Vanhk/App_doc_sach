package com.example.app_docsach;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dang_Nhap extends AppCompatActivity {
    Button btndang_nhap, btndang_ky;
    EditText edtTen_dang_nhap, edtmatkhau;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);

        btndang_nhap = findViewById(R.id.btndang_nhap);
        btndang_ky = findViewById(R.id.btndang_ky);
        edtTen_dang_nhap = findViewById(R.id.edtTen_dang_nhap);
        edtmatkhau = findViewById(R.id.edtmatkhau);

        btndang_ky.setOnClickListener(v -> {
            startActivity(new Intent(Dang_Nhap.this, Dang_Ky.class));;
        });
    }
}