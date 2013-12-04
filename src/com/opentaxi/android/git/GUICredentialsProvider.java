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

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

public class GUICredentialsProvider extends CredentialsProvider {


    private String username;

    private char[] password;

    public GUICredentialsProvider(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        return true;
    }

   /* @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        for (CredentialItem ci : items) {
            if (ci instanceof CredentialItem.YesNoType) {
                handle((CredentialItem.YesNoType) ci);
            } else if (ci instanceof CredentialItem.StringType) {
                handle(uri, (CredentialItem.StringType) ci);
            } else if (ci instanceof CredentialItem.CharArrayType) {
                handle(uri, (CredentialItem.CharArrayType) ci);
            } else {
                return false;
            }
        }
        return true;
    }*/

    @Override
    public boolean get(URIish uri, CredentialItem... items)
            throws UnsupportedCredentialItem {
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                ((CredentialItem.Username) i).setValue(username);
                continue;
            }
            if (i instanceof CredentialItem.Password) {
                ((CredentialItem.Password) i).setValue(password);
                continue;
            }
            if (i instanceof CredentialItem.StringType) {
                if (i.getPromptText().equals("Password: ")) { //$NON-NLS-1$
                    ((CredentialItem.StringType) i).setValue(new String(
                            password));
                    continue;
                }
            }
            throw new UnsupportedCredentialItem(uri, i.getClass().getName()
                    + ":" + i.getPromptText()); //$NON-NLS-1$
        }
        return true;
    }

    private void handle(URIish uri, CredentialItem.StringType ci) {
        if (ci instanceof CredentialItem.Username && uri.getUser() != null) {
            ci.setValue(uri.getUser());
        } else {
           // ci.setValue(blockingPrompt.request(prompt(String.class, uiNotificationFor(ci))));
        }
    }

    private void handle(URIish uri, CredentialItem.CharArrayType ci) {
        if (ci instanceof CredentialItem.Password && uri.getPass() != null) {
            ci.setValue(uri.getPass().toCharArray());
        } else {
           // ci.setValue(blockingPrompt.request(prompt(String.class, uiNotificationFor(ci))).toCharArray());
        }
    }

    private void handle(CredentialItem.YesNoType ci) {
       // ci.setValue(blockingPrompt.request(promptYesOrNo(uiNotificationFor(ci))));
    }

   /* private OpNotification uiNotificationFor(CredentialItem ci) {
        return alert(ci.getPromptText(), ci.getPromptText());
    }*/
}
