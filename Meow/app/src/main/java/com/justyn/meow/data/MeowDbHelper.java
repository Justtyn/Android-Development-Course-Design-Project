package com.justyn.meow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.justyn.meow.cat.CatProfile;
import com.justyn.meow.cat.FmTrack;

import java.util.ArrayList;
import java.util.List;

public class MeowDbHelper extends SQLiteOpenHelper {

    // 数据库名和版本号
    private static final String DB_NAME = "meow.db";
    private static final int DB_VERSION = 2;

    // 表名
    public static final String TABLE_USER = "user";
    public static final String TABLE_FM_TRACK = "fm_track";
    public static final String TABLE_CAT_PROFILE = "cat_profile";

    // user 表字段名
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_NICKNAME = "nickname";
    public static final String COL_USER_CREATED_AT = "created_at";

    // fm_track 表字段名
    public static final String COL_FM_ID = "id";
    public static final String COL_FM_TITLE = "title";
    public static final String COL_FM_SUBTITLE = "subtitle";
    public static final String COL_FM_AUDIO_RES_ID = "audio_res_id";
    public static final String COL_FM_AUDIO_URI = "audio_uri";
    public static final String COL_FM_CREATED_AT = "created_at";

    // cat_profile 表字段名
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_TITLE = "title";
    public static final String COL_CAT_AGE = "age";
    public static final String COL_CAT_PERSONALITY = "personality";
    public static final String COL_CAT_DESCRIPTION = "description";
    public static final String COL_CAT_AVATAR_RES_ID = "avatar_res_id";
    public static final String COL_CAT_AVATAR_URI = "avatar_uri";
    public static final String COL_CAT_CREATED_AT = "created_at";

    // 创建 user 表的 SQL
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_USERNAME + " TEXT NOT NULL UNIQUE," +
                    COL_USER_PASSWORD + " TEXT NOT NULL," +
                    COL_USER_NICKNAME + " TEXT," +
                    COL_USER_CREATED_AT + " INTEGER" +
                    ");";

    private static final String SQL_CREATE_FM_TRACK =
            "CREATE TABLE " + TABLE_FM_TRACK + " (" +
                    COL_FM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_FM_TITLE + " TEXT NOT NULL," +
                    COL_FM_SUBTITLE + " TEXT," +
                    COL_FM_AUDIO_RES_ID + " INTEGER," +
                    COL_FM_AUDIO_URI + " TEXT," +
                    COL_FM_CREATED_AT + " INTEGER" +
                    ");";

    private static final String SQL_CREATE_CAT_PROFILE =
            "CREATE TABLE " + TABLE_CAT_PROFILE + " (" +
                    COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_CAT_TITLE + " TEXT NOT NULL," +
                    COL_CAT_AGE + " TEXT," +
                    COL_CAT_PERSONALITY + " TEXT," +
                    COL_CAT_DESCRIPTION + " TEXT," +
                    COL_CAT_AVATAR_RES_ID + " INTEGER," +
                    COL_CAT_AVATAR_URI + " TEXT," +
                    COL_CAT_CREATED_AT + " INTEGER" +
                    ");";

    public MeowDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_FM_TRACK);
        db.execSQL(SQL_CREATE_CAT_PROFILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_FM_TRACK);
            db.execSQL(SQL_CREATE_CAT_PROFILE);
        }
    }

    // user 表方法

    /**
     * 注册新用户
     *
     * @return 插入行的 rowId，如果为 -1 表示失败（比如用户名重复）
     */
    public long registerUser(String username, String password, String nickname) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_USER_USERNAME, username);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_NICKNAME, nickname);
        values.put(COL_USER_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_USER, null, values);
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COL_USER_ID},
                COL_USER_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * 校验登录（用户名 + 密码）
     */
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COL_USER_ID},
                COL_USER_USERNAME + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null
        );

        boolean ok = cursor.getCount() > 0;
        cursor.close();
        return ok;
    }

    /**
     * 根据用户名查昵称（登录成功后用来在首页展示“欢迎 xxx”）
     */
    public String getNicknameByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String nickname = null;

        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COL_USER_NICKNAME},
                COL_USER_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            nickname = cursor.getString(0);
        }
        cursor.close();
        return nickname;
    }

    // fm_track 表方法

    public long insertFmTrack(String title, String subtitle, @Nullable Integer audioResId, @Nullable String audioUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_FM_TITLE, title);
        values.put(COL_FM_SUBTITLE, subtitle);
        if (audioResId != null) {
            values.put(COL_FM_AUDIO_RES_ID, audioResId);
        }
        if (audioUri != null) {
            values.put(COL_FM_AUDIO_URI, audioUri);
        }
        values.put(COL_FM_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_FM_TRACK, null, values);
    }

    public boolean updateFmTrack(long id, String title, String subtitle) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_FM_TITLE, title);
        values.put(COL_FM_SUBTITLE, subtitle);

        int rows = db.update(TABLE_FM_TRACK, values, COL_FM_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteFmTrack(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FM_TRACK, COL_FM_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<FmTrack> queryFmTracks(@Nullable String titleQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<FmTrack> result = new ArrayList<>();

        String selection = null;
        String[] selectionArgs = null;
        if (titleQuery != null && !titleQuery.trim().isEmpty()) {
            selection = COL_FM_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + titleQuery.trim() + "%"};
        }

        Cursor cursor = db.query(
                TABLE_FM_TRACK,
                new String[]{
                        COL_FM_ID,
                        COL_FM_TITLE,
                        COL_FM_SUBTITLE,
                        COL_FM_AUDIO_RES_ID,
                        COL_FM_AUDIO_URI
                },
                selection,
                selectionArgs,
                null,
                null,
                COL_FM_ID + " ASC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FM_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_FM_TITLE));
            String subtitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_FM_SUBTITLE));

            int audioResId = 0;
            int audioResIdIndex = cursor.getColumnIndex(COL_FM_AUDIO_RES_ID);
            if (audioResIdIndex >= 0 && !cursor.isNull(audioResIdIndex)) {
                audioResId = cursor.getInt(audioResIdIndex);
            }

            String audioUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_FM_AUDIO_URI));

            result.add(new FmTrack(id, title, subtitle, audioResId, audioUri));
        }
        cursor.close();
        return result;
    }

    public boolean hasAnyFmTracks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FM_TRACK, new String[]{COL_FM_ID}, null, null, null, null, null, "1");
        boolean hasAny = cursor.moveToFirst();
        cursor.close();
        return hasAny;
    }

    // cat_profile 表方法

    public long insertCatProfile(
            String title,
            String age,
            String personality,
            String description,
            @Nullable Integer avatarResId,
            @Nullable String avatarUri
    ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_CAT_TITLE, title);
        values.put(COL_CAT_AGE, age);
        values.put(COL_CAT_PERSONALITY, personality);
        values.put(COL_CAT_DESCRIPTION, description);
        if (avatarResId != null) {
            values.put(COL_CAT_AVATAR_RES_ID, avatarResId);
        }
        if (avatarUri != null) {
            values.put(COL_CAT_AVATAR_URI, avatarUri);
        }
        values.put(COL_CAT_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_CAT_PROFILE, null, values);
    }

    public boolean updateCatProfile(
            long id,
            String title,
            String age,
            String personality,
            String description,
            @Nullable Integer avatarResId,
            @Nullable String avatarUri
    ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_CAT_TITLE, title);
        values.put(COL_CAT_AGE, age);
        values.put(COL_CAT_PERSONALITY, personality);
        values.put(COL_CAT_DESCRIPTION, description);
        if (avatarResId != null) {
            values.put(COL_CAT_AVATAR_RES_ID, avatarResId);
        } else {
            values.putNull(COL_CAT_AVATAR_RES_ID);
        }
        if (avatarUri != null) {
            values.put(COL_CAT_AVATAR_URI, avatarUri);
        } else {
            values.putNull(COL_CAT_AVATAR_URI);
        }

        int rows = db.update(TABLE_CAT_PROFILE, values, COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteCatProfile(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CAT_PROFILE, COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<CatProfile> queryCatProfiles(@Nullable String titleQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<CatProfile> result = new ArrayList<>();

        String selection = null;
        String[] selectionArgs = null;
        if (titleQuery != null && !titleQuery.trim().isEmpty()) {
            selection = COL_CAT_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + titleQuery.trim() + "%"};
        }

        Cursor cursor = db.query(
                TABLE_CAT_PROFILE,
                new String[]{
                        COL_CAT_ID,
                        COL_CAT_TITLE,
                        COL_CAT_AGE,
                        COL_CAT_PERSONALITY,
                        COL_CAT_DESCRIPTION,
                        COL_CAT_AVATAR_RES_ID,
                        COL_CAT_AVATAR_URI
                },
                selection,
                selectionArgs,
                null,
                null,
                COL_CAT_ID + " ASC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CAT_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_TITLE));
            String age = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_AGE));
            String personality = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_PERSONALITY));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_DESCRIPTION));

            int avatarResId = 0;
            int avatarResIdIndex = cursor.getColumnIndex(COL_CAT_AVATAR_RES_ID);
            if (avatarResIdIndex >= 0 && !cursor.isNull(avatarResIdIndex)) {
                avatarResId = cursor.getInt(avatarResIdIndex);
            }
            String avatarUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_AVATAR_URI));

            result.add(new CatProfile(id, title, personality, age, description, avatarResId, avatarUri));
        }
        cursor.close();
        return result;
    }

    public boolean hasAnyCatProfiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CAT_PROFILE, new String[]{COL_CAT_ID}, null, null, null, null, null, "1");
        boolean hasAny = cursor.moveToFirst();
        cursor.close();
        return hasAny;
    }
}
