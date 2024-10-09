package com.reeman.agv.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.SpManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MediaPlayerHelper {

    private static volatile MediaPlayerHelper instance;

    private MediaPlayer mediaPlayer;
    private List<String> playList;

    private Disposable disposable;

    private int currentIndex = 0;

    private boolean isPaused = false;

    private boolean isUseNormalVolume = false;

    public boolean isPaused() {
        return isPaused;
    }

    public static MediaPlayerHelper getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerHelper.class) {
                if (instance == null) {
                    instance = new MediaPlayerHelper();
                }
            }
        }
        return instance;
    }

    public MediaPlayerHelper() {
        mediaPlayer = new MediaPlayer();
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }


    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
        currentIndex = 0;
        isPaused = false;
        instance = null;
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public void updateVolume(boolean useNormalVolume) {
        isUseNormalVolume = useNormalVolume;
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
        Log.w("MediaPlayHelper","useNormalVolume : "+useNormalVolume);
        Observable.timer(useNormalVolume ? 1000 : 10, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int volume = getVolume();
                            Log.w("MediaPlayHelper","updateVolume : "+volume);
                            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void playFileList(List<String> filePathList, boolean loop) {
        playList = filePathList;
        isPaused = false;
        playNextFile(loop, null);
    }

    private void playNextFile(boolean loop, OnCompleteListener listener) {
        if (playList == null || playList.isEmpty()) {
            if (listener != null) listener.onComplete();
            return;
        }

        String filePath = playList.get(currentIndex);
        try {
            mediaPlayer.reset();
            int volume = getVolume();
            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                currentIndex = (currentIndex + 1) % playList.size();
                playNextFile(loop, listener);
            });
        } catch (IOException e) {
            e.printStackTrace();
            playNextFile(loop, listener);
        }
    }

    private int getVolume() {
        int mediaVolume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        if (isUseNormalVolume) {
            return mediaVolume;
        }
        return Math.min(mediaVolume, 3);
    }


    public interface OnCompleteListener {
        void onComplete();
    }

}