INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_DEFAULT_ORGID', 'Organization id, where externally authenticated users will be created.', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_USE_ORGUNIT', 'Use Org. Unit IPA setting as organization name to create externally authenticated users in.', 'false', 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('EXTAUTH_KEEP_TEMPROLES', 'Keep temporary user roles granted due to the external authentication setup for subsequent logins using password.', 'false', 'false');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYSTEM_CHECKIN_THRESHOLD', 'Number of days before reporting a system as inactive', 1, 1);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MIN', 'Minimum number of characters in local user passwords', 4, 4);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MAX', 'Maximum number of characters in local user passwords', 32, 32);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LOWER_CHAR_FLAG', 'psw has to have at least one lower alpha character', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_UPPER_CHAR_FLAG', 'Password has to have at least one upper alpha character', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_DIGIT_FLAG', 'Password has to have at least one digit', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_CONSECUTIVE_CHAR_FLAG', 'Password has to have no consecutive characters', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHAR_FLAG', 'Password has to have at least a special character', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG', 'Password has to have no repeating characters', 0, 0);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_MAX_OCCURRENCE', 'Maximum number of valid occurrence of a character', 2, 2);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHARACTERS', 'List of special characters to check in a password', '!$%&()*+,./:;<=>?[]^_{|}~', '!$%&()*+,./:;<=>?[]^_{|}~');