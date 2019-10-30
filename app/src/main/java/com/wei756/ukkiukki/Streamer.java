package com.wei756.ukkiukki;

public class Streamer {
    private String mid;
    private String name;
    private int nameColor;
    private int icon;
    private String twitch;
    private String youtube;

    public Streamer(String mid, String name, int nameColor, int icon, String twitch, String youtube) {
        this.mid = mid;
        this.name = name;
        this.nameColor = nameColor;
        this.icon = icon;
        this.twitch = twitch;
        this.youtube = youtube;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTwitch() {
        return twitch;
    }

    public void setTwitch(String twitch) {
        this.twitch = twitch;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }
}
