alter table rhnConfiguration disable trigger rhn_conf_mod_trig;
UPDATE rhnConfiguration SET
    default_value = NULL
    WHERE key = 'extauth_default_orgid';
UPDATE rhnConfiguration SET
    default_value = 'false',
    value = COALESCE(value, default_value)
    WHERE key = 'extauth_use_orgunit';
UPDATE rhnConfiguration SET
    default_value = '1',
    value = COALESCE(value, default_value)
    WHERE key = 'system_checkin_threshold';
UPDATE rhnConfiguration SET
    default_value = 'false',
    value = COALESCE(value, default_value)
    WHERE key = 'extauth_keep_temproles';
alter table rhnConfiguration enable trigger rhn_conf_mod_trig;