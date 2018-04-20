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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@ConfigurationProperties("org.n52.stream")
public class CnCServiceConfiguration {

    private String sosendpoint = "http://sos:8080/52n-sos-webapp/service";
    private String dataflowhost = "http://dataflow:9393";
    private String baseurl = "http://cnc:8082/cnc";
    private Datasource datasource = new Datasource();
    
    public String getSosendpoint() {
        return sosendpoint;
    }

    public void setSosendpoint(String sosendpoint) {
        this.sosendpoint = sosendpoint;
    }

    public String getDataflowhost() {
        return dataflowhost;
    }

    public void setDataflowhost(String dataflowhost) {
        this.dataflowhost = dataflowhost;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }
    
    public class Datasource {
        
        private String url = "jdbc:postgresql://database:5432/sos";
        private String username = "postgres";
        private String password = "postgres";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
        
    }
    
}
