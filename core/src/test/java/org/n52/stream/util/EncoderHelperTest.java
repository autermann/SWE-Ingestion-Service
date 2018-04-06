package org.n52.stream.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.stream.AbstractCodingTest;
import org.n52.stream.generate.InsertSensorGenerator;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.exception.EncodingException;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

public class EncoderHelperTest extends AbstractCodingTest {

    private EncoderHelper helper;
    private InsertSensorRequest request;
    
    @Before
    public void setUp() throws DecodingException, IOException, XmlException {
        DecoderHelper decoderHelper = new DecoderHelper();
        decoderHelper.setDecoderRepository(initDecoderRepository());
        Path path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "sensors", "AggregateProcess.xml");
        request = new InsertSensorGenerator().generate((PhysicalSystem) ((AggregateProcess) decoderHelper.decode(path)).getComponents().get(1).getProcess());
        helper = new EncoderHelper();
        helper.setEncoderRepository(initEncoderRepository());
    }
    
    @Test
    public void encode() throws EncodingException, IOException, XmlException {
        Assert.isTrue(request != null, "Request should not null");
        XmlObject encode = helper.encode(request);
        Assert.isTrue(encode != null, "Should not null");
    }
    
    @Test
    public void encodeToString() throws EncodingException, IOException, XmlException {
        Assert.isTrue(request != null, "Request should not null");
        String encode = helper.encodeToString(request);
        Assert.isTrue(encode != null, "Should not null");
        Assert.isTrue(!encode.isEmpty(), "Should not empty");
    }
}
