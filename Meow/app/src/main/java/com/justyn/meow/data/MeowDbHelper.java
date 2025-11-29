package com.justyn.meow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MeowDbHelper extends SQLiteOpenHelper {

    // 数据库名和版本号
    private static final String DB_NAME = "meow.db";
    private static final int DB_VERSION = 1;

    // 表名
    public static final String TABLE_USER = "user";

    // user 表字段名
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_NICKNAME = "nickname";
    public static final String COL_USER_CREATED_AT = "created_at";

    // 创建 user 表的 SQL
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_USERNAME + " TEXT NOT NULL UNIQUE," +
                    COL_USER_PASSWORD + " TEXT NOT NULL," +
                    COL_USER_NICKNAME + " TEXT," +
                    COL_USER_CREATED_AT + " INTEGER" +
                    ");";

    public MeowDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级策略：先删后建
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
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
}
