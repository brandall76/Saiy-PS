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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.BVCredentials;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to get an initial access token, which will be valid for a short period of time. This
 * request must be made before emotion analysis or any other request.
 * <p>
 * Rather than using this class directly, it is better to
 * call {@link BVCredentials#refreshTokenIfRequired(Context, String)} which will invoke this class if
 * the current access token has expired.
 * <p>
 * The token request is always synchronous, as it is an 'on-demand' requirement.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class BVAuthRequest {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BVAuthRequest.class.getSimpleName();

    private static final String AUTH_URL = "https://token.beyondverbal.com/token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String API_KEY = "apiKey";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String ENCODING = "UTF-8";
    private static final String CHARSET = "Accept-Charset";

    private static final long THREAD_TIMEOUT = 7L;

    private final Context mContext;
    private final String apiKey;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param apiKey   the api key
     */
    public BVAuthRequest(@NonNull final Context mContext, @NonNull final String apiKey) {
        this.apiKey = apiKey;
        this.mContext = mContext.getApplicationContext();
    }

    /**
     * Method to get a temporary access token.
     *
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link BVCredentials} object, containing the token credentials. If the request was unsuccessful,
     * the second parameter may be null.
     */
    public Pair<Boolean, BVCredentials> getToken() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getID");
        }

        final RequestFuture<String> future = RequestFuture.newFuture();
        final RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.start();

        final StringRequest request = new StringRequest(Request.Method.POST, AUTH_URL, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            BVAuthRequest.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, ENCODING);
                params.put(CONTENT_TYPE, HEADER_CONTENT_TYPE);
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, ENCODING);
                params.put(API_KEY, apiKey);
                params.put(GRANT_TYPE, CLIENT_CREDENTIALS);
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
                MyLog.w(CLS_NAME, "getID: InterruptedException");
                e.printStackTrace();
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: ExecutionException");
                e.printStackTrace();
            }
        } catch (final TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: TimeoutException");
                e.printStackTrace();
            }
        } finally {
            queue.stop();
        }

        if (response != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "response: " + response);
            }

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final BVCredentials authResponse = gson.fromJson(response, BVCredentials.class);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "onResponse: getAccessToken: " + authResponse.getAccessToken());
                MyLog.i(CLS_NAME, "onResponse: getTokenType: " + authResponse.getTokenType());
                MyLog.i(CLS_NAME, "onResponse: getExpiresIn: " + authResponse.getExpiresIn());
            }

            authResponse.setExpiryTime(authResponse.getExpiresIn());
            SPH.setBeyondVerbalCredentials(mContext, gson.toJson(authResponse));

            return new Pair<>(true, authResponse);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "response: failed");
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
