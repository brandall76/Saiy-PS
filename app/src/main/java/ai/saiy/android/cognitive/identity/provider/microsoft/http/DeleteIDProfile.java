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

package ai.saiy.android.cognitive.identity.provider.microsoft.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 07/09/2016.
 */

public class DeleteIDProfile {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DeleteIDProfile.class.getSimpleName();

    private static final String DELETE_URL = "https://westus.api.cognitive.microsoft.com/spid/v1.0/identificationProfiles/";
    private static final String OCP_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String CHARSET = "Accept-Charset";

    private final Context mContext;
    private final String apiKey;
    private final String profileId;

    /**
     * Constructor
     *
     * @param mContext  the application context
     * @param apiKey    the api key
     * @param profileId to delete
     */
    public DeleteIDProfile(@NonNull final Context mContext, @NonNull final String apiKey,
                           @NonNull final String profileId) {
        this.apiKey = apiKey;
        this.mContext = mContext.getApplicationContext();
        this.profileId = profileId;
    }

    public void delete() {

        final long then = System.nanoTime();

        String url = null;

        try {
            url = DELETE_URL + URLEncoder.encode(profileId, Constants.ENCODING_UTF8);
        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "delete: UnsupportedEncodingException");
                e.printStackTrace();
            }
        }

        final RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.start();

        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onResponse: success");
                        }
                        queue.stop();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            DeleteIDProfile.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, Constants.ENCODING_UTF8);
                params.put(OCP_SUBSCRIPTION_KEY_HEADER, apiKey);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
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
