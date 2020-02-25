package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDDSOrganisationStatus;

import javax.ws.rs.core.Response;
import java.util.List;

public class ReportLogic {
    private static ExchangeDalI exchangeRepository = DalProvider.factoryExchangeDal();

    public Response getPublisherReport(List<String> odsCodes, String agreementName) throws Exception {

        List<JsonDDSOrganisationStatus> statuses = exchangeRepository.getOrganisationStatus(odsCodes, agreementName);

        return Response
                .ok()
                .entity(statuses)
                .build();

    }
}
