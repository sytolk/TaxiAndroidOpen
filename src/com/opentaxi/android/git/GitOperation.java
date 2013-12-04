package com.opentaxi.android.git;

import android.util.Log;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.opentaxi.android.git.GitOperation.Status.*;
import static java.lang.Thread.currentThread;

public abstract class GitOperation implements CancellationSignaller {

    public enum Status {
        NOT_STARTED,
        RUNNING,
        FINISHED
    }

    private Status status = NOT_STARTED;
    private boolean cancelled = false;
    protected final File gitdir;
    private Thread executionThread;
    private static final String TAG = "GO";

    public GitOperation(File gitdir) {
        this.gitdir = gitdir;
    }

    public abstract String getTickerText();

    public abstract int getOngoingIcon();

    public String executeAndRecordThread() throws Exception {
        executionThread = currentThread();
        status = RUNNING;
        try {
            return execute();
        } finally {
            status = FINISHED;
        }
    }

    public boolean isDone() {
        return status == FINISHED;
    }

    protected abstract String execute() throws Exception;

    public abstract String getName();

    public abstract String getShortDescription();

    public abstract String getDescription();

    public abstract CharSequence getUrl();

    public File getGitDir() {
        return gitdir;
    }

    public void cancel() {
        cancelled = true;
        if (executionThread != null) {
            Log.d(TAG, "Interrupting " + executionThread + " due to cancel");
            executionThread.interrupt();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public static URIish integrationGitServerURIFor(String repoPath) throws URISyntaxException, IOException {
        return new URIish()
                .setScheme("ssh")
                .setUser("git")
                .setHost("84.201.197.71")
                .setPort(2207)
                .setPath(repoPath);
    }
}
