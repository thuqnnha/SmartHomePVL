package com.example.smarthomepvl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;



public class NaviActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_navi);

        // Lấy username từ Intent
        String username = getIntent().getStringExtra("username");
        if (username != null && !username.isEmpty()) {
            Toast.makeText(this, "Hello " + username, Toast.LENGTH_LONG).show();
        }

        //ánh xạ
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        // Gắn toolbar làm action bar
        setSupportActionBar(toolbar);

        // Tạo nút 3 gạch để mở NavigationView
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Mặc định load Fragment "Nhà"
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    //.addToBackStack(null)
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Xử lý chọn menu
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        });
    }
    @Override
    public void onBackPressed() {
        // Đóng menu nếu đang mở
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Tùy chỉnh màu icon và text cho các item con
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        SubMenu subMenu = logoutItem.getSubMenu();

        if (subMenu != null) {
            for (int i = 0; i < subMenu.size(); i++) {
                MenuItem subItem = subMenu.getItem(i);

                // Đổi màu icon
                Drawable icon = subItem.getIcon();
                if (icon != null) {
                    icon.mutate();
                    icon.setTint(Color.BLACK); // ← đổi sang màu bạn muốn
                    subItem.setIcon(icon);
                }

                // Đổi màu chữ (áp dụng với Spannable)
                SpannableString s = new SpannableString(subItem.getTitle());
                s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0); // ← màu chữ
                subItem.setTitle(s);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: xu li dang xuat
        int id = item.getItemId();

        if (id == R.id.action_logout_normal) {

            return true;
        } else if (id == R.id.action_logout_db) {
            logOutAndDeleteAccount();
            return true;
        } else if (id == R.id.action_logout_api) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logOutAndDeleteAccount()
    {
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(NaviActivity.this, LoginDbActivity.class);
        startActivity(intent);
        finish();
    }
}
