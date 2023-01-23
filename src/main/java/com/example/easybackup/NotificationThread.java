package com.example.easybackup;

import javafx.util.Pair;

public abstract class NotificationThread extends Thread {

    private TaskListener listener;

    public abstract Pair<Boolean, Object> doWork();

    public void setListener(TaskListener taskListener) {
        listener = taskListener;
    }

    public TaskListener getListener() {
        return listener;
    }

    private void notifyListener(boolean executionResult, Object threadObject) {
        listener.threadCompleteExecution(this, executionResult, threadObject);
    }

    @Override
    public void run() {
        Pair<Boolean, Object> result = doWork();
        notifyListener(result.getKey(), result.getValue());
    }
}
