/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.nlu.local;

import android.content.Context;
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

import ai.saiy.android.R;
import ai.saiy.android.command.battery.Battery;
import ai.saiy.android.command.cancel.Cancel;
import ai.saiy.android.command.emotion.Emotion;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.hotword.Hotword;
import ai.saiy.android.command.pardon.Pardon;
import ai.saiy.android.command.songrecognition.SongRecognition;
import ai.saiy.android.command.spell.Spell;
import ai.saiy.android.command.tasker.Tasker;
import ai.saiy.android.command.translate.Translate;
import ai.saiy.android.command.username.UserName;
import ai.saiy.android.command.vocalrecognition.VocalRecognition;
import ai.saiy.android.command.wolframalpha.WolframAlpha;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

public final class InitStrings {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = InitStrings.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 1000L;

    private final List<Callable<ArrayList<Pair<CC, Float>>>> callableList;

    private static String testString;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public InitStrings(@NonNull final Context mContext) {

        callableList = new ArrayList<>();

        if (testString != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "test string initialised. Returning");
            }
            return;
        }

        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(mContext));
        final SaiyResources sr = new SaiyResources(mContext, sl);

        testString = sr.getString(R.string.test_string);

        final ArrayList<String> voiceData = new ArrayList<>();
        final float[] confidence = new float[]{};

        callableList.add(new Cancel(sr, sl, voiceData, confidence));
        callableList.add(new Spell(sr, sl, voiceData, confidence));
        callableList.add(new Translate(sr, sl, voiceData, confidence));
        callableList.add(new Pardon(sr, sl, voiceData, confidence));
        callableList.add(new UserName(sr, sl, voiceData, confidence));
        callableList.add(new SongRecognition(sr, sl, voiceData, confidence));
        callableList.add(new Battery(sr, sl, voiceData, confidence));
        callableList.add(new WolframAlpha(sr, sl, voiceData, confidence));
        callableList.add(new Tasker(sr, sl, voiceData, confidence));
        callableList.add(new Emotion(sr, sl, voiceData, confidence));
        callableList.add(new Hotword(sr, sl, voiceData, confidence));
        callableList.add(new VocalRecognition(sr, sl, voiceData, confidence));
        sr.reset();
    }

    public void init() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "init: availableProcessors: " + Runtime.getRuntime().availableProcessors());
        }

        final long then = System.nanoTime();

        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {

            final List<Future<ArrayList<Pair<CC, Float>>>> futures = executorService.invokeAll(callableList,
                    THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

            for (final Future<ArrayList<Pair<CC, Float>>> future : futures) {
                future.get();
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

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
    }
}
