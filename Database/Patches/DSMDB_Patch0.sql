-- DSMDB_Patch0

USE data_sharing_manager;

CREATE TABLE IF NOT EXISTS data_sharing_manager.patch_history (
    patch_name varchar(100) COMMENT 'Patch name',
    date_time_run datetime(3) COMMENT 'Datetime patch run',
    PRIMARY KEY data_sharing_manager_patch_history_patch_name (patch_name)
) COMMENT 'Hold patch history';