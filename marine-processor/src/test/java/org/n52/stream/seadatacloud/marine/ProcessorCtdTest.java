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
package org.n52.stream.seadatacloud.marine;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.n52.stream.core.Dataset;
import org.n52.stream.core.Timeseries;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorCtdTest {


    private Dataset dataset;

    @Before
    public void process() {
        dataset = new ProcessorCtd().process(OffsetDateTime.parse("2018-03-12T12:59:58.787Z"),
                "I-OCEAN7-304-0616641",
                "Galway Bay Cable Observatory",
                Arrays.asList("25.38","7.594","33.354","32.310","1477.9968","13:00:10.22M"));
    }

    @Test
    public void shouldProcessTimestamp() {
        assertThat(dataset, notNullValue());
        assertThat(dataset.getTimeseries().get(0).getMeasurements().get(0).getTimestamp(),
                is(OffsetDateTime.parse("2018-03-12T12:59:58.787Z")));
    }

    @Test
    public void shouldProcessSensor() {
        assertThat(dataset.getTimeseries().get(0).getSensor(), is("I-OCEAN7-304-0616641"));
    }

    @Test
    public void shouldProcessPressureValue() {
        Timeseries timeseries = dataset.getTimeseries().get(0);
        assertThat(timeseries.getPhenomenon(), is("pressure"));
        assertThat(timeseries.getUnit(), is("dbar"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("25.38")));
    }

    @Test
    public void shouldProcessSubseaTemperatureValue() {
        Timeseries timeseries = dataset.getTimeseries().get(1);
        assertThat(timeseries.getPhenomenon(), is("subsea-temperature"));
        assertThat(timeseries.getUnit(), is("°C"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("7.594")));
    }

    @Test
    public void shouldProcessConductivityValue() {
        Timeseries timeseries = dataset.getTimeseries().get(2);
        assertThat(timeseries.getPhenomenon(), is("conductivity"));
        assertThat(timeseries.getUnit(), is("mS/cm"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("33.354")));
    }

    @Test
    public void shouldProcessSalinityValue() {
        Timeseries timeseries = dataset.getTimeseries().get(3);
        assertThat(timeseries.getPhenomenon(), is("salinity"));
        assertThat(timeseries.getUnit(), is("PSU"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("32.310")));
    }

    @Test
    public void shouldProcessSoundVelocitiyValue() {
        Timeseries timeseries = dataset.getTimeseries().get(4);
        assertThat(timeseries.getPhenomenon(), is("sound-velocitiy"));
        assertThat(timeseries.getUnit(), is("m/s"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(BigDecimal.class)));
        assertThat(value, is(new BigDecimal("1477.9968")));
    }

    @Test
    public void shouldProcessInstrumentTimeDeviationValue() {
        Timeseries timeseries = dataset.getTimeseries().get(5);
        assertThat(timeseries.getPhenomenon(), is("instrument-time-deviation"));
        assertThat(timeseries.getUnit(), is("ms"));
        Object value = timeseries.getMeasurements().get(0).getValue();
        assertThat(value, is(instanceOf(Long.class)));
        assertThat(value, is(new Long("-11433")));
    }
}
