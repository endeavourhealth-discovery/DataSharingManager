package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.RegionEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonRegion;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.*;
import org.endeavourhealth.common.security.usermanagermodel.models.database.UserRegionEntity;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegionLogic {

    public Response getRegion(String uuid, String searchData, String userId) throws Exception {

        if (uuid == null && searchData == null && userId == null) {
            return getRegionList();
        } else if (userId != null) {
            return getRegionListFilterOnRegion(userId);
        }  else if (uuid != null){
            return getSingleRegion(uuid);
        } else {
            return search(searchData);
        }
    }

    private Response getRegionListFilterOnRegion(String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<RegionEntity> dpas = RegionCache.getAllChildRegionsForRegion(userRegion.getRegionId());

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response postRegion(JsonRegion region, String userProjectID) throws Exception {

        if (region.getUuid() != null) {
            new RegionDAL().updateRegion(region, userProjectID);
        } else {
            region.setUuid(UUID.randomUUID().toString());
            new RegionDAL().saveRegion(region, userProjectID);
        }

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

    public List<String> filterRegionsForUser(List<String> regions, String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<RegionEntity> regionsForUsers = RegionCache.getAllChildRegionsForRegion(userRegion.getRegionId());

        Set<String> regionForUserSet =
                regionsForUsers.stream()
                        .map(RegionEntity::getUuid)
                        .collect(Collectors.toSet());

        regions = regions.stream().filter(reg -> regionForUserSet.contains(reg)).collect(Collectors.toList());

        return regions;

    }

    public Response getParentRegions(String regionUuid, String userId) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getParentMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (userId != null) {
            regionUuids = filterRegionsForUser(regionUuids, userId);
        }

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
            ret = new DataProcessingAgreementCache().getDPADetails(processingAgreementUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
