package com.example.thyrocare;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class AppStorage {
    private static final String PREFS_NAME = "thyrocare_prefs";
    private static final String LEGACY_PREFS_NAME = "thyrogal_prefs";
    private static final String KEY_TOTAL_POINTS = "TOTAL_POINTS";
    private static final String KEY_CURRENT_YEAR = "CURRENT_SAVED_YEAR";
    private static final String KEY_DISPLAY_NAME = "DISPLAY_NAME";
    private static final String KEY_CYCLE_LOGS = "CYCLE_LOGS";
    private static final String KEY_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final String KEY_ACCOUNT_EMAIL = "ACCOUNT_EMAIL";
    private static final String KEY_ACCOUNT_PASSWORD = "ACCOUNT_PASSWORD";

    private AppStorage() {
    }

    public static SharedPreferences getPreferences(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        migrateLegacyPreferences(appContext, preferences);
        return preferences;
    }

    private static void migrateLegacyPreferences(Context context, SharedPreferences newPreferences) {
        SharedPreferences legacyPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE);
        if (legacyPreferences.getAll().isEmpty() || !newPreferences.getAll().isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = newPreferences.edit();
        for (String key : legacyPreferences.getAll().keySet()) {
            Object value = legacyPreferences.getAll().get(key);
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            }
        }
        editor.apply();
    }

    public static void saveDisplayName(Context context, String displayName) {
        getPreferences(context).edit().putString(KEY_DISPLAY_NAME, displayName).apply();
    }

    public static String getDisplayName(Context context) {
        return getPreferences(context).getString(KEY_DISPLAY_NAME, "friend");
    }

    public static void saveAccount(Context context, String name, String email, String password) {
        getPreferences(context).edit()
                .putString(KEY_ACCOUNT_NAME, name)
                .putString(KEY_ACCOUNT_EMAIL, email)
                .putString(KEY_ACCOUNT_PASSWORD, password)
                .putString(KEY_DISPLAY_NAME, name)
                .apply();
    }

    public static boolean hasAccount(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return !preferences.getString(KEY_ACCOUNT_EMAIL, "").isEmpty()
                && !preferences.getString(KEY_ACCOUNT_PASSWORD, "").isEmpty();
    }

    public static boolean validateLogin(Context context, String emailOrUsername, String password) {
        SharedPreferences preferences = getPreferences(context);
        String savedEmail = preferences.getString(KEY_ACCOUNT_EMAIL, "");
        String savedName = preferences.getString(KEY_ACCOUNT_NAME, "");
        String savedPassword = preferences.getString(KEY_ACCOUNT_PASSWORD, "");

        boolean matchesIdentity = emailOrUsername.equalsIgnoreCase(savedEmail)
                || emailOrUsername.equalsIgnoreCase(savedName);
        return matchesIdentity && password.equals(savedPassword);
    }

    public static String getAccountName(Context context) {
        return getPreferences(context).getString(KEY_ACCOUNT_NAME, getDisplayName(context));
    }

    public static int getTotalPoints(Context context) {
        return getPreferences(context).getInt(KEY_TOTAL_POINTS, 0);
    }

    public static void setTotalPoints(Context context, int totalPoints) {
        getPreferences(context).edit().putInt(KEY_TOTAL_POINTS, totalPoints).apply();
    }

    public static void resetYearIfNeeded(Context context) {
        SharedPreferences preferences = getPreferences(context);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int savedYear = preferences.getInt(KEY_CURRENT_YEAR, currentYear);

        if (currentYear != savedYear) {
            preferences.edit()
                    .putInt(KEY_CURRENT_YEAR, currentYear)
                    .putInt("YEAR_MEDS_COUNT", 0)
                    .putInt("YEAR_GYM_COUNT", 0)
                    .putInt("YEAR_FOOD_COUNT", 0)
                    .apply();
        }
    }

    public static int incrementCounter(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        int newValue = preferences.getInt(key, 0) + 1;
        preferences.edit().putInt(key, newValue).apply();
        return newValue;
    }

    public static int getCounter(Context context, String key) {
        return getPreferences(context).getInt(key, 0);
    }

    public static String getTaskDate(Context context, String key) {
        return getPreferences(context).getString(key, "");
    }

    public static void setTaskDate(Context context, String key, String date) {
        getPreferences(context).edit().putString(key, date).apply();
    }

    public static void addCycleLog(Context context, CycleLog cycleLog) {
        List<CycleLog> logs = getCycleLogs(context);
        logs.add(0, cycleLog);
        saveCycleLogs(context, logs);
    }

    public static List<CycleLog> getCycleLogs(Context context) {
        List<CycleLog> logs = new ArrayList<>();
        String rawJson = getPreferences(context).getString(KEY_CYCLE_LOGS, "[]");

        try {
            JSONArray array = new JSONArray(rawJson);
            for (int index = 0; index < array.length(); index++) {
                JSONObject item = array.optJSONObject(index);
                if (item != null) {
                    logs.add(CycleLog.fromJson(item));
                }
            }
        } catch (Exception ignored) {
        }

        return logs;
    }

    private static void saveCycleLogs(Context context, List<CycleLog> logs) {
        JSONArray array = new JSONArray();
        for (CycleLog log : logs) {
            array.put(log.toJson());
        }
        getPreferences(context).edit().putString(KEY_CYCLE_LOGS, array.toString()).apply();
    }
}
