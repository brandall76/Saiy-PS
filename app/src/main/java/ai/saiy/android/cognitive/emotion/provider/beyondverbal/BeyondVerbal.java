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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.audio.AudioConfig;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.BVCredentials;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.StartRequestBody;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.StartResponse;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.http.BVStartRequest;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.http.BVStreamAudio;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.language.SupportedLanguageBV;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.user.MetaData;
import ai.saiy.android.configuration.BeyondVerbalConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 13/08/2016.
 */

public class BeyondVerbal {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BeyondVerbal.class.getSimpleName();

    public static final long FETCH_ANALYSIS_DELAY = 6000;
    public static final long MINIMUM_AUDIO_TIME = 13000;

    private final SupportedLanguage sl;
    private final RecognitionMic mic;
    private final Context mContext;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public BeyondVerbal(@NonNull final Context mContext, @NonNull final RecognitionMic mic,
                        @NonNull final SupportedLanguage sl) {
        this.mContext = mContext;
        this.mic = mic;
        this.sl = sl;
    }

    public void stream() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stream");
        }

        final String token = getToken();

        if (UtilsString.notNaked(token)) {

            final Pair<Boolean, StartResponse> startRequest = new BVStartRequest(mContext, token)
                    .getId(new StartRequestBody(AudioConfig.getDefault(), MetaData.getEmpty(),
                            SupportedLanguageBV.getSupportedLanguage(sl.getLocale())).prepare());

            if (startRequest.first) {
                if (startRequest.second.isSuccessful()) {
                    final String recordingId = startRequest.second.getRecordingId();

                    if (mic.isAvailable()) {
                        new BVStreamAudio(mic, sl, token, recordingId).stream();
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "mic unavailable");
                        }

                        Recognition.setState(Recognition.State.IDLE);
                        onError();
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "startRequest.second.isSuccessful()");
                    }
                    onError();
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startRequest.first");
                }
                onError();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "token naked");
            }
            onError();
        }
    }

    private void onError() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onError");
        }

        Recognition.setState(Recognition.State.IDLE);

        final LocalRequest localRequest = new LocalRequest(mContext);
        localRequest.setSupportedLanguage(sl);
        localRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        localRequest.setUtterance(PersonalityResponse.getBeyondVerbalServerErrorResponse(mContext, sl));
        localRequest.setTTSLocale(SPH.getTTSLocale(mContext));
        localRequest.setVRLocale(SPH.getVRLocale(mContext));
        localRequest.execute();
    }

    private String getToken() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getID");
        }

        final Pair<Boolean, BVCredentials> tokenPair = BVCredentials.refreshTokenIfRequired(mContext,
                BeyondVerbalConfiguration.API_KEY);

        if (tokenPair.first) {
            return tokenPair.second.getAccessToken();
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "failed to get token");
            }
        }

        return null;

    }
}
