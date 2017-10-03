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

package ai.saiy.android.command.translate.provider.google;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 02/05/2016.
 */
public class GoogleTranslateAPI {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = GoogleTranslateAPI.class.getSimpleName();

    /**
     * Perform a synchronous translation request
     *
     * @param ctx      the application context
     * @param text     the text to be translated
     * @param language the {@link TranslationLanguageGoogle}
     * @return a {@link Pair} with the first parameter donating success and the second the result
     */
    public Pair<Boolean, String> execute(@NonNull final Context ctx, @NonNull final String text,
                                         @NonNull final TranslationLanguageGoogle language) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        final long then = System.nanoTime();

        try {

            final Translate translate = new Translate.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(), null).setApplicationName(ctx.getString(R.string.app_name)).build();

            final List<String> textList = new ArrayList<>();
            textList.add(text);

            final Translate.Translations.List list = translate.new Translations().list(textList,
                    language.toString());
            list.setKey(GoogleConfiguration.GOOGLE_TRANSLATE_API_KEY);

            final TranslationsListResponse response = list.execute();

            if (response != null && !response.isEmpty()) {
                if (DEBUG) {
                    for (final TranslationsResource resource : response.getTranslations()) {
                        MyLog.i(CLS_NAME, "resource: " + resource.getTranslatedText());
                    }
                }

                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, then);
                }

                return new Pair<>(true, StringEscapeUtils.unescapeHtml4(response.getTranslations().get(0).getTranslatedText()));
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: IOException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: Exception");
                e.printStackTrace();
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return new Pair<>(false, null);
    }
}
