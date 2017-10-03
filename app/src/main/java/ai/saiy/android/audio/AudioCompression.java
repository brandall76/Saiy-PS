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

package ai.saiy.android.audio;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ai.saiy.android.cache.speech.IAudioCompression;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

/**
 * Class to handle compression of audio bytes.
 * <p>
 * Created by benrandall76@gmail.com on 27/04/2016.
 */
public class AudioCompression {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AudioCompression.class.getSimpleName();

    /**
     * Compress the audio bytes using GZIP
     *
     * @param listener the {@link IAudioCompression}
     * @param bytes    the byte array to compress
     */
    public static void compressBytes(@NonNull final IAudioCompression listener, @NonNull final byte[] bytes) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "compressBytes: bytes size: " + bytes.length);
        }

        final long then = System.nanoTime();

        ByteArrayOutputStream byteArrayOutputStream = null;
        GZIPOutputStream gzipOutputStream = null;
        byte[] returnBytes = null;

        try {

            byteArrayOutputStream = new ByteArrayOutputStream(bytes.length);
            gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream) {
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };

            gzipOutputStream.write(bytes);

        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "compressBytes IOException1");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "compressBytes NullPointerException1");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "compressBytes Exception1");
                e.printStackTrace();
            }
        } finally {

            try {

                if (gzipOutputStream != null) {
                    gzipOutputStream.close();
                }

                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();

                    returnBytes = byteArrayOutputStream.toByteArray();

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "compressBytes returnBytes size: " + returnBytes.length);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "compressBytes byteArrayOutputStream: null");
                    }
                }

            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "compressBytes IOException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "compressBytes NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "compressBytes Exception");
                    e.printStackTrace();
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        listener.onCompressionCompleted(returnBytes);
    }

    /**
     * Decompress the audio bytes
     *
     * @param ctx   the application context
     * @param bytes the array of audio bytes
     * @param rowId the row id of the {@link DBSpeech} they were stored in
     * @return an array of decompressed audio bytes
     */
    public static byte[] decompressBytes(final Context ctx, final byte[] bytes, final long rowId) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "decompressBytes bytes size: " + bytes.length);
        }

        final long then = System.nanoTime();

        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] returnBytes = null;

        try {

            byteArrayInputStream = new ByteArrayInputStream(bytes);
            gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];

            int count;
            while ((count = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, count);
            }

        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytes IOException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytes NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytes Exception");
                e.printStackTrace();
            }

        } finally {

            try {

                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }

                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }

                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();

                    returnBytes = byteArrayOutputStream.toByteArray();

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "decompressBytes returnBytes size: " + returnBytes.length);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "decompressBytes byteArrayOutputStream: null");
                    }
                }
            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytes IOException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytes NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytes Exception");
                    e.printStackTrace();
                }
            }
        }

        if (returnBytes == null || returnBytes.length <= 0) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "decompressBytes null or empty: deleting entry");
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    final DBSpeech dbs = new DBSpeech(ctx);
                    dbs.deleteEntry(rowId);
                }
            });
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return returnBytes;
    }

    /**
     * Decompress the audio bytes to a file
     *
     * @param ctx   the application context
     * @param bytes the array of audio bytes
     * @param rowId the row id of the {@link DBSpeech} they were stored in
     * @return a file containing the decompressed audio bytes
     */
    public static File decompressBytesToFile(final Context ctx, final byte[] bytes, final long rowId) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "decompressBytesToFile bytes size: " + bytes.length);
        }

        final long then = System.nanoTime();

        File tempAudioFile = null;
        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] returnBytes = null;

        try {

            byteArrayInputStream = new ByteArrayInputStream(bytes);
            gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];

            int count;
            while ((count = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, count);
            }

        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytesToFile IOException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytesToFile NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "decompressBytesToFile Exception");
                e.printStackTrace();
            }

        } finally {

            try {

                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }

                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }

                if (byteArrayOutputStream != null) {

                    returnBytes = byteArrayOutputStream.toByteArray();

                    if (returnBytes != null && returnBytes.length > 0) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "decompressBytesToFile returnBytes size: " + returnBytes.length);
                        }

                        tempAudioFile = UtilsFile.getTempAudioFile(ctx);

                        if (tempAudioFile != null) {
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "decompressBytesToFile: file name: " + tempAudioFile.getName());
                            }

                            final FileOutputStream fos = new FileOutputStream(tempAudioFile);
                            byteArrayOutputStream.writeTo(fos);
                            byteArrayOutputStream.close();
                            fos.write(bytes);
                            fos.flush();
                            fos.close();
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "tempAudioFile null");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "decompressBytesToFile returnBytes null or empty");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "decompressBytesToFile byteArrayOutputStream: null");
                    }
                }
            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytesToFile IOException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytesToFile NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "decompressBytesToFile Exception");
                    e.printStackTrace();
                }
            }
        }

        if (returnBytes == null || returnBytes.length <= 0) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "decompressBytesToFile null or empty: deleting entry");
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    final DBSpeech dbs = new DBSpeech(ctx);
                    dbs.deleteEntry(rowId);
                }
            });
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return tempAudioFile;
    }
}
