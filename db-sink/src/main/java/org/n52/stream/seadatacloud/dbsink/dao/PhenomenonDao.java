package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.PhenomenonEntity;

public class PhenomenonDao
        extends
        AbstractDao {

    public PhenomenonDao(DaoFactory daoFactory) {
        super(daoFactory);
    }
    
    public PhenomenonEntity get(String identifier) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<PhenomenonEntity> cq = builder.createQuery(PhenomenonEntity.class);
        Root<PhenomenonEntity> root = cq.from(PhenomenonEntity.class);
        cq.select(root).where(builder.equal(root.get(PhenomenonEntity.IDENTIFIER), identifier));
        Query<PhenomenonEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }

}
