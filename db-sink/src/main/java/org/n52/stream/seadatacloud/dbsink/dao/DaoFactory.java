package org.n52.stream.seadatacloud.dbsink.dao;

import org.hibernate.Session;

public class DaoFactory {

    private Session session;

    public DaoFactory(Session session) {
        this.session =  session;
    }
    
    public Session getSession() {
        return session;
    }

    public DatasetDao getDatasetDao() {
        return new DatasetDao(this);
    }

    public ObservationDao getObservationDao() {
        return new ObservationDao(this);
    }

    public ProcedureDao getProcedureDAO() {
        return new ProcedureDao(this);
    }
    
    public PhenomenonDao getPhenomenonDao() {
        return new PhenomenonDao(this);
    }
    
    public CategoryDao getCategoryDao() {
        return new CategoryDao(this);
    }
    
    public OfferingDao getOfferingDao() {
        return new OfferingDao(this);
    }
    
    public FeatureDao getFeatureDao() {
        return new FeatureDao(this);
    }

    public FormatDao getFormatDao() {
        return new FormatDao(this);
    }

    public UnitDao getUnitDao() {
        return new UnitDao(this);
    }
}
