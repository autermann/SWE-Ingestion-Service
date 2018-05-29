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
package org.n52.stream.seadatacloud.processors;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.n52.stream.AbstractIngestionServiceApp;
import org.n52.stream.core.DataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;

/**
 * CsvFileFilter<br>
 *
 * <ul><li><b>Input</b>: Expects single line as String.</li>
 * <li><b>Parameter</b>:<ul>
 *  <li>number of header lines</li>
 *  <li>number of footer lines</li>
 *  <li>number of Comment-Char</li></ul>
 * </li>
*  <li><b>Output</b>: <b>one</b> CSV lines with data including file START and END message. Each message
*  contains the <code>correlation-id</code> header, which is the same for each message of ONE file.</li>
 * </ul>
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
@SpringBootApplication
//@ComponentScan("org.n52.stream")
@ComponentScan("org.n52.stream.util")
@EnableBinding(Processor.class)
@EnableConfigurationProperties(AppConfiguration.class)
public class CsvFileFilter extends AbstractIngestionServiceApp {

    private int msgCount = 0;
    private int processedMsgCount = 0;
    private int propertiesHeaderLines;

    @Autowired
    private AppConfiguration properties;
    private static Map<String, Integer> counterMap;

    private static final Logger LOG = LoggerFactory.
            getLogger(CsvFileFilter.class);
    private static final Object FILE_NAME = "file_name";

    public static void main(String[] args) {
        SpringApplication.run(CsvFileFilter.class, args);
    }

    /**
     * Init the processor by checking the properties and finalize the
     * custom configuration
     */
    @PostConstruct
    public void init() {
        LOG.info("Init CsvFileFilter processor...");
        checkSetting("number-of-header-lines",
                properties.getNumberOfHeaderLines()+"");
        propertiesHeaderLines = properties.getNumberOfHeaderLines();
        counterMap = Collections.synchronizedMap(new HashMap<>());
        LOG.info("CsvFileFilter initialized");
    }

    /*
     * TODO change to single file processing and forward the START and END message
     * TODO forward the correlation-id header
     */
    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public Message<String> process(Message<?> csvFileLineMessage) {
        msgCount++;
        if (csvFileLineMessage == null) {
            throw logErrorAndCreateException(
                    "NO CSV file line message received! Input is 'null'.");
        }
        if (csvFileLineMessage.getHeaders() == null || !csvFileLineMessage.getHeaders().containsKey(FILE_NAME)) {
            throw logErrorAndCreateException("Missing CSV file name header.");
        }
        String fileName = csvFileLineMessage.getHeaders().get(FILE_NAME, String.class);
        String plainLine = csvFileLineMessage.getPayload().toString();
        LOG.trace("Payload received: '{}'", plainLine);
        if (csvFileLineMessage.getHeaders().containsKey("file_marker")) {
            String markerValue = csvFileLineMessage.getHeaders().get("file_marker", String.class);
            switch(markerValue) {
                case "START":
                    // if start => init counter for skipping header lines
                    counterMap.put(fileName,0);
                    break;
                case "END":
                    // end end => remove file from map
                    counterMap.remove(fileName);
                    break;
            }
            return new GenericMessage<>(markerValue,
                    csvFileLineMessage.getHeaders());
        }
        int sequenceNumber = counterMap.get(fileName);
        counterMap.put(fileName, ++sequenceNumber);
        // ignore header lines:
        if (sequenceNumber <= propertiesHeaderLines) {
            return null;
        }
        processedMsgCount++;
        Message<String> filtered = new GenericMessage<>(
                plainLine,
                csvFileLineMessage.getHeaders());
        return filtered;
    }

    @VisibleForTesting
    public String getDataMessageLog(DataMessage processedDataset) {
        ObjectNode n = nodeFactory().objectNode();
        // TODO what should be enter here? The whole file is to much!
        n.set("CsvFile", getJson(processedDataset));
        n.put("number", processedMsgCount);
        n.put("of", msgCount);
        return n.toString();
    }
}
