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

import android.os.RemoteException;
import android.util.Log;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSchException;
import com.madgag.ssh.android.authagent.AndroidAuthAgent;
import net.schmizz.sshj.common.Buffer;

public class SSHAgentIdentity implements Identity {
    private final String TAG = "SSHAgentIdentity";

    private final AndroidAuthAgent authAgentProvider;
    private final byte[] publicKey;
    private final String name;

    public SSHAgentIdentity(AndroidAuthAgent authAgentProvider, byte[] publicKey, String name) {
        this.authAgentProvider = authAgentProvider;
        this.publicKey = publicKey;
        this.name = name;
    }

    public void clear() {
    }

    public boolean decrypt() {
        return false;
    }

    public String getAlgName() {
        try {
            return new Buffer.PlainBuffer(publicKey).readString();
        } catch (Buffer.BufferException e) {
            Log.e(TAG, "getAlgName", e);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public byte[] getPublicKeyBlob() {
        return publicKey;
    }

    public byte[] getSignature(byte[] data) {
        try {
            return authAgentProvider.sign(publicKey, data);
        } catch (RemoteException e) {
            Log.e(TAG, "sign() failed", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isEncrypted() {
        return false;
    }

    public boolean setPassphrase(byte[] passphrase) throws JSchException {
        return false;
    }

}
