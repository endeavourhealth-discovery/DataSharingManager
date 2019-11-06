

alter table data_sharing_manager.dataset
add column technical_definition mediumtext null comment 'the technical definition of the cohort';

alter table data_sharing_manager.cohort
add column technical_definition mediumtext null comment 'the technical definition of the cohort';