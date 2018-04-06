package org.n52.stream.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.PhysicalSystem;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.stream.AbstractCodingTest;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DecoderHelperTest.class})
public class DecoderHelperTest extends AbstractCodingTest {

    private DecoderHelper helper;
    private Path path;
    
    @Before
    public void setUp() throws FileNotFoundException {
        DecoderRepository decoderRepository = initDecoderRepository();
        
        helper = new DecoderHelper();
        helper.setDecoderRepository(decoderRepository);
        path = Paths.get(ResourceUtils.getFile(this.getClass().getResource("/")).getPath(), "sensors", "AggregateProcess.xml");
    }
    
    @Test
    public void loadFromPath() throws DecodingException, IOException, XmlException {
        Object decode = helper.decode(path);
        Assert.isTrue(decode != null, "Should not null");
        Assert.isTrue(decode instanceof AggregateProcess, "Should be instance of AggregateProcess");
        AggregateProcess aggregateProcess = (AggregateProcess) decode;
        Assert.isTrue(aggregateProcess.isSetComponents(), "Should have Components");
        Assert.isTrue(aggregateProcess.getComponents().size() == 2, "Components size should be 2");
        Assert.isTrue(aggregateProcess.getComponents().get(0).getProcess() instanceof PhysicalSystem, "");
        PhysicalSystem physicalSystem = (PhysicalSystem) aggregateProcess.getComponents().get(0).getProcess();
        Assert.isTrue(physicalSystem.getOutputs().size() == 1, "");
        Assert.isTrue(physicalSystem.getOutputs().get(0).getIoValue() instanceof SmlDataInterface, "");
        Assert.isTrue(aggregateProcess.isSetConnections(), "Should have Connections");
        Assert.isTrue(aggregateProcess.getComponents().size() == 2, "Connections size should be 2");
    }
}
