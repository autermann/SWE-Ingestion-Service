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
    public void registerSourcesTest() {
        CloudService service = new CloudService();
        String logSink = service.registerApp("LogSink", "sink", "file://C:/Develop/2018/SWE-Ingestion-Service/LogSink/target/LogSink-0.0.1-SNAPSHOT.jar");
        Assert.isTrue(logSink.equals("success.") || logSink.contains("409"), "could not register Spring DataFlow Server application: LogSink");
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
    public void registerSinksTest() {
        CloudService service = new CloudService();
        String rabbitmqttsource = service.registerApp("mqttrabbitsource", "source", "file://C:/Develop/mqt/mqtt/mqtt/apps/mqtt-source-rabbit/target/mqtt-source-rabbit-2.0.0.BUILD-SNAPSHOT.jar");
        Assert.isTrue(rabbitmqttsource.equals("success.") || rabbitmqttsource.contains("409"), "could not register Spring DataFlow Server application: mqttrabbitsource");
    }

}
