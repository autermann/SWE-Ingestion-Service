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
package org.n52.stream.seadatacloud.marineprocessor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.n52.stream.core.Dataset;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.cloud.stream.messaging.Processor;

/**
 * Processes MQTT messages from:<br>
 * <em>Host</em>: <code>mqtt.marine.ie</code><br>
 * <em>Port</em>: <code>1883</code><br>
 * <em>Topics</em>: <code>spiddal-ctd</code><br>
 * <br>
 * <em>Raw Payload</em>:
 * <code>2018-03-12T12:59:58.787Z|I-OCEAN7-304-0616641|  25.38   7.594  33.354  32.310 1477.9968 13:00:10.22</code><br>
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
 * @author Maurin Radtke <m.radtke@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
@SpringBootApplication
@EnableBinding(Processor.class)
public class MarineProcessorApp {

    // TODO set default value
    @Value("${processor.config.feature.id}")
    private String featureIdentifier;

    private static final Logger LOG = LoggerFactory.getLogger(MarineProcessorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(MarineProcessorApp.class, args);
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public Dataset process(String mqttMessagePayload) {
        if (mqttMessagePayload == null || mqttMessagePayload.isEmpty()) {
            // TODO what is convention in spring cloud? test via runtime exception bzw. subtypen
            String msg = "Empty MQTT payload received.";
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
        LOG.trace("Received MQTT payload: '{}'", mqttMessagePayload);
        Dataset processedDataset = parseWeather(mqttMessagePayload);
        LOG.info("Processed dataset: {}", processedDataset);
        return processedDataset;
    }

    private Dataset parseWeather(String mqttMessagePayload) {
        LOG.trace("MQTT-Payload received: {}", mqttMessagePayload);

        String[] payloadChunks = mqttMessagePayload.split("\\|");
        if (payloadChunks.length != 3) {
            String msg = String.format(
                    "Received mqtt payload not in correct format. Expected three '|' separated chunks: '%s'",
                    mqttMessagePayload);
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }

        Dataset result = new Dataset();

        // TIMESTAMP
        LOG.trace("Timestamp chunk: '{}'", payloadChunks[0]);
        OffsetDateTime timestamp = OffsetDateTime.parse(payloadChunks[0]);

        // Sensor
        String sensor = payloadChunks[1];
        LOG.trace("Sensor Id: '{}'", sensor);

        // Feature
        // TODO the feature and its location are unknown at this moment. Need to be configured somehow!
        Feature feature = new Feature();
        feature.setId(featureIdentifier);

        // Process values
        String[] values = payloadChunks[2].split("\\s+");
        if (values.length != 7) {
            String msg = String.format(
                    "Received mqtt payload not in correct format. Expected six '\t' separated chunks: '%s'",
                    payloadChunks[2]);
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
        Measurement<BigDecimal> pressureMeasurement = new Measurement<>();
        pressureMeasurement.setValue(new BigDecimal(values[1]));
        pressureMeasurement.setTimestamp(timestamp);

        Timeseries pressureTimeseries = new Timeseries();
        pressureTimeseries.setFeature(feature);
        pressureTimeseries.setPhenomenon("pressure");
        pressureTimeseries.setSensor(sensor);
        pressureTimeseries.setUnit("dbar");
        pressureTimeseries.addMeasurementsItem(pressureMeasurement);

        result.addTimeseriesItem(pressureTimeseries);

        Measurement<BigDecimal> subseaTemperatureMeasurement = new Measurement<>();
        subseaTemperatureMeasurement.setValue(new BigDecimal(values[2]));
        subseaTemperatureMeasurement.setTimestamp(timestamp);

        Timeseries subseaTemperatureTimeseries = new Timeseries();
        subseaTemperatureTimeseries.setFeature(feature);
        subseaTemperatureTimeseries.setPhenomenon("subsea-temperature");
        subseaTemperatureTimeseries.setSensor(sensor);
        subseaTemperatureTimeseries.setUnit("°C");
        subseaTemperatureTimeseries.addMeasurementsItem(subseaTemperatureMeasurement);

        result.addTimeseriesItem(subseaTemperatureTimeseries);

        Measurement<BigDecimal> conductivityMeasurement = new Measurement<>();
        conductivityMeasurement.setValue(new BigDecimal(values[3]));
        conductivityMeasurement.setTimestamp(timestamp);

        Timeseries conductivityTimeseries = new Timeseries();
        conductivityTimeseries.setFeature(feature);
        conductivityTimeseries.setPhenomenon("conductivity");
        conductivityTimeseries.setSensor(sensor);
        conductivityTimeseries.setUnit("mS/cm");
        conductivityTimeseries.addMeasurementsItem(conductivityMeasurement);

        result.addTimeseriesItem(conductivityTimeseries);

        Measurement<BigDecimal> salinityMeasurement = new Measurement<>();
        salinityMeasurement.setValue(new BigDecimal(values[4]));
        salinityMeasurement.setTimestamp(timestamp);

        Timeseries salinityTimeseries = new Timeseries();
        salinityTimeseries.setFeature(feature);
        salinityTimeseries.setPhenomenon("salinity");
        salinityTimeseries.setSensor(sensor);
        salinityTimeseries.setUnit("PSU");
        salinityTimeseries.addMeasurementsItem(salinityMeasurement);

        result.addTimeseriesItem(salinityTimeseries);

        Measurement<BigDecimal> soundVelocitiyMeasurement = new Measurement<>();
        soundVelocitiyMeasurement.setValue(new BigDecimal(values[5]));
        soundVelocitiyMeasurement.setTimestamp(timestamp);

        Timeseries soundVelocitiyTimeseries = new Timeseries();
        soundVelocitiyTimeseries.setFeature(feature);
        soundVelocitiyTimeseries.setPhenomenon("sound-velocitiy");
        soundVelocitiyTimeseries.setSensor(sensor);
        soundVelocitiyTimeseries.setUnit("m/s");
        soundVelocitiyTimeseries.addMeasurementsItem(soundVelocitiyMeasurement);

        result.addTimeseriesItem(soundVelocitiyTimeseries);

        Measurement<Long> instrumentTimeDeviationMeasurement = new Measurement<>();
        LocalTime instrumentTime = LocalTime.parse(values[6].substring(0, values[6].length()-1));
        Long instrumentTimeDeviation = ChronoUnit.MILLIS.between(instrumentTime, timestamp);
        instrumentTimeDeviationMeasurement.setValue(instrumentTimeDeviation);
        instrumentTimeDeviationMeasurement.setTimestamp(timestamp);

        Timeseries instrumentTimeDeviationTimeseries = new Timeseries();
        instrumentTimeDeviationTimeseries.setFeature(feature);
        instrumentTimeDeviationTimeseries.setPhenomenon("instrument-time-deviation");
        instrumentTimeDeviationTimeseries.setSensor(sensor);
        instrumentTimeDeviationTimeseries.setUnit("ms");
        instrumentTimeDeviationTimeseries.addMeasurementsItem(instrumentTimeDeviationMeasurement);

        result.addTimeseriesItem(instrumentTimeDeviationTimeseries);

        return result;
    }
}
