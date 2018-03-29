package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.UoM;

public class UnitDao
        extends
        AbstractDao {

    public UnitDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

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
    
    public UnitEntity get(String unit) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<UnitEntity> cq = builder.createQuery(UnitEntity.class);
        Root<UnitEntity> root = cq.from(UnitEntity.class);
        cq.select(root).where(builder.equal(root.get(UnitEntity.PROPERTY_SYMBOL), unit));
        Query<UnitEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }

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
