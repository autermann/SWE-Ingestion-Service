package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.FormatEntity;

public class FormatDao
        extends
        AbstractDao {

    public FormatDao(DaoFactory daoFactory) {
        super(daoFactory);
    }
    
    public FormatEntity getOrInsert(String format) {
        FormatEntity hFormatEntity =
                getFormatEntityObject(format);
        if (hFormatEntity == null) {
            hFormatEntity = new FormatEntity();
            hFormatEntity.setFormat(format);
            getDaoFactory().getSession().save(hFormatEntity);
            getSession().flush();
        }
        return hFormatEntity;
    }
    
    public FormatEntity getFormatEntityObject(String format) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<FormatEntity> cq = builder.createQuery(FormatEntity.class);
        Root<FormatEntity> root = cq.from(FormatEntity.class);
        cq.select(root).where(builder.equal(root.get(FormatEntity.FORMAT), format));
        Query<FormatEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }

}
