ALTER TABLE suseMinionInfo
    ADD COLUMN IF NOT EXISTS reboot_needed CHAR(1);
