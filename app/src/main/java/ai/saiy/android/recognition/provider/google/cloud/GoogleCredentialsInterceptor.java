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

package ai.saiy.android.recognition.provider.google.cloud;

/**
 * Created by benrandall76@gmail.com on 27/09/2016.
 */

import com.google.auth.Credentials;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;

/**
 * Authenticates the gRPC channel.
 */
public class GoogleCredentialsInterceptor implements ClientInterceptor {

    private final Credentials mCredentials;

    private Metadata mCached;

    private Map<String, List<String>> mLastMetadata;

    public GoogleCredentialsInterceptor(Credentials credentials) {
        mCredentials = credentials;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, final Channel next) {
        return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {
            @Override
            protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                    throws StatusException {

                Metadata cachedSaved;
                URI uri = serviceUri(next, method);
                synchronized (GoogleCredentialsInterceptor.this) {
                    Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                    if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                        mLastMetadata = latestMetadata;
                        mCached = toHeaders(mLastMetadata);
                    }
                    cachedSaved = mCached;
                }
                headers.merge(cachedSaved);
                delegate().start(responseListener, headers);
            }
        };
    }

    /**
     * Generate a JWT-specific service URI. The URI is simply an identifier with enough
     * information for a service to know that the JWT was intended for it. The URI will
     * commonly be verified with a simple string equality check.
     */
    private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
            throws StatusException {

        String authority = channel.authority();
        if (authority == null) {
            throw Status.UNAUTHENTICATED
                    .withDescription("Channel has no authority")
                    .asException();
        }
        // Always use HTTPS, by definition.
        final String scheme = "https";
        final int defaultPort = 443;
        String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
        URI uri;
        try {
            uri = new URI(scheme, authority, path, null, null);
        } catch (URISyntaxException e) {
            throw Status.UNAUTHENTICATED
                    .withDescription("Unable to construct service URI for auth")
                    .withCause(e).asException();
        }
        // The default port must not be present. Alternative ports should be present.
        if (uri.getPort() == defaultPort) {
            uri = removePort(uri);
        }
        return uri;
    }

    private URI removePort(URI uri) throws StatusException {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw Status.UNAUTHENTICATED
                    .withDescription("Unable to construct service URI after removing port")
                    .withCause(e).asException();
        }
    }

    private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
        try {
            return mCredentials.getRequestMetadata(uri);
        } catch (IOException e) {
            throw Status.UNAUTHENTICATED.withCause(e).asException();
        }
    }

    private static Metadata toHeaders(Map<String, List<String>> metadata) {
        Metadata headers = new Metadata();
        if (metadata != null) {
            for (String key : metadata.keySet()) {
                Metadata.Key<String> headerKey = Metadata.Key.of(
                        key, Metadata.ASCII_STRING_MARSHALLER);
                for (String value : metadata.get(key)) {
                    headers.put(headerKey, value);
                }
            }
        }
        return headers;
    }

}
