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

package ai.saiy.android.nlu;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.apiai.NLUAPIAI;
import ai.saiy.android.nlu.apiai.NLUAPIAIHelper;
import ai.saiy.android.nlu.bluemix.NLUBluemix;
import ai.saiy.android.nlu.microsoft.Entity;
import ai.saiy.android.nlu.microsoft.Intent;
import ai.saiy.android.nlu.microsoft.NLUMicrosoft;
import ai.saiy.android.nlu.microsoft.NLUMicrosoftHelper;
import ai.saiy.android.nlu.nuance.Concept;
import ai.saiy.android.nlu.nuance.Interpretation;
import ai.saiy.android.nlu.nuance.NLUNuance;
import ai.saiy.android.nlu.nuance.NLUNuanceHelper;
import ai.saiy.android.nlu.saiy.ContextValue;
import ai.saiy.android.nlu.saiy.NLUSaiy;
import ai.saiy.android.nlu.saiy.NLUSaiyHelper;
import ai.saiy.android.nlu.wit.NLUWit;
import ai.saiy.android.processing.Quantum;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsMap;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to validate remote NLP models and coerce them into a generic {@link CommandRequest} object
 * for {@link Quantum} to process.
 * <p>
 * Created by benrandall76@gmail.com on 31/05/2016.
 */
public class NLUCoerce {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NLUCoerce.class.getSimpleName();

    private CommandRequest commandRequest;
    private final Object nluProvider;

    private final ArrayList<String> resultsArray;
    private final float[] confidenceArray;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final Context mContext;

    /**
     * Constructor
     *
     * @param nluProvider     one of the remote NLP providers
     * @param mContext        the application context
     * @param sl              the {@link SupportedLanguage}
     * @param vrLocale        the voice recognition {@link Locale}
     * @param ttsLocale       the Text to Speech {@link Locale}
     * @param confidenceArray float array of confidence scores
     * @param resultsArray    ArrayList of recognition results
     */
    public NLUCoerce(@NonNull final Object nluProvider, @NonNull final Context mContext,
                     @NonNull final SupportedLanguage sl, @NonNull final Locale vrLocale,
                     @NonNull final Locale ttsLocale, @NonNull final float[] confidenceArray,
                     @NonNull final ArrayList<String> resultsArray) {

        this.nluProvider = nluProvider;
        this.mContext = mContext;
        this.sl = sl;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.confidenceArray = confidenceArray;
        this.resultsArray = resultsArray;

        this.commandRequest = new CommandRequest(getVRLocale(), getTTSLocale(), getSupportedLanguage());
        commandRequest.setResultsArray(getResultsArray());
        commandRequest.setConfidenceArray(getConfidenceArray());
    }

    /**
     * Coerce the NLP results into a generic {@link CommandRequest} object, validating the minimal
     * requirements for each implementation.
     */
    public void coerce() {

        if (nluProvider instanceof NLUMicrosoft) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUMicrosoft");
            }

            if (validateNLUNLUMicrosoft((NLUMicrosoft) nluProvider)) {

                for (final Intent i : ((NLUMicrosoft) nluProvider).getIntents()) {

                    if (i.getScore() > NLUMicrosoft.MIN_THRESHOLD) {

                        commandRequest.setCC(NLUConstants.intentToCC(i.getIntent()));

                        if (!commandRequest.getCC().equals(CC.COMMAND_UNKNOWN)) {
                            final NLUMicrosoftHelper microsoftHelper = new NLUMicrosoftHelper();
                            commandRequest = microsoftHelper.prepareCommand(mContext, commandRequest,
                                    getSupportedLanguage(), ((NLUMicrosoft) nluProvider).getEntities());
                            if (commandRequest.isResolved()) {
                                break;
                            }
                            break;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "coerce: COMMAND_UNKNOWN");
                            }

                            commandRequest.setCC(CC.COMMAND_UNKNOWN);
                            commandRequest.setResolved(false);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "coerce: below threshold: " + i.getScore());
                        }

                        commandRequest.setCC(CC.COMMAND_UNKNOWN);
                        commandRequest.setResolved(false);
                    }
                }

                if (!commandRequest.isResolved()) {
                    commandRequest.setCC(CC.COMMAND_UNKNOWN);
                    commandRequest.setResolved(false);
                }

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "coerce: NLUMicrosoft validation failed");
                }

                commandRequest.setCC(CC.COMMAND_UNKNOWN);
                commandRequest.setResolved(false);
            }

        } else if (nluProvider instanceof NLUNuance) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUNuance");
            }

            if (validateNLUNuance((NLUNuance) nluProvider)) {

                for (final Interpretation interpretation : ((NLUNuance) nluProvider).getInterpretations()) {

                    if (interpretation.getAction().getIntent().getConfidence() > NLUNuance.MIN_THRESHOLD) {

                        commandRequest.setCC(NLUConstants.intentToCC(
                                interpretation.getAction().getIntent().getValue()));

                        if (!commandRequest.getCC().equals(CC.COMMAND_UNKNOWN)) {
                            final NLUNuanceHelper nuanceHelper = new NLUNuanceHelper();
                            commandRequest = nuanceHelper.prepareCommand(mContext, commandRequest, getSupportedLanguage(),
                                    interpretation.getConcept());
                            if (commandRequest.isResolved()) {
                                break;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "coerce: COMMAND_UNKNOWN");
                            }

                            commandRequest.setCC(CC.COMMAND_UNKNOWN);
                            commandRequest.setResolved(false);
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "coerce: below threshold: " + interpretation.getAction()
                                    .getIntent().getConfidence());
                        }

                        commandRequest.setCC(CC.COMMAND_UNKNOWN);
                        commandRequest.setResolved(false);
                    }
                }

                if (!commandRequest.isResolved()) {
                    commandRequest.setCC(CC.COMMAND_UNKNOWN);
                    commandRequest.setResolved(false);
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "coerce: NLUNuance validation failed");
                }

                commandRequest.setCC(CC.COMMAND_UNKNOWN);
                commandRequest.setResolved(false);
            }
        } else if (nluProvider instanceof NLUAPIAI) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUAPIAI");
            }

            if (validateNLUAPIAI((NLUAPIAI) nluProvider)) {

                commandRequest.setCC(NLUConstants.intentToCC(((NLUAPIAI) nluProvider).getIntent()));

                if (!commandRequest.getCC().equals(CC.COMMAND_UNKNOWN)) {

                    final NLUAPIAIHelper apiaiHelper = new NLUAPIAIHelper();
                    commandRequest = apiaiHelper.prepareCommand(mContext, commandRequest,
                            getSupportedLanguage(), ((NLUAPIAI) nluProvider).getParameters());

                    if (!commandRequest.isResolved()) {
                        commandRequest.setCC(CC.COMMAND_UNKNOWN);
                        commandRequest.setResolved(false);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "coerce: COMMAND_UNKNOWN");
                    }
                    commandRequest.setCC(CC.COMMAND_UNKNOWN);
                    commandRequest.setResolved(false);
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "coerce: NLUAPIAI validation failed");
                }

                commandRequest.setCC(CC.COMMAND_UNKNOWN);
                commandRequest.setResolved(false);
            }
        } else if (nluProvider instanceof NLUWit) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUWit");
            }
            // TODO
        } else if (nluProvider instanceof NLUBluemix) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUBluemix");
            }
            // TODO
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "coerce: instanceof NLUSaiy");
            }

            if (validateNLUSaiy((NLUSaiy) nluProvider)) {

                for (final ai.saiy.android.nlu.saiy.Intent intent : ((NLUSaiy) nluProvider).getIntents()) {
                    commandRequest.setCC(NLUConstants.intentToCC(intent.getIntent()));

                    if (!commandRequest.getCC().equals(CC.COMMAND_UNKNOWN)) {
                        final NLUSaiyHelper saiyHelper = new NLUSaiyHelper();
                        commandRequest = saiyHelper.prepareCommand(mContext, commandRequest,
                                getSupportedLanguage(), intent.getEntities());

                        if (commandRequest.isResolved()) {
                            break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "coerce: COMMAND_UNKNOWN");
                        }

                        commandRequest.setCC(CC.COMMAND_UNKNOWN);
                        commandRequest.setResolved(false);
                    }
                }

                if (!commandRequest.isResolved()) {
                    commandRequest.setCC(CC.COMMAND_UNKNOWN);
                    commandRequest.setResolved(false);
                }

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "coerce: NLUSaiy validation failed");
                }

                commandRequest.setCC(CC.COMMAND_UNKNOWN);
                commandRequest.setResolved(false);
            }
        }

        commandRequest.setResultsArray(getResultsArray());

        new Quantum(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commandRequest);
    }

    /**
     * Validate the parameters prior to use.
     *
     * @param nluAPIAI the {@link NLUAPIAI} response object
     * @return true if the minimum parameters are present, false otherwise.
     */
    private boolean validateNLUAPIAI(@NonNull final NLUAPIAI nluAPIAI) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateNLUAPIAI");
        }

        if (UtilsString.notNaked(nluAPIAI.getIntent())) {
            if (UtilsMap.notNaked(nluAPIAI.getParameters())) {
                if (UtilsList.notNaked(nluAPIAI.getResults())) {
                    if (UtilsList.notNaked(nluAPIAI.getConfidence())) {
                        return true;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validateNLUAPIAI: confidence naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateNLUAPIAI: results naked");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateNLUAPIAI: parameters naked: allowing");
                }

                return true;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "validateNLUAPIAI: intent naked");
            }
        }

        return false;
    }

    /**
     * Validate the parameters prior to use.
     *
     * @param nluNuance the {@link NLUNuance} response object
     * @return true if the minimum parameters are present, false otherwise.
     */
    private boolean validateNLUNuance(@NonNull final NLUNuance nluNuance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateNLUNuance");
        }

        final List<Interpretation> interpretations = nluNuance.getInterpretations();

        if (interpretations != null) {

            for (final Interpretation interpretation : interpretations) {
                if (UtilsString.notNaked(interpretation.getLiteral())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "interpretation: getLiteral: " + interpretation.getLiteral());
                    }

                    if (interpretation.getAction() != null) {
                        if (interpretation.getAction().getIntent() != null) {
                            if (UtilsString.notNaked(interpretation.getAction().getIntent().getValue())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "interpretation: getValue: " + interpretation.getAction()
                                            .getIntent().getValue());
                                    MyLog.i(CLS_NAME, "interpretation: getConfidence: " + interpretation.getAction()
                                            .getIntent().getConfidence());
                                }

                                final Map<String, List<Concept>> map = interpretation.getConcept();

                                if (UtilsMap.notNaked(map)) {

                                    for (final Map.Entry<String, List<Concept>> entry : map.entrySet()) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "key: " + entry.getKey() + " ~ " + entry.getValue());
                                        }

                                        final List<Concept> concepts = entry.getValue();
                                        if (UtilsList.notNaked(concepts)) {

                                            final int conceptSize = concepts.size();
                                            for (int i = 0; i < conceptSize; i++) {
                                                if (UtilsString.notNaked(concepts.get(i).getLiteral())) {
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "concept: getLiteral: "
                                                                + concepts.get(i).getLiteral());
                                                        MyLog.i(CLS_NAME, "concept: getValue: "
                                                                + concepts.get(i).getValue());
                                                        MyLog.i(CLS_NAME, "concept: getRanges: "
                                                                + Arrays.deepToString(concepts.get(i)
                                                                .getRanges()));
                                                    }

                                                    if (i == (conceptSize - 1)) {
                                                        return true;
                                                    }
                                                } else {
                                                    if (DEBUG) {
                                                        MyLog.w(CLS_NAME, "validateNLUNuance: literal naked");
                                                    }
                                                    break;
                                                }
                                            }
                                        } else {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "validateNLUNuance: concepts naked");
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "validateNLUNuance: no concepts to examine");
                                    }
                                    return true;
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "validateNLUNuance: value naked");
                                }
                                break;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "validateNLUNuance: intent null");
                            }
                            break;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validateNLUNuance: action null");
                        }
                        break;
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateNLUNuance: literal naked");
                    }
                    break;
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "validateNLUNuance: interpretations null");
            }
        }

        return false;
    }

    /**
     * Validate the parameters prior to use.
     *
     * @param nluMicrosoft the {@link NLUMicrosoft} response object
     * @return true if the minimum parameters are present, false otherwise.
     */
    private boolean validateNLUNLUMicrosoft(@NonNull final NLUMicrosoft nluMicrosoft) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateNLUNLUMicrosoft");
        }

        if (UtilsString.notNaked(nluMicrosoft.getQuery())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "query: " + nluMicrosoft.getQuery());
            }

            final List<Intent> intents = nluMicrosoft.getIntents();
            if (UtilsList.notNaked(intents)) {

                for (final Intent i : intents) {

                    if (UtilsString.notNaked(i.getIntent())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getIntent: " + i.getIntent());
                            MyLog.i(CLS_NAME, "getScore: " + i.getScore());
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validateNLUNLUMicrosoft: intent naked");
                        }
                        return false;
                    }
                }

                final List<Entity> entities = nluMicrosoft.getEntities();
                if (entities != null) {

                    final int entitiesSize = entities.size();

                    if (entitiesSize > 0) {

                        for (int i = 0; i < entitiesSize; i++) {

                            if (UtilsString.notNaked(entities.get(i).getEntity())
                                    && UtilsString.notNaked(entities.get(i).getType())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "getEntity: " + entities.get(i).getEntity());
                                    MyLog.i(CLS_NAME, "getType: " + entities.get(i).getType());
                                    MyLog.i(CLS_NAME, "getStartIndex: " + entities.get(i).getStartIndex());
                                    MyLog.i(CLS_NAME, "getEndIndex: " + entities.get(i).getEndIndex());
                                    MyLog.i(CLS_NAME, "getScore: " + entities.get(i).getScore());
                                }

                                if (i == (entitiesSize - 1)) {
                                    return true;
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "validateNLUNLUMicrosoft: entity/type naked");
                                }
                                break;
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validateNLUNLUMicrosoft: no entities to examine");
                        }

                        return true;
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateNLUNLUMicrosoft: entities null");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "validateNLUNLUMicrosoft: intents naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "validateNLUNLUMicrosoft: query naked");
            }
        }

        return false;
    }

    /**
     * Validate the parameters prior to use.
     *
     * @param nluSaiy the {@link NLUSaiy} response object
     * @return true if the minimum parameters are present, false otherwise.
     */
    private boolean validateNLUSaiy(@NonNull final NLUSaiy nluSaiy) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateNLUSaiy");
        }

        final List<String> results = nluSaiy.getResults();
        if (UtilsList.notNaked(results)) {

            final float[] confidence = nluSaiy.getConfidence();
            if (UtilsList.notNaked(confidence)) {

                final List<ai.saiy.android.nlu.saiy.Intent> intents = nluSaiy.getIntents();
                if (UtilsList.notNaked(intents)) {

                    for (final ai.saiy.android.nlu.saiy.Intent intent : intents) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getIntent: " + intent.getIntent());
                            MyLog.i(CLS_NAME, "getType: " + intent.getConfidence());
                        }

                        final List<ai.saiy.android.nlu.saiy.Entity> entities = intent.getEntities();
                        if (UtilsList.notNaked(entities)) {
                            final int entitiesSize = entities.size();

                            for (int i = 0; i < entitiesSize; i++) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "getVoiceName: " + entities.get(i).getName());
                                    MyLog.i(CLS_NAME, "getVoiceName: " + entities.get(i).getValue());
                                    MyLog.i(CLS_NAME, "getVoiceName: " + entities.get(i).getConfidence());
                                    MyLog.i(CLS_NAME, "getContextual: " + entities.get(i).getContextual());
                                    MyLog.i(CLS_NAME, "getVoiceName: " + Arrays.toString(entities.get(i).getIndex()));
                                }

                                final List<ai.saiy.android.nlu.saiy.Context> contexts = entities.get(i).getContextual();
                                for (final ai.saiy.android.nlu.saiy.Context context : contexts) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "getContext: " + context.getContext());
                                        MyLog.i(CLS_NAME, "getConfidence: " + context.getConfidence());
                                    }

                                    final List<ContextValue> contextValues = context.getContextValues();
                                    for (final ContextValue contextValue : contextValues) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "getIdentifier: " + contextValue.getIdentifier());
                                        }
                                    }
                                }

                                if (i == (entitiesSize - 1)) {
                                    return true;
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "validateNLUSaiy: entities naked");
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateNLUSaiy: intents naked");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "validateNLUSaiy: confidence naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "validateNLUSaiy: results naked");
            }
        }

        return false;
    }

    public Locale getVRLocale() {
        return vrLocale;
    }

    public Locale getTTSLocale() {
        return ttsLocale;
    }

    public SupportedLanguage getSupportedLanguage() {
        return sl;
    }

    public ArrayList<String> getResultsArray() {
        return resultsArray;
    }

    public Context getContext() {
        return mContext;
    }

    private float[] getConfidenceArray() {
        return confidenceArray;
    }
}
