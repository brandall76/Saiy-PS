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

package ai.saiy.android.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import ai.saiy.android.api.helper.BlackListHelper;
import ai.saiy.android.api.request.Regex;
import ai.saiy.android.api.request.SaiyKeyphrase;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.custom.CCC;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.Resolve;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle remote applications registering a keyphrase or hot word.
 * <p>
 * Started from {@link Context#sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)}
 * one of {@link Activity#RESULT_OK} or {@link Activity#RESULT_CANCELED} is returned to the
 * requesting application.
 * <p>
 * Created by benrandall76@gmail.com on 25/03/2016.
 */
public class BRRemote extends BroadcastReceiver {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BRRemote.class.getSimpleName();

    private static final String SAIY_INTENT_RECEIVER = "ai.saiy.android.SAIY_INTENT_RECEIVER";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onReceive");
        }

        if (intent == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, " onHandleIntent: Intent null");
            }
            return;
        }

        final String action = intent.getAction();
        if (DEBUG) {
            examineIntent(intent);
        }

        if (!UtilsString.notNaked(action)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, " onHandleIntent: action null");
            }
            return;
        }

        if (!intent.getAction().equals(SaiyKeyphrase.SAIY_REQUEST_RECEIVER)) {
            Log.e("Saiy Remote Request", "Incorrect ACTION: rejecting");
            return;
        }

        switch (intent.getIntExtra(SaiyKeyphrase.REQUEST_TYPE, 0)) {

            case SaiyKeyphrase.REQUEST_KEYPHRASE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onHandleIntent: REQUEST_KEYPHRASE");
                }

                final String keyphrase = intent.getStringExtra(SaiyKeyphrase.SAIY_KEYPHRASE);

                if (UtilsString.notNaked(keyphrase)) {

                    final String packageName = intent.getStringExtra(SaiyKeyphrase.REQUESTING_PACKAGE);

                    if (UtilsString.notNaked(packageName)) {

                        final BlackListHelper blackListHelper = new BlackListHelper();

                        if (!blackListHelper.isBlacklisted(context, packageName)) {

                            final Pair<Boolean, String> appPair = UtilsApplication.getAppNameFromPackage(
                                    context.getApplicationContext(), packageName);

                            if (appPair.first && UtilsString.notNaked(appPair.second)) {

                                final Locale vrLocale = SPH.getVRLocale(context.getApplicationContext());
                                final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                                        vrLocale);

                                final ArrayList<String> voiceData = new ArrayList<>(1);
                                voiceData.add(keyphrase);

                                final float[] confidence = new float[1];
                                confidence[0] = 1f;

                                final ArrayList<Pair<CC, Float>> resolvePair = new Resolve(
                                        context.getApplicationContext(), voiceData, confidence, sl).resolve();

                                if (!UtilsList.notNaked(resolvePair)) {

                                    final CustomCommand customCommand = new CustomCommand(CCC.CUSTOM_INTENT_SERVICE,
                                            CC.COMMAND_USER_CUSTOM, keyphrase, SaiyRequestParams.SILENCE,
                                            SaiyRequestParams.SILENCE,
                                            SPH.getTTSLocale(context.getApplicationContext()).toString(),
                                            vrLocale.toString(), LocalRequest.ACTION_SPEAK_ONLY);

                                    final Regex regex = (Regex) intent.getSerializableExtra(
                                            SaiyKeyphrase.KEYPHRASE_REGEX);

                                    switch (regex) {

                                        case MATCHES:
                                        case STARTS_WITH:
                                        case ENDS_WITH:
                                        case CONTAINS:
                                            customCommand.setRegex(regex);
                                            break;
                                        case CUSTOM:
                                            customCommand.setRegex(regex);
                                            customCommand.setRegularExpression(
                                                    intent.getStringExtra(SaiyKeyphrase.REGEX_CONTENT));
                                            break;
                                    }

                                    final Intent remoteIntent = new Intent(SAIY_INTENT_RECEIVER);
                                    remoteIntent.setPackage(packageName);

                                    final Bundle bundle = intent.getExtras();

                                    if (UtilsBundle.notNaked(bundle)) {
                                        if (!UtilsBundle.isSuspicious(bundle)) {
                                            if (DEBUG) {
                                                examineIntent(intent);
                                            }

                                            remoteIntent.putExtras(bundle);
                                            customCommand.setIntent(remoteIntent.toUri(0));

                                            final Pair<Boolean, Long> successPair = CustomCommandHelper.setCommand(
                                                    context.getApplicationContext(), customCommand, -1);

                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "Custom command created: " + successPair.first);
                                            }

                                            final Bundle responseBundle = new Bundle();
                                            final int responseCode = bundle.getInt(SaiyKeyphrase.SAIY_KEYPHRASE_ID, 0);

                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "Custom command responseCode: " + responseCode);
                                            }

                                            responseBundle.putInt(SaiyKeyphrase.SAIY_KEYPHRASE_ID, responseCode);

                                            final LocalRequest request = new LocalRequest(context.getApplicationContext());
                                            request.setVRLocale(vrLocale);
                                            request.setTTSLocale(SPH.getTTSLocale(context.getApplicationContext()));
                                            request.setSupportedLanguage(sl);
                                            request.setQueueType(SaiyTextToSpeech.QUEUE_ADD);
                                            request.setAction(LocalRequest.ACTION_SPEAK_ONLY);

                                            if (successPair.first) {

                                                request.setUtterance(PersonalityResponse.getRemoteCommandRegisterSuccess(
                                                        context.getApplicationContext(), sl, appPair.second, keyphrase));
                                                request.execute();

                                                setResult(Activity.RESULT_OK, SaiyKeyphrase.class.getSimpleName(),
                                                        responseBundle);
                                            } else {

                                                request.setUtterance(PersonalityResponse.getErrorRemoteCommandRegister(
                                                        context.getApplicationContext(), sl, appPair.second));
                                                request.execute();

                                                setResult(Activity.RESULT_CANCELED, SaiyKeyphrase.class.getSimpleName(),
                                                        responseBundle);
                                            }

                                        } else {
                                            Log.e("Saiy Remote Request", "Bundle rejected due to contents");
                                        }
                                    } else {
                                        Log.e("Saiy Remote Request", "Request bundle missing contents: rejected");
                                    }
                                } else {
                                    Log.e("Saiy Remote Request", "Conflict with inbuilt command: rejected");
                                }
                            } else {
                                Log.e("Saiy Remote Request", "Application name undetectable: rejected");
                            }
                        } else {
                            Log.e("Saiy Remote Request", "Application blacklisted: rejected");
                        }
                    } else {
                        Log.e("Saiy Remote Request", "Package name missing: rejected");
                    }
                } else {
                    Log.e("Saiy Remote Request", "Keyphrase missing: rejected");
                }

                break;
            default:
                Log.e("Saiy Remote Request", "Internal type error: rejected");
                break;
        }
    }

    /**
     * For debugging the intent extras
     *
     * @param intent containing potential extras
     */
    private void examineIntent(@NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineIntent");
        }

        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            for (final String key : keys) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "examineIntent: " + key + " ~ " + bundle.get(key));
                }
            }
        }
    }

}