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

import android.util.Log;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.util.Collection;

import static android.R.drawable.stat_sys_download;
import static com.opentaxi.android.git.Repos.uriForRemote;

public class Fetch extends GitOperation {

    public static final String TAG = "Fetch";

    private final Repository repository;
    private final String remote;
    private final String fetchUrl;
    private final Collection<RefSpec> toFetch;
    /**
     * Default remote name used by clone, push and fetch operations
     */
    public static final String DEFAULT_REMOTE_NAME = "origin";

    GitFetchService fetchService;

    public Fetch(Repository repository, CredentialsProvider credentialsProvider, TransportConfigCallback transportConfigCallback) {
        // List<RefSpec> refSpecList = new ArrayList<RefSpec>();
        //refSpecList.add(new RefSpec().setForceUpdate(true));
        this(repository, DEFAULT_REMOTE_NAME, null, credentialsProvider, transportConfigCallback);
    }

    public Fetch(Repository repository, String remote, Collection<RefSpec> toFetch, /*MessagingProgressMonitor messagingProgressMonitor,*/ CredentialsProvider credentialsProvider, TransportConfigCallback transportConfigCallback) {
        super(repository.getDirectory());
        this.repository = repository;
        this.remote = remote;
        this.toFetch = toFetch;
        fetchUrl = uriForRemote(repository, remote).toString();
        fetchService = new GitFetchService(repository, credentialsProvider, transportConfigCallback);
    }

    public int getOngoingIcon() {
        return stat_sys_download;
    }

    public String getTickerText() {
        return "Fetching " + remote + " " + fetchUrl;
    }

    public String execute() {
        Log.d(TAG, "start execute() : repository=" + repository + " remote=" + remote);
        FetchResult r = fetchService.fetch(remote, toFetch);
        return "Fetch complete Fetched " + remote + " " + fetchUrl;
    }

    public String getName() {
        return "Fetch";
    }

    public String getDescription() {
        return "fetching " + remote + " " + fetchUrl;
    }

    public CharSequence getUrl() {
        return fetchUrl;
    }

    public String getShortDescription() {
        return "Fetching " + remote;
    }

    public File getGitDir() {
        return repository.getDirectory();
    }
}
