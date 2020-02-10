-- oracle equivalent source sha1 1bc8d86b51641e0d6fd79422d30c5aca9640b3d4
--
-- Adding the missing columnt 'severity_id' to rhnErrataTmp
-- table to be consistent with rhnErrata table

DO $$
    BEGIN
        BEGIN
            ALTER TABLE rhnerratatmp ADD COLUMN severity_id NUMERIC;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column severity_id already exists in rhnerratatmp.';
        END;
    END;
$$;
