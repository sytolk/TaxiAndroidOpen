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
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.io.File;

import static android.R.drawable.stat_sys_download;
import static org.eclipse.jgit.api.Git.cloneRepository;
import static org.eclipse.jgit.lib.Constants.DOT_GIT;
import static org.eclipse.jgit.lib.Constants.HEAD;

public class Clone extends GitOperation {

    public static final String TAG = "Clone";

    private final boolean bare;
    private final URIish sourceUri;
    private final File directory;
    private String branch = HEAD;


    MessagingProgressMonitor messagingProgressMonitor;

    CredentialsProvider credentialsProvider;

    TransportConfigCallback transportConfigCallback; //todo

    public Clone(boolean bare, URIish sourceUri, File directory, TransportConfigCallback transportConfigCallback, CredentialsProvider credentialsProvider) {
        super(bare ? directory : new File(directory, DOT_GIT));
        this.bare = bare;
        this.sourceUri = sourceUri;
        this.directory = directory;
        this.transportConfigCallback = transportConfigCallback;
        this.credentialsProvider = credentialsProvider;

        Log.d(TAG, "Constructed with " + sourceUri + " directory=" + directory + " gitdir=" + gitdir);
    }

    public String execute() throws Exception {
        Log.d(TAG, "Starting execute... directory=" + directory);
        ensureFolderExists(directory.getParentFile());

        try {
            //UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider("git", "gitads_!");

            cloneRepository()
                    .setBare(bare)
                    .setDirectory(directory)
                    .setURI(sourceUri.toPrivateString())
                            //  .setProgressMonitor(messagingProgressMonitor)
                    .setTransportConfigCallback(transportConfigCallback)
                    .setCredentialsProvider(credentialsProvider)
                    .setCloneAllBranches(false)  //test
                    //.setNoCheckout(true)      //test
                    //.setRemote("origin")     //test
                    .call();

            Log.d(TAG, "Completed checkout!");
        } catch (JGitInternalException e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            Log.d(TAG, "finally!");
            // repoUpdateBroadcaster.broadcastUpdate();
        }

        return "Cloned " + sourceUri.getHumanishName() + "Clone completed" + sourceUri.toString();
    }

    private static void ensureFolderExists(File folder) {
        if (!folder.exists()) {
            Log.d(TAG, "Folder " + folder + " needs to be created...");
            boolean created = folder.mkdirs();
            Log.d(TAG, "mkdirs 'created' returned : " + created
                    + " and gitDirParentFolder.exists()=" + folder.exists());
        }
    }

    public int getOngoingIcon() {
        return stat_sys_download;
    }

    public String getTickerText() {
        return "Cloning " + sourceUri;
    }

    public String getName() {
        return "Clone";
    }

    public String getDescription() {
        return "cloning " + sourceUri;
    }

    public CharSequence getUrl() {
        return sourceUri.toString();
    }

    public String getShortDescription() {
        return "Cloning";
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + sourceUri + "]";
    }
}
