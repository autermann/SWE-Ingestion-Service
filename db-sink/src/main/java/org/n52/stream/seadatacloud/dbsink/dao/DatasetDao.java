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
package org.n52.stream.seadatacloud.dbsink.dao;

import java.math.BigDecimal;
import java.util.List;

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
import org.n52.series.db.beans.dataset.BooleanDataset;
import org.n52.series.db.beans.dataset.CategoryDataset;
import org.n52.series.db.beans.dataset.CountDataset;
import org.n52.series.db.beans.dataset.NotInitializedDataset;
import org.n52.series.db.beans.dataset.QuantityDataset;
import org.n52.series.db.beans.dataset.TextDataset;
import org.n52.shetland.ogc.UoM;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.simpleType.SweAbstractUomType;
import org.n52.stream.core.Feature;
import org.n52.stream.core.Measurement;
import org.n52.stream.core.Timeseries;

/**
 * DAO implementation for {@link DatasetEntity}s
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public class DatasetDao
        extends
        AbstractDao {

    /**
     * constructor
     * 
     * @param daoFactory
     *            the {@link DaoFactory}
     */
    public DatasetDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    /**
     * Get, update or insert a {@link DatasetEntity} for {@link Timeseries}
     * 
     * @param timeseries
     *            the {@link Timeseries} to add/get
     * @param outputs
     *            the output information
     * @param offering
     *            the offering identifier
     * @return the matching {@link DatasetEntity} or the new created
     */
    public DatasetEntity getOrInsert(Timeseries<?> timeseries, List<SmlIo> outputs, String offering) {
        Criteria criteria = getDefaultAllDatasetCriteria();
        addIdentifierRestrictionsToCritera(criteria, timeseries, offering);
        criteria.setMaxResults(1);
        DatasetEntity series = (DatasetEntity) criteria.uniqueResult();
        if (series == null || series instanceof NotInitializedDataset) {
            series = preCheck(timeseries, outputs, series, offering);
        }
        if (series == null || series.isSetFeature()
                && !series.getFeature().getIdentifier().equals(timeseries.getFeature().getId())) {
            series = get(timeseries, outputs);
            addValues(series, timeseries, outputs, offering);
            series.setDeleted(false);
            series.setPublished(true);
        } else if (!series.isSetFeature()) {
            addValues(series, timeseries, outputs, offering);
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

    /**
     * Update {@link DatasetEntity} with first/last {@link Data}
     * 
     * @param dataset
     *            the {@link DatasetEntity} to update
     * @param first
     *            the first {@link Data}
     * @param last
     *            the last {@link Data}
     */
    public void updateMetadata(DatasetEntity dataset, Data<?> first, Data<?> last) {
        boolean minChanged = false;
        boolean maxChanged = false;
        if (!dataset.isSetFirstValueAt()
                || dataset.isSetFirstValueAt() && dataset.getFirstValueAt().after(first.getSamplingTimeStart())) {
            minChanged = true;
            dataset.setFirstValueAt(first.getSamplingTimeStart());
            dataset.setFirstObservation(first);
        }
        if (!dataset.isSetLastValueAt()
                || dataset.isSetLastValueAt() && dataset.getLastValueAt().before(last.getSamplingTimeEnd())) {
            maxChanged = true;
            dataset.setLastValueAt(last.getSamplingTimeEnd());
            dataset.setLastObservation(last);
        }
        if (first instanceof QuantityDataEntity && minChanged) {
            dataset.setFirstQuantityValue(((QuantityDataEntity) first).getValue());
        }
        if (last instanceof QuantityDataEntity && maxChanged) {
            dataset.setLastQuantityValue(((QuantityDataEntity) last).getValue());
        }
        getSession().saveOrUpdate(dataset);
        getSession().flush();
    }

    /**
     * Get {@link DatasetEntity} for {@link Timeseries}
     * 
     * @param t
     *            the {@link Timeseries} to get {@link DatasetEntity} for
     * @param outputs
     *            the outputs
     * @return the matching {@link DatasetEntity}
     */
    private DatasetEntity get(Timeseries<?> t, List<SmlIo> outputs) {
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return new QuantityDatasetEntity();
        } else if (m.getValue() instanceof String) {
            if (t.hasUnit()) {
                return new CategoryDatasetEntity();
            }
            return new TextDatasetEntity();
        } else if (m.getValue() instanceof Integer) {
            if (t.hasUnit() && checkForQuantity(t.getPhenomenon(), outputs)) {
                return new QuantityDatasetEntity();
            }
            return new CountDatasetEntity();
        } else if (m.getValue() instanceof Boolean) {
            return new BooleanDatasetEntity();
        }
        return new NotInitializedDatasetEntity();
    }

    /**
     * Pre-check {@link DatasetEntity} if the type is set or update
     * 
     * @param t
     *            the {@link Timeseries}
     * @param outputs
     *            the outputs
     * @param dataset
     *            the {@link DatasetEntity} to check
     * @param offering
     *            the offering identifier
     * @return the checked and updated {@link DatasetEntity}
     */
    private DatasetEntity preCheck(Timeseries<?> t, List<SmlIo> outputs, DatasetEntity dataset, String offering) {
        if (dataset == null) {
            Criteria criteria = getDefaultNotDefinedDatasetCriteria();
            addIdentifierRestrictionsToCritera(criteria, t, offering, false);
            dataset = (DatasetEntity) criteria.uniqueResult();
        }
        if (dataset != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("update ").append(DatasetEntity.class.getSimpleName()).append(" set valueType = :valueType")
                    .append(" where id = :id");
            getSession().createQuery(builder.toString()).setParameter("valueType", getValueType(t, outputs))
                    .setParameter("id", dataset.getId()).executeUpdate();
            getSession().flush();
        }
        return dataset;
    }

    /**
     * Get the value type for {@link Measurement}
     * 
     * @param t
     *            the {@link Timeseries}
     * @param outputs
     *            the outputs
     * @return the value type
     */
    private String getValueType(Timeseries<?> t, List<SmlIo> outputs) {
        Measurement<?> m = t.getMeasurements().iterator().next();
        if (m.getValue() instanceof BigDecimal) {
            return QuantityDataset.DATASET_TYPE;
        } else if (m.getValue() instanceof String) {
            if (t.hasUnit()) {
                return CategoryDataset.DATASET_TYPE;
            }
            return TextDataset.DATASET_TYPE;
        } else if (m.getValue() instanceof Integer) {
            if (t.hasUnit() && checkForQuantity(t.getPhenomenon(), outputs)) {
                return QuantityDataset.DATASET_TYPE;
            }
            return CountDataset.DATASET_TYPE;
        } else if (m.getValue() instanceof Boolean) {
            return BooleanDataset.DATASET_TYPE;
        }
        return DatasetEntity.DEFAULT_VALUE_TYPE;
    }

    /**
     * Add values from {@link Timeseries} to {@link DatasetEntity}
     * 
     * @param dataset
     *            the {@link DatasetEntity} to add values
     * @param series
     *            the {@link Timeseries}
     * @param outputs
     *            the outputs
     * @param offering
     *            the offering identifier
     */
    private void addValues(DatasetEntity dataset, Timeseries<?> series, List<SmlIo> outputs, String offering) {
        if (dataset.getProcedure() == null) {
            dataset.setProcedure(getProcedure(series.getSensor()));
        }
        if (dataset.getOffering() == null) {
            dataset.setOffering(getOffering(getOfferingIdentifier(series.getSensor(), offering)));
        }
        if (dataset.getPhenomenon() == null) {
            dataset.setPhenomenon(getPhenomenon(series.getPhenomenon()));
        }
        if (dataset.getCategory() == null) {
            dataset.setCategory(getCategory(series.getPhenomenon()));
        }
        if (dataset.getFeature() == null) {
            dataset.setFeature(getOrInsertFeature(series.getFeature()));
        }
        if (series.getUnit() != null && !series.getUnit().isEmpty() && dataset.getUnit() == null) {
            dataset.setUnit(getOrInsertUnit(series.getUnit()));
        } else {
            SweAbstractDataComponent component = getOutput(series.getPhenomenon(), outputs);
            if (component != null && component instanceof SweAbstractUomType
                    && ((SweAbstractUomType<?>) component).isSetUom()) {
                dataset.setUnit(getOrInsertUnit(((SweAbstractUomType<?>) component).getUomObject()));
            }
        }
    }

    /**
     * Get or insert {@link UnitEntity}
     * 
     * @param unit
     *            the {@link UoM} to get
     * @return the matching or inserted {@link UnitEntity}
     */
    private UnitEntity getOrInsertUnit(UoM unit) {
        return getDaoFactory().getUnitDao().getOrInsert(unit);
    }

    /**
     * Get or insert {@link UnitEntity}
     * 
     * @param unit
     *            the unit to get
     * @return the matching or inserted {@link UnitEntity}
     */
    private UnitEntity getOrInsertUnit(String unit) {
        return getDaoFactory().getUnitDao().getOrInsert(unit);
    }

    /**
     * Get or insert {@link AbstractFeatureEntity}
     * 
     * @param feature
     *            the {@link Feature} to get {@link AbstractFeatureEntity} for
     * @return the matching or inserted {@link AbstractFeatureEntity}
     */
    private AbstractFeatureEntity<?> getOrInsertFeature(Feature feature) {
        return getDaoFactory().getFeatureDao().getOrInsert(feature);
    }

    /**
     * Get or insert {@link CategoryEntity}
     * 
     * @param identifier
     *            the identifier to get {@link CategoryEntity} for
     * @return the matching or inserted {@link CategoryEntity}
     */
    private CategoryEntity getCategory(String identifier) {
        return getDaoFactory().getCategoryDao().get(identifier);
    }

    /**
     * Get or insert {@link PhenomenonEntity}
     * 
     * @param identifier
     *            the identifier to get {@link PhenomenonEntity} for
     * @return the matching or inserted {@link PhenomenonEntity}
     */
    private PhenomenonEntity getPhenomenon(String identifier) {
        return getDaoFactory().getPhenomenonDao().get(identifier);
    }

    /**
     * Get or insert {@link OfferingEntity}
     * 
     * @param identifier
     *            the identifier to get {@link OfferingEntity} for
     * @return the matching or inserted {@link OfferingEntity}
     */
    private OfferingEntity getOffering(String identifier) {
        return getDaoFactory().getOfferingDao().get(identifier);
    }

    /**
     * Get or insert {@link ProcedureEntity}
     * 
     * @param identifier
     *            the identifier to get {@link ProcedureEntity} for
     * @return the matching or inserted {@link ProcedureEntity}
     */
    private ProcedureEntity getProcedure(String identifier) {
        return getDaoFactory().getProcedureDAO().get(identifier);
    }

    /**
     * Add identifier {@link Restrictions} to {@link Criteria}
     * 
     * @param c
     *            the {@link Criteria}
     * @param t
     *            the {@link Timeseries} with the identifier
     * @param offering
     *            the offering identifier
     */
    private void addIdentifierRestrictionsToCritera(Criteria c, Timeseries<?> t, String offering) {
        addIdentifierRestrictionsToCritera(c, t, offering, true);
    }

    /**
     * Add identifier {@link Restrictions} to {@link Criteria}
     * 
     * @param c
     *            the {@link Criteria}
     * @param t
     *            the {@link Timeseries} with the identifier
     * @param offering
     *            the offering identifier
     * @param includeFeature
     *            if the feature identifier {@link Restrictions} should be added
     */
    private void addIdentifierRestrictionsToCritera(Criteria c, Timeseries<?> t, String offering,
            boolean includeFeature) {
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
        c.createCriteria(DatasetEntity.PROPERTY_OFFERING).add(
                Restrictions.eq(OfferingEntity.PROPERTY_IDENTIFIER, getOfferingIdentifier(t.getSensor(), offering)));
        c.createCriteria(DatasetEntity.PROPERTY_CATEGORY)
                .add(Restrictions.eq(CategoryEntity.PROPERTY_IDENTIFIER, t.getPhenomenon()));
    }

    /**
     * Get default {@link Criteria} for all {@link DatasetEntity}s
     * 
     * @return the {@link Criteria}
     */
    private Criteria getDefaultAllDatasetCriteria() {
        return getSession().createCriteria(DatasetEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Get default {@link Criteria} for all {@link NotInitializedDatasetEntity}s
     * 
     * @return the {@link Criteria}
     */
    private Criteria getDefaultNotDefinedDatasetCriteria() {
        return getSession().createCriteria(NotInitializedDatasetEntity.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

}
