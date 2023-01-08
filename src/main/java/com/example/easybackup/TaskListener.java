package com.example.easybackup;

/**
 * An interface that can be used for NotificationThread class to inform an object
 * that a thread has finished the execution.
 */
public interface TaskListener {

    /**
     * Notifies the object that the thread has finished the execution.
     *
     * @param thread          The thread which finished the job.
     * @param executionResult boolean that represent if the thread do correctly the job.
     * @param threadObject     The class of the thread has finished.
     */
    void threadCompleteExecution(Runnable thread, boolean executionResult, Object threadObject);

    void setBackupSize(double value);

    void appendErrors(StringBuilder errors);
}
