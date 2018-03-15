/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.springframework.stereotype.Component;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@Component
public class RestService {

    public static final String BASE_URL = "http://localhost:9393";

    public String getApps(String type) {
        String response = "";
        try {
            URL url;
            if (type!=null) {
                url = new URL(BASE_URL + "/apps?type="+type);
            } else {
                url = new URL(BASE_URL + "/apps");
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            response = res.toString();

        } catch (Exception e) {
            System.out.println(e);
        }
        return response;
    }
    
    public String registerApp(String appName, String appType, String appUri) {
        String response = "";
        try {
            
            URL url = new URL(BASE_URL + "/apps/"+appType+"/"+appName);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("uri="+appUri).getBytes());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            response = res.toString() + "success.";

        } catch (IOException e) {
            return e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return response;
    }
    
    public String createStream(String streamName, String streamDefinition, boolean deploy) {
        String response = "";
        try {
            URL url = new URL(BASE_URL + "/streams/definitions?deploy="+deploy);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("name="+streamName+"&definition="+streamDefinition).getBytes());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            response = res.toString() + "success.";
        } catch (Exception e) {
            response = e.getMessage();
        }
        return response;
    }

}
