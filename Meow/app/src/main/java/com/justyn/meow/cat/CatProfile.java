package com.justyn.meow.cat;

/**
 * 猫咪档案数据模型：
 * 对应一只猫的基本信息 + 头像资源 id
 */
public class CatProfile {

    private String name;        // 名字：四月
    private String breed;       // 品种：英短 · 蓝白
    private String age;         // 年龄文案：2岁半/4个月/1岁
    private String intro;       // 简介：怕生但粘人…
    private int avatarResId;    // 头像资源：R.drawable.xxx

    public CatProfile(String name, String breed, String age, String intro, int avatarResId) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.intro = intro;
        this.avatarResId = avatarResId;
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
}
