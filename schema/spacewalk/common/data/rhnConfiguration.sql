-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

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

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_LOGIN_LENGTH_MIN', 'Minimum number of characters in user login names', null, '3');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_LOGIN_LENGTH_MAX', 'Maximum number of characters in user login names', null, '64');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USER_EMAIL_LENGTH_MAX', 'Maximum number of characters in user e-mail addresses', null, '128');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_DOWNLOAD_THREADS', 'Number of threads used by reposync to download packages', null, '5');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_TIMEOUT', 'Timeout of reposync downloads in seconds', null, '300');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_MINRATE', 'Minimal transfer rate of reposync downloads in bytes per second', null, '1000');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_NEVRA_FILTER', 'Filter the packages of a repository by their full NEVRA instead of the name only', null, '0');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYNC_SOURCE_PACKAGES', 'Synchronize source packages with reposync', null, '0');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('URLGRABBER_LOGSPEC', 'urlgrabber debug logging of reposync in the form level,filename (for example 1,-)', null, null);

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('BUFFER_SIZE', 'Size in bytes of the chunks in which the server streams files like packages to clients', null, '16384');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('FORCE_PACKAGE_UPLOAD', 'Allow mgr-push to force the upload of packages which already exist on the server', null, '0');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PROXY_AUTH_TIMEOUT', 'Lifetime in seconds of the authentication tokens which proxies get when they log in', null, '21600');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PUSH_SESSION_LIFETIME', 'Lifetime in seconds of the sessions which mgr-push gets when it logs in to upload packages', null, '1800');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_URL', 'URL of the SUSE Customer Center (SCC) API', null, 'https://scc.suse.com');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_UPDATE_HOST_DOMAIN', 'Domain of the official update server of the SUSE Customer Center (SCC)', null, '.suse.com');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_FORWARD_REGISTRATION', 'Forward the registrations of clients to the SUSE Customer Center (SCC)', null, '1');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_REG_ERROR_EXPIRE_TIME', 'Hours until a failed registration of a client is sent to the SUSE Customer Center (SCC) again', null, '168');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_REG_BATCH_SIZE', 'Number of systems sent to the SUSE Customer Center (SCC) in one registration call', null, '200');

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SCC_MIRROR_URL', 'URL of an internal mirror to load the repositories from instead of the SUSE Customer Center (SCC), when it provides them', null, null);
