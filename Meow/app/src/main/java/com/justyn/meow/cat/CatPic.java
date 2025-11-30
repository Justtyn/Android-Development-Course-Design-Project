package com.justyn.meow.cat;

public class CatPic {

    private final int imageResId;
    private final String name;

    public CatPic(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getName() {
        return name;
    }
}
