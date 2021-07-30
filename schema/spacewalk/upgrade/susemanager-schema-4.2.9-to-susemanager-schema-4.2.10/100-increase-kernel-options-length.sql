ALTER TABLE rhnKickstartableTree ALTER COLUMN kernel_options TYPE VARCHAR(2048);
ALTER TABLE rhnKickstartableTree ALTER COLUMN kernel_options_post TYPE VARCHAR(2048);
ALTER TABLE rhnKSData ALTER COLUMN kernel_params TYPE VARCHAR(2048);
DO $$
    BEGIN
        IF EXISTS
            (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name='rhnactionvirtcreate' AND column_name='vm_type'
            )
        THEN
            ALTER TABLE rhnActionVirtCreate ALTER COLUMN kernel_options TYPE VARCHAR(2048);
        END IF;
    END
$$ LANGUAGE plpgsql;
