package com.project.alwayscare.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceService {

    public void saveJWTToSharedPreferences(Context context, String jwt) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt", jwt);
        editor.apply();
    }

    public void saveUserIdToSharedPreferences(Context context, Long userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("userId", userId);
        editor.apply();
    }

    public String getJWTFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("jwt", "");
    }

    public long getUserIdToSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("userId", -1);
    }

    public void updateJWTInSharedPreferences(Context context, String newJWT) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("jwt");
        editor.putString("jwt", newJWT);
        editor.apply();
    }

    public void updateUserIdInSharedPreferences(Context context, long newUserId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.putLong("userId", newUserId);
        editor.apply();
    }
}
