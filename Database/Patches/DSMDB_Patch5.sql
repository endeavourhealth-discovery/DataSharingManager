-- DSMDB_Patch5
-- CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO

USE data_sharing_manager;

DROP PROCEDURE IF EXISTS RunPatchIfNotRunAlready;

DELIMITER //
CREATE PROCEDURE RunPatchIfNotRunAlready()
BEGIN
    -- (CHANGE THE PATCH NUMBER TO THE NEXT NEW ONE AFTER THOSE STORED IN DSM REPO, CHANGE IT BELOW HERE)
    IF ((SELECT date_time_run FROM data_sharing_manager.patch_history WHERE patch_name = 'DSMDB_Patch5') IS NULL)

    THEN
        -- ADD YOUR PATCH PROCEDURE CALL(S) HERE
        CALL CreateSystemSupplierSystemTable();
        CALL InsertDataIntoSystemSupplierSystemTable();
        CALL AddColumnsToOrganisation();

        INSERT INTO data_sharing_manager.patch_history (patch_name, date_time_run)
        VALUES ('DSMDB_Patch5', now());
        -- (CHANGE PATCH NUMBER ABOVE)

    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE CreateSystemSupplierSystemTable()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    CREATE TABLE IF NOT EXISTS data_sharing_manager.system_supplier_system (
        id SMALLINT(6) NOT NULL COMMENT 'Unique identifier for the system supplier system',
        system_supplier_system VARCHAR(100) NOT NULL COMMENT 'The system supplier system',
        system_supplier VARCHAR(100) NOT NULL COMMENT 'The system supplier',

        PRIMARY KEY data_sharing_manager_system_supplier_system_id (id)
    ) COMMENT 'Hold system supplier systems';

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE InsertDataIntoSystemSupplierSystemTable()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('0', 'Not entered', 'Not entered');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('1', 'EMIS Web', 'EMIS Health');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('2', 'SystmOne', 'TPP');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('3', 'Vision', 'In Practice Systems');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('4', 'Adastra', 'Advanced');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('5', 'Cerner Millennium', 'Cerner');
    INSERT INTO data_sharing_manager.system_supplier_system(id, system_supplier_system, system_supplier)
    VALUES ('6', 'Rio', 'Servelec');

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE AddColumnsToOrganisation()
BEGIN
    -- (ADD WHAT YOU WANT TO DO FOR YOUR PATCH PROCEDURE HERE)
    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='system_supplier_system_id'
                                                           AND TABLE_NAME='organisation' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.organisation
        ADD COLUMN system_supplier_system_id SMALLINT(6) NULL COMMENT 'The id of the system supplier system for the organisation';

    END IF;

    IF NOT EXISTS(SELECT * FROM information_schema.KEY_COLUMN_USAGE WHERE COLUMN_NAME='system_supplier_system_id'
                                                                    AND REFERENCED_TABLE_NAME='system_supplier_system' AND TABLE_NAME='organisation' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.organisation
        ADD FOREIGN KEY data_sharing_manager_organisation_system_supplier_system_id_fk (system_supplier_system_id) REFERENCES data_sharing_manager.system_supplier_system(id);

    END IF;

    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='system_supplier_reference'
                                                           AND TABLE_NAME='organisation' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.organisation
        ADD COLUMN system_supplier_reference VARCHAR(50) NULL COMMENT 'The system supplier reference for the organisation';

    END IF;

    IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE COLUMN_NAME='system_supplier_sharing_activated'
                                                           AND TABLE_NAME='organisation' AND TABLE_SCHEMA='data_sharing_manager')

    THEN
        ALTER TABLE data_sharing_manager.organisation
        ADD COLUMN system_supplier_sharing_activated TINYINT(1) NULL COMMENT 'Whether or not the system supplier has activated sharing for the organisation';

    END IF;

END //
DELIMITER ;

CALL RunPatchIfNotRunAlready();

DROP PROCEDURE RunPatchIfNotRunAlready;
-- (DROP YOUR PATCH PROCEDURE(S) BELOW HERE)
DROP PROCEDURE CreateSystemSupplierSystemTable;
DROP PROCEDURE InsertDataIntoSystemSupplierSystemTable;
DROP PROCEDURE AddColumnsToOrganisation;