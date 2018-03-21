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
import java.util.Collections;

import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorSkeletonTest {

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnMissingTimestamp() {
        new ProcessorSkeleton().validateInput(null, null, null, null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnSensorIdNull() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), null, null, null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnSensorIdEmptyString() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "", null, null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnFeatureIdNull() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "sensorId", null, null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnFeatureIdEmptyString() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "sensorId", "", null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnValuesNull() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "sensorId", "featureId", null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionOnValuesEmptyList() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "sensorId", "featureId", Collections.emptyList());
    }

    @Test
    public void shouldNotThrowExceptionOnValidInput() {
        new ProcessorSkeleton().validateInput(OffsetDateTime.now(), "sensorId", "featureId", Collections.singletonList(""));
    }
}
