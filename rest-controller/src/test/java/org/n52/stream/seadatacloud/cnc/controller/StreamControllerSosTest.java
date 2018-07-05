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
package org.n52.stream.seadatacloud.cnc.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.shetland.ogc.ows.exception.CompositeOwsException;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.stream.seadatacloud.cnc.kibana.KibanaControllerMock;
import org.n52.stream.seadatacloud.cnc.service.CloudService;
import org.n52.stream.seadatacloud.cnc.util.DefinitionToSourceAppMapping;
import org.n52.stream.util.DecoderHelper;
import org.n52.stream.util.EncoderHelper;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.decode.exception.XmlDecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { StreamController.class, DecoderHelper.class, EncoderHelper.class, CloudService.class,
        ObjectMapper.class, AppController.class, DefinitionToSourceAppMapping.class, KibanaControllerMock.class })
@ActiveProfiles("sos")
@Ignore
public class StreamControllerSosTest {
    
    @Autowired
    private StreamController streamController;
    @Autowired
    private DecoderHelper decoderHelper;

    private AggregateProcess aggregateProcess;

    private PhysicalSystem process;

    @Before
    public void setUp() throws DecodingException, XmlException, IOException {
        Path pathDwd = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "AggregateProcess-Weather.xml");
        aggregateProcess = (AggregateProcess) decoderHelper.decode(pathDwd);
        process = streamController.getProcess(aggregateProcess);
        streamController.generateInsertSensor(process);
    }
    
    @Test
    public void check_getProcessidentifier() {
        assertThat(process.getIdentifier(), equalTo("AIRMAR-RINVILLE-1"));
    }
    
    @Test
    public void check_checkCapabilites_return_empty() throws XmlDecodingException, DecodingException, EncodingException, URISyntaxException {
        String offering = streamController.checkCapabilities(process.getIdentifier());
        assertThat(offering, notNullValue());
        assertThat(offering.isEmpty(), is(true));
    }
    
    @Test
    public void check_generateGDA() throws XmlDecodingException, DecodingException, EncodingException, URISyntaxException {
        GetDataAvailabilityRequest request = streamController.generateGDARequest(process.getIdentifier());
        assertThat(request.getService(), equalTo("SOS"));
        assertThat(request.getVersion(), equalTo("2.0.0"));
        assertThat(request.getOperationName(), equalTo("GetDataAvailability"));
        assertThat(request.isSetProcedure(), is(true));
        assertThat(request.isSetProcedures(), is(true));
        assertThat(request.getProcedures().size(), is(1));
        assertThat(request.getProcedures().iterator().next(), equalTo("AIRMAR-RINVILLE-1"));
    }
    
    @Test
    public void check_executeGDASensor() throws XmlDecodingException, DecodingException, EncodingException, URISyntaxException {
        GetDataAvailabilityRequest request = streamController.generateGDARequest(process.getIdentifier());
        Object response = streamController.executeRequest(request);
        assertThat(response, notNullValue());
        assertThat(response, instanceOf(CompositeOwsException.class));
        CompositeOwsException exc = (CompositeOwsException) response;
        assertThat(exc.getMessage(), equalTo("The value 'AIRMAR-RINVILLE-1' of the parameter 'procedure' is invalid"));
    }
    
    @Test
    public void check_executeGDA() throws XmlDecodingException, DecodingException, EncodingException, URISyntaxException {
        GetDataAvailabilityRequest request = streamController.generateGDARequest("");
        request.setProcedure(Collections.emptyList());
        Object response = streamController.executeRequest(request);
        assertThat(response, notNullValue());
        assertThat(response, instanceOf(GetDataAvailabilityResponse.class));
        GetDataAvailabilityResponse r = (GetDataAvailabilityResponse) response;
        assertThat(r.getDataAvailabilities(), notNullValue());
        assertThat(r.getDataAvailabilities().isEmpty(), is(true));
    }

}
