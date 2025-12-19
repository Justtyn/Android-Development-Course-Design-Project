package com.justyn.meow.cat;

import androidx.annotation.Nullable;

public class FmTrack {
    private final long id;
    private final String title;
    private final String subtitle;
    private final int audioResId;   // 对应 R.raw.xxx（0 表示无）
    @Nullable
    private final String audioUri;  // content://...（可为空）
    private final boolean isAddEntry;

    // 默认构造（raw 资源）
    public FmTrack(String title, String subtitle, int audioResId) {
        this(0, title, subtitle, audioResId, null, false);
    }

    // 数据库构造
    public FmTrack(long id, String title, String subtitle, int audioResId, @Nullable String audioUri) {
        this(id, title, subtitle, audioResId, audioUri, false);
    }

    private FmTrack(long id, String title, String subtitle, int audioResId, @Nullable String audioUri, boolean isAddEntry) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.audioResId = audioResId;
        this.audioUri = audioUri;
        this.isAddEntry = isAddEntry;
    }

    public static FmTrack addEntry() {
        return new FmTrack(-1, "添加喵音", "点我上传音频并添加一条喵音～", 0, null, true);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getResId() {
        return audioResId;
    }

    public int getAudioResId() {
        return audioResId;
    }

    @Nullable
    public String getAudioUri() {
        return audioUri;
    }

    public boolean isAddEntry() {
        return isAddEntry;
    }
}
