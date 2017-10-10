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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.http;

import android.net.ParseException;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to send a file of audio data to the BV API
 * <p>
 * Created by benrandall76@gmail.com on 09/06/2016.
 */
public class BVSendFile {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BVSendFile.class.getSimpleName();

    private static final String RECORDING_URL = "https://apiv4.beyondverbal.com/v4/recording/";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    private HttpsURLConnection urlConnection;
    private OutputStream outputStream;
    private FileInputStream fileInputStream;

    private final String token;
    private final String recordingId;
    private final File file;

    public BVSendFile(@NonNull final String token, @NonNull final String recordingId,
                      @NonNull final File file) {
        this.token = token;
        this.recordingId = recordingId;
        this.file = file;
    }

    public void stream() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stream");
        }

        try {

            urlConnection = (HttpsURLConnection) new URL(RECORDING_URL + recordingId).openConnection();
            urlConnection.setRequestMethod(Constants.HTTP_POST);
            urlConnection.setRequestProperty(CONTENT_TYPE, HEADER_CONTENT_TYPE);
            urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + token);
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            outputStream = urlConnection.getOutputStream();
            fileInputStream = new FileInputStream(file);

            final byte[] buffer = new byte[1024];

            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "writing bytes: " + len);
                }
                outputStream.write(buffer, 0, len);
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "Requesting Response code");
            }

            final int responseCode = urlConnection.getResponseCode();

            if (DEBUG) {
                MyLog.i(CLS_NAME, "responseCode: " + responseCode);
            }

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "ErrorStream: "
                            + UtilsString.streamToString(urlConnection.getErrorStream()));
                }
            }

        } catch (final ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException");
                e.printStackTrace();
            }
        } catch (final UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException");
                e.printStackTrace();
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IOException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {

        if (fileInputStream != null) {

            try {
                fileInputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (outputStream != null) {

            try {
                outputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (urlConnection != null) {

            try {
                urlConnection.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}
