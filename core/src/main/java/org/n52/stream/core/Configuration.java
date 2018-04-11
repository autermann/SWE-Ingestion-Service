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
package org.n52.stream.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 *
 */
@Validated
@ConfigurationProperties("org.n52.stream")
public class Configuration {

    private String sensormlUrl = "";

    private String offering = "";

    private String sensor = "";

    public String getSensormlUrl() {
        return sensormlUrl;
    }

    public String getOffering() {
        return offering;
    }

    public void setSensormlUrl(String sensormlUrl) {
        this.sensormlUrl = sensormlUrl;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getSensor() {
        return sensor;
    }

}
