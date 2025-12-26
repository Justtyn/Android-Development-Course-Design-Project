package com.justyn.meow.cat;

import androidx.annotation.Nullable;

/**
 * 猫咪档案数据模型：
 * 对应一只猫的基本信息 + 头像资源或外部 URI。
 */
public class CatProfile {

    // 数据库主键 id
    private final long id;
    // 名字：四月
    private final String name;        // 名字：四月
    // 品种/性格：英短 · 蓝白 / 黏人
    private final String breed;       // 品种/性格：英短 · 蓝白 / 黏人
    // 年龄文案：2岁半/4个月/1岁
    private final String age;         // 年龄文案：2岁半/4个月/1岁
    // 简介：怕生但粘人…
    private final String intro;       // 简介：怕生但粘人…
    // 头像资源 id（0 表示无）
    private final int avatarResId;    // 头像资源：R.drawable.xxx（0 表示无）
    @Nullable
    // 头像图片的 Uri（从系统相册选择时保存）
    private final String avatarUri;   // content://...（可为空）
    // 是否为“添加入口”占位数据
    private final boolean isAddEntry;

    /**
     * 构造本地默认档案（仅使用资源头像）。
     */
    public CatProfile(String name, String breed, String age, String intro, int avatarResId) {
        this(0, name, breed, age, intro, avatarResId, null, false);
    }

    /**
     * 构造数据库读取的档案。
     */
    public CatProfile(long id, String name, String breed, String age, String intro, int avatarResId, @Nullable String avatarUri) {
        this(id, name, breed, age, intro, avatarResId, avatarUri, false);
    }

    /**
     * 内部构造方法，支持“添加入口”占位数据。
     */
    private CatProfile(
            long id,
            String name,
            String breed,
            String age,
            String intro,
            int avatarResId,
            @Nullable String avatarUri,
            boolean isAddEntry
    ) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.intro = intro;
        this.avatarResId = avatarResId;
        this.avatarUri = avatarUri;
        this.isAddEntry = isAddEntry;
    }

    /**
     * 返回“添加猫咪”入口数据（列表底部占位）。
     */
    public static CatProfile addEntry() {
        return new CatProfile(-1, "添加猫咪", "点我新增一只猫咪～", "", "", 0, null, true);
    }

    /**
     * 获取数据库 id。
     */
    public long getId() {
        return id;
    }

    /**
     * 获取猫咪名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 获取品种/性格描述。
     */
    public String getBreed() {
        return breed;
    }

    /**
     * 获取年龄文本。
     */
    public String getAge() {
        return age;
    }

    /**
     * 获取简介文本。
     */
    public String getIntro() {
        return intro;
    }

    /**
     * 获取头像资源 id（0 表示无）。
     */
    public int getAvatarResId() {
        return avatarResId;
    }

    /**
     * 获取头像 Uri（可能为 null）。
     */
    @Nullable
    public String getAvatarUri() {
        return avatarUri;
    }

    /**
     * 是否为“添加入口”占位条目。
     */
    public boolean isAddEntry() {
        return isAddEntry;
    }
}
