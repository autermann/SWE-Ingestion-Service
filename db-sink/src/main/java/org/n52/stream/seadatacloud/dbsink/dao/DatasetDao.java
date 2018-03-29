package org.n52.stream.seadatacloud.dbsink.dao;

import java.math.BigDecimal;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.BooleanDatasetEntity;
import org.n52.series.db.beans.CategoryDatasetEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.NotInitializedDatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.QuantityDatasetEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.series.db.beans.dataset.NotInitializedDataset;
import org.n52.shetland.ogc.UoM;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.simpleType.SweAbstractUomType;
import org.n52.shetland.ogc.swe.simpleType.SweBoolean;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweCount;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;

public class DatasetDao extends AbstractDao {

    public DatasetDao(DaoFactory daoFactory) {
        super(daoFactory);
    }
    
    public DatasetEntity getOrInsert(Timeseries<?> timeseries, AbstractProcess process) {
        Criteria criteria = getDefaultAllSeriesCriteria();
        addIdentifierRestrictionsToCritera(criteria, timeseries);
        criteria.setMaxResults(1);
        DatasetEntity series = (DatasetEntity) criteria.uniqueResult();
        if (series == null || series instanceof NotInitializedDataset) {
            series = preCheck(timeseries, process, series);
        }
        if (series == null || (series.isSetFeature() && !series.getFeature().getIdentifier().equals(timeseries.getFeature().getId()))) {
            series = get(timeseries);
            addValuesToSeries(series, timeseries, process);
            series.setDeleted(false);
            series.setPublished(true);
        } else if (!series.isSetFeature()) {
            addValuesToSeries(series, timeseries, process);
            series.setDeleted(false);
            series.setPublished(true);
        } else if (!series.isPublished()) {
            series.setPublished(true);
        } else if (series.isDeleted()) {
            series.setDeleted(false);
        } else {
            return series;
        }
        getSession().saveOrUpdate(series);
        getSession().flush();
        return series;
    }
    
    public void updateMetadata(DatasetEntity series, Data<?> first, Data<?> last) {
        boolean minChanged = false;
        boolean maxChanged = false;
        if (!series.isSetFirstValueAt() || (series.isSetFirstValueAt()
                && series.getFirstValueAt().after(first.getSamplingTimeStart()))) {
            minChanged = true;
            series.setFirstValueAt(first.getSamplingTimeStart());
            series.setFirstObservation(first);
        }
        if (!series.isSetLastValueAt() || (series.isSetLastValueAt()
                && series.getLastValueAt().before(last.getSamplingTimeEnd()))) {
            maxChanged = true;
            series.setLastValueAt(last.getSamplingTimeEnd());
            series.setLastObservation(last);
        }
        if (first instanceof QuantityDataEntity && minChanged) {
                series.setFirstQuantityValue(((QuantityDataEntity) first).getValue());
            }
        if (last instanceof QuantityDataEntity && maxChanged) {
            series.setLastQuantityValue(((QuantityDataEntity) last).getValue());
        }
        getSession().saveOrUpdate(series);
        getSession().flush();
    }

    private DatasetEntity get(Timeseries<?> t) {
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return new QuantityDatasetEntity();
        } else if (m.getValue() instanceof String) {
            return new TextDatasetEntity();
        } else if (m.getValue() instanceof Integer) {
            return new CountDatasetEntity();
        }
//        LOG.info("Not supported value type");
        return new NotInitializedDatasetEntity();
    }
    
    private DatasetEntity preCheck(Timeseries<?> t, AbstractProcess process, DatasetEntity dataset) {
        if (dataset == null) {
            Criteria criteria = getDefaultNotDefinedDatasetCriteria();
            addIdentifierRestrictionsToCritera(criteria, t, false);
            dataset = (DatasetEntity) criteria.uniqueResult();
        }
        if (dataset != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("update ")
                .append(DatasetEntity.class.getSimpleName())
                .append(" set valueType = :valueType")
                .append(" where id = :id");
            getSession().createQuery(builder.toString())
                .setParameter( "valueType", getValueType(t, process))
                .setParameter( "id", dataset.getId())
                .executeUpdate();
            getSession().flush();
        }
        return dataset;
    }
    
    private String getValueType(Timeseries<?> t, AbstractProcess process) {
        SweAbstractDataComponent component = getComponent(t.getPhenomenon(), process.getOutputs());
        if (component != null) {
            if (component instanceof SweQuantity) {
                return QuantityDatasetEntity.DATASET_TYPE;
            } else if (component instanceof SweText) {
                return TextDatasetEntity.DATASET_TYPE;
            } else if (component instanceof SweCategory) {
                return CategoryDatasetEntity.DATASET_TYPE;
            } else if (component instanceof SweBoolean) {
                return BooleanDatasetEntity.DATASET_TYPE;
            } else if (component instanceof SweCount) {
                return "count";
//                return CountDatasetEntity.DATASET_TYPE;
            }
        }
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return QuantityDatasetEntity.DATASET_TYPE;
        } else if (m.getValue() instanceof String) {
            return TextDatasetEntity.DATASET_TYPE;
        } else if (m.getValue() instanceof Integer) {
            return "count";
//            return CountDatasetEntity.DATASET_TYPE;
        }
//        LOG.info("Not supported value type");
        return DatasetEntity.DEFAULT_VALUE_TYPE;
    }
    
    private void addValuesToSeries(DatasetEntity series, Timeseries<?> timeseries, AbstractProcess process) {
        if (series.getProcedure() == null) {
            series.setProcedure(getProcedure(timeseries.getSensor()));
        }
        if (series.getOffering() == null) {
            series.setOffering(getOffering(timeseries.getSensor()));
        }
        if (series.getPhenomenon() == null) {
            series.setPhenomenon(getPhenomenon(timeseries.getSensor()));
        }
        if (series.getCategory() == null) {
            series.setCategory(getCategory(timeseries.getSensor()));
        }
        if (series.getFeature() == null) {
            series.setFeature(getOrInsertFeature(timeseries.getFeature()));
        }
        if (timeseries.getUnit() != null && !timeseries.getUnit().isEmpty() && series.getUnit() == null) {
            series.setUnit(getOrInsertUnit(timeseries.getUnit()));
        } else {
            SweAbstractDataComponent component = getComponent(timeseries.getPhenomenon(), process.getOutputs());
            if (component != null && component instanceof SweAbstractUomType && ((SweAbstractUomType)component).isSetUom()) {
                series.setUnit(getOrInsertUnit(((SweAbstractUomType)component).getUomObject()));
            }
        }
    }

    private UnitEntity getOrInsertUnit(UoM unit) {
        return getDaoFactory().getUnitDao().getOrInsert(unit);
    }

    private UnitEntity getOrInsertUnit(String unit) {
        return getDaoFactory().getUnitDao().getOrInsert(unit);
    }


    private AbstractFeatureEntity getOrInsertFeature(Feature feature) {
        return getDaoFactory().getFeatureDao().getOrInsert(feature);
    }

    private CategoryEntity getCategory(String identifier) {
        return getDaoFactory().getCategoryDao().get(identifier);
    }

    private PhenomenonEntity getPhenomenon(String identifier) {
        return getDaoFactory().getPhenomenonDao().get(identifier);
    }

    private OfferingEntity getOffering(String identifier) {
        return getDaoFactory().getOfferingDao().get(identifier);
    }

    private ProcedureEntity getProcedure(String identifier) {
        return getDaoFactory().getProcedureDAO().get(identifier);
    }
    
    private void addIdentifierRestrictionsToCritera(Criteria c, Timeseries<?> t) {
        addIdentifierRestrictionsToCritera(c, t, true);
    }

    private void addIdentifierRestrictionsToCritera(Criteria c, Timeseries<?> t, boolean includeFeature) {
        if (includeFeature) {
            c.createCriteria(DatasetEntity.PROPERTY_FEATURE)
                    .add(Restrictions.eq(FeatureEntity.PROPERTY_IDENTIFIER, t.getFeature().getId()));
        } else {
            c.add(Restrictions.isNull(DatasetEntity.PROPERTY_FEATURE));
        }
        c.createCriteria(DatasetEntity.PROPERTY_PHENOMENON)
                .add(Restrictions.eq(PhenomenonEntity.PROPERTY_IDENTIFIER, t.getPhenomenon()));
        c.createCriteria(DatasetEntity.PROPERTY_PROCEDURE)
                .add(Restrictions.eq(ProcedureEntity.PROPERTY_IDENTIFIER, t.getSensor()));
        c.createCriteria(DatasetEntity.PROPERTY_OFFERING)
                .add(Restrictions.eq(OfferingEntity.PROPERTY_IDENTIFIER, t.getSensor()));
        c.createCriteria(DatasetEntity.PROPERTY_CATEGORY)
                .add(Restrictions.eq(CategoryEntity.PROPERTY_IDENTIFIER, t.getPhenomenon()));
    }
    
    private Criteria getDefaultAllSeriesCriteria() {
        return getSession().createCriteria(DatasetEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }
    
    private Criteria getDefaultNotDefinedDatasetCriteria() {
        return getSession().createCriteria(NotInitializedDatasetEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }
}
