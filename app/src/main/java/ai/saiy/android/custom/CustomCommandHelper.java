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

package ai.saiy.android.custom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.distance.jarowinkler.JaroWinklerHelper;
import ai.saiy.android.algorithms.distance.levenshtein.LevenshteinHelper;
import ai.saiy.android.algorithms.doublemetaphone.DoubleMetaphoneHelper;
import ai.saiy.android.algorithms.fuzzy.FuzzyHelper;
import ai.saiy.android.algorithms.metaphone.MetaphoneHelper;
import ai.saiy.android.algorithms.mongeelkan.MongeElkanHelper;
import ai.saiy.android.algorithms.needlemanwunch.NeedlemanWunschHelper;
import ai.saiy.android.algorithms.regex.ContainsHelper;
import ai.saiy.android.algorithms.regex.CustomHelper;
import ai.saiy.android.algorithms.regex.EndsWithHelper;
import ai.saiy.android.algorithms.regex.StartsWithHelper;
import ai.saiy.android.algorithms.soundex.SoundexHelper;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.database.DBCustomCommand;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;

import static ai.saiy.android.custom.CCC.CUSTOM_INTENT_SERVICE;

/**
 * Class to extract any custom commands the user has created from {@link DBCustomCommand} and compare
 * with the voice data to see if we have any matches.
 * <p/>
 * Users often create weird and wonderful commands, using 'unnatural' phrases and therefore the
 * successful detection rate by the voice recognition provider can be lower.
 * <p/>
 * Due to this, we need to apply some String matching {@link ai.saiy.android.algorithms} either by
 * their distance or phonetics.
 * <p/>
 * The default threshold each algorithm applies needs to be selected carefully to avoid false positives.
 * Additionally, we have to allow advanced users to alter these thresholds - a necessary evil
 * when false positives are a common occurrence for them. One threshold does unfortunately not
 * suit all...
 * <p/>
 * Before any algorithm is applied, we need to check it is suitable for the {@link SupportedLanguage}
 * <p/>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class CustomCommandHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CustomCommandHelper.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 500L;

    private static final Object lock = new Object();

    private CustomCommand customCommand = null;

    /**
     * @param ctx       the application context
     * @param voiceData the array of recognition results
     * @param sl        the {@link SupportedLanguage}
     * @return true if a {@link CustomCommand} is detected. False otherwise
     */
    public boolean isCustomCommand(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                                   @NonNull final SupportedLanguage sl, @NonNull final ArrayList<CustomCommandContainer> cccArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        final long then = System.nanoTime();
        final Locale loc = sl.getLocale();

        if (!UtilsList.notNaked(cccArray)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no custom commands");
                MyLog.getElapsed(CustomCommandHelper.class.getSimpleName(), then);
            }
            return false;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have commands: " + cccArray.size());
            }
        }

        final ArrayList<CustomCommandContainer> cccArrayStartWith = new ArrayList<>();
        final ArrayList<CustomCommandContainer> cccArrayEndsWith = new ArrayList<>();
        final ArrayList<CustomCommandContainer> cccArrayContains = new ArrayList<>();
        final ArrayList<CustomCommandContainer> cccArrayCustom = new ArrayList<>();

        final ListIterator<CustomCommandContainer> itr = cccArray.listIterator();

        CustomCommandContainer container;
        while (itr.hasNext()) {
            container = itr.next();

            switch (container.getRegex()) {

                case MATCHES:
                    break;
                case STARTS_WITH:
                    cccArrayStartWith.add(container);
                    itr.remove();
                    break;
                case ENDS_WITH:
                    cccArrayEndsWith.add(container);
                    itr.remove();
                    break;
                case CONTAINS:
                    cccArrayContains.add(container);
                    itr.remove();
                    break;
                case CUSTOM:
                    cccArrayCustom.add(container);
                    itr.remove();
                    break;
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "algorithmic commands: " + cccArray.size());
            MyLog.i(CLS_NAME, "regex commands: " + (cccArrayStartWith.size() + cccArrayEndsWith.size()
                    + cccArrayContains.size() + cccArrayCustom.size()));
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final Algorithm[] algorithms = Algorithm.getAlgorithms(ctx, sl);

        final int callableListSize;

        if (UtilsList.notNaked(cccArray)) {
            callableListSize = algorithms.length + cccArrayStartWith.size() + cccArrayEndsWith.size()
                    + cccArrayContains.size() + cccArrayCustom.size();
        } else {
            callableListSize = cccArrayStartWith.size() + cccArrayEndsWith.size()
                    + cccArrayContains.size() + cccArrayCustom.size();
        }

        final List<Callable<Object>> callableList = new ArrayList<>(callableListSize);

        if (UtilsList.notNaked(cccArrayStartWith)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have starts with: " + cccArrayStartWith.size());
            }

            callableList.add(new StartsWithHelper(cccArrayStartWith, voiceData, loc));
        }

        if (UtilsList.notNaked(cccArrayEndsWith)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have ends with: " + cccArrayEndsWith.size());
            }

            callableList.add(new EndsWithHelper(cccArrayEndsWith, voiceData, loc));
        }

        if (UtilsList.notNaked(cccArrayContains)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have contains: " + cccArrayContains.size());
            }

            callableList.add(new ContainsHelper(cccArrayContains, voiceData, loc));
        }

        if (UtilsList.notNaked(cccArrayCustom)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have contains: " + cccArrayCustom.size());
            }

            callableList.add(new CustomHelper(cccArrayCustom, voiceData, loc));
        }

        if (UtilsList.notNaked(cccArray)) {

            for (final Algorithm algorithm : algorithms) {

                switch (algorithm) {

                    case JARO_WINKLER:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: JARO_WINKLER");
                        }

                        callableList.add(new JaroWinklerHelper(ctx, cccArray, voiceData, loc));
                        break;
                    case LEVENSHTEIN:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: LEVENSHTEIN");
                        }

                        callableList.add(new LevenshteinHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case SOUNDEX:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: SOUNDEX");
                        }

                        callableList.add(new SoundexHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case METAPHONE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: METAPHONE");
                        }

                        callableList.add(new MetaphoneHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case DOUBLE_METAPHONE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: DOUBLE_METAPHONE");
                        }

                        callableList.add(new DoubleMetaphoneHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case FUZZY:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: FUZZY");
                        }

                        callableList.add(new FuzzyHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case NEEDLEMAN_WUNCH:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: NEEDLEMAN_WUNCH");
                        }

                        callableList.add(new NeedlemanWunschHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                    case MONGE_ELKAN:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Running: MONGE_ELKAN");
                        }

                        callableList.add(new MongeElkanHelper(ctx, cccArray,
                                voiceData, loc));
                        break;
                }
            }
        }


        final ArrayList<CustomCommand> customCommandArray = new ArrayList<>();

        try {

            final List<Future<Object>> futures = executorService.invokeAll(callableList,
                    THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

            for (final Future<Object> future : futures) {
                customCommandArray.add((CustomCommand) future.get());
            }

        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: ExecutionException");
                e.printStackTrace();
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: CancellationException");
                e.printStackTrace();
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: InterruptedException");
                e.printStackTrace();
            }
        } finally {
            executorService.shutdown();
        }

        if (!customCommandArray.isEmpty()) {
            customCommandArray.removeAll(Collections.<CustomCommand>singleton(null));
            if (DEBUG) {
                MyLog.i(CLS_NAME, "algorithms returned " + customCommandArray.size() + " matches");
                for (final CustomCommand c : customCommandArray) {
                    MyLog.i(CLS_NAME, "Potentials: " + c.getAlgorithm().name() + " ~ "
                            + c.getKeyphrase() + " ~ " + c.getUtterance() + " ~ "
                            + c.getScore());
                }
            }

            CustomCommand cc;
            final ListIterator<CustomCommand> itrCC = customCommandArray.listIterator();

            while (itrCC.hasNext()) {
                cc = itrCC.next();
                if (cc == null) {
                    itrCC.remove();
                } else {

                    if (cc.isExactMatch()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "exact match: " + cc.getAlgorithm().name() + " ~ "
                                    + cc.getKeyphrase() + " ~ " + cc.getUtterance() + " ~ "
                                    + cc.getScore());
                        }
                        customCommand = cc;
                        break;
                    }
                }
            }
        }

        if (customCommand == null && !customCommandArray.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "No exact match, but have " + customCommandArray.size() + " commands");
                for (final CustomCommand c : customCommandArray) {
                    MyLog.i(CLS_NAME, "before order: " + c.getKeyphrase() + " ~ " + c.getScore());
                }
            }

            Collections.sort(customCommandArray, new Comparator<CustomCommand>() {
                @Override
                public int compare(final CustomCommand c1, final CustomCommand c2) {
                    return Double.compare(c2.getScore(), c1.getScore());
                }
            });

            if (DEBUG) {
                for (final CustomCommand c : customCommandArray) {
                    MyLog.i(CLS_NAME, "after order: " + c.getKeyphrase() + " ~ " + c.getScore());
                }
            }

            customCommand = customCommandArray.get(0);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "match: " + customCommand.getAlgorithm().name() + " ~ "
                        + customCommand.getKeyphrase() + " ~ " + customCommand.getUtterance()
                        + " ~ " + customCommand.getScore());
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CustomCommandHelper.class.getSimpleName(), then);
        }

        return customCommand != null;
    }

    /**
     * Extract all of the user's serialised {@link CustomCommand} from {@link DBCustomCommand} into
     * an array of {@link CustomCommandContainer}
     *
     * @param ctx the application context
     * @return an array of {@link CustomCommandContainer}
     */
    public ArrayList<CustomCommandContainer> getCustomCommands(@NonNull final Context ctx) {

        synchronized (lock) {

            final ArrayList<CustomCommandContainer> customCommandContainerArray;
            final DBCustomCommand dbCustomCommand = new DBCustomCommand(ctx);

            if (dbCustomCommand.databaseExists()) {
                customCommandContainerArray = dbCustomCommand.getKeyphrases();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: true: with " + customCommandContainerArray.size() + " commands.");
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: false");
                }

                customCommandContainerArray = new ArrayList<>();
            }

            return customCommandContainerArray;
        }
    }

    /**
     * Get any resolved {@link CustomCommand}. This object can be null.
     *
     * @return the {@link CustomCommand} or the null object
     */

    public CustomCommand getCommand() {
        return this.customCommand;
    }

    public void setCustomCommand(@Nullable final CustomCommand customCommand) {
        this.customCommand = customCommand;
    }

    /**
     * Get the {@link CC} that the custom command dictates
     *
     * @return the extracted {@link CC}
     */
    public CC getCommandConstant() {
        return this.customCommand.getCommandConstant();
    }


    /**
     * Insert a new {@link CustomCommand} in the {@link DBCustomCommand} synchronising with a basic
     * lock object in a vain attempt to prevent concurrency issues.
     *
     * @param ctx           the application context
     * @param customCommand to be set
     * @return true if the insertion was successful
     */
    public static Pair<Boolean, Long> setCommand(@NonNull final Context ctx, @NonNull final CustomCommand customCommand,
                                                 final long rowId) {

        synchronized (lock) {

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final String gsonString = gson.toJson(customCommand);

            final DBCustomCommand dbCustomCommand = new DBCustomCommand(ctx);

            final Pair<Boolean, Long> duplicatePair;
            if (rowId > -1) {
                duplicatePair = new Pair<>(true, rowId);
            } else {
                duplicatePair = commandExists(dbCustomCommand, customCommand);
            }

            return dbCustomCommand.insertPopulatedRow(customCommand.getKeyphrase(),
                    customCommand.getRegex(), gsonString, duplicatePair.first, duplicatePair.second);
        }
    }

    public static void deleteCustomCommand(@NonNull final Context ctx, final long rowId) {

        synchronized (lock) {
            final DBCustomCommand dbCustomCommand = new DBCustomCommand(ctx);
            dbCustomCommand.deleteRow(rowId);
        }

    }

    /**
     * Check if the keyphrase for the custom command already exists
     *
     * @param dbCustomCommand the {@link DBCustomCommand}
     * @param customCommand   the prepared {@link CustomCommand}
     * @return true if the keyphrase exists, false otherwise
     */
    private static Pair<Boolean, Long> commandExists(@NonNull final DBCustomCommand dbCustomCommand,
                                                     @NonNull final CustomCommand customCommand) {


        if (dbCustomCommand.databaseExists()) {

            final ArrayList<CustomCommandContainer> customCommandContainerArray = dbCustomCommand.getKeyphrases();

            if (UtilsList.notNaked(customCommandContainerArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "have commands");
                }

                final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                        UtilsLocale.stringToLocale(customCommand.getVRLocale()));

                final Locale loc = sl.getLocale();
                for (final CustomCommandContainer ccc : customCommandContainerArray) {
                    if (ccc.getKeyphrase().toLowerCase(loc).trim().matches(
                            customCommand.getKeyphrase().toLowerCase(loc).trim())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "keyphrase matched: " + ccc.getKeyphrase()
                                    + " ~ " + customCommand.getKeyphrase());
                        }
                        return new Pair<>(true, ccc.getRowId());
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "command is not a duplicate");
                }

            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "no commands");
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "databaseExists: false");
            }
        }

        return new Pair<>(false, -1L);
    }


    /**
     * Check if the keyphrase for the custom command already exists
     *
     * @param ctx           the application context
     * @param customCommand the prepared {@link CustomCommand}
     * @return true if the keyphrase exists, false otherwise
     */
    public boolean commandExists(@NonNull final Context ctx, @NonNull final CustomCommand customCommand) {

        final ArrayList<CustomCommandContainer> customCommandContainerArray = getCustomCommands(ctx);

        if (UtilsList.notNaked(customCommandContainerArray)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have commands");
            }

            final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                    UtilsLocale.stringToLocale(customCommand.getVRLocale()));

            final Locale loc = sl.getLocale();
            for (final CustomCommandContainer ccc : customCommandContainerArray) {
                if (ccc.getKeyphrase().toLowerCase(loc).trim().matches(
                        customCommand.getKeyphrase().toLowerCase(loc).trim())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "keyphrase matched: " + ccc.getKeyphrase()
                                + " ~ " + customCommand.getKeyphrase());
                    }
                    return true;
                }
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "command is not a duplicate");
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no commands");
            }
        }

        return false;
    }

    /**
     * Delete all of the {@link CustomCommand} in the {@link DBCustomCommand} synchronising with a basic
     * lock object in a vain attempt to prevent concurrency issues.
     *
     * @param ctx the application context
     * @return true if the process succeeded
     */
    public static boolean deleteAllCommands(@NonNull final Context ctx) {

        synchronized (lock) {
            final DBCustomCommand dbCustomCommand = new DBCustomCommand(ctx);
            return dbCustomCommand.deleteTable();
        }
    }

    /**
     * Delete all of the {@link CustomCommand} for the given package names.
     *
     * @param ctx          the application context
     * @param packageNames the package names for which commands should be deleted
     */
    public static void deleteCommandsForPackage(@NonNull final Context ctx,
                                                @NonNull final ArrayList<String> packageNames) {

        synchronized (lock) {

            final long then = System.nanoTime();

            final DBCustomCommand dbCustomCommand = new DBCustomCommand(ctx);

            if (dbCustomCommand.databaseExists()) {
                final ArrayList<CustomCommandContainer> customCommandContainerArray = dbCustomCommand.getKeyphrases();

                final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                final ArrayList<Long> rowIds = new ArrayList<>();
                CustomCommand customCommand;
                for (final CustomCommandContainer container : customCommandContainerArray) {

                    Intent remoteIntent = null;

                    try {
                        customCommand = gson.fromJson(container.getSerialised(), CustomCommand.class);

                        if (customCommand.getCustomAction() == CUSTOM_INTENT_SERVICE) {
                            remoteIntent = Intent.parseUri(customCommand.getIntent(), 0);
                        }
                    } catch (final URISyntaxException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "remoteIntent.parseUri: URISyntaxException");
                            e.printStackTrace();
                        }
                    } catch (final JsonSyntaxException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "gson.fromJson: JsonSyntaxException");
                            e.printStackTrace();
                        }
                    }

                    if (remoteIntent != null && packageNames.contains(remoteIntent.getPackage())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "adding " + remoteIntent.getPackage() + " to be deleted");
                        }

                        rowIds.add(container.getRowId());
                    }
                }

                if (!rowIds.isEmpty()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "deleting " + rowIds.size() + " commands");
                    }
                    dbCustomCommand.deleteRows(rowIds);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "no commands for packages");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: false");
                }
            }

            if (DEBUG) {
                MyLog.getElapsed("deleteCommandsForPackage", then);
            }
        }
    }
}
