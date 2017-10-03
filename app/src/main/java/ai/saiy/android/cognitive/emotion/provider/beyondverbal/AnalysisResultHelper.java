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
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Analysis;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.AnalysisSummary;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Arousal;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.AudioQuality;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Composite;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Emotions;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Gender;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Group11;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Group21;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Group7;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Mood;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Primary;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Result;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Secondary;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Segment;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Summary;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Temper;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Valence;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 14/08/2016.
 */

public class AnalysisResultHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AnalysisResultHelper.class.getSimpleName();

    private final Context mContext;
    private final SupportedLanguage sl;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage} object
     */
    public AnalysisResultHelper(@NonNull final Context mContext, @NonNull final SupportedLanguage sl) {
        this.mContext = mContext;
        this.sl = sl;
    }

    /**
     * Analyse the emotion response attempting to construct a verbose explanation of the interpretation to announce to the
     * user.
     *
     * @param emotions the {@link Emotions} response object
     */
    public void interpretAndStore(@Nullable final Emotions emotions) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "interpretAndStore");
        }

        if (emotions != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "interpretAndStore: getRecordingId: " + emotions.getRecordingId());
                MyLog.i(CLS_NAME, "interpretAndStore: getStatus: " + emotions.getStatus());
            }

            if (emotions.getStatus().matches(Emotions.SUCCESS)) {

                final Result result = emotions.getResult();

                if (result != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "result: getDuration: " + result.getDuration());
                        MyLog.i(CLS_NAME, "result: getSessionStatus: " + result.getSessionStatus());
                        verboseEmotions(result);
                    }

                    final AnalysisResult resultHolder = new AnalysisResult();
                    resultHolder.setAnalysisTime(System.currentTimeMillis());
                    resultHolder.setRecordingId(emotions.getRecordingId());
                    resultHolder.setDescription(constructResponse(result));

                    if (UtilsString.notNaked(resultHolder.getDescription())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "interpretAndStore: saving emotion analysis");
                        }
                        SPH.setEmotion(mContext, new GsonBuilder().disableHtmlEscaping().create().toJson(resultHolder));
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "interpretAndStore: resultHolder getDescription naked");
                        }
                        SPH.setEmotion(mContext, null);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "interpretAndStore: result null");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "interpretAndStore: status failure");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "interpretAndStore: emotions null");
            }
        }

        NotificationHelper.createEmotionAnalysisNotification(mContext);
    }

    private String getIntro(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_intro);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getStartDesc(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_start_desc);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getConnector(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_connector);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getGap(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_gap);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getConnectorTwo(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_connector_two);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getValenceIntro(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_valence_intro);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getConnectorThree(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_temper_connector_three);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getMoodConnector(@NonNull final SaiyResources sr) {
        final String[] stringArray = sr.getStringArray(R.array.array_beyond_verbal_moods_connector);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getModeOne(final String[] stringArray) {
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Helper method to avoid duplication of a modal response
     *
     * @param stringArray the array of possible responses
     * @return a random mode response
     */
    private String getModeTwo(@NonNull final String[] stringArray) {
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getModeThree(final String[] stringArray) {
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    private String getValenceLevel(@NonNull final SaiyResources sr, @NonNull final String valenceMode) {

        switch (valenceMode) {

            case Valence.NEGATIVE:
                return sr.getString(R.string.negative);
            case Valence.NEUTRAL:
                return sr.getString(R.string.neutral);
            case Valence.POSITIVE:
                return sr.getString(R.string.positive);
            default:
                return sr.getString(R.string.neutral);
        }
    }

    private String getArousalLevel(@NonNull final SaiyResources sr, @NonNull final String arousalMode) {

        switch (arousalMode) {

            case Analysis.LOW:
                return sr.getString(R.string.low);
            case Analysis.MED:
                return sr.getString(R.string.medium);
            case Analysis.HIGH:
                return sr.getString(R.string.high);
            default:
                return sr.getString(R.string.medium);
        }
    }

    /**
     * Method to construct a verbose interpretation of the emotion analysis.
     *
     * @param result the emotion {@link Result} object
     * @return a constructed String that can be announced to the user.
     */
    public String constructResponse(@Nullable final Result result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "constructResponse");
        }

        String response = null;

        if (result == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: result null");
            }
            return response;
        }

        final AnalysisSummary analysisSummary = result.getAnalysisSummary();

        if (analysisSummary == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "interpretAndStore: analysisSummary null");
            }
            return response;
        }

        final Analysis analysisResult = analysisSummary.getAnalysisResult();

        if (analysisResult == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: analysisResult null");
            }
            return response;
        }

        final Temper temper = analysisResult.getTemper();

        if (temper == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: temper null");
            }
            return response;
        }

        final String temperMode = temper.getMode();

        if (!UtilsString.notNaked(temperMode)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: temperMode null");
            }
        }

        final SaiyResources sr = new SaiyResources(mContext, sl);

        String mode;
        String modeOne;
        String modeTwo;
        String modeThree;
        String[] stringArray;

        final String intro = getIntro(sr);
        final String startDesc = getStartDesc(sr);
        final String connector = getConnector(sr);
        final String gap = getGap(sr);
        final String connectorTwo = getConnectorTwo(sr);
        final String valenceIntro = getValenceIntro(sr);
        final String connectorThree = getConnectorThree(sr);
        final String moodConnector = getMoodConnector(sr);

        final String arousalIntro = sr.getString(R.string.beyond_verbal_arousal_intro);

        String valenceLevel = null;
        String arousalLevel = null;

        String compositePrimaryPhrase = null;
        String compositeSecondaryPhrase = null;
        String group7PrimaryPhrase = null;
        String group7SecondaryPhrase = null;
        String group11PrimaryPhrase = null;
        String group11SecondaryPhrase = null;
        String group21PrimaryPhrase = null;
        String group21SecondaryPhrase = null;

        switch (temperMode) {

            case Analysis.LOW:
                mode = sr.getString(R.string.low);
                stringArray = sr.getStringArray(R.array.array_beyond_verbal_synonyms_low_first);
                modeOne = getModeOne(stringArray);
                modeTwo = getModeTwo(stringArray);

                while (modeTwo.matches(modeOne)) {
                    modeTwo = getModeTwo(stringArray);
                }

                modeThree = getModeThree(sr.getStringArray(R.array.array_beyond_verbal_synonyms_low_second));

                break;
            case Analysis.MED:
                mode = sr.getString(R.string.medium);
                stringArray = sr.getStringArray(R.array.array_beyond_verbal_synonyms_medium_first);
                modeOne = getModeOne(stringArray);
                modeTwo = getModeTwo(stringArray);

                while (modeTwo.matches(modeOne)) {
                    modeTwo = getModeTwo(stringArray);
                }

                modeThree = getModeThree(sr.getStringArray(R.array.array_beyond_verbal_synonyms_medium_second));

                break;
            case Analysis.HIGH:
                mode = sr.getString(R.string.high);
                stringArray = sr.getStringArray(R.array.array_beyond_verbal_synonyms_high_first);
                modeOne = getModeOne(stringArray);
                modeTwo = getModeTwo(stringArray);

                while (modeTwo.matches(modeOne)) {
                    modeTwo = getModeTwo(stringArray);
                }

                modeThree = getModeThree(sr.getStringArray(R.array.array_beyond_verbal_synonyms_high_second));

                break;
            default:
                mode = sr.getString(R.string.low);
                stringArray = sr.getStringArray(R.array.array_beyond_verbal_synonyms_low_first);
                modeOne = getModeOne(stringArray);
                modeTwo = getModeTwo(stringArray);

                while (modeTwo.matches(modeOne)) {
                    modeTwo = getModeTwo(stringArray);
                }

                modeThree = getModeThree(sr.getStringArray(R.array.array_beyond_verbal_synonyms_low_second));

                break;
        }

        final Valence valence = analysisResult.getValence();

        if (valence != null) {

            final String valenceMode = valence.getMode();

            if (UtilsString.notNaked(valenceMode)) {
                valenceLevel = getValenceLevel(sr, valenceMode);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "constructResponse: valenceMode naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: valence null");
            }
        }

        final Arousal arousal = analysisResult.getArousal();

        if (arousal != null) {

            final String arousalMode = arousal.getMode();

            if (UtilsString.notNaked(arousalMode)) {
                arousalLevel = getArousalLevel(sr, arousalMode);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "constructResponse: arousalMode naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "constructResponse: arousal null");
            }
        }

        final List<Segment> segments = result.getSegments();

        if (UtilsList.notNaked(segments)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "segments count: " + segments.size());
            }

            final Analysis analysis = segments.get(0).getAnalysis();

            if (analysis != null) {

                final Mood mood = analysis.getMood();

                if (mood != null) {

                    final Composite composite = mood.getComposite();
                    if (composite != null) {
                        final Primary compositePrimary = composite.getPrimary();
                        if (compositePrimary != null) {

                            compositePrimaryPhrase = compositePrimary.getPhrase();

                            if (!UtilsString.notNaked(compositePrimaryPhrase)) {
                                compositePrimaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood compositePrimary null");
                            }
                        }

                        final Secondary compositeSecondary = composite.getSecondary();
                        if (compositeSecondary != null) {

                            compositeSecondaryPhrase = compositeSecondary.getPhrase();

                            if (!UtilsString.notNaked(compositeSecondaryPhrase)) {
                                compositeSecondaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood compositeSecondary null");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "segment: mood composite null");
                        }
                    }

                    final Group7 group7 = mood.getGroup7();

                    if (group7 != null) {

                        final Primary group7Primary = group7.getPrimary();

                        if (group7Primary != null) {

                            group7PrimaryPhrase = group7Primary.getPhrase();

                            if (!UtilsString.notNaked(group7PrimaryPhrase)) {
                                group7PrimaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group7Primary null");
                            }
                        }

                        final Secondary group7Secondary = group7.getSecondary();

                        if (group7Secondary != null) {

                            group7SecondaryPhrase = group7Secondary.getPhrase();

                            if (!UtilsString.notNaked(group7SecondaryPhrase)) {
                                group7SecondaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group7Secondary null");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "segment: mood group7 null");
                        }
                    }

                    final Group11 group11 = mood.getGroup11();

                    if (group11 != null) {

                        final Primary group11Primary = group11.getPrimary();

                        if (group11Primary != null) {

                            group11PrimaryPhrase = group11Primary.getPhrase();

                            if (!UtilsString.notNaked(group11PrimaryPhrase)) {
                                group11PrimaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group11Primary null");
                            }
                        }

                        final Secondary group11Secondary = group11.getSecondary();

                        if (group11Secondary != null) {

                            group11SecondaryPhrase = group11Secondary.getPhrase();

                            if (!UtilsString.notNaked(group11SecondaryPhrase)) {
                                group11SecondaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group11Secondary null");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "segment: mood group11 null");
                        }
                    }

                    final Group21 group21 = mood.getGroup21();

                    if (group21 != null) {

                        final Primary group21Primary = group21.getPrimary();

                        if (group21Primary != null) {

                            group21PrimaryPhrase = group21Primary.getPhrase();

                            if (!UtilsString.notNaked(group21PrimaryPhrase)) {
                                group21PrimaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group21Primary null");
                            }
                        }

                        final Secondary group21Secondary = group21.getSecondary();

                        if (group21Secondary != null) {

                            group21SecondaryPhrase = group21Secondary.getPhrase();

                            if (!UtilsString.notNaked(group21SecondaryPhrase)) {
                                group21SecondaryPhrase = "";
                            }

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: mood group21Secondary null");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "segment: mood group21 null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "segment: mood null");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "constructResponse: analysis null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "constructResponse: segments naked");
            }
        }

        if (DEBUG) {
            MyLog.v(CLS_NAME, "constructResponse: intro: " + intro);
            MyLog.v(CLS_NAME, "constructResponse: mode: " + mode);
            MyLog.v(CLS_NAME, "constructResponse: startDesc: " + startDesc);
            MyLog.v(CLS_NAME, "constructResponse: connector: " + connector);
            MyLog.v(CLS_NAME, "constructResponse: modeOne: " + modeOne);
            MyLog.v(CLS_NAME, "constructResponse: gap: " + gap);
            MyLog.v(CLS_NAME, "constructResponse: modeTwo: " + modeTwo);
            MyLog.v(CLS_NAME, "constructResponse: connectorTwo: " + connectorTwo);
            MyLog.v(CLS_NAME, "constructResponse: modeThree: " + modeThree);
            MyLog.v(CLS_NAME, "constructResponse: arousalIntro: " + arousalIntro);
            MyLog.v(CLS_NAME, "constructResponse: arousalLevel: " + arousalLevel);
            MyLog.v(CLS_NAME, "constructResponse: valenceIntro: " + valenceIntro);
            MyLog.v(CLS_NAME, "constructResponse: valenceLevel: " + valenceLevel);
            MyLog.v(CLS_NAME, "constructResponse: connectorThree: " + connectorThree);
            MyLog.v(CLS_NAME, "constructResponse: compositePrimaryPhrase: " + compositePrimaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: compositeSecondaryPhrase: " + compositeSecondaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group7PrimaryPhrase: " + group7PrimaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group7SecondaryPhrase: " + group7SecondaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group11PrimaryPhrase: " + group11PrimaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group11SecondaryPhrase: " + group11SecondaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group21PrimaryPhrase: " + group21PrimaryPhrase);
            MyLog.v(CLS_NAME, "constructResponse: group21SecondaryPhrase: " + group21SecondaryPhrase);
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(intro);
        builder.append(" ");
        builder.append(mode);
        builder.append(", ");
        builder.append(startDesc);
        builder.append(" ");
        builder.append(connector);
        builder.append(" ");
        builder.append(modeOne);
        builder.append(", ");
        builder.append(gap);
        builder.append(" ");
        builder.append(modeTwo);
        builder.append(", ");
        builder.append(connectorTwo);
        builder.append(" ");
        builder.append(modeThree);
        builder.append(". ");

        if (UtilsString.notNaked(arousalLevel)) {
            builder.append(arousalIntro);
            builder.append(", ");
            builder.append(arousalLevel);
            builder.append(". ");
        }

        if (UtilsString.notNaked(valenceLevel)) {
            builder.append(valenceIntro);
            builder.append(", ");
            builder.append(valenceLevel);
            builder.append(". ");
        }

        builder.append(connectorThree);
        builder.append(", ");

        if (UtilsString.notNaked(compositePrimaryPhrase)) {
            builder.append(compositePrimaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(compositeSecondaryPhrase)) {
            builder.append(compositeSecondaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(group7PrimaryPhrase)) {
            builder.append(group7PrimaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(group7SecondaryPhrase)) {
            builder.append(group7SecondaryPhrase);
            builder.append(". ");
        }

        builder.append(moodConnector);
        builder.append(", ");

        if (UtilsString.notNaked(group11PrimaryPhrase)) {
            builder.append(group11PrimaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(group11SecondaryPhrase)) {
            builder.append(group11SecondaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(group21PrimaryPhrase)) {
            builder.append(group21PrimaryPhrase);
            builder.append(". ");
        }

        if (UtilsString.notNaked(group21SecondaryPhrase)) {
            builder.append(group21SecondaryPhrase);
            builder.append(". ");
        }

        response = builder.toString().replaceAll("\\.+", ".").trim();

        if (response.endsWith(".")) {
            response = UtilsString.replaceLast(StringUtils.removeEnd(response, "."), ".",
                    sr.getString(R.string._and));
        } else {
            response = UtilsString.replaceLast(response, ".", sr.getString(R.string._and));
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "constructResponse: response: " + response);
        }

        sr.reset();

        return response;

    }

    /**
     * Verbose emotion analysis information for debugging only
     *
     * @param result the {@link Result} object
     */
    private void verboseEmotions(@NonNull final Result result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "verboseEmotions");
            MyLog.i(CLS_NAME, "result: getDuration: " + result.getDuration());
            MyLog.i(CLS_NAME, "result: getSessionStatus: " + result.getSessionStatus());
        }

        Temper temper;
        Valence valence;
        Gender gender;
        Arousal arousal;
        AudioQuality audioQuality;

        final AnalysisSummary analysisSummary = result.getAnalysisSummary();

        if (analysisSummary != null) {

            final Analysis analysisResult = analysisSummary.getAnalysisResult();

            if (analysisResult != null) {

                temper = analysisResult.getTemper();

                if (temper != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "analysisResult: temper: getGroup: " + temper.getGroup());
                        MyLog.i(CLS_NAME, "analysisResult: temper: getValue: " + temper.getValue());
                        MyLog.i(CLS_NAME, "analysisResult: temper: getMode: " + temper.getMode());
                        MyLog.i(CLS_NAME, "analysisResult: temper: getMean: " + temper.getMean());
                    }

                    final Summary temperSummary = temper.getSummary();

                    if (temperSummary != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "analysisResult: temperSummary: getMode: " + temperSummary.getMode());
                            MyLog.i(CLS_NAME, "analysisResult: temperSummary: getMean: " + temperSummary.getMean());
                            MyLog.i(CLS_NAME, "analysisResult: temperSummary: getModePct: " + temperSummary.getModePct());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "analysisResult: temperSummary null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "analysisResult: temper null");
                    }
                }

                valence = analysisResult.getValence();

                if (valence != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "analysisResult: valence: getGroup: " + valence.getGroup());
                        MyLog.i(CLS_NAME, "analysisResult: valence: getValue: " + valence.getValue());
                        MyLog.i(CLS_NAME, "analysisResult: valence: getMode: " + valence.getMode());
                        MyLog.i(CLS_NAME, "analysisResult: valence: getMean: " + valence.getMean());
                    }

                    final Summary valenceSummary = valence.getSummary();

                    if (valenceSummary != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "analysisResult: valenceSummary: getMode: " + valenceSummary.getMode());
                            MyLog.i(CLS_NAME, "analysisResult: valenceSummary: getMean: " + valenceSummary.getMean());
                            MyLog.i(CLS_NAME, "analysisResult: valenceSummary: getModePct: " + valenceSummary.getModePct());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "analysisResult: valenceSummary null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "analysisResult: valence null");
                    }
                }

                gender = analysisResult.getGender();

                if (gender != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "analysisResult: gender: getGroup: " + gender.getGroup());
                        MyLog.i(CLS_NAME, "analysisResult: gender: getValue: " + gender.getValue());
                        MyLog.i(CLS_NAME, "analysisResult: gender: getMode: " + gender.getMode());
                        MyLog.i(CLS_NAME, "analysisResult: gender: getMean: " + gender.getMean());
                    }

                    final Summary genderSummary = gender.getSummary();

                    if (genderSummary != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "analysisResult: genderSummary: getMode: " + genderSummary.getMode());
                            MyLog.i(CLS_NAME, "analysisResult: genderSummary: getMean: " + genderSummary.getMean());
                            MyLog.i(CLS_NAME, "analysisResult: genderSummary: getModePct: " + genderSummary.getModePct());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "analysisResult: genderSummary null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "analysisResult: gender null");
                    }
                }

                arousal = analysisResult.getArousal();

                if (arousal != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "analysisResult: arousal: getGroup: " + arousal.getGroup());
                        MyLog.i(CLS_NAME, "analysisResult: arousal: getValue: " + arousal.getValue());
                        MyLog.i(CLS_NAME, "analysisResult: arousal: getMode: " + arousal.getMode());
                        MyLog.i(CLS_NAME, "analysisResult: arousal: getMean: " + arousal.getMean());
                    }

                    final Summary arousalSummary = arousal.getSummary();

                    if (arousalSummary != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "analysisResult: arousalSummary: getMode: " + arousalSummary.getMode());
                            MyLog.i(CLS_NAME, "analysisResult: arousalSummary: getMean: " + arousalSummary.getMean());
                            MyLog.i(CLS_NAME, "analysisResult: arousalSummary: getModePct: " + arousalSummary.getModePct());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "analysisResult: arousalSummary null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "analysisResult: arousal null");
                    }
                }

                audioQuality = analysisResult.getAudioQuality();

                if (audioQuality != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "analysisResult: audioQuality: getGroup: " + audioQuality.getGroup());
                        MyLog.i(CLS_NAME, "analysisResult: audioQuality: getValue: " + audioQuality.getValue());
                        MyLog.i(CLS_NAME, "analysisResult: audioQuality: getMode: " + audioQuality.getMode());
                        MyLog.i(CLS_NAME, "analysisResult: audioQuality: getMean: " + audioQuality.getMean());
                    }

                    final Summary audioQualitySummary = audioQuality.getSummary();

                    if (audioQualitySummary != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "analysisResult: audioQualitySummary: getMode: " + audioQualitySummary.getMode());
                            MyLog.i(CLS_NAME, "analysisResult: audioQualitySummary: getMean: " + audioQualitySummary.getMean());
                            MyLog.i(CLS_NAME, "analysisResult: audioQualitySummary: getModePct: " + audioQualitySummary.getModePct());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "analysisResult: audioQualitySummary null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "analysisResult: audioQuality null");
                    }
                }

                final List<Segment> segments = result.getSegments();

                if (UtilsList.notNaked(segments)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "segments count: " + segments.size());
                    }

                    for (final Segment segment : segments) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "segment: getDuration: " + segment.getDuration());
                            MyLog.i(CLS_NAME, "segment: getOffset: " + segment.getOffset());
                        }

                        final Analysis analysis = segment.getAnalysis();

                        if (analysis != null) {

                            temper = analysis.getTemper();

                            if (temper != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "analysis: temper: getGroup: " + temper.getGroup());
                                    MyLog.i(CLS_NAME, "analysis: temper: getValue: " + temper.getValue());
                                    MyLog.i(CLS_NAME, "analysis: temper: getMode: " + temper.getMode());
                                    MyLog.i(CLS_NAME, "analysis: temper: getMean: " + temper.getMean());
                                }

                                final Summary temperSummary = temper.getSummary();

                                if (temperSummary != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "analysis: temperSummary: getMode: " + temperSummary.getMode());
                                        MyLog.i(CLS_NAME, "analysis: temperSummary: getMean: " + temperSummary.getMean());
                                        MyLog.i(CLS_NAME, "analysis: temperSummary: getModePct: " + temperSummary.getModePct());
                                    }

                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "analysis: temperSummary null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "analysis: temper null");
                                }
                            }

                            valence = analysis.getValence();

                            if (valence != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "analysis: valence: getGroup: " + valence.getGroup());
                                    MyLog.i(CLS_NAME, "analysis: valence: getValue: " + valence.getValue());
                                    MyLog.i(CLS_NAME, "analysis: valence: getMode: " + valence.getMode());
                                    MyLog.i(CLS_NAME, "analysis: valence: getMean: " + valence.getMean());
                                }

                                final Summary valenceSummary = valence.getSummary();

                                if (valenceSummary != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "analysis: valenceSummary: getMode: " + valenceSummary.getMode());
                                        MyLog.i(CLS_NAME, "analysis: valenceSummary: getMean: " + valenceSummary.getMean());
                                        MyLog.i(CLS_NAME, "analysis: valenceSummary: getModePct: " + valenceSummary.getModePct());
                                    }

                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "analysis: valenceSummary null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "analysis: valence null");
                                }
                            }

                            gender = analysis.getGender();

                            if (gender != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "analysis: gender: getGroup: " + gender.getGroup());
                                    MyLog.i(CLS_NAME, "analysis: gender: getValue: " + gender.getValue());
                                    MyLog.i(CLS_NAME, "analysis: gender: getMode: " + gender.getMode());
                                    MyLog.i(CLS_NAME, "analysis: gender: getMean: " + gender.getMean());
                                }

                                final Summary genderSummary = gender.getSummary();

                                if (genderSummary != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "analysis: genderSummary: getMode: " + genderSummary.getMode());
                                        MyLog.i(CLS_NAME, "analysis: genderSummary: getMean: " + genderSummary.getMean());
                                        MyLog.i(CLS_NAME, "analysis:genderSummary: getModePct: " + genderSummary.getModePct());
                                    }

                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "analysis: genderSummary null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "analysis: gender null");
                                }
                            }

                            arousal = analysis.getArousal();

                            if (arousal != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "analysis: arousal: getGroup: " + arousal.getGroup());
                                    MyLog.i(CLS_NAME, "analysis: arousal: getValue: " + arousal.getValue());
                                    MyLog.i(CLS_NAME, "analysis: arousal: getMode: " + arousal.getMode());
                                    MyLog.i(CLS_NAME, "analysis: arousal: getMean: " + arousal.getMean());
                                }

                                final Summary arousalSummary = arousal.getSummary();

                                if (arousalSummary != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "analysis: arousalSummary: getMode: " + arousalSummary.getMode());
                                        MyLog.i(CLS_NAME, "analysis: arousalSummary: getMean: " + arousalSummary.getMean());
                                        MyLog.i(CLS_NAME, "analysis: arousalSummary: getModePct: " + arousalSummary.getModePct());
                                    }

                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "analysis: arousalSummary null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "analysis: arousal null");
                                }
                            }

                            audioQuality = analysis.getAudioQuality();

                            if (audioQuality != null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "analysis: audioQuality: getGroup: " + audioQuality.getGroup());
                                    MyLog.i(CLS_NAME, "analysis: audioQuality: getValue: " + audioQuality.getValue());
                                    MyLog.i(CLS_NAME, "analysis: audioQuality: getMode: " + audioQuality.getMode());
                                    MyLog.i(CLS_NAME, "analysis: audioQuality: getMean: " + audioQuality.getMean());
                                }

                                final Summary audioQualitySummary = audioQuality.getSummary();

                                if (audioQualitySummary != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "analysis: audioQualitySummary: getMode: " + audioQualitySummary.getMode());
                                        MyLog.i(CLS_NAME, "analysis: audioQualitySummary: getMean: " + audioQualitySummary.getMean());
                                        MyLog.i(CLS_NAME, "analysis: audioQualitySummary: getModePct: " + audioQualitySummary.getModePct());
                                    }

                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "analysis: audioQualitySummary null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "analysis: audioQuality null");
                                }
                            }

                            final Mood mood = analysis.getMood();

                            if (mood != null) {

                                final Composite composite = mood.getComposite();

                                if (composite != null) {

                                    final Primary compositePrimary = composite.getPrimary();

                                    if (compositePrimary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "compositePrimary: getPhrase: " + compositePrimary.getPhrase());
                                            MyLog.i(CLS_NAME, "compositePrimary: getId: " + compositePrimary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood compositePrimary null");
                                        }
                                    }

                                    final Secondary compositeSecondary = composite.getSecondary();

                                    if (compositeSecondary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "compositeSecondary: getPhrase: " + compositeSecondary.getPhrase());
                                            MyLog.i(CLS_NAME, "compositeSecondary: getId: " + compositeSecondary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood compositeSecondary null");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "segment: mood composite null");
                                    }
                                }

                                final Group7 group7 = mood.getGroup7();

                                if (group7 != null) {

                                    final Primary group7Primary = group7.getPrimary();

                                    if (group7Primary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group7Primary: getPhrase: " + group7Primary.getPhrase());
                                            MyLog.i(CLS_NAME, "group7Primary: getId: " + group7Primary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group7Primary null");
                                        }
                                    }

                                    final Secondary group7Secondary = group7.getSecondary();

                                    if (group7Secondary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group7Secondary: getPhrase: " + group7Secondary.getPhrase());
                                            MyLog.i(CLS_NAME, "group7Secondary: getId: " + group7Secondary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group7Secondary null");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "segment: mood group7 null");
                                    }
                                }

                                final Group11 group11 = mood.getGroup11();

                                if (group11 != null) {

                                    final Primary group11Primary = group11.getPrimary();

                                    if (group11Primary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group11Primary: getPhrase: " + group11Primary.getPhrase());
                                            MyLog.i(CLS_NAME, "group11Primary: getId: " + group11Primary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group11Primary null");
                                        }
                                    }

                                    final Secondary group11Secondary = group11.getSecondary();

                                    if (group11Secondary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group11Secondary: getPhrase: " + group11Secondary.getPhrase());
                                            MyLog.i(CLS_NAME, "group11Secondary: getId: " + group11Secondary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group11Secondary null");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "segment: mood group11 null");
                                    }
                                }

                                final Group21 group21 = mood.getGroup21();

                                if (group21 != null) {

                                    final Primary group21Primary = group21.getPrimary();

                                    if (group21Primary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group21Primary: getPhrase: " + group21Primary.getPhrase());
                                            MyLog.i(CLS_NAME, "group21Primary: getId: " + group21Primary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group21Primary null");
                                        }
                                    }

                                    final Secondary group21Secondary = group21.getSecondary();

                                    if (group21Secondary != null) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "group21Secondary: getPhrase: " + group21Secondary.getPhrase());
                                            MyLog.i(CLS_NAME, "group21Secondary: getId: " + group21Secondary.getId());
                                        }

                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "segment: mood group21Secondary null");
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "segment: mood group21 null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "segment: mood null");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "segment: analysis null");
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "interpretAndStore: segments naked");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "interpretAndStore: analysisResult null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "interpretAndStore: analysisSummary null");
            }
        }
    }

    /**
     * Check if we have an emotion analysis result saved
     *
     * @param ctx the application context
     * @return true if a {@link AnalysisResult} is stored
     */
    public static boolean hasEmotion(@NonNull final Context ctx) {
        return SPH.getEmotion(ctx) != null;
    }

    /**
     * Get the {@link AnalysisResult} description we have stored
     *
     * @param ctx the application context
     * @return the {@link AnalysisResult} description
     */
    public static String getEmotionDescription(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final AnalysisResult analysisResult;

        if (hasEmotion(ctx)) {

            try {
                analysisResult = gson.fromJson(SPH.getEmotion(ctx), AnalysisResult.class);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "emotion: " + gson.toJson(analysisResult));
                }
                return analysisResult.getDescription();
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "emotion: JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "emotion: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "emotion: Exception");
                    e.printStackTrace();
                }
            }
        }

        return PersonalityResponse.getBeyondVerbalErrorResponse(ctx, sl);
    }
}