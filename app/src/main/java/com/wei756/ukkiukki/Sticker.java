package com.wei756.ukkiukki;

import com.wei756.ukkiukki.Network.Web;

public class Sticker extends Item {
    private int errorcode = Web.RETURNCODE_SUCCESS;

    private String packCode;
    private String stickerCode;
    private String originalUrl;
    private boolean selected;
    private int stickerCount;
    private int ind;
    private int imageWidth, imageHeight;


    public Sticker() { }

    public int getErrorcode() {
        return errorcode;
    }

    public Sticker setErrorcode(int errorcode) {
        this.errorcode = errorcode;
        return this;
    }

    public String getPackCode() {
        return packCode;
    }

    public Sticker setPackCode(String packCode) {
        this.packCode = packCode;
        return this;
    }

    public String getStickerCode() {
        return stickerCode;
    }

    public Sticker setStickerCode(String stickerCode) {
        this.stickerCode = stickerCode;
        return this;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Sticker setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public Sticker setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public int getStickerCount() {
        return stickerCount;
    }

    public Sticker setStickerCount(int stickerCount) {
        this.stickerCount = stickerCount;
        return this;
    }

    public int getInd() {
        return ind;
    }

    public Sticker setInd(int ind) {
        this.ind = ind;
        return this;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public Sticker setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
        return this;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public Sticker setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        return this;
    }
}
