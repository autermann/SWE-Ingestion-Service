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

    public Dataset process(OffsetDateTime timestamp, String sensorId, String featureId, List<String> values) {
        validateInput(timestamp, sensorId, featureId, values);
        if (values.size() != 7) {
            String valuesString = values.toString();
            if ("[Press, Temp, Cond, Sal, SoundV]".equalsIgnoreCase(valuesString) ||
                    "[Acquisition:, <^C>Stop]".equalsIgnoreCase(valuesString)) {
                // TODO throw exception or log something?
                // TODO is this valid here?
                return null;
            }
            throw createInvalidNumberOfValuesException("seven", values);
        }

        Dataset result = new Dataset();
        Feature feature = new Feature();
        feature.setId(featureId);

        Measurement<BigDecimal> fluorescenceWavelengthMeasurement = new Measurement<>();
        fluorescenceWavelengthMeasurement.setValue(new BigDecimal(values.get(2)));
        fluorescenceWavelengthMeasurement.setTimestamp(timestamp);

        Timeseries fluorescenceWavelengthTimeseries = new Timeseries();
        fluorescenceWavelengthTimeseries.setFeature(feature);
        fluorescenceWavelengthTimeseries.setPhenomenon("fluorescence-wavelength");
        fluorescenceWavelengthTimeseries.setSensor(sensorId);
        fluorescenceWavelengthTimeseries.setUnit("nm");
        fluorescenceWavelengthTimeseries.addMeasurementsItem(fluorescenceWavelengthMeasurement);

        result.addTimeseriesItem(fluorescenceWavelengthTimeseries);

        Measurement<Integer> chlMeasurement = new Measurement<>();
        chlMeasurement.setValue(Integer.parseInt(values.get(3)));
        chlMeasurement.setTimestamp(timestamp);

        Timeseries chlTimeseries = new Timeseries();
        chlTimeseries.setFeature(feature);
        chlTimeseries.setPhenomenon("CHL");
        chlTimeseries.setSensor(sensorId);
        chlTimeseries.addMeasurementsItem(chlMeasurement);

        result.addTimeseriesItem(chlTimeseries);

        Measurement<BigDecimal> turbidityWavelengthMeasurement = new Measurement<>();
        turbidityWavelengthMeasurement.setValue(new BigDecimal(values.get(4)));
        turbidityWavelengthMeasurement.setTimestamp(timestamp);

        Timeseries turbidityWavelengthTimeseries = new Timeseries();
        turbidityWavelengthTimeseries.setFeature(feature);
        turbidityWavelengthTimeseries.setPhenomenon("turbidity-wavelength");
        turbidityWavelengthTimeseries.setSensor(sensorId);
        turbidityWavelengthTimeseries.setUnit("nm");
        turbidityWavelengthTimeseries.addMeasurementsItem(turbidityWavelengthMeasurement);

        result.addTimeseriesItem(turbidityWavelengthTimeseries);

        Measurement<Integer> ntuMeasurement = new Measurement<>();
        ntuMeasurement.setValue(Integer.parseInt(values.get(5)));
        ntuMeasurement.setTimestamp(timestamp);

        Timeseries ntuTimeseries = new Timeseries();
        ntuTimeseries.setFeature(feature);
        ntuTimeseries.setPhenomenon("NTU");
        ntuTimeseries.setSensor(sensorId);
        ntuTimeseries.addMeasurementsItem(ntuMeasurement);

        result.addTimeseriesItem(ntuTimeseries);

        Measurement<Integer> thermistorMeasurement = new Measurement<>();
        thermistorMeasurement.setValue(Integer.parseInt(values.get(6)));
        thermistorMeasurement.setTimestamp(timestamp);

        Timeseries thermistorTimeseries = new Timeseries();
        thermistorTimeseries.setFeature(feature);
        thermistorTimeseries.setPhenomenon("thermistor");
        thermistorTimeseries.setSensor(sensorId);
        thermistorTimeseries.addMeasurementsItem(thermistorMeasurement);

        result.addTimeseriesItem(thermistorTimeseries);

        Measurement<Long> instrumentTimeDeviationMeasurement = new Measurement<>();
        String dateTimeString = new StringBuilder(values.get(0)).append(" ").append(values.get(1)).toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        LocalDateTime instrumentTimestamp = LocalDateTime.parse(dateTimeString, formatter);
        Long instrumentTimeDeviation = ChronoUnit.MILLIS.between(instrumentTimestamp, timestamp);
        instrumentTimeDeviationMeasurement.setValue(instrumentTimeDeviation);
        instrumentTimeDeviationMeasurement.setTimestamp(timestamp);

        Timeseries instrumentTimeDeviationTimeseries = new Timeseries();
        instrumentTimeDeviationTimeseries.setFeature(feature);
        instrumentTimeDeviationTimeseries.setPhenomenon("instrument-time-deviation");
        instrumentTimeDeviationTimeseries.setSensor(sensorId);
        instrumentTimeDeviationTimeseries.setUnit("ms");
        instrumentTimeDeviationTimeseries.addMeasurementsItem(instrumentTimeDeviationMeasurement);

        result.addTimeseriesItem(instrumentTimeDeviationTimeseries);

        return result;
    }

}
