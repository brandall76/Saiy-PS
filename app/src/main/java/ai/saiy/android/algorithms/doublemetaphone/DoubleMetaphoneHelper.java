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

package ai.saiy.android.algorithms.doublemetaphone;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.distance.jarowinkler.JaroWinklerDistance;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomCommandContainer;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.AlgorithmicContainer;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;

/**
 * Class to apply the Double Metaphone phonetic algorithm. To avoid false positives on partial phonetic
 * matches, we apply a further two filters - the first compares the String lengths to make sure they
 * fall within the {@link Algorithm#LENGTH_THRESHOLD} and the seconds runs the {@link JaroWinklerDistance}
 * algorithm to check it falls within the {@link Algorithm#JWD_LOWER_THRESHOLD}
 * <p/>
 * Created by benrandall76@gmail.com on 21/04/2016.
 */
public class DoubleMetaphoneHelper implements Callable<Object> {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DoubleMetaphoneHelper.class.getSimpleName();

    private final Context mContext;
    private final ArrayList<String> inputData;
    private final Locale loc;
    private final ArrayList<?> genericData;


    /**
     * Constructor
     *
     * @param mContext    the application context
     * @param genericData an array containing generic data
     * @param inputData   an array of Strings containing the input comparison data
     * @param loc         the {@link Locale} extracted from the {@link SupportedLanguage}
     */
    public DoubleMetaphoneHelper(@NonNull final Context mContext, @NonNull final ArrayList<?> genericData,
                                 @NonNull final ArrayList<String> inputData, @NonNull final Locale loc) {
        this.mContext = mContext;
        this.genericData = genericData;
        this.inputData = inputData;
        this.loc = loc;
    }

    /**
     * Method to iterate through the voice data and attempt to match the user's custom commands
     * using the {@link DoubleMetaphone} within ranges applied by the associated thresholds constants.
     *
     * @return the highest scoring {@link CustomCommand} or null if thresholds aren't satisfied
     */
    public CustomCommand executeCustomCommand() {

        long then = System.nanoTime();

        final double jwdLowerThreshold = SPH.getJaroWinklerLower(mContext);

        CustomCommand customCommand = null;
        final ArrayList<CustomCommandContainer> toKeep = new ArrayList<>();
        final DoubleMetaphone metaphone = new DoubleMetaphone();
        final JaroWinklerDistance jwd = new JaroWinklerDistance();

        String phrase;
        CustomCommandContainer container;
        double score;
        boolean matches;

        int size = genericData.size();

        outer:
        for (int i = 0; i < size; i++) {
            container = (CustomCommandContainer) genericData.get(i);
            phrase = container.getKeyphrase().toLowerCase(loc).trim();

            for (String vd : inputData) {
                vd = vd.toLowerCase(loc).trim();
                matches = metaphone.isDoubleMetaphoneEqual(phrase, vd);

                if (matches && Algorithm.checkLength(phrase, vd)) {
                    score = jwd.apply(phrase, vd);

                    if (score > jwdLowerThreshold) {

                        container.setScore(score);
                        container.setUtterance(vd);
                        container.setExactMatch(true);
                        toKeep.add(SerializationUtils.clone(container));
                        break outer;
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Matches: double check JW: rejected");
                        }
                    }
                }
            }
        }

        if (UtilsList.notNaked(toKeep)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have a match");
            }

            final CustomCommandContainer ccc = toKeep.get(0);

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            customCommand = gson.fromJson(ccc.getSerialised(), CustomCommand.class);
            customCommand.setExactMatch(ccc.isExactMatch());
            customCommand.setUtterance(ccc.getUtterance());
            customCommand.setScore(ccc.getScore());
            customCommand.setAlgorithm(Algorithm.DOUBLE_METAPHONE);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no custom phrases matched");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return customCommand;
    }

    /**
     * Method to iterate through the given input data and attempt to match the given String data
     * using the {@link DoubleMetaphone} within ranges applied by the associated thresholds constants.
     *
     * @return an {@link AlgorithmicContainer} or null if thresholds aren't satisfied
     */
    public AlgorithmicContainer executeGeneric() {

        long then = System.nanoTime();

        final double jwdLowerThreshold = SPH.getJaroWinklerLower(mContext);

        final ArrayList<AlgorithmicContainer> toKeep = new ArrayList<>();
        final DoubleMetaphone metaphone = new DoubleMetaphone();
        final JaroWinklerDistance jwd = new JaroWinklerDistance();

        String generic;
        String genericLower;
        AlgorithmicContainer container = null;
        double score;
        boolean matches;

        int size = genericData.size();

        outer:
        for (int i = 0; i < size; i++) {
            generic = (String) genericData.get(i);
            genericLower = generic.toLowerCase(loc).trim();

            for (String vd : inputData) {
                vd = vd.toLowerCase(loc).trim();
                matches = metaphone.isDoubleMetaphoneEqual(genericLower, vd);

                if (matches && Algorithm.checkLength(genericLower, vd)) {
                    score = jwd.apply(genericLower, vd);

                    if (score > jwdLowerThreshold) {

                        container = new AlgorithmicContainer();
                        container.setInput(vd);
                        container.setGenericMatch(generic);
                        container.setScore(score);
                        container.setAlgorithm(Algorithm.DOUBLE_METAPHONE);
                        container.setParentPosition(i);
                        container.setExactMatch(true);
                        toKeep.add(container);
                        break outer;
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Matches: double check JW: rejected");
                        }
                    }
                }
            }
        }

        if (UtilsList.notNaked(toKeep)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have a match");
            }
            container = toKeep.get(0);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no matches");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return container;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call() throws Exception {

        if (UtilsList.notNaked(genericData)) {
            if (genericData.get(0) instanceof String) {
                return executeGeneric();
            } else {
                return executeCustomCommand();
            }
        }

        return null;
    }
}
