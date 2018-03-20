/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.n52.stream.seadatacloud.restcontroller.model.Processor;
import org.n52.stream.seadatacloud.restcontroller.model.Processors;
import org.n52.stream.seadatacloud.restcontroller.model.Sink;
import org.n52.stream.seadatacloud.restcontroller.model.Sinks;
import org.n52.stream.seadatacloud.restcontroller.model.Source;
import org.n52.stream.seadatacloud.restcontroller.model.AppOption;
import org.n52.stream.seadatacloud.restcontroller.model.Sinks;
import org.n52.stream.seadatacloud.restcontroller.model.Sources;
import org.n52.stream.seadatacloud.restcontroller.model.Stream;
import org.n52.stream.seadatacloud.restcontroller.model.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@Component
public class CloudService {

    public static final String BASE_URL = "http://localhost:9393";

    private List<AppOption> getAppOptions(String appType, String appName) {
        List<AppOption> options = new ArrayList();
        try {
            URL url = new URL(BASE_URL + "/apps/" + appType + "/" + appName);
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
            String response = res.toString();
            JSONObject json = new JSONObject(response);
            JSONArray jsoptions = json.getJSONArray("options");
            for (int i = 0; i < jsoptions.length(); i++) {
                JSONObject jscurrent = jsoptions.getJSONObject(i);
                AppOption current = new AppOption();
                current.setName(jscurrent.getString("name"));
                current.setDescription(jscurrent.getString("description"));
                current.setType(jscurrent.getString("type"));
                current.setDefaultValue(jscurrent.get("defaultValue").toString());
                options.add(current);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return options;
    }

    public Sources getSources() {
        Sources sources = new Sources();
        try {
            URL url = new URL(BASE_URL + "/apps?type=source");
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
            String response = res.toString();

            JSONObject json = new JSONObject(response);
            JSONArray jssources = json.getJSONObject("_embedded").getJSONArray("appRegistrationResourceList");
            List<Source> sourcesList = new ArrayList();
            for (int i = 0; i < jssources.length(); i++) {
                JSONObject jscurrent = jssources.getJSONObject(i);
                Source current = new Source();
                current.setName(jscurrent.getString("name"));
                current.setOptions(getAppOptions("source", current.getName()));
                sourcesList.add(current);
            }
            sources.setSources(sourcesList);

        } catch (Exception e) {
            System.out.println(e);
        }
        return sources;
    }

    public Processors getProcessors() {
        Processors processors = new Processors();
        try {
            URL url = new URL(BASE_URL + "/apps?type=processor");
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
            String response = res.toString();

            JSONObject json = new JSONObject(response);
            JSONArray jsprocessors = json.getJSONObject("_embedded").getJSONArray("appRegistrationResourceList");
            List<Processor> processorList = new ArrayList();
            for (int i = 0; i < jsprocessors.length(); i++) {
                JSONObject jscurrent = jsprocessors.getJSONObject(i);
                Processor current = new Processor();
                current.setName(jscurrent.getString("name"));
                current.setOptions(getAppOptions("processor", current.getName()));
                processorList.add(current);
            }
            processors.setProcessors(processorList);

        } catch (Exception e) {
            System.out.println(e);
        }
        return processors;
    }

    public Sinks getSinks() {
        Sinks sinks = new Sinks();
        try {
            URL url = new URL(BASE_URL + "/apps?type=sink");
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
            String response = res.toString();

            JSONObject json = new JSONObject(response);
            JSONArray jssinks = json.getJSONObject("_embedded").getJSONArray("appRegistrationResourceList");
            List<Sink> sinkList = new ArrayList();
            for (int i = 0; i < jssinks.length(); i++) {
                JSONObject jscurrent = jssinks.getJSONObject(i);
                Sink current = new Sink();
                current.setName(jscurrent.getString("name"));
                current.setOptions(getAppOptions("sink", current.getName()));
                sinkList.add(current);
            }
            sinks.setSinks(sinkList);

        } catch (Exception e) {
            System.out.println(e);
        }
        return sinks;
    }

    public String registerApp(String appName, String appType, String appUri) {
        String response = "";
        try {

            URL url = new URL(BASE_URL + "/apps/" + appType + "/" + appName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("uri=" + appUri).getBytes());

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
            response = e.getMessage();
        } catch (Exception e) {
            response = e.getMessage();
        }
        return response;
    }

    public String createStream(String streamName, String streamDefinition, boolean deploy) {
        String response = "";
        try {
            URL url = new URL(BASE_URL + "/streams/definitions?deploy=" + deploy);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("name=" + streamName + "&definition=" + streamDefinition).getBytes());

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

    public String undeployStream(String streamName) {
        String response = "";
        try {

            URL url = new URL(BASE_URL + "/streams/deployments/" + streamName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);

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
            response = e.getMessage();
        }
        return response;
    }

    public String deployStream(String streamName) {
        String response = "";
        try {

            URL url = new URL(BASE_URL + "/streams/deployments/" + streamName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(("").getBytes());

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
            response = e.getMessage();
        } catch (Exception e) {
            response = e.getMessage();
        }
        return response;
    }

    public String deleteStream(String streamName) {
        String response = "";
        try {
            URL url = new URL(BASE_URL + "/streams/definitions/" + streamName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);

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
            response = e.getMessage();
        }
        return response;
    }

    public Streams getStreams() {
        Streams streams = new Streams();
        try {
            URL url = new URL(BASE_URL + "/streams/definitions");
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
            String response = res.toString();

            JSONObject json = new JSONObject(response);
            if (!json.has("_embedded")) {
                streams.setStreams(new ArrayList());
            } else {
                JSONArray jsstreams = json.getJSONObject("_embedded").getJSONArray("streamDefinitionResourceList");

                List<Stream> streamsList = new ArrayList();
                for (int i = 0; i < jsstreams.length(); i++) {
                    JSONObject jscurrent = jsstreams.getJSONObject(i);
                    Stream current = new Stream();
                    current.setName(jscurrent.getString("name"));
                    current.setStatus(jscurrent.getString("status"));
                    current.setDefinition(jscurrent.getString("dslText"));
                    streamsList.add(current);
                }
                streams.setStreams(streamsList);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return streams;
    }

    public Stream getStream(String streamId) {
        Stream stream = new Stream();
        try {
            URL url = new URL(BASE_URL + "/streams/definitions/" + streamId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = in.readLine()) != null) {
                res.append(line);
                res.append("\n");
            }
            in.close();
            conn.disconnect();
            String response = res.toString();

            JSONObject json = new JSONObject(response);
            stream.setName(json.getString("name"));
            stream.setStatus(json.getString("status"));
            stream.setDefinition(json.getString("dslText"));

        } catch (Exception e) {
            // TODO: error
            return null;
        }
        return stream;
    }

}
