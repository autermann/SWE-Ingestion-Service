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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlObject;
import org.n52.janmayen.Json;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.CompositeOwsException;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.shetland.ogc.sos.response.InsertSensorResponse;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.stream.generate.InsertSensorGenerator;
import org.n52.stream.seadatacloud.cnc.CnCServiceConfiguration;
import org.n52.stream.seadatacloud.cnc.model.AppOption;
import org.n52.stream.seadatacloud.cnc.model.Source;
import org.n52.stream.seadatacloud.cnc.model.Stream;
import org.n52.stream.seadatacloud.cnc.model.StreamStatus;
import org.n52.stream.seadatacloud.cnc.model.Streams;
import org.n52.stream.seadatacloud.cnc.service.CloudService;
import org.n52.stream.seadatacloud.cnc.util.DataRecordDefinitions;
import org.n52.stream.seadatacloud.cnc.util.ProcessDescriptionStore;
import org.n52.stream.util.DecoderHelper;
import org.n52.stream.util.EncoderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RestController
@Component
@ImportResource("classpath*:/svalbard-*.xml")
@RequestMapping("/api/streams")
public class StreamController {

    private static final Logger LOG = LoggerFactory.getLogger(StreamController.class);

    private final HttpHeaders CONTENT_TYPE_APPLICATION_JSON = new HttpHeaders();
    private final HttpHeaders CONTENT_TYPE_APPLICATION_XML = new HttpHeaders();
    private final HttpHeaders HEADER_ACCEPT_ALL = new HttpHeaders();
    private final HttpHeaders HEADER_ACCEPT_XML = new HttpHeaders();

    @Autowired
    CloudService service;

    @Autowired
    AppController appController;

    @Autowired
    CnCServiceConfiguration properties;

    @Autowired
    private DecoderHelper decoderHelper;

    @Autowired
    private EncoderHelper encoderHelper;

    @Autowired
    private DataRecordDefinitions dataRecordDefinitions;

    @Autowired
    private ProcessDescriptionStore processDescriptionStore;

    @PostConstruct
    public void init() {
        dataRecordDefinitions.add("https://52north.org/swe-ingestion/mqtt/3.1", "mqtt-source-rabbit");
        CONTENT_TYPE_APPLICATION_JSON.setContentType(MediaType.APPLICATION_JSON);
        CONTENT_TYPE_APPLICATION_XML.setContentType(MediaType.APPLICATION_XML);
        List<MediaType> defaultMediaType = new ArrayList<>();
        defaultMediaType.add(MediaType.TEXT_HTML);
        defaultMediaType.add(MediaType.TEXT_PLAIN);
        defaultMediaType.add(MediaType.APPLICATION_JSON);
        defaultMediaType.remove(MediaType.APPLICATION_XML);
        HEADER_ACCEPT_ALL.setAccept(defaultMediaType);
        List<MediaType> xmlMediaType = new ArrayList<>();
        xmlMediaType.add(MediaType.APPLICATION_XML);
        HEADER_ACCEPT_XML.setAccept(xmlMediaType);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Streams> listStreams() {
        Streams result = service.getStreams();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createStream(@RequestBody byte[] requestBody) {
        try {
            String streamName = UUID.randomUUID().toString();
            String processDescription = new String(requestBody);

            Object decodedProcessDescription = decoderHelper.decode(processDescription);

            if (decodedProcessDescription instanceof AggregateProcess) {
                ArrayList<SmlComponent> al = (ArrayList<SmlComponent>) ((AggregateProcess) decodedProcessDescription)
                        .getComponents();
                SmlComponent comp = al.get(0);
                if (!(comp.getProcess() instanceof AbstractProcess)) {
                    return new ResponseEntity<>("{ \"error\": \"Process Descritiopn not containing instance of '" +
                            AbstractProcess.class.getName() +
                            "'.\"}",
                            CONTENT_TYPE_APPLICATION_JSON,
                            HttpStatus.BAD_REQUEST);
                }
                AbstractProcess asml = (AbstractProcess) comp.getProcess();
                ArrayList<SmlIo> smlOutputs = (ArrayList<SmlIo>) asml.getOutputs();
                SmlIo smlIo = smlOutputs.get(0);
                SmlDataInterface smlDataInterface = (SmlDataInterface) smlIo.getIoValue();
                SweDataRecord sdr = smlDataInterface.getInterfaceParameters();

                String sdrDefinition = sdr.getDefinition();
                String sourceName = "";
                Source source;

                if (dataRecordDefinitions.hasDataRecordDefinition(sdrDefinition)) {
                    source = appController.getSourceByName(dataRecordDefinitions.getSourceType(sdrDefinition));
                } else {
                    return new ResponseEntity<>("{\"error\":\"No supported Source found for DataRecord definition '" +
                            sdrDefinition +
                            "'\"}",
                            HttpStatus.NOT_FOUND);
                }
                if (source == null) {
                    return new ResponseEntity<>("{ \"error\": \"DataRecord definition '" + sdrDefinition +
                            "' is supposed to be supported by Source '" +
                            sourceName +
                            "', but Source '" +
                            sourceName +
                            "' not found.\"}",
                            HttpStatus.NOT_FOUND);
                }
                sourceName = source.getName();

                LinkedList<SweField> sweFields = (LinkedList<SweField>) sdr.getFields();

                String streamSourceDefinition = "";
                String streamDefinition = "";

                for (SweField current : sweFields) {
                    SweText sweText = (SweText) current.getElement();
                    String optionUrl = sweText.getDefinition();
                    String appOptionName;
                    if (optionUrl.indexOf('#') > -1) {
                        appOptionName = optionUrl.substring(optionUrl.lastIndexOf('#') + 1);
                    } else {
                        return new ResponseEntity<>("{ \"error\": \"swe:Text definition '" +
                                optionUrl +
                                "' requires a hashtag ( # ) option.\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    AppOption ao = appController.getSourceOptionByName(source, appOptionName);
                    if (ao == null) {
                        return new ResponseEntity<>("{ \"error\": \"Option '" +
                                appOptionName +
                                "' is not supported by source '" +
                                sourceName +
                                "'.\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    streamSourceDefinition += " --" + ao.getName() + "=" + sweText.getValue();
                };
                if (streamSourceDefinition.length() > 0) {
                    streamDefinition = sourceName + streamSourceDefinition + " ";
                } else {
                    streamDefinition = sourceName + " ";
                }

                InsertSensorGenerator generator = new InsertSensorGenerator();
                AggregateProcess aggregateProcess = (AggregateProcess) decodedProcessDescription;
                PhysicalSystem process = (PhysicalSystem) aggregateProcess.getComponents().get(1).getProcess();
                InsertSensorRequest request = generator.generate(process);
                // encode request
                XmlObject xbRequest = encoderHelper.encode(request);
                String insertSensor = xbRequest.xmlText();
                ResponseEntity<String> responseDocument = null;
                URI sosEndpoint = new URI(properties.getSosendpoint());
                try {
                    RestTemplate sosClient = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
                    headers.setContentType(MediaType.APPLICATION_XML);
                    HttpEntity<String> entity = new HttpEntity<>(insertSensor, headers);
                    responseDocument = sosClient.exchange(sosEndpoint, HttpMethod.POST, entity, String.class);
                } catch (RestClientException e) {
                    logAndThrowException(sosEndpoint, e);
                }
                if (!responseDocument.getStatusCode().is2xxSuccessful()) {
                    // TODO check for sensor already inserted error!
                    logAndThrowException(sosEndpoint, new RuntimeException("HttpResponseCode != 2xx."));
                }
                Object decodedResponse = decoderHelper.decode(responseDocument.getBody());
                String offering = "";
                String sensor = "";
                if (decodedResponse instanceof CompositeOwsException) {
                    // SOS error message returned
                    CompositeOwsException compositeOwsException = (CompositeOwsException) decodedResponse;
                    if (compositeOwsException.getCause() instanceof CodedException &&
                            ((CodedException) compositeOwsException.getCause()).getLocator().equalsIgnoreCase("offeringIdentifier")) {
                        offering = compositeOwsException.getCause().getMessage().split("'")[1];
                        sensor = process.getIdentifier();
                    }
                }
                if (decodedResponse instanceof InsertSensorResponse ) {
                    InsertSensorResponse isr = (InsertSensorResponse) decodedResponse;
                    offering = isr.getAssignedOffering();
                    sensor = isr.getAssignedProcedure();
                    LOG.info(getSensorInsertedLog(offering, sensor));
                    isr.close();
                } else if (offering.isEmpty() || sensor.isEmpty()){
                    String msg = String.format(
                            "XML document received from '%s' isn't sml2.0:InsertSensorResponse! Received: %s",
                            sosEndpoint,
                            decodedResponse.getClass());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                // parse processor:
                String commonAppProperties = " --sensormlurl=" +
                        properties.getBaseurl() +
                        "/api/streams/" +
                        "s" + streamName +
                        " --offering=" + offering +
                        " --sensor=" + sensor;
                streamDefinition += "| csv-processor" + commonAppProperties + " ";
                // parse sink:
                streamDefinition += "| db-sink" + commonAppProperties
                        + " --url=" + properties.getDatasource().getUrl()
                        + " --username=" + properties.getDatasource().getUsername()
                        + " --password=" + properties.getDatasource().getPassword() + " ";

                Stream createdStream = null;
                streamName = "s" + streamName;
                Future<Stream> futureStream = service.createStream(streamName, streamDefinition, false);

                createdStream = futureStream.get(15, TimeUnit.SECONDS);

                if (createdStream != null) {
                    processDescriptionStore.addProcessDescription(streamName, processDescription);
                    // InserObservation:

                    return new ResponseEntity<>(createdStream, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("{\"error\": \"A stream with the name '" +
                            streamName +
                            " already exists.'\"}",
                            CONTENT_TYPE_APPLICATION_JSON,
                            HttpStatus.CONFLICT);
                }
            } else {
                return new ResponseEntity<>("{\"error\": \"The xml request body is no valid "
                        + "aggregateProcess sensorML description.\"}",
                        CONTENT_TYPE_APPLICATION_JSON,
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            LOG.error("Exception thrown:", e);
            return new ResponseEntity<>(e.getMessage(), CONTENT_TYPE_APPLICATION_JSON, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/{streamId}", produces = "application/json")
    public ResponseEntity<?> getStream(
            @PathVariable String streamId) {
        Stream result = service.getStream(streamId);
        if (result == null) {
            return new ResponseEntity<>("{ \"error\": \"stream with name '" + streamId + "' not found.\"}",
                    CONTENT_TYPE_APPLICATION_JSON, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/{streamId}", produces = "application/xml")
    public ResponseEntity<?> getStreamProcessDescription(@PathVariable String streamId) {
        Stream result = service.getStream(streamId);
        if (result == null) {
            return new ResponseEntity<>("{ \"error\": \"stream with name '" + streamId + "' not found.\"}",
                    HttpStatus.NOT_FOUND);
        }
        if (processDescriptionStore.hasProcessDescrptionForStream(streamId)) {
            String sensorMLProcessDescription = processDescriptionStore.getProcessDescriptionForStream(streamId);
            if (sensorMLProcessDescription != null) {
                return new ResponseEntity<>(sensorMLProcessDescription, CONTENT_TYPE_APPLICATION_XML, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"error\": \"no sensorML process decription found for stream '"
                        + streamId + "'.\"}", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("{\"error\": \"no sensorML process decription found for stream '" + streamId
                    + "'.\"}", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * DELETE deletes a stream.
     *
     * @param streamId - name of the stream
     * @return succes or error message
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/{streamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteStream(
            @PathVariable String streamId) {
        String result = service.deleteStream(streamId);
        return new ResponseEntity<>(result, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.NO_CONTENT);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/{streamId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStreamStatus(
            @PathVariable String streamId,
            @RequestBody StreamStatus newStreamStatus) {
        Stream stream = service.getStream(streamId);
        if (stream == null) {
            return new ResponseEntity<>("{\"error\":\"Stream '" + streamId + "' not found.\"}", HttpStatus.NOT_FOUND);
        } else {
            String status = stream.getStatus();
            if (status.equals("deploying")) {
                return new ResponseEntity<>("{\"Accepted\": \"The Stream '" + streamId +
                        "' is currently 'deploying' and thus, the resource' status will not be changed.\"}",
                        HttpStatus.ACCEPTED);
            }
            if (newStreamStatus.getStatus() == null) {
                return new ResponseEntity<>("{\"error\":\"Request is missing required field 'status'.\"}",
                        HttpStatus.BAD_REQUEST);
            }
            switch (newStreamStatus.getStatus()) {
                case "deployed":
                    Stream deployedStream = service.deployStream(streamId);
                    return new ResponseEntity<>(deployedStream, HttpStatus.NO_CONTENT);
                case "undeployed":
                    Stream undeployedStream = service.undeployStream(streamId);
                    return new ResponseEntity<>(undeployedStream, HttpStatus.NO_CONTENT);
                default:
                    return new ResponseEntity<>("{\"error\":\"The requested status '" + newStreamStatus.getStatus() +
                            "' is not supported. Supported status are: 'deployed' and 'undeployed'.\"}",
                            HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void logAndThrowException(URI sensormlUrl, Exception e) throws RuntimeException {
        String msg = String.format("Error while inserting sensor in SOS instance ('%s') :"
                + " %s (set loglevel to 'TRACE' for stacktrace)",
                sensormlUrl.toString(),
                e.getMessage());
        LOG.error(msg);
        LOG.trace("Exception thrown: ", e);
        throw new RuntimeException(e);
    }

    private String getSensorInsertedLog(String offering, String sensor) {
        ObjectNode o = nodeFactory().objectNode();
        o.put("SOS", properties.getSosendpoint());
        o.put("sensor", sensor);
        o.put("offering", offering);
        ObjectNode n = nodeFactory().objectNode();
        
        n.set("InsertSensor", o);
        return n.toString();
    }
    
    protected JsonNodeFactory nodeFactory() {
        return Json.nodeFactory();
    }
}
