INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_LOGIN_LENGTH_MIN', 'Minimum number of characters in user login names', null, '3')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_LOGIN_LENGTH_MAX', 'Maximum number of characters in user login names', null, '64')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_EMAIL_LENGTH_MAX', 'Maximum number of characters in user e-mail addresses', null, '128')
ON CONFLICT (key) DO NOTHING;
