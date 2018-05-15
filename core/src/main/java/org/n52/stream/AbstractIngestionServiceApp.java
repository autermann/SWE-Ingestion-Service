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
package org.n52.stream;

import java.io.IOException;
import java.io.Serializable;

import org.n52.janmayen.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.annotations.VisibleForTesting;

/**
 * Abstract class that provides some methods
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public abstract class AbstractIngestionServiceApp {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIngestionServiceApp.class);

    /**
     * Log and throw {@link IllegalArgumentException} for messae
     * 
     * @param msg
     *            The message to log and throw
     * @throws IllegalArgumentException
     */;
    protected IllegalArgumentException logErrorAndCreateException(String msg)
            throws IllegalArgumentException {
        LOG.error(msg);
        throw new IllegalArgumentException(msg);
    }

    /**
     * Check the setting for null and emtpy
     * 
     * @param settingName
     *            the setting name
     * @param setting
     *            the setting value
     * @throws IllegalArgumentException
     *             If the setting is null or empty
     */
    protected void checkSetting(String settingName, String setting)
            throws IllegalArgumentException {
        if (setting == null || setting.isEmpty()) {
            logErrorAndCreateException(
                    String.format("setting '%s' not set correct. Received value: '%s'.", settingName, setting));
        }
        LOG.trace("'{}': '{}'", settingName, setting);
    }
    
    protected JsonNode toJson(String json) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readTree(json);
        } catch (IOException e) {
            LOG.error(String.format("Error while parsing JSON string: %s", json), e);
        }
        return null;
    }
    
    @VisibleForTesting
    protected JsonNode getJson(Serializable s) {
        return toJson(getJsonString(s));
    }
    
    @VisibleForTesting
    protected String getJsonString(Serializable s) {
        
        JacksonAnnotationIntrospector ignore = new JacksonAnnotationIntrospector() {
            @Override
            protected TypeResolverBuilder<?> _findTypeResolver(
                    MapperConfig<?> mc, Annotated a, JavaType jt) {
                if (!a.hasAnnotation(JsonTypeInfo.class)) {
                    return super._findTypeResolver(mc, a, jt);
                }
                return StdTypeResolverBuilder.noTypeInfoBuilder();
            }
        };
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.setAnnotationIntrospector(ignore);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
       
        try {
            return om.writeValueAsString(s);
        } catch (JsonProcessingException e) {
            LOG.error("Error while createing JSON string", e);
        }
        return "";
    }
    
    protected JsonNodeFactory nodeFactory() {
        return Json.nodeFactory();
    }
}
