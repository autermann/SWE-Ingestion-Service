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
package org.n52.stream.seadatacloud.cnc.util;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.n52.stream.seadatacloud.cnc.model.Stream;
import org.n52.stream.seadatacloud.cnc.service.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class RestartStreamThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(RestartStreamThread.class);

    private CloudService service;
    private String streamName;
    private String streamDefinition;

    public RestartStreamThread(String streamName, String streamDefinition, CloudService service) {
        this.streamName = streamName;
        this.streamDefinition = streamDefinition;
        this.service = service;
    }

    @Override
    public void run() {
        try {
            Future<Stream> futureStream = service.createStream(streamName, streamDefinition, false);
            Stream createdStream = futureStream.get(120, TimeUnit.SECONDS);
            if (createdStream == null) {
                LOG.error("Restarting stream '{}' failed. Could not CREATE stream.",
                        streamName);
                return;
            }
            if (service.deployStream(streamName) == null) {
                LOG.error("Restarting stream '{}' failed. Could not DEPLOY stream.",
                        streamName);
            } else {
                LOG.info("Restarted stream '{}'.", streamName);
            }
        } catch (Exception e) {
            LOG.error("Restarting stream '{}' failed. Reason: {} : {}.",
                    streamName, e, e.getMessage());
        }
    }
}
