package org.n52.stream.seadatacloud.dbsink;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.stream.core.Dataset;
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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableTransactionManagement
@Transactional
@EnableBinding(Sink.class)
public class DatabaseSinkApplication {
    
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSinkApplication.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    private Map<String, AbstractProcess> descriptions = new LinkedHashMap<>();
    
    @Transactional(rollbackFor=Exception.class)
    @StreamListener(Sink.INPUT)
    public synchronized void input(Dataset dataset) {
        Session session = null;
        try {
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            DaoFactory daoFactory = new DaoFactory(session);
            session.beginTransaction();
            DatasetDao datasetDao = daoFactory.getDatasetDao();
            OfferingDao offeringDao = daoFactory.getOfferingDao();
            for (Timeseries<?> timeseries : dataset.getTimeseries()) {
                if (timeseries.getMeasurements() != null && !timeseries.getMeasurements().isEmpty()) {
                    ProcedureEntity procedure = daoFactory.getProcedureDAO().get(timeseries.getSensor());
                    if (procedure != null && descriptions.containsKey(timeseries.getSensor())) {
                        DatasetEntity datasetEntity = datasetDao.getOrInsert(timeseries, descriptions.get(timeseries.getSensor()));
                        if (datasetEntity != null) {
                            ObservationDao observationDao = daoFactory.getObservationDao();
                            Data<?> first = null;
                            Data<?> last = null;
                            for (Measurement<?> m : timeseries.getMeasurements()) {
                                Data<?> data = observationDao.persist(m, datasetEntity,
                                        observationDao.getComponent(datasetEntity.getPhenomenon().getIdentifier(),
                                                descriptions.get(timeseries.getSensor()).getOutputs()));
                                first = updateFirst(first, data);
                                last = updateLast(last, data);
                            }
                            // update dataset and offering with times and geometry
                            datasetDao.updateMetadata(datasetEntity, first, last);
                            offeringDao.updateMetadata(datasetEntity.getOffering(), first, last, null);
                        }
                    }
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            LOG.error("error", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.clear();
                session.close();
            }
        }
        LOG.info("Received processor output:\n{}", dataset);
    }

    private Data<?> updateFirst(Data<?> first, Data<?> data) {
        return  first == null || first.getSamplingTimeStart().after(data.getSamplingTimeStart()) ? data : first;
    }

    private Data<?> updateLast(Data<?> last, Data<?> data) {
        return last == null ||last.getSamplingTimeEnd().before(data.getSamplingTimeEnd()) ? data : last;
    }

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSinkApplication.class, args);
    }
}
