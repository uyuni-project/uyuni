ALTER TABLE rhnProxyInfo
    ADD COLUMN IF NOT EXISTS salt_publish_port NUMERIC,
    ADD COLUMN IF NOT EXISTS salt_request_port NUMERIC;
