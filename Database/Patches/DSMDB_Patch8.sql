-- DSMDB_Patch6
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch9') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL AddAuthoriserToProject();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch9', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddAuthoriserToProject()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
        
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='authorised_by'
                                                           AND TABLE_NAME='project' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
    ALTER TABLE data_sharing_manager.project
    ADD COLUMN 	authorised_by varchar(36) NULL COMMENT 'The uuid of the user that authorised the project';

    END IF;
    
    
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='authorised_date'
                                                           AND TABLE_NAME='project' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
    ALTER TABLE data_sharing_manager.project
    ADD COLUMN 	authorised_date datetime NULL COMMENT 'The date that the user authorised the project';

    END IF;


END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE AddAuthoriserToProject;