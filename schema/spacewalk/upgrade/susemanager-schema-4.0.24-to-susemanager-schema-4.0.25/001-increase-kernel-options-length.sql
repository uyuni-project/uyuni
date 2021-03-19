ALTER TABLE rhnKickstartableTree ALTER COLUMN kernel_options TYPE VARCHAR(2048);
ALTER TABLE rhnKickstartableTree ALTER COLUMN kernel_options_post TYPE VARCHAR(2048);
ALTER TABLE rhnKSData ALTER COLUMN kernel_params TYPE VARCHAR(2048);