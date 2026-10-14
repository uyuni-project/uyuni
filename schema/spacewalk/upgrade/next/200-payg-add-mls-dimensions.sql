DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'billing_dimension_t') THEN
        ALTER TYPE billing_dimension_t ADD VALUE IF NOT EXISTS 'multi_linux_support';
        ALTER TYPE billing_dimension_t ADD VALUE IF NOT EXISTS 'multi_linux_support_ltss';
        ALTER TYPE billing_dimension_t ADD VALUE IF NOT EXISTS 'multi_linux_support_ems';
        ALTER TYPE billing_dimension_t ADD VALUE IF NOT EXISTS 'multi_linux_support_ha';
    ELSE
        RAISE NOTICE 'type "billing_dimension_t" does not exist, skipping';
    END IF;
END $$;
