package com.example.androiddevwifiscan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText edtTenFile;
    private AppCompatButton btnTaoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        edtTenFile = findViewById(R.id.edt_ten_file);
        btnTaoFile = findViewById(R.id.btn_tao_file);

        // Xử lý sự kiện chuyển intent
        btnTaoFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenFile = edtTenFile.getText().toString().trim();
                if (TextUtils.isEmpty(tenFile)) {
                    Toast.makeText(MainActivity.this,"Bạn chưa nhập tên file!",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ScannerAcitvity.class);
                intent.putExtra("file",tenFile);
                Toast.makeText(MainActivity.this,"Nhập tên file thành công!",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

    }
}