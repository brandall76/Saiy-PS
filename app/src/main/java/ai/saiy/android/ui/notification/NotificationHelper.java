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

package ai.saiy.android.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.personality.AI;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.NotificationService;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to deal with notification configuration and updates. Static access for ease.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public final class NotificationHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = NotificationHelper.class.getSimpleName();

    public static final int MIN_NOTIFICATION_SPEECH_LENGTH = 35;

    public static final int NOTIFICATION_SELF_AWARE = 0;
    public static final int NOTIFICATION_HOTWORD = 1;
    public static final int NOTIFICATION_DRIVING_PROFILE = 2;
    public static final int NOTIFICATION_TUTORIAL = 3;

    public static final String NOTIFICATION_CHANNEL_PERMANENT = "not_channel_permanent";
    public static final String NOTIFICATION_CHANNEL_PRIORITY = "not_channel_priority";
    public static final String NOTIFICATION_CHANNEL_INTERACTION = "not_channel_interaction";
    public static final String NOTIFICATION_CHANNEL_INFORMATION = "not_channel_information";

    /**
     * Prevent instantiation
     */
    public NotificationHelper() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static void createNotificationChannels(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createNotificationChannels");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            if (UtilsList.notNaked(notificationManager.getNotificationChannels())) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "createNotificationChannels: already exist");
                }

                return;
            }

            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_PERMANENT,
                    ctx.getString(R.string.menu_not_channel_permanent), NotificationManager.IMPORTANCE_LOW);

            mChannel.setDescription(ctx.getString(R.string.menu_not_channel_permanent_description));
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_PRIORITY,
                    ctx.getString(R.string.menu_not_channel_priority), NotificationManager.IMPORTANCE_DEFAULT);

            mChannel.setDescription(ctx.getString(R.string.menu_not_channel_priority_description));
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setSound(null, null);
            notificationManager.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_INTERACTION,
                    ctx.getString(R.string.menu_not_channel_interaction), NotificationManager.IMPORTANCE_LOW);

            mChannel.setDescription(ctx.getString(R.string.menu_not_channel_interaction_description));
            mChannel.enableLights(false);
            notificationManager.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_INFORMATION,
                    ctx.getString(R.string.menu_not_channel_information), NotificationManager.IMPORTANCE_HIGH);

            mChannel.setDescription(ctx.getString(R.string.menu_not_channel_information_description));
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChannel);

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createNotificationChannels: not supported");
            }
        }
    }

    /**
     * Start the foreground notification
     *
     * @param ctx                  the application context
     * @param notificationConstant integer constant denoting if a notification action button should be displayed
     */
    @SuppressWarnings("deprecation")
    public static Notification getForegroundNotification(@NonNull final Context ctx, final int notificationConstant) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getForegroundNotification");
        }

        final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
        actionIntent.setPackage(ctx.getPackageName());
        actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_FOREGROUND);

        final PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_FOREGROUND,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_FOREGROUND,
                    actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String channel;

        switch (notificationConstant) {

            case NOTIFICATION_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_HOTWORD");
                }
                channel = NOTIFICATION_CHANNEL_PRIORITY;
                break;
            case NOTIFICATION_DRIVING_PROFILE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_DRIVING_PROFILE");
                }
                channel = NOTIFICATION_CHANNEL_PRIORITY;
                break;
            case NOTIFICATION_TUTORIAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_TUTORIAL");
                }
                channel = NOTIFICATION_CHANNEL_PRIORITY;
                break;
            case NOTIFICATION_SELF_AWARE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_SELF_AWARE");
                }
                channel = NOTIFICATION_CHANNEL_PERMANENT;
                break;

        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channel);

        final String contentTitle;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contentTitle = ctx.getString(R.string.app_name);
        } else {
            contentTitle = ctx.getString(R.string.app_name);
        }

        builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(ctx.getString(ai.saiy.android.R.string.notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(contentTitle)
                .setOngoing(true)
                .setContentText(String.format(ctx.getString(ai.saiy.android.R.string.notification_ai_level),
                        AI.getAILevel()));

        PendingIntent actionPendingIntent;

        switch (notificationConstant) {

            case NOTIFICATION_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_HOTWORD");
                }

                actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_HOTWORD);

                actionPendingIntent = PendingIntent.getService(ctx,
                        NotificationService.NOTIFICATION_HOTWORD, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.addAction(R.drawable.ic_blur, ctx.getString(R.string.notification_stop_hotword),
                        actionPendingIntent);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    builder.setPriority(Notification.PRIORITY_MAX);
                } else {
                    builder.setColorized(true);
                    builder.setColor(Color.RED);
                }

                break;

            case NOTIFICATION_SELF_AWARE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getForegroundNotification: NOTIFICATION_SELF_AWARE");
                }

                builder.setColorized(false);
                builder.setColor(Color.RED);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    builder.setPriority(Notification.PRIORITY_MIN);
                }

                break;

        }

        return builder.build();
    }

    /**
     * Show a listening notification
     *
     * @param ctx the application context
     */
    public static void createListeningNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createListeningNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_LISTENING);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_LISTENING,
                    actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INTERACTION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_btn_speak_now)
                    .setTicker(ctx.getString(ai.saiy.android.R.string.notification_listening)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(ai.saiy.android.R.string.notification_listening) + "... "
                            + ctx.getString(ai.saiy.android.R.string.notification_tap_cancel))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_LISTENING, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createListeningNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the listening notification
     *
     * @param ctx the application context
     */
    public static void cancelListeningNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelListeningNotification");
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationService.NOTIFICATION_LISTENING);
    }

    /**
     * Show a speaking notification
     *
     * @param ctx    the application context
     * @param length the length of the utterance
     */
    public static void createSpeakingNotification(@NonNull final Context ctx, final int length) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createSpeakingNotification");
        }

        if (length > MIN_NOTIFICATION_SPEECH_LENGTH) {

            try {

                final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
                actionIntent.setPackage(ctx.getPackageName());
                actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_SPEAKING);

                final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_SPEAKING,
                        actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INTERACTION);

                builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_media_pause)
                        .setTicker(ctx.getString(ai.saiy.android.R.string.notification_speaking)).setWhen(System.currentTimeMillis())
                        .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                        .setContentText(ctx.getString(ai.saiy.android.R.string.notification_speaking) + "... "
                                + ctx.getString(ai.saiy.android.R.string.notification_tap_stop))
                        .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setColorized(false);
                    builder.setColor(Color.RED);
                }

                final Notification not = builder.build();
                final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationService.NOTIFICATION_SPEAKING, not);

            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "createSpeakingNotification failure");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createSpeakingNotification: length " + length + " too short ignoring");
            }
        }
    }

    /**
     * Remove the speaking notification
     *
     * @param ctx the application context
     */
    public static void cancelSpeakingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelSpeakingNotification");
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationService.NOTIFICATION_SPEAKING);
    }

    /**
     * Show a fetching notification.
     *
     * @param ctx the application context
     */
    public static void createFetchingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createFetchingNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_FETCHING);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_FETCHING,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INTERACTION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setTicker(ctx.getString(ai.saiy.android.R.string.notification_fetching)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(ai.saiy.android.R.string.notification_fetching) + "... "
                            + ctx.getString(ai.saiy.android.R.string.notification_tap_cancel))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_FETCHING, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createFetchingNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the fetching notification
     *
     * @param ctx the application context
     */
    public static void cancelFetchingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelFetchingNotification");
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationService.NOTIFICATION_FETCHING);
    }

    /**
     * Show an initialising notification.
     *
     * @param ctx the application context
     */
    public static void createInitialisingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createInitialisingNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_INITIALISING);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_INITIALISING,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INTERACTION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_popup_sync)
                    .setTicker(ctx.getString(ai.saiy.android.R.string.notification_initialising_tts))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(ai.saiy.android.R.string.notification_initialising_tts) + "... "
                            + ctx.getString(ai.saiy.android.R.string.notification_tap_cancel))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_INITIALISING, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createInitialisingNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the initialising notification
     *
     * @param ctx the application context
     */
    public static void cancelInitialisingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelInitialisingNotification");
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationService.NOTIFICATION_INITIALISING);
    }

    /**
     * Show a computing notification.
     *
     * @param ctx the application context
     */
    public static void createComputingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createComputingNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_COMPUTING);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_COMPUTING,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INTERACTION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_popup_sync)
                    .setTicker(ctx.getString(ai.saiy.android.R.string.notification_computing)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(ai.saiy.android.R.string.notification_computing) + "... "
                            + ctx.getString(ai.saiy.android.R.string.notification_tap_cancel))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_COMPUTING, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createComputingNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the computing notification
     *
     * @param ctx the application context
     */
    public static void cancelComputingNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelComputingNotification");
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationService.NOTIFICATION_COMPUTING);
    }

    /**
     * Show a permissions notification.
     *
     * @param ctx          the application context
     * @param permissionId the permission id constant
     */
    public static void createPermissionsNotification(@NonNull final Context ctx, final int permissionId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createPermissionsNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_PERMISSIONS);
            actionIntent.putExtra(PermissionHelper.REQUESTED_PERMISSION, permissionId);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_PERMISSIONS,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INFORMATION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setTicker(ctx.getString(ai.saiy.android.R.string.permission_notification_ticker)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(ai.saiy.android.R.string.permission_notification_text))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_PERMISSIONS, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createPermissionsNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Show an emotion analysis notification.
     *
     * @param ctx the application context
     */
    public static void createEmotionAnalysisNotification(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createEmotionAnalysisNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION, NotificationService.NOTIFICATION_EMOTION);

            final PendingIntent pendingIntent = PendingIntent.getService(ctx, NotificationService.NOTIFICATION_EMOTION,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INFORMATION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setTicker(ctx.getString(R.string.emotion_notification_ticker)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(R.string.emotion_notification_text))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_EMOTION, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createEmotionAnalysisNotification failure");
                e.printStackTrace();
            }
        }
    }

    /**
     * Show an emotion analysis notification.
     *
     * @param ctx the application context
     */
    public static void createIdentificationNotification(@NonNull final Context ctx, final int condition,
                                                        final boolean success, @Nullable final Speaker.Confidence confidence) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createIdentificationNotification");
        }

        try {

            final Intent actionIntent = new Intent(NotificationService.INTENT_CLICK);
            actionIntent.setPackage(ctx.getPackageName());
            actionIntent.putExtra(NotificationService.CLICK_ACTION,
                    NotificationService.NOTIFICATION_IDENTIFICATION);
            actionIntent.putExtra(LocalRequest.EXTRA_CONDITION, condition);

            switch (condition) {

                case Condition.CONDITION_IDENTIFY:
                    actionIntent.putExtra(Speaker.EXTRA_IDENTIFY_OUTCOME, confidence);
                    break;
                case Condition.CONDITION_IDENTITY:
                    actionIntent.putExtra(Speaker.EXTRA_IDENTITY_OUTCOME, success);
                    break;
            }

            final PendingIntent pendingIntent = PendingIntent.getService(ctx,
                    NotificationService.NOTIFICATION_IDENTIFICATION,
                    actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_INFORMATION);

            builder.setContentIntent(pendingIntent).setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setTicker(ctx.getString(R.string.vocal_notification_ticker)).setWhen(System.currentTimeMillis())
                    .setContentTitle(ctx.getString(ai.saiy.android.R.string.app_name))
                    .setContentText(ctx.getString(R.string.vocal_notification_text))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setColorized(false);
                builder.setColor(Color.RED);
            }

            final Notification not = builder.build();
            final NotificationManager notificationManager = (NotificationManager)
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationService.NOTIFICATION_IDENTIFICATION, not);

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createIdentificationNotification failure");
                e.printStackTrace();
            }
        }
    }
}
