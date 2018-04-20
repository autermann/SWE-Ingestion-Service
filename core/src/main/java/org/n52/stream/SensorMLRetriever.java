/*
 * Copyright (C) 2018-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import javax.inject.Named;

import org.apache.xmlbeans.XmlException;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.stream.util.DecoderHelper;
import org.n52.svalbard.decode.exception.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike
 * Hinderk</a>
 */
@Configuration
public class SensorMLRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(SensorMLRetriever.class);

    @Autowired
    private DecoderHelper decoderHelper;

    @Bean
    @Named("sensorml")
    public AggregateProcess loadSensorML(@Value("${org.n52.stream.sensormlurl}") String url)
            throws DecodingException, XmlException, IOException {
        LOG.info(url);
        URL sensormlUrl = null;
        try {
            sensormlUrl = new URL(url);
            LOG.info(sensormlUrl.toString());
        } catch (MalformedURLException e) {
            String msg = String.format("Setting 'sensormlurl' malformed: %s (set loglevel to 'TRACE' for stacktrace)",
                    e.getMessage());
            LOG.error(msg);
            LOG.trace("Exception thrown: ", e);
        }
        /*ResponseEntity<String> responseDocument = null;*/
        String responseDocument = "";
        try {
            /*RestTemplate restClient = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            responseDocument = restClient.exchange(sensormlUrl, HttpMethod.GET, entity, String.class);*/
            HttpURLConnection conn = (HttpURLConnection) sensormlUrl.openConnection();
//            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            responseDocument = res.toString();
        } catch (RestClientException e) {
            logAndThrowException(sensormlUrl, e);
        }
//        if (!responseDocument.getStatusCode().is2xxSuccessful()) {
//            logAndThrowException(sensormlUrl, new RuntimeException("HttpResponseCode != 2xx."));
//        }
        Object decodedGetResponse = decoderHelper.decode(responseDocument/*.getBody()*/);
        if (decodedGetResponse instanceof AggregateProcess) {
            return (AggregateProcess) decodedGetResponse;
        } else {
            String msg = String.format(
                    "XML document received from '%s' isn't sml2.0:AggregateProcess! Received: %s",
                    sensormlUrl,
                    decodedGetResponse.getClass());
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void logAndThrowException(URL sensormlUrl, Exception e) throws RuntimeException {
        String msg = String.format("Error while retrieving file from sensormlurl ('%s') :"
                + " %s (set loglevel to 'TRACE' for stacktrace)",
                sensormlUrl.toString(),
                e.getMessage());
        LOG.error(msg);
        LOG.trace("Exception thrown: ", e);
        throw new RuntimeException(e);
    }

}
