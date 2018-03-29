package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.CategoryEntity;

public class CategoryDao
        extends
        AbstractDao {

    public CategoryDao(DaoFactory daoFactory) {
        super(daoFactory);
    }
    
    public CategoryEntity get(String identifier) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = builder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);
        cq.select(root).where(builder.equal(root.get(CategoryEntity.IDENTIFIER), identifier));
        Query<CategoryEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }

}
