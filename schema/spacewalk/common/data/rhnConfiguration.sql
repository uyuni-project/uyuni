INSERT INTO rhnConfiguration (key, description) VALUES ('extauth_default_orgid', 'Organization id, where externally authenticated users will be created.');
INSERT INTO rhnConfiguration (key, description, default_value) VALUES ('extauth_use_orgunit', 'Use Org. Unit IPA setting as organization name to create externally authenticated users in.', 'false');
INSERT INTO rhnConfiguration (key, description, default_value) VALUES ('extauth_keep_temproles', 'Keep temporary user roles granted due to the external authentication setup for subsequent logins using password.', 'false');
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('system_checkin_threshold', 'Number of days before reporting a system as inactive', null, 1);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_length_min', 'Minimum number of characters in local user passwords', null, 4);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_length_max', 'Maximum number of characters in local user passwords', null, 32);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_lower_char_flag', 'Password has to have at least one lower alpha character', null, 1);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_upper_char_flag', 'Password has to have at least one upper alpha character', null, 1);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_digit_flag', 'Password has to have at least one digit', null, 1);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_consecutive_char_flag', 'Password has to have no consecutive characters', null, 0);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_special_char_flag', 'Password has to have at least a special character', null, 0);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_restricted_occurrence_flag', 'Password has to have no repeating characters', null, 0);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_max_occurrence', 'Maximum number of valid occurrence of a character', null, 2);
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('password_check_special_characters', 'List of special characters to check in a password', null, '!$%&()*+,./:;<=>?[\\]^_{|}~');
