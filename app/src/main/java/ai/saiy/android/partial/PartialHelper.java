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

package ai.saiy.android.partial;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.command.cancel.CancelPartial;
import ai.saiy.android.command.translate.TranslatePartial;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to detect commands within the partial voice results from the recognition provider. We can
 * use any early detection to initialise and/or prefetch resources, so should the full command be
 * resolved later in the final results, it should, in theory, execute more quickly.
 * <p/>
 * Created by benrandall76@gmail.com on 23/04/2016.
 */
public class PartialHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = PartialHelper.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 100L;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final List<Callable<Pair<Boolean, Integer>>> callableList = new ArrayList<>();
    private final CancelPartial cancelPartial;
    private volatile TranslatePartial translatePartial;
    private final IPartial iPartial;

    /**
     * Constructor
     * <p/>
     * Initialises the Strings used to analyse the partial results for triggers we need to react to.
     * The {@link SaiyResources} are released here.
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage}
     * @param iPartial the {@link IPartial} listener
     */
    public PartialHelper(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
                         @NonNull final IPartial iPartial) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }

        this.iPartial = iPartial;

        final SaiyResources sr = new SaiyResources(mContext, sl);
        cancelPartial = new CancelPartial(sr, sl);
        callableList.add(cancelPartial);

        switch (SPH.getDefaultTranslationProvider(mContext)) {
            case TranslationProvider.TRANSLATION_PROVIDER_BING:
                translatePartial = new TranslatePartial(sr, sl);
                callableList.add(translatePartial);
                break;
        }

        sr.reset();
    }

    /**
     * Utility method to detect the phrase during a recognition loop. Handling the initialisation
     * of localised resources can be slow, so we need to do this only once.
     *
     * @param partialResults the bundle of partial results
     */
    public void isPartial(@NonNull final Bundle partialResults) {

        new Thread() {
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                final List<Pair<Boolean, Integer>> resultList = new ArrayList<>(callableList.size());
                cancelPartial.setPartialData(partialResults);

                if (translatePartial != null) {
                    translatePartial.setPartialData(partialResults);
                }

                try {

                    final List<Future<Pair<Boolean, Integer>>> futures = executorService.invokeAll(callableList,
                            THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

                    for (final Future<Pair<Boolean, Integer>> future : futures) {
                        resultList.add(future.get());
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
                }

                for (final Pair<Boolean, Integer> result : resultList) {
                    if (result.first) {
                        switch (result.second) {
                            case Partial.CANCEL:
                                iPartial.onCancelDetected();
                                break;
                            case Partial.TRANSLATE:
                                iPartial.onTranslateDetected();
                                break;
                        }
                    }
                }
            }
        }.start();
    }

    public void shutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shutdown");
        }

        try {

            executorService.shutdown();

            if (!executorService.awaitTermination(THREADS_TIMEOUT, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shutdownNow: CancellationException");
                e.printStackTrace();
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shutdownNow: InterruptedException");
                e.printStackTrace();
            }
        } finally {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "shutdown: complete");
            }
        }
    }

    public boolean isShutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isShutdown");
        }
        return executorService.isShutdown();
    }
}
