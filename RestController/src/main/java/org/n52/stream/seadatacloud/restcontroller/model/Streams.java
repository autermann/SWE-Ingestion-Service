/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.model;

import java.util.List;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
public class Streams {
    
    public List<Stream> streams;

    public Streams() {
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }

    
}
