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
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileList;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem;
import ai.saiy.android.utils.MyLog;

/**
 * Class to get a list of enrollment ids.
 * <p>
 * This request is always synchronous, as it is an 'on-demand' requirement.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class ListIDProfiles {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ListIDProfiles.class.getSimpleName();

    private static final String LIST_URL = "https://westus.api.cognitive.microsoft.com/spid/v1.0/identificationProfiles";
    private static final String OCP_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String ENCODING = "UTF-8";
    private static final String CHARSET = "Accept-Charset";

    /**
     * The initial volley request can be inexplicably slow
     */
    private static final int OTT_MULTIPLIER = 20;
    private static final long THREAD_TIMEOUT = 20L;

    private final Context mContext;
    private final String apiKey;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param apiKey   the api key
     */
    public ListIDProfiles(@NonNull final Context mContext, @NonNull final String apiKey) {
        this.apiKey = apiKey;
        this.mContext = mContext.getApplicationContext();
    }

    /**
     * Method to get all enrollment profile information.
     *
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link ProfileList} object, containing the list of profiles. If the request was unsuccessful,
     * the second parameter may be null.
     */
    public Pair<Boolean, ProfileList> getProfiles() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getProfiles");
        }

        final long then = System.nanoTime();

        final RequestFuture<String> future = RequestFuture.newFuture();
        final RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.start();

        final StringRequest request = new StringRequest(Request.Method.GET, LIST_URL, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            ListIDProfiles.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, ENCODING);
                params.put(OCP_SUBSCRIPTION_KEY_HEADER, apiKey);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * OTT_MULTIPLIER,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

        String response = null;

        try {
            response = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProfiles: InterruptedException");
                e.printStackTrace();
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProfiles: ExecutionException");
                e.printStackTrace();
            }
        } catch (final TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProfiles: TimeoutException");
                e.printStackTrace();
            }
        } finally {
            queue.stop();
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        if (response != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "response: " + response);

            }

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final Type type = new TypeToken<List<ProfileItem>>() {
            }.getType();

            final ProfileList profileList = new ProfileList(gson.<List<ProfileItem>>fromJson(response,type));

            if (DEBUG) {
                MyLog.i(CLS_NAME, "onResponse: profileList size: " + profileList.getItems().size());
            }

            return new Pair<>(true, profileList);

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
