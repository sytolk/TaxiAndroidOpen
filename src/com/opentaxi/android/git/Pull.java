/*
 * Copyright (c) 2011, 2012 Roberto Tyley
 *
 * This file is part of 'Agit' - an Android Git client.
 *
 * Agit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Agit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/ .
 */

package com.opentaxi.android.git;

import android.content.Context;
import android.util.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import static android.R.drawable.stat_sys_download;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_REBASE;
import static org.eclipse.jgit.lib.Constants.R_HEADS;

public class Pull extends GitOperation {

    public static final String TAG = "Pull";
    private final Repository repo;

    Git git;
    //MessagingProgressMonitor messagingProgressMonitor;
    CredentialsProvider credentialsProvider;
    TransportConfigCallback transportConfigCallback;
    Context context;

    public Pull(Repository repository, /*MessagingProgressMonitor messagingProgressMonitor,*/ TransportConfigCallback transportConfigCallback, CredentialsProvider credentialsProvider) {
        super(repository.getDirectory());
        this.repo = repository;
        git = new Git(repository);
        //this.messagingProgressMonitor = messagingProgressMonitor;
        this.credentialsProvider = credentialsProvider;
        this.transportConfigCallback = transportConfigCallback;
    }

    public int getOngoingIcon() {
        return stat_sys_download;
    }

    public String getTickerText() {
        return "Pulling!";
    }


    public RuntimeException exceptionMessage(int resId, java.lang.Object... formatArgs) {
        return new RuntimeException(context.getString(resId, formatArgs));
    }

    public String execute() {
        try {
            git.pull()//.setRebase(true)//.setProgressMonitor(messagingProgressMonitor)
                    .setTransportConfigCallback(transportConfigCallback)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return "Pull complete";
    }

    private AnyObjectId commitToMergeFor(FetchResult fetchRes, String remoteBranchName, boolean remote) {
        // we check the updates to see which of the updated branches
        // corresponds
        // to the remote branch name
        AnyObjectId commitToMerge;
        if (remote) {
            Ref r = null;
            if (fetchRes != null) {
                r = fetchRes.getAdvertisedRef(remoteBranchName);
                if (r == null)
                    r = fetchRes.getAdvertisedRef(R_HEADS + remoteBranchName);
            }
            if (r == null)
                throw new JGitInternalException(MessageFormat.format(JGitText
                        .get().couldNotGetAdvertisedRef, remoteBranchName));
            else
                commitToMerge = r.getObjectId();
        } else {
            try {
                commitToMerge = repo.resolve(remoteBranchName);
                if (commitToMerge == null)
                    Log.e(TAG, remoteBranchName);
            } catch (IOException e) {
                throw new JGitInternalException(
                        JGitText.get().exceptionCaughtDuringExecutionOfPullCommand,
                        e);
            }
        }
        return commitToMerge;
    }

    private void checkCancellation() {
        if (isCancelled()) Log.e(TAG, "pull_cancelled");
    }

    private String getConfigOrDie(Config repoConfig, String section, String subsection, String name) {
        String val = repoConfig.getString(section, subsection, name);
        if (val == null) {
            Log.e(TAG, "missing_configuration_for_key " + section + " " + subsection + " " + name);
        }
        return val;
    }

    private boolean branchUsesPullRebase(String branchName, Config repoConfig) {
        return repoConfig.getBoolean(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_REBASE, false);
    }

    public String getName() {
        return "Pull";
    }

    public String getDescription() {
        return "Pulling!";
    }

    public CharSequence getUrl() {
        return "Arrgg";
    }

    public String getShortDescription() {
        return "Pulling";
    }

    public File getGitDir() {
        return repo.getDirectory();
    }
}
