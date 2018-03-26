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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.n52.stream.core.Dataset;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;

/**
 * Processes MQTT messages from:<br>
 * <em>Host</em>: <code>mqtt.marine.ie</code><br>
 * <em>Port</em>: <code>1883</code><br>
 * <em>Topics</em>: <code>spiddal-fluorometer</code><br>
 * <br>
 * <em>Raw Payload</em>:
 * <code>2018-03-12T13:00:39.035Z|WL-ECO-FLNTU-4476|03/12/18    12:57:07    695 43  700 55  554</code><br>
 * <br>
 * <em>Formatted Payload</em>:<br>
 * <em>Time</em>: 2018-03-12T13:00:39.035Z<br>
 * <em>Sensor</em>: WL-ECO-FLNTU-4476<br>
 * <em>Fluorescence Wavelength</em>: 695 nm<br>
 * <em>CHL</em>: 43 counts<br>
 * <em>Turbidity Wavelength</em>: 700 nm<br>
 * <em>NTU</em>: 55 counts<br>
 * <em>Thermistor</em>: 554<br>
 * <em>Instrument Time</em>: 03/12/18 12:57:07

 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class ProcessorFluorometer extends ProcessorSkeleton {

    public Dataset process(OffsetDateTime receiverStationTimestamp, String sensorId, String featureId, List<String> values) {
        validateInput(receiverStationTimestamp, sensorId, featureId, values);
        if (values.size() != 7) {
            String valuesString = values.toString();
            if ("[Press, Temp, Cond, Sal, SoundV]".equalsIgnoreCase(valuesString) ||
                    "[Acquisition:, <^C>Stop]".equalsIgnoreCase(valuesString)) {
                // TODO is this valid here?
                return null;
            }
            throw createInvalidNumberOfValuesException("seven", values);
        }

        Feature feature = new Feature();
        feature.setId(featureId);

        String dateTimeString = new StringBuilder(values.get(0)).append(" ").append(values.get(1)).toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        OffsetDateTime instrumentTimestamp = OffsetDateTime.of(
                LocalDateTime.parse(dateTimeString, formatter),
                ZoneOffset.UTC);

        Measurement<Long> instrumentTimeDeviationMeasurement = new Measurement<>();
        Long instrumentTimeDeviation = ChronoUnit.SECONDS.between(instrumentTimestamp, receiverStationTimestamp);
        instrumentTimeDeviationMeasurement.setValue(instrumentTimeDeviation);
        instrumentTimeDeviationMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<Long> instrumentTimeDeviationTimeseries = new Timeseries<>();
        instrumentTimeDeviationTimeseries.setFeature(feature);
        instrumentTimeDeviationTimeseries.setPhenomenon("receiver-latency");
        instrumentTimeDeviationTimeseries.setSensor(sensorId);
        instrumentTimeDeviationTimeseries.setUnit("s");
        instrumentTimeDeviationTimeseries.addMeasurementsItem(instrumentTimeDeviationMeasurement);

        Measurement<BigDecimal> fluorescenceWavelengthMeasurement = new Measurement<>();
        fluorescenceWavelengthMeasurement.setValue(new BigDecimal(values.get(2)));
        fluorescenceWavelengthMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<BigDecimal> fluorescenceWavelengthTimeseries = new Timeseries<>();
        fluorescenceWavelengthTimeseries.setFeature(feature);
        fluorescenceWavelengthTimeseries.setSensor(sensorId);
        fluorescenceWavelengthTimeseries.setPhenomenon("fluorescence-wavelength");
        fluorescenceWavelengthTimeseries.setUnit("nm");
        fluorescenceWavelengthTimeseries.addMeasurementsItem(fluorescenceWavelengthMeasurement);

        Measurement<Integer> chlMeasurement = new Measurement<>();
        chlMeasurement.setValue(Integer.parseInt(values.get(3)));
        chlMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<Integer> chlTimeseries = new Timeseries<>();
        chlTimeseries.setFeature(feature);
        chlTimeseries.setSensor(sensorId);
        chlTimeseries.setPhenomenon("CHL");
        chlTimeseries.addMeasurementsItem(chlMeasurement);

        Measurement<BigDecimal> turbidityWavelengthMeasurement = new Measurement<>();
        turbidityWavelengthMeasurement.setValue(new BigDecimal(values.get(4)));
        turbidityWavelengthMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<BigDecimal> turbidityWavelengthTimeseries = new Timeseries<>();
        turbidityWavelengthTimeseries.setFeature(feature);
        turbidityWavelengthTimeseries.setSensor(sensorId);
        turbidityWavelengthTimeseries.setPhenomenon("turbidity-wavelength");
        turbidityWavelengthTimeseries.setUnit("nm");
        turbidityWavelengthTimeseries.addMeasurementsItem(turbidityWavelengthMeasurement);

        Measurement<Integer> ntuMeasurement = new Measurement<>();
        ntuMeasurement.setValue(Integer.parseInt(values.get(5)));
        ntuMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<Integer> ntuTimeseries = new Timeseries<>();
        ntuTimeseries.setFeature(feature);
        ntuTimeseries.setSensor(sensorId);
        ntuTimeseries.setPhenomenon("NTU");
        ntuTimeseries.addMeasurementsItem(ntuMeasurement);

        Measurement<Integer> thermistorMeasurement = new Measurement<>();
        thermistorMeasurement.setValue(Integer.parseInt(values.get(6)));
        thermistorMeasurement.setTimestamp(instrumentTimestamp);

        Timeseries<Integer> thermistorTimeseries = new Timeseries<>();
        thermistorTimeseries.setFeature(feature);
        thermistorTimeseries.setPhenomenon("thermistor");
        thermistorTimeseries.setSensor(sensorId);
        thermistorTimeseries.addMeasurementsItem(thermistorMeasurement);

        Dataset result = new Dataset();
        result.addTimeseriesItem(fluorescenceWavelengthTimeseries);
        result.addTimeseriesItem(chlTimeseries);
        result.addTimeseriesItem(turbidityWavelengthTimeseries);
        result.addTimeseriesItem(ntuTimeseries);
        result.addTimeseriesItem(thermistorTimeseries);
        result.addTimeseriesItem(instrumentTimeDeviationTimeseries);

        return result;
    }

}
