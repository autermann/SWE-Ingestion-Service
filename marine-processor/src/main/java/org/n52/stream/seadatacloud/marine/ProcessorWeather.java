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

import org.n52.stream.core.Dataset;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorWeather extends ProcessorSkeleton {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorWeather.class);

    public Dataset process(OffsetDateTime timestamp, String sensorId, String featureId, List<String> values) {
        validateInput(timestamp, sensorId, featureId, values);

        Dataset dataset = new Dataset();
        LOG.info("Weather data processing: " + timestamp);
        Timeseries<Object> timeseries = new Timeseries<>().
                feature(new Feature().id(featureId)).
                sensor(sensorId);
        Measurement<Object> measurement = new Measurement<>();
        measurement.setTimestamp(timestamp);
        timeseries.addMeasurementsItem(measurement);
        dataset.addTimeseriesItem(timeseries);
        dataset.setId("weather-" + dataset.hashCode());
        return dataset;
    }

}
