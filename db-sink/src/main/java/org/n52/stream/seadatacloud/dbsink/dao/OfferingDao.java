package org.n52.stream.seadatacloud.dbsink.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.data.Data;

public class OfferingDao
        extends
        AbstractDao {

    public OfferingDao(DaoFactory daoFactory) {
        super(daoFactory);
    }

    
    public OfferingEntity get(String identifier) {
        CriteriaBuilder builder = getDaoFactory().getSession().getCriteriaBuilder();
        CriteriaQuery<OfferingEntity> cq = builder.createQuery(OfferingEntity.class);
        Root<OfferingEntity> root = cq.from(OfferingEntity.class);
        cq.select(root).where(builder.equal(root.get(OfferingEntity.IDENTIFIER), identifier));
        Query<OfferingEntity> q = getSession().createQuery(cq);
        return q.uniqueResult();
    }
    
    public OfferingEntity updateMetadata(OfferingEntity offering, Data<?> first, Data<?> last, Object geometry) {
        if (offering.getSamplingTimeStart() == null || (offering.getSamplingTimeStart() != null
                && first.getSamplingTimeStart() != null
                && offering.getSamplingTimeStart().after(first.getSamplingTimeStart()))) {
            offering.setSamplingTimeStart(first.getSamplingTimeStart());
        }
        if (offering.getSamplingTimeEnd() == null || (offering.getSamplingTimeEnd() != null
                && last.getSamplingTimeEnd() != null
                && offering.getSamplingTimeEnd().before(last.getSamplingTimeEnd()))) {
            offering.setSamplingTimeEnd(last.getSamplingTimeEnd());
        }
        if (offering.getResultTimeStart() == null || (offering.getResultTimeStart() != null
                && first.getResultTime() != null
                && offering.getResultTimeStart().after(first.getResultTime()))) {
            offering.setResultTimeStart(first.getResultTime());
        }
        if (offering.getResultTimeEnd() == null || (offering.getResultTimeEnd() != null
                && last.getResultTime() != null
                && offering.getResultTimeEnd().before(last.getResultTime()))) {
            offering.setResultTimeEnd(last.getResultTime());
        }
//        if (offering.getValidTimeStart() == null || (offering.getValidTimeStart() != null
//                && observation.getValidTimeStart() != null
//                && offering.getValidTimeStart().after(observation.getValidTimeStart()))) {
//            offering.setValidTimeStart(observation.getValidTimeStart());
//        }
//        if (offering.getValidTimeEnd() == null || (offering.getValidTimeEnd() != null
//                && observation.getValidTimeEnd() != null
//                && offering.getValidTimeEnd().before(observation.getValidTimeEnd()))) {
//            offering.setValidTimeEnd(observation.getValidTimeEnd());
//        }
//        if (observation.isSetGeometryEntity()) {
//            if (offering.isSetGeometry()) {
//                offering.getGeometryEntity().getGeometry().union(observation.getGeometryEntity().getGeometry());
//            } else {
//                offering.setGeometryEntity(observation.getGeometryEntity());
//            }
//        } else if (observation.getDataset().isSetFeature() && observation.getDataset().getFeature().isSetGeometry()) {
//            if (offering.isSetGeometry()) {
//                offering.getGeometryEntity().getGeometry()
//                        .union(observation.getDataset().getFeature().getGeometryEntity().getGeometry());
//            } else {
//                offering.setGeometryEntity(observation.getDataset().getFeature().getGeometryEntity());
//            }
//        }
        getSession().saveOrUpdate(offering);
        return offering;
    }
}
