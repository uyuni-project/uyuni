ALTER TABLE suseMinionInfo ADD COLUMN IF NOT EXISTS
    reboot_required_after TIMESTAMPTZ;

DO $$ 
BEGIN 
    IF EXISTS (
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'suseminioninfo' AND column_name = 'reboot_needed'
    ) THEN
        UPDATE suseMinionInfo
        SET reboot_required_after = CASE
            WHEN reboot_needed = 'Y' THEN CURRENT_TIMESTAMP
            ELSE NULL
        END;
    END IF;
END $$;

ALTER TABLE suseMinionInfo DROP COLUMN IF EXISTS reboot_needed;
