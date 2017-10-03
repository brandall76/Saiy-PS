/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.api.helper;

import android.support.annotation.NonNull;

import ai.saiy.android.api.RequestParcel;

/**
 * Helper class to store remote request information.
 * <p>
 * Created by benrandall76@gmail.com on 06/03/2016.
 */
public class Callback {

    private final RequestParcel parcel;
    private final String packageName;
    private final int callingUid;
    private final long requestTime;

    /**
     * Constructor
     *
     * @param parcel      of request information sent via IPC
     * @param packageName of the remote application
     * @param callingUid  of the remote application
     * @param requestTime of the remote request
     */
    public Callback(final RequestParcel parcel, @NonNull final String packageName,
                    final int callingUid, final long requestTime) {
        this.parcel = parcel;
        this.packageName = packageName;
        this.callingUid = callingUid;
        this.requestTime = requestTime;
    }

    /**
     * Get the Uid of the remote application
     *
     * @return the Uid
     */
    public int getCallingUid() {
        return callingUid;
    }

    /**
     * Get the package name of the remote application
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Get the time of the remote request
     *
     * @return the EPOCH time
     */
    public long getRequestTime() {
        return requestTime;
    }

    /**
     * Get the request parcel sent via IPC
     *
     * @return the {@link RequestParcel}
     */
    public RequestParcel getParcel() {
        return parcel;
    }
}
