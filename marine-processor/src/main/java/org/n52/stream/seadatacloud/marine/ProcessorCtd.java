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
 */package org.n52.stream.seadatacloud.marine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.n52.stream.core.Dataset;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;

/**
 * Processes MQTT messages from:<br>
 * <em>Host</em>: <code>mqtt.marine.ie</code><br>
 * <em>Port</em>: <code>1883</code><br>
 * <em>Topic</em>: <code>spiddal-ctd</code><br>
 * <br>
 * <em>Raw Payload</em>:
 * <code>2018-03-12T12:59:58.787Z|I-OCEAN7-304-0616641|  25.38   7.594  33.354  32.310 1477.9968 13:00:10.22M</code><br>
 * <br>
 * <em>Formatted Payload</em>:<br>
 * <em>Shore Station Time</em>: 2018-03-12T12:59:58.787Z<br>
 * <em>Sensor</em>: I-OCEAN7-304-0616641<br>
 * <em>Pressure</em>: 25.38 dbar<br>
 * <em>Subsea Temperature</em>: 7.594 °C<br>
 * <em>Conductivity</em>: 33.354 mS/cm<br>
 * <em>Salinity</em>: 32.310 PSU<br>
 * <em>Sound Velocitiy</em>: 1477.9968 m/s<br>
 * <em>Instrument Time</em>: 13:00:10.22
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorCtd extends ProcessorSkeleton {

    public Dataset process(OffsetDateTime receiverStationtimestamp, String sensorId, String featureId, List<String> values) {
        validateInput(receiverStationtimestamp, sensorId, featureId, values);
        if (values.size() != 6) {
            String valuesString = values.toString();
            if ("[Press, Temp, Cond, Sal, SoundV]".equalsIgnoreCase(valuesString) ||
                    "[Acquisition:, <^C>Stop]".equalsIgnoreCase(valuesString)) {
                return null;
            }
            throw createInvalidNumberOfValuesException("six", values);
        }

        Feature feature = new Feature();
        feature.setId(featureId);

        LocalTime instrumentTime = LocalTime.parse(values.get(5).substring(0, values.get(5).length()-1));
        LocalDate instrumentDate = receiverStationtimestamp.toLocalDate();
        OffsetDateTime timestamp = OffsetDateTime.of(instrumentDate, instrumentTime, ZoneOffset.UTC);;
        Long receiverLatency = ChronoUnit.SECONDS.between(timestamp, receiverStationtimestamp);
        if (receiverLatency < 0) {
            instrumentDate = instrumentDate.minus(1, ChronoUnit.DAYS);
            timestamp = OffsetDateTime.of(instrumentDate, instrumentTime, ZoneOffset.UTC);
            receiverLatency = ChronoUnit.SECONDS.between(timestamp, receiverStationtimestamp);
        }

        Measurement<Long> receiverLatencyMeasurement = new Measurement<>();
        receiverLatencyMeasurement.setValue(receiverLatency);
        receiverLatencyMeasurement.setTimestamp(timestamp);

        Timeseries<Long> receiverLatencyTimeseries = new Timeseries<>();
        receiverLatencyTimeseries.setFeature(feature);
        receiverLatencyTimeseries.setSensor(sensorId);
        receiverLatencyTimeseries.setPhenomenon("receiver-latency");
        receiverLatencyTimeseries.setUnit("s");
        receiverLatencyTimeseries.addMeasurementsItem(receiverLatencyMeasurement);


        Measurement<BigDecimal> pressureMeasurement = new Measurement<>();
        pressureMeasurement.setValue(new BigDecimal(values.get(0)));
        pressureMeasurement.setTimestamp(timestamp);

        Timeseries<BigDecimal> pressureTimeseries = new Timeseries<>();
        pressureTimeseries.setFeature(feature);
        pressureTimeseries.setSensor(sensorId);
        pressureTimeseries.setPhenomenon("pressure");
        pressureTimeseries.setUnit("dbar");
        pressureTimeseries.addMeasurementsItem(pressureMeasurement);


        Measurement<BigDecimal> subseaTemperatureMeasurement = new Measurement<>();
        subseaTemperatureMeasurement.setValue(new BigDecimal(values.get(1)));
        subseaTemperatureMeasurement.setTimestamp(timestamp);

        Timeseries<BigDecimal> subseaTemperatureTimeseries = new Timeseries<>();
        subseaTemperatureTimeseries.setFeature(feature);
        subseaTemperatureTimeseries.setSensor(sensorId);
        subseaTemperatureTimeseries.setPhenomenon("subsea-temperature");
        subseaTemperatureTimeseries.setUnit("°C");
        subseaTemperatureTimeseries.addMeasurementsItem(subseaTemperatureMeasurement);


        Measurement<BigDecimal> conductivityMeasurement = new Measurement<>();
        conductivityMeasurement.setValue(new BigDecimal(values.get(2)));
        conductivityMeasurement.setTimestamp(timestamp);

        Timeseries<BigDecimal> conductivityTimeseries = new Timeseries<>();
        conductivityTimeseries.setFeature(feature);
        conductivityTimeseries.setSensor(sensorId);
        conductivityTimeseries.setPhenomenon("conductivity");
        conductivityTimeseries.setUnit("mS/cm");
        conductivityTimeseries.addMeasurementsItem(conductivityMeasurement);


        Measurement<BigDecimal> salinityMeasurement = new Measurement<>();
        salinityMeasurement.setValue(new BigDecimal(values.get(3)));
        salinityMeasurement.setTimestamp(timestamp);

        Timeseries<BigDecimal> salinityTimeseries = new Timeseries<>();
        salinityTimeseries.setFeature(feature);
        salinityTimeseries.setSensor(sensorId);
        salinityTimeseries.setPhenomenon("salinity");
        salinityTimeseries.setUnit("PSU");
        salinityTimeseries.addMeasurementsItem(salinityMeasurement);


        Measurement<BigDecimal> soundVelocitiyMeasurement = new Measurement<>();
        soundVelocitiyMeasurement.setValue(new BigDecimal(values.get(4)));
        soundVelocitiyMeasurement.setTimestamp(timestamp);

        Timeseries<BigDecimal> soundVelocitiyTimeseries = new Timeseries<>();
        soundVelocitiyTimeseries.setFeature(feature);
        soundVelocitiyTimeseries.setSensor(sensorId);
        soundVelocitiyTimeseries.setPhenomenon("sound-velocitiy");
        soundVelocitiyTimeseries.setUnit("m/s");
        soundVelocitiyTimeseries.addMeasurementsItem(soundVelocitiyMeasurement);

        Dataset result = new Dataset();
        result.addTimeseriesItem(pressureTimeseries);
        result.addTimeseriesItem(subseaTemperatureTimeseries);
        result.addTimeseriesItem(conductivityTimeseries);
        result.addTimeseriesItem(salinityTimeseries);
        result.addTimeseriesItem(soundVelocitiyTimeseries);
        result.addTimeseriesItem(receiverLatencyTimeseries);
        result.setId("ctd-" + result.hashCode());

        return result;
    }

}
