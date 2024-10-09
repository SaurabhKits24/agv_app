package com.reeman.agv.constants;

import com.reeman.commons.state.TaskMode;
import com.reeman.dao.repository.entities.RouteWithPoints;

import java.io.Serializable;

public class TaskResult implements Serializable {

    private final String prompt;

    private final String voice;

    private final RouteWithPoints routeWithPoints;

    private final TaskMode taskMode;

    public String getPrompt() {
        return prompt;
    }

    public String getVoice() {
        return voice;
    }

    public RouteWithPoints getRouteWithPoints() {
        return routeWithPoints;
    }

    public TaskMode getTaskMode() {
        return taskMode;
    }

    public TaskResult(String prompt, String voice, RouteWithPoints routeWithPoints, TaskMode taskMode) {
        this.prompt = prompt;
        this.voice = voice;
        this.routeWithPoints = routeWithPoints;
        this.taskMode = taskMode;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "prompt='" + prompt + '\'' +
                ", voice='" + voice + '\'' +
                ", routeWithPoints=" + routeWithPoints + '\'' +
                ", taskMode=" + taskMode +
                '}';
    }
}
