/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.logsink;

import org.n52.stream.core.MarineWeatherData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@SpringBootApplication
@EnableBinding(Sink.class)
public class LogSinkApplication {
    
    @StreamListener(Sink.INPUT)
    public void input(MarineWeatherData mqtt){
        System.out.println(mqtt.toString());
    }
    
    public static void main(String[] args){
        SpringApplication.run(LogSinkApplication.class, args);
    }
    
}