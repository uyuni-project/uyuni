INSERT INTO rhnConfiguration (key, description, default_value)
VALUES ('extauth_default_orgid', 'Organization id, where externally authenticated users will be created.', NULL)
ON CONFLICT (key) DO UPDATE
SET default_value = EXCLUDED.default_value;
INSERT INTO rhnConfiguration (key, description, default_value)
VALUES ('extauth_use_orgunit', 'Use Org. Unit IPA setting as organization name to create externally authenticated users in.', FALSE)
ON CONFLICT (key) DO UPDATE
SET default_value = EXCLUDED.default_value;
INSERT INTO rhnConfiguration (key, description, default_value)
VALUES ('extauth_keep_temproles', 'Keep temporary user roles granted due to the external authentication setup for subsequent logins using password.', FALSE)
ON CONFLICT (key) DO UPDATE
SET default_value = EXCLUDED.default_value;
INSERT INTO rhnConfiguration (key, description, default_value)
VALUES ('system_checkin_threshold', 'Number of days before reporting a system as inactive.', 1)
ON CONFLICT (key) DO UPDATE
SET default_value = EXCLUDED.default_value;
UPDATE rhnConfiguration SET
    value = COALESCE(value, default_value)
    WHERE key = 'extauth_default_orgid' 
    OR key = 'extauth_use_orgunit' 
    OR key = 'extauth_keep_temproles' 
    OR key = 'system_checkin_threshold';