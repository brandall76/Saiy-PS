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

package ai.saiy.android.nlu.local;

import android.content.Context;
import android.support.annotation.NonNull;

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
import ai.saiy.android.algorithms.soundex.SoundexHelper;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 11/08/2016.
 */

public class AlgorithmicResolver {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AlgorithmicResolver.class.getSimpleName();

    private final long THREADS_TIMEOUT;
    public static final long THREADS_TIMEOUT_500 = 500L;
    public static final long THREADS_TIMEOUT_2000 = 2000L;

    private final Context mContext;
    private final ArrayList<String> inputData;
    private final ArrayList<?> genericData;
    private final Algorithm[] algorithms;
    private final Locale loc;
    private final ExecutorService executorService;
    private final List<Callable<Object>> callableList;
    private AlgorithmicContainer algorithmicContainer = null;
    private final boolean precision;

    public AlgorithmicResolver(@NonNull final Context mContext, @NonNull final Algorithm[] algorithms,
                               @NonNull final Locale loc, @NonNull final ArrayList<String> inputData,
                               @NonNull final ArrayList<?> genericData, final long timeout, final boolean precision) {
        this.mContext = mContext;
        this.genericData = genericData;
        this.inputData = inputData;
        this.algorithms = algorithms;
        this.loc = loc;
        this.THREADS_TIMEOUT = timeout;
        this.precision = precision;

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        callableList = new ArrayList<>(this.algorithms.length);
    }

    public AlgorithmicContainer resolve() {

        final long then = System.nanoTime();

        for (final Algorithm algorithm : algorithms) {

            switch (algorithm) {

                case JARO_WINKLER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: JARO_WINKLER");
                    }

                    callableList.add(new JaroWinklerHelper(mContext, genericData, inputData, loc));
                    break;
                case LEVENSHTEIN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: LEVENSHTEIN");
                    }

                    callableList.add(new LevenshteinHelper(mContext, genericData, inputData, loc));
                    break;
                case SOUNDEX:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: SOUNDEX");
                    }

                    callableList.add(new SoundexHelper(mContext, genericData, inputData, loc));
                    break;
                case METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: METAPHONE");
                    }

                    callableList.add(new MetaphoneHelper(mContext, genericData, inputData, loc));
                    break;
                case DOUBLE_METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: DOUBLE_METAPHONE");
                    }

                    callableList.add(new DoubleMetaphoneHelper(mContext, genericData, inputData, loc));
                    break;
                case FUZZY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: FUZZY");
                    }

                    callableList.add(new FuzzyHelper(mContext, genericData, inputData, loc));
                    break;
                case NEEDLEMAN_WUNCH:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: NEEDLEMAN_WUNCH");
                    }

                    callableList.add(new NeedlemanWunschHelper(mContext, genericData, inputData, loc));
                    break;
                case MONGE_ELKAN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: MONGE_ELKAN");
                    }

                    callableList.add(new MongeElkanHelper(mContext, genericData, inputData, loc));
                    break;

            }
        }

        final ArrayList<AlgorithmicContainer> algorithmicContainerArray = new ArrayList<>();

        try {

            final List<Future<Object>> futures = executorService.invokeAll(callableList,
                    THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

            for (final Future<Object> future : futures) {
                algorithmicContainerArray.add((AlgorithmicContainer) future.get());
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

        if (!algorithmicContainerArray.isEmpty()) {
            algorithmicContainerArray.removeAll(Collections.<AlgorithmicContainer>singleton(null));
            if (DEBUG) {
                MyLog.i(CLS_NAME, "algorithms returned " + algorithmicContainerArray.size() + " matches");
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "Potentials: " + a.getAlgorithm().name() + " ~ "
                            + a.getGenericMatch() + " ~ " + a.getInput() + " ~ "
                            + a.getScore());
                }
            }

            AlgorithmicContainer ac;
            final ListIterator<AlgorithmicContainer> itr = algorithmicContainerArray.listIterator();

            while (itr.hasNext()) {
                ac = itr.next();
                if (ac == null) {
                    itr.remove();
                } else {

                    if (ac.isExactMatch()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "exact match: " + ac.getAlgorithm().name() + " ~ "
                                    + ac.getGenericMatch() + " ~ " + ac.getInput() + " ~ "
                                    + ac.getScore());
                        }
                        algorithmicContainer = ac;
                        break;
                    }
                }
            }
        }

        if (algorithmicContainer == null && !algorithmicContainerArray.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "No exact match, but have " + algorithmicContainerArray.size() + " commands");
                for (final AlgorithmicContainer c : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "before order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
            }

            Collections.sort(algorithmicContainerArray, new Comparator<AlgorithmicContainer>() {
                @Override
                public int compare(final AlgorithmicContainer a1, final AlgorithmicContainer a2) {
                    return Double.compare(a2.getScore(), a1.getScore());
                }
            });

            if (DEBUG) {
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "after order: " + a.getGenericMatch() + " ~ " + a.getScore());
                }
            }

            algorithmicContainer = algorithmicContainerArray.get(0);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "match: " + algorithmicContainer.getAlgorithm().name() + " ~ "
                        + algorithmicContainer.getGenericMatch() + " ~ " + algorithmicContainer.getInput()
                        + " ~ " + algorithmicContainer.getScore());
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return algorithmicContainer;
    }
}
