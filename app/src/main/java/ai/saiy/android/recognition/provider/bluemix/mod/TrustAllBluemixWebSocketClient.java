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

package ai.saiy.android.recognition.provider.bluemix.mod;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ai.saiy.android.recognition.provider.bluemix.IWebSocketCallback;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 03/08/2016.
 */

public class TrustAllBluemixWebSocketClient extends WebSocketClient {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = TrustAllBluemixWebSocketClient.class.getSimpleName();

    private static final String SSL_NULL = "ssl == null";

    private final IWebSocketCallback callback;

    public TrustAllBluemixWebSocketClient(@NonNull final URI serverURL, @NonNull final Map<String, String> header,
                                          final IWebSocketCallback callback) {
        super(serverURL, new Draft_17(), header, 10000);
        this.callback = callback;
    }

    public void start() throws NoSuchAlgorithmException, KeyManagementException, InterruptedException, CertificateException {

        final SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            }
        }}, new java.security.SecureRandom());

        this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));

        this.connectBlocking();
    }

    public void disconnect() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "disconnect");
        }

        boolean shouldDisconnect = true;

        switch (getReadyState()) {

            case NOT_YET_CONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "READY_STATE: NOT_YET_CONNECTED");
                }
                break;
            case CONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "READY_STATE: CONNECTING");
                }
                break;
            case OPEN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "READY_STATE: OPEN");
                }
                break;
            case CLOSING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "READY_STATE: CLOSING");
                }
                shouldDisconnect = false;
                break;
            case CLOSED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "READY_STATE: CLOSED");
                }
                shouldDisconnect = false;
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "READY_STATE: default");
                }
                break;
        }

        if (shouldDisconnect) {

            try {
                this.send(new byte[0]);
            } catch (final WebsocketNotConnectedException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "disconnect: byte0: WebSocketNotConnectedException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "disconnect: byte0: Exception");
                    e.printStackTrace();
                }
            }

            try {
                this.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "disconnect: close: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "close");
        }
        super.close();
    }

    @Override
    public void onOpen(final ServerHandshake handshakeData) {
        callback.onOpen(handshakeData);
    }

    @Override
    public void onMessage(final String message) {
        callback.onMessage(message);
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        callback.onClose(code, reason, remote);
    }

    @Override
    public void onError(final Exception e) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
            MyLog.w(CLS_NAME, "onError: getMessage: " + e.getMessage());
            e.printStackTrace();
        }

        if (shouldError(e)) {
            callback.onError(e);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onError: ignoring SSL message");
            }
        }
    }

    private boolean shouldError(@NonNull final Exception e) {
        return !(UtilsString.notNaked(e.getMessage()) && e.getMessage().matches(SSL_NULL));
    }
}
