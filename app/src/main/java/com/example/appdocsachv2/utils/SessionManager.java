package com.example.appdocsachv2.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
//Tạo một phiên đăng nhập mới khi người dùng đăng nhập thành công
    public void createLoginSession(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
//Lấy ID của người dùng hiện đang đăng nhập
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
//Kiểm tra xem người dùng có đang đăng nhập hay không
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
//Đăng xuất người dùng bằng cách xóa toàn bộ dữ liệu phiên
    public void logout() {
        editor.clear();
        editor.apply();
    }
}