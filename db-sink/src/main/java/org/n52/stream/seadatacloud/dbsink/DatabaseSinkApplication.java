package org.n52.stream.seadatacloud.dbsink;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.stream.core.Dataset;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.n52.stream.seadatacloud.dbsink.dao.MeasurementPersister;
import org.n52.stream.seadatacloud.dbsink.dao.TimeseriesPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableBinding(Sink.class)
public class DatabaseSinkApplication {
    
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSinkApplication.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    @StreamListener(Sink.INPUT)
    @Transactional
    public synchronized void input(Dataset dataset) {
        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        TimeseriesPersister timeseriesPersister = new TimeseriesPersister(session);
        for (Timeseries<?> timeseries : dataset.getTimeseries()) {
            if (timeseries.getMeasurements() != null && !timeseries.getMeasurements().isEmpty()) {
                // get DatasetEntity from dataset
                DatasetEntity datasetEntity = timeseriesPersister.getOrInsert(timeseries);
                if (datasetEntity != null) {
                    MeasurementPersister persister = new MeasurementPersister(datasetEntity, session);
                    Data<?> first = null;
                    Data<?> last = null;
                    for (Measurement<?> measurement : timeseries.getMeasurements()) {
                        Data<?> data = persister.persist(measurement);
                        first = updateFirst(first, data);
                        last = updateLast(last, data);
                    }
                    // update dataset and offering with times and geometry
                    timeseriesPersister.updateSeriesWithFirstLatestValues(datasetEntity, first, last);
                    timeseriesPersister.updateOfferingMetadata(datasetEntity.getOffering(), first, last, null);
                }
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
