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

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.command.clipboard.ClipboardHelper;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.translate.CommandTranslateValues;
import ai.saiy.android.command.translate.Translate;
import ai.saiy.android.command.translate.provider.bing.TranslationLanguageBing;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.processing.EntangledPair;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.processing.Position;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

import static ai.saiy.android.command.translate.CommandTranslate.CLIPBOARD_DELAY;

/**
 * Helper Class to resolve elements of a translation request using Google Translate
 * <p>
 * Created by benrandall76@gmail.com on 19/04/2016.
 */
public class GoogleTranslate {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = GoogleTranslate.class.getSimpleName();

    private static final String B_START = ".*\\b";
    private static final String B_END = "\\b.*";

    private static final int MAX_TRANSLATE_LENGTH = 2000;

    private final Context mContext;
    private final SupportedLanguage sl;
    private final CommandRequest cr;
    private final Outcome outcome;

    private static Pattern pGERMAN;
    private static Pattern pFRENCH;
    private static Pattern pITALIAN;
    private static Pattern pPOLISH;
    private static Pattern pSPANISH;
    private static Pattern pROMANIAN;
    private static Pattern pENGLISH;
    private static Pattern pARABIC;
    private static Pattern pBULGARIAN;
    private static Pattern pCATALAN;
    private static Pattern pCHINESE_S;
    private static Pattern pCHINESE_T;
    private static Pattern pCHINESE;
    private static Pattern pDANISH;
    private static Pattern pDUTCH;
    private static Pattern pESTONIAN;
    private static Pattern pFINNISH;
    private static Pattern pGREEK;
    private static Pattern pHEBREW;
    private static Pattern pHINDI;
    private static Pattern pHUNGARIAN;
    private static Pattern pINDONESIAN;
    private static Pattern pJAPANESE;
    private static Pattern pKOREAN;
    private static Pattern pLATVIAN;
    private static Pattern pLITHUANIAN;
    private static Pattern pNORWEGIAN;
    private static Pattern pPORTUGUESE;
    private static Pattern pPORTUGUESE_BRAZILIAN;
    private static Pattern pBRAZILIAN;
    private static Pattern pRUSSIAN;
    private static Pattern pSWEDISH;
    private static Pattern pTHAI;
    private static Pattern pTURKISH;
    private static Pattern pUKRAINIAN;
    private static Pattern pVIETNAMESE;
    private static Pattern pMALTESE;
    private static Pattern pPERSIAN;
    private static Pattern pSLOVAK;
    private static Pattern pSLOVENIAN;
    private static Pattern pWELSH;
    private static Pattern pCROATIAN;
    private static Pattern pCZECH;
    private static Pattern pCZECHOSLOVAKIAN;
    private static Pattern pSWAHILI;
    private static Pattern pKLINGON;
    private static Pattern pMALAY;
    private static Pattern pMALAYSIAN;
    private static Pattern pSERBIAN;
    private static Pattern pURDU;
    private static Pattern pMANDARIN;
    private static Pattern pCANTONESE;
    private static Pattern pTranslate;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage}
     * @param cr       the {@link CommandRequest}
     */
    public GoogleTranslate(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
                           @NonNull final CommandRequest cr) {
        this.mContext = mContext;
        this.sl = sl;
        this.cr = cr;

        outcome = new Outcome();
        outcome.setTTSLocale(cr.getTTSLocale(mContext));

    }

    /**
     * Resolve the translation request and return the {@link Outcome}
     *
     * @return the created {@link Outcome}
     */
    public Outcome getResponse() {

        final CommandTranslateValues ctv = (CommandTranslateValues) cr.getVariableData();
        final TranslationLanguageGoogle language = resolveLanguage(ctv.getLanguage());

        if (language != TranslationLanguageGoogle.AUTO_DETECT) {

            String translationRequest = ctv.getText();

            if (DEBUG) {
                MyLog.d(CLS_NAME, "language: " + language.name());
                MyLog.d(CLS_NAME, "request: " + translationRequest);
            }

            if (ClipboardHelper.isClipboard(mContext, translationRequest)) {

                try {
                    Thread.sleep(CLIPBOARD_DELAY);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "InterruptedException");
                        e.printStackTrace();
                    }
                }

                final Pair<Boolean, String> clipboardPair = ClipboardHelper.getClipboardContentPair(mContext, sl);

                if (clipboardPair.first) {
                    translationRequest = clipboardPair.second;
                } else {
                    outcome.setUtterance(clipboardPair.second);
                    outcome.setOutcome(Outcome.FAILURE);
                    return outcome;
                }
            }

            if (!tooLong(translationRequest)) {

                final Pair<Boolean, String> translationResult = execute(mContext, language, translationRequest);

                if (translationResult.first) {
                    outcome.setUtterance(translationResult.second);
                    outcome.setOutcome(Outcome.SUCCESS);

                    final EntangledPair entangledPair = new EntangledPair(Position.TOAST_LONG, CC.COMMAND_TRANSLATE);
                    entangledPair.setToastContent(translationResult.second);
                    outcome.setEntangledPair(entangledPair);
                    final Qubit qubit = new Qubit();
                    qubit.setTranslatedText(translationResult.second);
                    outcome.setQubit(qubit);
                    outcome.setTTSLocale(UtilsLocale.stringToLocale(language.getLanguage()));
                } else {
                    outcome.setUtterance(mContext.getString(ai.saiy.android.R.string.error_translate,
                            PersonalityHelper.getUserNameOrNot(mContext)));
                    outcome.setOutcome(Outcome.FAILURE);
                    return outcome;
                }

            } else {
                outcome.setUtterance(mContext.getString(ai.saiy.android.R.string.error_translate_length,
                        PersonalityHelper.getUserNameOrNot(mContext)));
                outcome.setOutcome(Outcome.FAILURE);
                return outcome;
            }
        } else {
            outcome.setUtterance(mContext.getString(ai.saiy.android.R.string.error_translate_unsupported,
                    PersonalityHelper.getUserNameOrNot(mContext)));
            outcome.setOutcome(Outcome.FAILURE);
            return outcome;
        }

        return outcome;
    }

    /**
     * Execute the translation request
     *
     * @param ctx               the application context
     * @param language          the {@link TranslationLanguageBing}
     * @param translationString the content to translate
     * @return the translation result {@link Pair} with the first parameter denoting success
     */
    public static Pair<Boolean, String> execute(@NonNull final Context ctx,
                                                @NonNull final TranslationLanguageGoogle language,
                                                @NonNull final String translationString) {
        return new GoogleTranslateAPI().execute(ctx, translationString, language);
    }

    private static void initStrings(@NonNull final SaiyResources sr) {

        pGERMAN = Pattern.compile(B_START + sr.getString(R.string.german) + B_END);
        pFRENCH = Pattern.compile(B_START + sr.getString(R.string.french) + B_END);
        pITALIAN = Pattern.compile(B_START + sr.getString(R.string.italian) + B_END);
        pPOLISH = Pattern.compile(B_START + sr.getString(R.string.polish) + B_END);
        pSPANISH = Pattern.compile(B_START + sr.getString(R.string.spanish) + B_END);
        pROMANIAN = Pattern.compile(B_START + sr.getString(R.string.romanian) + B_END);
        pENGLISH = Pattern.compile(B_START + sr.getString(R.string.english) + B_END);
        pARABIC = Pattern.compile(B_START + sr.getString(R.string.arabic) + B_END);
        pBULGARIAN = Pattern.compile(B_START + sr.getString(R.string.bulgarian) + B_END);
        pCATALAN = Pattern.compile(B_START + sr.getString(R.string.catalan) + B_END);
        pCHINESE_S = Pattern.compile(B_START + sr.getString(R.string.chinese_simplified) + B_END);
        pCHINESE_T = Pattern.compile(B_START + sr.getString(R.string.chinese_traditional) + B_END);
        pCHINESE = Pattern.compile(B_START + sr.getString(R.string.chinese) + B_END);
        pDANISH = Pattern.compile(B_START + sr.getString(R.string.danish) + B_END);
        pDUTCH = Pattern.compile(B_START + sr.getString(R.string.dutch) + B_END);
        pESTONIAN = Pattern.compile(B_START + sr.getString(R.string.estonian) + B_END);
        pFINNISH = Pattern.compile(B_START + sr.getString(R.string.finnish) + B_END);
        pGREEK = Pattern.compile(B_START + sr.getString(R.string.greek) + B_END);
        pHEBREW = Pattern.compile(B_START + sr.getString(R.string.hebrew) + B_END);
        pHINDI = Pattern.compile(B_START + sr.getString(R.string.hindi) + B_END);
        pHUNGARIAN = Pattern.compile(B_START + sr.getString(R.string.hungarian) + B_END);
        pINDONESIAN = Pattern.compile(B_START + sr.getString(R.string.indonesian) + B_END);
        pJAPANESE = Pattern.compile(B_START + sr.getString(R.string.japanese) + B_END);
        pKOREAN = Pattern.compile(B_START + sr.getString(R.string.korean) + B_END);
        pLATVIAN = Pattern.compile(B_START + sr.getString(R.string.latvian) + B_END);
        pLITHUANIAN = Pattern.compile(B_START + sr.getString(R.string.lithuanian) + B_END);
        pNORWEGIAN = Pattern.compile(B_START + sr.getString(R.string.norwegian) + B_END);
        pPORTUGUESE = Pattern.compile(B_START + sr.getString(R.string.portuguese) + B_END);
        pPORTUGUESE_BRAZILIAN = Pattern.compile(B_START + sr.getString(R.string.portuguese_brazilian) + B_END);
        pBRAZILIAN = Pattern.compile(B_START + sr.getString(R.string.brazilian) + B_END);
        pRUSSIAN = Pattern.compile(B_START + sr.getString(R.string.russian) + B_END);
        pSWEDISH = Pattern.compile(B_START + sr.getString(R.string.swedish) + B_END);
        pTHAI = Pattern.compile(B_START + sr.getString(R.string.thai) + B_END);
        pTURKISH = Pattern.compile(B_START + sr.getString(R.string.turkish) + B_END);
        pUKRAINIAN = Pattern.compile(B_START + sr.getString(R.string.ukrainian) + B_END);
        pVIETNAMESE = Pattern.compile(B_START + sr.getString(R.string.vietnamese) + B_END);
        pMALTESE = Pattern.compile(B_START + sr.getString(R.string.maltese) + B_END);
        pPERSIAN = Pattern.compile(B_START + sr.getString(R.string.persian) + B_END);
        pSLOVAK = Pattern.compile(B_START + sr.getString(R.string.slovak) + B_END);
        pSLOVENIAN = Pattern.compile(B_START + sr.getString(R.string.slovenian) + B_END);
        pWELSH = Pattern.compile(B_START + sr.getString(R.string.welsh) + B_END);
        pCROATIAN = Pattern.compile(B_START + sr.getString(R.string.croatian) + B_END);
        pCZECH = Pattern.compile(B_START + sr.getString(R.string.czech) + B_END);
        pCZECHOSLOVAKIAN = Pattern.compile(B_START + sr.getString(R.string.czechoslovakian) + B_END);
        pSWAHILI = Pattern.compile(B_START + sr.getString(R.string.swahili) + B_END);
        pKLINGON = Pattern.compile(B_START + sr.getString(R.string.klingon) + B_END);
        pMALAY = Pattern.compile(B_START + sr.getString(R.string.malay) + B_END);
        pMALAYSIAN = Pattern.compile(B_START + sr.getString(R.string.malaysian) + B_END);
        pSERBIAN = Pattern.compile(B_START + sr.getString(R.string.serbian) + B_END);
        pURDU = Pattern.compile(B_START + sr.getString(R.string.urdu) + B_END);
        pMANDARIN = Pattern.compile(B_START + sr.getString(R.string.mandarin) + B_END);
        pCANTONESE = Pattern.compile(B_START + sr.getString(R.string.cantonese) + B_END);
        pTranslate = Pattern.compile(B_START + sr.getString(R.string.translate_) + B_END);
    }

    /**
     * Extract the required translation {@link TranslationLanguageGoogle} and translation string
     *
     * @param ctx       the application context
     * @param voiceData the utterance to which the extraction will take place
     * @param sl        the {@link SupportedLanguage}
     * @return a {@link Pair} with the first parameter denoting success
     */
    public static Pair<TranslationLanguageGoogle, String> extract(@NonNull final Context ctx,
                                                                  @NonNull final ArrayList<String> voiceData,
                                                                  @NonNull final SupportedLanguage sl) {
        final long then = System.nanoTime();

        TranslationLanguageGoogle language = null;
        String toTranslate = null;

        if (pTranslate == null) {
            final SaiyResources sr = new SaiyResources(ctx, sl);
            initStrings(sr);
            sr.reset();
        }

        final Locale loc = sl.getLocale();
        Pair<TranslationLanguageGoogle, String> translationPair;

        int size = voiceData.size();
        String vdLower;
        for (int i = 0; i < size; i++) {
            vdLower = voiceData.get(i).toLowerCase(loc).trim();
            if (DEBUG) {
                MyLog.v(CLS_NAME, "vdLower: " + vdLower);
            }

            if (pTranslate.matcher(vdLower).matches()) {

                translationPair = getTranslationPair(ctx, vdLower, sl);
                language = translationPair.first;
                toTranslate = translationPair.second;

                if (language != null && UtilsString.notNaked(toTranslate)) {
                    break;
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return new Pair<>(language, toTranslate);
    }

    private static Pair<TranslationLanguageGoogle, String> getTranslationPair(@NonNull final Context ctx,
                                                                              @NonNull final String vdLower,
                                                                              @NonNull final SupportedLanguage sl) {

        TranslationLanguageGoogle language = null;
        String toTranslate = null;

        if (pGERMAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.GERMAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.german));
        } else if (pMANDARIN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.MANDARIN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.mandarin));
        } else if (pCANTONESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CANTONESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.cantonese));
        } else if (pURDU.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.URDU;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.urdu));
        } else if (pSERBIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SERBIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.serbian));
        } else if (pMALAYSIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.MALAYSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.malaysian));
        } else if (pMALAY.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.MALAY;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.malay));
        } else if (pKLINGON.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.KLINGON;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.klingon));
        } else if (pSWAHILI.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SWAHILI;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.swahili));
        } else if (pCZECH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CZECH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.czech));
        } else if (pCZECHOSLOVAKIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CZECHOSLOVAKIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.czechoslovakian));
        } else if (pCROATIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CROATIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.croatian));
        } else if (pWELSH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.WELSH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.welsh));
        } else if (pSLOVENIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SLOVENIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.slovenian));
        } else if (pSLOVAK.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SLOVAK;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.slovak));
        } else if (pPERSIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.PERSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.persian));
        } else if (pMALTESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.MALTESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.maltese));
        } else if (pFRENCH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.FRENCH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.french));
        } else if (pITALIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.ITALIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.italian));
        } else if (pSPANISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SPANISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.spanish));
        } else if (pPOLISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.POLISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.polish));
        } else if (pROMANIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.ROMANIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.romanian));
        } else if (pENGLISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.ENGLISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.english));
        } else if (pARABIC.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.ARABIC;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.arabic));
        } else if (pBULGARIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.BULGARIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.bulgarian));
        } else if (pCATALAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CATALAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.catalan));
        } else if (pCHINESE_S.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CHINESE_SIMPLIFIED;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.chinese_simplified));
        } else if (pCHINESE_T.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CHINESE_TRADITIONAL;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.chinese_traditional));
        } else if (pCHINESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.CHINESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.chinese));
        } else if (pDANISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.DANISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.danish));
        } else if (pDUTCH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.DUTCH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.dutch));
        } else if (pESTONIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.ESTONIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.estonian));
        } else if (pFINNISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.FINNISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.finnish));
        } else if (pGREEK.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.GREEK;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.greek));
        } else if (pHEBREW.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.HEBREW;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.hebrew));
        } else if (pHINDI.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.HINDI;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.hindi));
        } else if (pHUNGARIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.HUNGARIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.hungarian));
        } else if (pINDONESIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.INDONESIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.indonesian));
        } else if (pJAPANESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.JAPANESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.japanese));
        } else if (pKOREAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.KOREAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.korean));
        } else if (pLATVIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.LATVIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.latvian));
        } else if (pLITHUANIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.LITHUANIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.lithuanian));
        } else if (pNORWEGIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.NORWEGIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.norwegian));
        } else if (pPORTUGUESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.PORTUGUESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.portuguese));
        } else if (pPORTUGUESE_BRAZILIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.PORTUGUESE_BRAZILIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.portuguese_brazilian));
        } else if (pBRAZILIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.BRAZILIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.brazilian));
        } else if (pRUSSIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.RUSSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.russian));
        } else if (pSWEDISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.SWEDISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.swedish));
        } else if (pTHAI.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.THAI;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.thai));
        } else if (pTURKISH.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.TURKISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.turkish));
        } else if (pUKRAINIAN.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.UKRAINIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.ukrainian));
        } else if (pVIETNAMESE.matcher(vdLower).matches()) {
            language = TranslationLanguageGoogle.VIETNAMESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vdLower, ctx.getString(R.string.vietnamese));
        }

        return new Pair<>(language, toTranslate);
    }

    /**
     * Resolve the required translation language from the voice data
     *
     * @param vd the voice data string
     * @return the matching {@link TranslationLanguageGoogle} or {@link TranslationLanguageGoogle#AUTO_DETECT}
     */
    private TranslationLanguageGoogle resolveLanguage(@NonNull final String vd) {

        if (vd.contains(mContext.getString(R.string.german))) {
            return TranslationLanguageGoogle.GERMAN;
        } else if (vd.contains(mContext.getString(R.string.french))) {
            return TranslationLanguageGoogle.FRENCH;
        } else if (vd.contains(mContext.getString(R.string.italian))) {
            return TranslationLanguageGoogle.ITALIAN;
        } else if (vd.contains(mContext.getString(R.string.spanish))) {
            return TranslationLanguageGoogle.SPANISH;
        } else if (vd.contains(mContext.getString(R.string.polish))) {
            return TranslationLanguageGoogle.POLISH;
        } else if (vd.contains(mContext.getString(R.string.romanian))) {
            return TranslationLanguageGoogle.ROMANIAN;
        } else if (vd.contains(mContext.getString(R.string.english))) {
            return TranslationLanguageGoogle.ENGLISH;
        } else if (vd.contains(mContext.getString(R.string.arabic))) {
            return TranslationLanguageGoogle.ARABIC;
        } else if (vd.contains(mContext.getString(R.string.bulgarian))) {
            return TranslationLanguageGoogle.BULGARIAN;
        } else if (vd.contains(mContext.getString(R.string.catalan))) {
            return TranslationLanguageGoogle.CATALAN;
        } else if (vd.contains(mContext.getString(R.string.chinese_simplified))) {
            return TranslationLanguageGoogle.CHINESE_SIMPLIFIED;
        } else if (vd.contains(mContext.getString(R.string.chinese_traditional))) {
            return TranslationLanguageGoogle.CHINESE_TRADITIONAL;
        } else if (vd.contains(mContext.getString(R.string.chinese))) {
            return TranslationLanguageGoogle.CHINESE;
        } else if (vd.contains(mContext.getString(R.string.mandarin))) {
            return TranslationLanguageGoogle.MANDARIN;
        } else if (vd.contains(mContext.getString(R.string.cantonese))) {
            return TranslationLanguageGoogle.CANTONESE;
        } else if (vd.contains(mContext.getString(R.string.danish))) {
            return TranslationLanguageGoogle.DANISH;
        } else if (vd.contains(mContext.getString(R.string.dutch))) {
            return TranslationLanguageGoogle.DUTCH;
        } else if (vd.contains(mContext.getString(R.string.estonian))) {
            return TranslationLanguageGoogle.ESTONIAN;
        } else if (vd.contains(mContext.getString(R.string.finnish))) {
            return TranslationLanguageGoogle.FINNISH;
        } else if (vd.contains(mContext.getString(R.string.greek))) {
            return TranslationLanguageGoogle.GREEK;
        } else if (vd.contains(mContext.getString(R.string.hebrew))) {
            return TranslationLanguageGoogle.HEBREW;
        } else if (vd.contains(mContext.getString(R.string.hindi))) {
            return TranslationLanguageGoogle.HINDI;
        } else if (vd.contains(mContext.getString(R.string.hungarian))) {
            return TranslationLanguageGoogle.HUNGARIAN;
        } else if (vd.contains(mContext.getString(R.string.indonesian))) {
            return TranslationLanguageGoogle.INDONESIAN;
        } else if (vd.contains(mContext.getString(R.string.japanese))) {
            return TranslationLanguageGoogle.JAPANESE;
        } else if (vd.contains(mContext.getString(R.string.korean))) {
            return TranslationLanguageGoogle.KOREAN;
        } else if (vd.contains(mContext.getString(R.string.latvian))) {
            return TranslationLanguageGoogle.LATVIAN;
        } else if (vd.contains(mContext.getString(R.string.lithuanian))) {
            return TranslationLanguageGoogle.LITHUANIAN;
        } else if (vd.contains(mContext.getString(R.string.norwegian))) {
            return TranslationLanguageGoogle.NORWEGIAN;
        } else if (vd.contains(mContext.getString(R.string.portuguese))) {
            return TranslationLanguageGoogle.PORTUGUESE;
        } else if (vd.contains(mContext.getString(R.string.portuguese_brazilian))) {
            return TranslationLanguageGoogle.PORTUGUESE_BRAZILIAN;
        } else if (vd.contains(mContext.getString(R.string.brazilian))) {
            return TranslationLanguageGoogle.BRAZILIAN;
        } else if (vd.contains(mContext.getString(R.string.russian))) {
            return TranslationLanguageGoogle.RUSSIAN;
        } else if (vd.contains(mContext.getString(R.string.slovenian))) {
            return TranslationLanguageGoogle.SLOVENIAN;
        } else if (vd.contains(mContext.getString(R.string.swedish))) {
            return TranslationLanguageGoogle.SWEDISH;
        } else if (vd.contains(mContext.getString(R.string.thai))) {
            return TranslationLanguageGoogle.THAI;
        } else if (vd.contains(mContext.getString(R.string.turkish))) {
            return TranslationLanguageGoogle.TURKISH;
        } else if (vd.contains(mContext.getString(R.string.ukrainian))) {
            return TranslationLanguageGoogle.UKRAINIAN;
        } else if (vd.contains(mContext.getString(R.string.vietnamese))) {
            return TranslationLanguageGoogle.VIETNAMESE;
        } else if (vd.contains(mContext.getString(R.string.urdu))) {
            return TranslationLanguageGoogle.URDU;
        } else if (vd.contains(mContext.getString(R.string.serbian))) {
            return TranslationLanguageGoogle.SERBIAN;
        } else if (vd.contains(mContext.getString(R.string.malaysian))) {
            return TranslationLanguageGoogle.MALAYSIAN;
        } else if (vd.contains(mContext.getString(R.string.malay))) {
            return TranslationLanguageGoogle.MALAY;
        } else if (vd.contains(mContext.getString(R.string.klingon))) {
            return TranslationLanguageGoogle.KLINGON;
        } else if (vd.contains(mContext.getString(R.string.swahili))) {
            return TranslationLanguageGoogle.SWAHILI;
        } else if (vd.contains(mContext.getString(R.string.czech))) {
            return TranslationLanguageGoogle.CZECH;
        } else if (vd.contains(mContext.getString(R.string.czechoslovakian))) {
            return TranslationLanguageGoogle.CZECHOSLOVAKIAN;
        } else if (vd.contains(mContext.getString(R.string.croatian))) {
            return TranslationLanguageGoogle.CROATIAN;
        } else if (vd.contains(mContext.getString(R.string.welsh))) {
            return TranslationLanguageGoogle.WELSH;
        } else if (vd.contains(mContext.getString(R.string.slovak))) {
            return TranslationLanguageGoogle.SLOVAK;
        } else if (vd.contains(mContext.getString(R.string.persian))) {
            return TranslationLanguageGoogle.PERSIAN;
        } else if (vd.contains(mContext.getString(R.string.maltese))) {
            return TranslationLanguageGoogle.MALTESE;
        } else {
            return TranslationLanguageGoogle.AUTO_DETECT;
        }
    }

    /**
     * Check if the translation request exceeds the {@link #MAX_TRANSLATE_LENGTH}
     *
     * @param toTranslate the string to translate
     * @return true if the string is too long. False otherwise
     */
    public static boolean tooLong(@NonNull final String toTranslate) {
        return toTranslate.length() > MAX_TRANSLATE_LENGTH;
    }
}
