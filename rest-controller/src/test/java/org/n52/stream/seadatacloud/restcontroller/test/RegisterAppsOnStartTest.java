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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.stream.seadatacloud.restcontroller.service.CloudService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RegisterAppsOnStartTest.class})
public class RegisterAppsOnStartTest {

    @Test
    public void registerSinksTest() {
        CloudService service = new CloudService();
        String logSink = service.registerApp("LogSink", "sink", "file://C:/Develop/2018/SWE-Ingestion-Service/LogSink/target/LogSink-0.0.1-SNAPSHOT.jar");
        String dbSink = service.registerApp("DbSink", "sink", "file://C:/Develop/2018/SWE-Ingestion-Service/db-sink/target/db-sink-0.0.1-SNAPSHOT.jar");
        Assert.isTrue(logSink.equals("success.") || logSink.contains("409"), "could not register Spring DataFlow Server application: LogSink");
        Assert.isTrue(dbSink.equals("success.") || dbSink.contains("409"), "could not register Spring DataFlow Server application: DbSink");
    }

    @Test
    public void registerProcessorsTest() {
        CloudService service = new CloudService();
        String marineCTDprocessor = service.registerApp("MarineCTDProcessor", "processor", "file://C:/Develop/2018/SWE-Ingestion-Service/MarineCTDProcessor/target/MarineCTDProcessor-0.0.1-SNAPSHOT.jar");
        Assert.isTrue(marineCTDprocessor.equals("success.") || marineCTDprocessor.contains("409"), "could not register Spring DataFlow Server application: MarineCTDProcessor");
        String marineWeatherprocessor = service.registerApp("MarineWeatherProcessor", "processor", "file://C:/Develop/2018/SWE-Ingestion-Service/MarineWeatherProcessor/target/MarineWeatherProcessor-0.0.1-SNAPSHOT.jar");
        Assert.isTrue(marineWeatherprocessor.equals("success.") || marineWeatherprocessor.contains("409"), "could not register Spring DataFlow Server application: MarineWeatherProcessor");
    }

    @Test
    public void registerSourcesTest() {
        CloudService service = new CloudService();
        String rabbitmqttsource = service.registerApp("mqttrabbitsource", "source", "file://C:/Develop/mqt/mqtt/mqtt/apps/mqtt-source-rabbit/target/mqtt-source-rabbit-2.0.0.BUILD-SNAPSHOT.jar");
        Assert.isTrue(rabbitmqttsource.equals("success.") || rabbitmqttsource.contains("409"), "could not register Spring DataFlow Server application: mqttrabbitsource");
    }

}
