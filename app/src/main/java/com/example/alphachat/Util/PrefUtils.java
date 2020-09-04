package com.example.alphachat.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;
    private static String PREF_NAME = "login";
    private static String USER_ID = "user_id";
    private static String USER_FULL_NAME = "user_name";
    private static String USER_IMAGE = "image";
    private static String USER_EMAIL = "email";

    public PrefUtils(Context context){
        this.context = context;
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void SaveDetails(String name, String uid, String email, String image){
        editor.putString(USER_FULL_NAME, name);
        editor.putString(USER_ID, uid);
        editor.putString(USER_EMAIL, email);
        editor.putString(USER_IMAGE, image);
        editor.commit();
    }

    public static String getUserId(){
        return sp.getString(USER_ID, null);
    }

    public static String getUserFullName(){
        return sp.getString(USER_FULL_NAME, null);
    }

    public static String getUserImage(){
        return sp.getString(USER_IMAGE, null);
    }

    public static String getUserEmail(){
        return sp.getString(USER_EMAIL, null);
    }

    public void eraseDetails(){
        editor.clear();
        editor.commit();
    }

}
