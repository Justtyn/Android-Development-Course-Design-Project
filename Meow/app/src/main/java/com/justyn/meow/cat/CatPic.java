package com.justyn.meow.cat;

/**
 * 猫图数据模型：
 * 用于图片墙列表，包含图片资源 id 和展示名称。
 */
public class CatPic {

    // 图片资源 id（对应 R.drawable.*）
    private final int imageResId;
    // 图片标题/描述
    private final String name;

    /**
     * 构造一条猫图数据。
     *
     * @param imageResId 图片资源 id
     * @param name       展示名称
     */
    public CatPic(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    /**
     * 获取图片资源 id。
     */
    public int getImageResId() {
        return imageResId;
    }

    /**
     * 获取图片名称。
     */
    public String getName() {
        return name;
    }
}
