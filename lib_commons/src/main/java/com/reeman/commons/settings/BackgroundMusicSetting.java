package com.reeman.commons.settings;

import java.util.ArrayList;
import java.util.List;

public class BackgroundMusicSetting {

    public boolean enableBackgroundMusic;

    public List<String> backgroundMusicFileNames;

    public List<String> backgroundMusicPaths;

    public BackgroundMusicSetting(boolean enableBackgroundMusic, List<String> backgroundMusicFileNames, List<String> backgroundMusicPaths){
        this.enableBackgroundMusic = enableBackgroundMusic;
        this.backgroundMusicFileNames = backgroundMusicFileNames;
        this.backgroundMusicPaths = backgroundMusicPaths;
    }

    public static BackgroundMusicSetting getDefault() {
        return new BackgroundMusicSetting(
                false,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
