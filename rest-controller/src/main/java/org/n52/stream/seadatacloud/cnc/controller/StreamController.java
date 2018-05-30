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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlObject;
import org.n52.janmayen.Json;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.CompositeOwsException;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceRequest;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.Parameter;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sensorML.v20.SimpleProcess;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.shetland.ogc.sensorML.v20.SmlFeatureOfInterest;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityConstants;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.shetland.ogc.sos.response.InsertSensorResponse;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweDataStream;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.shetland.ogc.swe.encoding.SweTextEncoding;
import org.n52.shetland.ogc.swe.simpleType.SweCount;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.shetland.ogc.swe.simpleType.SweTime;
import org.n52.stream.generate.InsertSensorGenerator;
import org.n52.stream.seadatacloud.cnc.CnCServiceConfiguration;
import org.n52.stream.seadatacloud.cnc.kibana.KibanaController;
import org.n52.stream.seadatacloud.cnc.model.AppOption;
import org.n52.stream.seadatacloud.cnc.model.Processors;
import org.n52.stream.seadatacloud.cnc.model.Sinks;
import org.n52.stream.seadatacloud.cnc.model.Source;
import org.n52.stream.seadatacloud.cnc.model.Sources;
import org.n52.stream.seadatacloud.cnc.model.Stream;
import org.n52.stream.seadatacloud.cnc.model.StreamStatus;
import org.n52.stream.seadatacloud.cnc.model.Streams;
import org.n52.stream.seadatacloud.cnc.service.CloudService;
import org.n52.stream.seadatacloud.cnc.util.DataRecordDefinitions;
import org.n52.stream.seadatacloud.cnc.util.ProcessDescriptionStore;
import org.n52.stream.seadatacloud.cnc.util.RestartStreamThread;
import org.n52.stream.util.DecoderHelper;
import org.n52.stream.util.EncoderHelper;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.decode.exception.XmlDecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
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
    private KibanaController kibanaController;

    private ProcessDescriptionStore processDescriptionStore;

    private String kibanaIndex;

    private InsertSensorGenerator generator;

    private static final String processDescriptionStoreFileName = "pds.dat";

    @PostConstruct
    public void init() {
        generator = new InsertSensorGenerator();
        dataRecordDefinitions = new DataRecordDefinitions();
        dataRecordDefinitions.add("https://52north.org/swe-ingestion/mqtt/3.1", "mqtt-source-rabbit");
        dataRecordDefinitions.add("https://52north.org/swe-ingestion/ftp", "ftp-source");

        LOG.info("loading stored streams from file...");
        Set<String> streamNames = new HashSet<>();
        File file = new File(processDescriptionStoreFileName);
        if (file.exists()) {
            try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(file))) {
                processDescriptionStore = (ProcessDescriptionStore) o.readObject();
                LOG.info("...finished loading processDescriptionStore.");
                // TODO: iterate streams: create & deploy:
                HashMap<String, AbstractMap.SimpleEntry<String, String>> map = processDescriptionStore.getDescriptions();
                ArrayList<RestartStreamThread> restartStreamThreads = new ArrayList<>();
                for (Map.Entry<String, SimpleEntry<String, String>> entry : map.entrySet()) {
                    String streamName = entry.getKey();
                    streamNames.add(streamName);
                    SimpleEntry<String, String> processType = entry.getValue();
                    String streamDefinition = processType.getValue();
                    restartStreamThreads.add(new RestartStreamThread(streamName, streamDefinition, service));
                }
                Sources sources = null;
                Processors processors = null;
                Sinks sinks = null;
                boolean appRegistered = false;
                do {
                    try {
                        Thread.sleep(5000);
                        sources = service.getSources();
                        processors = service.getProcessors();
                        sinks = service.getSinks();
                    } catch (Exception e) {
                    } // TODO: check for every single app?!
                    appRegistered = sources != null
                            && !sources.getSources().isEmpty()
                            && processors != null
                            && !processors.getProcessors().isEmpty()
                            && sinks != null
                            && !sinks.getSinks().isEmpty();
                } while (!appRegistered);

                // TODO: ThreadPool mit ExecutorService.
                for (RestartStreamThread rst : restartStreamThreads) {
                    rst.start();
                }
            } catch (Exception e) {
            }
        } else {
            processDescriptionStore = new ProcessDescriptionStore();
        }
        boolean kibanaInitialized = false;
        do {
            try {
                Thread.sleep(5000);
                kibanaInitialized = kibanaController.isInitialized();
            } catch (Exception e) {
            }
        } while (!kibanaInitialized);
        kibanaIndex = kibanaController.getOrCreateIndex();
        if (!streamNames.isEmpty()) {
            for (String streamName : streamNames) {
                kibanaController.checkOrCreateVisualization(kibanaIndex, streamName);
            }
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Streams> listStreams() {
        Streams result = service.getStreams();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private ResponseEntity<?> createStream(byte[] requestBody, String streamName) {
        try {
            String processDescription = new String(requestBody);

            Object decodedProcessDescription = decoderHelper.decode(processDescription);

            if (decodedProcessDescription instanceof AggregateProcess) {
                ArrayList<SmlComponent> al = (ArrayList<SmlComponent>) ((AggregateProcess) decodedProcessDescription)
                        .getComponents();
                SmlComponent comp = al.get(0);
                if (!(comp.getProcess() instanceof AbstractProcess)) {
                    return new ResponseEntity<>("{ \"error\": \"Process Descritiopn not containing instance of '"
                            + AbstractProcess.class.getName()
                            + "'.\"}",
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
                Source source = null;

                if (dataRecordDefinitions.hasDataRecordDefinition(sdrDefinition)) {
                    source = appController.getSourceByName(dataRecordDefinitions.getSourceType(sdrDefinition));
                } else {
                    return new ResponseEntity<>("{\"error\":\"No supported Source found for DataRecord definition '"
                            + sdrDefinition
                            + "'\"}",
                            HttpStatus.NOT_FOUND);
                }
                if (source == null) {
                    return new ResponseEntity<>("{ \"error\": \"DataRecord definition '" + sdrDefinition
                            + "' is supposed to be supported by Source '"
                            + sourceName
                            + "', but Source '"
                            + sourceName
                            + "' not found.\"}",
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
                        return new ResponseEntity<>("{ \"error\": \"swe:Text definition '"
                                + optionUrl
                                + "' requires a hashtag ( # ) option.\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    AppOption ao = appController.getSourceOptionByName(source, appOptionName);
                    if (ao == null) {
                        return new ResponseEntity<>("{ \"error\": \"Option '"
                                + appOptionName
                                + "' is not supported by source '"
                                + sourceName
                                + "'.\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    streamSourceDefinition += " --" + ao.getName() + "=" + sweText.getValue();
                }
                if (sourceName.equalsIgnoreCase("ftp-source")) {
                    streamSourceDefinition += " --mode=lines --with-markers=true --time-unit=MINUTES --fixed-delay=15 --initial-delay=0"; 
                }
                if (streamSourceDefinition.length() > 0) {
                    streamDefinition = sourceName + streamSourceDefinition + " ";
                } else {
                    streamDefinition = sourceName + " ";
                }

                PhysicalSystem process = getProcess((AggregateProcess) decodedProcessDescription);
                String sensor = process.getIdentifier();
                // check capabilites if sensor is occurs in contents section
                String offering = checkCapabilities(sensor);
                // if sensor occurs in contents section, offering is not empty
                String lastSeenDateTime = "";
                if (offering != null && !offering.isEmpty()) {
                    //get last seen timestamp via GetDataAvailability
                    Object gdaResponse = executeRequest(generateGDARequest(sensor));
                    if (gdaResponse instanceof GetDataAvailabilityResponse) {
                        lastSeenDateTime = getDateTime((GetDataAvailabilityResponse) gdaResponse);
                    }
                } else {
                    // if sensor is not inserted, insert sensor
                    InsertSensorRequest request = generateInsertSensor(process);
                    Object decodedResponse = executeRequest(request);
                    if (decodedResponse instanceof CompositeOwsException) {
                        // SOS error message returned
                        CompositeOwsException compositeOwsException = (CompositeOwsException) decodedResponse;
                        if (compositeOwsException.getCause() instanceof CodedException
                                && ((CodedException) compositeOwsException.getCause()).getLocator().equalsIgnoreCase("offeringIdentifier")) {
                            offering = compositeOwsException.getCause().getMessage().split("'")[1];
                            sensor = process.getIdentifier();
                        }
                    }
                    if (decodedResponse instanceof InsertSensorResponse) {
                        InsertSensorResponse isr = (InsertSensorResponse) decodedResponse;
                        offering = isr.getAssignedOffering();
                        sensor = isr.getAssignedProcedure();
                        LOG.info(getSensorInsertedLog(offering, sensor));
                        isr.close();
                    } else if (offering.isEmpty() || sensor.isEmpty()) {
                        String msg = String.format(
                                "XML document received from '%s' isn't sml2.0:InsertSensorResponse! Received: %s",
                                properties.getSosendpoint(),
                                decodedResponse.getClass());
                        LOG.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }

                Integer componentIdentifier = 0;
                // parse processor(s) and sink(s) according source type:
                String featureOfInterestId = "";
                for (SmlComponent smlComponent : al) {
                    if (smlComponent.getProcess() instanceof PhysicalSystem) {
                        PhysicalSystem ps = (PhysicalSystem) smlComponent.getProcess();
                        if (ps.getSmlFeatureOfInterest() != null) {
                            if (ps.getSmlFeatureOfInterest() instanceof SmlFeatureOfInterest) {
                                SmlFeatureOfInterest featureOfInterest = ps.getSmlFeatureOfInterest();
                                featureOfInterestId = featureOfInterest.getFeaturesOfInterest().iterator().next();
                            }
                        }
                    }
                }
                String timestampDefinitions = "";
                if (sourceName.equals("ftp-source")) {
                    SimpleProcess csvFileFilter = (SimpleProcess) al.get(1).getProcess();
                    List<Parameter> parameters = csvFileFilter.getParameters();
                    for (Parameter p : parameters) {
                        // get csv-file-filter:
                        SweAbstractDataComponent sweComponent = p.getParameter();
                        if (sweComponent instanceof SweCount) {
                            int headerLineCount = ((SweCount) sweComponent).getValue();
                            streamDefinition += "| csv-file-filter --number-of-header-lines=" + headerLineCount + " ";
                        }
                    }
                    boolean hasTimestampFilter = false;
                    for (Parameter p : parameters) {
                        // get csv-timestamp-filter:
                        SweAbstractDataComponent sweComponent = p.getParameter();
                        if (sweComponent instanceof SweText) {
                            if (!hasTimestampFilter) {
                                streamDefinition += "| csv-timestamp-filter";
                                if (lastSeenDateTime != null
                                        && !lastSeenDateTime.isEmpty()) {
                                        streamDefinition += " --last-seen-timestamp=" + lastSeenDateTime;
                                }
                                hasTimestampFilter = true;
                            }
                            SweText csvTimestampFilter = (SweText) sweComponent;
                            if (csvTimestampFilter.getDefinition().endsWith("date-column-format")) {
                                // case date:
                                String dateColumnFormat = csvTimestampFilter.getValue();
                                timestampDefinitions += " --date-column-format=" + dateColumnFormat;
                            } else if (csvTimestampFilter.getDefinition().endsWith("time-column-format")) {
                                // case time:
                                String timeColumnFormat = csvTimestampFilter.getValue();
                                timestampDefinitions += " --time-column-format=" + timeColumnFormat;
                            } else if (csvTimestampFilter.getDefinition().endsWith("datetime-column-format")) {
                                // case datetime:
                                String datetimeColumnFormat = csvTimestampFilter.getValue();
                                timestampDefinitions += " --date-column-format=" + datetimeColumnFormat
                                        + " --time-column-format=" + datetimeColumnFormat;
                            }
                        }
                    }
                    if (hasTimestampFilter) {
                        timestampDefinitions += " ";
                    } else {
                        return new ResponseEntity<>("{\"error\": \"The xml request body is no valid aggregateProcess sensorML description. AggregateProcess of '" + sdrDefinition + "' requires either date-column-format and time-column-format or datetime-column-format parameters, but found none of them.\"}", HttpStatus.BAD_REQUEST);
                    }
                    List<SmlIo> inputs = csvFileFilter.getInputs();
                    SmlIo ftpSmlIo = inputs.get(0);
                    SmlDataInterface dataInterface = (SmlDataInterface) ftpSmlIo.getIoValue();
                    SweDataStream dataStream = dataInterface.getData();
                    SweDataRecord dataRecord = (SweDataRecord) dataStream.getElementType();
                    LinkedList<SweField> recordFields = (LinkedList<SweField>) dataRecord.getFields();
                    for (int i = 0; i < recordFields.size(); i++) {
                        SweField sweField = recordFields.get(i);
                        if (sweField.getElement() instanceof SweTime) {
                            SweTime timeField = (SweTime) sweField.getElement();
                            String sweFieldDefinition = timeField.getDefinition();
                            if (sweFieldDefinition.contains("PhenomenonTime#")) {
                                switch (sweFieldDefinition.substring(sweFieldDefinition.lastIndexOf('#') + 1)) {
                                    case "date":
                                        timestampDefinitions += " --date-column-index=" + i;
                                        break;
                                    case "time":
                                        timestampDefinitions += " --time-column-index=" + i;
                                        break;
                                    case "datetime":
                                        timestampDefinitions += " --date-column-index=" + i
                                                + " --time-column-index=" + i;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                    SweAbstractEncoding encoding = dataStream.getEncoding();
                    if (encoding instanceof SweTextEncoding) {
                        SweTextEncoding textEncoding = (SweTextEncoding) encoding;
                        streamDefinition += timestampDefinitions + " --column-seperator=" + textEncoding.getTokenSeparator();
                    }

                } else if (sourceName.equals("mqtt-source-rabbit")) {
                }
                String commonAppProperties = " --sensormlurl="
                        + properties.getBaseurl()
                        + "/api/streams/"
                        + streamName
                        + " --offering=" + offering
                        + " --sensor=" + sensor;

                streamDefinition += " | csv-processor" + commonAppProperties
                        + " --componentidentifier=" + componentIdentifier
                        + " --featureofinterestid=" + "\"" + featureOfInterestId + "\""
                        + timestampDefinitions;

                // parse sink:
                streamDefinition += " | db-sink" + commonAppProperties
                        + " --url=" + properties.getDatasource().getUrl()
                        + " --username=" + properties.getDatasource().getUsername()
                        + " --password=" + properties.getDatasource().getPassword() + " ";

                Stream createdStream = null;
                Future<Stream> futureStream = service.createStream(streamName, streamDefinition, false);

                createdStream = futureStream.get(15, TimeUnit.SECONDS);

                if (createdStream != null) {
                    processDescriptionStore.addProcessDescription(streamName, processDescription, streamDefinition);
                    // InserObservation:

                    // store latest updates processDescriptionStore into file:
                    FileOutputStream f = new FileOutputStream(new File(processDescriptionStoreFileName));
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    o.writeObject(processDescriptionStore);
                    o.close();
                    f.close();

                    kibanaController.checkOrCreateVisualization(kibanaIndex, streamName);

                    return new ResponseEntity<>(createdStream, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("{\"error\": \"A stream with the name '"
                            + streamName
                            + " already exists.'\"}",
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
            LOG.error("Exception thrown:", e.getMessage());
            return new ResponseEntity<>("{\"error\": \"The xml request body is no valid "
                    + "aggregateProcess sensorML description. " + e.getMessage() + "\"}",
                    CONTENT_TYPE_APPLICATION_JSON,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createStream(@RequestBody byte[] requestBody
    ) {
        String streamName = "s" + UUID.randomUUID().toString();
        return this.createStream(requestBody, streamName);
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
        if (processDescriptionStore.hasProcessDescriptionForStream(streamId)) {
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
        FileOutputStream f = null;
        Stream stream = service.getStream(streamId);
        if (stream == null) {
            return new ResponseEntity<>("{\"error\":\"Stream '" + streamId + "' not found.\"}", CONTENT_TYPE_APPLICATION_JSON, HttpStatus.NOT_FOUND);
        }
        String result = service.deleteStream(streamId);
        processDescriptionStore.deleteProcessDescription(streamId);
        // store latest updates processDescriptionStore into file:
        try (ObjectOutputStream o = new ObjectOutputStream(f)) {
            f = new FileOutputStream(new File(processDescriptionStoreFileName));
            o.writeObject(processDescriptionStore);
            o.close();
            f.close();
            return new ResponseEntity<>(result, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.NO_CONTENT);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(StreamController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(StreamController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ResponseEntity<>(result, CONTENT_TYPE_APPLICATION_JSON, HttpStatus.NO_CONTENT);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/{streamId}", consumes = "application/xml")
    public ResponseEntity<?> updateStream(
            @PathVariable String streamId,
            @RequestBody byte[] requestBody) {
        // 1. delete stream 'streamId'
        Stream stream = service.getStream(streamId);
        if (stream == null) {
            return new ResponseEntity<>("{\"error\":\"Stream '" + streamId + "' not found.\"}", HttpStatus.NOT_FOUND);
        }
        String streamStatus = stream.getStatus();
        service.deleteStream(streamId);

        // 2. create Stream from requestBody with name 'streamName'
        this.createStream(requestBody, streamId);

        // 3. set stream Status to status of previous stream
        StreamStatus newStreamStatus = new StreamStatus();
        newStreamStatus.setStatus(streamStatus);
        ResponseEntity<?> result = updateStreamStatus(streamId, newStreamStatus);
        return result;
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
                return new ResponseEntity<>("{\"Accepted\": \"The Stream '" + streamId
                        + "' is currently 'deploying' and thus, the resource' status will not be changed.\"}",
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
                    return new ResponseEntity<>("{\"error\":\"The requested status '" + newStreamStatus.getStatus()
                            + "' is not supported. Supported status are: 'deployed' and 'undeployed'.\"}",
                            HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void logAndThrowException(URI sensormlUrl, OwsServiceRequest request, Exception e) throws RuntimeException {
        String msg = String.format("Error while requesting SOS instance ('%s') with operation ('%s'):"
                + " %s (set loglevel to 'TRACE' for stacktrace)",
                sensormlUrl.toString(),
                request.getOperationName(),
                e.getMessage());
        LOG.error(msg);
        LOG.trace("Exception thrown: ", e);
        throw new RuntimeException(e);
    }

    private void logAndThrowException(URI sensormlUrl, Exception e) throws RuntimeException {
        String msg = String.format("Error while requesting SOS instance ('%s') with operation ('%s'):"
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

    private String checkCapabilities(String sensor)
            throws XmlDecodingException,
            DecodingException,
            EncodingException,
            URISyntaxException {
        GetCapabilitiesRequest request = new GetCapabilitiesRequest();
        request.setService(Sos2Constants.SOS).setVersion(Sos2Constants.SERVICEVERSION);
        List<String> sections = new LinkedList<>();
        sections.add(SosConstants.CapabilitiesSections.Contents.name());
        request.setSections(sections);
        Object response = executeRequest(request);
        if (response instanceof GetCapabilitiesResponse
                && ((GetCapabilitiesResponse) response).getCapabilities() instanceof SosCapabilities) {
            SosCapabilities capabilities = (SosCapabilities) ((GetCapabilitiesResponse) response).getCapabilities();
            if (capabilities.getContents().isPresent() && !capabilities.getContents().get().isEmpty()) {
                for (SosObservationOffering obsOff : capabilities.getContents().get()) {
                    if (obsOff.getProcedures().contains(sensor)) {
                        return obsOff.getOffering().getIdentifier();
                    }
                }
            }
        }
        return "";
    }

    private PhysicalSystem getProcess(AggregateProcess aggregateProcess) {
        List<SmlComponent> componentsList = aggregateProcess.getComponents();
        return componentsList.get(componentsList.size() - 1).getProcess() instanceof PhysicalSystem
                ? (PhysicalSystem) componentsList.get(componentsList.size() - 1).getProcess()
                : null;
    }

    private GetDataAvailabilityRequest generateGDARequest(String sensor) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest();
        request.setService(Sos2Constants.SOS).setVersion(Sos2Constants.SERVICEVERSION);
        request.setResponseFormat(GetDataAvailabilityConstants.NS_GDA);
        request.addProcedure(sensor);
        return request;
    }

    private String getDateTime(GetDataAvailabilityResponse respoonse) {
        if (respoonse != null && respoonse.getDataAvailabilities() != null && !respoonse.getDataAvailabilities().isEmpty()) {
            for (DataAvailability da : respoonse.getDataAvailabilities()) {
                if (!da.getPhenomenonTime().isReferenced()) {
                    return da.getPhenomenonTime().getEnd().toString();
                }
            }
        }
        return null;
    }

    private InsertSensorRequest generateInsertSensor(PhysicalSystem process) {
        return generator.generate(process);
    }

    private Object executeRequest(OwsServiceRequest request) throws XmlDecodingException, DecodingException, EncodingException, URISyntaxException {
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
            logAndThrowException(sosEndpoint, request, e);
        }
        if (!responseDocument.getStatusCode().is2xxSuccessful()) {
            logAndThrowException(sosEndpoint, request, new RuntimeException("HttpResponseCode != 2xx."));
        }
        return decoderHelper.decode(responseDocument.getBody());
    }

    protected JsonNodeFactory nodeFactory() {
        return Json.nodeFactory();
    }

}
