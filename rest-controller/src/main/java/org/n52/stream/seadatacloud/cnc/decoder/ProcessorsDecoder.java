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
package org.n52.stream.seadatacloud.cnc.decoder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.n52.stream.seadatacloud.cnc.model.Processor;
import org.n52.stream.seadatacloud.cnc.model.Processors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.stream.seadatacloud.cnc.CnCServiceConfiguration;
import org.n52.stream.seadatacloud.cnc.model.AppOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@EnableConfigurationProperties(CnCServiceConfiguration.class)
public class ProcessorsDecoder extends BaseDeserializer<Processors> {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorsDecoder.class);

    ObjectMapper objectMapper = new ObjectMapper();
       
    @Autowired
    private CnCServiceConfiguration properties;
    
    @Override
    public Processors deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        Processors results = new Processors();

        JsonNode node = jp.readValueAsTree();
        JsonNode embedded = node.get("_embedded");
        List<Processor> processorList = new ArrayList();
        if (embedded == null) {
            results.setProcessors(processorList);
            return results;
        }
        ArrayNode appRegistrationResourceList = (ArrayNode) embedded.get("appRegistrationResourceList");

        appRegistrationResourceList.forEach((proc) -> {
            Processor current = new Processor();
            current.setName(proc.get("name").asText());

            try {
                URL url = new URL(properties.getDataflowhost() + "/apps/" + "processor" + "/" + current.getName());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuffer res = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    res.append(line);
                    res.append("\n");
                }
                in.close();
                conn.disconnect();
                String response = res.toString();
                
                AppOptions aos = objectMapper.readValue(response, AppOptions.class);

                current.setOptions(aos.getAppOptions());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                e.printStackTrace();
            }
            processorList.add(current);
        });
        results.setProcessors(processorList);
        return results;
    }

}