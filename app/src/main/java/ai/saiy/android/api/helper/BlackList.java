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

import java.util.ArrayList;
import java.util.ListIterator;

import ai.saiy.android.utils.MyLog;

/**
 * Helper class to store remote requests that have been denied due to a throttle limit.
 * <p>
 * Created by benrandall76@gmail.com on 06/03/2016.
 */
public class BlackList {

    private transient static final boolean DEBUG = MyLog.DEBUG;
    private transient static final String CLS_NAME = BlackList.class.getSimpleName();

    private static final int MAX_ACQUIRE_REJECT = 10;
    private static final int MAX_PAST_TIME = 30000;

    private final String packageName;
    private final int callingUid;
    private final long requestTime;

    /**
     * Constructor
     * <p>
     * Creates a BlackList object to store for future reference
     *
     * @param packageName of the calling application
     * @param callingUid  of the calling application
     * @param requestTime at which the remote request was made
     */
    public BlackList(@NonNull final String packageName, final int callingUid, final long requestTime) {
        this.packageName = packageName;
        this.callingUid = callingUid;
        this.requestTime = requestTime;
    }

    /**
     * Get the Uid of the calling package
     *
     * @return the calling Uid
     */
    private int getCallingUid() {
        return callingUid;
    }

    /**
     * Get the package name of the remote caller
     *
     * @return the remote package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Get the time the remote request was made
     *
     * @return the EPOCH time the request was made
     */
    private long getRequestTime() {
        return requestTime;
    }

    /**
     * Check if the remote package that is making the denied requests has done this too many times,
     * calculated using {@link #MAX_ACQUIRE_REJECT} & {@link #MAX_PAST_TIME}
     * <p>
     * It would also be possible to extend this method to analyse the Uid, but that is perhaps a
     * little too cautious?
     *
     * @param blackListArray the ArrayList of {@link BlackList}
     * @return true if the package should be permanently blacklisted
     */
    public static synchronized boolean shouldBlackList(final ArrayList<BlackList> blackListArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shouldBlackList: " + blackListArray.size());
        }

        final int blackListSize = blackListArray.size();

        if (blackListSize > 5) {

            final BlackList currentBlackList = blackListArray.get(blackListSize - 1);
            final String currentPackageName = currentBlackList.getPackageName();
            final int currentUid = currentBlackList.getCallingUid();
            final long currentRequestTime = currentBlackList.getRequestTime();

            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldBlackList: currentPackageName: " + currentPackageName);
                MyLog.v(CLS_NAME, "shouldBlackList: currentUid: " + currentUid);
                MyLog.v(CLS_NAME, "shouldBlackList: currentRequestTime: " + currentRequestTime);
            }

            BlackList blackList;
            String packageName;
            int matches = 0;

            for (int i = 0; i < (blackListSize - 1); i++) {

                blackList = blackListArray.get(i);
                packageName = blackList.getPackageName();

                if (packageName.matches(currentPackageName)) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "shouldBlackList: difference: "
                                + (currentRequestTime - blackList.getRequestTime()));
                    }

                    if ((currentRequestTime - blackList.getRequestTime()) < MAX_PAST_TIME) {
                        matches++;
                    }
                }
            }

            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldBlackList: matches: " + matches);
            }

            final ListIterator itr = blackListArray.listIterator();

            if (matches > MAX_ACQUIRE_REJECT) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Blacklisted package: " + currentPackageName);
                }

                while (itr.hasNext()) {
                    blackList = (BlackList) itr.next();
                    packageName = blackList.getPackageName();

                    if (packageName.matches(currentPackageName)) {
                        itr.remove();
                    } else if ((currentRequestTime - blackList.getRequestTime()) > MAX_PAST_TIME) {
                        itr.remove();
                    }
                }

                return true;
            } else {

                while (itr.hasNext()) {
                    blackList = (BlackList) itr.next();

                    if ((currentRequestTime - blackList.getRequestTime()) > MAX_PAST_TIME) {
                        itr.remove();
                    }
                }
            }

            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldBlackList: blackListArray trimmed size: " + blackListArray.size());
            }
        }

        return false;
    }
}
