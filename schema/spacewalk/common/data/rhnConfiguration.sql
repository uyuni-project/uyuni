INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_DEFAULT_ORGID', 'Organization id, where externally authenticated users will be created.', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_USE_ORGUNIT', 'Use Org. Unit IPA setting as organization name to create externally authenticated users in.', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_KEEP_TEMPROLES', 'Keep temporary user roles granted due to the external authentication setup for subsequent logins using password.', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYSTEM_CHECKIN_THRESHOLD', 'Number of days before reporting a system as inactive', null, '1');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MIN', 'Minimum number of characters in local user passwords', null, '4');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MAX', 'Maximum number of characters in local user passwords', null, '32');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LOWER_CHAR_FLAG', 'psw has to have at least one lower alpha character', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_UPPER_CHAR_FLAG', 'Password has to have at least one upper alpha character', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_DIGIT_FLAG', 'Password has to have at least one digit', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_CONSECUTIVE_CHAR_FLAG', 'Password has to have no consecutive characters', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHAR_FLAG', 'Password has to have at least a special character', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG', 'Password has to have no repeating characters', null, 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_MAX_OCCURRENCE', 'Maximum number of valid occurrence of a character', null, '2');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHARACTERS', 'List of special characters to check in a password', null, '!$%&()*+,./:;<=>?[]^_{|}~');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY', 'HTTP proxy used by the server to connect to the internet in the form host:port', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_USERNAME', 'Username for the HTTP proxy', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_PASSWORD', 'Password for the HTTP proxy', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('NO_PROXY', 'Comma separated list of hosts and domains which are connected without the HTTP proxy', null, null);
