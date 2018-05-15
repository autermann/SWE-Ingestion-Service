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
package org.n52.stream.seadatacloud.dbsink;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.n52.janmayen.Json;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AggregateProcess;
import org.n52.stream.AbstractIngestionServiceApp;
import org.n52.stream.core.DataMessage;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.n52.stream.seadatacloud.dbsink.dao.DaoFactory;
import org.n52.stream.seadatacloud.dbsink.dao.DatasetDao;
import org.n52.stream.seadatacloud.dbsink.dao.ObservationDao;
import org.n52.stream.seadatacloud.dbsink.dao.OfferingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;

/**
 * {@link Sink} implementation for database insertion.
 * 
 * Requires an existing database with SOS data model
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages={"org.n52.stream.util"})
@EnableTransactionManagement
@Transactional
@EnableBinding(Sink.class)
@EnableConfigurationProperties(AppConfiguration.class)
public class DatabaseSinkApplication extends AbstractIngestionServiceApp {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSinkApplication.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private AppConfiguration properties;

    @Autowired(required=false)
    @Named("sensorml")
    private AggregateProcess processDescription;

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSinkApplication.class, args);
    }

    /**
     * Init the processor by checking the properties and finalize the custom configuration
     */
    @PostConstruct
    public void init() {
        checkSetting("offering", properties.getOffering());
        checkSetting("sensor", properties.getSensor());
        LOG.info("DbSink successfully initialized!");
    }

    /**
     * Input method to process the {@link DataMessage}s
     * 
     * @param message to process
     */
    @Transactional(rollbackFor=Exception.class)
    @StreamListener(Sink.INPUT)
    public synchronized void input(DataMessage message) {
        if (message != null && message.getTimeseries() != null) {
            Session session = null;
            try {
                session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
                DaoFactory daoFactory = new DaoFactory(session);
                session.beginTransaction();
                DatasetDao datasetDao = daoFactory.getDatasetDao();
                OfferingDao offeringDao = daoFactory.getOfferingDao();
                for (Timeseries<?> t : message.getTimeseries()) {
                    if (t.hasMeasurements()) {
                        ProcedureEntity procedure = daoFactory.getProcedureDAO().get(t.getSensor());
                        if (procedure != null && properties.getSensor().equals(t.getSensor())) {
                            DatasetEntity datasetEntity = datasetDao.getOrInsert(t, getOutputs(), properties.getOffering());
                            if (datasetEntity != null) {
                                ObservationDao observationDao = daoFactory.getObservationDao();
                                Data<?> first = null;
                                Data<?> last = null;
                                for (Measurement<?> m : t.getMeasurements()) {
                                    Data<?> data = observationDao.persist(m, datasetEntity, getOutputs());
                                    first = checkFirst(first, data);
                                    last = checkLast(last, data);
                                }
                                // update dataset and offering with times and geometry
                                datasetDao.updateMetadata(datasetEntity, first, last);
                                offeringDao.updateMetadata(datasetEntity.getOffering(), first, last, null);
                            }
                        }
                    }
                }
                session.getTransaction().commit();
                logSuccessfulInsertion(message);
            } catch (Exception e) {
                session.getTransaction().rollback();
                logFailedInsertion(message);
            } finally {
                if (session != null && session.isOpen()) {
                    session.clear();
                    session.close();
                }
            }
        }
    }

    /**
     * Check which {@link Data} is the first
     * 
     * @param first
     *            Current first {@link Data}
     * @param data
     *            {@link Data} to check
     * @return the first {@link Data}
     */
    private Data<?> checkFirst(Data<?> first, Data<?> data) {
        return first == null || first.getSamplingTimeStart().after(data.getSamplingTimeStart()) ? data : first;
    }

    /**
     * Check which {@link Data} is the last
     * 
     * @param last
     *            Current last {@link Data}
     * @param data
     *            {@link Data} to check
     * @return the last {@link Data}
     */
    private Data<?> checkLast(Data<?> last, Data<?> data) {
        return last == null || last.getSamplingTimeEnd().before(data.getSamplingTimeEnd()) ? data : last;
    }

    /**
     * Get the {@link SmlIo} outputs from the process description
     * 
     * @return Outputs or an emtpy list
     */
    private List<SmlIo> getOutputs() {
        if (processDescription != null) {
            if (processDescription.isSetOutputs()) {
                return processDescription.getOutputs();
            } else if (processDescription.isSetComponents()
                    && processDescription.getComponents().get(processDescription.getComponents().size() - 1)
                            .isSetProcess()
                    && processDescription.getComponents().get(processDescription.getComponents().size() - 1)
                            .getProcess() instanceof AbstractProcess
                    && ((AbstractProcess) processDescription.getComponents()
                            .get(processDescription.getComponents().size() - 1).getProcess()).isSetOutputs()) {
                return ((AbstractProcess) processDescription.getComponents()
                        .get(processDescription.getComponents().size() - 1).getProcess()).getOutputs();
            }
        }
        return Collections.emptyList();
    }

    private void logSuccessfulInsertion(DataMessage message) {
        for (String s : getObservationLogStatements(message, true)) {
            LOG.info("{}", s);
        }
    }

    private void logFailedInsertion(DataMessage message) {
        for (String s : getObservationLogStatements(message, false)) {
            LOG.error("{}", s);
        }
    }
    
    @VisibleForTesting
    public List<String> getObservationLogStatements(DataMessage message, boolean persisted) {
        List<String> list = new LinkedList<>();
        if (message != null && message.getTimeseries() != null) {
            for (Timeseries<?> t : message.getTimeseries()) {
               if (t.hasMeasurements()) {
                   for (Measurement<?> m : t.getMeasurements()) {
                       ObjectNode o = nodeFactory().objectNode();
                       o.put("persisted", persisted);
                       o.put("procedure", t.getSensor());
                       o.put("phenomenon", t.getPhenomenon());
                       o.put("feature", t.getFeature().getId());
                       if (m.getPhenomenonTime() != null) {
                           o.put("phenomenontime", m.getPhenomenonTime().toString());
                       } else {
                           o.set("phenomenontime", null);
                       }
                       ObjectNode n = nodeFactory().objectNode();
                       n.set("IngestObservation", o);
                       list.add(n.toString());
                   }
               }
            }
        }
        return list;
    }
    
    protected JsonNodeFactory nodeFactory() {
        return Json.nodeFactory();
    }

}
