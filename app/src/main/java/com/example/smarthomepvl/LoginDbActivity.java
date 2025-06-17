package com.example.smarthomepvl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginDbActivity extends AppCompatActivity {
    private EditText editDBName, editUsername, editPass;
    private Button btnLogin;
    private TextView txtRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_db);

        //-------------------------------------Ánh xạ view--------------------------------------
        editDBName = findViewById(R.id.editDBName);
        editUsername = findViewById(R.id.editUsername);
        editPass = findViewById(R.id.editPass);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        dbHelper = new DatabaseHelper(this);

        //Kiểm tra trạng thái đã đăng nhập
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String dbName = prefs.getString("dbName", "");
            String username = prefs.getString("username", "");
            String password = prefs.getString("password", "");

            dbHelper.checkLoginDB(dbName, username, password, success -> {
                if (success) {
                    Intent intent = new Intent(LoginDbActivity.this, NaviActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Hiển thị thông báo nếu có
        String message = getIntent().getStringExtra("success_message");
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        //Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String dbName = editDBName.getText().toString().trim();
            String user = editUsername.getText().toString().trim();
            String pass = editPass.getText().toString().trim();

            if (dbName.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên database, tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.checkLoginDB(dbName, user, pass, success -> {
                if (success) {
                    //Lưu thông tin
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("dbName", dbName);
                    editor.putString("username", user);
                    editor.putString("password", pass);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    //Chuyển tiếp
                    Intent intent = new Intent(LoginDbActivity.this, NaviActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginDbActivity.this, "Kết nối thất bại! Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //Chuyển sang đăng ký
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginDbActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
