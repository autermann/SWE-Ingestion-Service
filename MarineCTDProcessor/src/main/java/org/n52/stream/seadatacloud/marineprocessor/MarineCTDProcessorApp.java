/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class MarineCTDProcessorApp {

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
        SpringApplication.run(MarineCTDProcessorApp.class, args);
    }

}
