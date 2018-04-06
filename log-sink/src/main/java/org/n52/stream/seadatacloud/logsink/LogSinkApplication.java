/*
 * Copyright 2018-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.stream.seadatacloud.logsink;

import org.n52.stream.core.DataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@SpringBootApplication
@EnableBinding(Sink.class)
public class LogSinkApplication {

    private static final Logger LOG = LoggerFactory.getLogger(LogSinkApplication.class);

    @StreamListener(Sink.INPUT)
    public void input(DataMessage dataMessage){
        LOG.info("Received processor output:\n{}", dataMessage);
    }

    public static void main(String[] args){
        SpringApplication.run(LogSinkApplication.class, args);
    }

}