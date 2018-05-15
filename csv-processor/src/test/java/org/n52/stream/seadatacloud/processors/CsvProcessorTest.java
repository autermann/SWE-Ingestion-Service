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
package org.n52.stream.seadatacloud.processors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.Before;
import org.junit.Test;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class CsvProcessorTest {

    private DataMessage message;
    private CsvProcessor csvp;
    
    @Before
    public void setUp() throws FileNotFoundException {
        message = new DataMessage();
        
        Timeseries<BigDecimal> t1 = new Timeseries<BigDecimal>();
        t1.setSensor("sensor1");
        t1.setPhenomenon("phenomenon1");
        t1.setFeature(new Feature().withId("featuer1"));
        Measurement<BigDecimal> m1 = new Measurement<>();
        m1.setValue(new BigDecimal("1.0"));
        m1.setPhenomenonTime(OffsetDateTime.now());
        t1.addMeasurementsItem(m1);
        message.addTimeseriesItem(t1);
        
        Timeseries<BigDecimal> t2 = new Timeseries<BigDecimal>();
        t2.setSensor("sensor2");
        t2.setPhenomenon("phenomenon2");
        t2.setFeature(new Feature().withId("featuer2"));
        Measurement<BigDecimal> m20 = new Measurement<>();
        m20.setValue(new BigDecimal("2.0"));
        m20.setPhenomenonTime(OffsetDateTime.now().plusSeconds(10));
        t2.addMeasurementsItem(m20);
        Measurement<BigDecimal> m21 = new Measurement<>();
        m21.setValue(new BigDecimal("2.1"));
        t2.addMeasurementsItem(m21);
        message.addTimeseriesItem(t2);
     
        csvp = new CsvProcessor();
    }
    
    @Test
    public void test() throws IOException {
        System.out.println(csvp.getDataMessageLog(message));
    }

}
