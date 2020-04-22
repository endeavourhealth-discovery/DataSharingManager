-- DSMDB_Patch6
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch8') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL AmendProject();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch8', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AmendProject()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    
    select * from data_sharing_manager.project_type;
    
    IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 2
					AND project_type = 'API')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (2, 'API');
    
    END IF;
    
    IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 3
					AND project_type = 'Data Assurance')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (3, 'Data Assurance');
    
    END IF;
    
	IF NOT EXISTS(SELECT * FROM data_sharing_manager.project_type WHERE id = 4
					AND project_type = 'Distribution')
	THEN

    insert into data_sharing_manager.project_type (id, project_type)
    values (4, 'Distribution');
    
    END IF;
    
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='config_name'
                                                           AND TABLE_NAME='project' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
    ALTER TABLE data_sharing_manager.project
    ADD COLUMN 	config_name varchar(200) NULL COMMENT 'The name of the config record this project uses';

    END IF;


END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE AmendProject;