/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.stream.seadatacloud.restcontroller.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.xmlbeans.XmlOptions;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.stream.seadatacloud.restcontroller.model.AppOption;
import org.n52.stream.seadatacloud.restcontroller.model.Source;
import org.n52.stream.seadatacloud.restcontroller.model.Stream;
import org.n52.stream.seadatacloud.restcontroller.service.CloudService;
import org.n52.svalbard.decode.DecoderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 */
@RestController
@Component
@RequestMapping("/api/streams")
public class StreamController {

    private static final Logger LOG = LoggerFactory.getLogger(StreamController.class);

    public final String APPLICATION_JSON = "application/json";
    public final String APPLICATION_XML = "application/xml";

    @Autowired
    CloudService service;

    @Autowired
    AppController appController;

    @Autowired
    StreamController streamController;
    
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_XML)
    public ResponseEntity<Stream> uploadConfig(
            @RequestBody byte[] requestBody) {

        try {
            Path path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "tmp.xml");
            String fileName = path.toAbsolutePath().toString();
            Files.write(Paths.get(fileName), requestBody);

            DecoderRepository decoderRepository = initDecoderRepository();

            DecoderHelper helper = new DecoderHelper();
            helper.setDecoderRepository(decoderRepository);
            path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "tmp.xml");

            Object decode = helper.decode(path);
            
                    
            if (decode instanceof AggregateProcess) {
                ArrayList<SmlComponent> al = (ArrayList<SmlComponent>) ((AggregateProcess) decode).getComponents();
                SmlComponent comp = al.get(0);
                // TODO: instanceof Abfragen:
                AbstractProcess asml = (AbstractProcess) comp.getProcess();
                ArrayList<SmlIo> smlOutputs = (ArrayList<SmlIo>) asml.getOutputs();
                SmlIo smlIo = smlOutputs.get(0);
                SmlDataInterface smlDataInterface = (SmlDataInterface) smlIo.getIoValue();
                SweDataRecord sdr = smlDataInterface.getInterfaceParameters();
                LinkedList<SweField> sweFields = (LinkedList<SweField>) sdr.getFields();
                String sourceName = "";
                Source source = null;

                for (SweField current : sweFields) {
                    SweText sweText = (SweText) current.getElement();
                    if (sweText.getDefinition().equals("source")) {
                        sourceName = sweText.getValue();
                        source = appController.getSourceByName(sourceName);
                        if (source == null) {
                            return new ResponseEntity("Source '" + sourceName + "' not found", HttpStatus.NOT_FOUND);
                        }
                    }
                }
                if (source == null) {
                    return new ResponseEntity("Source '" + sourceName + "' not found", HttpStatus.NOT_FOUND);
                }
                sourceName = source.getName();

                String streamSourceDefinition = "";
                String streamDefinition = "";

                for (SweField current : sweFields) {
                    SweText sweText = (SweText) current.getElement();
                    if (!sweText.getDefinition().equals("source")) {
                        String appOptionName = sweText.getDefinition();
                        AppOption ao = appController.getSourceOptionByName(source, appOptionName);
                        if (ao == null) {
                            return new ResponseEntity("Option '" + appOptionName + "' is not supported by source '" + sourceName + "'.", HttpStatus.EXPECTATION_FAILED);
                        }
                        streamSourceDefinition += " --" + ao.getName() + "=" + sweText.getValue();
                    }
                };
                if (streamSourceDefinition.length() > 0) {
                    streamDefinition = sourceName + " " + streamSourceDefinition + " ";
                } else {
                    streamDefinition = sourceName + " ";
                }
                
                // TODO: parse processor...
                // TODO: parse sink...
                streamDefinition += "| log-sink --semsormlurl="+path;
                
                Stream createdStream = service.createStream("aStreamName", streamDefinition, false);
                if (createdStream != null) {
                    return new ResponseEntity(createdStream, HttpStatus.CREATED);
                } else {
                    return new ResponseEntity(null, HttpStatus.CONFLICT);
                }
            } else {
                return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }
    }

    private DecoderRepository initDecoderRepository() {
        DecoderRepository decoderRepository = new DecoderRepository();

        SensorMLDecoderV20 sensorMLDecoderV20 = new SensorMLDecoderV20();
        sensorMLDecoderV20.setDecoderRepository(decoderRepository);
        sensorMLDecoderV20.setXmlOptions(XmlOptions::new);

        SweCommonDecoderV20 sweCommonDecoderV20 = new SweCommonDecoderV20();
        sweCommonDecoderV20.setDecoderRepository(decoderRepository);
        sweCommonDecoderV20.setXmlOptions(XmlOptions::new);

        GmlDecoderv321 gmlDecoderv321 = new GmlDecoderv321();
        gmlDecoderv321.setDecoderRepository(decoderRepository);
        gmlDecoderv321.setXmlOptions(XmlOptions::new);

        SamplingDecoderv20 samplingDecoderv20 = new SamplingDecoderv20();
        samplingDecoderv20.setDecoderRepository(decoderRepository);
        samplingDecoderv20.setXmlOptions(XmlOptions::new);

        decoderRepository.setDecoders(Arrays.asList(sensorMLDecoderV20,
                sweCommonDecoderV20,
                gmlDecoderv321,
                samplingDecoderv20));
        decoderRepository.init();
        return decoderRepository;
    }

}
