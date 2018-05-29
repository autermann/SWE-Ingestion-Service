/*
 * Copyright (C) 2018-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.stream.seadatacloud.cnc.kibana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.n52.stream.seadatacloud.cnc.CnCServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class KibanaController {

    private static final Logger LOG = LoggerFactory.getLogger(KibanaController.class);
    private static final String API = "/api/";

    @Autowired
    private CnCServiceConfiguration properties;

    @Autowired
    private ObjectMapper objectMapper;

    private String visualization;
    
    private String fields = "[{\"name\": \"@timestamp\",\"type\": \"date\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"@version\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"_id\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": false},{\"name\": \"_index\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": false},{\"name\": \"_score\",\"type\": \"number\",\"searchable\": false,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"_source\",\"type\": \"_source\",\"searchable\": false,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"_type\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": false},{\"name\": \"class\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"class.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"geoip.ip\",\"type\": \"ip\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"geoip.latitude\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"geoip.location\",\"type\": \"geo_point\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"geoip.longitude\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"host\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"host.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"level\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"level.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"message\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"port\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"rawtext\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"rawtext.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.feature.id\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.DataMessage.timeseries.feature.id.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.measurements.phenomenonTime\",\"type\": \"date\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.measurements.value\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.phenomenon\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.DataMessage.timeseries.phenomenon.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.sensor\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.DataMessage.timeseries.sensor.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.DataMessage.timeseries.unit\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.DataMessage.timeseries.unit.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.IngestObservation.feature\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.IngestObservation.feature.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.IngestObservation.persisted\",\"type\": \"boolean\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.IngestObservation.phenomenon\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.IngestObservation.phenomenon.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.IngestObservation.phenomenontime\",\"type\": \"date\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.IngestObservation.procedure\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"statistics.IngestObservation.procedure.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.number\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"statistics.of\",\"type\": \"number\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"streamName\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"streamName.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"thread\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"thread.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true},{\"name\": \"time\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": false,\"readFromDocValues\": false},{\"name\": \"time.keyword\",\"type\": \"string\",\"searchable\": true,\"aggregatable\": true,\"readFromDocValues\": true}]";

    @PostConstruct
    public void init() {
        try {
            visualization = new BufferedReader(new InputStreamReader(new ClassPathResource("visualization.json").getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOG.error("Error while loading visualization file!", e);
        }
        LOG.trace("{} initialized!", KibanaController.class.getSimpleName());
    }

    public boolean isInitialized() throws IOException {
        BufferedReader in = null;
        try {
            URL url = new URL(properties.getKibana());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            boolean initialized = conn.getResponseCode() == 200 ? true : false;
            conn.disconnect();
            return initialized;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
    }

    public String getOrCreateIndex() {
        String indexPattern = getIndexPattern();
        return indexPattern != null && !indexPattern.isEmpty() ? indexPattern : createIndexPattern();
    }

    private String getIndexPattern() {
        BufferedReader in = null;
        try {
            URL url = new URL(getSavedObjectsUrl() + "index-pattern");
    
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
    
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            JsonNode node = objectMapper.readTree(res.toString());
            return getIndexPatternFromResponse(node);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
        return null;
    }

    private String createIndexPattern() {
        BufferedReader in = null;
        try {
            URL url = new URL(getSavedObjectsUrl() + "index-pattern");
    
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("kbn-xsrf", "true");
            conn.setRequestMethod("POST");
    
            conn.setDoOutput(true);
            ObjectNode attributes = objectMapper.createObjectNode();
            attributes.set("title", objectMapper.getNodeFactory().textNode("logstash-*"));
            attributes.set("timeFieldName", objectMapper.getNodeFactory().textNode("@timestamp"));
            attributes.set("fields", objectMapper.getNodeFactory().textNode(fields));
            ObjectNode on = objectMapper.createObjectNode();
            on.set("attributes", attributes);
            conn.getOutputStream().write(objectMapper.writeValueAsBytes(on));
    
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            setDefaultIndex();
            return getIndexPatternFromResponse(objectMapper.readTree(res.toString()));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
        return getIndexPattern();
    }

    private void setDefaultIndex() {
        BufferedReader in = null;
        try {
            URL url = new URL(getBaseUrl() + "kibana/settings/defaultIndex");
    
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("kbn-xsrf", "anything");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestMethod("POST");
    
            conn.setDoOutput(true);
            ObjectNode on = objectMapper.createObjectNode();
            on.set("value", objectMapper.getNodeFactory().textNode("logstash-*"));
            conn.getOutputStream().write(objectMapper.writeValueAsBytes(on));
    
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            if (conn.getResponseCode() != 200) {
                LOG.error("Error while setting default index: {}", res.toString());
            }
            in.close();
            conn.disconnect();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
    }

    public boolean checkOrCreateVisualization(String kibanaIndex, String streamName) {
        return !checkVisualization(streamName) ? createVisualization(kibanaIndex, streamName) : false;
    }

    private boolean checkVisualization(String streamName) {
        BufferedReader in = null;
        try {
            URL url = new URL(getSavedObjectsUrl() + "visualization/" + streamName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            JsonNode node = objectMapper.readTree(res.toString());
            return node.findPath("id").isMissingNode();
        } catch (IOException e) {
            LOG.trace("Visualization '{}' does not exist", streamName);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
        return false;
    }

    private boolean createVisualization(String kibanaIndex, String streamName) {
        BufferedReader in = null;
        try {
            JsonNode node = getVisualizationNode(objectMapper, kibanaIndex, streamName);
            
            URL url = new URL(getSavedObjectsUrl() + "visualization/" + streamName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("kbn-xsrf", "true");
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.getOutputStream().write(objectMapper.writeValueAsBytes(node));
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
        return false;
    }
    
    private String getBaseUrl() {
        return properties.getKibana() + API;
    }
    
    private String getSavedObjectsUrl() {
        return getBaseUrl() + "saved_objects/";
    }
    
    protected String getIndexPatternFromResponse(JsonNode node) {
        if (node != null && !node.findPath("id").isMissingNode() && !node.findPath("id").asText().isEmpty()) {
            return node.findValue("id").textValue();
        }
        return null;
    }

    protected JsonNode getVisualizationNode(ObjectMapper mapper, String kibanaIndex, String streamName) throws IOException {
        return mapper.readTree(
                visualization.replaceAll("@KibanaIndex", kibanaIndex).replaceAll("@StreamName", streamName));
    }
    
    protected String getVisualization() {
        return visualization;
    }

}
