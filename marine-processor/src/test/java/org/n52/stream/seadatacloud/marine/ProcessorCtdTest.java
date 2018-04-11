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
package org.n52.stream.seadatacloud.marine;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Timeseries;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorCtdTest {


    private DataMessage dataMessage;

    @Before
    public void process() {
        dataMessage = new ProcessorCtd().process(OffsetDateTime.parse("2018-03-12T12:59:58.787Z"),
                "I-OCEAN7-304-0616641",
                "Galway Bay Cable Observatory",
                Arrays.asList("25.38","7.594","33.354","32.310","1477.9968","13:00:10.22M"));
    }

    @Test
    public void shouldProcessTimestamp() {
        assertThat(dataMessage, notNullValue());
        assertThat(dataMessage.getTimeseries().get(0).getMeasurements().get(0).getTimestamp(),
                is(OffsetDateTime.parse("2018-03-11T13:00:10.220Z")));
    }

    @Test
    public void shouldProcessSensor() {
        assertThat(dataMessage.getTimeseries().get(0).getSensor(), is("I-OCEAN7-304-0616641"));
    }

    @Test
    public void shouldProcessPressureValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(0);
        assertThat(timeseries.getPhenomenon(), is("pressure"));
        assertThat(timeseries.getUnit(), is("dbar"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("25.38")));
    }

    @Test
    public void shouldProcessSubseaTemperatureValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(1);
        assertThat(timeseries.getPhenomenon(), is("subsea-temperature"));
        assertThat(timeseries.getUnit(), is("°C"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("7.594")));
    }

    @Test
    public void shouldProcessConductivityValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(2);
        assertThat(timeseries.getPhenomenon(), is("conductivity"));
        assertThat(timeseries.getUnit(), is("mS/cm"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("33.354")));
    }

    @Test
    public void shouldProcessSalinityValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(3);
        assertThat(timeseries.getPhenomenon(), is("salinity"));
        assertThat(timeseries.getUnit(), is("PSU"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("32.310")));
    }

    @Test
    public void shouldProcessSoundVelocitiyValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(4);
        assertThat(timeseries.getPhenomenon(), is("sound-velocitiy"));
        assertThat(timeseries.getUnit(), is("m/s"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("1477.9968")));
    }

    @Test
    public void shouldProcessInstrumentTimeDeviationValue() {
        Timeseries<?> timeseries = dataMessage.getTimeseries().get(5);
        assertThat(timeseries.getPhenomenon(), is("receiver-latency"));
        assertThat(timeseries.getUnit(), is("s"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("86388")));
    }

    @Test
    public void instrumentDateCalculation() {
        LocalTime instrumentTime = LocalTime.parse("22:50:40");
        OffsetDateTime receiverStationtimestamp = OffsetDateTime.parse("2018-03-23T13:50:40Z");
        Long receiverLatency = ChronoUnit.HOURS.between(instrumentTime, receiverStationtimestamp);
        assertThat(receiverLatency, is(-9L));

        instrumentTime = LocalTime.parse("13:40:40");
        receiverStationtimestamp = OffsetDateTime.parse("2018-03-23T13:50:40Z");
        receiverLatency = ChronoUnit.MINUTES.between(instrumentTime, receiverStationtimestamp);
        assertThat(receiverLatency, is(10L));
    }

}