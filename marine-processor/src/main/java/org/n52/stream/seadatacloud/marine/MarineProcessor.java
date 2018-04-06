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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.stream.core.DataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.cloud.stream.messaging.Processor;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
@SpringBootApplication
@EnableBinding(Processor.class)
@EnableConfigurationProperties(MarineProcessorConfiguration.class)
public class MarineProcessor {

    private int msgCount = 0;

    @Autowired
    private MarineProcessorConfiguration properties = new MarineProcessorConfiguration();

    private static final Logger LOG = LoggerFactory.getLogger(MarineProcessor.class);

    public static void main(String[] args) {
        SpringApplication.run(MarineProcessor.class, args);
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public DataMessage process(Message<String> mqttMessage) {
        if (mqttMessage == null) {
            String msg = "NO MQTT message received! Input is 'null'.";
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
        String mqttMessagePayload = mqttMessage.getPayload();
        String mqttTopic = mqttMessage.getHeaders().get("mqtt_receivedTopic", String.class);
        msgCount++;
        if (mqttTopic == null || mqttTopic.isEmpty()) {
            String msg = "MQTT topic not specified.";
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
        if (mqttMessagePayload == null || mqttMessagePayload.isEmpty()) {
            String msg = "Empty MQTT payload received.";
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
        DataMessage processedDataset = processMarineMqttPayload(mqttTopic, mqttMessagePayload);
        LOG.info("Processed dataset #{}", msgCount);
        LOG.trace("DataMessage: \n{}", processedDataset);
        return processedDataset;
    }

    private DataMessage processMarineMqttPayload(String mqttTopic, String mqttMessagePayload) {
        LOG.trace("MQTT-Payload received: {}", mqttMessagePayload);

        String[] payloadChunks = mqttMessagePayload.split("\\|");
        if (payloadChunks.length != 3) {
            String msg = String.format(
                    "Received mqtt payload not in correct format. Expected three '|' separated chunks: '%s'",
                    mqttMessagePayload);
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }

        // TIMESTAMP
        LOG.trace("Receiver Station Timestamp chunk: '{}'", payloadChunks[0]);
        OffsetDateTime receiverStationTimestamp = OffsetDateTime.parse(payloadChunks[0]);

        // Sensor
        String sensor = payloadChunks[1];
        LOG.trace("Sensor chunk   : '{}'", sensor);

        // Process values
        LOG.trace("Data chunk     : '{}", payloadChunks[2]);
        List<String> values = Stream.of(payloadChunks[2].split("\\s+"))
                .filter(value -> (value!=null && !value.isEmpty()))
                .collect(Collectors.toList());
        // switch depending on topic to different marine processors
        // which one to choose is configured in application.yml::processor.*.topic
        if (properties.getCtd().getTopic().equals(mqttTopic)) {
            return new ProcessorCtd().process(receiverStationTimestamp, sensor, properties.getCtd().getFeatureId(),
                    values);
        } else if (properties.getWeather().getTopic().equals(mqttTopic)) {
            return new ProcessorWeather().process(receiverStationTimestamp, sensor,
                    properties.getWeather().getFeatureId(), values);
        } else if (properties.getFluorometer().getTopic().equals(mqttTopic)) {
            return new ProcessorFluorometer().process(receiverStationTimestamp, sensor,
                    properties.getFluorometer().getFeatureId(), values);
        } else {
            String msg = String.format("Could not identify processor for topic '%s'.", mqttTopic);
            LOG.error(msg);
            throw new RuntimeException(new IllegalArgumentException(msg));
        }
    }

}