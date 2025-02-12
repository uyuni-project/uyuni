INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MIN', 'Minimum number of characters in local user passwords', '4', '4')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LENGTH_MAX', 'Maximum number of characters in local user passwords', '32', '32')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_LOWER_CHAR_FLAG', 'Password has to have at least one lower alpha character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_UPPER_CHAR_FLAG', 'Password has to have at least one upper alpha character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_DIGIT_FLAG', 'Password has to have at least one digit', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_CONSECUTIVE_CHAR_FLAG', 'Password has to have no consecutive characters', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHAR_FLAG', 'Password has to have at least a special character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG', 'Password has to have no repeating characters', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_MAX_OCCURRENCE', 'Maximum number of valid occurrence of a character', '1', '1')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PSW_CHECK_SPECIAL_CHARACTERS', 'List of special characters to check in a password', '!$%&()*+,./:;<=>?[\\]^_{|}~', '!$%&()*+,./:;<=>?[\\]^_{|}~')
ON CONFLICT (key) DO NOTHING;
