package com.reeman.agv.utils;

import static com.reeman.agv.base.BaseApplication.mApp;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.commons.utils.SpManager;


import java.io.IOException;
import java.util.Locale;


public class VoiceHelper {

    private static MediaPlayer mediaPlayer;

    public static void play(String name) {
        play(name, () -> {
        });
    }

    public static void play(String name, OnCompleteListener listener) {
        String path;
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        if (languageType != -1) {
            path = LocaleUtil.getAssetsPathByLanguage(languageType);
        } else {
            path = Locale.getDefault().getLanguage() + "/";
        }
        AssetFileDescriptor assetFileDescriptor;
        try {
            assetFileDescriptor = mApp.getAssets().openFd(path + name + ".wav");
            playAssetsFile(assetFileDescriptor, listener);
        } catch (Exception e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (listener == null) return;
                listener.onComplete();
            }, 1500);
        }
    }

    public static boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public static void pause() {
        try {
            mediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playFile(String path) {
        playFile(path, null);
    }

    public static void playAssetsFile(AssetFileDescriptor assetFileDescriptor) {
        playAssetsFile(assetFileDescriptor, null);
    }

    public static void playAssetsFile(AssetFileDescriptor assetFileDescriptor, OnCompleteListener listener) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mediaPlayer.setOnCompletionListener(mp -> {
                if (listener == null) return;
                listener.onComplete();
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (listener == null) return;
                listener.onComplete();
            }, 1500);
        }
    }


    public static void playFile(String path, OnCompleteListener listener) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (listener != null) listener.onComplete();
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (listener == null) return;
                listener.onComplete();
            }, 1500);
        }
    }

    public interface OnCompleteListener {
        void onComplete();
    }
}
