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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.shetland.ogc.gml.CodeWithAuthority;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.om.OmObservableProperty;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.om.OmObservationConstellation;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.QuantityValue;
import org.n52.shetland.ogc.ows.exception.CompositeOwsException;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosProcedureDescriptionUnknownType;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.request.InsertObservationRequest;
import org.n52.shetland.ogc.sos.response.InsertSensorResponse;
import org.n52.stream.seadatacloud.cnc.kibana.KibanaControllerMock;
import org.n52.stream.seadatacloud.cnc.service.CloudService;
import org.n52.stream.seadatacloud.cnc.util.DataRecordDefinitions;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { StreamController.class, DecoderHelper.class, EncoderHelper.class, CloudService.class,
        ObjectMapper.class, AppController.class, DataRecordDefinitions.class, KibanaControllerMock.class })
@ActiveProfiles("sos")
@Ignore
public class StreamControllerSosXInsertTest {

    @Autowired
    private StreamController streamController;
    @Autowired
    private DecoderHelper decoderHelper;

    private AggregateProcess aggregateProcess;
    private PhysicalSystem process;
    private Object isResponse;
    private boolean alreadyInserted;

    @Before
    public void setUp()
            throws DecodingException,
            XmlException,
            IOException,
            EncodingException,
            URISyntaxException {
        Path pathDwd = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(),
                "AggregateProcess-Weather.xml");
        aggregateProcess = (AggregateProcess) decoderHelper.decode(pathDwd);
        process = streamController.getProcess(aggregateProcess);
//        isResponse = streamController.executeRequest(streamController.generateInsertSensor(process));
//        alreadyInserted = isResponse instanceof CompositeOwsException ? true : false;
    }

    @Test
    public void check_isResponse() {
        assertThat(isResponse, notNullValue());
        if (isResponse instanceof InsertSensorResponse) {
            assertThat(isResponse, instanceOf(InsertSensorResponse.class));
            InsertSensorResponse r = (InsertSensorResponse) isResponse;
        } else {
            assertThat(isResponse, instanceOf(CompositeOwsException.class));
            CompositeOwsException exc = (CompositeOwsException) isResponse;
            assertThat(exc.getMessage(), equalTo(
                    "The offering with the identifier 'AIRMAR-RINVILLE-1/observations' still exists in this service and it is not allowed to insert more than one procedure to an offering!"));
        }
    }

    @Test
    public void check_generateGDA()
            throws XmlDecodingException,
            DecodingException,
            EncodingException,
            URISyntaxException {
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
    public void check_executeGDA()
            throws XmlDecodingException,
            DecodingException,
            EncodingException,
            URISyntaxException {
        InsertObservationRequest ioRequest = createInsertObservation(process);
        streamController.executeRequest(ioRequest);
        GetDataAvailabilityRequest request = streamController.generateGDARequest(process.getIdentifier());
        Object response = streamController.executeRequest(request);
        assertThat(response, notNullValue());
        assertThat(response, instanceOf(GetDataAvailabilityResponse.class));
        GetDataAvailabilityResponse r = (GetDataAvailabilityResponse) response;
        assertThat(r.getDataAvailabilities(), notNullValue());
        assertThat(r.getDataAvailabilities().isEmpty(), is(false));
    }

    private InsertObservationRequest createInsertObservation(PhysicalSystem process) {
        InsertObservationRequest request =
                new InsertObservationRequest(Sos2Constants.SOS, Sos2Constants.SERVICEVERSION);
        request.setOfferings(Lists.newArrayList(process.getIdentifier() + "/observations"));
        OmObservation o = new OmObservation();
        o.setObservationConstellation(new OmObservationConstellation(
                new SosProcedureDescriptionUnknownType(process.getIdentifier(), null, null),
                new OmObservableProperty("http://vocab.nerc.ac.uk/collection/B39/current/airtemp/"),
                Sets.newHashSet(process.getIdentifier() + "/observations"),
                new SamplingFeature(new CodeWithAuthority("Marine Institute")), OmConstants.OBS_TYPE_MEASUREMENT));
        QuantityValue quantityValue = new QuantityValue(123.2, "degC");
        SingleObservationValue<BigDecimal> v = new SingleObservationValue<BigDecimal>();
        TimeInstant pt = new TimeInstant();
        pt.setValue(DateTime.now());
        v.setPhenomenonTime(pt);
        v.setValue(quantityValue);
        o.setValue(v);
        request.addObservation(o);
        return request;
    }

}