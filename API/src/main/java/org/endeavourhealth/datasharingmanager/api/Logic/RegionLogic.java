package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.RegionEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonRegion;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataSharingAgreementCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.OrganisationCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.RegionCache;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegionLogic {

    public Response getRegion(String uuid, String searchData) throws Exception {

        if (uuid == null && searchData == null) {
            return getRegionList();
        } else if (uuid != null){
            return getSingleRegion(uuid);
        } else {
            return search(searchData);
        }
    }

    public Response postRegion(JsonRegion region) throws Exception {

        if (region.getUuid() != null) {
            new RegionDAL().updateRegion(region);
            new MasterMappingDAL().deleteAllMappings(region.getUuid());
        } else {
            region.setUuid(UUID.randomUUID().toString());
            new RegionDAL().saveRegion(region);
        }

        //Process Mappings
        new MasterMappingDAL().saveRegionMappings(region);

        return Response
                .ok()
                .entity(region.getUuid())
                .build();
    }

    private Response getRegionList() throws Exception {

        List<RegionEntity> regions = RegionCache.getAllRegions();

        return Response
                .ok()
                .entity(regions)
                .build();
    }

    private Response getSingleRegion(String uuid) throws Exception {
        RegionEntity regionEntity = RegionCache.getRegionDetails(uuid);

        return Response
                .ok()
                .entity(regionEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<RegionEntity> regions = new RegionDAL().search(searchData);

        return Response
                .ok()
                .entity(regions)
                .build();
    }

    public Response getRegionOrganisations(String regionUUID) throws Exception {

        List<String> organisationUuids = new SecurityMasterMappingDAL().getChildMappings(regionUUID, MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(organisationUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getParentRegions(String regionUuid) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getParentMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = new RegionDAL().getRegionsFromList(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getChildRegions(String regionUuid) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = new RegionDAL().getRegionsFromList(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getSharingAgreements(String regionUuid) throws Exception {

        List<String> sharingAgreementUuids = new SecurityMasterMappingDAL().getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!sharingAgreementUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(sharingAgreementUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getProcessingAgreements(String regionUuid) throws Exception {

        List<String> processingAgreementUuids = new SecurityMasterMappingDAL().getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!processingAgreementUuids.isEmpty())
            ret = new DataProcessingAgreementDAL().getDPAsFromList(processingAgreementUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
