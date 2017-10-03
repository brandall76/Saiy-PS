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

package ai.saiy.android.cognitive.motion.provider.google;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.security.ProviderInstaller;

import ai.saiy.android.utils.MyLog;

/**
 * Class to setup the ActivityRecognition API and register the {@link MotionIntentService} for callbacks.
 * <p>
 * This must only survive as long as {@link ai.saiy.android.service.SelfAware} is running, so must
 * be destroyed as per its lifecycle.
 * <p>
 * Created by benrandall76@gmail.com on 06/07/2016.
 */

public class MotionRecognition implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = MotionRecognition.class.getSimpleName();

    private PendingIntent pendingIntent;
    private GoogleApiClient activityClient;

    /**
     * Prepare the Activity Recognition API for use.
     *
     * @param ctx the application context
     */
    public void prepare(@NonNull final Context ctx) {

        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionResult = apiAvailability.isGooglePlayServicesAvailable(ctx);

        switch (connectionResult) {

            case ConnectionResult.SUCCESS:

                activityClient = new GoogleApiClient.Builder(ctx).addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).addApi(ActivityRecognition.API).build();

                pendingIntent = PendingIntent.getService(ctx, MotionIntentService.REQUEST_CODE,
                        new Intent(ctx, MotionIntentService.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                try {

                    ProviderInstaller.installIfNeededAsync(ctx, new ProviderInstaller.ProviderInstallListener() {
                        @Override
                        public void onProviderInstalled() {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "prepare: play services onProviderInstalled");
                            }
                        }

                        @Override
                        public void onProviderInstallFailed(final int errorCode, final Intent intent) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "prepare: play services onProviderInstallFailed");
                            }

                            if (apiAvailability.isUserResolvableError(errorCode)) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "prepare: play services onProviderInstallFailed");
                                }

                                apiAvailability.showErrorNotification(ctx, errorCode);

                            } else {
                                // TODO - unrecoverable
                            }
                        }
                    });
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "prepare: play services unavailable");
                        e.printStackTrace();
                    }
                }

                break;

            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "prepare: play services unavailable");
                }
                apiAvailability.showErrorNotification(ctx, connectionResult);
                break;
        }
    }

    /**
     * Connect to receive activity recognition callbacks
     */
    public void connect() {

        if (activityClient != null) {
            activityClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnected");
        }

        if (activityClient != null && pendingIntent != null) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(activityClient,
                    MotionIntentService.UPDATE_INTERVAL, pendingIntent).setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnectionSuspended");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnectionFailed");
        }
    }

    @Override
    public void onResult(@NonNull final Status status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResult");
        }
    }

    /**
     * Cancel any future callbacks and disconnect the client.
     */
    public void destroy() {

        if (activityClient != null && pendingIntent != null) {

            try {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(activityClient, pendingIntent);
                activityClient.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "destroy: Exception");
                    e.printStackTrace();
                }
            }
        }
    }
}
