package org.n52.stream.seadatacloud.dbsink.dao;

import java.math.BigDecimal;
import java.sql.Date;

import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.series.db.beans.data.Data.BooleanData;
import org.n52.series.db.beans.data.Data.CategoryData;
import org.n52.series.db.beans.data.Data.CountData;
import org.n52.series.db.beans.data.Data.QuantityData;
import org.n52.series.db.beans.data.Data.TextData;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.simpleType.SweBoolean;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweCount;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.stream.core.Measurement;

public class ObservationDao extends AbstractDao {

    public ObservationDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public Data<?> persist(Measurement<?> m, DatasetEntity dataset, AbstractProcess process) {
        return persist(m, dataset, getComponent(dataset.getPhenomenon().getIdentifier(), process.getOutputs()));
    }

    public Data<?> persist(Measurement<?> m, DatasetEntity dataset, SweAbstractDataComponent component) {
        if (component != null) {
            if (component instanceof SweQuantity) {
                return persistQuantity(m.getValue(), m, dataset);
            } else if (component instanceof SweText) {
                return persistText(m.getValue(), m, dataset);
            } else if (component instanceof SweCategory) {
                return persistCategory(m.getValue(), m, dataset);
            } else if (component instanceof SweBoolean) {
                return persistBoolean(m.getValue(), m, dataset);
            } else if (component instanceof SweCount) {
                return persistCount(m.getValue(), m, dataset);
            }
        }
        if (m.getValue() instanceof BigDecimal) {
            return persistQuantity(m.getValue(), m, dataset);
        } else if (m.getValue() instanceof String) {
            if (dataset.hasUnit()) {
                return persistCategory(m.getValue(), m, dataset);
            }
            return persistText(m.getValue(), m, dataset);
        } else if (m.getValue() instanceof Integer) {
            return persistCount(m.getValue(), m, dataset);
        } else if (m.getValue() instanceof Boolean) {
            return persistBoolean(m.getValue(), m, dataset);
        }
//        LOG.info("Not supported value type");
        return null;
    }

    protected Data<?> persistQuantity(Object value, Measurement<?> m, DatasetEntity dataset) {
        QuantityData data = new QuantityDataEntity();
        data.setValue(new BigDecimal(value.toString()));
        return persist(data, m, dataset);
    }

    protected Data<?> persistText(Object value, Measurement<?> m, DatasetEntity dataset){
        TextData data = new TextDataEntity();
        data.setValue(value.toString());
        return persist(data, m, dataset);
    }
    protected Data<?> persistCategory(Object value, Measurement<?> m, DatasetEntity dataset){
        CategoryData data = new CategoryDataEntity();
        data.setValue(value.toString());
        return persist(data, m, dataset);
    }
    
    protected Data<?> persistCount(Object value, Measurement<?> m, DatasetEntity dataset) {
        CountData data = new CountDataEntity();
        data.setValue(Integer.parseInt(value.toString()));
        return persist(data, m, dataset);
    }
    
    protected Data<?> persistBoolean(Object value, Measurement<?> m, DatasetEntity dataset) {
        BooleanData data = new BooleanDataEntity();
        data.setValue(Boolean.valueOf(value.toString()));
        return persist(data, m, dataset);
    }

    private Data<?> persist(Data<?> data, Measurement<?> m, DatasetEntity dataset) {
        data.setDataset(dataset);
        java.util.Date date = Date.from(m.getTimestamp().toInstant());
        data.setSamplingTimeStart(date);
        data.setSamplingTimeEnd(date);
        data.setResultTime(date);
        getSession().saveOrUpdate(data);
        getSession().flush();
        getSession().refresh(data);
        return data;
    }
}