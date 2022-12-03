package com.zhiliaoapp.musically.musicplayerexample;

public class MusicActionConstant {
    private static final String BASE_NAME_ACTION
            = "com.zhiliaoapp.musically.musicplayerexample.ACTION.";

    static final String INIT_MUSIC_PLAYER_SERVICE
            = BASE_NAME_ACTION.concat("INIT_MUSIC_PLAYER_SERVICE");
    static final String PREPARE_MEDIA_PLAYER = BASE_NAME_ACTION.concat("PREPARE_MEDIA_PLAYER");
    static final String PLAY_NEXT = BASE_NAME_ACTION.concat("PLAY_NEXT");
    static final String PLAY_PREVIOUS = BASE_NAME_ACTION.concat("PLAY_PREVIOUS");
    static final String PAUSE = BASE_NAME_ACTION.concat("PAUSE");
    static final String PLAY = BASE_NAME_ACTION.concat("PLAY");
    static final String EXIT = BASE_NAME_ACTION.concat("EXIT");
    static final String FORWARD_10_SECOND = BASE_NAME_ACTION.concat("FORWARD_10_SECOND");
    static final String BACK_10_SECOND = BASE_NAME_ACTION.concat("BACK_10_SECOND");
    static final String RESUME = BASE_NAME_ACTION.concat("RESUME");
    static final String SEEK = BASE_NAME_ACTION.concat("SEEK");
}
