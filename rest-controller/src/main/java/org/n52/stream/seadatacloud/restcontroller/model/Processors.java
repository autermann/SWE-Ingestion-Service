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
public class Processors {
    
    public List<Processor> processors;

    public Processors() {
    }

    public List<Processor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<Processor> processors) {
        this.processors = processors;
    }
    
}
