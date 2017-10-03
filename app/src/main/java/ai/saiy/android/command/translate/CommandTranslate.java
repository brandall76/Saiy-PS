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

package ai.saiy.android.command.translate;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.command.translate.provider.bing.BingTranslate;
import ai.saiy.android.command.translate.provider.google.GoogleTranslate;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to process a Translation request. Handles both remote NLP intents and falling back to
 * resolving locally.
 * <p>
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class CommandTranslate {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CommandTranslate.class.getSimpleName();

    public static final long CLIPBOARD_DELAY = 175L;

    private long then;

    /**
     * Resolve the translation request and return the {@link Outcome}
     *
     * @param ctx       the application context
     * @param voiceData the array list of voice data
     * @param sl        the {@link SupportedLanguage}
     * @param cr        the {@link CommandRequest}
     * @return the created {@link Outcome}
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl, @NonNull final CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        then = System.nanoTime();

        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }

            switch (SPH.getDefaultTranslationProvider(ctx)) {

                case TranslationProvider.TRANSLATION_PROVIDER_BING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TRANSLATION_PROVIDER_BING");
                    }
                    return returnOutcome(new BingTranslate(ctx, sl, cr).getResponse());
                case TranslationProvider.TRANSLATION_PROVIDER_GOOGLE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TRANSLATION_PROVIDER_GOOGLE");
                    }
                    return returnOutcome(new GoogleTranslate(ctx, sl, cr).getResponse());
                case TranslationProvider.TRANSLATION_PROVIDER_UNKNOWN:
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "TRANSLATION_PROVIDER_UNKNOWN");
                    }
                    return returnOutcome(new BingTranslate(ctx, sl, cr).getResponse());
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }

            return returnOutcome(new CommandTranslateLocal().getResponse(ctx, voiceData, sl, cr));
        }
    }

    /**
     * A single point of return to check the elapsed time in debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull final Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}

