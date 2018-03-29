package org.n52.stream.seadatacloud.dbsink.dao;

import java.util.List;

import org.hibernate.Session;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;

public abstract class AbstractDao {

    private DaoFactory daoFactory;

    public AbstractDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    public DaoFactory getDaoFactory( ) {
        return daoFactory;
    }
    
    public Session getSession() {
        return getDaoFactory().getSession();
    }
    
    public SweAbstractDataComponent getComponent(String phenomenon, List<SmlIo> outputs) {
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
}
