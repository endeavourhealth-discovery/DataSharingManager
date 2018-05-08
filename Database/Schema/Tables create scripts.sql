drop database if exists data_sharing_manager;

create database data_sharing_manager;

/*
Database containing all the tables required to hold the data sharing agreements, 
organisation, regions and the mappings that tie them all together.
*/

/*Main entity tables containing the core information*/
drop table if exists data_sharing_manager.region;
drop table if exists data_sharing_manager.organisation;
drop table if exists data_sharing_manager.address;
drop table if exists data_sharing_manager.cohort;
drop table if exists data_sharing_manager.dataset;
drop table if exists data_sharing_manager.data_flow;
drop table if exists data_sharing_manager.data_sharing_agreement;
drop table if exists data_sharing_manager.data_sharing_summary;
drop table if exists data_sharing_manager.purpose;
drop table if exists data_sharing_manager.documentation;
drop table if exists data_sharing_manager.data_processing_agreement;

/*Mapping table linking all the entities together*/
drop table if exists data_sharing_manager.master_mapping;

/*
Look up tables containing enumerations for certain fields.  
Will probably be replaced with the IM in time
*/
drop table if exists data_sharing_manager.flow_direction;
drop table if exists data_sharing_manager.flow_schedule;
drop table if exists data_sharing_manager.data_exchange_method;
drop table if exists data_sharing_manager.flow_status;
drop table if exists data_sharing_manager.security_infrastructure;
drop table if exists data_sharing_manager.security_architecture;
drop table if exists data_sharing_manager.dsa_status;
drop table if exists data_sharing_manager.consent_model;
drop table if exists data_sharing_manager.storage_protocol;
drop table if exists data_sharing_manager.nature_of_information;
drop table if exists data_sharing_manager.format_type;
drop table if exists data_sharing_manager.data_subject_type;
drop table if exists data_sharing_manager.review_cycle;
drop table if exists data_sharing_manager.map_type;
drop table if exists data_sharing_manager.organisation_type;

/*
Look up tables containing enumerations for certain fields.  
Will probably be replaced with the IM in time
*/
create table data_sharing_manager.flow_direction (
	id smallint not null comment 'Lookup Id',
    direction varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_flow_direction_uuid_pk primary key (id)    
) comment 'Lookup table holding enumerations for flow direction';

create table data_sharing_manager.flow_schedule (
	id smallint not null comment 'Lookup Id',
    flow_schedule varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_flow_schedule_id_pk primary key (id)      
) comment 'Lookup table holding enumerations for flow schedule';


create table data_sharing_manager.data_exchange_method (
	id smallint not null comment 'Lookup Id',
    data_exchange_method varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_data_exchange_method_id_pk primary key (id)     
) comment 'Lookup table holding enumerations for the data exchange method';


create table data_sharing_manager.flow_status (
	id smallint not null comment 'Lookup Id',
    flow_status varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_flow_status_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for flow status';

create table data_sharing_manager.security_infrastructure (
	id smallint not null comment 'Lookup Id',
    security_infrastructure varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_security_infrastructure_id_pk primary key (id)     
) comment 'Lookup table holding enumerations for security infrastructure';


create table data_sharing_manager.security_architecture (
	id smallint not null comment 'Lookup Id',
    security_architecture varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_security_architecture_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for security architecture';

create table data_sharing_manager.dsa_status (
	id smallint not null comment 'Lookup Id',
    dsa_status varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_dsa_status_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for status';

create table data_sharing_manager.consent_model (
	id smallint not null comment 'Lookup Id',
    consent_model varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_consent_model_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for the consent model used';

create table data_sharing_manager.storage_protocol (
	id smallint not null comment 'Lookup Id',
    storage_protocol varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_storage_protocol_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for the storage protocol';

create table data_sharing_manager.nature_of_information (
	id smallint not null comment 'Lookup Id',
    nature_of_information varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_nature_of_information_id_pk primary key (id)     
) comment 'Lookup table holding enumerations for the nature of information';

create table data_sharing_manager.format_type (
	id smallint not null comment 'Lookup Id',
    format_type varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_format_type_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for the format type used';

create table data_sharing_manager.data_subject_type (
	id smallint not null comment 'Lookup Id',
    data_subject_type varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_data_subject_type_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for the format type';

create table data_sharing_manager.review_cycle (
	id smallint not null comment 'Lookup Id',
    review_cycle varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_review_cycle_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for the review cycle';

create table data_sharing_manager.map_type (
	id smallint not null comment 'Lookup Id',
    map_type varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_map_type_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for map types used in the master mapping table';

create table data_sharing_manager.organisation_type (
	id tinyint not null comment 'Lookup Id',
    organisation_type varchar(100) not null comment 'Lookup Value',   
    
    constraint data_sharing_manager_organisation_type_id_pk primary key (id)  
) comment 'Lookup table holding enumerations for organisation types';

/*Main entity tables containing the core information*/
create table data_sharing_manager.region (
	uuid char(36) not null comment 'Unique identifier for the region',
    name varchar(100) not null comment 'Region name',
    description varchar(10000) null comment 'Optional description of the region',    
    
    constraint data_sharing_manager_region_uuid_pk primary key (uuid),
    index data_sharing_manager_region_name_idx (name) 
) comment 'Holds all the details of the regions';


create table data_sharing_manager.organisation (
	uuid char(36) not null comment 'Unique identifier for the organisation',
    name varchar(100) not null comment 'Name of the organisation',
    alternative_name varchar(100) null comment 'Optional alternative name',
    ods_code varchar(10) null comment 'ODS Code',
    ico_code varchar(10) null comment 'ICO Code',
    ig_toolkit_status varchar(10) null comment 'What is the status of the organisations IG Toolkit assesement',
    date_of_registration date null comment 'When was the organisation registered',
    registration_person char(36) null comment 'Who registered the organisation',  
    evidence_of_registration varchar(500) null comment 'Documented evidence of registration', 
    is_service boolean not null comment 'Flag to determine whether this is an organisation or a service',
    type tinyint not null comment 'The type of organisation eg GP Surgery, NHS Trust',
    active boolean not null default 1 comment 'Flag to determine if the organisation is active',
    bulk_imported boolean not null comment 'Flag to determine if the organisation has been bulk imported',
    bulk_item_updated boolean not null comment 'Flag to determine if the organisation has been updated since the bulk import',
    bulk_conflicted_with char(36) null comment 'If organisation has been updated, this holds the duplicate organisation UUID to allow the conflicts to be resolved',
        
    constraint data_sharing_manager_organisation_uuid_pk primary key (uuid),
    index data_sharing_manager_organisation_name_idx (name),
    index data_sharing_manager_organisation_ods_code_idx (ods_code),
    index data_sharing_manager_organisation_name_ods_code_idx (name, ods_code),
    index data_sharing_manager_organisation_type_idx (type)
) comment 'Holds all the details of the organisations';


create table data_sharing_manager.address (
	uuid char(36) not null comment 'Unique identifier for the adress',
    organisation_uuid char(36) not null comment 'The organisation the address is associated with',
    building_name varchar(100) null comment 'Building name',
    Number_and_street varchar(100) null comment 'Number and street',
    locality varchar(100) null comment 'Locality',
    city varchar(100) null comment 'City',
    county varchar(100) null comment 'County',
    postcode varchar(100) null comment 'Postcode',
    lat float(10,6) null comment 'Latitude co-ordinates used for mapping location',
    lng float(10,6) null comment 'Longitude co-ordinates used for mapping location',
    geolocation_reprocess tinyint(1) comment 'Do we need to reprocess the GeoLocation details',        
    
    constraint data_sharing_manager_address_uuid_pk primary key (uuid),
    foreign key data_sharing_manager_address_organisation_uuid_fk (organisation_uuid) references data_sharing_manager.organisation(uuid) on delete cascade
) comment 'Holds address information for organisations';

create table data_sharing_manager.cohort (
	uuid char(36) not null comment 'Unique identifier for the cohort',
    name varchar(100) not null comment 'Name of the cohort',
    consent_model_id smallint null comment 'Consent model of the cohort',
    description varchar(10000) null comment 'description of the cohort',
    
    constraint data_sharing_manager_cohort_uuid_pk primary key (uuid),
    index data_sharing_manager_cohort_name_idx (name),    
    foreign key data_sharing_manager_cohort_consent_model_id_fk (consent_model_id) references data_sharing_manager.consent_model(id)
    
) comment 'Holds details of the Cohorts that have been defined';

create table data_sharing_manager.dataset (
	uuid char(36) not null comment 'Unique identifier for the cohort',
    name varchar(100) not null,
    description varchar(10000) null,
    
    constraint data_sharing_manager_dataset_uuid_pk primary key (uuid),    
    index data_sharing_manager_dataset_name_idx (name) 

) comment 'Holds details of the datasets that have been defined';

create table data_sharing_manager.data_flow (
	uuid char(36) not null comment 'Unique identifier for the data flow agreement',
    name varchar(100) not null comment 'Name of the data flow agreement',
    direction_id smallint null comment 'Whether the data flow is inbound or outbound', 
    flow_schedule_id smallint not null comment 'The flow schedule eg Daily, On Demand', 
    approximate_volume int not null comment 'Approximate volume of data that is contained in the data flow',
    data_exchange_method_id smallint not null comment 'The data exchange method eg Paper, electronic',
    flow_status_id smallint not null comment 'The status of the data flow eg In development, Live',    
    additional_documentation varchar(100) null comment 'Any Associated documentation',
    sign_off varchar(10) comment 'Who signed off the data flow',
    storage_protocol_id smallint not null comment 'Storage protocol eg Temporary store and forward, permanent',
    security_infrastructure_id smallint not null comment 'Security Infrastructure eg N3, Internet',
    security_architecture_id smallint not null comment 'Security Architecture eg TLS/MA, Secure FTP',
    
    constraint data_sharing_manager_data_flow_uuid_pk primary key (uuid),
    index data_sharing_manager_data_flow_name_idx (name),
    foreign key data_sharing_manager_data_flow_flow_scheduleId_fk (flow_schedule_id) references data_sharing_manager.flow_schedule(id),    
    foreign key data_sharing_manager_data_flow_data_exchange_methodId_fk (data_exchange_method_id) references data_sharing_manager.data_exchange_method(id),
    foreign key data_sharing_manager_data_flow_flow_statusId_fk (flow_status_id) references data_sharing_manager.flow_status(id),
    foreign key data_sharing_manager_data_flow_DirectionId_fk (direction_id) references data_sharing_manager.flow_direction(id),
    foreign key data_sharing_manager_data_storage_protocolId_fk (storage_protocol_id) references data_sharing_manager.storage_protocol(id)
    
) comment 'Holds details of the data flow agreements that have been defined';

create table data_sharing_manager.data_processing_agreement (
	uuid char(36) not null comment 'Unique identifier for the data processing agreement',
    name varchar(100) not null comment 'Name of the data processing agreement',
    description varchar(10000) null comment 'Description of the data processing agreement',
    derivation varchar(100) null comment 'Does this derivate from another data processing agreement', 
    publisher_information varchar(100) null comment 'Who published the agreement', 
    publisher_contract_information varchar(100) null comment 'Contract Information',
    publisher_dataset char(36) null comment 'Publisher dataset associated with the agreement',
    dsa_status_id smallint not null comment 'Status of the agreement',
    return_to_sender_policy varchar(100) null comment 'Details of the return to sender policy',
    start_date date null comment 'Date the agreement starts',
    end_date date null comment 'Date the agreement ends',
    
    constraint data_sharing_manager_data_processing_agreement_uuid_pk primary key (uuid),
    index data_sharing_manager_data_processing_agreement_name_idx (name),    
    foreign key data_sharing_manager_data_processing_agreement_dsa_status_id_fk (dsa_status_id) references data_sharing_manager.dsa_status(id)
) comment 'Holds details of the data processing agreements that have been defined';

create table data_sharing_manager.data_sharing_summary (
	uuid char(36) not null comment 'Unique identifier for the data processing summary',
    name varchar(100) not null comment 'Name of the data sharing summary',
    description varchar(10000) null comment 'Description of the data sharing summary',
    purpose varchar(10000) null comment 'Purpose of the data sharing summary', 
    nature_of_information_id smallint not null comment 'What is the nature of information',
    schedule2_condition varchar(100) null comment 'What is the Schedule2 condition',
    benefit_to_sharing varchar(200) null comment 'What are the benefits of sharing',
    overview_of_data_items varchar(100) null comment 'Overview of data items being shared',
    format_type_id smallint not null comment 'What is the format eg Electronic Structured data, removable media',
    data_subject_type_id smallint not null comment 'What is the data subject type eg Patient',
    nature_of_persons_accessing_data varchar(100) null comment 'Details about the nature of access',
    review_cycle_id smallint not null comment 'What is the review cycle eg Annually, Monthly',
    review_date date null comment 'Review data of the sharing agreement',
    start_date date null comment 'Start date of the agreement',
    evidence_of_agreement varchar(200) null comment 'Evidence of the agreement',
    
    constraint data_sharing_manager_data_sharing_summary_uuid_pk primary key (uuid),
    index data_sharing_manager_data_sharing_summary_name_idx (name),    
    foreign key data_sharing_manager_data_sharing_summary_format_type_id_fk (format_type_id) references data_sharing_manager.format_type(id),
    foreign key data_sharing_manager_data_sharing_summary_nature_of_info_id_fk (nature_of_information_id) references data_sharing_manager.nature_of_information(id),
    foreign key data_sharing_manager_data_sharing_summary_subject_type_id_fk (data_subject_type_id) references data_sharing_manager.data_subject_type(id),
    foreign key data_sharing_manager_data_sharing_summary_review_cycle_id_fk (review_cycle_id) references data_sharing_manager.review_cycle(id)
) comment 'Holds details of the data sharing summaries that have been defined';

create table data_sharing_manager.data_sharing_agreement (
	uuid char(36) not null comment 'Unique identifier for the data sharing agreement',
    name varchar(100) not null comment 'Name of the data sharing agreement',
    description varchar(10000) null comment 'Description of the data sharing agreement',
    derivation varchar(100) null comment 'Does this derivate from another data sharing agreement', 
    dsa_status_id smallint not null comment 'Status of the sharing agreement',
    consent_model_id smallint not null comment 'Consent model used eg Explicit, Implied',
    start_date Date null comment 'Start Date of the sharing agreement',
    end_date Date null comment 'End Date of the sharing agreement',   
    
    constraint data_sharing_manager_data_sharing_agreement_uuid_pk primary key (uuid),
    index data_sharing_manager_data_sharing_agreement_name_idx (name),    
    
    foreign key data_sharing_manager_data_sharing_agreement_dsa_status_id_fk (dsa_status_id) references data_sharing_manager.dsa_status(id),
    foreign key data_sharing_manager_data_sharing_agreement_consent_model_id_fk (consent_model_id) references data_sharing_manager.consent_model(id)
) comment 'Holds details of the data sharing agreements that have been defined';

create table data_sharing_manager.documentation (
	uuid char(36) not null comment 'Unique identifier for the data sharing agreement',
    title varchar(50) not null comment 'Title of the document',
    filename varchar(50) not null comment 'Filename of the document',
    fileData mediumtext not null comment 'Base64 encoded file data',   
    
    constraint data_sharing_manager_documentation_uuid_pk primary key (uuid),
    index data_sharing_manager_documentation_title_idx (title)
) comment 'Hold documentation associated with sharing agreements';

create table data_sharing_manager.purpose (
	uuid char(36) not null comment 'Unique identifier for the purpose',
    title varchar(50) not null comment 'Title of the purpose/benefit',
    detail varchar(10000) not null comment 'Full details of the purpose/benefit',
    
    constraint data_sharing_manager_purpose_uuid_pk primary key (uuid)
) comment 'Hold details of purposes and benefits associated with sharing agreements';

/*Mapping table linking all the entities together*/
create table data_sharing_manager.master_mapping (
	child_uuid char(36) not null comment 'UUID of the child item in the relationship',
    child_map_type_id smallint not null comment 'The item type of the child in the relationship',
    parent_uuid char(36) not null comment 'UUID of the parent item in the relationship',
    parent_map_type_id smallint not null comment 'The item type of the parent in the relationship',
    is_default boolean not null comment 'Is this the default relationship of this type',
    
    foreign key data_sharing_manager_master_mapping_child_map_type_id_pk (child_map_type_id) references data_sharing_manager.map_type(id),
    foreign key data_sharing_manager_master_mapping_parent_map_type_id_pk (parent_map_type_id) references data_sharing_manager.map_type(id),
    primary key data_sharing_manager_master_mapping_pk (child_uuid, child_map_type_id, parent_uuid, parent_map_type_id)
) comment 'Single mapping table storing how each item is linked to other items';

