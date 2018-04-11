/*
 * Copyright (C) 2018-2018 52Â°North Initiative for Geospatial Open Source
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
package org.n52.stream.seadatacloud.marine;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Timeseries;

public class ProcessorFluorometerTest {

    private DataMessage dataMessage;

    @Before
    public void process() {
        dataMessage = new ProcessorFluorometer().process(OffsetDateTime.parse("2018-03-12T13:00:39.035Z"),
                "WL-ECO-FLNTU-4476",
                "Galway Bay Cable Observatory",
                Arrays.asList("03/12/18","12:57:07","695","43","700","55","554"));
    }


    @Test
    public void shouldProcessTimestamp() {
        assertThat(dataMessage, notNullValue());
        assertThat(dataMessage.getTimeseries().get(0).getMeasurements().get(0).getTimestamp(),
                is(OffsetDateTime.parse("2018-03-12T12:57:07Z")));
    }

//    @Test
//    public void shouldProcessFluorescenceWaveLength() throws Exception {
//        Timeseries<?> timeseries = dataMessage.getTimeseries().get(0);
//        assertThat(timeseries.getPhenomenon(), is("fluorescence-wavelength"));
//        assertThat(timeseries.getUnit(), is("nm"));
//        Object value = timeseries.getMeasurements().get(0).getValue();
//        assertThat(value, is(instanceOf(BigDecimal.class)));
//        assertThat(value, is(new BigDecimal("695")));
//    }

    @Test
    public void shouldProcessCHLCounts() throws Exception {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(0);
        assertThat(timeseries.getPhenomenon(), is("http://vocab.nerc.ac.uk/collection/B39/current/fluorescence/"));
        assertThat(timeseries.getUnit(), is(nullValue()));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(Integer.class)));
        assertThat(value, is(43));
    }

//    @Test
//    public void shouldProcessTurbidityWaveLength() throws Exception {
//        Timeseries<?> timeseries = dataMessage.getTimeseries().get(2);
//        assertThat(timeseries.getPhenomenon(), is("turbidity-wavelength"));
//        assertThat(timeseries.getUnit(), is("nm"));
//        Object value = timeseries.getMeasurements().get(0).getValue();
//        assertThat(value, is(instanceOf(BigDecimal.class)));
//        assertThat(value, is(new BigDecimal("700")));
//    }

    @Test
    public void shouldProcessNTUCounts() throws Exception {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(1);
        assertThat(timeseries.getPhenomenon(), is("http://vocab.nerc.ac.uk/collection/P01/current/TURBXXXX/"));
        assertThat(timeseries.getUnit(), is(nullValue()));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(Integer.class)));
        assertThat(value, is(55));
    }

//    @Test
//    public void shouldProcessThermistorValues() throws Exception {
//        Timeseries<?> timeseries = dataMessage.getTimeseries().get(4);
//        assertThat(timeseries.getPhenomenon(), is("thermistor"));
//        assertThat(timeseries.getUnit(), is(nullValue()));
//        Object value = timeseries.getMeasurements().get(0).getValue();
//        assertThat(value, is(instanceOf(Integer.class)));
//        assertThat(value, is(554));
//    }
//
//    @Test
//    public void shouldProcessInstrumentTimeDeviationValue() {
//        Timeseries<?> timeseries = dataMessage.getTimeseries().get(5);
//        assertThat(timeseries.getPhenomenon(), is("receiver-latency"));
//        assertThat(timeseries.getUnit(), is("s"));
//        Object value = timeseries.getMeasurements().get(0).getValue();
//        assertThat(value, is(instanceOf(BigDecimal.class)));
//        assertThat(value, is(new BigDecimal("212")));
//    }

}
