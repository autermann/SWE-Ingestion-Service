package org.n52.stream.seadatacloud.dbsink.dao;

import java.math.BigDecimal;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.NotInitializedDatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.QuantityDatasetEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.beans.data.Data;
import org.n52.series.db.beans.dataset.NotInitializedDataset;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;
import org.n52.stream.seadatacloud.dbsink.DatabaseSinkApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeseriesPersister {
    
    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersister.class);    
    
    private Session session;
    
    public TimeseriesPersister(Session session) {
        this.session = session;
    }

    public DatasetEntity getOrInsert(Timeseries<?> timeseries) {
        Criteria criteria = getDefaultAllSeriesCriteria(session);
        addIdentifierRestrictionsToCritera(criteria, timeseries);
        criteria.setMaxResults(1);
        DatasetEntity series = (DatasetEntity) criteria.uniqueResult();
        if (series == null || series instanceof NotInitializedDataset) {
            series = preCheckDataset(timeseries, series, session);
        }
        if (series == null || (series.isSetFeature() && !series.getFeature().getIdentifier().equals(timeseries.getFeature().getId()))) {
            series = getDatesetEntity(timeseries);
            addValuesToSeries(series, timeseries);
            series.setDeleted(false);
            series.setPublished(true);
        } else if (!series.isSetFeature()) {
            addValuesToSeries(series, timeseries);
            series.setDeleted(false);
            series.setPublished(true);
        } else if (!series.isPublished()) {
            series.setPublished(true);
        } else if (series.isDeleted()) {
            series.setDeleted(false);
        } else {
            return series;
        }
        session.saveOrUpdate(series);
        session.flush();
        return series;
    }
    
    public void updateSeriesWithFirstLatestValues(DatasetEntity series, Data<?> first, Data<?> last) {
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
        session.saveOrUpdate(series);
        session.flush();
    }
    
    public OfferingEntity updateOfferingMetadata(OfferingEntity offering, Data<?> first, Data<?> last, Object geometry) {
        if (offering.getSamplingTimeStart() == null || (offering.getSamplingTimeStart() != null
                && first.getSamplingTimeStart() != null
                && offering.getSamplingTimeStart().after(first.getSamplingTimeStart()))) {
            offering.setSamplingTimeStart(first.getSamplingTimeStart());
        }
        if (offering.getSamplingTimeEnd() == null || (offering.getSamplingTimeEnd() != null
                && last.getSamplingTimeEnd() != null
                && offering.getSamplingTimeEnd().before(last.getSamplingTimeEnd()))) {
            offering.setSamplingTimeEnd(last.getSamplingTimeEnd());
        }
        if (offering.getResultTimeStart() == null || (offering.getResultTimeStart() != null
                && first.getResultTime() != null
                && offering.getResultTimeStart().after(first.getResultTime()))) {
            offering.setResultTimeStart(first.getResultTime());
        }
        if (offering.getResultTimeEnd() == null || (offering.getResultTimeEnd() != null
                && last.getResultTime() != null
                && offering.getResultTimeEnd().before(last.getResultTime()))) {
            offering.setResultTimeEnd(last.getResultTime());
        }
//        if (offering.getValidTimeStart() == null || (offering.getValidTimeStart() != null
//                && observation.getValidTimeStart() != null
//                && offering.getValidTimeStart().after(observation.getValidTimeStart()))) {
//            offering.setValidTimeStart(observation.getValidTimeStart());
//        }
//        if (offering.getValidTimeEnd() == null || (offering.getValidTimeEnd() != null
//                && observation.getValidTimeEnd() != null
//                && offering.getValidTimeEnd().before(observation.getValidTimeEnd()))) {
//            offering.setValidTimeEnd(observation.getValidTimeEnd());
//        }
//        if (observation.isSetGeometryEntity()) {
//            if (offering.isSetGeometry()) {
//                offering.getGeometryEntity().getGeometry().union(observation.getGeometryEntity().getGeometry());
//            } else {
//                offering.setGeometryEntity(observation.getGeometryEntity());
//            }
//        } else if (observation.getDataset().isSetFeature() && observation.getDataset().getFeature().isSetGeometry()) {
//            if (offering.isSetGeometry()) {
//                offering.getGeometryEntity().getGeometry()
//                        .union(observation.getDataset().getFeature().getGeometryEntity().getGeometry());
//            } else {
//                offering.setGeometryEntity(observation.getDataset().getFeature().getGeometryEntity());
//            }
//        }
        session.saveOrUpdate(offering);
        return offering;
    }

    private Criteria getDefaultAllSeriesCriteria(Session session) {
        return session.createCriteria(DatasetEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }
    
    private Criteria getDefaultNotDefinedDatasetCriteria(Session session) {
        return session.createCriteria(NotInitializedDatasetEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }
    
    private DatasetEntity preCheckDataset(Timeseries<?> t, DatasetEntity dataset, Session session) {
        if (dataset == null) {
            Criteria criteria = getDefaultNotDefinedDatasetCriteria(session);
            addIdentifierRestrictionsToCritera(criteria, t, false);
            dataset = (DatasetEntity) criteria.uniqueResult();
        }
        if (dataset != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("update ")
                .append(DatasetEntity.class.getSimpleName())
                .append(" set valueType = :valueType")
                .append(" where id = :id");
            session.createQuery(builder.toString())
                .setParameter( "valueType", getValueType(t))
                .setParameter( "id", dataset.getId())
                .executeUpdate();
            session.flush();
        }
        return dataset;
    }
    
     
    private String getValueType(Timeseries<?> t) {
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return QuantityDatasetEntity.DATASET_TYPE;
        } else if (m.getValue() instanceof String) {
            return TextDatasetEntity.DATASET_TYPE;
        } else if (m.getValue() instanceof Integer) {
            return "count";
//            return CountDatasetEntity.DATASET_TYPE;
        }
        LOG.info("Not supported value type");
        return DatasetEntity.DEFAULT_VALUE_TYPE;
    }
    
    private DatasetEntity getDatesetEntity(Timeseries<?> t) {
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return new QuantityDatasetEntity();
        } else if (m.getValue() instanceof String) {
            return new TextDatasetEntity();
        } else if (m.getValue() instanceof Integer) {
            return new CountDatasetEntity();
        }
        LOG.info("Not supported value type");
        return new NotInitializedDatasetEntity();
    }

    private void addValuesToSeries(DatasetEntity series, Timeseries<?> timeseries) {
        // TODO Auto-generated method stub
        
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
    
    private ProcedureEntity getProcedure(final String identifier, final Session session) {
        Criteria criteria =
                session.createCriteria(ProcedureEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.eq(ProcedureEntity.IDENTIFIER, identifier));
        return (ProcedureEntity) criteria.uniqueResult();
    }

    private PhenomenonEntity getPhenomenon(final String identifier, final Session session) {
        Criteria criteria =
                session.createCriteria(PhenomenonEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.eq(PhenomenonEntity.IDENTIFIER, identifier));
        return (PhenomenonEntity) criteria.uniqueResult();
    }

    private CategoryEntity getCategory(final String identifier, final Session session) {
        Criteria criteria =
                session.createCriteria(CategoryEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.eq(CategoryEntity.IDENTIFIER, identifier));
        return (CategoryEntity) criteria.uniqueResult();
    }

    private OfferingEntity getOffering(final String identifier, final Session session) {
        Criteria criteria =
                session.createCriteria(OfferingEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.eq(OfferingEntity.IDENTIFIER, identifier));
        return (OfferingEntity) criteria.uniqueResult();
    }
    
    private AbstractFeatureEntity getOrInsert(Feature f, String url, Session session) {
        AbstractFeatureEntity feature = get(f.getId(), session);
        if (feature == null) {
            feature = new FeatureEntity();
            feature.setIdentifier(f.getId());
            if (url != null && !url.isEmpty()) {
                feature.setUrl(url);
            }
            FormatEntity type = getOrInsertFormatEntity("http://www.opengis.net/def/nil/OGC/0/unknown", session);
            feature.setFeatureType(type);
//            if (f.getGeometry() != null) {
//                feature.setGeometry(f.getGeometry());
//            }
            session.save(feature);
        } else if (feature.getUrl() != null && !feature.getUrl().isEmpty() && url != null && !url.isEmpty()) {
            feature.setUrl(url);
            session.saveOrUpdate(feature);
        }
        // don't flush here because we may be batching
        return feature;
    }
    
    private AbstractFeatureEntity get(String identifier, Session session) {
        Criteria criteria = session.createCriteria(AbstractFeatureEntity.class)
                .add(Restrictions.eq(AbstractFeatureEntity.IDENTIFIER, identifier));
        return (AbstractFeatureEntity) criteria.uniqueResult();
    }
    
    private FormatEntity getOrInsertFormatEntity(String format,
            Session session) {
        FormatEntity hFormatEntity =
                getFormatEntityObject(format, session);
        if (hFormatEntity == null) {
            hFormatEntity = new FormatEntity();
            hFormatEntity.setFormat(format);
            session.save(hFormatEntity);
            session.flush();
        }
        return hFormatEntity;
    }
    
    private FormatEntity getFormatEntityObject(String format,
            Session session) {
        Criteria criteria =
                session.createCriteria(FormatEntity.class).add(
                        Restrictions.eq(FormatEntity.FORMAT,
                                format));
        return (FormatEntity) criteria.uniqueResult();
    }

}
