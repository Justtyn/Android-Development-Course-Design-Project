package com.justyn.meow.checkin;

import android.content.Context;
import android.content.SharedPreferences;

import com.justyn.meow.util.MeowPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class CheckInStore {

    private static final String PREFS_NAME = "meow_checkin_prefs";
    private static final String KEY_LAST_CHECKIN_DATE = "last_checkin_date";
    private static final String KEY_STREAK = "checkin_streak";
    private static final String KEY_DATES = "checkin_dates";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private CheckInStore() {
    }

    public static CheckInResult checkInToday(Context context) {
        CheckInKeys keys = getKeys(context);
        String today = getTodayString();
        String lastDate = keys.prefs.getString(keys.keyLastCheckInDate, null);
        int streak = keys.prefs.getInt(keys.keyStreak, 0);
        Set<String> dates = loadDateSet(keys);

        if (today.equals(lastDate) || dates.contains(today)) {
            return new CheckInResult(true, streak);
        }

        if (lastDate == null) {
            streak = 1;
        } else if (isYesterday(lastDate, today)) {
            streak = streak + 1;
        } else {
            streak = 1;
        }

        dates.add(today);
        keys.prefs.edit()
                .putString(keys.keyLastCheckInDate, today)
                .putInt(keys.keyStreak, streak)
                .putStringSet(keys.keyDates, new HashSet<>(dates))
                .apply();

        return new CheckInResult(false, streak);
    }

    public static int getStreak(Context context) {
        CheckInKeys keys = getKeys(context);
        return keys.prefs.getInt(keys.keyStreak, 0);
    }

    public static Set<String> getCheckInDates(Context context) {
        CheckInKeys keys = getKeys(context);
        return loadDateSet(keys);
    }

    public static boolean isTodayCheckedIn(Context context) {
        return isCheckedInDate(context, getTodayString());
    }

    public static boolean isCheckedInDate(Context context, String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        CheckInKeys keys = getKeys(context);
        Set<String> dates = loadDateSet(keys);
        return dates.contains(date);
    }

    public static String getTodayString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    private static CheckInKeys getKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = MeowPreferences.getUsername(context);
        CheckInKeys keys = new CheckInKeys(
                prefs,
                buildCheckInKey(KEY_LAST_CHECKIN_DATE, username),
                buildCheckInKey(KEY_STREAK, username),
                buildCheckInKey(KEY_DATES, username)
        );
        migrateLegacyCheckInIfNeeded(prefs, username, keys);
        return keys;
    }

    private static String buildCheckInKey(String baseKey, String username) {
        if (username == null || username.trim().isEmpty()) {
            return baseKey;
        }
        return baseKey + "_" + username;
    }

    private static void migrateLegacyCheckInIfNeeded(SharedPreferences prefs, String username, CheckInKeys keys) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        if (prefs.contains(keys.keyStreak) || prefs.contains(keys.keyLastCheckInDate) || prefs.contains(keys.keyDates)) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.contains(KEY_STREAK)) {
            editor.putInt(keys.keyStreak, prefs.getInt(KEY_STREAK, 0));
        }
        String legacyLastDate = prefs.getString(KEY_LAST_CHECKIN_DATE, null);
        if (legacyLastDate != null) {
            editor.putString(keys.keyLastCheckInDate, legacyLastDate);
            Set<String> dates = new HashSet<>();
            dates.add(legacyLastDate);
            editor.putStringSet(keys.keyDates, dates);
        }
        editor.apply();
    }

    private static Set<String> loadDateSet(CheckInKeys keys) {
        Set<String> stored = keys.prefs.getStringSet(keys.keyDates, null);
        if (stored != null) {
            return new HashSet<>(stored);
        }
        Set<String> fallback = new HashSet<>();
        String lastDate = keys.prefs.getString(keys.keyLastCheckInDate, null);
        if (lastDate != null) {
            fallback.add(lastDate);
            keys.prefs.edit().putStringSet(keys.keyDates, new HashSet<>(fallback)).apply();
        }
        return fallback;
    }

    private static boolean isYesterday(String lastDateStr, String todayStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        try {
            Date last = sdf.parse(lastDateStr);
            Date today = sdf.parse(todayStr);
            if (last == null || today == null) {
                return false;
            }
            long diffMs = today.getTime() - last.getTime();
            long days = diffMs / (1000 * 60 * 60 * 24);
            return days == 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class CheckInResult {
        private final boolean alreadyChecked;
        private final int streak;

        public CheckInResult(boolean alreadyChecked, int streak) {
            this.alreadyChecked = alreadyChecked;
            this.streak = streak;
        }

        public boolean isAlreadyChecked() {
            return alreadyChecked;
        }

        public int getStreak() {
            return streak;
        }
    }

    private static class CheckInKeys {
        private final SharedPreferences prefs;
        private final String keyLastCheckInDate;
        private final String keyStreak;
        private final String keyDates;

        private CheckInKeys(SharedPreferences prefs, String keyLastCheckInDate, String keyStreak, String keyDates) {
            this.prefs = prefs;
            this.keyLastCheckInDate = keyLastCheckInDate;
            this.keyStreak = keyStreak;
            this.keyDates = keyDates;
        }
    }
}
