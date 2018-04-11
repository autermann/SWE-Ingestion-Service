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

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorSkeleton {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorSkeleton.class);


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

    protected RuntimeException createInvalidNumberOfValuesException(String numberOfExpectedValues, List<String> values) {
        String msg = String.format(
                "Received mqtt payload not in correct format. Expected %s 'space' separated chunks: '%s'",
                numberOfExpectedValues,
                values);
        LOG.error(msg);
        return new RuntimeException(new IllegalArgumentException(msg));
    }

    private void throwException(String parameterName, Object parameter) throws RuntimeException {
        String msg = String.format("Received parameter %s is invalid: '%s'", parameterName, parameter);
        LOG.error(msg);
        throw new RuntimeException(new IllegalArgumentException(msg));
    }

}
