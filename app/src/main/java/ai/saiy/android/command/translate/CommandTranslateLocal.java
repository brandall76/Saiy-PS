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
import android.util.Pair;

import java.util.ArrayList;

import ai.saiy.android.command.clipboard.ClipboardHelper;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.command.translate.provider.bing.BingTranslate;
import ai.saiy.android.command.translate.provider.bing.TranslationLanguageBing;
import ai.saiy.android.command.translate.provider.google.GoogleTranslate;
import ai.saiy.android.command.translate.provider.google.TranslationLanguageGoogle;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.processing.EntangledPair;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.processing.Position;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsLocale;

import static ai.saiy.android.command.translate.CommandTranslate.CLIPBOARD_DELAY;

/**
 * Created by benrandall76@gmail.com on 03/05/2016.
 */
public class CommandTranslateLocal {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CommandTranslate.class.getSimpleName();

    private final Outcome outcome = new Outcome();
    private final EntangledPair entangledPair = new EntangledPair(Position.TOAST_LONG, CC.COMMAND_TRANSLATE);

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

        outcome.setTTSLocale(cr.getTTSLocale(ctx));

        switch (SPH.getDefaultTranslationProvider(ctx)) {

            case TranslationProvider.TRANSLATION_PROVIDER_BING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TRANSLATION_PROVIDER_BING");
                }

                final Pair<TranslationLanguageBing, String> bingPair = BingTranslate.extract(ctx, voiceData, sl);

                if (bingPair.first != null && bingPair.second != null) {

                    String translationRequest = bingPair.second;

                    if (ClipboardHelper.isClipboard(ctx, translationRequest)) {

                        try {
                            Thread.sleep(CLIPBOARD_DELAY);
                        } catch (final InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "InterruptedException");
                                e.printStackTrace();
                            }
                        }

                        final Pair<Boolean, String> clipboardPair = ClipboardHelper.getClipboardContentPair(ctx,
                                cr.getSupportedLanguage());

                        if (clipboardPair.first) {
                            translationRequest = clipboardPair.second;
                        } else {
                            outcome.setUtterance(clipboardPair.second);
                            outcome.setOutcome(Outcome.FAILURE);
                            return outcome;
                        }
                    }

                    if (!BingTranslate.tooLong(translationRequest)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "translationRequest: " + translationRequest);
                        }

                        final Pair<Boolean, String> translationResult = BingTranslate.execute(ctx,
                                bingPair.first, translationRequest);

                        if (translationResult.first) {
                            outcome.setUtterance(translationResult.second);
                            outcome.setOutcome(Outcome.SUCCESS);
                            entangledPair.setToastContent(translationResult.second);
                            outcome.setEntangledPair(entangledPair);

                            final Qubit qubit = new Qubit();
                            qubit.setTranslatedText(translationResult.second);
                            outcome.setQubit(qubit);
                            outcome.setTTSLocale(bingPair.first.getLocale());
                        } else {
                            outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate,
                                    PersonalityHelper.getUserNameOrNot(ctx)));
                            outcome.setOutcome(Outcome.FAILURE);
                        }
                    } else {
                        outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate_length,
                                PersonalityHelper.getUserNameOrNot(ctx)));
                        outcome.setOutcome(Outcome.FAILURE);
                    }
                } else {
                    outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate_unsupported,
                            PersonalityHelper.getUserNameOrNot(ctx)));
                    outcome.setOutcome(Outcome.FAILURE);
                }

                return outcome;
            case TranslationProvider.TRANSLATION_PROVIDER_GOOGLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TRANSLATION_PROVIDER_GOOGLE");
                }

                final Pair<TranslationLanguageGoogle, String> googlePair = GoogleTranslate.extract(ctx, voiceData, sl);

                if (googlePair.first != null && googlePair.second != null) {

                    String translationRequest = googlePair.second;

                    if (ClipboardHelper.isClipboard(ctx, translationRequest)) {

                        try {
                            Thread.sleep(CLIPBOARD_DELAY);
                        } catch (final InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "InterruptedException");
                                e.printStackTrace();
                            }
                        }

                        final Pair<Boolean, String> clipboardPair = ClipboardHelper.getClipboardContentPair(ctx,
                                cr.getSupportedLanguage());

                        if (clipboardPair.first) {
                            translationRequest = clipboardPair.second;
                        } else {
                            outcome.setUtterance(clipboardPair.second);
                            outcome.setOutcome(Outcome.FAILURE);
                            return outcome;
                        }
                    }

                    if (!GoogleTranslate.tooLong(translationRequest)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "translationRequest: " + translationRequest);
                        }

                        final Pair<Boolean, String> translationResult = GoogleTranslate.execute(ctx,
                                googlePair.first, translationRequest);

                        if (translationResult.first) {
                            outcome.setUtterance(translationResult.second);
                            outcome.setOutcome(Outcome.SUCCESS);
                            entangledPair.setToastContent(translationResult.second);
                            outcome.setEntangledPair(entangledPair);
                            final Qubit qubit = new Qubit();
                            qubit.setTranslatedText(translationResult.second);
                            outcome.setQubit(qubit);
                            outcome.setTTSLocale(UtilsLocale.stringToLocale(googlePair.first.getLanguage()));
                        } else {
                            outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate,
                                    PersonalityHelper.getUserNameOrNot(ctx)));
                            outcome.setOutcome(Outcome.FAILURE);
                        }
                    } else {
                        outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate_length,
                                PersonalityHelper.getUserNameOrNot(ctx)));
                        outcome.setOutcome(Outcome.FAILURE);
                    }
                } else {
                    outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate_unsupported,
                            PersonalityHelper.getUserNameOrNot(ctx)));
                    outcome.setOutcome(Outcome.FAILURE);
                }

                return outcome;
            case TranslationProvider.TRANSLATION_PROVIDER_UNKNOWN:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TRANSLATION_PROVIDER_UNKNOWN");
                }
                outcome.setUtterance(ctx.getString(ai.saiy.android.R.string.error_translate_unsupported,
                        PersonalityHelper.getUserNameOrNot(ctx)));
                outcome.setOutcome(Outcome.FAILURE);
                return outcome;
        }
    }
}

