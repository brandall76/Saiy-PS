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

package ai.saiy.android.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.File;

import ai.saiy.android.cache.speech.SpeechCachePrepare;
import ai.saiy.android.cache.speech.SpeechCacheResult;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Database class to hold compressed audio of Text to Speech Engine utterances. Utterances stored here,
 * will be streamed via {@link android.media.AudioTrack} object, to remove the necessity of fetching
 * network synthesis and the associated latency, along with being faster than initialising a
 * {@link android.speech.tts.TextToSpeech} object.
 * <p>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class DBSpeech extends SQLiteOpenHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBSpeech.class.getSimpleName();

    public static final long MAX_CACHE_SIZE = 20000000L;
    private static final long MAX_UNUSED_THRESHOLD = 2600000000L;
    private static final long VACUUM_THRESHOLD = 25L;

    private static final String DATABASE_NAME = "speech.db";
    private final String DATABASE_PATH;
    private static final String TABLE_SPEECH = "table_speech";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ENGINE_PACKAGE = "engine_package";
    private static final String COLUMN_VOICE_NAME = "voice_name";
    private static final String COLUMN_VOICE_LOCALE = "voice_locale";
    private static final String COLUMN_UTTERANCE = "utterance";
    private static final String COLUMN_BINARY = "binary";
    private static final String COLUMN_DATE = "last_used_date";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_ENGINE_PACKAGE, COLUMN_VOICE_NAME,
            COLUMN_VOICE_LOCALE, COLUMN_UTTERANCE, COLUMN_BINARY, COLUMN_DATE};

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SPEECH
            + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_ENGINE_PACKAGE
            + " text not null, "
            + COLUMN_VOICE_NAME
            + " text not null, "
            + COLUMN_VOICE_LOCALE
            + " text not null, "
            + COLUMN_UTTERANCE
            + " text not null, "
            + COLUMN_BINARY
            + " blob not null, "
            + COLUMN_DATE
            + " integer not null);";

    private SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public DBSpeech(@NonNull final Context mContext) {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }
        DATABASE_PATH = mContext.getDatabasePath(DATABASE_NAME).getPath();
    }

    /**
     * Open the database
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "open");
        }
        database = this.getWritableDatabase();
    }

    @Override
    public void close() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "close");
        }
        database.close();
    }

    @Override
    public void onCreate(final SQLiteDatabase dataBase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        dataBase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUpgrade");
            MyLog.w(CLS_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPEECH);
        onCreate(db);
    }

    /**
     * Check if the database exists
     *
     * @return true if it exists. False otherwise
     */
    public boolean databaseExists() {
        return new File(DATABASE_PATH).exists();
    }

    /**
     * Delete all entries in the current table
     */
    public boolean deleteTable() {

        try {

            open();

            if (database.isOpen()) {
                database.delete(TABLE_SPEECH, null, null);
                return true;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: database not open");
                }
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "deleteTable: finally closing");
                    }
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Delete a given entry
     *
     * @param rowId the row identifier
     * @return true if the deletion was successful
     */
    public boolean deleteEntry(final long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteEntry");
        }

        final long then = System.nanoTime();

        try {
            open();
            if (database.isOpen()) {
                database.delete(TABLE_SPEECH, COLUMN_ID + "=?", new String[]{String.valueOf(rowId)});
                return true;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteEntry: database not open");
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteEntry: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteEntry: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteEntry: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "deleteEntry: finally closing");
                    }
                    close();

                    if (DEBUG) {
                        MyLog.getElapsed(CLS_NAME, then);
                    }
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteEntry: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteEntry: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteEntry: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Check if an entry exists
     *
     * @param initEngine the package name of the Text to Speech Engine
     * @param voice      the {@link android.speech.tts.Voice}
     * @param utterance  the utterance
     * @return true if the entry exits. False otherwise
     */
    public boolean entryExists(@NonNull final String initEngine, @NonNull final String voice,
                               @NonNull final String utterance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "entryExists");
        }

        boolean exists = false;
        final long then = System.nanoTime();

        try {

            open();
            if (database.isOpen()) {

                final String whereClause = COLUMN_ENGINE_PACKAGE + "='" + initEngine
                        + "' AND " + COLUMN_VOICE_NAME + "='" + voice
                        + "' AND " + COLUMN_UTTERANCE + "='" + utterance.trim().replaceAll("[^a-zA-Z0-9]", "") + "'";

                final Cursor cursor = database.query(TABLE_SPEECH, new String[]{COLUMN_ID, COLUMN_ENGINE_PACKAGE,
                                COLUMN_VOICE_NAME, COLUMN_UTTERANCE, COLUMN_BINARY}, whereClause, null, null, null,
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "entryExists: true");
                    }

                    if (cursor.moveToFirst()) {
                        exists = true;
                    }

                    cursor.close();

                } else {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, ": entryExists: false");
                    }
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "entryExists: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "entryExists: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "entryExists: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "entryExists: finally closing");
                    }
                    close();

                    if (DEBUG) {
                        MyLog.getElapsed(CLS_NAME, then);
                    }
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "entryExists: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "entryExists: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "entryExists: Exception");
                    e.printStackTrace();
                }
            }
        }

        return exists;
    }

    /**
     * Get the compressed audio
     *
     * @param initEngine the initialised {@link android.speech.tts.TextToSpeech} package name
     * @param voice      the {@link android.speech.tts.Voice}
     * @param utterance  the utterance
     * @return a populated {@link SpeechCacheResult} or null if no entry exists
     */
    public SpeechCacheResult getBytes(@NonNull final String initEngine, @NonNull final String voice,
                                      @NonNull final String utterance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getBytes");
        }

        final long then = System.nanoTime();

        byte[] compressedBytes = null;

        long rowId = -1;

        try {

            open();

            if (database.isOpen()) {

                final String whereClause = COLUMN_ENGINE_PACKAGE + "='" + initEngine
                        + "' AND " + COLUMN_VOICE_NAME + "='" + voice
                        + "' AND " + COLUMN_UTTERANCE + "='" + utterance.trim().replaceAll("[^a-zA-Z0-9]", "") + "'";

                final Cursor cursor = database.query(TABLE_SPEECH, new String[]{COLUMN_ID, COLUMN_ENGINE_PACKAGE,
                                COLUMN_VOICE_NAME, COLUMN_UTTERANCE, COLUMN_BINARY}, whereClause, null, null, null,
                        null);

                if (cursor != null && cursor.getCount() > 0) {

                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "speechExists: true");
                    }

                    if (cursor.moveToFirst()) {
                        compressedBytes = cursor.getBlob(4);

                        final ContentValues values = new ContentValues();
                        values.put(COLUMN_DATE, String.valueOf(System.currentTimeMillis()));

                        database.update(TABLE_SPEECH, values, COLUMN_ID + "=?",
                                new String[]{String.valueOf(cursor.getLong(0))});

                        rowId = cursor.getLong(0);
                    }

                    cursor.close();

                } else {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "speechExists: false");
                    }
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getBytes: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getBytes: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getBytes: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getBytes: finally closing");
                    }
                    close();

                    if (DEBUG) {
                        MyLog.getElapsed(CLS_NAME, then);
                    }
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getBytes: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getBytes: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getBytes: Exception");
                    e.printStackTrace();
                }
            }
        }

        if (compressedBytes != null && compressedBytes.length > 0) {
            return new SpeechCacheResult(compressedBytes, rowId, true);
        } else {
            return new SpeechCacheResult(null, rowId, false);
        }
    }


    /**
     * Check if we should run the database maintenance, so it doesn't exceed a size that a user may
     * be concerned about.
     *
     * @param ctx the application context
     * @return true if the total size of the database exceeds {@link SPH#getMaxSpeechCacheSize(Context)}
     */
    public boolean shouldRunMaintenance(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, ": shouldRunMaintenance");
        }

        final long then = System.nanoTime();
        long dbSize = 0;

        try {
            open();
            if (database.isOpen()) {

                final String dbPath = database.getPath();
                if (dbPath != null) {

                    final File dbFile = new File(dbPath);
                    dbSize = dbFile.length();

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, ": Database size: " + dbSize);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, ": Database Path: null");
                    }
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shouldRunMaintenance: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shouldRunMaintenance: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shouldRunMaintenance: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "shouldRunMaintenance: finally closing");
                    }
                    close();

                    if (DEBUG) {
                        MyLog.getElapsed(CLS_NAME, then);
                    }
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "shouldRunMaintenance: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "shouldRunMaintenance: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "shouldRunMaintenance: Exception");
                    e.printStackTrace();
                }
            }
        }

        return dbSize > SPH.getMaxSpeechCacheSize(ctx);
    }

    /**
     * Check which entries have remained unused for a period longer than {@link #MAX_UNUSED_THRESHOLD}
     * and remove them.
     *
     * @param ctx the application context
     */
    public void runMaintenance(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "runMaintenance");
        }

        final long then = System.nanoTime();

        try {
            open();
            if (database.isOpen()) {
                final Cursor cursor = database.query(TABLE_SPEECH,
                        new String[]{COLUMN_ID, COLUMN_UTTERANCE, COLUMN_DATE}, null, null, null, null, null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    final long lastUsedApp = SPH.getLastUsed(ctx);
                    long lastUsedEntry;
                    long usageGap;
                    int deleteCount = 0;

                    while (!cursor.isAfterLast()) {

                        lastUsedEntry = cursor.getLong(2);
                        usageGap = lastUsedApp - lastUsedEntry;

                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "utterance: " + cursor.getString(1));
                            MyLog.d(CLS_NAME, "lastUsedApp: " + lastUsedApp);
                            MyLog.d(CLS_NAME, "lastUsedEntry: " + lastUsedEntry);
                            MyLog.d(CLS_NAME, "usageGap: " + usageGap);
                        }

                        if (usageGap > MAX_UNUSED_THRESHOLD) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "deleting: true");
                            }
                            database.delete(TABLE_SPEECH, COLUMN_ID + "=?",
                                    new String[]{String.valueOf(cursor.getLong(0))});
                            deleteCount++;
                        } else {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "deleting: false");
                            }
                        }

                        cursor.moveToNext();
                    }

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "deleteCount: " + deleteCount);
                    }

                    if (deleteCount > VACUUM_THRESHOLD) {
                        database.execSQL("VACUUM");
                    }

                    cursor.close();
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runMaintenance: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runMaintenance: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runMaintenance: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "runMaintenance: finally closing");
                    }
                    close();

                    if (DEBUG) {
                        MyLog.getElapsed(CLS_NAME, then);
                    }
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runMaintenance: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runMaintenance: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runMaintenance: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Insert a populated row
     *
     * @param scp the prepared {@link SpeechCachePrepare} object
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void insertRow(@NonNull final SpeechCachePrepare scp) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, ": insertRow");
        }

        final long then = System.nanoTime();

        if (scp.getCompressedAudio() != null && scp.getCompressedAudio().length > 0) {

            try {
                open();
                if (database.isOpen()) {

                    final ContentValues values = new ContentValues();

                    values.put(COLUMN_ENGINE_PACKAGE, scp.getEngine());
                    values.put(COLUMN_VOICE_NAME, scp.getVoice().getName());
                    values.put(COLUMN_UTTERANCE, scp.getUtterance().trim().replaceAll("[^a-zA-Z0-9]", ""));
                    values.put(COLUMN_VOICE_LOCALE, scp.getLocale());
                    values.put(COLUMN_BINARY, scp.getCompressedAudio());
                    values.put(COLUMN_DATE, String.valueOf(System.currentTimeMillis()));

                    final long insertId = database.insert(TABLE_SPEECH, null, values);
                    final Cursor cursor = database.query(TABLE_SPEECH, ALL_COLUMNS,
                            COLUMN_ID + " = " + insertId, null, null,
                            null, null);
                    cursor.moveToFirst();
                    cursor.close();
                }

            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertRow: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertRow: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertRow: Exception");
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (database.isOpen()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "insertRow: finally closing");
                        }
                        close();

                        if (DEBUG) {
                            MyLog.getElapsed(CLS_NAME, then);
                        }
                    }
                } catch (final IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "insertRow: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (final SQLException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "insertRow: SQLException");
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "insertRow: Exception");
                        e.printStackTrace();
                    }
                }
            }
        } else {

            if (DEBUG) {
                MyLog.w(CLS_NAME, ": insertRow: compression failed");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed("insertRow took - ", then);
        }
    }
}