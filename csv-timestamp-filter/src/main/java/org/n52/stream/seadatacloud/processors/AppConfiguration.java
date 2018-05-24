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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 *
 */
@Validated
@ConfigurationProperties("org.n52.stream")
public class AppConfiguration {

    /**
     * The last timestamp inserted into the database for the stream.
     */
    private String lastSeenTimestamp = "";
    /**
     * The column separator for the stream.
     */
    private String columnSeparator;
    /**
     * The index of the date column.
     * Same as {@link AppConfiguration#timeColumnIndex} if datetime column.
     */
    private Integer dateColumnIndex;
    /**
     * The index of the time column.
     * Same as {@link AppConfiguration#dateColumnIndex} if datetime column.
     */
    private Integer timeColumnIndex;
    /**
     * The format of the date column.
     * Same as {@link AppConfiguration#timeColumnFormat} if datetime column.
     * If full ISO 8601, not format definition is required.
     */
    private String dateColumnFormat;
    /**
     * The format of the time column.
     * Same as {@link AppConfiguration#dateColumnFormat} if datetime column.
     * If full ISO 8601, not format definition is required.
     */
    private String timeColumnFormat;

    public String getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(String lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }
    
    public boolean isSetLastSeenTimestamp() {
        return getLastSeenTimestamp() != null && !getLastSeenTimestamp().isEmpty();
    }

    public String getColumnSeparator() {
        return columnSeparator;
    }

    public void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public Integer getDateColumnIndex() {
        return dateColumnIndex;
    }

    public void setDateColumnIndex(Integer dateColumnIndex) {
        this.dateColumnIndex = dateColumnIndex;
    }
    
    public Integer getTimeColumnIndex() {
        return timeColumnIndex;
    }

    public void setTimeColumnIndex(Integer timeColumnIndex) {
        this.timeColumnIndex = timeColumnIndex;
    }
    
    public boolean isDateTimeColumnIndex() {
        return getDateColumnIndex() == getTimeColumnIndex();
    }

    public String getDateColumnFormat() {
        return dateColumnFormat;
    }

    public void setDateColumnFormat(String dateColumnFormat) {
        this.dateColumnFormat = dateColumnFormat;
    }

    public boolean isDateColumnFormat() {
        return getDateColumnFormat() != null && !getDateColumnFormat().isEmpty();
    }
    
    public String getTimeColumnFormat() {
        return timeColumnFormat;
    }

    public void setTimeColumnFormat(String timeColumnFormat) {
        this.timeColumnFormat = timeColumnFormat;
    }

    public boolean isTimeColumnFormat() {
        return getTimeColumnFormat() != null && !getTimeColumnFormat().isEmpty();
    }
    
}
