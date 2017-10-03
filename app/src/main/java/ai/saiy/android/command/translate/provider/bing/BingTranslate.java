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

package ai.saiy.android.command.translate.provider.bing;

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
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.processing.EntangledPair;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.processing.Position;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

import static ai.saiy.android.command.translate.CommandTranslate.CLIPBOARD_DELAY;

/**
 * Helper Class to resolve elements of a translation request using Bing Translator
 * <p>
 * Created by benrandall76@gmail.com on 17/04/2016.
 */
public class BingTranslate {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BingTranslate.class.getSimpleName();

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
    private static Pattern pSERBIAN_CYRILLIC;
    private static Pattern pSERBIAN_LATIN;
    private static Pattern pHAITIAN_CREOLE;
    private static Pattern pHAITIAN;
    private static Pattern pKISWAHILI;
    private static Pattern pSLOVAKIAN;
    private static Pattern pBOSNIAN;
    private static Pattern pTranslate;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage}
     * @param cr       the {@link CommandRequest}
     */
    public BingTranslate(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
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
        final TranslationLanguageBing language = resolveLanguage(ctv.getLanguage());

        if (language != TranslationLanguageBing.AUTO_DETECT) {

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
                    outcome.setTTSLocale(language.getLocale());
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
                                                @NonNull final TranslationLanguageBing language,
                                                @NonNull final String translationString) {

        if (BingCredentials.isTokenValid(ctx)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "execute: token valid");
            }
            return new BingTranslateAPI().execute(ctx, SPH.getBingToken(ctx), translationString,
                    TranslationLanguageBing.AUTO_DETECT, language);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "execute: token invalid");
            }
            if (new BingOAuth().execute(ctx, true)) {
                return new BingTranslateAPI().execute(ctx, SPH.getBingToken(ctx), translationString,
                        TranslationLanguageBing.AUTO_DETECT, language);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "execute: refreshing token failed");
                }
                return new Pair<>(false, null);
            }
        }
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
        pSERBIAN_CYRILLIC = Pattern.compile(B_START + sr.getString(R.string.serbian_cyrillic) + B_END);
        pSERBIAN_LATIN = Pattern.compile(B_START + sr.getString(R.string.serbian_latin) + B_END);
        pHAITIAN_CREOLE = Pattern.compile(B_START + sr.getString(R.string.haitian_creole) + B_END);
        pHAITIAN = Pattern.compile(B_START + sr.getString(R.string.haitian) + B_END);
        pKISWAHILI = Pattern.compile(B_START + sr.getString(R.string.kiswahili) + B_END);
        pBOSNIAN = Pattern.compile(B_START + sr.getString(R.string.bosnian) + B_END);
        pSLOVAKIAN = Pattern.compile(B_START + sr.getString(R.string.slovakian) + B_END);
    }


    /**
     * Extract the required translation {@link TranslationLanguageBing} and translation string
     *
     * @param ctx       the application context
     * @param voiceData the utterance to which the extraction will take place
     * @param sl        the {@link SupportedLanguage}
     * @return a {@link Pair} with the first parameter denoting success
     */
    public static Pair<TranslationLanguageBing, String> extract(@NonNull final Context ctx,
                                                                @NonNull final ArrayList<String> voiceData,
                                                                @NonNull final SupportedLanguage sl) {
        final long then = System.nanoTime();

        TranslationLanguageBing language = null;
        String toTranslate = null;

        if (pTranslate == null) {
            final SaiyResources sr = new SaiyResources(ctx, sl);
            initStrings(sr);
            sr.reset();
        }

        final Locale loc = sl.getLocale();
        Pair<TranslationLanguageBing, String> translationPair;

        int size = voiceData.size();

        String vd;
        for (int i = 0; i < size; i++) {
            vd = voiceData.get(i).toLowerCase(loc).trim();
            if (DEBUG) {
                MyLog.v(CLS_NAME, "vd: " + vd);
            }

            if (pTranslate.matcher(vd).matches()) {

                translationPair = getTranslationPair(ctx, vd, sl);
                language = translationPair.first;
                toTranslate = translationPair.second;

                if (language != null && UtilsString.notNaked(toTranslate)) {
                    break;
                }

            }
        }

        if (DEBUG) {
            MyLog.getElapsed(BingTranslate.class.getSimpleName(), then);
        }

        return new Pair<>(language, toTranslate);
    }

    private static Pair<TranslationLanguageBing, String> getTranslationPair(@NonNull final Context ctx,
                                                                            @NonNull final String vd,
                                                                            @NonNull final SupportedLanguage sl) {

        TranslationLanguageBing language = null;
        String toTranslate = null;

        if (pGERMAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.GERMAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.german));
        } else if (pMANDARIN.matcher(vd).matches()) {
            language = TranslationLanguageBing.MANDARIN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.mandarin));
        } else if (pCANTONESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.CANTONESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.cantonese));
        } else if (pURDU.matcher(vd).matches()) {
            language = TranslationLanguageBing.URDU;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.urdu));
        } else if (pSERBIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.SERBIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.serbian));
        } else if (pSERBIAN_LATIN.matcher(vd).matches()) {
            language = TranslationLanguageBing.SERBIAN_LATIN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.serbian_latin));
        } else if (pSERBIAN_CYRILLIC.matcher(vd).matches()) {
            language = TranslationLanguageBing.SERBIAN_CYRILLIC;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.serbian_cyrillic));
        } else if (pMALAYSIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.MALAYSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.malaysian));
        } else if (pMALAY.matcher(vd).matches()) {
            language = TranslationLanguageBing.MALAY;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.malay));
        } else if (pKLINGON.matcher(vd).matches()) {
            language = TranslationLanguageBing.KLINGON;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.klingon));
        } else if (pSWAHILI.matcher(vd).matches()) {
            language = TranslationLanguageBing.SWAHILI;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.swahili));
        } else if (pKISWAHILI.matcher(vd).matches()) {
            language = TranslationLanguageBing.KISWAHILI;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.kiswahili));
        } else if (pHAITIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.HAITIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.haitian));
        } else if (pHAITIAN_CREOLE.matcher(vd).matches()) {
            language = TranslationLanguageBing.HAITIAN_CREOLE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.haitian_creole));
        } else if (pCZECH.matcher(vd).matches()) {
            language = TranslationLanguageBing.CZECH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.czech));
        } else if (pCZECHOSLOVAKIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.CZECHOSLOVAKIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.czechoslovakian));
        } else if (pCROATIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.CROATIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.croatian));
        } else if (pBOSNIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.BOSNIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.bosnian));
        } else if (pWELSH.matcher(vd).matches()) {
            language = TranslationLanguageBing.WELSH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.welsh));
        } else if (pSLOVENIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.SLOVENIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.slovenian));
        } else if (pSLOVAKIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.SLOVAKIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.slovakian));
        } else if (pSLOVAK.matcher(vd).matches()) {
            language = TranslationLanguageBing.SLOVAK;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.slovak));
        } else if (pPERSIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.PERSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.persian));
        } else if (pMALTESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.MALTESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.maltese));
        } else if (pFRENCH.matcher(vd).matches()) {
            language = TranslationLanguageBing.FRENCH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.french));
        } else if (pITALIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.ITALIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.italian));
        } else if (pSPANISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.SPANISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.spanish));
        } else if (pPOLISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.POLISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.polish));
        } else if (pROMANIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.ROMANIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.romanian));
        } else if (pENGLISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.ENGLISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.english));
        } else if (pARABIC.matcher(vd).matches()) {
            language = TranslationLanguageBing.ARABIC;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.arabic));
        } else if (pBULGARIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.BULGARIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.bulgarian));
        } else if (pCATALAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.CATALAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.catalan));
        } else if (pCHINESE_S.matcher(vd).matches()) {
            language = TranslationLanguageBing.CHINESE_SIMPLIFIED;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.chinese_simplified));
        } else if (pCHINESE_T.matcher(vd).matches()) {
            language = TranslationLanguageBing.CHINESE_TRADITIONAL;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.chinese_traditional));
        } else if (pCHINESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.CHINESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.chinese));
        } else if (pDANISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.DANISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.danish));
        } else if (pDUTCH.matcher(vd).matches()) {
            language = TranslationLanguageBing.DUTCH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.dutch));
        } else if (pESTONIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.ESTONIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.estonian));
        } else if (pFINNISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.FINNISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.finnish));
        } else if (pGREEK.matcher(vd).matches()) {
            language = TranslationLanguageBing.GREEK;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.greek));
        } else if (pHEBREW.matcher(vd).matches()) {
            language = TranslationLanguageBing.HEBREW;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.hebrew));
        } else if (pHINDI.matcher(vd).matches()) {
            language = TranslationLanguageBing.HINDI;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.hindi));
        } else if (pHUNGARIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.HUNGARIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.hungarian));
        } else if (pINDONESIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.INDONESIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.indonesian));
        } else if (pJAPANESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.JAPANESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.japanese));
        } else if (pKOREAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.KOREAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.korean));
        } else if (pLATVIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.LATVIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.latvian));
        } else if (pLITHUANIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.LITHUANIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.lithuanian));
        } else if (pNORWEGIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.NORWEGIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.norwegian));
        } else if (pPORTUGUESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.PORTUGUESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.portuguese));
        } else if (pPORTUGUESE_BRAZILIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.PORTUGUESE_BRAZILIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.portuguese_brazilian));
        } else if (pBRAZILIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.BRAZILIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.brazilian));
        } else if (pRUSSIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.RUSSIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.russian));
        } else if (pSWEDISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.SWEDISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.swedish));
        } else if (pTHAI.matcher(vd).matches()) {
            language = TranslationLanguageBing.THAI;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.thai));
        } else if (pTURKISH.matcher(vd).matches()) {
            language = TranslationLanguageBing.TURKISH;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.turkish));
        } else if (pUKRAINIAN.matcher(vd).matches()) {
            language = TranslationLanguageBing.UKRAINIAN;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.ukrainian));
        } else if (pVIETNAMESE.matcher(vd).matches()) {
            language = TranslationLanguageBing.VIETNAMESE;
            toTranslate = new Translate(sl).resolveBody(ctx, vd, ctx.getString(ai.saiy.android.R.string.vietnamese));
        }

        return new Pair<>(language, toTranslate);
    }

    /**
     * Resolve the required translation language from the voice data
     *
     * @param vd the voice data string
     * @return the matching {@link TranslationLanguageBing} or {@link TranslationLanguageBing#AUTO_DETECT}
     */
    private TranslationLanguageBing resolveLanguage(@NonNull final String vd) {

        if (vd.contains(mContext.getString(ai.saiy.android.R.string.german))) {
            return TranslationLanguageBing.GERMAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.french))) {
            return TranslationLanguageBing.FRENCH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.italian))) {
            return TranslationLanguageBing.ITALIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.spanish))) {
            return TranslationLanguageBing.SPANISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.polish))) {
            return TranslationLanguageBing.POLISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.romanian))) {
            return TranslationLanguageBing.ROMANIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.english))) {
            return TranslationLanguageBing.ENGLISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.arabic))) {
            return TranslationLanguageBing.ARABIC;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.bulgarian))) {
            return TranslationLanguageBing.BULGARIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.catalan))) {
            return TranslationLanguageBing.CATALAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.chinese_simplified))) {
            return TranslationLanguageBing.CHINESE_SIMPLIFIED;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.chinese_traditional))) {
            return TranslationLanguageBing.CHINESE_TRADITIONAL;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.chinese))) {
            return TranslationLanguageBing.CHINESE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.mandarin))) {
            return TranslationLanguageBing.MANDARIN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.cantonese))) {
            return TranslationLanguageBing.CANTONESE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.danish))) {
            return TranslationLanguageBing.DANISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.dutch))) {
            return TranslationLanguageBing.DUTCH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.estonian))) {
            return TranslationLanguageBing.ESTONIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.finnish))) {
            return TranslationLanguageBing.FINNISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.greek))) {
            return TranslationLanguageBing.GREEK;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.hebrew))) {
            return TranslationLanguageBing.HEBREW;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.hindi))) {
            return TranslationLanguageBing.HINDI;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.hungarian))) {
            return TranslationLanguageBing.HUNGARIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.indonesian))) {
            return TranslationLanguageBing.INDONESIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.japanese))) {
            return TranslationLanguageBing.JAPANESE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.korean))) {
            return TranslationLanguageBing.KOREAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.latvian))) {
            return TranslationLanguageBing.LATVIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.lithuanian))) {
            return TranslationLanguageBing.LITHUANIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.norwegian))) {
            return TranslationLanguageBing.NORWEGIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.portuguese))) {
            return TranslationLanguageBing.PORTUGUESE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.portuguese_brazilian))) {
            return TranslationLanguageBing.PORTUGUESE_BRAZILIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.brazilian))) {
            return TranslationLanguageBing.BRAZILIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.russian))) {
            return TranslationLanguageBing.RUSSIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.slovenian))) {
            return TranslationLanguageBing.SLOVENIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.swedish))) {
            return TranslationLanguageBing.SWEDISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.thai))) {
            return TranslationLanguageBing.THAI;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.turkish))) {
            return TranslationLanguageBing.TURKISH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.ukrainian))) {
            return TranslationLanguageBing.UKRAINIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.vietnamese))) {
            return TranslationLanguageBing.VIETNAMESE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.urdu))) {
            return TranslationLanguageBing.URDU;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.serbian))) {
            return TranslationLanguageBing.SERBIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.serbian_latin))) {
            return TranslationLanguageBing.SERBIAN_LATIN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.serbian_cyrillic))) {
            return TranslationLanguageBing.SERBIAN_CYRILLIC;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.malaysian))) {
            return TranslationLanguageBing.MALAYSIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.malay))) {
            return TranslationLanguageBing.MALAY;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.klingon))) {
            return TranslationLanguageBing.KLINGON;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.swahili))) {
            return TranslationLanguageBing.SWAHILI;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.kiswahili))) {
            return TranslationLanguageBing.KISWAHILI;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.haitian))) {
            return TranslationLanguageBing.HAITIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.haitian_creole))) {
            return TranslationLanguageBing.HAITIAN_CREOLE;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.czech))) {
            return TranslationLanguageBing.CZECH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.czechoslovakian))) {
            return TranslationLanguageBing.CZECHOSLOVAKIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.croatian))) {
            return TranslationLanguageBing.CROATIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.bosnian))) {
            return TranslationLanguageBing.BOSNIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.welsh))) {
            return TranslationLanguageBing.WELSH;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.slovakian))) {
            return TranslationLanguageBing.SLOVAKIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.slovak))) {
            return TranslationLanguageBing.SLOVAK;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.persian))) {
            return TranslationLanguageBing.PERSIAN;
        } else if (vd.contains(mContext.getString(ai.saiy.android.R.string.maltese))) {
            return TranslationLanguageBing.MALTESE;
        } else {
            return TranslationLanguageBing.AUTO_DETECT;
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
