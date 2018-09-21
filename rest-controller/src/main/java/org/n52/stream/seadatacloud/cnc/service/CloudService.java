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
package org.n52.stream.seadatacloud.cnc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.n52.stream.seadatacloud.cnc.CnCServiceConfiguration;
import org.n52.stream.seadatacloud.cnc.model.Processors;
import org.n52.stream.seadatacloud.cnc.model.Sinks;
import org.n52.stream.seadatacloud.cnc.model.Sources;
import org.n52.stream.seadatacloud.cnc.model.Stream;
import org.n52.stream.seadatacloud.cnc.model.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@Component
@EnableConfigurationProperties(CnCServiceConfiguration.class)
public class CloudService {

    private static final Logger LOG = LoggerFactory.getLogger(CloudService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CnCServiceConfiguration properties;

    public Sources getSources() {
        Sources sources = null;
        try {
            sources = objectMapper.readValue(executeRequest(HttpMethod.GET, "/apps?type=source").toString(),
                    Sources.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        }
        return sources;
    }

    public Processors getProcessors() {
        Processors processors = null;
        try {
            processors = objectMapper.readValue(executeRequest(HttpMethod.GET, "/apps?type=processor").toString(),
                    Processors.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        }
        return processors;
    }

    public Sinks getSinks() {
        Sinks sinks = null;
        try {
            sinks = objectMapper.readValue(executeRequest(HttpMethod.GET, "/apps?type=sink").toString(),
                    Sinks.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        }
        return sinks;
    }

    public String registerApp(String appName, String appType, String appUri) {
        String response = "";
        try {

            URL url = new URL(properties.getDataflowhost() + "/apps/" + appType + "/" + appName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("uri=" + appUri).getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            response = res.toString() + "success.";

        } catch (Exception e) {
            response = e.getMessage();
            LOG.error(e.getMessage());
        }
        return response;
    }

    public Future<Stream> createStream(String streamName, String streamDefinition, boolean deploy) throws InterruptedException {
        CompletableFuture<Stream> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Stream stream = null;
            URL url = new URL(properties.getDataflowhost() + "/streams/definitions");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream inputStream;
            try {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.getOutputStream().write(("name=" + streamName + "&definition=" + streamDefinition + "&deploy=" + deploy).getBytes());
                int responseCode = conn.getResponseCode();
                if (responseCode >= 300) {
                    inputStream = conn.getErrorStream();
                    Scanner scanner = new Scanner(inputStream);
                    scanner.useDelimiter("\\Z");
                    String response = scanner.next();
                    scanner.close();
                    LOG.error(response);
                }
                inputStream = conn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader in = new BufferedReader(inputStreamReader);
                String line;
                StringBuffer res = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    res.append(line);
                    res.append("\n");
                }
                in.close();
                conn.disconnect();
                String response = res.toString();
                stream = objectMapper.readValue(response, Stream.class);
            } catch (IOException e) {
                LOG.error(e.getMessage());
                LOG.debug("Exception thrown: ", e);
            }
            completableFuture.complete(stream);
            return stream;
        });

        return completableFuture;
    }

    public Stream undeployStream(String streamName) {
        Stream stream = null;
        try {
            stream = objectMapper.readValue(
                    executeRequest(HttpMethod.DELETE, "/streams/deployments/" + streamName).toString(),
                    Stream.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        }
        return stream;
    }

    public Stream deployStream(String streamName) {
        Stream stream = null;
        StringBuilder res = new StringBuilder();
        try {
            URL url = new URL(properties.getDataflowhost() + "/streams/deployments/" + streamName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"deployer.*.local.javaOpts\"").append(":");
            sb.append("\"-DSTREAM_ID=").append(streamName).append("\"}");
            conn.getOutputStream().write(sb.toString().getBytes());

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    res.append(line);
                    res.append("\n");
                }
            }
            conn.disconnect();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown: ", e);
        }
        String response = res.toString();
        try {
            stream = objectMapper.readValue(response, Stream.class);
        } catch (IOException ex) {
            LOG.debug("Exception thrown: ", ex);
        }
        return stream;
    }

    public String deleteStream(String streamName) {
        return executeRequest(HttpMethod.DELETE, "/streams/definitions/" + streamName).toString();
    }

    public Streams getStreams() {
        Streams streams = new Streams();
        StringBuffer serviceResponse = executeRequest(HttpMethod.GET, "/streams/definitions?size=100");
        try {
            streams = objectMapper.readValue(serviceResponse.toString(), Streams.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.debug("Exception thrown:", e);
        }
        return streams;
    }

    public Stream getStream(String streamId) {
        Stream stream = null;
        String getRequest = "/streams/definitions/" + streamId;
        try {
            StringBuffer response = executeRequest(HttpMethod.GET, getRequest);
            stream = objectMapper.readValue(response.toString(), Stream.class);
        } catch (IOException e) {
            LOG.error("Error while retrieving stream with id '{}'.", streamId);
            LOG.debug("Exception thrown:", e);
        }
        return stream;
    }

    private StringBuffer executeRequest(HttpMethod method, String pathAndQueryString) {
        BufferedReader in = null;
        StringBuffer serviceResponse = new StringBuffer();
        try {
            URL url = new URL(properties.getDataflowhost() + pathAndQueryString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod(method.toString());
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                serviceResponse.append(line);
                serviceResponse.append("\n");
            }
            conn.disconnect();
        } catch (IOException e) {
            LOG.error("Exception thrown while connecting to dataflow server.");
            LOG.debug("Exception thrown: ", e);
            serviceResponse.append(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOG.error("Could not close stream: ", e);
            }
        }
        return serviceResponse;
    }

}
