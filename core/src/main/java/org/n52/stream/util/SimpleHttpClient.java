/*
 * Copyright 2018-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.stream.util;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

/**
 *
 * TODO move to arctic-sea (update sos-importer when done)
 *
 */
public class SimpleHttpClient implements HttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClient.class);
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final ContentType CONTENT_TYPE_TEXT_XML = ContentType.create("text/xml", Consts.UTF_8);

    // TODO is this retry policy okay for us?
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(ConnectException.class)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxDuration(15, TimeUnit.MINUTES);
    private CloseableHttpClient httpclient;
    private int connectionTimeout;
    private int socketTimeout;

    /**
     * Creates an instance with <code>timeout = {@value #DEFAULT_CONNECTION_TIMEOUT}</code> ms.
     */
    public SimpleHttpClient() {
        this(DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Creates an instance with a given connection timeout.
     *
     * @param connectionTimeout the connection timeout in milliseconds.
     */
    public SimpleHttpClient(int connectionTimeout) {
        this(connectionTimeout, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Creates an instance with the given timeouts.
     *
     * @param connectionTimeout the connection timeout in milliseconds.
     * @param socketTimeout     the socket timeout in milliseconds.
     */
    public SimpleHttpClient(int connectionTimeout, int socketTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        recreateClient();
    }

    protected SimpleHttpClient(CloseableHttpClient httpclient) {
        this.httpclient = httpclient;
    }

    @Override
    public HttpResponse executeGet(String uri) throws IOException {
        LOG.debug("executing GET method '{}'", uri);
        return executeMethod(new HttpGet(uri));
    }

    @Override
    public HttpResponse executePost(String uri, String payloadToSend) throws IOException {
        return executePost(uri, payloadToSend, CONTENT_TYPE_TEXT_XML);
    }

    @Override
    public HttpResponse executePost(String uri, String payloadToSend, ContentType contentType) throws IOException {
        StringEntity requestEntity = new StringEntity(payloadToSend, contentType);
        LOG.trace("payload to send: {}", payloadToSend);
        return executePost(uri, requestEntity);
    }

    @Override
    public HttpResponse executePost(String uri, HttpEntity payloadToSend) throws IOException {
        LOG.debug("executing POST method to '{}'.", uri);
        HttpPost post = new HttpPost(uri);
        post.setEntity(payloadToSend);
        return executeMethod(post);
    }

    @Override
    public HttpResponse executeMethod(HttpRequestBase method) throws IOException {
        return Failsafe.with(RETRY_POLICY)
                .onFailedAttempt(ex -> LOG.warn("Could not connect to host; retrying", ex))
                .get(() -> httpclient.execute(method));
    }

    public void setConnectionTimout(int timeout) {
        connectionTimeout = timeout;
        recreateClient();
    }

    public void setSocketTimout(int timeout) {
        socketTimeout = timeout;
        recreateClient();
    }

    private void recreateClient() {
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch (IOException ex) {
                LOG.warn("Error closing client", ex);
            }
            httpclient = null;
        }
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout).build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(socketTimeout).build();
        httpclient = HttpClientBuilder.create()
                .useSystemProperties()
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

}