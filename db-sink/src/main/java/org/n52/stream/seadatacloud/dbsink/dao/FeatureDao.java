package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.stream.core.Feature;

public class FeatureDao
        extends
        AbstractDao {

    public FeatureDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public AbstractFeatureEntity getOrInsert(Feature f) {
        AbstractFeatureEntity feature = get(f.getId());
        if (feature == null) {
            feature = new FeatureEntity();
            feature.setIdentifier(f.getId());

            FormatEntity type = getDaoFactory().getFormatDao().getOrInsert("http://www.opengis.net/def/nil/OGC/0/unknown");
            feature.setFeatureType(type);
//            if (f.getGeometry() != null) {
//                feature.setGeometry(f.getGeometry());
//            }
            getSession().save(feature);
        }
        // don't flush here because we may be batching
        return feature;
    }
    
    public AbstractFeatureEntity get(String identifier) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<AbstractFeatureEntity> cq = builder.createQuery(AbstractFeatureEntity.class);
        Root<AbstractFeatureEntity> root = cq.from(AbstractFeatureEntity.class);
        cq.select(root).where(builder.equal(root.get(AbstractFeatureEntity.IDENTIFIER), identifier));
        Query<AbstractFeatureEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }
}
