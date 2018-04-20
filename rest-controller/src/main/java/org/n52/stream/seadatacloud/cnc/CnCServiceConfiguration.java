/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
