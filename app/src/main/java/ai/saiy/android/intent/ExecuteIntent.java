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

package ai.saiy.android.intent;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequest;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class collecting the most common intents used throughout the application
 * <p>
 * Created by benrandall76@gmail.com on 13/04/2016.
 */
public class ExecuteIntent {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ExecuteIntent.class.getSimpleName();

    /**
     * Execute a given intent
     *
     * @param ctx    the application context
     * @param intent the intent to execute
     * @return true if the intent executed correctly. False otherwise.
     */
    public static boolean executeIntent(@NonNull final Context ctx, @NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "executeIntent");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "executeIntent: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "executeIntent: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Start a service with a given intent
     *
     * @param ctx    the application context
     * @param intent the intent to execute
     * @return true if the intent executed correctly. False otherwise.
     */
    public static boolean startService(@NonNull final Context ctx, @NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startService");
        }

        try {
            final ComponentName componentName = ctx.startService(intent);

            if (componentName != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startService for: " + componentName.getPackageName());
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startService: componentName null");
                }
            }
            return true;
        } catch (final SecurityException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "startService: SecurityException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "startService: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Broadcast a given intent
     *
     * @param ctx    the application context
     * @param intent the intent to execute
     * @return true if the intent executed correctly. False otherwise.
     */
    public static boolean sendBroadcast(@NonNull final Context ctx, @NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendBroadcast");
        }

        try {
            ctx.sendBroadcast(intent, SaiyRequest.CONTROL_SAIY);
            return true;
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "sendBroadcast: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * Search the Play Store using the name of the desired application. If the user has the
     * Play Store application installed and defaulted, this URL will open directly in the application.
     * Otherwise, it will default to a browser search.
     *
     * @param ctx     the application context
     * @param appName to search for
     * @return true if an Activity was available to handle the intent. False otherwise.
     */
    public static boolean playStoreSearch(@NonNull final Context ctx, @NonNull final String appName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "playStoreSearch");
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(IntentConstants.PLAY_STORE_SEARCH_URL +
                appName + IntentConstants.PLAY_STORE_APPS_EXTENSION));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "playStoreSearch: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "playStoreSearch: Exception");
                e.printStackTrace();
            }
        }

        return false;

    }

    /**
     * Perform a web search from the device.
     *
     * @param ctx the application context
     * @param url the url to open
     * @return true if an Activity was available to handle the intent. False otherwise.
     */
    public static boolean webSearch(@NonNull final Context ctx, @NonNull final String url) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "webSearch: " + url);
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "webSearch: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "webSearch: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Send an uninstall broadcast
     *
     * @param ctx         the application context
     * @param packageName of the app to be uninstalled
     * @return true if an Activity was available to handle the intent. False otherwise.
     */
    public static boolean uninstallApp(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "uninstallApp");
        }

        final Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(IntentConstants.PACKAGE + packageName));

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "uninstallApp: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "uninstallApp: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Send an email.
     *
     * @param ctx       the application context
     * @param addresses recipient emails
     * @param subject   the email subject
     * @return true if an Activity was available to handle the intent. False otherwise.
     */
    public static boolean sendEmail(@NonNull final Context ctx, @NonNull final String[] addresses,
                                    @Nullable final String subject, @Nullable final String body) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendEmail");
        }

        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(IntentConstants.MAILTO));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (UtilsString.notNaked(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        if (UtilsString.notNaked(body)) {
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "sendEmail: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "sendEmail: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Launch the Android application settings for a specific application
     *
     * @param ctx         the application context
     * @param packageName of the desired application
     * @return true if the application settings are correctly opened
     */
    public static boolean openApplicationSpecificSettings(@NonNull final Context ctx,
                                                          @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "openApplicationSpecificSettings");
        }

        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"
                + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "openApplicationSpecificSettings: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "openApplicationSpecificSettings: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Start Google Now in listening mode
     *
     * @param ctx    the application context
     * @param secure true if the device is in secure mode
     * @return true if the intent was successful, false otherwise
     */
    public static boolean googleNowListen(@NonNull final Context ctx, final boolean secure) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "googleNowListen");
        }

        final Intent intent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        intent.setPackage(IntentConstants.PACKAGE_NAME_GOOGLE_NOW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(RecognizerIntent.EXTRA_SECURE, secure);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "googleNowListen: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "googleNowListen: Exception");
                e.printStackTrace();
            }
        }

        return false;

    }

    /**
     * Launch Google Now with a specific search term to resolve
     *
     * @param ctx        the application context
     * @param searchTerm the search term to resolve
     * @return true if the search term was handled correctly, false otherwise
     */
    public static boolean googleNow(@NonNull final Context ctx, @NonNull final String searchTerm) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "googleNow");
        }

        final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.setComponent(new ComponentName(IntentConstants.PACKAGE_NAME_GOOGLE_NOW,
                IntentConstants.PACKAGE_NAME_GOOGLE_NOW + IntentConstants.ACTIVITY_GOOGLE_NOW_SEARCH));

        intent.putExtra(SearchManager.QUERY, searchTerm);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "googleNow: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "googleNow: Exception");
                e.printStackTrace();
            }
        }

        return false;

    }

    /**
     * Launch Wolfram Alpha with a specific search term to resolve
     *
     * @param ctx        the application context
     * @param searchTerm the search term to resolve
     * @return true if the search term was passed correctly, false otherwise
     */
    public static boolean wolframAlpha(@NonNull final Context ctx, @NonNull final String searchTerm) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "wolframAlpha");
        }

        final Intent intent = new Intent(IntentConstants.INTENT_SEARCH_WOLFRAM_ALPHA);
        intent.setComponent(new ComponentName(IntentConstants.PACKAGE_NAME_WOLFRAM_ALPHA,
                IntentConstants.PACKAGE_NAME_WOLFRAM_ALPHA + IntentConstants.ACTIVITY_WOLFRAM_ALPHA_SEARCH));
        intent.setData(Uri.parse(searchTerm));
        intent.putExtra(SearchManager.QUERY, searchTerm);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "wolframAlpha: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "wolframAlpha: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Standard share intent
     *
     * @param ctx the application context
     * @return true if the intent was successfully processed, false otherwise
     */
    public static boolean shareIntent(@NonNull final Context ctx, @NonNull final String content) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shareIntent");
        }

        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType(IntentConstants.TEXT_PLAIN);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        final Intent chooserIntent = Intent.createChooser(intent, ctx.getString(R.string.chooser_share_via));
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(chooserIntent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "shareIntent: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "shareIntent: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Intent to prepare and send a feedback email.
     *
     * @param ctx the application context
     * @return true if the intent was successfully processed, false otherwise
     */
    public static boolean sendDeveloperEmail(final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "developerEmail");
        }
        return sendEmail(ctx, new String[]{Constants.SAIY_FEEDBACK_EMAIL}, ctx.getString(R.string.feedback),
                DeviceInfo.getDeviceInfo(ctx));
    }

    /**
     * Intent to return to the device home screen/launcher
     *
     * @param ctx the application context
     * @return true if the intent was successfully processed, false otherwise
     */
    public static boolean goHome(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "goHome");
        }

        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "goHome: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "goHome: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Launch an Activity of the application
     *
     * @param ctx     the application context
     * @param cls     the class to launch
     * @param newTask if the corresponding flag should be added
     * @return true if the activity was launched successfully, false otherwise
     */
    public static boolean saiyActivity(@NonNull final Context ctx, @NonNull final Class<?> cls,
                                       @Nullable final Bundle bundle, final boolean newTask) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "saiyActivity");
        }

        final Intent intent = new Intent(ctx, cls);

        if (newTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (UtilsBundle.notNaked(bundle)) {
            intent.putExtras(bundle);
        }

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "saiyActivity: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "saiyActivity: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Execute a Settings Intent
     *
     * @param ctx            the application context
     * @param intentConstant the {@link IntentConstants} identifying the intent to execute
     * @return true if the intent executed correctly. False otherwise.
     */
    @SuppressLint("InlinedApi")
    public static boolean settingsIntent(@NonNull final Context ctx, final int intentConstant) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "settingsIntent");
        }

        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (intentConstant) {

            case IntentConstants.SETTINGS_ACCESSIBILITY:
                intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                break;
            case IntentConstants.SETTINGS_VOICE_SEARCH:
                return voiceSearchSettings(ctx);
            case IntentConstants.SETTINGS_INPUT_METHOD:
                intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                break;
            case IntentConstants.SETTINGS_USAGE_STATS:
                intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                break;
            case IntentConstants.SETTINGS_VOLUME:
                intent.setAction(Settings.ACTION_SOUND_SETTINGS);
                break;
            case IntentConstants.SETTINGS_TEXT_TO_SPEECH:
                intent.setAction(IntentConstants.ACTION_TEXT_TO_SPEECH);
                break;
            case IntentConstants.SETTINGS_ADD_ACCOUNT:
                intent.setAction(Settings.ACTION_ADD_ACCOUNT);
                intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{Install.getAccountType()});
                break;
            default:
                break;
        }

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "settingsIntent: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "settingsIntent: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Execute a Voice Search Settings Intent
     *
     * @param ctx the application context
     * @return true if the intent executed correctly. False otherwise.
     */
    private static boolean voiceSearchSettings(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceSearchSettings");
        }

        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final ArrayList<ComponentName> componentArray = new ArrayList<>();
        componentArray.add(new ComponentName(Installed.PACKAGE_NAME_GOOGLE_NOW,
                IntentConstants.COMPONENT_VOICE_SEARCH_PREFERENCES_VELVET));
        componentArray.add(new ComponentName(Installed.PACKAGE_NAME_GOOGLE_NOW,
                IntentConstants.COMPONENT_VOICE_SEARCH_PREFERENCES));

        for (final ComponentName componentName : componentArray) {

            try {
                intent.setComponent(componentName);
                ctx.startActivity(intent);
                return true;
            } catch (final ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "voiceSearchSettings: ActivityNotFoundException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "voiceSearchSettings: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Execute a Voice Assist Settings Intent
     *
     * @param ctx the application context
     * @return true if the intent executed correctly. False otherwise.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean voiceAssistSettings21(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceAssistSettings21");
        }

        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_VOICE_INPUT_SETTINGS);

        try {
            ctx.startActivity(intent);
            return true;
        } catch (final ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "voiceAssistSettings21: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "voiceAssistSettings21: Exception");
                e.printStackTrace();
            }
        }

        return voiceSearchSettings(ctx);
    }
}
