package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.ddsmodel.models.DAL.SecurityDDSDAL;
import org.endeavourhealth.common.security.ddsmodel.models.json.JsonDDSOrganisationStatus;

import javax.ws.rs.core.Response;
import java.util.List;

public class ReportLogic {

    public Response getPublisherReport(List<String> odsCodes, String agreementName) throws Exception {

        List<JsonDDSOrganisationStatus> statuses = new SecurityDDSDAL().getOrganisationStatus(odsCodes, agreementName);

        return Response
                .ok()
                .entity(statuses)
                .build();

    }
}
