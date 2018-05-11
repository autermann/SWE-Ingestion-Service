package org.n52.stream.seadatacloud.dbsink;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.n52.stream.seadatacloud.dbsink.DatabaseSinkApplication;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.read.ListAppender;
import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.marker.Markers;

public class DatabaseSinkApplicationTest {
    
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DatabaseSinkApplicationTest.class);

    private final ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) LOGGER.getAppender("listAppender");

    private final JsonFactory jsonFactory = new MappingJsonFactory();
    
    private DataMessage message;
    private DatabaseSinkApplication dsa;
    
    @Before
    public void setUp() throws FileNotFoundException {
        listAppender.list.clear();
        message = new DataMessage();
        
        Timeseries<BigDecimal> t1 = new Timeseries<BigDecimal>();
        t1.setSensor("sensor1");
        t1.setPhenomenon("phenomenon1");
        t1.setFeature(new Feature("featuer1"));
        Measurement<BigDecimal> m1 = new Measurement<>();
        m1.setValue(new BigDecimal("1.0"));
        m1.setPhenomenonTime(OffsetDateTime.now());
        t1.addMeasurementsItem(m1);
        message.addTimeseriesItem(t1);
        
        Timeseries<BigDecimal> t2 = new Timeseries<BigDecimal>();
        t2.setSensor("sensor2");
        t2.setPhenomenon("phenomenon2");
        t2.setFeature(new Feature("featuer2"));
        Measurement<BigDecimal> m20 = new Measurement<>();
        m20.setValue(new BigDecimal("2.0"));
        m20.setPhenomenonTime(OffsetDateTime.now().plusSeconds(10));
        t2.addMeasurementsItem(m20);
        Measurement<BigDecimal> m21 = new Measurement<>();
        m21.setValue(new BigDecimal("2.1"));
        t2.addMeasurementsItem(m21);
        message.addTimeseriesItem(t2);
        
        dsa = new DatabaseSinkApplication();
    }
    
    @Test
    public void test() throws IOException {
        LoggingEventCompositeJsonEncoder encoder = getEncoder("test");
        List<JsonProvider<ILoggingEvent>> providers = encoder.getProviders().getProviders();
        List<String> logs = dsa.getObservationLogStatements(message);
        
        assertThat(logs.size(), is(3));
        for (String message : logs) {
            verifyOutput(encoder, message);
        }
        
//        assertThat(logs.get(0).equals("{\"Procedure\":\"sensor1\",\"Phenomenon\":\"phenomenon1\",\"Feature\":\"featuer1\",\"Observations\":1}"), is(true));
//        assertThat(logs.get(1).equals("{\"Procedure\":\"sensor2\",\"Phenomenon\":\"phenomenon2\",\"Feature\":\"featuer2\",\"Observations\":2}"), is(true));
    }
    
    private void verifyOutput(LoggingEventCompositeJsonEncoder encoder, String message) throws IOException {
        LOGGER.info("{}", message);

        byte[] encoded = encoder.encode(listAppender.list.get(0));
        Map<String, Object> output = parseJson(new String(encoded, "UTF-8"));
    }
        
    @SuppressWarnings("unchecked")
    private <T extends Appender<ILoggingEvent>> T getAppender(String appenderName) {
        return (T) LOGGER.getAppender(appenderName);
    }

    @SuppressWarnings("unchecked")
    private <T extends Encoder<ILoggingEvent>> T getEncoder(String appenderName) {
        return (T) this.<OutputStreamAppender<ILoggingEvent>>getAppender(appenderName).getEncoder();
    }

    private Map<String, Object> parseJson(final String text) throws IOException {
        return jsonFactory.createParser(text).readValueAs(new TypeReference<Map<String, Object>>() {
        });
    }

}
