INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_length_min', 'Minimum number of characters in local user passwords', '4', '4')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_length_max', 'Maximum number of characters in local user passwords', '32', '32')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_lower_char_flag', 'Password has to have at least one lower alpha character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_upper_char_flag', 'Password has to have at least one upper alpha character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_digit_flag', 'Password has to have at least one digit', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_consecutive_char_flag', 'Password has to have no consecutive characters', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_special_char_flag', 'Password has to have at least a special character', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_restricted_occurrence_flag', 'Password has to have no repeating characters', 'false', 'false')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_max_occurrence', 'Maximum number of valid occurrence of a character', '1', '1')
ON CONFLICT (key) DO NOTHING;
INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('psw_check_special_characters', 'List of special characters to check in a password', '!$%&()*+,./:;<=>?[\\]^_{|}~', '!$%&()*+,./:;<=>?[\\]^_{|}~')
ON CONFLICT (key) DO NOTHING;
