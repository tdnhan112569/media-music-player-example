package com.zhiliaoapp.musically.musicplayerexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(context.getApplicationContext() instanceof  MusicApplication) {
            final MusicApplication musicApplication =
                    (MusicApplication) context.getApplicationContext();

            if(action.equals(MusicActionConstant.PREPARE_MEDIA_PLAYER)) {
                musicApplication.prepareMediaPlayer();
            }

            if (action.equals(MusicActionConstant.PLAY)) {
                musicApplication.startMusic();
            }

            if (action.equals(MusicActionConstant.PAUSE)) {
                musicApplication.pauseMusic();
            }

            if (action.equals(MusicActionConstant.PLAY_NEXT)) {
                musicApplication.playNextSong();
            }

            if (action.equals(MusicActionConstant.PLAY_PREVIOUS)) {
                musicApplication.playPreviousSong();
            }

            if (action.equals(MusicActionConstant.EXIT)) {
                musicApplication.stopMusicPlayerService();
            }

            if (action.equals(MusicActionConstant.INIT_MUSIC_PLAYER_SERVICE)) {
                musicApplication.startAndBindMusicPlayerService();
            }

            if(action.equals(MusicActionConstant.FORWARD_10_SECOND)) {
                musicApplication.forward10Second();
            }

            if(action.equals(MusicActionConstant.BACK_10_SECOND)) {
                musicApplication.back10Second();
            }

            if(action.equals(MusicActionConstant.SEEK)) {
                long duration = intent.getExtras().getInt(MusicPlayerService.DURATION_SEEK_KEY);
                musicApplication.seekTo(duration);
            }
        }
    }

}