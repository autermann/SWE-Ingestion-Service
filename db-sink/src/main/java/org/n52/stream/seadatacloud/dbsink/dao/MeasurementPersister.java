package org.n52.stream.seadatacloud.dbsink.dao;


import java.math.BigDecimal;
import java.sql.Date;

import org.hibernate.Session;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.series.db.beans.data.Data.CountData;
import org.n52.series.db.beans.data.Data.QuantityData;
import org.n52.series.db.beans.data.Data.TextData;
import org.n52.stream.core.Measurement;
import org.n52.stream.seadatacloud.dbsink.DatabaseSinkApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasurementPersister {
    
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementPersister.class);
    
    private DatasetEntity dataset;

    private Session session;
    
    public MeasurementPersister(DatasetEntity dataset, Session session) {
        this.dataset = dataset;
        this.session = session;
    }
    
    public Data<?> persist(Measurement<?> m) {
        if (m.getValue() instanceof BigDecimal) {
            return persist((BigDecimal)m.getValue(), m);
        } else if (m.getValue() instanceof String) {
            return persist((String)m.getValue(), m);
        } else if (m.getValue() instanceof Integer) {
            return persist((Integer)m.getValue(), m);
        }
        LOG.info("Not supported value type");
        return null;
    }

    protected Data<?> persist(BigDecimal value, Measurement<?> m) {
        QuantityData data = new QuantityDataEntity();
        data.setValue(value);
        return persist(data, m);
    }

    protected Data<?> persist(String value, Measurement<?> m){
        TextData data = new TextDataEntity();
        data.setValue(value);
        return persist(data, m);
    }
    
    protected Data<?> persist(Integer value, Measurement<?> m) {
        CountData data = new CountDataEntity();
        data.setValue(value);
        return persist(data, m);
    }

    private Data<?> persist(Data<?> data, Measurement<?> m) {
        data.setDataset(dataset);
        java.util.Date date = Date.from(m.getTimestamp().toInstant());
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        session.saveOrUpdate(data);
        session.flush();
        session.refresh(data);
        return data;
    }
    
    
}
