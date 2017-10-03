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

package ai.saiy.android.processing;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

/**
 * If you are here to read more about the quantum methods used to resolve commands, I'm afraid
 * you are going to be a little disappointed...
 * <p>
 * In actual fact, the main {@link Quantum} class is built upon the standard Android AsyncTask. It
 * seemed so boring that such an integral part of the application functioned in this way, that I
 * decided to implement this class purely so I could change the names of the default methods, in
 * an attempt to make it appear more exciting. Sorry....
 * <p>
 * So, this really is the main Class for resolving and actioning a spoken command.
 * It uses the standard AsyncTask as it pretty much ticks all of the boxes for what is required as a base
 * at this point in the application process - That would be the ability to call the UI thread
 * through {@link #onProgressUpdate(EntangledPair...)} (whilst we continue background processing elsewhere)
 * and also in {@link #onPostExecute(Qubit)}, if required. Handy.
 * <p>
 * The {@link Quantum} class will inevitably become very long, as each command is assigned an Enum identifier
 * and we use the case/switch statement to enter the correct one, where analysis relative to the command
 * is performed. Regardless or not of whether the response is resolved externally, we still need to know
 * how to 'behave' at the end.
 * <p>
 * I have gone through many iterations of how best to construct what could be considered as the global
 * command resolver, and this is the fastest so far and remains relatively readable, despite the size.
 * <p>
 * Making up any lost performance is better done in the command detection process, preparation and
 * sorting classes themselves, rather than {@link Quantum}, where there may be only minimal gains to be had.
 * <p>
 * The performance of a large number of Enum constants (and extended defaults) over integers is negligible
 * and outweighed certainly by type safety, but perhaps also negated by the resource of any equivalently
 * functioning implementation.
 * <p>
 * Created by benrandall76@gmail.com on 18/09/2016.
 */

abstract class Tunnelling extends AsyncTask<CommandRequest, EntangledPair, Qubit> {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Tunnelling.class.getSimpleName();

    private static final long COMPUTING_DELAY = 1000L;
    public static final long CLIPBOARD_DELAY = 175L;

    protected int result = Outcome.SUCCESS;
    CC COMMAND = CC.COMMAND_UNKNOWN;
    private final Timer timer = new Timer();
    protected final long then;
    protected boolean secure;

    protected Locale vrLocale;
    protected Locale ttsLocale;
    protected SupportedLanguage sl;

    CustomCommandHelper cch;

    protected final LocalRequest request;
    protected final Context mContext;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    Tunnelling(@NonNull final Context mContext) {
        this.mContext = mContext.getApplicationContext();
        this.request = new LocalRequest(this.mContext);
        then = System.nanoTime();
    }

    /**
     * Set a countdown timer to show the processing notification, so they user doesn't think nothing is
     * happening should there be a delay - caused by network latency perhaps.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPreExecute");
        }

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "timerTask: show computing");
                }
                onProgressUpdate(new EntangledPair(Position.SHOW_COMPUTING, CC.COMMAND_UNKNOWN));
            }
        };

        timer.schedule(timerTask, COMPUTING_DELAY);
    }

    @Override
    protected Qubit doInBackground(final CommandRequest... commandRequestArray) {

        final CommandRequest cr = commandRequestArray[0];
        vrLocale = cr.getVRLocale(mContext);
        ttsLocale = cr.getTTSLocale(mContext);
        sl = cr.getSupportedLanguage();

        request.setVRLocale(vrLocale);
        request.setTTSLocale(ttsLocale);

        cr.getResultsArray().removeAll(Collections.singleton(""));
        cr.getResultsArray().removeAll(Collections.<String>singleton(null));

        return doTunnelling(cr);
    }

    protected abstract Qubit doTunnelling(final CommandRequest commandRequest);

    @Override
    protected void onProgressUpdate(final EntangledPair... entangledPairArray) {
        onEntanglement(entangledPairArray[0]);
    }

    protected abstract void onEntanglement(final EntangledPair entangledPair);

    @Override
    protected void onPostExecute(final Qubit qubit) {
        cancelTimer();
        onSuperposition(qubit);
    }

    protected abstract void onSuperposition(final Qubit qubit);

    /**
     * Helper method to remove clutter when checking to see if the Pair contains any instructions.
     *
     * @param entangledPair the {@link EntangledPair} containing possible instructions
     * @return true if instructions are present
     */
    boolean validatePosition(final EntangledPair entangledPair) {
        return entangledPair != null && entangledPair.getPosition() != null && entangledPair.getCC() != null;
    }

    /**
     * Helper method to remove clutter when checking to see if the Pair contains any instructions.
     *
     * @param qubit the {@link Qubit} containing possible instructions
     * @return true if instructions are present
     */
    boolean validateQubit(final Qubit qubit) {
        return qubit != null;
    }

    /**
     * Cancel the notification timer, as it's no longer needed.
     */
    private void cancelTimer() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelTimer");
        }

        try {
            timer.cancel();
            timer.purge();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "cancelTimer: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "cancelTimer: Exception");
                e.printStackTrace();
            }
        }
    }
}
