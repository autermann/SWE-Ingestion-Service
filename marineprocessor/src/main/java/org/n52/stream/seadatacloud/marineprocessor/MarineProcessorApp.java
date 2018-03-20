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
package org.n52.stream.seadatacloud.marineprocessor;

import java.time.Instant;
import java.util.Date;
import org.n52.stream.core.MarineWeatherData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.cloud.stream.messaging.Processor;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@SpringBootApplication
@EnableBinding(Processor.class)
public class MarineProcessorApp {

    private MarineWeatherData parseWeather(String input) {
        MarineWeatherData parsed = new MarineWeatherData();
        if (input != null && input.length() > 0) {
            String parsedDate = input.substring(0,24);
            System.out.println(parsedDate);
            Date date = Date.from(Instant.parse(parsedDate));
            parsed.setDate(date);
        }
        return parsed;
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public MarineWeatherData process(String mqtt) {
        // TODO: processing
        MarineWeatherData mwd = parseWeather(mqtt);
        return mwd;
    }

    public static void main(String[] args) {
        SpringApplication.run(MarineProcessorApp.class, args);
    }

}
