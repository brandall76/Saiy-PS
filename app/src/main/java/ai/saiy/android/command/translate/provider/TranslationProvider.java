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

package ai.saiy.android.command.translate.provider;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 19/04/2016.
 */
public class TranslationProvider {

    public static final int TRANSLATION_PROVIDER_UNKNOWN = 0;
    public static final int TRANSLATION_PROVIDER_BING = 1;
    public static final int TRANSLATION_PROVIDER_GOOGLE = 2;

    private static final int COMMAND_TRANSLATE_VERBOSE_LIMIT = 3;

    /**
     * Check to see if the completed translation command needs verbose explanation.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     */
    public static boolean shouldAction(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        if (SPH.getTranslateCommandVerbose(ctx) <= COMMAND_TRANSLATE_VERBOSE_LIMIT) {
            SPH.incrementTranslateCommandVerbose(ctx);

            final Bundle bundle = new Bundle();
            bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
            bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
            bundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_IGNORE);
            bundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityResponse.getClipboardSpell(ctx, sl));
            bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(ctx).toString());
            new LocalRequest(ctx, bundle).execute();
            return true;
        }

        return false;
    }
}
