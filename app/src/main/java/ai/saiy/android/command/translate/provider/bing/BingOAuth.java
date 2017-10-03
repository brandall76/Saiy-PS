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
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/04/2016.
 */
public class BingOAuth {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BingOAuth.class.getSimpleName();

    private static final String ENCODING = "UTF-8";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String BING_OAUTH_URL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";

    private static final String OCP_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    private static final String PARAM_CONTENT_TYPE = "Content-Type";
    private static final String PARAM_ACCEPT_CHARSET = "Accept-Charset";
    private static final long THREAD_TIMEOUT = 7L;

    /**
     * Get the Bing OAuth refresh token and store in {@link BingCredentials}
     *
     * @param ctx         the application context
     * @param synchronous whether or not this request should execute synchronously
     * @return true if the process succeeded. False otherwise
     */
    public boolean execute(@NonNull final Context ctx, final boolean synchronous) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        final long then = System.nanoTime();

        final String encodedSubscriptionKey;

        try {
            encodedSubscriptionKey = URLEncoder.encode(MicrosoftConfiguration.MS_TRANSLATE_SUBSCRIPTION_KEY, ENCODING);
        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "execute: UnsupportedEncodingException");
                e.printStackTrace();
            }

            return false;
        }

        final RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.start();

        Response.Listener<String> listener;
        RequestFuture<String> future = null;

        if (synchronous) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synchronous: true");
            }

            future = RequestFuture.newFuture();
            listener = future;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synchronous: false");
            }

            listener = new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onResponse: " + response);
                    }

                    queue.stop();

                    final BingCredentials credentials = new BingCredentials(response, 600);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onResponse: subscription_key: " + credentials.getRefreshToken());
                        MyLog.i(CLS_NAME, "onResponse: expires_in: " + credentials.getExpires());
                    }

                    SPH.setBingToken(ctx, credentials.getRefreshToken());
                    SPH.setBingTokenExpiryTime(ctx, (System.currentTimeMillis() + (credentials.getExpires() * 1000)));
                }
            };
        }

        final StringRequest request = new StringRequest(Request.Method.POST, BING_OAUTH_URL, listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onErrorResponse: " + error.toString());
                            BingOAuth.this.verboseError(error);
                        }
                        queue.stop();
                    }
                })

        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> params = new HashMap<>();
                params.put(PARAM_CONTENT_TYPE, HEADER_CONTENT_TYPE);
                params.put(PARAM_ACCEPT_CHARSET, ENCODING);
                params.put(OCP_SUBSCRIPTION_KEY_HEADER, encodedSubscriptionKey);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

        String jsonResponse;

        if (synchronous) {
            try {
                jsonResponse = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);

                final BingCredentials credentials = new BingCredentials(jsonResponse, 600);

                SPH.setBingToken(ctx, credentials.getRefreshToken());
                SPH.setBingTokenExpiryTime(ctx, (System.currentTimeMillis() + (credentials.getExpires() * 1000)));

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onResponse: subscription_key: " + credentials.getRefreshToken());
                    MyLog.i(CLS_NAME, "onResponse: expires_in: " + credentials.getExpires());
                    MyLog.getElapsed(CLS_NAME, then);
                }

                return true;
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "execute: JsonSyntaxException");
                    e.printStackTrace();
                }
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
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "execute: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "execute: Exception");
                    e.printStackTrace();
                }
            } finally {
                queue.stop();
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(BingOAuth.class.getSimpleName(), then);
        }

        return false;
    }

    /**
     * Used for debugging only to view verbose error information
     *
     * @param error the {@link VolleyError}
     */
    private void verboseError(final VolleyError error) {

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