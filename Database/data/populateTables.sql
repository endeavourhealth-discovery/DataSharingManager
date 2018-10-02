
/*data_sharing_manager.flow_direction*/
insert into data_sharing_manager.flow_direction (id, direction)
values (0, "Inbound");

insert into data_sharing_manager.flow_direction (id, direction)
values (1, "Outbound");

/*data_sharing_manager.flow_schedule*/
insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (1, "On Demand");

insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (0, "Daily");

insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (2, "Weekly");

insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (3, "Monthly");

insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (4, "Annually");

insert into data_sharing_manager.flow_schedule (id, flow_schedule)
values (5, "One off");

/*data_sharing_manager.business_case_status*/
insert into data_sharing_manager.business_case_status (id, business_case_status)
values (0, "Submitted");

insert into data_sharing_manager.business_case_status (id, business_case_status)
values (1, "Approved");

/*data_sharing_manager.data_exchange_method*/
insert into data_sharing_manager.data_exchange_method (id, data_exchange_method)
values (0, "Paper");

insert into data_sharing_manager.data_exchange_method (id, data_exchange_method)
values (1, "Electronic");

/*data_sharing_manager.flow_status*/
insert into data_sharing_manager.flow_status (id, flow_status)
values (0, "In Development");

insert into data_sharing_manager.flow_status (id, flow_status)
values (1, "Live");

/*data_sharing_manager.dsa_status*/
insert into data_sharing_manager.dsa_status (id, dsa_status)
values (0, "Active");

insert into data_sharing_manager.dsa_status (id, dsa_status)
values (1, "Inactive");

/*data_sharing_manager.storage_protocol*/
insert into data_sharing_manager.storage_protocol (id, storage_protocol)
values (0, "Audit Only");

insert into data_sharing_manager.storage_protocol (id, storage_protocol)
values (1, "Temporary Store and Forward");

insert into data_sharing_manager.storage_protocol (id, storage_protocol)
values (2, "Permanent Record Store");

/*data_sharing_manager.nature_of_information*/
insert into data_sharing_manager.nature_of_information (id, nature_of_information)
values (0, "Personal");

insert into data_sharing_manager.nature_of_information (id, nature_of_information)
values (1, "Personal Sensitive");

insert into data_sharing_manager.nature_of_information (id, nature_of_information)
values (2, "Commercial");

/*data_sharing_manager.format_type*/
insert into data_sharing_manager.format_type (id, format_type)
values (0, "Removable Media");

insert into data_sharing_manager.format_type (id, format_type)
values (1, "Electronic Structured Data");

/*data_sharing_manager.data_subject_type*/
insert into data_sharing_manager.data_subject_type (id, data_subject_type)
values (0, "Patient");

/*data_sharing_manager.review_cycle*/
insert into data_sharing_manager.review_cycle (id, review_cycle)
values (0, "Annually");

insert into data_sharing_manager.review_cycle (id, review_cycle)
values (1, "Monthly");

insert into data_sharing_manager.review_cycle (id, review_cycle)
values (2, "Weekly");

/*data_sharing_manager.map_type*/
insert into data_sharing_manager.map_type (id, map_type)
values (0, "Service");

insert into data_sharing_manager.map_type (id, map_type)
values (1, "Organisation");

insert into data_sharing_manager.map_type (id, map_type)
values (2, "Region");

insert into data_sharing_manager.map_type (id, map_type)
values (3, "Data Sharing Agreement");

insert into data_sharing_manager.map_type (id, map_type)
values (4, "Data Flow");

insert into data_sharing_manager.map_type (id, map_type)
values (5, "Data Processing Agreement");

insert into data_sharing_manager.map_type (id, map_type)
values (6, "Cohort");

insert into data_sharing_manager.map_type (id, map_type)
values (7, "Data Set");

insert into data_sharing_manager.map_type (id, map_type)
values (8, "Publisher");

insert into data_sharing_manager.map_type (id, map_type)
values (9, "Subscriber");

insert into data_sharing_manager.map_type (id, map_type)
values (10, "Purpose");

insert into data_sharing_manager.map_type (id, map_type)
values (11, "Benefit");

insert into data_sharing_manager.map_type (id, map_type)
values (12, "Document");

insert into data_sharing_manager.map_type (id, map_type)
values (13, "Data Exchange");

insert into data_sharing_manager.map_type (id, map_type)
values (14, "Project");

/*data_sharing_manager.security_infrastructure*/
insert into data_sharing_manager.security_infrastructure (id, security_infrastructure)
values (0, "N3");

insert into data_sharing_manager.security_infrastructure (id, security_infrastructure)
values (1, "PSN");

insert into data_sharing_manager.security_infrastructure (id, security_infrastructure)
values (2, "Internet");

/*data_sharing_manager.security_architecture*/
insert into data_sharing_manager.security_architecture (id, security_architecture)
values (0, "TLS/MA");

insert into data_sharing_manager.security_architecture (id, security_architecture)
values (1, "Secure FTP");

/*data_sharing_manager.deidentification_level*/
insert into data_sharing_manager.deidentification_level (id, deidentification_level)
values (0, "Patient identifiable data");

insert into data_sharing_manager.deidentification_level (id, deidentification_level)
values (1, "Pseudonymised data");

/*data_sharing_manager.project_type*/
insert into data_sharing_manager.project_type (id, project_type)
values (0, "Extract");

insert into data_sharing_manager.project_type (id, project_type)
values (1, "Query");


/*data_sharing_manager.ConsentModel*/
insert into data_sharing_manager.consent_model (id, consent_model)
values (0, "Explicit Consent");

insert into data_sharing_manager.consent_model (id, consent_model)
values (1, "Implied Consent");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (0, "GP Practice");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (1, "NHS Trust");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (2, "NHS Trust Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (3, "Pathology Laboratory");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (4, "Branch");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (5, "Commissioning Region");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (6, "Care Trust");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (7, "Care Trust Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (8, "CCG");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (9, "CCG Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (10, "CSU");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (11, "CSU Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (12, "Education Establishment");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (13, "NHS Hospice");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (14, "Non NHS Hospice");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (15, "IoM Government Directorate");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (16, "IoM Government Department");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (17, "Justice Entity");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (18, "Non NHS Organisation");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (19, "NHS Support Agency");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (20, "Optical HQ");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (21, "Optical Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (22, "Other");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (23, "Pharmacy HQ");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (24, "ISHP");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (25, "ISHP Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (26, "Prison");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (27, "School");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (28, "Special Health Authority");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (29, "Local Authority");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (30, "Local Authority Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (31, "NI organisation");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (32, "Scottish GP Practice");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (33, "Scottish Provider Organisation");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (34, "Wales Health Board");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (35, "Wales Health Board Site");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (36, "Dispensary");

insert into data_sharing_manager.organisation_type (id, organisation_type)
values (37, "IoM Government Directorate Site");
