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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *      Copyright 2011-2016, Institute of Cybernetics at Tallinn University of Technology
 *
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *      contributor license agreements.  See the NOTICE file distributed with
 *      this work for additional information regarding copyright ownership.
 *      The ASF licenses this file to You under the Apache License, Version 2.0
 *      (the "License"); you may not use this file except in compliance with
 *      the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package ai.saiy.android.audio.pause;

import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 15/02/2016.
 * <p/>
 * Class to detect when a user has stopped speaking adapted from the excellent library from the
 * author below.
 *
 * @author Kaarel Kaljurand
 */
public class PauseDetector {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = PauseDetector.class.getSimpleName();

    private final int PAUSE_THRESHOLD = 7;
    public static final long DEFAULT_PAUSE_IGNORE_TIME = 4000;
    private final int MAX_RECORDING_LENGTH = 120;

    private long pauseCheck;
    private final long pauseIgnoreTime;

    private final short RESOLUTION_IN_BYTES = 2;
    private final int mOneSec;

    // TODO: use: mRecording.length instead
    // TODO: synchronisation and atomics
    private volatile int mRecordedLength = 0;
    private final PauseListener pauseListener;
    private volatile boolean hasDetected;
    private final byte[] mRecording;
    private volatile double mAvgEnergy = 0;
    private final int maxSize;

    /**
     * Constructor for the PauseDetector
     *
     * @param pauseListener  the listener connected to the recognition object
     * @param sampleRateInHz the sampling rate in hertz
     * @param nChannels      the number of channels
     */
    public PauseDetector(final PauseListener pauseListener, final int sampleRateInHz,
                         final int nChannels, final long pauseIgnoreTime) {
        this.pauseListener = pauseListener;
        this.pauseIgnoreTime = pauseIgnoreTime;

        mOneSec = RESOLUTION_IN_BYTES * nChannels * sampleRateInHz;
        mRecording = new byte[mOneSec * MAX_RECORDING_LENGTH];
        hasDetected = false;
        maxSize = mRecording.length;
    }

    /**
     * Start the pause detection
     */
    public void begin() {
        pauseCheck = System.currentTimeMillis();
    }

    /**
     * Add information from the buffer
     *
     * @param buffer           the audio buffer
     * @param bufferReadResult the previous read result
     */
    public void addLength(final byte[] buffer, final int bufferReadResult) {

        synchronized (this) {

            if (!hasDetected) {
                new Thread() {
                    public void run() {

                        mRecordedLength += buffer.length;

                        if (mRecordedLength <= maxSize) {
                            System.arraycopy(buffer, 0, mRecording, mRecordedLength, bufferReadResult);
                        } else {
                            hasDetected = true;
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * Check if a pause has been detected
     */
    public boolean hasDetected() {
        return hasDetected;
    }

    /**
     * Monitor the audio data
     */
    public void monitor() {

        synchronized (this) {

            if (!hasDetected) {

                new Thread() {
                    public void run() {

                        double pauseScore = getPauseScore();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Pause score: " + pauseScore);
                        }

                        if (System.currentTimeMillis() - pauseCheck < pauseIgnoreTime) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "PAUSE_IGNORE_TIME: too early");
                            }
                        } else {
                            if (pauseScore > PAUSE_THRESHOLD) {
                                hasDetected = true;
                                pauseListener.onPauseDetected();
                            }
                        }
                    }
                }.start();

                if (DEBUG) {
                    int running = 0;
                    for (final Thread thread : Thread.getAllStackTraces().keySet()) {
                        if (thread.getState() == Thread.State.RUNNABLE) {
                            running++;
                        }
                    }

                    MyLog.v(CLS_NAME, "Threads running: " + running);
                }
            }
        }
    }

    /**
     * In order to calculate if the user has stopped speaking we take the
     * data from the last second of the recording, map it to a number
     * and compare this number to the numbers obtained previously. We
     * return a confidence score (0-INF) of a longer pause having occurred in the
     * speech input.
     * <p/>
     * TODO: base the implementation on some well-known technique.
     * TODO: behaves very differently depending on the {@link android.media.MediaRecorder.AudioSource}
     *
     * @return positive value which the caller can use to determine if there is a pause
     */
    private double getPauseScore() {

        long t2 = getRms(mRecordedLength, mOneSec);

        if (t2 == 0) {
            return 0;
        }

        double t = mAvgEnergy / t2;
        mAvgEnergy = (2 * mAvgEnergy + t2) / 3;

        return t;
    }

    private long getRms(int end, int span) {
        int begin = end - span;
        if (begin < 0) {
            begin = 0;
        }

        if (0 != (begin % 2)) {
            begin++;
        }

        long sum = 0;
        for (int i = begin; i < end; i += 2) {
            short curSample = getShort(mRecording[i], mRecording[i + 1]);
            sum += curSample * curSample;
        }
        return sum;
    }

    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }
}
