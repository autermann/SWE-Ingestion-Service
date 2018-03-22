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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 *
 */
@Validated
@ConfigurationProperties("processor.marine")
public class MarineProcessorConfiguration {

    private final TopicConfiguration topicConfiguration = new TopicConfiguration();

    private final TopicConfiguration weather = new TopicConfiguration();

    private final TopicConfiguration fluorometer = new TopicConfiguration();

    public TopicConfiguration getWeather() {
        return weather;
    }

    public TopicConfiguration getCtd() {
        return topicConfiguration;
    }

    public TopicConfiguration getFluorometer() {
        return fluorometer;
    }

    public static class TopicConfiguration {

        private List<String> sensors = Collections.emptyList();

        private String featureId = "";

        public void setFeatureId(String featureId) {
            this.featureId = featureId;
        }

        public String getFeatureId() {
            return featureId;
        }

        public void setSensors(List<String> sensors) {
            this.sensors = sensors;
        }

        public List<String> getSensors() {
            return sensors;
        }
    }

}
