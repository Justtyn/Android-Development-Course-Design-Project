package com.justyn.meow.cat;

import androidx.annotation.Nullable;

/**
 * FM 音频数据模型：
 * 支持本地 raw 资源与外部 Uri 两种音源。
 */
public class FmTrack {
    // 数据库主键 id
    private final long id;
    // 标题
    private final String title;
    // 副标题/描述
    private final String subtitle;
    // 本地音频资源 id（0 表示无）
    private final int audioResId;   // 对应 R.raw.xxx（0 表示无）
    @Nullable
    // 外部音频 Uri（可为空）
    private final String audioUri;  // content://...（可为空）
    // 是否为“添加入口”占位数据
    private final boolean isAddEntry;

    /**
     * 默认构造（raw 资源）。
     */
    public FmTrack(String title, String subtitle, int audioResId) {
        this(0, title, subtitle, audioResId, null, false);
    }

    /**
     * 数据库构造（含 id 与可选 Uri）。
     */
    public FmTrack(long id, String title, String subtitle, int audioResId, @Nullable String audioUri) {
        this(id, title, subtitle, audioResId, audioUri, false);
    }

    /**
     * 内部构造：支持占位条目。
     */
    private FmTrack(long id, String title, String subtitle, int audioResId, @Nullable String audioUri, boolean isAddEntry) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.audioResId = audioResId;
        this.audioUri = audioUri;
        this.isAddEntry = isAddEntry;
    }

    /**
     * 生成“添加喵音”入口条目。
     */
    public static FmTrack addEntry() {
        return new FmTrack(-1, "添加喵音", "点我上传音频并添加一条喵音～", 0, null, true);
    }

    /**
     * 获取数据库 id。
     */
    public long getId() {
        return id;
    }

    /**
     * 获取音频标题。
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取副标题/描述。
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * 获取音频资源 id（兼容旧命名）。
     */
    public int getResId() {
        return audioResId;
    }

    /**
     * 获取音频资源 id。
     */
    public int getAudioResId() {
        return audioResId;
    }

    /**
     * 获取音频 Uri（可能为 null）。
     */
    @Nullable
    public String getAudioUri() {
        return audioUri;
    }

    /**
     * 是否为“添加入口”占位条目。
     */
    public boolean isAddEntry() {
        return isAddEntry;
    }
}
