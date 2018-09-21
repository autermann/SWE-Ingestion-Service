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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.Message;
import org.n52.stream.AbstractIngestionServiceApp;
import org.springframework.integration.annotation.Splitter;
import org.springframework.messaging.support.MessageBuilder;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@SpringBootApplication
//@ComponentScan("org.n52.stream")
//@ComponentScan("org.n52.stream.util")
@EnableBinding(Processor.class)
@EnableConfigurationProperties(AppConfiguration.class)
public class CsvFileSplitterProcessor extends AbstractIngestionServiceApp {

    @Autowired
    private AppConfiguration properties;

    private String propertiesDelimiter;
    private String propertiesUrl;
    private int propertiesMaxMessages;

    private static final String FILE_NAME = "file_name";

    private static final Logger LOG = LoggerFactory.
            getLogger(CsvFileSplitterProcessor.class);

    private int lastPolledLine;

    public static void main(String[] args) {
        SpringApplication.run(CsvFileSplitterProcessor.class, args);
    }

    @PostConstruct
    public void init() {
        LOG.info("Init CsvFileSplitter processor..");
        checkSetting("delimiter",
                properties.getDelimiter() + "");
        propertiesDelimiter = properties.getDelimiter();
        propertiesUrl = properties.getUrl();
        propertiesMaxMessages = properties.getMaxmessages();
        LOG.info("CsvFileSplitter initialized");
        lastPolledLine = -1;
    }

    @Splitter(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public Collection<Message<String>> splitItem(Message<String> csvFileMessage) {
        List<String> list = new LinkedList<String>(Arrays.asList(
                csvFileMessage.getPayload().toString()
                        .split(propertiesDelimiter)));
        LOG.debug("list size before removing items: " + list.size());
        // add START & END marker:
        int endRow = Math.min(lastPolledLine + propertiesMaxMessages, list.size() - 1);
        Message<String> endMsg = MessageBuilder.withPayload(list.get(endRow - 1))
                .setHeader("file_marker", "END")
                .setHeader(FILE_NAME, propertiesUrl)
                .copyHeadersIfAbsent(csvFileMessage.getHeaders())
                .build();

        int listSize = list.size();
        for (int i = endRow + 1; i < listSize; i++) {
            if (list.size() > endRow + 1) {
                list.remove(endRow + 1);
            }
        }
        Message<String> startMsg = MessageBuilder.withPayload(list.get(lastPolledLine + 1))
                .setHeader("file_marker", "START")
                .setHeader(FILE_NAME, propertiesUrl)
                .copyHeadersIfAbsent(csvFileMessage.getHeaders())
                .build();
        for (int i = 0; i < lastPolledLine + 1; i++) {
            list.remove(0);
        }
        LOG.debug("list size after removing items: " + list.size());
        List<Message<String>> msgList = list.stream()
                .map(s -> {
                    Message<String> gm = MessageBuilder.withPayload(s.toString())
                            .setHeader(FILE_NAME, propertiesUrl)
                            .copyHeadersIfAbsent(csvFileMessage.getHeaders())
                            .build();
                    return gm;
                }).collect(Collectors.toList());
        msgList.add(0, startMsg);
        msgList.add(msgList.size(), endMsg);
        LOG.debug("splitted '" + (msgList.size() - 2) + "' messages for file '" + propertiesUrl + "' for rows '" + (lastPolledLine + 1) + "' to '" + (lastPolledLine + propertiesMaxMessages) + "'.");
        lastPolledLine = endRow;
        return msgList;
    }

}