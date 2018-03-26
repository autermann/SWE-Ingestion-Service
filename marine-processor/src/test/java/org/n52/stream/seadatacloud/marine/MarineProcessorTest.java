/*
 * Copyright 2018-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.stream.seadatacloud.marine;

import java.util.Collections;

import org.junit.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class MarineProcessorTest {

    MessageHeaders headers = new MessageHeaders(Collections.singletonMap("mqtt_receivedTopic", "spiddal-ctd"));

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnNullInput() throws Exception {
        new MarineProcessor().process(null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnNullPayload() throws Exception {
        new MarineProcessor().process(new GenericMessage<>(null, headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnEmptyPayload() throws Exception {
        new MarineProcessor().process(new GenericMessage<>("", headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongPayload() throws Exception {
        new MarineProcessor().process(new GenericMessage<>("wrong-input|not enough pipe separated chunks", headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongPayloadTimestampFormat() throws Exception {
        new MarineProcessor().process(new GenericMessage<>("wrong-input|not enough pipe |separated chunks", headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongPayloadChunkFormat() throws Exception {
        new MarineProcessor().process(new GenericMessage<>("2018-03-12T12:59:58.787Z|enough pipe |separated chunks", headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnEmptyTopic() throws Exception {
        MessageHeaders headers = new MessageHeaders(Collections.singletonMap("mqtt_receivedTopic", ""));
        new MarineProcessor().process(new GenericMessage<>("2018-03-12T12:59:58.787Z|enough pipe |separated chunks six we need now",
                headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnNullTopic() throws Exception {
        MessageHeaders headers = new MessageHeaders(Collections.singletonMap("mqtt_receivedTopic", null));
        new MarineProcessor().process(new GenericMessage<>("2018-03-12T12:59:58.787Z|enough pipe |separated chunks six we need now",
                headers));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongTopic() throws Exception {
        MessageHeaders headers = new MessageHeaders(Collections.singletonMap("mqtt_receivedTopic", "not-supported-topic"));
        new MarineProcessor().process(new GenericMessage<>("2018-03-12T12:59:58.787Z|enough pipe |separated chunks six we need now",
                headers));
    }

}
