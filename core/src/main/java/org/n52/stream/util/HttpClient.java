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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

public interface HttpClient {

    /**
     * @param uri            the target to send the GET request to.
     * @return               the HTTP response returned by the target.
     *
     * @throws IOException - in case of given invalid uri or
     *                       a problem or the connection was aborted or
     *                       an http protocol error
     */
    HttpResponse executeGet(String uri) throws IOException;

    /**
     * Sends the given payload as content-type text/xml with UTF-8 encoding to the determined URI.
     * <strong>Callees are responsible for ensuring that the contents are actually encoded as UTF-8</strong>. If not
     * UTF-8, use {@link #executePost(String, String, ContentType)} instead.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend the POST payload as XML encoded as UTF-8.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, String payloadToSend) throws IOException;

    /**
     * Sends the given payload (marked to be of a specific content-type) to the determined URI.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend the POST payload as XML.
     * @param contentType   the content-type of the payload.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, String payloadToSend, ContentType contentType) throws IOException;

    /**
     * Sends the given payload to the determined URI. Refer to the <a
     * href="http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/index.html">HTTP components docs</a>
     * to get more information which entity types are possible.
     *
     * @param uri           the target to send the POST request to.
     * @param payloadToSend a more generic way to send arbitrary content.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails.
     */
    HttpResponse executePost(String uri, HttpEntity payloadToSend) throws IOException;

    /**
     * @param method the HTTP method to execute.
     *
     * @return the HTTP response returned by the target.
     *
     * @throws IOException if sending the request fails
     */
    HttpResponse executeMethod(HttpRequestBase method) throws IOException;

}