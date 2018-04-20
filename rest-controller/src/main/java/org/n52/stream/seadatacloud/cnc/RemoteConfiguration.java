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
package org.n52.stream.seadatacloud.cnc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.n52.stream.seadatacloud.cnc.decoder.AppOptionsDecoder;
import org.n52.stream.seadatacloud.cnc.decoder.ProcessorsDecoder;
import org.n52.stream.seadatacloud.cnc.decoder.SinksDecoder;
import org.n52.stream.seadatacloud.cnc.decoder.SourcesDecoder;
import org.n52.stream.seadatacloud.cnc.decoder.StreamDecoder;
import org.n52.stream.seadatacloud.cnc.decoder.StreamsDecoder;
import org.n52.stream.seadatacloud.cnc.model.AppOptions;
import org.n52.stream.seadatacloud.cnc.model.Processors;
import org.n52.stream.seadatacloud.cnc.model.Sinks;
import org.n52.stream.seadatacloud.cnc.model.Sources;
import org.n52.stream.seadatacloud.cnc.model.Stream;
import org.n52.stream.seadatacloud.cnc.model.Streams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@Configuration
public class RemoteConfiguration {
    
    @Autowired
    private SourcesDecoder sourcesDecoder;
    @Autowired
    private ProcessorsDecoder processorsDecoder;
    @Autowired
    private SinksDecoder sinksDecoder;
    
    @Bean
    public ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(Streams.class, new StreamsDecoder());
        sm.addDeserializer(Stream.class, new StreamDecoder());
        sm.addDeserializer(Processors.class, processorsDecoder);
        sm.addDeserializer(Sinks.class, sinksDecoder);
        sm.addDeserializer(Sources.class, sourcesDecoder);
        sm.addDeserializer(AppOptions.class, new AppOptionsDecoder());
        mapper.registerModule(sm);
        return mapper;
    }

}