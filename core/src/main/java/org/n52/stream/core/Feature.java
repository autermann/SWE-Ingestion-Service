/*
 * Copyright ${inceptionYear}-${latestYearOfContribution} 52&#176;North Initiative for Geospatial Open Source
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;

/**
 * Feature
 */
@Validated

public class Feature {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("properties")
    @Valid
    private List<SimpleEntry<String, String>> properties = null;

    @JsonProperty("geometry")
    private Object geometry = null;

    public Feature id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Feature properties(List<SimpleEntry<String, String>> properties) {
        this.properties = properties;
        return this;
    }

    public Feature addPropertiesItem(SimpleEntry<String, String> propertiesItem) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(propertiesItem);
        return this;
    }

    @Valid
    public List<SimpleEntry<String, String>> getProperties() {
        return properties;
    }

    public void setProperties(List<SimpleEntry<String, String>> properties) {
        this.properties = properties;
    }

    public Feature geometry(Object geometry) {
        this.geometry = geometry;
        return this;
    }

    public Object getGeometry() {
        return geometry;
    }

    public void setGeometry(Object geometry) {
        this.geometry = geometry;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feature feature = (Feature) o;
        return Objects.equals(id, feature.id) &&
                Objects.equals(properties, feature.properties) &&
                Objects.equals(geometry, feature.geometry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, properties, geometry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Feature {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    geometry: ").append(toIndentedString(geometry)).append("\n");
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
