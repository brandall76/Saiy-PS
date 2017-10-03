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

package ai.saiy.android.thirdparty.tasker;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 10/08/2016.
 */

public class TaskerHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TaskerHelper.class.getSimpleName();

    private static final String TASKER_ACCESS_URI = "content://net.dinglisch.android.tasker/prefs";
    private static final String TASKER_CONTENT_URI = "content://net.dinglisch.android.tasker/tasks";
    private static final String COLUMN_PROJECT_NAME = "project_name";
    private static final String COLUMN_TASK_NAME = "name";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_ACCESS = "ext_access";


    /**
     * Get a list of the user's tasks and associated project names
     *
     * @param ctx the application context
     * @return an ArrayList of {@link TaskerTask} objects
     */
    public ArrayList<TaskerTask> getTasks(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTasks");
        }

        long then = System.nanoTime();

        final ArrayList<TaskerTask> taskArrayList = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = ctx.getContentResolver().query(Uri.parse(TASKER_CONTENT_URI), null, null, null, null);
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getTasks: cursor open Exception");
                e.printStackTrace();
            }
        }

        if (cursor != null) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getTasks: cursor count: " + cursor.getCount());
            }

            if (cursor.getCount() > 0) {

                try {

                    final int projectIndex = cursor.getColumnIndex(COLUMN_PROJECT_NAME);
                    final int taskIndex = cursor.getColumnIndex(COLUMN_TASK_NAME);

                    String projectName;
                    String taskName;
                    TaskerTask taskerTask;
                    while (cursor.moveToNext()) {

                        projectName = cursor.getString(projectIndex);
                        taskName = cursor.getString(taskIndex);

                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "Project Name: " + projectName);
                            MyLog.d(CLS_NAME, "Task Name: " + taskName);
                        }

                        if (UtilsString.notNaked(projectName) && UtilsString.notNaked(taskName)) {
                            taskerTask = new TaskerTask();
                            taskerTask.setProjectName(projectName);
                            taskerTask.setTaskName(taskName);
                            taskArrayList.add(taskerTask);
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "getTasks: project/task naked");
                            }
                        }
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getTasks: cursor seek Exception");
                        e.printStackTrace();
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTasks: cursor empty");
                }
            }

            try {
                cursor.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTasks: cursor close Exception");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getTasks: cursor null");
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "Tasks count: " + taskArrayList.size());
            MyLog.getElapsed(TaskerHelper.class.getSimpleName(), then);
        }

        return taskArrayList;
    }

    /**
     * Check if Tasker in enabled and external access permission have been granted.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting enabled, the seconds access permissions
     */
    public Pair<Boolean, Boolean> canInteract(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canInteract");
        }

        long then = System.nanoTime();

        Pair<Boolean, Boolean> toReturn = new Pair<>(false, false);

        Cursor cursor = null;

        try {
            cursor = ctx.getContentResolver().query(Uri.parse(TASKER_ACCESS_URI), null, null, null, null);
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "canInteract: cursor open Exception");
                e.printStackTrace();
            }
        }

        if (cursor != null) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "canInteract: cursor count: " + cursor.getCount());
            }

            if (cursor.getCount() > 0) {

                try {

                    final int enabledIndex = cursor.getColumnIndex(COLUMN_ENABLED);
                    final int accessIndex = cursor.getColumnIndex(COLUMN_ACCESS);

                    String enabled;
                    String access;
                    if (cursor.moveToFirst()) {

                        enabled = cursor.getString(enabledIndex);
                        access = cursor.getString(accessIndex);

                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "enabled: " + enabled);
                            MyLog.d(CLS_NAME, "access: " + access);
                        }

                        toReturn = new Pair<>(Boolean.parseBoolean(enabled), Boolean.parseBoolean(access));

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "canInteract: cursor failed to move to first?");
                        }
                    }

                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "canInteract: cursor seek Exception");
                        e.printStackTrace();
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "canInteract: cursor empty");
                }
            }

            try {
                cursor.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "canInteract: cursor close Exception");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "canInteract: cursor null");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(TaskerHelper.class.getSimpleName(), then);
        }

        return toReturn;
    }

    /**
     * Check if either of the Tasker packages are installed. The Eclair version is not supported
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting true if a package is installed, false
     * otherwise and the second the installed package name or null.
     */
    public Pair<Boolean, String> isTaskerInstalled(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isTaskerInstalled");
        }

        if (Installed.isPackageInstalled(ctx, Installed.PACKAGE_TASKER_DIRECT)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isTaskerInstalled: PACKAGE_TASKER_DIRECT");
            }
            return new Pair<>(true, Installed.PACKAGE_TASKER_DIRECT);
        }

        if (Installed.isPackageInstalled(ctx, Installed.PACKAGE_TASKER_MARKET)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isTaskerInstalled: PACKAGE_TASKER_MARKET");
            }
            return new Pair<>(true, Installed.PACKAGE_TASKER_MARKET);
        }

        return new Pair<>(false, null);
    }

    /**
     * Execute a tasker task of the supplied name
     *
     * @param ctx      the application context
     * @param taskName the task name
     * @return true if the Broadcast is sent successfully, false otherwise.
     */
    public boolean executeTask(@NonNull final Context ctx, @NonNull final String taskName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "executeTask");
        }

        final TaskerIntent intent = new TaskerIntent(taskName);

        try {
            ctx.sendBroadcast(intent);
            return true;
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "executeTask Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Show the external access settings in Tasker
     *
     * @param ctx the application context
     * @return true if the intent was executed successfully, false otherwise
     */
    public boolean showTaskerExternalAccess(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showTaskerExternalAccess");
        }

        final Intent intent = new Intent(TaskerIntent.getExternalAccessPrefsIntent());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "showTaskerExternalAccess Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Check if there is an install order permission issue
     *
     * @param ctx the application context
     * @return true if the receiver is reachable, false otherwise
     */
    public boolean receiverExists(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "receiverExists");
        }

        try {
            final List<ResolveInfo> receivers = ctx.getPackageManager().queryBroadcastReceivers(
                    new TaskerIntent(""), 0);
            return receivers != null && receivers.size() > 0;
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "receiverExists Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Send an Intent to Tasker containing an array list of detected voice data
     *
     * @param ctx       the application context
     * @param voiceData an array list of voice data
     * @return true if the intent executed successfully, false otherwise
     */
    public static boolean broadcastVoiceData(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "broadcastVoiceData");
        }

        final TaskerHelper taskerHelper = new TaskerHelper();
        final Pair<Boolean, String> taskerPair = taskerHelper.isTaskerInstalled(ctx);

        if (taskerPair.first) {

            final Intent intent = new Intent(IntentConstants.ACTION_SAIY_VOICE_DATA);
            intent.setPackage(taskerPair.second);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putStringArrayListExtra(IntentConstants.EXTRA_VOICE_DATA, voiceData);

            try {
                ctx.startActivity(intent);
                return true;
            } catch (final ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "broadcastVoiceData: ActivityNotFoundException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "broadcastVoiceData: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
}
