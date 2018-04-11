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
package org.n52.stream.seadatacloud.restcontroller.controller;


import org.n52.stream.seadatacloud.restcontroller.service.CloudService;
import org.n52.stream.seadatacloud.restcontroller.model.Stream;
import org.n52.stream.seadatacloud.restcontroller.model.Streams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RestController
@Component
@RequestMapping("/api/streams")
public class FlowServerStreamController {

    public final String APPLICATION_JSON = "application/json";
    public final String APPLICATION_XML = "application/xml";

    @Autowired
    CloudService service;

//    @Autowired
//    private DecoderRepository decoderRepository;

//    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_JSON)
//    public ResponseEntity<String> createJsonStream(
//            @RequestBody Map<String, Object> payload) {
//
//        try {
//            String streamDefinition = "";
//            String streamName = (String) payload.get("name");
//            Stream stream = service.getStream(streamName);
//            if (stream != null) {
//                return new ResponseEntity("{Error: Stream " + streamName + " already exists.}", HttpStatus.CONFLICT);
//            }
//            Map<String, Object> source = (Map<String, Object>) payload.get("source");
//            String sourceName = (String) source.get("name");
//            Map<String, Object> processor = (Map<String, Object>) payload.get("processor");
//            String processorName = (String) processor.get("name");
//            Map<String, Object> sink = (Map<String, Object>) payload.get("sink");
//            String sinkName = (String) sink.get("name");
//            if (sourceName.equals("mqttrabbitsource")) {
//                streamDefinition = sourceName;
//                String mqttUrl = (String) source.get("url");
//                if (mqttUrl != null) {
//                    streamDefinition += " --url=" + mqttUrl;
//                    String mqttPort = (String) source.get("port");
//                    if (mqttPort != null) {
//                        streamDefinition += ":" + mqttPort;
//                    }
//                }
//                String mqttTopic = (String) source.get("topic");
//                if (mqttTopic != null) {
//                    streamDefinition += " --topics=" + mqttTopic;
//                }
//                String mqttUsername = (String) source.get("username");
//                if (mqttUsername != null) {
//                    streamDefinition += " --username=" + mqttUsername;
//                }
//                String mqttPassword = (String) source.get("password");
//                if (mqttPassword != null) {
//                    streamDefinition += " --password=" + mqttPassword;
//                }
//            } else if (streamName.equals("some other source")) {
//
//            } else {
//
//            }
//
//            String sinkLabel = (String) sink.get("label");
//            streamDefinition += " | " + processorName + " | " + sinkName;
//
//            String attempt = this.createStream(streamName, streamDefinition, false);
//            if (attempt.endsWith("success.")) {
//                return new ResponseEntity(attempt, HttpStatus.CREATED);
//            } else {
//                return new ResponseEntity(attempt, HttpStatus.EXPECTATION_FAILED);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

//    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_XML)
//    public ResponseEntity<String> createJXmlStream(
//            @RequestBody byte[] requestBody) {
//
//        try {
//            Path path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "tmp.xml");
//            String fileName = path.toAbsolutePath().toString();
//            Files.write(Paths.get(fileName), requestBody);
//
//            DecoderHelper helper = new DecoderHelper();
//            helper.setDecoderRepository(decoderRepository);
//            path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "tmp.xml");
//
//            Object decode = helper.decode(path);
//
//            if (decode instanceof AggregateProcess) {
//                ArrayList<SmlComponent> al = (ArrayList<SmlComponent>) ((AggregateProcess) decode).getComponents();
//                SmlComponent comp = al.get(0);
//                // TODO: instanceof Abfragen:
//                AbstractProcess asml = (AbstractProcess) comp.getProcess();
//                ArrayList<SmlIo> smlOutputs = (ArrayList<SmlIo>) asml.getOutputs();
//                SmlIo smlIo = smlOutputs.get(0);
//                SmlDataInterface smlDataInterface = (SmlDataInterface) smlIo.getIoValue();
//                SweDataRecord sdr = smlDataInterface.getInterfaceParameters();
//                LinkedList<SweField> sweFields = (LinkedList<SweField>) sdr.getFields();
//                String sourceName = "";
//                Source source = null;
//
//                for (SweField current : sweFields) {
//                    SweText sweText = (SweText) current.getElement();
//                    if (sweText.getDefinition().equals("source")) {
//                        sourceName = sweText.getValue();
//                        source = appController.getSourceByName(sourceName);
//                        if (source == null) {
//                            return new ResponseEntity("Source '" + sourceName + "' not found", HttpStatus.NOT_FOUND);
//                        }
//                    }
//                }
//                if (source == null) {
//                    return new ResponseEntity("Source '" + sourceName + "' not found", HttpStatus.NOT_FOUND);
//                }
//                sourceName = source.getName();
//
//                String streamSourceDefinition = "";
//                String streamDefinition = "";
//
//                for (SweField current : sweFields) {
//                    SweText sweText = (SweText) current.getElement();
//                    if (!sweText.getDefinition().equals("source")) {
//                        String appOptionName = sweText.getDefinition();
//                        AppOption ao = appController.getSourceOptionByName(source, appOptionName);
//                        if (ao == null) {
//                            return new ResponseEntity("Option '" + appOptionName + "' is not supported by source '" + sourceName + "'.", HttpStatus.EXPECTATION_FAILED);
//                        }
//                        streamSourceDefinition += " --" + ao.getName() + "=" + sweText.getValue();
//                    }
//                };
//                if (streamSourceDefinition.length() > 0) {
//                    streamDefinition = sourceName + " " + streamSourceDefinition + " ";
//                } else {
//                    streamDefinition = sourceName + " ";
//                }
//
//                // TODO: parse processor...
//                // TODO: parse sink...
//                streamDefinition += "| log-sink --semsormlurl=" + path;
//
//                Stream createdStream = service.createStream("aStreamName", streamDefinition, false);
//                if (createdStream != null) {
//                    return new ResponseEntity(createdStream, HttpStatus.CREATED);
//                } else {
//                    return new ResponseEntity(null, HttpStatus.CONFLICT);
//                }
//            } else {
//                return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
//        }
//    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public ResponseEntity<Streams> getStreams() {
        Streams result = service.getStreams();
        return new ResponseEntity(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{streamId}", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public ResponseEntity<Stream> getStream(
            @PathVariable String streamId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Stream result = service.getStream(streamId);
        if (result == null) {
            return new ResponseEntity("{ \"error\": \"stream with name '"+streamId+"' not found.\"}", headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
    
//    @RequestMapping(value = "/{streamId}", method = RequestMethod.PUT)
//    public ResponseEntity<Stream> putStream(
//            @PathVariable String streamId,
//            @RequestBody Map<String, Object> payload) {
//        Stream stream = service.getStream(streamId);
//        if (stream == null) {
//            return new ResponseEntity(null, HttpStatus.EXPECTATION_FAILED);
//        } else {
//            String status = stream.getStatus();
//            if (status.equals("deploying")) {
//                // what to do when it's currently deploying?
//                return new ResponseEntity(status, HttpStatus.CONFLICT);
//            } else if (status.equals("undeployed")) {
//                // deploy Stream
//                service.deployStream(streamId);
//                stream.setStatus("deploying");
//                return new ResponseEntity(stream, HttpStatus.OK);
//            } else if (status.equals("deployed")) {
//                // undeploy Stream
//                service.undeployStream(streamId);
//                stream.setStatus("undeployed");
//                return new ResponseEntity(stream, HttpStatus.OK);
//            } else {
//                // NoSuchStreamDefinitionException ==> Error
//                return new ResponseEntity(stream, HttpStatus.NOT_FOUND);
//            }
//        }
//    }
    
    /**
     * DELETE deletes a stream.
     *
     * @param streamId - name of the stream
     * @return succes or error message
     */
    @RequestMapping(value = "/{streamId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteStream(
            @PathVariable String streamId) {
        String result = service.deleteStream(streamId);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/createStream", method = RequestMethod.GET)
    public Stream createStream(
            @RequestParam("streamName") String streamName,
            @RequestParam("streamDefinition") String streamDefinition,
            @RequestParam("deploy") boolean deploy
    ) {
        Stream result = service.createStream(streamName, streamDefinition, deploy);
        return result;
    }
    
}