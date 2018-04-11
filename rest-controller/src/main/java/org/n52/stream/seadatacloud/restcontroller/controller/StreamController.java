/*
 * Copyright (C) 2018-2018 52Â°North Initiative for Geospatial Open Source
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.controller;

import org.n52.stream.seadatacloud.restcontroller.service.CloudService;

import java.util.Map;
import org.n52.stream.seadatacloud.restcontroller.model.Stream;
import org.n52.stream.seadatacloud.restcontroller.model.Streams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
public class StreamController {

    public final String APPLICATION_JSON = "application/json";
    public final String APPLICATION_XML = "application/xml";

    @Autowired
    CloudService service;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_JSON)
    public ResponseEntity<String> createJsonStream(
            @RequestBody Map<String, Object> payload) {

        try {
            String streamDefinition = "";
            String streamName = (String) payload.get("name");
            Stream stream = service.getStream(streamName);
            if (stream != null) {
                return new ResponseEntity("{Error: Stream " + streamName + " already exists.}", HttpStatus.CONFLICT);
            }
            Map<String, Object> source = (Map<String, Object>) payload.get("source");
            String sourceName = (String) source.get("name");
            Map<String, Object> processor = (Map<String, Object>) payload.get("processor");
            String processorName = (String) processor.get("name");
            Map<String, Object> sink = (Map<String, Object>) payload.get("sink");
            String sinkName = (String) sink.get("name");
            if (sourceName.equals("mqttrabbitsource")) {
                streamDefinition = sourceName;
                String mqttUrl = (String) source.get("url");
                if (mqttUrl != null) {
                    streamDefinition += " --url=" + mqttUrl;
                    String mqttPort = (String) source.get("port");
                    if (mqttPort != null) {
                        streamDefinition += ":" + mqttPort;
                    }
                }
                String mqttTopic = (String) source.get("topic");
                if (mqttTopic != null) {
                    streamDefinition += " --topics=" + mqttTopic;
                }
                String mqttUsername = (String) source.get("username");
                if (mqttUsername != null) {
                    streamDefinition += " --username=" + mqttUsername;
                }
                String mqttPassword = (String) source.get("password");
                if (mqttPassword != null) {
                    streamDefinition += " --password=" + mqttPassword;
                }
            } else if (streamName.equals("some other source")) {

            } else {

            }

            String sinkLabel = (String) sink.get("label");
            streamDefinition += " | " + processorName + " | " + sinkName;

            String attempt = this.createStream(streamName, streamDefinition, false);
            if (attempt.endsWith("success.")) {
                return new ResponseEntity(attempt, HttpStatus.CREATED);
            } else {
                return new ResponseEntity(attempt, HttpStatus.EXPECTATION_FAILED);
            }
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_XML)
    public ResponseEntity<String> createJXmlStream(
            @RequestBody String xml) {
        String result = xml;
        return new ResponseEntity(result, HttpStatus.CREATED);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public ResponseEntity<Streams> getStreams() {
        Streams result = service.getStreams();
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{streamId}", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public ResponseEntity<Stream> getStream(
            @PathVariable String streamId) {
        Stream result = service.getStream(streamId);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/{streamId}", method = RequestMethod.PUT)
    public ResponseEntity<Stream> putStream(
            @PathVariable String streamId,
            @RequestBody Map<String, Object> payload) {
        Stream stream = service.getStream(streamId);
        if (stream == null) {
            return new ResponseEntity(null, HttpStatus.EXPECTATION_FAILED);
        } else {
            String status = stream.getStatus();
            if (status.equals("deploying")) {
                // what to do when it's currently deploying?
                return new ResponseEntity(status, HttpStatus.CONFLICT);
            } else if (status.equals("undeployed")) {
                // deploy Stream
                service.deployStream(streamId);
                stream.setStatus("deploying");
                return new ResponseEntity(stream, HttpStatus.OK);
            } else if (status.equals("deployed")) {
                // undeploy Stream
                service.undeployStream(streamId);
                stream.setStatus("undeployed");
                return new ResponseEntity(stream, HttpStatus.OK);
            } else {
                // NoSuchStreamDefinitionException ==> Error
                return new ResponseEntity(stream, HttpStatus.NOT_FOUND);
            }
        }
    }

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
    public String createStream(
            @RequestParam("streamName") String streamName,
            @RequestParam("streamDefinition") String streamDefinition,
            @RequestParam("deploy") boolean deploy
    ) {
        String result = "";
        result = service.createStream(streamName, streamDefinition, deploy);
        return result;
    }
}
