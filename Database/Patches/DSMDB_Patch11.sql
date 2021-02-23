-- DSMDB_Patch6
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch11') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL AmendProject();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch11', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AmendProject()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)

    IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 5
					AND project_type = 'Query - view down to practice level')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (5, 'Query - view down to practice level');
    
    END IF;
    
    IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 6
					AND project_type = 'Query - view down to CCG/Borough level')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (6, 'Query - view down to CCG/Borough level');
    
    END IF;
    
	IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 7
					AND project_type = 'Query - view down to STP level')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (7, 'Query - view down to STP level');
    
    END IF;


END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE AmendProject;