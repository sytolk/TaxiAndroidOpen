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

package com.opentaxi.android.git.jsch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.madgag.ssh.android.authagent.AndroidAuthAgent;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.BIND_AUTO_CREATE;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AndroidAuthAgentProvider implements AndroidAuthAgent {

    protected static final String TAG = "AAAP";

    private final Lock lock = new ReentrantLock();
    private final Condition authAgentBound = lock.newCondition(); // TODO CountDownLatch or
    // AbstractQueuedSynchronizer varient?
    private AndroidAuthAgent authAgent;
    //private final ComponentName preferredAuthAgentComponentNameProvider;


    public AndroidAuthAgentProvider(Context context) {
        //this.preferredAuthAgentComponentNameProvider = preferredAuthAgentComponentNameProvider;
        bindSshAgentTo(context);
    }

    public AndroidAuthAgent get() {
        waitForAuthAgentBind();
        return authAgent;
    }

    private void bindSshAgentTo(Context context) {
        Intent intent = new Intent("org.openintents.ssh.BIND_SSH_AGENT_SERVICE");
        //intent.setComponent(preferredAuthAgentComponentNameProvider);
        context.bindService(intent, new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected() : Lost " + authAgent);
                authAgent = null;
            }

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.i(TAG, "onServiceConnected() : componentName=" + name + " binder=" + binder);
                authAgent = AndroidAuthAgent.Stub.asInterface(binder);
                // showDebugInfoForAuthAgent(); Showing this info is actually a bit confusing
                signalAuthAgentBound();
            }
        }, BIND_AUTO_CREATE);
        Log.i(TAG, "made request using context " + context + " to bind to the SSH_AGENT_SERVICE");
    }

    private void waitForAuthAgentBind() {
        lock.lock();
        Log.d(TAG, "waitForAuthAgentBind() entered: agent=" + authAgent);
        try {
            if (authAgent != null) {
                Log.d(TAG, "Already got non-null agent=" + authAgent + " -no need to wait.");
                return;
            }
            boolean gotAgentBeforeTimeOut = authAgentBound.await(5, SECONDS);
            Log.d(TAG, "gotAgentBeforeTimeOut=" + gotAgentBeforeTimeOut + " agent=" + authAgent);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted waiting for AndroidAuthAgent", e);
        } finally {
            lock.unlock();
        }
    }

    private void signalAuthAgentBound() {
        lock.lock();
        try {
            authAgentBound.signal();
        } finally {
            lock.unlock();
        }
    }

    /*private void showDebugInfoForAuthAgent() {
        Log.d(TAG, "authAgent=" + authAgent);
        try {
            Log.d(TAG, "authAgent.getIdentities()=" + authAgent.getIdentities());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public Map getIdentities() throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] sign(byte[] bytes, byte[] bytes2) throws RemoteException {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IBinder asBinder() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
