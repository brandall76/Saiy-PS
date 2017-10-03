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

package ai.saiy.android.nlu.apiai;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.gson.GsonBuilder;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.saiy.android.api.language.nlu.NLULanguageAPIAI;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class RemoteAPIAI {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RemoteAPIAI.class.getSimpleName();

    private final String utterance;
    private final AIDataService aiDataService;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param apiKey   the API AI api key
     * @param vrLocale the {@link NLULanguageAPIAI}
     */
    public RemoteAPIAI(@NonNull final Context mContext,
                       @NonNull final String utterance,
                       @NonNull final String apiKey,
                       @NonNull final NLULanguageAPIAI vrLocale) {
        this.utterance = utterance;

        final AIConfiguration config = new AIConfiguration(apiKey,
                AIConfiguration.SupportedLanguages.fromLanguageTag(vrLocale.getLocaleString()),
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(mContext, config);
    }

    public Pair<Boolean, String> fetch() {

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(utterance);

        try {

            final AIResponse response = aiDataService.request(aiRequest);

            if (response != null) {

                final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(response);

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "gsonString: " + response.toString());
                }

                return new Pair<>(true, gsonString);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "response null");
                }
            }

        } catch (final AIServiceException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "AIResponse AIServiceException");
                e.printStackTrace();
            }
        }

        return new Pair<>(false, null);
    }
}
