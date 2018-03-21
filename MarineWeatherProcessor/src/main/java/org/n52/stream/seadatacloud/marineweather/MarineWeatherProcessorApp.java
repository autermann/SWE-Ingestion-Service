/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.marineweather;

import java.time.OffsetDateTime;
import org.n52.stream.core.Dataset;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.cloud.stream.messaging.Processor;

/**
 * @author Maurin Radtke <m.radtke@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
@SpringBootApplication
@EnableBinding(Processor.class)
public class MarineWeatherProcessorApp {

    private Dataset parseWeather(String input) {
        Dataset dataset = new Dataset();
        if (input != null && input.length() > 0) {
            String timestampString = input.substring(0,24);
            System.out.println(timestampString);
            OffsetDateTime timestamp = OffsetDateTime.parse(timestampString);
            Timeseries timeseries = new Timeseries();
            Measurement<Object> measurement = new Measurement<>();
            measurement.setTimestamp(timestamp);
            timeseries.addMeasurementsItem(measurement );
            dataset.addTimeseriesItem(timeseries);
        }
        return dataset;
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public Dataset process(String mqtt) {
        // TODO: processing
        Dataset mwd = parseWeather(mqtt);
        return mwd;
    }

    public static void main(String[] args) {
        SpringApplication.run(MarineWeatherProcessorApp.class, args);
    }

}
