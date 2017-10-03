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

package ai.saiy.android.service.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ai.saiy.android.cache.speech.SpeechCachePrepare;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.tts.helper.SaiyVoice;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsFile;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to manage the caching of data associated with the
 * {@link SelfAware} Service.
 * <p/>
 * Created by benrandall76@gmail.com on 28/04/2016.
 */
public class SelfAwareCache extends SaiyProgressListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SelfAwareCache.class.getSimpleName();

    public static final int MAX_UTTERANCE_CHARS = 150;
    private static final long DBS_MAINTENANCE_INCREMENT = 40L;

    private final Context mContext;

    private volatile TextToSpeech ttsCache;
    private volatile SpeechCachePrepare scp;
    private volatile File tempFile;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public SelfAwareCache(@NonNull final Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Check if the current synthesis should be cached.
     *
     * @param params     the {@link SelfAwareParameters}
     * @param ttsLocale  the {@link TextToSpeech} {@link Locale}
     * @param utterance  the utterance
     * @param initEngine the initialised {@link TextToSpeech} engine
     * @param voice      the {@link SaiyVoice}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void shouldCache(@NonNull final SelfAwareParameters params, @NonNull final Locale ttsLocale,
                               @NonNull final String utterance, @NonNull final String initEngine,
                               @NonNull final SaiyVoice voice) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);

                if (UtilsString.notNaked(initEngine)) {
                    if (voice.isNetworkConnectionRequired()) {
                        if (params.shouldNetwork()) {

                            final DBSpeech dbSpeech = new DBSpeech(mContext);
                            if (!dbSpeech.entryExists(initEngine, voice.getName(), utterance)) {

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "shouldCache: proceeding");
                                }

                                final SpeechCachePrepare scp1 = new SpeechCachePrepare(mContext);
                                scp1.setVoice(voice);
                                scp1.setEngine(initEngine);
                                scp1.setUtterance(utterance);
                                scp1.setLocale(ttsLocale.toString());
                                SelfAwareCache.this.doAudioCache(scp1, params);


                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "shouldCache: entry already exists");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "shouldCache: network not requested");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "shouldCache: not network voice");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "shouldCache: initEngine naked");
                    }
                }
            }
        });
    }

    /**
     * Synthesise the audio to a temporary file in the application's internal storage cache, using
     * a temporary and short-lived {@link TextToSpeech} object. The result of this will report
     * to {@link SaiyProgressListener#onDone(String)} from where the raw pcm will be stored in {@link DBSpeech}
     *
     * @param scp    the populated {@link SpeechCachePrepare}
     * @param params the {@link SelfAwareParameters}
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doAudioCache(@NonNull final SpeechCachePrepare scp, @NonNull final SelfAwareParameters params) {
        this.scp = scp;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                ttsCache = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        switch (status) {
                            case TextToSpeech.SUCCESS:

                                ttsCache.setVoice(scp.getVoice());
                                ttsCache.setOnUtteranceProgressListener(SelfAwareCache.this);

                                tempFile = UtilsFile.getTempAudioFile(mContext);

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "doAudioCache: " + tempFile.getAbsolutePath());
                                }

                                ttsCache.synthesizeToFile(scp.getUtterance(), params.getBundle(), tempFile, params.getUtteranceId());

                                break;
                            case TextToSpeech.ERROR:
                                break;
                        }
                    }
                }, scp.getEngine());
            }
        });
    }

    @Override
    public void onAudioAvailable(final String utteranceId, final byte[] audio) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAudioAvailable");
        }
    }

    @Override
    public void onBeginSynthesis(final String utteranceId, final int sampleRateInHz, final int audioFormat,
                                 final int channelCount) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBeginSynthesis");
        }
    }

    @Override
    public void onStart(final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }
    }

    @Override
    public void onDone(final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDone");
        }

        try {
            scp.setUncompressedAudio(FileUtils.readFileToByteArray(tempFile));
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onDone: IOException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null) {
                final boolean success = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onDone: tempFile deleted: " + success);
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onDone: tempFile null");
                }
            }
        }

        shutdownTTS();
        speechMaintenance();
    }

    @Override
    public void onError(final String utteranceId) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
        }

        shutdownTTS();
    }

    /**
     * Check the {@link DBSpeech} size to see if we need to reduce it.
     */

    private void speechMaintenance() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "speechMaintenance");
        }

        if (SPH.getUsedIncrement(mContext) % DBS_MAINTENANCE_INCREMENT == 0) {
            new Thread() {
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

                    final DBSpeech dbSpeech = new DBSpeech(mContext);
                    if (dbSpeech.shouldRunMaintenance(mContext)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "shouldRunMaintenance: true");
                        }
                        dbSpeech.runMaintenance(mContext);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "shouldRunMaintenance: false");
                        }
                    }
                }
            }.start();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "shouldRunMaintenance: false: " + SPH.getUsedIncrement(mContext));
            }
        }
    }

    /**
     * Shutdown the temporary {@link TextToSpeech} object
     */
    private void shutdownTTS() {
        if (ttsCache != null) {
            try {
                ttsCache.shutdown();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "shutdownTTS: Exception");
                    e.printStackTrace();
                }
            }
        }
    }
}
