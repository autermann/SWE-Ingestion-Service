/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller;

import org.n52.stream.seadatacloud.restcontroller.controller.AppController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@SpringBootApplication
@ComponentScan("org.n52.stream.seadatacloud.restcontroller")
public class RestApplication {
    
    @Autowired
    AppController appController;

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

}