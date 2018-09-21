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
package org.n52.stream.seadatacloud.processors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.n52.shetland.ogc.sensorML.elements.SmlComponent;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AbstractProcessV20;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweConstants.SweDataComponentType;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.shetland.ogc.swe.encoding.SweTextEncoding;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.stream.AbstractIngestionServiceApp;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import org.n52.stream.core.GeometryPoint;

/**
 *
 * @author Maurin Radtke <m.radtke@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike
 * Hinderk</a>
 */
@SpringBootApplication
@ComponentScan("org.n52.stream")
@ComponentScan("org.n52.stream.util")
@EnableBinding(Processor.class)
@EnableConfigurationProperties(AppConfiguration.class)
public class CsvProcessor extends AbstractIngestionServiceApp {

    private static final String DEFINITION_PHENOMENON_TIME = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";

    private static final String DEFINITION_RESULT_TIME = "http://www.opengis.net/def/property/OGC/0/ResultTime";

    private static final String DEFINITION_RESULT_GEOMETRY = "http://52north.org/geometry/point";

    private int msgCount = 0;
    private int processedMsgCount = 0;

    private int resultGeometryLatitudeIndex = -1;
    private int resultGeometryLongitudeIndex = -1;
    private int resultGeometryHeightIndex = -1;

    @Autowired
    private AppConfiguration properties;

    @Autowired
    @Named("sensorml")
    private AggregateProcess processDescription;

    private SweTextEncoding textEncoding;

    private SweDataRecord tokenAssigments;

    private DataMessage templateDataMessage;

    private Feature feature;

    private boolean isNoDataValueSet;

    private String noDataValue = null;

    private static final Logger LOG = LoggerFactory.getLogger(CsvProcessor.class);

    private DateTimeFormatter formatter;

    private boolean isReplaceDecimalSeparator = false;

    public static void main(String[] args) {
        SpringApplication.run(CsvProcessor.class, args);
    }

    /**
     * Init the processor by checking the properties and finalize the custom
     * configuration
     */
    @PostConstruct
    public void init() {
        LOG.info("init(); processor called");
        checkSetting("offering", properties.getOffering());
        checkSetting("sensor", properties.getSensor());
        checkSetting("sensorml-url", properties.getSensormlurl());
        checkSetting("componentidentifier", properties.getComponentindex());
        checkSetting("date-column-index", properties.getDateColumnIndex() + "");
        checkSetting("time-column-index", properties.getTimeColumnIndex() + "");
        if (!isDateTimeColumnIndex()) {
            checkSetting("date-column-format", properties.getDateColumnFormat());
            checkSetting("time-column-format", properties.getTimeColumnFormat());
        }
        formatter = initDateTimeFormatter();
        if (properties.getFeatureofinterestid() == null
                || properties.getFeatureofinterestid().isEmpty()) {
            throw logErrorAndCreateException("No Feature Of Interest Identifier received.");
            // TODO: implement workflow for foiId contained in csv data
            //extractFeature(process);
        } else {
            feature = new Feature().setId(properties.getFeatureofinterestid());
        }
        if (properties.getNodatavalue() != null && !properties.getNodatavalue().isEmpty()) {
            isNoDataValueSet = true;
            noDataValue = properties.getNodatavalue();
        }
        AbstractProcessV20 process = checkAndExtractProcess();
        SmlDataInterface dataInterface = checkAndExtractDataInterface(process);
        checkAndStoreEncoding(dataInterface);
        if (!textEncoding.getDecimalSeparator().equals(".")) {
            isReplaceDecimalSeparator = true;
        }
        storeTokenAssignments(dataInterface);
        if (properties.getDateColumnIndex() == -1) {
            Integer extractedIndex = extractTimestampIndex();
            properties.setDateColumnIndex(extractedIndex);
            properties.setTimeColumnIndex(extractedIndex);
        }
        extractResultGeometryIndices();
        fillTemplateDataMessage();
        LOG.info("CsvProcessor initialized for procedure: {}", properties.getSensor());
    }

    private void extractResultGeometryIndices() {
        List<SweField> sweFields = tokenAssigments.getFields();
        for (int i = 0; i < sweFields.size(); i++) {
            SweAbstractDataComponent sweElement = sweFields.get(i).getElement();
            if (sweElement != null
                    && sweElement.getDefinition() != null
                    && sweElement.getDefinition().contains(DEFINITION_RESULT_GEOMETRY)) {
                if (sweElement.getDefinition().toLowerCase().endsWith("latitude")) {
                    resultGeometryLatitudeIndex = i;
                } else if (sweElement.getDefinition().toLowerCase().endsWith("longitude")) {
                    resultGeometryLongitudeIndex = i;
                } else if (sweElement.getDefinition().toLowerCase().endsWith("height")) {
                    resultGeometryHeightIndex = i;
                }
            }
        }
    }

    private Integer extractTimestampIndex() {
        int phenTimeIndex = tokenAssigments.getFieldIndexByIdentifier(DEFINITION_PHENOMENON_TIME);
        if (phenTimeIndex != -1) {
            return phenTimeIndex;
        }

        int resultTimeIndex = tokenAssigments.getFieldIndexByIdentifier(DEFINITION_RESULT_TIME);
        if (resultTimeIndex != -1) {
            return resultTimeIndex;
        }
        throw logErrorAndCreateException("No field with result or phenomenon time found => cancel processing");
    }

    private void fillTemplateDataMessage() throws IllegalArgumentException {
        templateDataMessage = new DataMessage();
        //SweAbstractDataComponent tokenSpecification = tokenAssigments.getFields().get(i).getElement();
        boolean phenTimeSet = false;
        for (SweField field : tokenAssigments.getFields()) {
            SweAbstractDataComponent sweElement = field.getElement();
            if (sweElement == null || !sweElement.isSetDefinition() || sweElement.getDefinition().contains(DEFINITION_RESULT_TIME)) {
                continue;
            } else if (sweElement.getDefinition().contains(DEFINITION_PHENOMENON_TIME)) {
                phenTimeSet = true;
            } else {
                String phenomenon = sweElement.getDefinition();
                Timeseries<?> ts = null;
                String unit = null;
                SweDataComponentType sweType = sweElement.getDataComponentType();
                switch (sweType) {
                    case Boolean:
                        ts = new Timeseries<Boolean>();
                        break;
                    case Category:
                        unit = ((SweCategory) sweElement).getUom();
                    case Text:
                        ts = new Timeseries<String>();
                        break;
                    case Quantity:
                        ts = new Timeseries<BigDecimal>();
                        unit = ((SweQuantity) sweElement).getUom();
                        break;
                    case Count:
                        ts = new Timeseries<Integer>();
                        break;
                    case Time:
                        continue;
                    default:
                        throw logErrorAndCreateException(
                                String.format("Not supported swe:field element type found '%s'!",
                                        sweType));
                }
                ts.withPhenomenon(phenomenon)
                        .withSensor(properties.getSensor())
                        .withFeature(feature);
                if (unit != null) {
                    ts.setUnit(unit);
                }
                templateDataMessage.addTimeseriesItem(ts);
            }
        }
        if (!phenTimeSet) {
            throw logErrorAndCreateException("PhenomenonTime not configured in Datastream definition");
        }
    }

    private void storeTokenAssignments(SmlDataInterface dataInterface) throws IllegalArgumentException {
        SweAbstractDataComponent elementType = dataInterface.getData().getElementType();
        if (!(elementType instanceof SweDataRecord)) {
            throw logErrorAndCreateException(
                    String.format("Datastream not encoded with DataRecord as ElementType! Found type '%s'.",
                            elementType.getClass().getName()));
        }
        tokenAssigments = (SweDataRecord) elementType;
    }

    private void checkAndStoreEncoding(SmlDataInterface dataInterface) throws IllegalArgumentException {
        SweAbstractEncoding encoding = dataInterface.getData().getEncoding();
        if (!(encoding instanceof SweTextEncoding)) {
            throw logErrorAndCreateException(
                    String.format("Datastream not encoded with text encoding! Found type: '%s'.",
                            encoding.getClass().getName()));
        }
        textEncoding = (SweTextEncoding) encoding;
    }

    private SmlDataInterface checkAndExtractDataInterface(AbstractProcessV20 process) throws IllegalArgumentException {
        if (!process.isSetOutputs() || process.getOutputs().isEmpty()) {
            throw logErrorAndCreateException("Element <outputs><OutputList><output> of first component is NOT set!");
        }
        List<SmlIo> ioValue = process.getOutputs().stream().
                filter(p -> p.getIoName().equalsIgnoreCase("streamOutput")
                && p.getIoValue().getClass().getName().equalsIgnoreCase(SmlDataInterface.class.getName())).
                collect(Collectors.toList());
        if (ioValue.isEmpty() || ioValue.size() > 1 || !(ioValue.get(0).getIoValue() instanceof SmlDataInterface)) {
            throw logErrorAndCreateException(
                    "Element <outputs><OutputList><output><DataInterface> of 1st component is not set correct!");
        }
        SmlDataInterface dataInterface = (SmlDataInterface) ioValue.get(0).getIoValue();
        if (dataInterface.getData().isEmpty()) {
            throw logErrorAndCreateException(
                    "Element <outputs><OutputList><output><DataInterface><Data> of 1st component is not set correct!");
        }
        return dataInterface;
    }

//    private void extractFeature(AbstractProcessV20 process) throws IllegalArgumentException {
//        if (!process.isSetSmlFeatureOfInterest()) {
//            throw logErrorAndCreateException("Element <featureofinterest> of first component is NOT set!");
//        }
//        Set<String> featuresOfInterest = process.getSmlFeatureOfInterest().getFeaturesOfInterest();
//        if (featuresOfInterest.isEmpty() || featuresOfInterest.size() > 1) {
//            throw logErrorAndCreateException("Only ONE Element <featureofinterest> of first component supported!");
//        }
//        feature = new Feature().withId(featuresOfInterest.iterator().next());
//    }
    private AbstractProcessV20 checkAndExtractProcess() throws IllegalArgumentException {
        if (!processDescription.isSetComponents() || processDescription.getComponents().isEmpty()) {
            throw logErrorAndCreateException("AggregateProcess does not contain any component!");
        }
        SmlComponent smlComponent = processDescription.getComponents().get(properties.getComponentindex());
        if (smlComponent.isReferencedExternally()) {
            throw logErrorAndCreateException("First component is referenced externally but should not!");
        }
        if (!(smlComponent.getProcess() instanceof AbstractProcessV20)) {
            throw logErrorAndCreateException(
                    String.format("First component is not of type '%s' but '%s'!",
                            AbstractProcessV20.class,
                            smlComponent.getProcess().getClass()));
        }
        AbstractProcessV20 process = (AbstractProcessV20) smlComponent.getProcess();
        return process;
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public DataMessage process(Message<String> csvMessage) {
        msgCount++;
        if (csvMessage == null) {
            throw logErrorAndCreateException("NO CSV message received! Input is 'null'.");
        }
        String messagePayload = csvMessage.getPayload();
        if (messagePayload == null || messagePayload.isEmpty()) {
            throw logErrorAndCreateException("Empty CSV payload received.");
        }
        DataMessage processedDataset = processPayload(messagePayload);
        processedMsgCount++;
        LOG.info(getDataMessageLog(processedDataset));
        return processedDataset;
    }

    private DataMessage processPayload(String messagePayload) {
        LOG.trace("Message-Payload received: {}", messagePayload);
        List<String> payloadBlocks = Arrays.asList(messagePayload.split(textEncoding.getBlockSeparator()));
        if (payloadBlocks.isEmpty()) {
            return null;
        }
        DataMessage dataMessage = templateDataMessage.clone();
        for (String block : payloadBlocks) {
            List<String> tokens = Arrays.asList(block.split(textEncoding.getTokenSeparator()));
            if (tokens.isEmpty()) {
                continue;
            }
            OffsetDateTime resultTime = getCsvTimestamp(block);
            OffsetDateTime phenTime = resultTime;
            List<Measurement<?>> measurements = new LinkedList<>();
            if (tokens.size() != tokenAssigments.getFields().size()) {
                throw logErrorAndCreateException(
                        "Number of values in message does NOT match number of values in stream definition.");
            }
            GeometryPoint resultGeometry = getCsvResultGeometry(block);
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                SweAbstractDataComponent tokenSpecification = tokenAssigments.getFields().get(i).getElement();
//                if ((tokenSpecification == null) || (!tokenSpecification.isSetDefinition())) {
//                    continue;
//                }
                String definition = null;
                if (tokenSpecification != null && tokenSpecification.isSetDefinition()) {
                    definition = tokenSpecification.getDefinition();
                    if (!definition.contains(DEFINITION_RESULT_TIME)
                            && !definition.contains(DEFINITION_PHENOMENON_TIME)
                            && !definition.contains(DEFINITION_RESULT_GEOMETRY)
                            && isNoDataValueSet && !noDataValue.equalsIgnoreCase(token) || !isNoDataValueSet) {
                        processDataValue(dataMessage, measurements, token, tokenSpecification, definition);
                    } else {
                        continue;
                    }
                } else {
                    processDataValue(dataMessage, measurements, token, tokenSpecification, definition);
                    continue;
                }
            }
            setTimestamps(resultTime, phenTime, measurements);
            if (resultGeometry != null
                    && resultGeometry.hasLongitude()
                    && resultGeometry.hasLatitude()) {
                setResultGeometry(resultGeometry, measurements);
            }
        }
        return dataMessage;
    }

    /*
     * We can suppress these warnings because we know the type of the values from the specification. If something
     * fails, the input MUST be invalid and an exception is okay!
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDataValue(DataMessage dataMessage, List<Measurement<?>> measurements, String token,
            SweAbstractDataComponent tokenSpecification, String definition)
            throws IllegalArgumentException, NumberFormatException {
        Optional<Timeseries<?>> ts = dataMessage.getTimeseriesForPhenomenon(definition);
        if (!ts.isPresent()) {
            LOG.trace("Could not get timeseries for phenomenon '%s'!",
                    definition);
            return;
        }
        Timeseries<?> timeseries = ts.get();
        SweDataComponentType sweType = tokenSpecification.getDataComponentType();
        Measurement<?> measurementsItem = createMeasurement(token, sweType);
        if (measurementsItem == null) {
            LOG.trace("Could not create measurement for token '" + token + "'.");
            return;
        }
        timeseries.addMeasurementsItem((Measurement) measurementsItem);
        measurements.add(measurementsItem);
    }

    private GeometryPoint getCsvResultGeometry(String csvLine) {
        GeometryPoint result = null;
        String latitude = null;
        String longitude = null;
        String height = null;
        String[] split = csvLine.split(textEncoding.getTokenSeparator());
        if (resultGeometryLatitudeIndex > -1
                && resultGeometryLongitudeIndex > -1) {
            result = new GeometryPoint();
            latitude = split[resultGeometryLatitudeIndex];
            longitude = split[resultGeometryLongitudeIndex];
            if (resultGeometryHeightIndex > -1) {
                height = split[resultGeometryHeightIndex];
                try {
                    Double alt = Double.parseDouble(height.replace(textEncoding.getDecimalSeparator(), "."));
                    result.setHeight(alt);
                } catch (IllegalArgumentException iae) {
                    LOG.debug("No height given for csvLine " + msgCount);
                }
            }
            try {
                Double lat = Double.parseDouble(latitude.replace(textEncoding.getDecimalSeparator(), "."));
                result.setLatitude(lat);
                Double lng = Double.parseDouble(longitude.replace(textEncoding.getDecimalSeparator(), "."));
                result.setLongitude(lng);
            } catch (IllegalArgumentException iae) {
                LOG.debug("No latitude or longitude given for csvLine " + msgCount);
            }
        }
        return result;
    }

    private OffsetDateTime getCsvTimestamp(String csvLine) {
        String datetime = null;
        String[] split = csvLine.split(textEncoding.getTokenSeparator());
        if (isDateTimeColumnIndex()) {
            datetime = split[properties.getDateColumnIndex()];
        } else {
            StringBuilder time = new StringBuilder();
            time.append(split[properties.getDateColumnIndex()]);
            time.append(" ");
            time.append(split[properties.getTimeColumnIndex()]);
            datetime = time.toString();
        }
        return LocalDateTime.parse(datetime, formatter).atOffset(ZoneOffset.UTC);
    }

    private DateTimeFormatter initDateTimeFormatter() {
        if (!isDateTimeColumnIndex()) {
            StringBuilder pattern = new StringBuilder();
            pattern.append(properties.getDateColumnFormat());
            pattern.append(" ");
            pattern.append(properties.getTimeColumnFormat());
            return DateTimeFormatter.ofPattern(pattern.toString());
        } else if (isDateColumnFormat()) {
            return DateTimeFormatter.ofPattern(properties.getDateColumnFormat());
        }
        return DateTimeFormatter.ISO_DATE_TIME;
    }

    private boolean isDateTimeColumnIndex() {
        return properties.getDateColumnIndex() == properties.getTimeColumnIndex();
    }

    private boolean isDateColumnFormat() {
        return properties.getDateColumnFormat() != null && !properties.getDateColumnFormat().isEmpty();
    }

    private Measurement<?> createMeasurement(String token, SweDataComponentType sweType)
            throws NumberFormatException, IllegalArgumentException {
        Measurement<?> measurementsItem = null;
        if (token.isEmpty()) {
            LOG.trace("Data value is empty.");
            return null;
        }
        switch (sweType) {
            case Boolean:
                Measurement<Boolean> booleanMeasurement = new Measurement<>();
                booleanMeasurement.setValue(Boolean.valueOf(token));
                measurementsItem = booleanMeasurement;
                break;
            case Category:
            case Text:
                Measurement<String> stringMeasurement = new Measurement<>();
                stringMeasurement.setValue(token);
                measurementsItem = stringMeasurement;
                break;
            case Quantity:
                Measurement<BigDecimal> bigDecimalMeasurement = new Measurement<>();
                if (isReplaceDecimalSeparator) {
                    token = token.replaceAll(textEncoding.getDecimalSeparator(), ".");
                }
                bigDecimalMeasurement.setValue(new BigDecimal(token));
                measurementsItem = bigDecimalMeasurement;
                break;
            case Count:
                Measurement<Integer> integerMeasurement = new Measurement<>();
                integerMeasurement.setValue(Integer.parseInt(token));
                measurementsItem = integerMeasurement;
                break;
            default:
                throw logErrorAndCreateException(
                        String.format("Not supported swe:field element type found '%s'!",
                                sweType));
        }
        return measurementsItem;
    }

    private void setTimestamps(OffsetDateTime resultTime, OffsetDateTime phenTime, List<Measurement<?>> measurements) {
        for (Measurement<?> measurement : measurements) {
            if (phenTime == null) {
                throw logErrorAndCreateException("Missing timestamp in payload!");
            }
            measurement.setPhenomenonTime(phenTime);
            if (resultTime != null) {
                measurement.setResultTime(resultTime);
            }
        }
    }

    private void setResultGeometry(GeometryPoint geometryPoint, List<Measurement<?>> measurements) {
        for (Measurement<?> measurement : measurements) {
            measurement.setResultGeometry(geometryPoint);
        }
    }

    @VisibleForTesting
    public String getDataMessageLog(DataMessage processedDataset) {
        ObjectNode n = nodeFactory().objectNode();
        n.set("DataMessage", getJson(processedDataset));
        n.put("number", processedMsgCount);
        n.put("of", msgCount);
        return n.toString();
//
//
//        StringBuilder sb = new StringBuilder("{");
//        sb.append("\"DataMessage\":").append(getJson(processedDataset)).append(",");
//        sb.append("\"number\":").append(processedMsgCount).append(",");
//        sb.append("\"of\":").append(msgCount);
//        sb.append("}");
//        return sb.toString();
    }
}
