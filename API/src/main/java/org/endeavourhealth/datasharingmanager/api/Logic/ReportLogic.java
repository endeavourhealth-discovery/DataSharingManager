package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDDSOrganisationStatus;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.MasterMappingEntity;
import org.endeavourhealth.uiaudit.logic.EntityNameGetter;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportLogic {
    private static ExchangeDalI exchangeRepository = DalProvider.factoryExchangeDal();
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();

    public Response getPublisherReport(List<String> odsCodes, String agreementName) throws Exception {

        List<JsonDDSOrganisationStatus> statuses = exchangeRepository.getOrganisationStatus(odsCodes, agreementName);

        return Response
                .ok()
                .entity(statuses)
                .build();

    }

    public Response getActivityReport(Short parentMapTypeId, Short childMapTypeId, int days) throws Exception {

        List<MasterMappingEntity> mappings = masterMappingRepository.getChildMappingsInLastXDays(parentMapTypeId, childMapTypeId, days);

        List<String> parents = mappings.stream()
                .map(MasterMappingEntity::getParentUuid)
                .distinct()
                .collect(Collectors.toList());

        List<String> children = mappings.stream()
                .map(MasterMappingEntity::getChildUuid)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> parentNames =  EntityNameGetter.replaceUUIDsWithNameMap(MapType.valueOfTypeId(parentMapTypeId), parents);
        Map<String, String> childNames =  EntityNameGetter.replaceUUIDsWithNameMap(MapType.valueOfTypeId(childMapTypeId), children);

        for (MasterMappingEntity map : mappings) {
            map.setParentUuid(parentNames.get(map.getParentUuid()));
            map.setChildUuid(childNames.get(map.getChildUuid()));
        }

        return Response
                .ok()
                .entity(mappings)
                .build();

    }


}
