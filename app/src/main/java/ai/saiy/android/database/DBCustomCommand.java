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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import ai.saiy.android.api.request.Regex;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomCommandContainer;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class DBCustomCommand extends SQLiteOpenHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBCustomCommand.class.getSimpleName();

    private static final String DATABASE_NAME = "customCommands.db";
    private final String DATABASE_PATH;
    public static final String TABLE_CUSTOM_COMMANDS = "custom_commands";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEYPHRASE = "keyphrase";
    public static final String COLUMN_REGEX = "regex";
    public static final String COLUMN_SERIALISED = "serialised";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_KEYPHRASE, COLUMN_REGEX, COLUMN_SERIALISED};

    private static final String DATABASE_CREATE = "create table " + TABLE_CUSTOM_COMMANDS
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_KEYPHRASE + " text not null, "
            + COLUMN_REGEX + " text not null, "
            + COLUMN_SERIALISED + " text not null);";

    private SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public DBCustomCommand(@NonNull final Context mContext) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_COMMANDS);
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
                database.delete(TABLE_CUSTOM_COMMANDS, null, null);
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
     * Insert a row, separating the command phrase and serialised class.
     *
     * @param keyphrase   the keyphrase
     * @param regex       the regular expression to be used one of {@link Regex}
     * @param serialised  the serialised class
     * @param isDuplicate true if a command is being replaced
     * @param rowId       the row id of the command to be replaced
     * @return true if the insertion was successful. False otherwise
     */
    public Pair<Boolean, Long> insertPopulatedRow(@NonNull final String keyphrase, @NonNull final Regex regex,
                                                  @NonNull final String serialised, final boolean isDuplicate,
                                                  final long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertPopulatedRow: duplicate: " + isDuplicate + " " + rowId);
        }

        boolean success = false;
        long insertId = -1;

        try {

            open();

            if (database.isOpen()) {

                final ContentValues values = new ContentValues();
                values.put(COLUMN_KEYPHRASE, keyphrase);
                values.put(COLUMN_REGEX, regex.name());
                values.put(COLUMN_SERIALISED, serialised);

                insertId = database.insert(TABLE_CUSTOM_COMMANDS, null, values);
                final Cursor cursor = database.query(TABLE_CUSTOM_COMMANDS, ALL_COLUMNS,
                        COLUMN_ID + " = " + insertId, null, null,
                        null, null);
                cursor.moveToFirst();
                cursor.close();
                success = true;
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "insertPopulatedRow: finally closing");
                    }
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: Exception");
                    e.printStackTrace();
                }
            }
        }

        if (isDuplicate) {
            deleteRow(rowId);
        }

        return new Pair<>(success, insertId);

    }

    /**
     * Delete rows from the database
     *
     * @param rowIDs the row identifiers
     */
    public void deleteRows(final ArrayList<Long> rowIDs) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteRows");
        }

        try {
            open();
            if (database.isOpen()) {

                final String where = COLUMN_ID + " IN ("
                        + TextUtils.join(",", Collections.nCopies(rowIDs.size(), "?")) + ")";

                final String[] rowIdArray = new String[rowIDs.size()];

                for (int i = 0; i < rowIDs.size(); i++) {
                    rowIdArray[i] = String.valueOf(rowIDs.get(i));
                }

                final int deleted = database.delete(TABLE_CUSTOM_COMMANDS, where, rowIdArray);

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "deleteRow count: " + deleted);
                }
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRows: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRows: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRows: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRows: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRows: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRows: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete a row from the database
     *
     * @param rowID the row identifier
     */
    public void deleteRow(final long rowID) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteRow: " + rowID);
        }

        try {
            open();
            if (database.isOpen()) {
                final int deleteResult = database.delete(TABLE_CUSTOM_COMMANDS, COLUMN_ID + "=?",
                        new String[]{String.valueOf(rowID)});

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "deleteResult: " + deleteResult);
                }
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get all keyphrases from the database, including the corresponding row identifier and
     * serialised command data.
     *
     * @return the {@link Pair} keyphrase and row identifier
     */
    public ArrayList<CustomCommandContainer> getKeyphrases() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getKeyphrases");
        }

        final ArrayList<CustomCommandContainer> keyPhrases = new ArrayList<>();

        try {

            open();

            if (database.isOpen()) {

                final Cursor cursor = database.query(TABLE_CUSTOM_COMMANDS,
                        ALL_COLUMNS, null, null, null, null, null);

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    keyPhrases.add(new CustomCommandContainer(cursor.getLong(0), cursor.getString(1),
                            cursor.getString(2), cursor.getString(3)));
                    cursor.moveToNext();
                }

                cursor.close();
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getKeyphrases: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getKeyphrases: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getKeyphrases: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getKeyphrases: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getKeyphrases: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getKeyphrases: Exception");
                    e.printStackTrace();
                }
            }
        }

        return keyPhrases;
    }

    /**
     * Get the serialisable class
     *
     * @param rowId the row identifier
     * @return the serialised {@link CustomCommand}
     */
    public String getSerialisable(final long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSerialisable");
        }

        String serialisable = null;

        try {
            open();

            if (database.isOpen()) {
                final Cursor cursor = database.query(TABLE_CUSTOM_COMMANDS, new String[]{COLUMN_SERIALISED}, COLUMN_ID + "=?",
                        new String[]{String.valueOf(rowId)}, null, null, null);

                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    serialisable = cursor.getString(0);
                }

                cursor.close();
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSerialisable: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSerialisable: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSerialisable: Exception");
                e.printStackTrace();
            }
        } finally {

            try {
                if (database.isOpen()) {
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getSerialisable: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getSerialisable: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getSerialisable: Exception");
                    e.printStackTrace();
                }
            }
        }

        return serialisable;
    }
}