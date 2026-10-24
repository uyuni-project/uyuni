INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY', 'HTTP proxy used by the server to connect to the internet in the form host:port', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_USERNAME', 'Username for the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_PASSWORD', 'Password for the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('NO_PROXY', 'Comma separated list of hosts and domains which are connected without the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;
