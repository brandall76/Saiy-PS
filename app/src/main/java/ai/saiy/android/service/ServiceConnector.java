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

package ai.saiy.android.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Pair;

import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.utils.MyLog;

/**
 * All requests to the {@link SelfAware} service should be directed here. Locally accessing the service
 * in this way is better for performance that statically using any speech or recognition methods directly.
 * <p/>
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public class ServiceConnector {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ServiceConnector.class.getSimpleName();

    private SelfAware selfAwareService;

    private boolean bound = false;

    private long then;

    private final Context mContext;
    private final LocalRequest request;

    /**
     * Constructor.
     *
     * @param mContext the application context
     * @param request  the {@link LocalRequest} parameters
     */
    public ServiceConnector(@NonNull final Context mContext, @NonNull final LocalRequest request) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }

        this.mContext = mContext.getApplicationContext();
        this.request = request;

        SelfAwareHelper.startSelfAwareIfRequired(this.mContext);
    }

    /**
     * Create the connection
     */
    public void createConnection() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createConnection");
        }

        then = System.nanoTime();

        final Intent intent = request.getRequestIntent();
        doBindService(intent);
    }

    /**
     * Bind to the service
     *
     * @param intent to send
     */
    private void doBindService(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doBindService");
        }

        bound = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (DEBUG) {
            MyLog.d(CLS_NAME, "doBindService: bound: " + bound);
        }
    }

    /**
     * Our {@link ServiceConnection} object
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder iBinder) {

            if (iBinder != null) {

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected");
                    MyLog.i(CLS_NAME, "onServiceConnected: CLS: " + iBinder.getClass().getSimpleName());
                    MyLog.v(CLS_NAME, "onServiceConnected: binder alive: " + iBinder.isBinderAlive());
                }

                selfAwareService = ((SelfAware.BoundSA) iBinder).getService();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected: TTS Locale: " + request.getTTSLocale().toString());
                    MyLog.i(CLS_NAME, "onServiceConnected: Recognition Locale: " + request.getVRLocale().toString());
                }

                final Pair<Boolean, Integer> isSpeakingPair = selfAwareService.isSpeaking();
                final boolean isListening = selfAwareService.isListening();
                final Pair<Boolean, Boolean> isHotwordActive = selfAwareService.isHotwordActive();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: " + isSpeakingPair.first);
                    MyLog.i(CLS_NAME, "isListening:" + isListening);
                    MyLog.i(CLS_NAME, "isHotwordActive:" + isHotwordActive.first);
                    MyLog.i(CLS_NAME, "isHotwordRestartScheduled:" + isHotwordActive.second);
                }

                if (request.getAction() == LocalRequest.ACTION_TOGGLE_HOTWORD) {

                    if (isHotwordActive.first || isHotwordActive.second) {
                        request.setAction(LocalRequest.ACTION_STOP_HOTWORD);
                        request.setShutdownHotword();
                    } else {
                        request.setAction(LocalRequest.ACTION_START_HOTWORD);
                    }
                }

                if (isHotwordActive.first && request.getAction() == LocalRequest.ACTION_START_HOTWORD) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "ACTION_START_HOTWORD: already running");
                    }
                } else if (isListening) {
                    selfAwareService.stopListening(request.getShutdownHotword());
                } else if (isSpeakingPair.first) {

                    final int currentPriority = isSpeakingPair.second;
                    final int requestPriority = request.getSpeechPriority();

                    if (requestPriority == currentPriority && request.getQueueType() != TextToSpeech.QUEUE_ADD) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "priorities match - stopping speech");
                        }
                        selfAwareService.stopSpeech(request.shouldPreventRecognition());
                    } else {

                        if (request.getAction() == LocalRequest.ACTION_UNKNOWN) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "priority mismatch - ACTION_UNKNOWN - stopping speech");
                            }
                            selfAwareService.stopSpeech(request.shouldPreventRecognition());
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "priority mismatch - completing request");
                            }
                            completeRequest();
                        }
                    }

                } else {
                    completeRequest();
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onServiceConnected: iBinder null");
                }
            }

            doUnbindService();
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onServiceDisconnected");
            }
            selfAwareService = null;
        }
    };

    private void completeRequest() {

        switch (request.getAction()) {

            case LocalRequest.ACTION_SPEAK_ONLY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected: ACTION_SPEAK_ONLY");
                }
                selfAwareService.speakOnly(request.getBundle());
                break;
            case LocalRequest.ACTION_SPEAK_LISTEN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected: ACTION_SPEAK_LISTEN");
                }
                selfAwareService.speakListen(request.getBundle());
                break;
            case LocalRequest.ACTION_START_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected: ACTION_START_HOTWORD");
                }
                selfAwareService.startHotwordDetection(request.getBundle());
                break;
            case LocalRequest.ACTION_STOP_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected: ACTION_STOP_HOTWORD");
                }
                selfAwareService.stopListening(true);
                break;
            case LocalRequest.ACTION_UNKNOWN:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onServiceConnected: ACTION_UNKNOWN");
                }

                request.setUtterance(PersonalityResponse.getErrorActionUnknown(mContext,
                        request.getSupportedLanguage()));
                selfAwareService.speakOnly(request.getBundle());
                break;
        }
    }


    /**
     * Unbind the service, being careful to handle any unwanted behaviour
     */
    private void doUnbindService() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doUnbindService");
        }

        if (bound) {
            if (selfAwareService != null) {
                if (mContext != null) {
                    if (mConnection != null) {
                        mContext.unbindService(mConnection);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "doUnbindService: mConnection: null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "doUnbindService: mContext: null");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "doUnbindService: selfAwareService: null");
                }
            }

            bound = false;

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "doUnbindService: bound: false");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
    }
}

