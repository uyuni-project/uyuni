ALTER TABLE rhnProxyInfo
    ADD COLUMN IF NOT EXISTS ssh_port NUMERIC,
    ADD COLUMN IF NOT EXISTS created  TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;


CREATE OR REPLACE FUNCTION rhn_proxy_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
    new.modified := current_timestamp;
    return new;
END;
$$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS rhn_proxy_info_mod_trig ON rhnProxyInfo;

CREATE TRIGGER rhn_proxy_info_mod_trig BEFORE INSERT OR UPDATE ON rhnProxyInfo
    FOR EACH ROW EXECUTE PROCEDURE rhn_proxy_info_mod_trig_fun();
