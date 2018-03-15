/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.core;

import java.util.Date;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
public class MarineWeatherData {

    private Date date;
    
    /**
     * Mandatory constructor
     */
    public MarineWeatherData() {
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
     
    public String toString() {
        return "MarineWeatherData[DateTime: "+this.date.toString()+" ]";
    }
    
}