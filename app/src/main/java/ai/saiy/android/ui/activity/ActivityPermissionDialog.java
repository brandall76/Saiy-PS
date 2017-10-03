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

package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;

/**
 * Short-lived Activity class to handle the permission requests from the user.
 * <p>
 * Created by benrandall76@gmail.com on 12/04/2016.
 */
public class ActivityPermissionDialog extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityPermissionDialog.class.getSimpleName();

    private long then;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        this.setFinishOnTouchOutside(false);

        then = System.nanoTime();

        final Bundle bundle = getIntent().getExtras();

        if (UtilsBundle.notNaked(bundle) && !UtilsBundle.isSuspicious(bundle)) {

            if (bundle.containsKey(PermissionHelper.REQUESTED_PERMISSION)) {

                final String[] permissions = bundle.getStringArray(PermissionHelper.REQUESTED_PERMISSION);

                if (permissions != null && permissions.length > 0) {

                    if (bundle.containsKey(PermissionHelper.REQUESTED_PERMISSION_ID)) {

                        final int permissionsId = bundle.getInt(PermissionHelper.REQUESTED_PERMISSION_ID, 0);

                        if (permissionsId > 0) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "extractPermission: extract successful");
                            }

                            if (shouldShowRequestPermissionRationale(permissions)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "User has previously denied");
                                }
                                createPermissionsNotification(permissionsId);
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "First time request");
                                }
                                ActivityCompat.requestPermissions(ActivityPermissionDialog.this, permissions, permissionsId);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "extractPermission: bundle REQUESTED_PERMISSION_ID 0");
                            }
                            createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "extractPermission: bundle missing REQUESTED_PERMISSION_ID");
                        }
                        createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "extractPermission: bundle REQUESTED_PERMISSION naked");
                    }
                    createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "extractPermission: bundle missing REQUESTED_PERMISSION");
                }
                createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "extractPermission: bundle null or empty");
            }
            createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRequestPermissionsResult");
        }

        switch (requestCode) {

            case PermissionHelper.REQUEST_AUDIO:

                if (granted(grantResults)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQUEST_AUDIO: PERMISSION_GRANTED");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onRequestPermissionsResult: REQUEST_AUDIO: PERMISSION_DENIED");
                    }
                    createPermissionsNotification(requestCode);
                }
                break;
            case PermissionHelper.REQUEST_FILE:

                if (granted(grantResults)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQUEST_FILE: PERMISSION_GRANTED");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onRequestPermissionsResult: REQUEST_FILE: PERMISSION_DENIED");
                    }
                    createPermissionsNotification(requestCode);
                }
                break;
            case PermissionHelper.REQUEST_GROUP_CONTACTS:

                if (granted(grantResults)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQUEST_GROUP_CONTACTS: PERMISSION_GRANTED");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onRequestPermissionsResult: REQUEST_GROUP_CONTACTS: PERMISSION_DENIED");
                    }
                    createPermissionsNotification(requestCode);
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onRequestPermissionsResult: Unknown request?");
                }
                createPermissionsNotification(PermissionHelper.REQUEST_UNKNOWN);
                break;
        }

        finish();
    }

    /**
     * Loop through the permissions we are about to request and check if a rationale is required
     * for any of them
     *
     * @param permissions the String array of permissions that will be requested
     * @return true if any of the permissions require a rationale to be shown.
     */
    private boolean shouldShowRequestPermissionRationale(@NonNull final String[] permissions) {
        for (final String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityPermissionDialog.this, permission)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Helper method to confirm if the permissions were granted or denied
     *
     * @param grantResults from the permission listener
     * @return true if the permissions were granted. False otherwise
     */
    private boolean granted(int[] grantResults) {
        for (final int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * We deal with permission rationale via speech, rather than a dialog.
     *
     * @param permissionId constant id from {@link PermissionHelper} of the requested permission
     */
    private void createPermissionsNotification(final int permissionId) {
        NotificationHelper.createPermissionsNotification(getApplicationContext(), permissionId);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
    }

}
