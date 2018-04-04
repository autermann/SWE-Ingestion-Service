package org.n52.stream.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@ImportResource("classpath:svalbard-*.xml")
@Component
public class DecoderHelper {
    
    @Autowired
    private DecoderRepository decoderRepository;
    
    @PostConstruct
    protected void init() {
        decoderRepository.init();
    }

    public Object decode(XmlObject xml) throws IOException, DecodingException, XmlException {
        Decoder<Object, Object> decoder = decoderRepository.getDecoder(CodingHelper.getDecoderKey(xml));
        if (decoder != null) {
            return decoder.decode(xml);
        }
        return null;
    }

    public Object decode(InputStream inputStream) throws XmlException, IOException, DecodingException {
        return decode(XmlObject.Factory.parse(inputStream));
    }
    
    public Object decode(Path path) throws IOException, DecodingException, XmlException {
        return decode(Files.newInputStream(path));
    }
    
    protected void setDecoderRepository(DecoderRepository decoderRepository) {
        this.decoderRepository = decoderRepository;
    }
}
