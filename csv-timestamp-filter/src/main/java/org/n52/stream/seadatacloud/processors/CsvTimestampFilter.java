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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.n52.stream.AbstractIngestionServiceApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * START and END would not be forwarded.
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since
 *
 */
@SpringBootApplication
@EnableBinding(Processor.class)
@EnableConfigurationProperties(AppConfiguration.class)
public class CsvTimestampFilter extends AbstractIngestionServiceApp {
    
    private static final Logger LOG = LoggerFactory.getLogger(CsvTimestampFilter.class);
    private static final String FILE_NAME = "file_name";
    private static final String START = "START";
    private static final String END = "END";
    
    private final Lock lock = new ReentrantLock();
    private OffsetDateTime globalLastSeenTimestamp;
    private Map<String, OffsetDateTime> fileLastSeenTimestamps = Collections.synchronizedMap(new HashMap<String, OffsetDateTime>());
    private Map<String, OffsetDateTime> fileStartLastSeenTimestamp = Collections.synchronizedMap(new HashMap<String, OffsetDateTime>());
    private DateTimeFormatter formatter;
    
    @Autowired
    private AppConfiguration properties;

    public static void main(String[] args) {
        SpringApplication.run(CsvTimestampFilter.class, args);
    }

    /**
     * Init the processor by checking the properties and finalize the custom configuration
     */
    @PostConstruct
    public void init() {
        LOG.info("Init CsvFileFilter processor...");
        globalLastSeenTimestamp =
                properties.isSetLastSeenTimestamp() ? OffsetDateTime.parse(properties.getLastSeenTimestamp())
                        : OffsetDateTime.MIN;
        checkSetting("column-separator", properties.getColumnSeparator());
        checkSetting("date-column-index", properties.getDateColumnIndex() + "");
        checkSetting("time-column-index", properties.getTimeColumnIndex() + "");
        if (!properties.isDateTimeColumnIndex()) {
            checkSetting("date-column-format", properties.getDateColumnFormat());
            checkSetting("time-column-format", properties.getTimeColumnFormat());
        }
        formatter = initDateTimeFormatter();
        LOG.info("CsvFileFilter initialized");
    }
    
    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public Message<String> process(Message<String> csvLineMessage) {
        if (csvLineMessage == null) {
            throw logErrorAndCreateException("NO CSV line message received! Input is 'null'.");
        }
        if (csvLineMessage.getPayload() == null || csvLineMessage.getPayload().isEmpty()) {
            throw logErrorAndCreateException("Empty CSV line payload received.");
        }
        if (csvLineMessage.getHeaders() == null || !csvLineMessage.getHeaders().containsKey(FILE_NAME)) {
            throw logErrorAndCreateException("Missing CSV file name header.");
        }
        return checkTimestamp(csvLineMessage) ? csvLineMessage : logAndReturnNull(csvLineMessage);
    }

    private boolean checkTimestamp(Message<String> csvLineMessage) {
        String fileName = getFileName(csvLineMessage);
        if (csvLineMessage.getPayload().equalsIgnoreCase(START)) {
            initFile(fileName);
            return false;
        } else if (csvLineMessage.getPayload().equalsIgnoreCase(END)) {
            removeFile(fileName);
            return false;
        } else {
            OffsetDateTime csvTimestamp = getCsvTimestamp(csvLineMessage.getPayload());
            OffsetDateTime refactorLastSeenTimestamp = fileStartLastSeenTimestamp.get(fileName);
            if (refactorLastSeenTimestamp == null || refactorLastSeenTimestamp.equals(OffsetDateTime.MIN)
                    || csvTimestamp.isAfter(refactorLastSeenTimestamp)) {
                updateLastSeenTimestamp(csvTimestamp, fileName);
                return true;
            }
            return false;
        }
    }
    
    private void initFile(String fileName) {
        fileLastSeenTimestamps.put(fileName, getGlobalLastSeenTimestamp().withNano(0));
        fileStartLastSeenTimestamp.put(fileName, getGlobalLastSeenTimestamp().withNano(0));
    }

    private void removeFile(String fileName) {
       if (fileLastSeenTimestamps.containsKey(fileName)) {
           updateGlobalLastSeenTimestamp(fileLastSeenTimestamps.get(fileName));
           fileLastSeenTimestamps.remove(fileName);
           fileStartLastSeenTimestamp.remove(fileName);
       }
    }

    private OffsetDateTime getCsvTimestamp(String csvLine) {
        String datetime = null;
        String[] split = csvLine.split(properties.getColumnSeparator());
        if (properties.isDateTimeColumnIndex()) {
            datetime = split[properties.getDateColumnIndex()];
        } else {
            StringBuilder time = new StringBuilder();
            time.append(split[properties.getDateColumnIndex()]);
            time.append(" ");
            time.append(split[properties.getTimeColumnIndex()]);
            datetime = time.toString();
        }
        return LocalDateTime.parse(datetime, formatter).atOffset(ZoneOffset.UTC);
    }

    private void updateLastSeenTimestamp(OffsetDateTime csvTimestamp, String fileName) {
        if (fileLastSeenTimestamps.get(fileName) == null || csvTimestamp.isAfter(fileLastSeenTimestamps.get(fileName))) {
            fileLastSeenTimestamps.put(fileName, csvTimestamp);
        }
    }

    private String getFileName(Message<String> csvLineMessage) {
        return csvLineMessage.getHeaders().get(FILE_NAME, String.class);
    }
    
    private void updateGlobalLastSeenTimestamp(OffsetDateTime lastSeenTimestamp) {
        try {
            lock.lock();
            this.globalLastSeenTimestamp =
                    lastSeenTimestamp.isAfter(globalLastSeenTimestamp) 
                            ? lastSeenTimestamp
                            : globalLastSeenTimestamp;
        } finally {
            lock.unlock();
        }
       
    }
    
    private OffsetDateTime getGlobalLastSeenTimestamp() {
        try {
            lock.lock();
            return globalLastSeenTimestamp;
        } finally {
            lock.unlock();
        }
    }

    private Message<String> logAndReturnNull(Message<String> csvLineMessage) {
        LOG.trace("Message in file '{}' ignored: {}", getFileName(csvLineMessage) ,csvLineMessage.getPayload());
        return null;
    }

    private DateTimeFormatter initDateTimeFormatter() {
        if (!properties.isDateTimeColumnIndex()) {
            StringBuilder pattern = new StringBuilder();
            pattern.append(properties.getDateColumnFormat());
            pattern.append(" ");
            pattern.append(properties.getTimeColumnFormat());
            return DateTimeFormatter.ofPattern(pattern.toString());
        } else if (properties.isDateColumnFormat()) {
            return DateTimeFormatter.ofPattern(properties.getDateColumnFormat());
        }
        return DateTimeFormatter.ISO_DATE_TIME;
    }
}
