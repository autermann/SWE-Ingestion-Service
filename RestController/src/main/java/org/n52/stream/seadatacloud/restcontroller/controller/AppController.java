/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.controller;

import java.util.Optional;
import okhttp3.Call;
import org.n52.stream.seadatacloud.restcontroller.model.Processors;
import org.n52.stream.seadatacloud.restcontroller.model.Sinks;
import org.n52.stream.seadatacloud.restcontroller.model.Sources;
import org.n52.stream.seadatacloud.restcontroller.service.CloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RestController
@Component
@RequestMapping("/api")
public class AppController {
    
    public final String APPLICATION_JSON = "application/json";
    public final String APPLICATION_XML = "application/xml";

    @Autowired
    CloudService service;

    @RequestMapping(value = "/sources", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public Sources getSources() {
        Sources result = service.getSources();
        return result;
    }
    
    @RequestMapping(value = "/processors", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public Processors getProcessors() {
        Processors result = service.getProcessors();
        return result;
    }
    
    @RequestMapping(value = "/sinks", method = RequestMethod.GET, produces = APPLICATION_JSON)
    public Sinks getSinks() {
        Sinks result = service.getSinks();
        return result;
    }

    @RequestMapping(value = "/registerApp", method = RequestMethod.GET)
    public ResponseEntity<String> registerApp(
            @RequestParam("name") String appName,
            @RequestParam("type") String appType,
            @RequestParam("uri") String appUri
    ) {
        String result = service.registerApp(appName, appType, appUri);
        return new ResponseEntity(result, HttpStatus.OK);
    }
    
}