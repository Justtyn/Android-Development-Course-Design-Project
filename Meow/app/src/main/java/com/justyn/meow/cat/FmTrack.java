package com.justyn.meow.cat;

public class FmTrack {
    private final String title;
    private final String subtitle;
    private final int resId;   // 对应 R.raw.xxx

    public FmTrack(String title, String subtitle, int resId) {
        this.title = title;
        this.subtitle = subtitle;
        this.resId = resId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getResId() {
        return resId;
    }
}
