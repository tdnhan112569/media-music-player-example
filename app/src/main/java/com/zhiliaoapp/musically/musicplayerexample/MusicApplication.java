package com.zhiliaoapp.musically.musicplayerexample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MusicApplication extends Application implements IMusicPlayerListener{

    public final static String CHANNEL_ID = "ChannelServiceMyAppMediaPlayer";
    public final static int NOTIFICATION_ID = 101;
    RemoteViews notificationCollapseLayout;
    RemoteViews notificationExpandedLayout;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;
    NotificationChannel notificationChannel;
    MediaSession mediaSession;
    private MusicPlayerService.MusicServiceBinder musicServiceBinder;
    public IConnectiveMusicService iConnectiveMusicService;
    public boolean isBound = false;
    private IMusicPlayerListener musicPlayerListener;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicServiceBinder = (MusicPlayerService.MusicServiceBinder) service;
            musicServiceBinder.setOnMusicEventListener(MusicApplication.this);
            startNotificationForeground();
            musicServiceBinder.initMediaPlayer();
            isBound = true;
            if(iConnectiveMusicService != null) {
                iConnectiveMusicService.onServiceConnected(name, service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            if(iConnectiveMusicService != null) {
                iConnectiveMusicService.onServiceDisconnected(name);
            }
        }
    };

    public void prepareMediaPlayer() {
        if(musicServiceBinder != null) {
            musicServiceBinder.prepareMediaPlayer();
        }
    }

    public void startMusic() {
        if(musicServiceBinder != null) {
            musicServiceBinder.startMusic();
        }
    }

    public void pauseMusic() {
        if(musicServiceBinder != null) {
            musicServiceBinder.pause();
        }
    }

    public void playNextSong() {
        if(musicServiceBinder != null) {
            musicServiceBinder.playNextSong();
        }
    }

    public void playPreviousSong() {
        if(musicServiceBinder != null) {
            musicServiceBinder.playPreviousSong();
        }
    }

    public void forward10Second() {
        if(musicServiceBinder != null) {
            musicServiceBinder.forward10Second();
        }
    }

    public void back10Second() {
        if(musicServiceBinder != null) {
            musicServiceBinder.back10Second();
        }
    }

    public void seekTo(long duration) {
        if(musicServiceBinder != null) {
            musicServiceBinder.seek(duration);
        }
    }

    public void setMusicPlayerListener(IMusicPlayerListener listener) {
        this.musicPlayerListener = listener;
    }

    public void removeMusicPlayerListener() {
        this.musicPlayerListener = null;
    }

    @Override
    public void onUpdate(long currentDuration, long maxDuration, String songName) {
        if (musicPlayerListener != null) {
            musicPlayerListener.onUpdate(currentDuration, maxDuration, songName);
        }
    }

    @Override
    public void onStartNotificationForegroundDone() {
        if (musicPlayerListener != null) {
            musicPlayerListener.onStartNotificationForegroundDone();
        }
    }

    @Override
    public void onServiceStopped() {
        if (musicPlayerListener != null) {
            musicPlayerListener.onServiceStopped();
        }
    }

    @Override
    public void onMusicPause() {
        if (musicPlayerListener != null) {
            musicPlayerListener.onMusicPause();
        }
    }

    public void startNotificationForeground() {
        if(musicServiceBinder != null) {
            musicServiceBinder.startNotificationForeground();
        }
    }

    public void startAndBindMusicPlayerService() {
        if(!isBound) {
            Intent intentStartService
                    = new Intent(this, MusicPlayerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intentStartService);
            } else {
                startService(intentStartService);
            }
            bindService(
                    intentStartService,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannelId();
        mediaSession = new MediaSession(getApplicationContext(), "My Music Session");
        notificationCollapseLayout = new RemoteViews(getPackageName(), R.layout.notification_media_normal);
        notificationExpandedLayout = new RemoteViews(getPackageName(), R.layout.notification_media_expanded);
    }

    public void createChannelId() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "My Music Player App", NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if(manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }

     /*
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken()
        );*/

    public void pushNotification() {
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(MusicApplication.NOTIFICATION_ID, notification);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(MusicApplication.NOTIFICATION_ID, notification);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void
    createNotification(String contentText, Long currentDuration, Long maxDuration) {
        notificationCollapseLayout.setTextViewText(R.id.tvSongName, contentText);
        notificationExpandedLayout.setTextViewText(R.id.tvSongName, contentText);

        notificationCollapseLayout.setTextViewText(R.id.tvTime, Utils.milliSecondsToTimer(currentDuration));
        notificationExpandedLayout.setTextViewText(R.id.tvTime, Utils.milliSecondsToTimer(currentDuration));
        notificationCollapseLayout.setProgressBar(
            R.id.progress,
            Math.toIntExact(maxDuration),
            Math.toIntExact(currentDuration),
            true
        );
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ứng dụng nghe nhạc của Nhân nè :v")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_twotone_library_music_24)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final Intent musicEventSenderIntent =
                new Intent(this, MusicActionReceiver.class);

        /*------------------------------CONFIG ACTION------------------------------*/
        /*
         * Play Next
         * */
        musicEventSenderIntent.setAction(MusicActionConstant.PLAY_NEXT);
        PendingIntent actionPlayNextIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionPlayNextIntent = PendingIntent.getBroadcast(
                    this,
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            actionPlayNextIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationCollapseLayout.setOnClickPendingIntent(R.id.ibPlayNext, actionPlayNextIntent);
        notificationExpandedLayout.setOnClickPendingIntent(R.id.ibPlayNext, actionPlayNextIntent);
        /*
        * Play Previous
        * */
        musicEventSenderIntent.setAction(MusicActionConstant.PLAY_PREVIOUS);
        PendingIntent actionPlayPreviousIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionPlayPreviousIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            actionPlayPreviousIntent = PendingIntent.getBroadcast(
                    this,
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationCollapseLayout.setOnClickPendingIntent(R.id.ibPlayBack, actionPlayPreviousIntent);
        notificationExpandedLayout.setOnClickPendingIntent(R.id.ibPlayBack, actionPlayPreviousIntent);
        /*
        * Play or Pause
        * */
        PendingIntent actionPlayPauseIntent = null;
        if(musicServiceBinder != null) {
            if (musicServiceBinder.isPlaying()) {
                musicEventSenderIntent.setAction(MusicActionConstant.PAUSE);
                notificationCollapseLayout.setImageViewResource(R.id.ibPlayOrPause, R.drawable.ic_baseline_pause_24);
                notificationExpandedLayout.setImageViewResource(R.id.ibPlayOrPause, R.drawable.ic_baseline_pause_24);
            } else {
                musicEventSenderIntent.setAction(MusicActionConstant.PLAY);
                notificationCollapseLayout.setImageViewResource(R.id.ibPlayOrPause, R.drawable.ic_round_play_arrow_24);
                notificationExpandedLayout.setImageViewResource(R.id.ibPlayOrPause, R.drawable.ic_round_play_arrow_24);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                actionPlayPauseIntent = PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0, musicEventSenderIntent,
                        PendingIntent.FLAG_IMMUTABLE);
            }
            else
            {
                actionPlayPauseIntent = PendingIntent.getBroadcast(
                        this,
                        0, musicEventSenderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            notificationCollapseLayout.setOnClickPendingIntent(R.id.ibPlayOrPause, actionPlayPauseIntent);
            notificationExpandedLayout.setOnClickPendingIntent(R.id.ibPlayOrPause, actionPlayPauseIntent);
        }
        /*
         * Exit
         * */
        musicEventSenderIntent.setAction(MusicActionConstant.EXIT);
        PendingIntent actionExitIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionExitIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            actionExitIntent = PendingIntent.getBroadcast(
                    this,
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationCollapseLayout.setOnClickPendingIntent(R.id.ibExist, actionExitIntent);
        notificationCollapseLayout.setOnClickPendingIntent(R.id.icExist, actionExitIntent);
        notificationExpandedLayout.setOnClickPendingIntent(R.id.ibExist, actionExitIntent);
        /*
         * Back 10 Second
         * */
        musicEventSenderIntent.setAction(MusicActionConstant.BACK_10_SECOND);
        PendingIntent actionBack10SecondIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionBack10SecondIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            actionBack10SecondIntent = PendingIntent.getBroadcast(
                    this,
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationCollapseLayout.setOnClickPendingIntent(R.id.ibPlayBack10s, actionBack10SecondIntent);
        notificationExpandedLayout.setOnClickPendingIntent(R.id.ibPlayBack10s, actionBack10SecondIntent);
        /*
         * Forward 10 Second
         * */
        musicEventSenderIntent.setAction(MusicActionConstant.FORWARD_10_SECOND);
        PendingIntent actionForward10SecondIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionForward10SecondIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            actionForward10SecondIntent = PendingIntent.getBroadcast(
                    this,
                    0, musicEventSenderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationCollapseLayout.setOnClickPendingIntent(R.id.ibPlayForward10s, actionForward10SecondIntent);
        notificationExpandedLayout.setOnClickPendingIntent(R.id.ibPlayForward10s, actionForward10SecondIntent);
        /*------------------------------------------------------------------------------------------*/

        if(notificationCollapseLayout != null) {
            notificationBuilder
              //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
              .setCustomContentView(notificationCollapseLayout);
        }
        if(notificationExpandedLayout != null) {
            notificationBuilder
                    //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomBigContentView(notificationExpandedLayout);
        }
//        if(notificationCollapseLayout != null || notificationExpandedLayout != null) {
//            notificationBuilder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
//        }
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mainActivityPlayer;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mainActivityPlayer = PendingIntent.getActivity(
                    this,
                    0, mainActivityIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            mainActivityPlayer = PendingIntent.getActivity(
                    getApplicationContext(),
                    0, mainActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if(mainActivityPlayer != null) {
            notificationBuilder.setContentIntent(mainActivityPlayer);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        notificationBuilder.addAction(R.drawable.ic_baseline_arrow_back_24, getString(R.string.back), actionPlayPreviousIntent);
        int iconId;
        String title;
        if(musicServiceBinder != null && actionPlayPauseIntent != null) {
            if (musicServiceBinder.isPlaying()) {
                iconId = R.drawable.ic_baseline_pause_24;
                title = getString(R.string.pause);
            } else {
                iconId = R.drawable.ic_round_play_arrow_24;
                title = getString(R.string.play);
            }
            Log.e("config_notification", title);
            notificationBuilder.addAction(iconId, title, actionPlayPauseIntent);

        }
        notificationBuilder.addAction(R.drawable.ic_baseline_arrow_forward_24,  getString(R.string.next), actionPlayNextIntent);
        notificationBuilder.addAction(R.drawable.ic_baseline_exit_to_app_24,  getString(R.string.stop), actionExitIntent);
        notification = notificationBuilder.build();
        pushNotification();
    }

    public void stopMusicPlayerService() {
        if(isBound) {
            musicServiceBinder.stop();
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("onLowMemory", "onLowMemory DAY NE!" );
    }

}
