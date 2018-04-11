/*
 * Copyright (C) 2018-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
