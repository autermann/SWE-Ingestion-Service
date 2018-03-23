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

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;

/**
 * <b>Timeseries&lt;T&gt;</b><br>
 * <br>
 * is a collection of {@link Measurement}s which share the following properties:
 * <ul>
 * <li>Unit</li>
 * <li>Phenomenon</li>
 * <li>Sensor</li>
 * <li>Feature</li>
 * </ul>
 * as explicit ones and the type as implicit via the generic parameter T.
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
@Validated
public class Timeseries<T> {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("unit")
    private String unit = null;

    @JsonProperty("phenomenon")
    private String phenomenon = null;

    @JsonProperty("sensor")
    private String sensor = null;

    @JsonProperty("feature")
    private Feature feature = null;

    @JsonProperty("measurements")
    @Valid
    private List<Measurement<T>> measurements = null;

    public Timeseries<T> id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timeseries<T> unit(String unit) {
        this.unit = unit;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Timeseries<T> phenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
        return this;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    public Timeseries<T> sensor(String sensor) {
        this.sensor = sensor;
        return this;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public Timeseries<T> feature(Feature feature) {
        this.feature = feature;
        return this;
    }

    @Valid
    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Timeseries<T> measurements(List<Measurement<T>> measurements) {
        this.measurements = measurements;
        return this;
    }

    public Timeseries<T> addMeasurementsItem(Measurement<T> measurementsItem) {
        if (measurements == null) {
            measurements = new ArrayList<>();
        }
        measurements.add(measurementsItem);
        return this;
    }

    @Valid
    public List<Measurement<T>> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement<T>> measurements) {
        this.measurements = measurements;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Timeseries<?> timeseries = (Timeseries<?>) o;
        return Objects.equals(id, timeseries.id) &&
                Objects.equals(unit, timeseries.unit) &&
                Objects.equals(phenomenon, timeseries.phenomenon) &&
                Objects.equals(sensor, timeseries.sensor) &&
                Objects.equals(feature, timeseries.feature) &&
                Objects.equals(measurements, timeseries.measurements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, unit, phenomenon, sensor, feature, measurements);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Timeseries {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
        sb.append("    phenomenon: ").append(toIndentedString(phenomenon)).append("\n");
        sb.append("    sensor: ").append(toIndentedString(sensor)).append("\n");
        sb.append("    feature: ").append(toIndentedString(feature)).append("\n");
        sb.append("    measurements: ").append(toIndentedString(measurements)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
