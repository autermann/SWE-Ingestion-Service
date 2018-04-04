package org.n52.stream.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.util.Assert;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.GmlDecoderv321;
import org.n52.svalbard.decode.SensorMLDecoderV20;
import org.n52.svalbard.decode.SweCommonDecoderV20;
import org.n52.svalbard.decode.exception.DecodingException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DecoderHelperTest.class})
public class DecoderHelperTest {

    private DecoderHelper helper;
    private Path path;
    
    @Before
    public void setUp() throws FileNotFoundException {
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

        decoderRepository.setDecoders(Arrays.asList(sensorMLDecoderV20,
                                                    sweCommonDecoderV20,
                                                    gmlDecoderv321));
        decoderRepository.init();
        
        helper = new DecoderHelper();
        helper.setDecoderRepository(decoderRepository);
        path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "sensors", "AggregateProcess.xml");
    }
    
    @Test
    public void loadFromPath() throws DecodingException, IOException, XmlException {
        Object decode = helper.decode(path);
        Assert.isTrue(decode != null);
        Assert.isTrue(decode instanceof AggregateProcess);
        AggregateProcess aggregateProcess = (AggregateProcess) decode;
        Assert.isTrue(aggregateProcess.isSetComponents());
        Assert.isTrue(aggregateProcess.getComponents().size() == 2);
        Assert.isTrue(aggregateProcess.getComponents().get(0).getProcess() instanceof PhysicalSystem);
        PhysicalSystem physicalSystem = (PhysicalSystem) aggregateProcess.getComponents().get(0).getProcess();
        Assert.isTrue(physicalSystem.getOutputs().size() == 1);
        Assert.isTrue(physicalSystem.getOutputs().get(0).getIoValue() instanceof SmlDataInterface);
        Assert.isTrue(aggregateProcess.isSetConnections());
        Assert.isTrue(aggregateProcess.getComponents().size() == 2);
    }
}
