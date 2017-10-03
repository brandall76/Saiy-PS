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

package ai.saiy.android.command.translate.provider.bing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.utils.UtilsVolley;

/**
 * Created by benrandall76@gmail.com on 18/04/2016.
 */
public class BingTranslateAPI {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BingTranslateAPI.class.getSimpleName();

    private static final String SERVICE_URL = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String ENCODING = "UTF-8";
    private static final String PARAM_TO_LANGUAGE = "&to=";
    private static final String PARAM_FROM_LANGUAGE = "&from=";
    private static final String PARAM_TEXT = "&text=";

    private static final long THREAD_TIMEOUT = 7L;
    private static final String ARGUMENT_EXCEPTION = "Argument";

    /**
     * Perform a synchronous translation request
     *
     * @param ctx   the application context
     * @param token the Bing OAuth refresh token
     * @param text  the text to be translated
     * @param from  the {@link TranslationLanguageBing} to translate from
     * @param to    the {@link TranslationLanguageBing} to translate to
     * @return a {@link Pair} with the first parameter donating success and the second the result
     */
    public Pair<Boolean, String> execute(@NonNull final Context ctx, final String token, final String text,
                                         final TranslationLanguageBing from, final TranslationLanguageBing to) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        final long then = System.nanoTime();
        final String params;

        try {

            params = PARAM_FROM_LANGUAGE + URLEncoder.encode(from.getLanguage(), ENCODING)
                    + PARAM_TO_LANGUAGE + URLEncoder.encode(to.getLanguage(), ENCODING)
                    + PARAM_TEXT + URLEncoder.encode(text, ENCODING);

        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: UnsupportedEncodingException");
                e.printStackTrace();
            }
            return new Pair<>(false, null);
        }

        final RequestFuture<String> future = RequestFuture.newFuture();
        final Cache cache = UtilsVolley.getCache(ctx);
        final Network network = new BasicNetwork(new HurlStack());
        final RequestQueue queue = new RequestQueue(cache, network);
        queue.start();

        final String url = SERVICE_URL + params;

        final StringRequest request = new StringRequest(Request.Method.GET, url, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            BingTranslateAPI.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put("Content-Type", HEADER_CONTENT_TYPE);
                params.put("Accept-Charset", ENCODING);
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

        String translation = "";

        try {
            translation = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: TimeoutException");
                e.printStackTrace();
            }
        } finally {
            queue.stop();
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        if (UtilsString.notNaked(translation) && !translation.contains(ARGUMENT_EXCEPTION)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "translation: " + translation);
            }

            try {
                translation = new GsonBuilder().disableHtmlEscaping().create().fromJson(translation, String.class);
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "translation: JsonSyntaxException");
                    e.printStackTrace();
                }
            }

            return new Pair<>(true, translation);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "translation: failed");
            }
            return new Pair<>(false, null);
        }
    }

    /**
     * Used for debugging only to view verbose error information
     *
     * @param error the {@link VolleyError}
     */
    private void verboseError(@NonNull final VolleyError error) {

        final NetworkResponse response = error.networkResponse;

        if (response != null && error instanceof ServerError) {

            try {
                final String result = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                MyLog.i(CLS_NAME, "result: " + result);
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
