package com.example.smarthomepvl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.videogo.constant.Constant;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.bean.EZAccessToken;
import com.videogo.openapi.bean.EZAreaInfo;

import java.util.List;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // Thông tin camera
//    private final String deviceSerial = "F69721360"; // Thay bằng mã thiết bị của bạn
//    private final int cameraNo = 1; // Số camera
//    private final String verifyCode = "Thuan2012"; // Mã xác minh 6 ký tự ở đáy camera

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Ánh xạ view

        // Đăng nhập EZVIZ bằng openLoginPage
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<EZAreaInfo> areaList = EZGlobalSDK.getInstance().getAreaList();
                if (areaList != null && !areaList.isEmpty()) {
                    int areaId = areaList.get(0).getId();

                    runOnUiThread(() -> {
                        // Gọi login từ context là LoginActivity (không phải application)
                        EZGlobalSDK.getInstance().openLoginPage(areaId);
                    });
                }
            } catch (BaseException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi lấy khu vực: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });


        // Đăng ký receiver nhận khi đăng nhập thành công
        IntentFilter filter = new IntentFilter(Constant.OAUTH_SUCCESS_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(oauthSuccessReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(oauthSuccessReceiver, filter);
        }
    }


    private final BroadcastReceiver oauthSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Log.d("LoginActivity", "OAUTH_SUCCESS_ACTION - Lấy AccessToken và tạo player");

            // Lấy access token tự động
            EZAccessToken tokenObj = EZGlobalSDK.getInstance().getEZAccessToken();
            if (tokenObj != null && tokenObj.getAccessToken() != null) {
                String accessToken = tokenObj.getAccessToken();
                Log.d("LoginActivity", "AccessToken: " + accessToken);
                EZGlobalSDK.getInstance().setAccessToken(accessToken); // Nếu SDK yêu cầu
//
//                SharedPreferences preferences = getSharedPreferences("ezviz", MODE_PRIVATE);
//                preferences.edit().putString("access_token", accessToken).apply();

                //Chuyển form
                Intent i = new Intent(LoginActivity.this, LoginDbActivity.class);
                startActivity(i);

                finish(); // Đóng login
            } else {
                Log.d("LoginActivity", "AccessToken object is null");
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(oauthSuccessReceiver);
    }

}
