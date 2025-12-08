package com.lcv.commands.etcg;

import java.io.Serializable;

public class Quest implements Serializable
{
    private final String id;
    private final String name;
    private final String description;
    private boolean isCompleted;
    private int progress;
    private final int goal;

    public Quest(String id, String name, String description, int goal)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isCompleted = false;
        this.progress = 0;
        this.goal = goal;
    }

    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public String getDescription()
    {
        return description;
    }
    public boolean isCompleted()
    {
        return isCompleted;
    }
    public int getProgress()
    {
        return progress;
    }
    public int getGoal()
    {
        return goal;
    }

    public void complete()
    {
        progress = goal;
        isCompleted = true;
    }
    public void addProgress(int amount)
    {
        progress += amount;
        isCompleted = progress >= goal;
    }
}
