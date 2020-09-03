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

    public PrefUtils(Context context){
        this.context = context;
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void SaveDetails(String name, String uid){
        editor.putString(USER_FULL_NAME, name);
        editor.putString(USER_ID, uid);
        editor.commit();
    }

    public static String getUserId(){
        return sp.getString(USER_ID, null);
    }

    public void RemoveDetails(){
        editor.clear();
        editor.commit();
    }

}
