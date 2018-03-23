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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dataset
 */
@Validated

public class Dataset {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("timeseries")
    @Valid
    private List<Timeseries<?>> timeseries = null;

    public Dataset id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dataset timeseries(List<Timeseries<?>> timeseries) {
        this.timeseries = timeseries;
        return this;
    }

    public Dataset addTimeseriesItem(Timeseries<?> timeseriesItem) {
        if (timeseries == null) {
            timeseries = new ArrayList<>();
        }
        timeseries.add(timeseriesItem);
        return this;
    }

    @Valid
    public List<Timeseries<?>> getTimeseries() {
        return timeseries;
    }

    public void setTimeseries(List<Timeseries<?>> timeseries) {
        this.timeseries = timeseries;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dataset dataset = (Dataset) o;
        return Objects.equals(id, dataset.id) &&
                Objects.equals(timeseries, dataset.timeseries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeseries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Dataset {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    timeseries: ").append(toIndentedString(timeseries)).append("\n");
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
