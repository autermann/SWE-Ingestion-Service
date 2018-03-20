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
public class Processor {
    
    public String name;
    public List<AppOption> options;

    public Processor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AppOption> getOptions() {
        return options;
    }

    public void setOptions(List<AppOption> options) {
        this.options = options;
    }
}
