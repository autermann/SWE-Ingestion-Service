/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.controller;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.n52.stream.seadatacloud.restcontroller.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RestController
@RequestMapping("/api")
public class SpringRestController {

    @Autowired
    RestService service;

    @RequestMapping(value = "/apps", method = RequestMethod.GET)
    public ResponseEntity<String> apps(
            @RequestParam(value = "type") Optional<String> type) {
        String result = "";
        if (type.isPresent()) {
            result = service.getApps(type.get());
        } else {
            result = service.getApps(null);
        }
        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/registerApp", method = RequestMethod.GET)
    public ResponseEntity<String> apps(
            @RequestParam("name") String appName,
            @RequestParam("type") String appType,
            @RequestParam("uri") String appUri
    ) {
        String result = service.registerApp(appName, appType, appUri);
        return new ResponseEntity<String>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/createStream", method = RequestMethod.GET)
    public ResponseEntity<String> createStream(
            @RequestParam("streamName") String streamName,
            @RequestParam("streamDefinition") String streamDefinition,
            @RequestParam("deploy") boolean deploy
    ) {
        String result = "";
        result = service.createStream(streamName, streamDefinition, deploy);
        return new ResponseEntity<String>(result ,HttpStatus.OK);
    }
}