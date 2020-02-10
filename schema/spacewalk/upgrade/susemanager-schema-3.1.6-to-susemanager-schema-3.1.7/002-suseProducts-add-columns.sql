-- Adding the column 'channel_family_id' to suseProducts
-- Adding the column 'base' to suseProducts

DO $$
    BEGIN
        BEGIN
            ALTER TABLE suseProducts ADD channel_family_id NUMERIC CONSTRAINT suse_products_cfid_fk REFERENCES rhnChannelFamily (id) ON DELETE SET NULL;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column channel_family_id already exists in suseProducts';
        END;
        BEGIN
            ALTER TABLE suseProducts ADD base CHAR(1) DEFAULT ('N') NOT NULL;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column base already exists in suseProducts';
        END;
    END;
$$;
