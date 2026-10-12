package sanju.newproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "online_ai_prefs";
    private static final String KEY_ONBOARDING_COMPLETED = "is_onboarding_completed";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_THEME = "is_dark_theme";

    private static final String KEY_LANGUAGE = "selected_language";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed, String email, String phone) {
        prefs.edit()
                .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_THEME, true);
    }

    public void setDarkTheme(boolean isDark) {
        prefs.edit().putBoolean(KEY_THEME, isDark).apply();
    }



    public String getSelectedLanguage() {
        return prefs.getString(KEY_LANGUAGE, "auto");
    }

    public void setSelectedLanguage(String langCode) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }
}