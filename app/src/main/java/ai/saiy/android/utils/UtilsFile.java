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

package ai.saiy.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Pair;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Utility class of handy file method. Static for ease of access
 * <p>
 * Created by benrandall76@gmail.com on 10/09/2016.
 */

public class UtilsFile {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsFile.class.getSimpleName();

    private static final String FILE_PROVIDER = "ai.saiy.android.fileprovider";
    private static final String SOUND_EFFECT_DIR = "/se/";

    /**
     * Prevent instantiation
     */
    public UtilsFile() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Convert a raw resource to a file on the private storage
     *
     * @param ctx        the application context
     * @param resourceId the resource identifier
     * @return the file or null if the process failed
     */
    public static File oggToCacheAndGrant(@NonNull final Context ctx, final int resourceId, final String packageName) {

        final String cachePath = getPrivateDirPath(ctx);

        if (UtilsString.notNaked(cachePath)) {

            final String name = ctx.getResources().getResourceEntryName(resourceId);

            final File file = resourceToFile(ctx, resourceId, new File(cachePath + SOUND_EFFECT_DIR + name
                    + "." + Constants.OGG_AUDIO_FILE_SUFFIX));

            if (file != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "wavToCacheAndGrant file exists: " + file.exists());
                    MyLog.i(CLS_NAME, "wavToCacheAndGrant file path: " + file.getAbsolutePath());
                }

                final Uri contentUri = FileProvider.getUriForFile(ctx, FILE_PROVIDER, file);
                ctx.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                return file;
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "wavToCacheAndGrant file null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "wavToCacheAndGrant failed to get any path");
            }
        }

        return null;

    }

    /**
     * Utility to copy the contents of a raw resource to a file
     *
     * @param ctx        the application context
     * @param resourceId the resource identifier
     * @param file       the destination file
     * @return the completed file or null if the process failed
     */
    private static File resourceToFile(@NonNull final Context ctx, final int resourceId,
                                       @NonNull final File file) {

        try {
            FileUtils.copyInputStreamToFile(ctx.getResources().openRawResource(resourceId), file);
            return file;
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "resourceToFile IOException");
                e.printStackTrace();
            }
        } catch (final Resources.NotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "resourceToFile NotFoundException");
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Attempt to get a directory that does not require permission to read/write. This should be
     * simple but @see https://code.google.com/p/android/issues/detail?id=81357
     *
     * @param ctx the application context
     * @return the directory or null if all efforts fail.
     */
    public static File getPrivateDir(@NonNull final Context ctx) {

        Pair<Boolean, File> dirPair = getExternalFilesDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        dirPair = getExternalCacheDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        dirPair = getCacheDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        return null;
    }

    /**
     * Attempt to get a directory location that does not require permission to read/write. This should be
     * simple but @see https://code.google.com/p/android/issues/detail?id=81357
     *
     * @param ctx the application context
     * @return the absolute path of the location or null if all efforts fail.
     */
    public static String getPrivateDirPath(@NonNull final Context ctx) {

        final File file = getPrivateDir(ctx);

        if (file != null) {
            return file.getAbsolutePath();
        }

        return null;
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getExternalFilesDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ContextCompat.getExternalFilesDirs(ctx, null)[0]);

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getExternalFilesDir(null));
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getExternalFilesDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalFilesDir: IOException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalFilesDir: IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getExternalFilesDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getExternalFilesDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getExternalCacheDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ContextCompat.getExternalCacheDirs(ctx)[0]);

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getExternalCacheDir());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getExternalCacheDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalCacheDir: IOException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalCacheDir: IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getExternalCacheDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getExternalCacheDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getCacheDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ctx.getCacheDir());

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getCacheDir());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getCacheDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getCacheDir: IOException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getCacheDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getCacheDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Get a default temporary file to write audio to
     *
     * @param ctx the application context
     * @return the created file
     */
    public static File getTempAudioFile(@NonNull final Context ctx) {

        final File tempFile = getPrivateDir(ctx);

        if (tempFile != null) {

            try {
                return File.createTempFile(Constants.DEFAULT_AUDIO_FILE_PREFIX,
                        "." + Constants.DEFAULT_AUDIO_FILE_SUFFIX, tempFile);
            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTempAudioFile: IOException");
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
