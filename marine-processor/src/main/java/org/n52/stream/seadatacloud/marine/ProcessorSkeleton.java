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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorSkeleton {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorSkeleton.class);


    protected RuntimeException createInvalidNumberOfValuesException(String numberOfExpectedValues, List<String> values) {
        String msg = String.format(
                "Received mqtt payload not in correct format. Expected %s 'space' separated chunks: '%s'",
                numberOfExpectedValues,
                values);
        LOG.error(msg);
        return new RuntimeException(new IllegalArgumentException(msg));
    }

    public void validateInput(OffsetDateTime timestamp, String sensorId, String featureId, List<String> values)
            throws RuntimeException {
        if (timestamp == null) {
            throwException("timestamp", timestamp);
        }
        if (sensorId == null || sensorId.isEmpty()) {
            throwException("sensorId", sensorId);
        }
        if (featureId == null || featureId.isEmpty()) {
            throwException("featureId", featureId);
        }
        if (values == null || values.isEmpty()) {
            throwException("values", values);
        }
    }

    private void throwException(String parameterName, Object parameter) throws RuntimeException {
        String msg = String.format("Received parameter %s is invalid: '%s'", parameterName, parameter);
        LOG.error(msg);
        throw new RuntimeException(new IllegalArgumentException(msg));
    }

}
