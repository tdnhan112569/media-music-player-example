package com.zhiliaoapp.musically.musicplayerexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IMusicPlayerListener, SeekBar.OnSeekBarChangeListener {

    ObjectAnimator rotateAnimation;
    Button btBack, btPauseOrPlay, btNext, btStop;
    ImageView ivDisk;
    TextView tvCurrentTimePlaying, tvSongName, tvTotalTimeOfSong;
    SeekBar sbProcess;
    Boolean isPressedPlay = false;
    private long totalDuration = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        ivDisk = findViewById(R.id.ivDisk);
        btBack = findViewById(R.id.btBack);
        btPauseOrPlay = findViewById(R.id.btPauseOrPlay);
        btNext = findViewById(R.id.btNext);
        tvCurrentTimePlaying = findViewById(R.id.tvCurrentTimePlaying);
        tvSongName = findViewById(R.id.tvSongName);
        tvTotalTimeOfSong = findViewById(R.id.tvTotalTimeOfSong);
        sbProcess = findViewById(R.id.sbProcess);
        sbProcess.setOnSeekBarChangeListener(this);
        initAnimation();
//        if (getApplication() instanceof MusicApplication) {
//            final MusicApplication musicApplication =(MusicApplication) getApplication();
//            musicApplication.setMusicPlayerListener(this);
//            if(musicApplication.isBound) {
//                musicApplication.startNotificationForeground();
//            }
//        }
        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        btPauseOrPlay.setOnClickListener(v -> {
            if(getApplication() instanceof  MusicApplication) {
                final MusicApplication musicApplication = (MusicApplication) getApplication();
                if (!musicApplication.isBound) {
                    musicBroadcast.setAction(MusicActionConstant.INIT_MUSIC_PLAYER_SERVICE);
                    sendBroadcast(musicBroadcast,null);
                } else {
                    if(isPressedPlay) {
                        isPressedPlay = false;
                        btPauseOrPlay.setText(R.string.play);
                        musicBroadcast.setAction(MusicActionConstant.PAUSE);
                    } else {
                        isPressedPlay = true;
                        btPauseOrPlay.setText(R.string.pause);
                        resumeDiskAnimation();
                        musicBroadcast.setAction(MusicActionConstant.PLAY);
                    }
                    sendBroadcast(musicBroadcast,null);
                }
            }
        });
        btNext.setOnClickListener(v -> {
            clearDiskAnimation();
            startDiskAnimation();
            musicBroadcast.setAction(MusicActionConstant.PLAY_NEXT);
            sendBroadcast(musicBroadcast);
        });
        btBack.setOnClickListener(v -> {
            clearDiskAnimation();
            startDiskAnimation();
            musicBroadcast.setAction(MusicActionConstant.PLAY_PREVIOUS);
            sendBroadcast(musicBroadcast);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getApplication() instanceof MusicApplication) {
            final MusicApplication musicApplication =(MusicApplication) getApplication();
            musicApplication.setMusicPlayerListener(this);
            if(musicApplication.isBound) {
                musicApplication.startNotificationForeground();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getApplication() instanceof MusicApplication) {
            final MusicApplication musicApplication =(MusicApplication) getApplication();
            musicApplication.removeMusicPlayerListener();
        }
    }

    @Override
    public void onUpdate(long currentDuration, long maxDuration, String songName) {
        //Log.e("Listener", "current duration: "+ currentDuration + ", max duration: " + maxDuration + ", song name: " + songName);
        tvCurrentTimePlaying.setText(Utils.milliSecondsToTimer(currentDuration));
        tvTotalTimeOfSong.setText(Utils.milliSecondsToTimer(maxDuration));
        totalDuration = maxDuration;
        int progress = (int)(Utils.getProgressPercentage(currentDuration, maxDuration));
        tvSongName.setText(songName);
        sbProcess.setProgress(progress);
        if(!isPressedPlay) {
            isPressedPlay = true;
            btPauseOrPlay.setText(R.string.pause);
            startDiskAnimation();
        }
    }

    @Override
    public void onStartNotificationForegroundDone() {
        Intent musicBroadcast = new Intent(this, MusicActionReceiver.class);
        isPressedPlay = true;
        btPauseOrPlay.setText(R.string.pause);
        musicBroadcast.setAction(MusicActionConstant.PLAY);
        sendOrderedBroadcast(musicBroadcast,null);
        startDiskAnimation();
    }

    @Override
    public void onServiceStopped() {
//        clearDiskAnimation();
//        startDiskAnimation();
        pauseDiskAnimation();
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
        sbProcess.setProgress(0);
        tvCurrentTimePlaying.setText("0:00");
    }

    @Override
    public void onMusicPause() {
        pauseDiskAnimation();
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
    }

    private void initAnimation(){
        rotateAnimation = ObjectAnimator.ofFloat(ivDisk, View.ROTATION, 0f, 360f);
        rotateAnimation.setDuration(6000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void startDiskAnimation() {
        rotateAnimation.start();
    }

    private void clearDiskAnimation() {
        ivDisk.clearAnimation();
    }

    private void pauseDiskAnimation() {
        rotateAnimation.pause();
    }

    private void resumeDiskAnimation() {
        rotateAnimation.resume();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
        pauseDiskAnimation();
        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        musicBroadcast.setAction(MusicActionConstant.PAUSE);
        sendBroadcast(musicBroadcast);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int currentProcessing = Utils.progressToTimer(seekBar.getProgress(), Math.toIntExact(totalDuration));
        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        musicBroadcast.putExtra(MusicPlayerService.DURATION_SEEK_KEY, currentProcessing);
        musicBroadcast.setAction(MusicActionConstant.SEEK);
        sendOrderedBroadcast(musicBroadcast, null);
        musicBroadcast.removeExtra(MusicPlayerService.DURATION_SEEK_KEY);
        musicBroadcast.setAction(MusicActionConstant.PLAY);
        sendOrderedBroadcast(musicBroadcast, null);
    }
}