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

package ai.saiy.android.cognitive.knowledge.provider.wolframalpha;

import android.net.ParseException;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.resolve.ResolveWolframAlpha;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.resolve.WolframAlphaRequest;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.resolve.WolframAlphaResponse;
import ai.saiy.android.configuration.WolframConfiguration;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

public class WolframAlphaCognitive {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = WolframAlphaCognitive.class.getSimpleName();

    private static final String REINTERPRET = "&reinterpret=true";
    private static final String FORMAT = "&format=plaintext";
    private static final String PARSE_TIMEOUT = "&parsetimeout=10";
    private static final String SCAN_TIMEOUT = "&scantimeout=2";
    private static final String FORMAT_TIMEOUT = "&formattimeout=5";
    private static final String APP_ID = "&appid=";
    private static final String IGNORE_CASE = "&ignorecase=true";
    private static final String ENCODING = "utf-8";

    private static final String POD_INDEX = "&podindex=1,2,3";
    private static final String LAT_LONG = "&latlong=";

    // TODO - need to contact W|A
    private static final String SIGNATURE = "sig";

    private String response;
    private HttpURLConnection urlConnection;

    /**
     * Perform a synchronous validation request to Wolfram Alpha
     *
     * @param text the text to be queried
     * @return true if the request passed validation, false otherwise
     */
    public boolean validate(@NonNull final String text) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validate");
        }

        final long then = System.nanoTime();

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try {

            final String urlString = WolframConfiguration.VALIDATE_URL + URLEncoder.encode(text, ENCODING)
                    + APP_ID + WolframConfiguration.WOLFRAM_APP_ID + FORMAT + IGNORE_CASE;

            if (DEBUG) {
                MyLog.i(CLS_NAME, "url:" + urlString);
            }

            urlConnection = (HttpURLConnection) new URL(urlString).openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();

            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "error stream: "
                            + UtilsString.streamToString(urlConnection.getErrorStream()));
                }
            } else {
                response = UtilsString.streamToString(urlConnection.getInputStream());
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "response: " + response);
                }
            }
        } catch (final MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException");
                e.printStackTrace();
            }
        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                e.printStackTrace();
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
                MyLog.e(CLS_NAME, "IOException");
                e.printStackTrace();
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException");
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

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        if (UtilsString.notNaked(response)) {
            return new ResolveWolframAlpha().validate(response);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "response: failed");
            }
            return false;
        }
    }


    /**
     * Perform a synchronous Wolfram Alpha request
     *
     * @param request the {@link WolframAlphaRequest} object
     * @return a {@link Pair} with the first parameter donating success and the second the result
     */
    public Pair<Boolean, WolframAlphaResponse> execute(@NonNull final WolframAlphaRequest request) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        final long then = System.nanoTime();

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try {

            final String urlString = WolframConfiguration.QUERY_URL
                    + URLEncoder.encode(request.getQuery(), ENCODING) + APP_ID
                    + WolframConfiguration.WOLFRAM_APP_ID + FORMAT + PARSE_TIMEOUT + SCAN_TIMEOUT
                    + FORMAT_TIMEOUT + REINTERPRET + IGNORE_CASE;

            if (DEBUG) {
                MyLog.i(CLS_NAME, "url:" + urlString);
            }

            urlConnection = (HttpURLConnection) new URL(urlString).openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();

            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "error stream: "
                            + UtilsString.streamToString(urlConnection.getErrorStream()));
                }
            } else {
                response = UtilsString.streamToString(urlConnection.getInputStream());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "response: " + response);
                }
            }
        } catch (final MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException");
                e.printStackTrace();
            }
        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                e.printStackTrace();
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
                MyLog.e(CLS_NAME, "IOException");
                e.printStackTrace();
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException");
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

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        if (UtilsString.notNaked(response)) {
            return new ResolveWolframAlpha().resolve(request, response);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "response: failed");
            }
            return new Pair<>(false, null);
        }
    }

    private void closeConnection() {

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
