package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.ProcedureEntity;

public class ProcedureDao extends AbstractDao {

    public ProcedureDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public ProcedureEntity get(String identifier) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<ProcedureEntity> cq = builder.createQuery(ProcedureEntity.class);
        Root<ProcedureEntity> root = cq.from(ProcedureEntity.class);
        cq.select(root).where(builder.equal(root.get(ProcedureEntity.IDENTIFIER), identifier));
        Query<ProcedureEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
//        Criteria criteria =
//                session.createCriteria(ProcedureEntity.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
//                        .add(Restrictions.eq(ProcedureEntity.IDENTIFIER, identifier));
//        return (ProcedureEntity) criteria.uniqueResult();
    }

}
