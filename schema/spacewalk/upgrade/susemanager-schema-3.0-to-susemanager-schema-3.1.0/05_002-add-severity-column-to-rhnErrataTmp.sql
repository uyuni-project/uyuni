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
