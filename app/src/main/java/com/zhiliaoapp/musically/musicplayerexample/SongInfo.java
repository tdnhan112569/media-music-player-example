package com.zhiliaoapp.musically.musicplayerexample;

public class SongInfo {
    public String name;
    public String url;

    public int rawId;

    public SongInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public SongInfo(String name, String url, int rawId) {
        this.name = name;
        this.url = url;
        this.rawId = rawId;
    }
}
