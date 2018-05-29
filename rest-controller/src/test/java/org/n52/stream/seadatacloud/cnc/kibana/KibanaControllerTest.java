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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KibanaControllerTest {

    private KibanaController kc;

    @Before
    public void setUp() {
       kc = new KibanaController();
       kc.init();
    }
     
    @Test
    public void testInitialization() throws IOException {
        Assert.assertThat(kc.getVisualization(), notNullValue());
        JsonNode visualizationNode = kc.getVisualizationNode(new ObjectMapper(), "kibanaTest", "streamTest");
        Assert.assertThat(visualizationNode, notNullValue());
        Assert.assertThat(visualizationNode.toString(), containsString("kibanaTest"));
        Assert.assertThat(visualizationNode.toString(), containsString("streamTest"));
    }
    
    @Test
    public void testGetIndexPatternFromResponse() throws IOException {
        String response = new BufferedReader(new InputStreamReader(new ClassPathResource("getIndexPattern.json").getInputStream())).lines()
        .collect(Collectors.joining("\n"));
        String indexPatternFromResponse = kc.getIndexPatternFromResponse(new ObjectMapper().readTree(response));
        Assert.assertThat(indexPatternFromResponse, equalTo("9f1721d0-624c-11e8-979e-815b9bb08403"));
    }
    
    @Test
    public void testCreateIndexPatternFromResponse() throws IOException {
        String response = new BufferedReader(new InputStreamReader(new ClassPathResource("createIndexPatter.json").getInputStream())).lines()
        .collect(Collectors.joining("\n"));
        String indexPatternFromResponse = kc.getIndexPatternFromResponse(new ObjectMapper().readTree(response));
        Assert.assertThat(indexPatternFromResponse, equalTo("48fb88e0-6314-11e8-954a-4da398c3eb77"));
    }
}
