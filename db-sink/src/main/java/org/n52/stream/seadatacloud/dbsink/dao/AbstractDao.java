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

import java.util.List;

import org.hibernate.Session;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;

/**
 * Abstract DAO class.
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public abstract class AbstractDao {

    private DaoFactory daoFactory;

    /**
     * constructor
     * 
     * @param daoFactory
     *            the {@link DaoFactory}
     */
    public AbstractDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    /**
     * Get the {@link DaoFactory}
     * 
     * @return the {@link DaoFactory}
     */
    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    /**
     * Get the {@link Session}
     * 
     * @return the {@link Session}
     */
    public Session getSession() {
        return getDaoFactory().getSession();
    }

    /**
     * Get the output for the phenomenon
     * 
     * @param phenomenon
     *            the phenomenon to get the output for
     * @param outputs
     *            the outputs to check
     * @return the matching output or null if not found
     */
    protected SweAbstractDataComponent getOutput(String phenomenon, List<SmlIo> outputs) {
        if (outputs != null) {
            for (SmlIo smlIo : outputs) {
                if (smlIo.getIoValue() != null && smlIo.getIoValue().isSetDefinition()
                        && phenomenon.equals(smlIo.getIoValue().getDefinition())) {
                    return smlIo.getIoValue();
                }
            }
        }
        return null;
    }

    /**
     * check if phenomenon is of type {@link SweQuantity}
     * 
     * @param phenomenon
     *            the phenomenon to check
     * @param outputs
     *            the outputs to check
     * @return <code>true</code> if phenomenon is of type {@link SweQuantity}
     */
    protected boolean checkForQuantity(String phenomenon, List<SmlIo> outputs) {
        SweAbstractDataComponent component = getOutput(phenomenon, outputs);
        return component != null && component instanceof SweQuantity;
    }

    /**
     * Get the offering identifier from definition or create from sensor id.
     * 
     * @param sensorId
     *            the sensor id
     * @param offering
     *            the offering definition
     * @return offering identifier
     */
    protected String getOfferingIdentifier(String sensorId, String offering) {
        if (offering != null && !offering.isEmpty()) {
            return offering;
        }
        return sensorId + "/observations";
    }
}
