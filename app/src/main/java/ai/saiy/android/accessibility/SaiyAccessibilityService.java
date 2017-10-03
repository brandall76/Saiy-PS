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

package ai.saiy.android.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.Resolve;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.RecognitionAction;
import ai.saiy.android.recognition.helper.RecognitionDefaults;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle accessibility service events.
 * Created by benrandall76@gmail.com on 03/08/2016.
 */

public class SaiyAccessibilityService extends AccessibilityService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyAccessibilityService.class.getSimpleName();

    private static final long COMMAND_UPDATE_DELAY = 3000L;
    private static final long UPDATE_TIMEOUT = 250L;
    private long previousCommandTime;
    private String previousCommandProcessed = null;
    private String previousCommandInterimChecked = null;
    private String previousCommandFinalChecked = null;

    private final Pattern pGoogleNow = Pattern.compile(Installed.PACKAGE_NAME_GOOGLE_NOW, Pattern.CASE_INSENSITIVE);
    private final Pattern pGoogleNowInterim = Pattern.compile(RecognitionDefaults.GOOGLE_NOW_INTERIM_FIELD, Pattern.CASE_INSENSITIVE);
    private final Pattern pGoogleNowFinal = Pattern.compile(RecognitionDefaults.GOOGLE_NOW_FINAL_EDIT_TEXT, Pattern.CASE_INSENSITIVE);


    private SupportedLanguage sl;
    private Pattern pListening;

    private boolean initInterceptGoogle;
    private boolean initAnnounceNotifications;

    private final boolean EXTRA_VERBOSE = false;

    @Override
    public void onCreate() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext()));
        final SaiyResources sr = new SaiyResources(getApplicationContext(), sl);
        final String listening = sr.getString(R.string.listening);
        sr.reset();

        pListening = Pattern.compile("^" + listening + ".*?", Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected void onServiceConnected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onServiceConnected");
        }

        initInterceptGoogle = SPH.getInterceptGoogle(getApplicationContext());
        initAnnounceNotifications = SPH.getAnnounceNotifications(getApplicationContext());

        setDynamicContent();
    }

    /**
     * Set the content this service should be receiving
     */
    private void setDynamicContent() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setDynamicContent: interceptGoogle: " + initInterceptGoogle);
            MyLog.i(CLS_NAME, "setDynamicContent: announceNotifications: " + initAnnounceNotifications);
        }

        if (!initInterceptGoogle && !initAnnounceNotifications) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDynamicContent: none required: finishing");
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                this.disableSelf();
//            }
//
//            this.stopSelf();

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDynamicContent: updating content");
            }

            final AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();

            serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
            serviceInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            serviceInfo.notificationTimeout = UPDATE_TIMEOUT;

            if (initInterceptGoogle && initAnnounceNotifications) {
                serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                        | AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                        | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            } else if (initInterceptGoogle) {
                serviceInfo.packageNames = new String[]{Installed.PACKAGE_NAME_GOOGLE_NOW};
                serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                        | AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;
            } else {
                serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            }

            this.setServiceInfo(serviceInfo);
        }
    }

    /**
     * Check if we need to update the content we are receiving by comparing the current content values to the ones checked
     * on the most recent accessibility event.
     *
     * @param interceptGoogle       if we should be intercepting Google Now commands
     * @param announceNotifications if we should be announcing notification content
     */
    private void updateServiceInfo(final boolean interceptGoogle, final boolean announceNotifications) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateServiceInfo");
        }

        if (initInterceptGoogle != interceptGoogle || initAnnounceNotifications != announceNotifications) {
            initInterceptGoogle = interceptGoogle;
            initAnnounceNotifications = announceNotifications;
            setDynamicContent();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "updateServiceInfo: no change");
            }
        }
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAccessibilityEvent");
        }

        updateServiceInfo(SPH.getInterceptGoogle(getApplicationContext()),
                SPH.getAnnounceNotifications(getApplicationContext()));

        if (!initAnnounceNotifications && !initInterceptGoogle) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: not required");
            }
            return;
        }

        if (event != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: contentDesc: " + event.getContentDescription());
                getEventType(event.getEventType());
            }

            AccessibilityNodeInfo source = null;

            switch (event.getEventType()) {

                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:

                    if (initAnnounceNotifications) {

                        final Parcelable parcelable = event.getParcelableData();

                        if (parcelable != null) {

                            if (parcelable instanceof Notification) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "fast instance of Notification: continuing");
                                }

                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "fast not instance of Notification");
                                }
                                return;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "fast parcelable null");
                            }
                            return;
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: fast not announcing notifications");
                        }
                        return;
                    }

                    break;
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:

                    if (initInterceptGoogle) {

                        if (event.getPackageName() != null && pGoogleNow.matcher(event.getPackageName()).matches()) {

                            source = event.getSource();

                            if (source == null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: fast: source null");
                                }
                                return;
                            }

                            if (source.getClassName() == null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: fast: source className null");
                                }

                                try {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: fast: recycling source");
                                    }
                                    source.recycle();
                                } catch (final IllegalStateException e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "onAccessibilityEvent: fast: IllegalStateException source recycle");
                                        e.printStackTrace();
                                    }
                                }

                                return;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: fast: checking for google: false");
                            }
                            return;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: fast: not intercepting Google");
                        }
                        return;
                    }

                    break;

            }

            switch (event.getEventType()) {

                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOW_CONTENT_CHANGED");
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: checking for google: true");
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: event.getPackageName: " + event.getPackageName());
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: event.getClassName: " + event.getClassName());
                    }

                    //noinspection ConstantConditions
                    if (pGoogleNowInterim.matcher(source.getClassName()).matches()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: className interim: true");
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: source.getClassName: " + source.getClassName());
                        }

                        if (source.getText() != null) {

                            final String text = source.getText().toString();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: interim text: " + text);
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: interim contentDesc: " + source.getContentDescription());
                            }

                            if (UtilsString.notNaked(text)) {

                                if (!commandPreviousInterimChecked(text)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: false");
                                    }

                                    previousCommandInterimChecked = text;

                                    if (interimMatch(text)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: child: interim match: true");
                                        }

                                        if (commandDelaySufficient(event.getEventTime())) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: true");
                                            }

                                            if (!commandPreviousMatches(text)) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: false");
                                                }

                                                previousCommandTime = event.getEventTime();
                                                previousCommandProcessed = text;

                                                killGoogle(true);
                                                process(text);

                                            } else {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: true");
                                                }
                                            }
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: false");
                                            }
                                        }
                                        break;
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: child: interim match: false");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: true");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: interim text: naked");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: interim text: null");
                            }
                        }
                    } else if (pGoogleNowFinal.matcher(source.getClassName()).matches()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: className final: true");
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: source.getClassName: " + source.getClassName());
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: source.getText: " + source.getText());
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: source.contentDesc: " + source.getContentDescription());
                        }

                        final int childCount = source.getChildCount();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: childCount: " + childCount);
                        }

                        if (childCount > 0) {
                            for (int i = 0; i < childCount; i++) {

                                final String text = examineChild(source.getChild(i));

                                if (UtilsString.notNaked(text)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: child text: " + text);
                                    }

                                    if (!commandPreviousFinalChecked(text)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: false");
                                        }

                                        previousCommandFinalChecked = text;

                                        if (finalMatch(text)) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: child: final match: true");
                                            }

                                            if (commandDelaySufficient(event.getEventTime())) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: true");
                                                }

                                                if (!commandPreviousMatches(text)) {
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: false");
                                                    }

                                                    previousCommandTime = event.getEventTime();
                                                    previousCommandProcessed = text;

                                                    killGoogle(true);
                                                    process(text);

                                                } else {
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: true");
                                                    }
                                                }
                                            } else {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: false");
                                                }
                                            }
                                            break;
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: child: final match: false");
                                            }
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: true");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: child text: naked");
                                    }
                                }
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: className: unwanted " + source.getClassName());
                        }

                        if (EXTRA_VERBOSE) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted contentDesc: " + source.getContentDescription());
                            }

                            if (source.getText() != null) {

                                final String text = source.getText().toString();
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: " + text);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: null");
                                }
                            }

                            final int childCount = source.getChildCount();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted childCount: " + childCount);
                            }

                            if (childCount > 0) {

                                for (int i = 0; i < childCount; i++) {

                                    final String text = examineChild(source.getChild(i));

                                    if (UtilsString.notNaked(text)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted child text: " + text);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    break;

                case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_SELECTION_CHANGED");
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: checking for google: true");
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: event.getPackageName: " + event.getPackageName());
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: event.getClassName: " + event.getClassName());
                    }

                    //noinspection ConstantConditions
                    if (pGoogleNowFinal.matcher(source.getClassName()).matches()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: className final editText: true");
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: source.getClassName: " + source.getClassName());
                        }

                        if (source.getText() != null) {

                            final String text = source.getText().toString();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: final editText text: " + text);
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: final editText contentDesc: " + source.getContentDescription());
                            }

                            if (UtilsString.notNaked(text)) {

                                if (!commandPreviousFinalChecked(text)) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: false");
                                    }

                                    previousCommandFinalChecked = text;

                                    if (finalMatch(text)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: child: final match: true");
                                        }

                                        if (commandDelaySufficient(event.getEventTime())) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: true");
                                            }

                                            if (!commandPreviousMatches(text)) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: false");
                                                }

                                                previousCommandTime = event.getEventTime();
                                                previousCommandProcessed = text;

                                                killGoogle(true);
                                                process(text);

                                            } else {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousMatches: true");
                                                }
                                            }
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "onAccessibilityEvent: commandDelaySufficient: false");
                                            }
                                        }
                                        break;
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onAccessibilityEvent: final editText match: false");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: commandPreviousChecked: true");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onAccessibilityEvent: final editText: naked");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: final editText text: null");
                            }
                        }
                    }

                    break;

                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAccessibilityEvent: not interested in type");
                    }

                    if (EXTRA_VERBOSE) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted contentDesc: " + source.getContentDescription());
                        }

                        if (source.getText() != null) {

                            final String text = source.getText().toString();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: " + text);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted text: null");
                            }
                        }

                        final int childCount = source.getChildCount();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted childCount: " + childCount);
                        }

                        if (childCount > 0) {

                            for (int i = 0; i < childCount; i++) {

                                final String text = examineChild(source.getChild(i));

                                if (text != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAccessibilityEvent: unwanted child text: " + text);
                                    }
                                }
                            }
                        }
                    }

                    break;

            }

            try {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: recycling source");
                }
                if (source != null) {
                    source.recycle();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onAccessibilityEvent: IllegalStateException source recycle");
                    e.printStackTrace();
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccessibilityEvent: event null");
            }
        }

    }

    /**
     * Check if the previous command was actioned within the {@link #COMMAND_UPDATE_DELAY}
     *
     * @param currentTime the time of the current {@link AccessibilityEvent}
     * @return true if the delay is sufficient to proceed, false otherwise
     */
    private boolean commandDelaySufficient(final long currentTime) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandDelaySufficient");
        }

        final long delay = (currentTime - COMMAND_UPDATE_DELAY);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandDelaySufficient: delay: " + delay);
            MyLog.i(CLS_NAME, "commandDelaySufficient: previousCommandTime: " + previousCommandTime);
        }

        return delay > previousCommandTime;
    }

    /**
     * Check if the previous command/text matches the previous text we processed
     *
     * @param text the current text
     * @return true if the text matches the previous text we processed, false otherwise.
     */
    private boolean commandPreviousMatches(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandPreviousMatches");
        }

        return previousCommandProcessed != null && previousCommandProcessed.matches(text);
    }

    /**
     * Check if the previous command/text matches the current text we are considering processing
     *
     * @param text the current text
     * @return true if the text matches the previous text we processed, false otherwise.
     */
    private boolean commandPreviousInterimChecked(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandPreviousInterimChecked");
        }

        return previousCommandInterimChecked != null && previousCommandInterimChecked.matches(text);
    }

    /**
     * Check if the previous command/text matches the current text we are considering processing
     *
     * @param text the current text
     * @return true if the text matches the previous text we processed, false otherwise.
     */
    private boolean commandPreviousFinalChecked(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "commandPreviousFinalChecked");
        }

        return previousCommandFinalChecked != null && previousCommandFinalChecked.matches(text);
    }

    /**
     * Check if the interim text matches a command we want to intercept
     *
     * @param text the intercepted text
     * @return true if the text matches a command false otherwise
     */
    private boolean interimMatch(@NonNull final String text) {

        if (isListening(text)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "interimMatch: listening");
            }
            return false;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "interimMatch: Processing: " + text);
            }
        }

        final ArrayList<String> toResolve = new ArrayList<>(1);
        toResolve.add(text);

        final float[] confidence = new float[1];
        confidence[0] = 1f;

        final ArrayList<Pair<CC, Float>> resolveArray = new Resolve(getApplicationContext(),
                toResolve, confidence, sl, true).resolve();

        if (DEBUG) {
            if (UtilsList.notNaked(resolveArray)) {
                MyLog.i(CLS_NAME, "interimMatch: resolveArray size:  " + resolveArray.size());
                MyLog.i(CLS_NAME, "interimMatch: resolveArray CC:  " + resolveArray.get(0).first.name());
            } else {
                MyLog.i(CLS_NAME, "interimMatch: resolveArray naked");
            }
        }

        return UtilsList.notNaked(resolveArray);
    }

    /**
     * Check if the final text matches a command we want to intercept
     *
     * @param text the intercepted text
     * @return true if the text matches a command false otherwise
     */
    private boolean finalMatch(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "finalMatch");
        }

        if (isListening(text)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "finalMatch: listening");
            }
            return false;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "finalMatch: Processing: " + text);
            }
        }

        final ArrayList<String> toResolve = new ArrayList<>(1);
        toResolve.add(text);

        final float[] confidence = new float[1];
        confidence[0] = 1f;

        final ArrayList<Pair<CC, Float>> resolveArray = new Resolve(getApplicationContext(),
                toResolve, confidence, sl, false).resolve();

        if (DEBUG) {
            if (UtilsList.notNaked(resolveArray)) {
                MyLog.i(CLS_NAME, "finalMatch: resolveArray size:  " + resolveArray.size());
                MyLog.i(CLS_NAME, "finalMatch: resolveArray CC:  " + resolveArray.get(0).first.name());
            } else {
                MyLog.i(CLS_NAME, "finalMatch: resolveArray naked");
            }
        }

        return UtilsList.notNaked(resolveArray);
    }

    /**
     * Check if the text matches the standard 'listening...' text
     *
     * @param text the input text
     * @return true if a match is found, false otherwise
     */
    private boolean isListening(@NonNull final String text) {
        return pListening.matcher(text).matches();
    }

    /**
     * Process the extracted text as identified as a command
     *
     * @param text the command to process
     */
    private void process(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "process");
        }

        final Bundle bundle = new Bundle();

        final ArrayList<String> voiceResults = new ArrayList<>(1);
        voiceResults.add(text);

        final float[] confidence = new float[1];
        confidence[0] = 1f;

        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, voiceResults);
        bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, confidence);
        bundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_GOOGLE_NOW);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                new RecognitionAction(SaiyAccessibilityService.this.getApplicationContext(), SPH.getVRLocale(SaiyAccessibilityService.this.getApplicationContext()),
                        SPH.getTTSLocale(SaiyAccessibilityService.this.getApplicationContext()), sl, bundle);
            }
        });
    }

    /**
     * Kill or reset Google
     */
    private void killGoogle(final boolean terminate) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "killGoogle");
        }

        if (terminate) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    ExecuteIntent.googleNow(SaiyAccessibilityService.this.getApplicationContext(), "");

                    try {
                        Thread.sleep(150);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "killGoogle InterruptedException");
                            e.printStackTrace();
                        }
                    }

                    ExecuteIntent.goHome(SaiyAccessibilityService.this.getApplicationContext());
                    UtilsApplication.killPackage(SaiyAccessibilityService.this.getApplicationContext(), IntentConstants.PACKAGE_NAME_GOOGLE_NOW);
                }
            });
        } else {
            ExecuteIntent.googleNow(getApplicationContext(), "");
        }
    }

    /**
     * Recursively examine the {@link AccessibilityNodeInfo} object
     *
     * @param parent the {@link AccessibilityNodeInfo} parent object
     * @return the extracted text or null if no text was contained in the child objects
     */
    private String examineChild(@Nullable final AccessibilityNodeInfo parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineChild");
        }

        if (parent != null) {

            for (int i = 0; i < parent.getChildCount(); i++) {

                final AccessibilityNodeInfo nodeInfo = parent.getChild(i);

                if (nodeInfo != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo: getClassName: " + nodeInfo.getClassName());
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo: contentDesc: " + nodeInfo.getContentDescription());
                    }

                    if (nodeInfo.getText() != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: have text: returning: " + nodeInfo.getText().toString());
                        }
                        return nodeInfo.getText().toString();
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: text: null: recurse");
                        }

                        final int childCount = nodeInfo.getChildCount();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "examineChild: childCount: " + childCount);
                        }

                        if (childCount > 0) {

                            final String text = examineChild(nodeInfo);

                            if (text != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "examineChild: have recursive text: returning: " + text);
                                }
                                return text;
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "examineChild: recursive text: null");
                                }
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "examineChild: nodeInfo null");
                    }
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examineChild: parent null");
            }
        }

        return null;
    }

    /**
     * Check the event type for debugging
     *
     * @param eventType the Accessibility event type
     * @return the Accessibility event type
     */
    private int getEventType(final int eventType) {

        switch (eventType) {

            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_ANNOUNCEMENT");
                }
                break;
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_ASSIST_READING_CONTEXT");
                }
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_GESTURE_DETECTION_END");
                }
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_GESTURE_DETECTION_START");
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_NOTIFICATION_STATE_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_EXPLORATION_GESTURE_END");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_EXPLORATION_GESTURE_START");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_END");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_START");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_CONTEXT_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_FOCUSED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_HOVER_ENTER");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_HOVER_EXIT");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_LONG_CLICKED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_SCROLLED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_SELECTED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_SELECTION_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOW_CONTENT_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPE_WINDOWS_CHANGED");
                }
                break;
            case AccessibilityEvent.TYPES_ALL_MASK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccessibilityEvent: TYPES_ALL_MASK");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onAccessibilityEvent: default");
                }
                break;
        }

        return eventType;
    }

    @Override
    public void onInterrupt() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onInterrupt");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}
