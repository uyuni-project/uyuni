alter table rhnConfiguration disable trigger rhn_conf_mod_trig;

INSERT INTO rhnConfiguration (key, value, default_value)
    SELECT 'EXTAUTH_DEFAULT_ORGID', value, null
    FROM rhnConfiguration
    WHERE key = 'extauth_default_orgid';
DELETE FROM rhnConfiguration
    WHERE key = 'extauth_default_orgid';

INSERT INTO rhnConfiguration (key, default_value, value)
    SELECT 'EXTAUTH_USE_ORGUNIT', 'false', COALESCE(value, 'false')
    FROM rhnConfiguration
    WHERE key = 'extauth_use_orgunit';
DELETE FROM rhnConfiguration
    WHERE key = 'extauth_use_orgunit';

INSERT INTO rhnConfiguration (key, default_value, value)
    SELECT 'SYSTEM_CHECKIN_THRESHOLD', '1', COALESCE(value, '1')
    FROM rhnConfiguration
    WHERE key = 'system_checkin_threshold';
DELETE FROM rhnConfiguration
    WHERE key = 'system_checkin_threshold';

INSERT INTO rhnConfiguration (key, default_value, value)
    SELECT 'EXTAUTH_KEEP_TEMPROLES', 'false', COALESCE(value, 'false')
    FROM rhnConfiguration
    WHERE key = 'extauth_keep_temproles';
DELETE FROM rhnConfiguration
    WHERE key = 'extauth_keep_temproles';

alter table rhnConfiguration enable trigger rhn_conf_mod_trig;