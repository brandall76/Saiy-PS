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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.StartResponse;
import ai.saiy.android.utils.MyLog;

/**
 * Class to get a recording identifier that will be used for emotion analysis request. This identifier
 * should be stored for future use, as it can be used to retrieve meta-data regarding the recording
 * in the future.
 * <p/>
 * This is a synchronous request, as the identifier will be required immediately.
 * <p/>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class BVStartRequest {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BVStartRequest.class.getSimpleName();

    private static final String START_URL = "https://apiv3.beyondverbal.com/v3/recording/start";

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String ENCODING = "UTF-8";
    private static final String CHARSET = "Accept-Charset";

    private static final long THREAD_TIMEOUT = 7L;

    private final Context mContext;
    private final String token;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param token    the access token
     */
    public BVStartRequest(@NonNull final Context mContext, @NonNull final String token) {
        this.token = token;
        this.mContext = mContext.getApplicationContext();
    }

    /**
     * Method to get a recording identifier.
     *
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link StartResponse} object, containing the recording id.
     * <p>
     * If the request was unsuccessful, the second parameter may be null.
     */
    public Pair<Boolean, StartResponse> getId(@NonNull final JSONObject body) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getId");
        }

        final RequestFuture<JSONObject> future = RequestFuture.newFuture();
        final RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.start();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, START_URL,
                body, future, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                    BVStartRequest.this.verboseError(error);
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

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

        JSONObject response = null;

        try {
            response = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
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

        if (response != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "response: " + response);
            }

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final StartResponse startResponse = gson.fromJson(response.toString(), StartResponse.class);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "onResponse: getStatus: " + startResponse.getStatus());
            }

            if (startResponse.isSuccessful()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onResponse: getRecordingId: " + startResponse.getRecordingId());
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onResponse: getReason: " + startResponse.getReason());
                }
            }

            return new Pair<>(true, startResponse);

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
