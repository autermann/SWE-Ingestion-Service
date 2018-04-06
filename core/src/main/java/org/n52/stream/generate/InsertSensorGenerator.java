package org.n52.stream.generate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.ogc.om.features.samplingFeatures.AbstractSamplingFeature;
import org.n52.shetland.ogc.sensorML.AbstractProcess;
import org.n52.shetland.ogc.sensorML.elements.SmlIo;
import org.n52.shetland.ogc.sensorML.v20.AbstractProcessV20;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosInsertionMetadata;
import org.n52.shetland.ogc.sos.SosProcedureDescription;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweConstants;

public class InsertSensorGenerator {

    public InsertSensorRequest generate(AbstractProcess process) {
        InsertSensorRequest request = new InsertSensorRequest(Sos2Constants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedureDescription(new SosProcedureDescription<AbstractFeature>(process));
        request.setProcedureDescriptionFormat(process.getDefaultElementEncoding());
        request.setObservableProperty(getObservableProperties(process));
        request.setMetadata(getSosInsertionMetadata(process));
        return request;
    }

    private List<String> getObservableProperties(AbstractProcess process) {
        List<String> list = new LinkedList<>();
        if (process.isSetOutputs()) {
            for (SmlIo smlIo : process.getOutputs()) {
                if (smlIo.getIoValue().isSetDefinition()) {
                    list.add(smlIo.getIoValue().getDefinition());
                }
            }
        }
        return list;
    }

    private SosInsertionMetadata getSosInsertionMetadata(AbstractProcess process) {
        SosInsertionMetadata metadata = new SosInsertionMetadata();
        metadata.setFeatureOfInterestTypes(getFeatureTypes(process));
        metadata.setObservationTypes(getObservationType(process));
        return metadata;
    }

    private Collection<String> getObservationType(AbstractProcess process) {
        List<String> list = new LinkedList<>();
        if (process.isSetOutputs()) {
            for (SmlIo smlIo : process.getOutputs()) {
                list.add(getObservationTypeByOutput(smlIo.getIoValue()));
            }
        }
        return list;
    }

    private String getObservationTypeByOutput(SweAbstractDataComponent ioValue) {
        SweConstants.SweDataComponentType.values();
        switch (ioValue.getDataComponentType()) {
            case Boolean:
                return OmConstants.OBS_TYPE_TRUTH_OBSERVATION;
            case Category:
                return OmConstants.OBS_TYPE_CATEGORY_OBSERVATION;
            case Count:
                return OmConstants.OBS_TYPE_COUNT_OBSERVATION;
            case DataArray:
                return OmConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
            case DataRecord:
                return OmConstants.OBS_TYPE_COMPLEX_OBSERVATION;
            case Quantity:
                return OmConstants.OBS_TYPE_MEASUREMENT;
            case Text:
                return OmConstants.OBS_TYPE_TEXT_OBSERVATION;
            case Time:
                return OmConstants.OBS_TYPE_TEMPORAL_OBSERVATION;
            
            default:
                return OmConstants.OBS_TYPE_UNKNOWN;
        }
    }

    private Collection<String> getFeatureTypes(AbstractProcess process) {
        Set<String> set = new HashSet<>();
        if (process instanceof AbstractProcessV20 && ((AbstractProcessV20) process).isSetSmlFeatureOfInterest()) {
            for (AbstractFeature feature : ((AbstractProcessV20) process).getSmlFeatureOfInterest().getFeaturesOfInterestMap().values()) {
                if (feature != null && feature instanceof AbstractSamplingFeature) {
                    if (((AbstractSamplingFeature)feature).isSetFeatureType()) {
                        set.add(((AbstractSamplingFeature)feature).getFeatureType());
                    } else if (((AbstractSamplingFeature)feature).isSetGeometry()) {
                        set.add(getFeatureTypeByGeometry(((AbstractSamplingFeature)feature).getGeometry()));
                    }
                }
            }
        }
        if (set.isEmpty()) {
            set.add(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
        }
        return set;
    }

    private String getFeatureTypeByGeometry(Geometry geometry) {
        switch (geometry.getGeometryType()) {
            case "Point":
                return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
            case "LineString":
                return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
            case "Polygon":
                return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
            default:
                return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE;
        }
    }
    
}
