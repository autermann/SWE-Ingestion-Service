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

import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class MarineProcessorTest {

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnNullInput() throws Exception {
        new MarineProcessor().process(null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnEmptyInput() throws Exception {
        new MarineProcessor().process("");
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongInput() throws Exception {
        new MarineProcessor().process("wrong-input|not enough pipe separated chunks");
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongTimestampFormat() throws Exception {
        new MarineProcessor().process("wrong-input|not enough pipe |separated chunks");
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongChunkFormat() throws Exception {
        new MarineProcessor().process("2018-03-12T12:59:58.787Z|enough pipe |separated chunks");
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExcptionOnWrongSensor() throws Exception {
        new MarineProcessor().process("2018-03-12T12:59:58.787Z|enough pipe |separated chunks six we need now");
    }

}
