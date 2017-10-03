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

package ai.saiy.android.files;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

/**
 * Class to handle writing audio data to a file whilst it is being recorded. This can be done
 * simultaneously to other audio functions, although performance can take a hit.
 * <p/>
 * Created by benrandall76@gmail.com on 14/02/2016.
 */
public class FileCreator {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = FileCreator.class.getSimpleName();

    private volatile int payloadSize;
    private volatile RandomAccessFile fWriter;
    private volatile String absolutePath;

    private final Object lock = new Object();
    private final int samplingRate;
    private final int nChannels;
    private final int bSamples;
    private File defaultFile;

    /**
     * Constructor
     *
     * @param mContext     the application context
     * @param nChannels    the number of channels
     * @param samplingRate the sampling rate in hertz
     * @param bSamples     the sampling rate
     */
    public FileCreator(@NonNull final Context mContext, final int nChannels, final int samplingRate,
                       final int bSamples) {
        this.nChannels = nChannels;
        this.samplingRate = samplingRate;
        this.bSamples = bSamples;

        final File filePath = UtilsFile.getPrivateDir(mContext);

        try {

            defaultFile = File.createTempFile(Constants.DEFAULT_FILE_PREFIX,
                    Constants.DEFAULT_AUDIO_FILE_SUFFIX, filePath);

            if (defaultFile.exists()) {
                final boolean deleteSuccess = defaultFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "file deleted successfully: " + deleteSuccess);
                }
            }

            this.absolutePath = defaultFile.getAbsolutePath();
            this.fWriter = getWriter();

        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Temp file: IOException");
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the file that has been created
     *
     * @return the created file
     */
    public File getDefaultFile() {
        return defaultFile;
    }

    /**
     * Pass the audio buffer data
     *
     * @param buff the audio buffer
     */
    public void passBuffer(@NonNull final byte[] buff) {

        if (fWriter != null) {

            synchronized (lock) {

                new Thread() {
                    public void run() {
                        try {
                            fWriter.write(buff);
                            payloadSize += buff.length;
                        } catch (final IOException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "IOException: recording is aborted");
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * Finish writing the file
     */
    public boolean completeWrite() {

        if (fWriter != null) {

            try {

                fWriter.seek(4);
                fWriter.writeInt(Integer.reverseBytes(36 + payloadSize));
                fWriter.seek(40);
                fWriter.writeInt(Integer.reverseBytes(payloadSize));
                fWriter.close();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "finished successfully with payload: " + payloadSize);
                }

                return true;

            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "IOException: completeFileWrite");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }


    /**
     * Constructs the wave header data
     */
    private RandomAccessFile getWriter() {

        RandomAccessFile myWriter = null;

        try {

            myWriter = new RandomAccessFile(absolutePath, "rws");

            myWriter.setLength(0);
            myWriter.writeBytes("RIFF");
            myWriter.writeInt(0);
            myWriter.writeBytes("WAVE");
            myWriter.writeBytes("fmt ");
            myWriter.writeInt(Integer.reverseBytes(16));
            myWriter.writeShort(Short.reverseBytes((short) 1));
            myWriter.writeShort(Short.reverseBytes((short) nChannels));
            myWriter.writeInt(Integer.reverseBytes(samplingRate));
            myWriter.writeInt(Integer.reverseBytes(samplingRate * bSamples * nChannels / 8));
            myWriter.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8)));
            myWriter.writeShort(Short.reverseBytes((short) bSamples));
            myWriter.writeBytes("data");
            myWriter.writeInt(0);

        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IOException: get writer");
                e.printStackTrace();
            }
        }

        return myWriter;
    }

    /**
     * Used to get information about the written file
     */
    private void getFileMeta() {

        if (absolutePath != null) {

            try {

                final MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(absolutePath);
                final String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                mmr.release();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recording duration: " + duration);
                }

            } catch (final RuntimeException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "RuntimeException: completeFileWrite");
                    e.printStackTrace();
                }
            }
        }
    }
}