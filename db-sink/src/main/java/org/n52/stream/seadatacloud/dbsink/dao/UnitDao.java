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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.UoM;

/**
 * DAO implementation for {@link UnitEntity}s
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public class UnitDao
        extends
        AbstractDao {

    /**
     * constructor
     * 
     * @param daoFactory
     *            the {@link DaoFactory}
     */
    public UnitDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    /**
     * @param unit
     * @return
     */
    /**
     * Get {@link UnitEntity} for unit
     * 
     * @param unit
     *            the unit
     * @return the matching {@link UnitEntity}
     */
    public UnitEntity get(String unit) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<UnitEntity> cq = builder.createQuery(UnitEntity.class);
        Root<UnitEntity> root = cq.from(UnitEntity.class);
        cq.select(root).where(builder.equal(root.get(UnitEntity.PROPERTY_SYMBOL), unit));
        Query<UnitEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }

    /**
     * Get or insert an {@link UnitEntity}
     * 
     * @param unit
     *            the unit to get/insert
     * @return the matching {@link UnitEntity} or the new created
     */
    public UnitEntity getOrInsert(String unit) {
        UnitEntity hUnitEntity =
                get(unit);
        if (hUnitEntity == null) {
            hUnitEntity = new UnitEntity();
            hUnitEntity.setUnit(unit);
            getSession().save(hUnitEntity);
            getSession().flush();
        }
        return hUnitEntity;
    }
    
    /**
     * Get or insert an {@link UnitEntity}
     * 
     * @param unit
     *            the {@link UoM} to get/insert
     * @return the matching {@link UnitEntity} or the new created
     */
    public UnitEntity getOrInsert(UoM unit) {
        UnitEntity hUnitEntity =
                get(unit.getUom());
        if (hUnitEntity == null) {
            hUnitEntity = new UnitEntity();
            hUnitEntity.setUnit(unit.getUom());
            hUnitEntity.setName(hUnitEntity.getName());
            hUnitEntity.setLink(unit.getLink());
            getSession().save(hUnitEntity);
            getSession().flush();
        }
        return hUnitEntity;
    }
}
