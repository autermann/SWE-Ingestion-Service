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
package org.n52.stream.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensorMLRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(SensorMLRetriever.class);

    @Bean
    @Named("sensorml")
    public Object loadSensorML(@Value("${org.n52.stream.sensorml-url}") String url) {
        //http request und parse;
        URL sensormlUrl = null;
        try {
            sensormlUrl = new URL(url);
        } catch (MalformedURLException e) {
            String msg = String.format("Setting 'sensorml-url' malformed: %s (set loglevel to 'TRACE' for stacktrace)",
                    e.getMessage());
            LOG.error(msg);
            LOG.trace("Exception thrown: ", e);
        }
        HttpResponse getResponse = null;
        try {
            getResponse = new SimpleHttpClient().executeGet(sensormlUrl.toString());
        } catch (IOException e) {
            logAndThrowException(sensormlUrl, e);
        }
        if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logAndThrowException(sensormlUrl, new RuntimeException("HttpResponseCode != " + HttpStatus.SC_OK));
        }
        return getResponse;
    }

//    private Object decodeResponse(HttpResponse response)
//            throws OwsExceptionReport, DecodingException, XmlException, IOException {
//        try (InputStream content = response.getEntity().getContent()) {
//            XmlObject xmlResponse = XmlObject.Factory.parse(content);
//            DecoderKey decoderKey = CodingHelper.getDecoderKey(xmlResponse);
//            Decoder<Object, Object> decoder = getDecoderRepository().getDecoder(decoderKey);
//            if (decoder == null) {
//                throw new NoDecoderForKeyException(decoderKey);
//            }
//            Object decode = decoder.decode(xmlResponse);
//            if (decode instanceof OwsExceptionReport) {
//                throw (OwsExceptionReport) decode;
//            }
//            return decode;
//        }
//    }

    private void logAndThrowException(URL sensormlUrl, Exception e) throws RuntimeException {
        String msg = String.format("Error while retrieving file from sensorml-url ('%s') :"
                + " %s (set loglevel to 'TRACE' for stacktrace)",
                sensormlUrl.toString(),
                e.getMessage());
        LOG.error(msg);
        LOG.trace("Exception thrown: ", e);
        throw new RuntimeException(e);
    }

}
