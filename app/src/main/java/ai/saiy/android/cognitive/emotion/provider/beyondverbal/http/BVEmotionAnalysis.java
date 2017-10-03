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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nuance.dragon.toolkit.oem.api.json.JSONObject;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResultHelper;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis.Emotions;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.BVCredentials;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsVolley;

/**
 * Class to get an initial access token, which will be valid for a short period of time. This
 * request must be made before emotion analysis or any other request.
 * <p/>
 * Rather than using this class directly, it is better to
 * call {@link BVCredentials#refreshTokenIfRequired(Context, String)} which will invoke this class if
 * the current access token has expired.
 * <p/>
 * The token request is always synchronous, as it is an 'on-demand' requirement.
 * <p/>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class BVEmotionAnalysis {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BVEmotionAnalysis.class.getSimpleName();

    private static final String ANALYSIS_URL = "https://apiv3.beyondverbal.com/v3/recording/";
    private static final String FROM_MS = "/analysis?fromMs=";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String ENCODING = "UTF-8";
    private static final String CHARSET = "Accept-Charset";

    private static final long THREAD_TIMEOUT = 7L;

    private final Context mContext;
    private final SupportedLanguage sl;
    private final String token;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage} object
     * @param token    the access token
     */
    public BVEmotionAnalysis(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
                             @NonNull final String token) {
        this.token = token;
        this.mContext = mContext;
        this.sl = sl;
    }

    /**
     * Method to get a temporary access token.
     *
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link BVCredentials} object, containing the token credentials. If the request was unsuccessful,
     * the second parameter may be null.
     */
    public Pair<Boolean, Emotions> getAnalysis(@NonNull final String recordingId, final int offset) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAnalysis");
        }

        final RequestFuture<String> future = RequestFuture.newFuture();
        final Cache cache = UtilsVolley.getCache(mContext);
        final Network network = new BasicNetwork(new HurlStack());
        final RequestQueue queue = new RequestQueue(cache, network);
        queue.start();

        final String url = ANALYSIS_URL + recordingId + FROM_MS + String.valueOf(offset);

        final StringRequest request = new StringRequest(Request.Method.GET, url, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            BVEmotionAnalysis.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, ENCODING);
                params.put(AUTHORIZATION, BEARER_ + token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

        String response = null;

        try {
            response = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: InterruptedException");
                e.printStackTrace();
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: ExecutionException");
                e.printStackTrace();
            }
        } catch (final TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: TimeoutException");
                e.printStackTrace();
            }
        } finally {
            queue.stop();
        }

        if (response != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onResponse: " + response);
                try {
                    final JSONObject object = new JSONObject(response);
                    MyLog.i(CLS_NAME, "object: " + object.toString(4));
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final Emotions emotions = gson.fromJson(response, Emotions.class);
            emotions.setRecordingId(recordingId);

            new AnalysisResultHelper(mContext, sl).interpretAndStore(emotions);

            return new Pair<>(true, emotions);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onResponse: failed");
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
