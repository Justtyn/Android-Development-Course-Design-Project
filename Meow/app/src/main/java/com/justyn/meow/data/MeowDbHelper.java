package com.justyn.meow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.justyn.meow.cat.CatProfile;
import com.justyn.meow.cat.FmTrack;
import com.justyn.meow.util.MeowPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用本地 SQLite 数据库帮助类。
 * <p>
 * 负责数据库的创建、升级，以及对 user / fm_track / cat_profile 三张表的基础 CRUD 操作。
 * </p>
 */
public class MeowDbHelper extends SQLiteOpenHelper {

    // 数据库文件名（保存在应用私有目录）
    private static final String DB_NAME = "meow.db";
    // 数据库版本号（升级时用于触发 onUpgrade）
    private static final int DB_VERSION = 3;

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
    public static final String COL_FM_USERNAME = "user_username";

    // cat_profile 表字段名
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_TITLE = "title";
    public static final String COL_CAT_AGE = "age";
    public static final String COL_CAT_PERSONALITY = "personality";
    public static final String COL_CAT_DESCRIPTION = "description";
    public static final String COL_CAT_AVATAR_RES_ID = "avatar_res_id";
    public static final String COL_CAT_AVATAR_URI = "avatar_uri";
    public static final String COL_CAT_CREATED_AT = "created_at";
    public static final String COL_CAT_USERNAME = "user_username";

    // 创建 user 表的 SQL（含唯一用户名约束）
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_USERNAME + " TEXT NOT NULL UNIQUE," +
                    COL_USER_PASSWORD + " TEXT NOT NULL," +
                    COL_USER_NICKNAME + " TEXT," +
                    COL_USER_CREATED_AT + " INTEGER" +
                    ");";

    // 创建 fm_track 表的 SQL
    private static final String SQL_CREATE_FM_TRACK =
            "CREATE TABLE " + TABLE_FM_TRACK + " (" +
                    COL_FM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_FM_TITLE + " TEXT NOT NULL," +
                    COL_FM_SUBTITLE + " TEXT," +
                    COL_FM_AUDIO_RES_ID + " INTEGER," +
                    COL_FM_AUDIO_URI + " TEXT," +
                    COL_FM_CREATED_AT + " INTEGER," +
                    COL_FM_USERNAME + " TEXT" +
                    ");";

    // 创建 cat_profile 表的 SQL
    private static final String SQL_CREATE_CAT_PROFILE =
            "CREATE TABLE " + TABLE_CAT_PROFILE + " (" +
                    COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_CAT_TITLE + " TEXT NOT NULL," +
                    COL_CAT_AGE + " TEXT," +
                    COL_CAT_PERSONALITY + " TEXT," +
                    COL_CAT_DESCRIPTION + " TEXT," +
                    COL_CAT_AVATAR_RES_ID + " INTEGER," +
                    COL_CAT_AVATAR_URI + " TEXT," +
                    COL_CAT_CREATED_AT + " INTEGER," +
                    COL_CAT_USERNAME + " TEXT" +
                    ");";

    /**
     * 构造数据库帮助类实例。
     *
     * @param context 上下文（用于定位数据库文件）
     */
    public MeowDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context == null ? null : context.getApplicationContext();
    }

    private final Context appContext;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 首次创建数据库时初始化三张表
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_FM_TRACK);
        db.execSQL(SQL_CREATE_CAT_PROFILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 版本升级逻辑：从旧版本逐步补建新表（保持已有数据）
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_FM_TRACK);
            db.execSQL(SQL_CREATE_CAT_PROFILE);
        }
        if (oldVersion < 3) {
            addColumnIfMissing(db, TABLE_FM_TRACK, COL_FM_USERNAME);
            addColumnIfMissing(db, TABLE_CAT_PROFILE, COL_CAT_USERNAME);
            migrateLegacyUser(db);
        }
    }

    // user 表方法

    /**
     * 注册新用户
     *
     * @param username 用户名（唯一）
     * @param password 密码（当前实现为明文存储）
     * @param nickname 昵称（可为空）
     * @return 插入行的 rowId，如果为 -1 表示失败（比如用户名重复）
     */
    public long registerUser(String username, String password, String nickname) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 组织要写入的字段和值
        ContentValues values = new ContentValues();
        values.put(COL_USER_USERNAME, username);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_NICKNAME, nickname);
        // 使用系统时间戳记录创建时间（毫秒）
        values.put(COL_USER_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_USER, null, values);
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 仅查询 id 字段即可判断是否存在
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

        // 同时匹配用户名和密码，匹配到即可视为登录成功
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

        // 只取昵称字段，减少读取开销
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

    /**
     * 新增 FM 音轨数据。
     *
     * @param title       标题（必填）
     * @param subtitle    副标题（可为空）
     * @param audioResId  本地音频资源 id（可为空）
     * @param audioUri    外部音频 URI（可为空）
     * @return 插入行的 rowId，失败返回 -1
     */
    public long insertFmTrack(String username, String title, String subtitle, @Nullable Integer audioResId, @Nullable String audioUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 组装插入字段
        ContentValues values = new ContentValues();
        values.put(COL_FM_USERNAME, safeUsername(username));
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

    /**
     * 更新指定 FM 音轨的标题与副标题。
     *
     * @param id       记录 id
     * @param title    新标题
     * @param subtitle 新副标题
     * @return 是否更新成功（影响行数 > 0）
     */
    public boolean updateFmTrack(long id, String title, String subtitle) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 只更新必要字段
        ContentValues values = new ContentValues();
        values.put(COL_FM_TITLE, title);
        values.put(COL_FM_SUBTITLE, subtitle);

        int rows = db.update(TABLE_FM_TRACK, values, COL_FM_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    /**
     * 删除指定 FM 音轨。
     *
     * @param id 记录 id
     * @return 是否删除成功（影响行数 > 0）
     */
    public boolean deleteFmTrack(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FM_TRACK, COL_FM_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    /**
     * 查询 FM 音轨列表，可按标题模糊搜索。
     *
     * @param titleQuery 标题关键字（为空时返回全部）
     * @return 查询结果列表
     */
    public List<FmTrack> queryFmTracks(String username, @Nullable String titleQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<FmTrack> result = new ArrayList<>();

        String selection = COL_FM_USERNAME + " = ?";
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(safeUsername(username));
        if (titleQuery != null && !titleQuery.trim().isEmpty()) {
            // 构造 LIKE 查询条件
            selection += " AND " + COL_FM_TITLE + " LIKE ?";
            selectionArgsList.add("%" + titleQuery.trim() + "%");
        }
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        // 按 id 升序读取，保证稳定展示顺序
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
                // 只有字段存在且非空时才读取资源 id
                audioResId = cursor.getInt(audioResIdIndex);
            }

            // audioUri 允许为 null，直接读取即可
            String audioUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_FM_AUDIO_URI));

            result.add(new FmTrack(id, title, subtitle, audioResId, audioUri));
        }
        cursor.close();
        return result;
    }

    /**
     * 判断 FM 音轨表中是否已有数据。
     */
    public boolean hasAnyFmTracks(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 只取一条记录即可判断是否存在
        Cursor cursor = db.query(
                TABLE_FM_TRACK,
                new String[]{COL_FM_ID},
                COL_FM_USERNAME + " = ?",
                new String[]{safeUsername(username)},
                null,
                null,
                null,
                "1"
        );
        boolean hasAny = cursor.moveToFirst();
        cursor.close();
        return hasAny;
    }

    // cat_profile 表方法

    /**
     * 新增猫咪档案。
     *
     * @param title        标题（必填）
     * @param age          年龄（可为空）
     * @param personality  性格描述（可为空）
     * @param description  详细说明（可为空）
     * @param avatarResId  本地头像资源 id（可为空）
     * @param avatarUri    头像 URI（可为空）
     * @return 插入行的 rowId，失败返回 -1
     */
    public long insertCatProfile(
            String username,
            String title,
            String age,
            String personality,
            String description,
            @Nullable Integer avatarResId,
            @Nullable String avatarUri
    ) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 组织插入字段
        ContentValues values = new ContentValues();
        values.put(COL_CAT_USERNAME, safeUsername(username));
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

    /**
     * 更新猫咪档案信息。
     *
     * @param id           记录 id
     * @param title        标题
     * @param age          年龄
     * @param personality  性格描述
     * @param description  详细说明
     * @param avatarResId  本地头像资源 id（可为空）
     * @param avatarUri    头像 URI（可为空）
     * @return 是否更新成功（影响行数 > 0）
     */
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

        // 将 null 显式写入数据库，保证字段被清空
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

    /**
     * 删除指定猫咪档案。
     *
     * @param id 记录 id
     * @return 是否删除成功（影响行数 > 0）
     */
    public boolean deleteCatProfile(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CAT_PROFILE, COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    /**
     * 查询猫咪档案列表，可按标题模糊搜索。
     *
     * @param titleQuery 标题关键字（为空时返回全部）
     * @return 查询结果列表
     */
    public List<CatProfile> queryCatProfiles(String username, @Nullable String titleQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<CatProfile> result = new ArrayList<>();

        String selection = COL_CAT_USERNAME + " = ?";
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(safeUsername(username));
        if (titleQuery != null && !titleQuery.trim().isEmpty()) {
            // 构造 LIKE 查询条件
            selection += " AND " + COL_CAT_TITLE + " LIKE ?";
            selectionArgsList.add("%" + titleQuery.trim() + "%");
        }
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        // 按 id 升序读取
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
                // 头像资源 id 为空时保持默认值
                avatarResId = cursor.getInt(avatarResIdIndex);
            }
            String avatarUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_AVATAR_URI));

            result.add(new CatProfile(id, title, personality, age, description, avatarResId, avatarUri));
        }
        cursor.close();
        return result;
    }

    /**
     * 判断猫咪档案表中是否已有数据。
     */
    public boolean hasAnyCatProfiles(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 只取一条记录即可判断是否存在
        Cursor cursor = db.query(
                TABLE_CAT_PROFILE,
                new String[]{COL_CAT_ID},
                COL_CAT_USERNAME + " = ?",
                new String[]{safeUsername(username)},
                null,
                null,
                null,
                "1"
        );
        boolean hasAny = cursor.moveToFirst();
        cursor.close();
        return hasAny;
    }

    public void claimLegacyDataForUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues fmValues = new ContentValues();
        fmValues.put(COL_FM_USERNAME, safeUsername(username));
        db.update(
                TABLE_FM_TRACK,
                fmValues,
                COL_FM_USERNAME + " IS NULL OR " + COL_FM_USERNAME + " = ''",
                null
        );

        ContentValues catValues = new ContentValues();
        catValues.put(COL_CAT_USERNAME, safeUsername(username));
        db.update(
                TABLE_CAT_PROFILE,
                catValues,
                COL_CAT_USERNAME + " IS NULL OR " + COL_CAT_USERNAME + " = ''",
                null
        );
    }

    private static String safeUsername(String username) {
        return username == null ? "" : username;
    }

    private void addColumnIfMissing(SQLiteDatabase db, String tableName, String columnName) {
        if (hasColumn(db, tableName, columnName)) {
            return;
        }
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " TEXT");
    }

    private static boolean hasColumn(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    private void migrateLegacyUser(SQLiteDatabase db) {
        if (appContext == null) {
            return;
        }
        String username = MeowPreferences.getUsername(appContext);
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        ContentValues fmValues = new ContentValues();
        fmValues.put(COL_FM_USERNAME, username);
        db.update(
                TABLE_FM_TRACK,
                fmValues,
                COL_FM_USERNAME + " IS NULL OR " + COL_FM_USERNAME + " = ''",
                null
        );

        ContentValues catValues = new ContentValues();
        catValues.put(COL_CAT_USERNAME, username);
        db.update(
                TABLE_CAT_PROFILE,
                catValues,
                COL_CAT_USERNAME + " IS NULL OR " + COL_CAT_USERNAME + " = ''",
                null
        );
    }
}
