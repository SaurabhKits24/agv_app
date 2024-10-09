package com.reeman.commons.settings;



import java.util.ArrayList;
import java.util.List;

public class ObstacleSetting {

    public boolean enableObstaclePrompt;

    public List<Integer> targetObstaclePrompts;

    public List<String> obstaclePrompts;

    public List<String> obstaclePromptAudioList;

    public ObstacleSetting(boolean enableObstaclePrompt, List<Integer> targetObstaclePrompts, List<String> obstaclePrompts, List<String> obstaclePromptAudioList) {
        this.enableObstaclePrompt = enableObstaclePrompt;
        this.targetObstaclePrompts = targetObstaclePrompts;
        this.obstaclePrompts = obstaclePrompts;
        this.obstaclePromptAudioList = obstaclePromptAudioList;
    }

    public static ObstacleSetting getDefault() {
        return new ObstacleSetting(true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        return "ObstacleSetting{" +
                "enableObstaclePrompt=" + enableObstaclePrompt +
                ", targetObstaclePrompts=" + targetObstaclePrompts +
                ", obstaclePrompts=" + obstaclePrompts +
                ", obstaclePromptAudioList=" + obstaclePromptAudioList +
                '}';
    }
}
