package com.zhiliaoapp.musically.musicplayerexample;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    private IMusicPlayerListener iMusicPlayerListener;
    private final IBinder musicServiceBinder = new MusicServiceBinder();
    private final ArrayList<SongInfo> songInfos = new ArrayList<>();
    private final Handler musicUpdateProgressHandler = new Handler();
    private final Handler musicWaitPrepareHandler = new Handler();
    private long currentDuration = 0L;
    private long maxDuration = 0L;
    private SongInfo currentSong;
    MediaPlayer mediaPlayer;
    private final long durationForwardOrBack = 10000L;
    private final long durationUpdateNotificationEachTime = 1000L;
    private final long durationWaitPrepareEachTime = 100L;
    private boolean isPrepared = false;
    private boolean isReleased = false;
    private boolean isAutoNext = true;
    private boolean isNeedToStartAfterPrepare = false;
    private boolean isStartForegroundNotification = false;
    public static String DURATION_SEEK_KEY = "DURATION_SEEK_KEY";

    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if(mediaPlayer != null) {
                currentDuration = mediaPlayer.getCurrentPosition();
                maxDuration = mediaPlayer.getDuration();
                if(iMusicPlayerListener != null) {
                    iMusicPlayerListener.onUpdate(currentDuration, maxDuration, currentSong.name);
                }
                updateNotificationView();
                musicUpdateProgressHandler.postDelayed(this, durationUpdateNotificationEachTime);
                if(!mediaPlayer.isPlaying()) {
                    musicUpdateProgressHandler.removeCallbacks(this);
                    if(iMusicPlayerListener != null) {
                        iMusicPlayerListener.onMusicPause();
                    }
                }
            }
        }
    };

    private final Runnable mWaitPrepareTask = new Runnable() {
        public void run() {
            if(!isPrepared) {
                musicWaitPrepareHandler.postDelayed(this, durationWaitPrepareEachTime);
            } else {
                startMusic();
                musicWaitPrepareHandler.removeCallbacks(this);
            }
            Log.e("Hello","Wait to prepare");
        }
    };


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class MusicServiceBinder extends Binder {

        MusicPlayerService getInstanceService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicPlayerService.this;
        }

        public void startNotificationForeground() {
            MusicPlayerService.this.startNotificationForeground();
        }

        public void back10Second() {
            MusicPlayerService.this.back10Second();
        }

        public void forward10Second() {
            MusicPlayerService.this.forward10Second();
        }

        public void playNextSong() {
            MusicPlayerService.this.playNextSong();
        }

        public void playPreviousSong() {
            MusicPlayerService.this.playPreviousSong();
        }

        public void pause() {
            MusicPlayerService.this.pauseMusic();
        }

        public void stop() {
            MusicPlayerService.this.stopMusic();
        }

        public void startMusic() {
            MusicPlayerService.this.startMusic();
        }

        public boolean isPlaying() {
            if(mediaPlayer != null) {
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        public void initMediaPlayer() {
            MusicPlayerService.this.initMusicPlayer();
        }

        public void prepareMediaPlayer() {
            if (mediaPlayer != null) {
                MusicPlayerService.this.prepareMusicPlayer();
            }
        }

        public void setOnMusicEventListener(IMusicPlayerListener listener) {
            MusicPlayerService.this.iMusicPlayerListener = listener;
        }

        public void removeMusicEventListener() {
            MusicPlayerService.this.iMusicPlayerListener = null;
        }

        public void seek(long duration) {
            if (duration != 0L) {
                MusicPlayerService.this.mediaPlayer.seekTo(Math.toIntExact(duration));
            }
        }
    }

    public MusicPlayerService() {
        Log.e(MusicPlayerService.class.getSimpleName(), "constructor");
        initMusicPlayer();
//        songInfos.add(new SongInfo("Từ chối nhẹ nhàng thôi", "https://data3.chiasenhac.com/downloads/2109/1/2108037/128/Tu%20Choi%20Nhe%20Nhang%20Nhac%20chuong_%20-%20Bich%20Ph.mp3"));
//        songInfos.add(new SongInfo("See tình", "https://data.chiasenhac.com/down2/2226/1/2225812-e3722baa/128/See%20Tinh%20-%20Hoang%20Thuy%20Linh.mp3"));
//        songInfos.add(new SongInfo("Muộn rồi mà sao còn -  Sơn Tùng MTP", "https://data.chiasenhac.com/down2/2169/1/2168156-4608576a/128/Muon%20Roi%20Ma%20Sao%20Con%20-%20Son%20Tung%20M-TP.mp3"));
        songInfos.add(new SongInfo("WFU", "https://data.chiasenhac.com/down2/2276/0/2275150-9f672b16/128/Waiting%20For%20You%20-%20MONO_%20Onionn.mp3"));
        songInfos.add(new SongInfo(
                "Bên Trên Tầng Lầu",
                "https://data.chiasenhac.com/down2/2263/0/2262193-48617f77/128/Ben%20Tren%20Tang%20Lau%20-%20Tang%20Duy%20Tan.mp3"
        ));
        songInfos.add(new SongInfo(
                "Ngã tư không đèn",
                "https://data.chiasenhac.com/down2/2265/0/2264080-971f0649/128/Nga%20Tu%20Khong%20Den%20-%20Trang_%20Khoa%20Vu.mp3"
        ));
        currentSong = songInfos.get(0);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(MusicPlayerService.class.getSimpleName(), "onCreate");
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        WifiManager.WifiLock wifiLock =
                ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        //Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        Log.e(MusicPlayerService.class.getSimpleName(), "onStartCommand");
        ///Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e(MusicPlayerService.class.getSimpleName(), "onBind");
        //Toast.makeText(this, "onBind", Toast.LENGTH_SHORT).show();
        return musicServiceBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        isReleased = false;
        if(isNeedToStartAfterPrepare) {
            startMusic();
            isNeedToStartAfterPrepare = false;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //musicUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
        if(isAutoNext) {
            playNextSong();
        }
    }

    private void playNextSong() {
        if(mediaPlayer != null) {
            musicUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            mediaPlayer.reset();
            isNeedToStartAfterPrepare = true;
            final int currentIndexSong = songInfos.indexOf(currentSong);
            if(currentIndexSong < songInfos.size() - 1) {
                currentSong = songInfos.get(currentIndexSong + 1);
            } else {
                currentSong = songInfos.get(0);
            }
            prepareMusicPlayer();
        }
    }

    private void playPreviousSong() {
        if (mediaPlayer != null) {
            musicUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            mediaPlayer.reset();
            isNeedToStartAfterPrepare = true;
            final int currentIndexSong = songInfos.indexOf(currentSong);
            if(currentIndexSong - 1 >= 0) {
                currentSong = songInfos.get(currentIndexSong - 1);
            } else {
                currentSong = songInfos.get(songInfos.size() - 1);
            }
            prepareMusicPlayer();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
              //  updateNotificationView();
                mediaPlayer.pause();
                //musicUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            }
        }
    }

    private void initMusicPlayer() {
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
            );
           // mediaPlayer.setLooping(true);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
        }
    }

    private void prepareMusicPlayer() {
        try {
            if(!mediaPlayer.isPlaying() && !isReleased) {
                mediaPlayer.setDataSource(currentSong.url);
                mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMusic() {
        try {
            if(!isStartForegroundNotification) {
                startNotificationForeground();
            }
            if(!isPrepared) {
                prepareMusicPlayer();
                musicWaitPrepareHandler.postDelayed(mWaitPrepareTask, durationWaitPrepareEachTime);
            } else {
                mediaPlayer.start();
                musicUpdateProgressHandler.postDelayed(mUpdateTimeTask, durationUpdateNotificationEachTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
        this.stopSelf();
        if(iMusicPlayerListener != null) {
            iMusicPlayerListener.onServiceStopped();
        }
        musicUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
        isReleased = true;
        isNeedToStartAfterPrepare = false;
        isPrepared = false;
    }

    private void updateNotificationView() {
        final MusicApplication musicApplication = (MusicApplication) getApplication();
        musicApplication.createNotification(
            currentSong.name, currentDuration, maxDuration
        );
    }

    private void startNotificationForeground() {
        if(getApplication() instanceof  MusicApplication) {
            final MusicApplication musicApplication = (MusicApplication) getApplication();
            musicApplication.createNotification(
                currentSong.name, 0L, 0L
            );
            new Thread(() -> startForeground(
                    MusicApplication.NOTIFICATION_ID, musicApplication.notification)
            ).start();
            isStartForegroundNotification = true;
            if(iMusicPlayerListener != null) {
                iMusicPlayerListener.onStartNotificationForegroundDone();
            }
        }
    }

    private void back10Second() {
        if(currentDuration < durationForwardOrBack) {
            mediaPlayer.seekTo(0);
        } else {
            mediaPlayer.seekTo((int) (currentDuration - durationForwardOrBack));
        }
    }

    private void forward10Second() {
        if(currentDuration + durationForwardOrBack >= maxDuration) {
            mediaPlayer.seekTo((int) maxDuration);
        } else {
            mediaPlayer.seekTo((int) (currentDuration + durationForwardOrBack));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        Log.e(MusicPlayerService.class.getSimpleName(), "onDestroy");
        stopMusic();
    }

   /*
        public void startMusicLocal() {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.waiting_for_you_offline);
            mediaPlayer.start();
        }
    */
}