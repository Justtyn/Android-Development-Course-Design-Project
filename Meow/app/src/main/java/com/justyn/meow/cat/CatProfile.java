package com.justyn.meow.cat;

import androidx.annotation.Nullable;

/**
 * 猫咪档案数据模型：
 * 对应一只猫的基本信息 + 头像资源 id
 */
public class CatProfile {

    private final long id;
    private final String name;        // 名字：四月
    private final String breed;       // 品种/性格：英短 · 蓝白 / 黏人
    private final String age;         // 年龄文案：2岁半/4个月/1岁
    private final String intro;       // 简介：怕生但粘人…
    private final int avatarResId;    // 头像资源：R.drawable.xxx（0 表示无）
    @Nullable
    private final String avatarUri;   // content://...（可为空）
    private final boolean isAddEntry;

    public CatProfile(String name, String breed, String age, String intro, int avatarResId) {
        this(0, name, breed, age, intro, avatarResId, null, false);
    }

    public CatProfile(long id, String name, String breed, String age, String intro, int avatarResId, @Nullable String avatarUri) {
        this(id, name, breed, age, intro, avatarResId, avatarUri, false);
    }

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

    public static CatProfile addEntry() {
        return new CatProfile(-1, "添加猫咪", "点我新增一只猫咪～", "", "", 0, null, true);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String getIntro() {
        return intro;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    @Nullable
    public String getAvatarUri() {
        return avatarUri;
    }

    public boolean isAddEntry() {
        return isAddEntry;
    }
}
