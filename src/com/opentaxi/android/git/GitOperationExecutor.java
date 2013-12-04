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

public class GitOperationExecutor {

    static {
        HarmonyFixInflater.establishHarmoniousRepose();
    }

    private static final String TAG = "GOE";


    public String call(GitOperation operation) throws Exception {

        try {
            return operation.executeAndRecordThread();
        } finally {
            Log.d(TAG, "Exiting op scope");
        }
    }
}
