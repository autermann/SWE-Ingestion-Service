package org.n52.stream.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.janmayen.http.MediaTypes;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.ows.service.OwsOperationKey;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.svalbard.encode.AbstractXmlEncoder;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.OperationRequestEncoderKey;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@ImportResource("classpath:svalbard-*.xml")
@Component
public class EncoderHelper {

    @Autowired
    private EncoderRepository encoderRepository;

    @PostConstruct
    protected void init() {
        encoderRepository.init();
    }

    public XmlObject encode(InsertSensorRequest request)
            throws IOException,
            EncodingException,
            XmlException {
        Encoder<XmlObject, Object> encoder = getEncoder(request);
        if (encoder != null) {
            return encoder.encode(request);
        }
        return null;
    }

    
    public String encodeToString(InsertSensorRequest request)
            throws IOException,
            EncodingException,
            XmlException {
        XmlObject xml = encode(request);
        if (xml != null) {
            return xml.xmlText(((AbstractXmlEncoder) getEncoder(request)).getXmlOptions());
        }
        return null;
    }

    protected void setEncoderRepository(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }
    
    private Encoder<XmlObject, Object> getEncoder(InsertSensorRequest request) {
        OwsOperationKey key = new OwsOperationKey(request.getService(), request.getVersion(), request.getOperationName());
        return encoderRepository.getEncoder(new OperationRequestEncoderKey(key, MediaTypes.APPLICATION_XML));
    }
}
