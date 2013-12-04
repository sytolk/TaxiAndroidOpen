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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.*;

import java.util.Collection;
import java.util.Map;


public class GitFetchService {

    private static String TAG = "GFS";

    Git git;
    //MessagingProgressMonitor messagingProgressMonitor;
    CredentialsProvider credentialsProvider;
    TransportConfigCallback transportConfigCallback;

    public GitFetchService(Repository repository, /*MessagingProgressMonitor messagingProgressMonitor,*/ CredentialsProvider credentialsProvider, TransportConfigCallback transportConfigCallback) {

        this.git = new Git(repository);
        // this.messagingProgressMonitor = messagingProgressMonitor;
        this.credentialsProvider = credentialsProvider;
        this.transportConfigCallback = transportConfigCallback;
    }

    public FetchResult fetch(String remote, Collection<RefSpec> toFetch) {
        Log.i(TAG, "About to run fetch : " + remote);

        for (Map.Entry<String, Ref> entry : git.getRepository().getAllRefs().entrySet()) {
            Log.i(TAG, entry.getKey() + " = " + entry.getValue());
        }
        FetchResult fetchResult = null;
        try {
            RemoteConfig config = new RemoteConfig(git.getRepository().getConfig(), remote);
            final String dst = Constants.R_REMOTES + config.getName() + "/" + "*";

            RefSpec refSpec = new RefSpec();
            refSpec = refSpec.setForceUpdate(true);
            refSpec = refSpec.setSourceDestination(Constants.R_HEADS + "*", dst);

            fetchResult = git.fetch()
                    .setRemote(remote)
                    .setTagOpt(TagOpt.FETCH_TAGS)
                            //.setRefSpecs(toFetch == null ? Collections.<RefSpec>emptyList() : newArrayList(toFetch))
                            //.setProgressMonitor(messagingProgressMonitor)

                    .setTransportConfigCallback(transportConfigCallback)
                    .setCredentialsProvider(credentialsProvider)
                    .setRefSpecs(refSpec)
                    .call();

           // checkout(git.getRepository(),fetchResult);
           //git.checkout().setName("master").setForce(true).call();
            //Log.i(TAG, "Reset to: " + remote);
           git.reset().setRef("remotes/origin/master").setMode(ResetCommand.ResetType.HARD).call();//.setRef(initialCommit.getName()).call();

            //mergeResult = mergeObjects(git.getRepository(), fetchResult);


            /*MergeCommand merge = new MergeCommand(repo);
            merge.setStrategy(MergeStrategy.THEIRS);
            merge.include(upstreamName, commitToMerge);
            MergeResult mergeRes = merge.call();
            result = new PullResult(fetchRes, remote, mergeRes);*/

        } catch (GitAPIException e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage()); // throw exceptionWithFriendlyMessageFor(e);
            e.printStackTrace();
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage()); // throw exceptionWithFriendlyMessageFor(e);
            e.printStackTrace();
        }
        if (fetchResult != null) {
            Log.i(TAG, "Fetch complete with : " + fetchResult + " messages=" + fetchResult.getMessages());
            for (Ref ref : fetchResult.getAdvertisedRefs()) {
                Log.d(TAG, "AdvertisedRef : " + ref.getName() + " objectId=" + ref.getObjectId());
            }
            for (TrackingRefUpdate update : fetchResult.getTrackingRefUpdates()) {
                Log.i(TAG, "TrackingRefUpdate : " + update.getLocalName() + " old=" + update.getOldObjectId() + " new=" + update.getNewObjectId());
            }
        }
       /* if (mergeResult != null) {
            Log.i(TAG, "Merge complete with : " + mergeResult + " MergeStatus=" + mergeResult.getMergeStatus().isSuccessful());
            for (String conflict : mergeResult.getCheckoutConflicts()) {
                Log.d(TAG, "Merge conflict : " + conflict);
            }
        }*/
        //repoUpdateBroadcaster.broadcastUpdate();
        return fetchResult;
    }

    /*private void checkout(Repository clonedRepo, FetchResult result)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException, GitAPIException {
        String branch = Constants.HEAD;

        Ref head = result.getAdvertisedRef(branch);
        if (branch.equals(Constants.HEAD)) {
            Ref foundBranch = findBranchToCheckout(result);
            if (foundBranch != null)
                head = foundBranch;
        }

        if (head == null || head.getObjectId() == null)
            return; // throw exception?

        *//*if (head.getName().startsWith(Constants.R_HEADS)) {
            final RefUpdate newHead = clonedRepo.updateRef(Constants.HEAD);
            newHead.disableRefLog();
            newHead.link(head.getName());
            addMergeConfig(clonedRepo, head);
        }*//*

        final RevCommit commit = parseCommit(clonedRepo, head);

        boolean detached = !head.getName().startsWith(Constants.R_HEADS);
        RefUpdate u = clonedRepo.updateRef(Constants.HEAD, detached);
        u.setNewObjectId(commit.getId());
        u.forceUpdate();

       // if (!bare) {
            DirCache dc = clonedRepo.lockDirCache();
            DirCacheCheckout co = new DirCacheCheckout(clonedRepo, dc, commit.getTree());
            co.checkout();
            //if (cloneSubmodules) cloneSubmodules(clonedRepo);
       // }
    }

    private Ref findBranchToCheckout(FetchResult result) {
        final Ref idHEAD = result.getAdvertisedRef(Constants.HEAD);
        if (idHEAD == null)
            return null;

        Ref master = result.getAdvertisedRef(Constants.R_HEADS
                + Constants.MASTER);
        if (master != null && master.getObjectId().equals(idHEAD.getObjectId()))
            return master;

        Ref foundBranch = null;
        for (final Ref r : result.getAdvertisedRefs()) {
            final String n = r.getName();
            if (!n.startsWith(Constants.R_HEADS))
                continue;
            if (r.getObjectId().equals(idHEAD.getObjectId())) {
                foundBranch = r;
                break;
            }
        }
        return foundBranch;
    }

    private RevCommit parseCommit(final Repository clonedRepo, final Ref ref)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException {
        final RevWalk rw = new RevWalk(clonedRepo);
        final RevCommit commit;
        try {
            commit = rw.parseCommit(ref.getObjectId());
        } finally {
            rw.release();
        }
        return commit;
    }

    private final static String DOT = "."; //$NON-NLS-1$

    private MergeResult mergeObjects(Repository repo, FetchResult fetchRes) throws GitAPIException {

        // we check the updates to see which of the updated branches
        // corresponds
        // to the remote branch name
        // get the configured remote for the currently checked out branch
        // stored in configuration key branch.<branch name>.remote
        String branchName = null;
        try {
            String fullBranch = repo.getFullBranch();
            if (fullBranch == null)
                throw new NoHeadException(
                        JGitText.get().pullOnRepoWithoutHEADCurrentlyNotSupported);
            if (!fullBranch.startsWith(Constants.R_HEADS)) {
                // we can not pull if HEAD is detached and branch is not
                // specified explicitly
                throw new DetachedHeadException();
            }
            branchName = fullBranch.substring(Constants.R_HEADS.length());
        } catch (IOException e) {
            throw new JGitInternalException(
                    JGitText.get().exceptionCaughtDuringExecutionOfPullCommand,
                    e);
        } catch (NoHeadException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DetachedHeadException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Config repoConfig = repo.getConfig();
        String remote = repoConfig.getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                ConfigConstants.CONFIG_KEY_REMOTE);
        if (remote == null)
            // fall back to default remote
            remote = Constants.DEFAULT_REMOTE_NAME;

        final boolean isRemote = !remote.equals("."); //$NON-NLS-1$

        // get the name of the branch in the remote repository
        // stored in configuration key branch.<branch name>.merge
        String remoteBranchName = repoConfig.getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                ConfigConstants.CONFIG_KEY_MERGE);

        AnyObjectId commitToMerge = null;
        String remoteUri;
        if (isRemote) {
            remoteUri = repoConfig.getString(
                    ConfigConstants.CONFIG_REMOTE_SECTION, remote,
                    ConfigConstants.CONFIG_KEY_URL);
            Ref r = null;
            if (fetchRes != null) {
                r = fetchRes.getAdvertisedRef(remoteBranchName);
                if (r == null)
                    r = fetchRes.getAdvertisedRef(Constants.R_HEADS
                            + remoteBranchName);
            }
            if (r == null)
                throw new JGitInternalException(MessageFormat.format(JGitText
                        .get().couldNotGetAdvertisedRef, remoteBranchName));
            else
                commitToMerge = r.getObjectId();
        } else {
            remoteUri = "local repository";
            try {
                commitToMerge = repo.resolve(remoteBranchName);
                if (commitToMerge == null)
                    throw new RefNotFoundException(MessageFormat.format(
                            JGitText.get().refNotResolved, remoteBranchName));
            } catch (IOException e) {
                throw new JGitInternalException(
                        JGitText.get().exceptionCaughtDuringExecutionOfPullCommand,
                        e);
            } catch (RefNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


        String upstreamName = "branch \'"
                + Repository.shortenRefName(remoteBranchName) + "\' of "
                + remoteUri;

        Log.i(TAG, "upstreamName:" + upstreamName);
        return git.merge().setStrategy(MergeStrategy.RESOLVE).include(upstreamName, commitToMerge).call();
    }*/
}
