INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('AUTO_GENERATE_BOOTSTRAP_REPO', 'Regenerate the bootstrap repositories after a channel was synced', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('BOOTSTRAP_REPO_FLUSH', 'Remove existing bootstrap repositories before creating new ones', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('CHECKSUM_PRIORITY_LIST', 'Priority of package checksum types used by satellite-sync', null, 'sha512, sha384, sha256')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('DISALLOWED_SUFFIXES', 'Comma separated list of e-mail address suffixes not allowed for new users', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('DISALLOW_USER_CREATION', 'Disallow creating users during system registration', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('DISCONNECTED', 'Run the server in disconnected mode', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('ENABLE_NVREA', 'Use the full NEVRA to identify packages during import', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('ENABLE_SNAPSHOTS', 'Create system snapshots', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('FORCE_PACKAGE_UPLOAD', 'Allow overwriting packages with a forced package upload', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('FORWARD_REGISTRATION', 'Forward system registrations to SCC', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_ACTIONS_DISPLAY_LIMIT', 'Maximum number of actions shown in the action lists', null, '10000')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_BASE_DOMAIN', 'Domain used in the URL sent in the e-mail to new users', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_COBBLER_BOOTSTRAP_ARCH', 'Architecture of the Cobbler bare metal bootstrap image', null, 'x86_64')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_COBBLER_BOOTSTRAP_BREED', 'Breed of the Cobbler bare metal bootstrap image', null, 'suse')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_COBBLER_BOOTSTRAP_EXTRA_KERNEL_OPTIONS', 'Extra kernel options of the Cobbler bare metal bootstrap image', null, 'ROOTFS_FSCK=0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_COBBLER_BOOTSTRAP_INITRD', 'Initrd path of the Cobbler bare metal bootstrap image', null, '/srv/pxe-default-image/initrd0.img')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_COBBLER_BOOTSTRAP_KERNEL', 'Kernel path of the Cobbler bare metal bootstrap image', null, '/srv/pxe-default-image/vmlinuz0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_CONFIG_FILE_EDIT_SIZE', 'Maximum size in KB of a configuration file which can be edited in the web UI', null, '32')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_CUSTOM_FOOTER', 'Custom HTML footer shown in the web UI', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_CUSTOM_HEADER', 'Custom HTML header shown in the web UI', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_CVE_AUDIT_ENABLE_OVAL_METADATA', 'Use OVAL metadata for CVE audit', null, 'true')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_DISABLE_REMOTE_COMMANDS_FROM_UI', 'Disable running remote commands from the web UI and the API', null, 'false')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_DISABLE_SUPPORTDATA_UPLOAD', 'Disable uploading support data to SCC', null, 'false')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_DISABLE_UPDATE_STATUS', 'Do not show the update status of system groups', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_DOWNLOAD_URL_LIFETIME', 'Lifetime in minutes of package download URLs, 0 disables the expiration', null, '600')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_ERRATA_ADVISORY_MAP_CSV_DOWNLOAD_URL', 'URL to download the map of patch IDs, announcement IDs and advisory URLs', null, 'https://ftp.suse.com/pub/projects/security/advisory-map.csv')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_ERRATA_CACHE_COMPUTE_THRESHOLD', 'Number of servers above which the errata cache is computed for an organization asynchronously', null, '30')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_KIWI_OS_IMAGE_BUILDING_ENABLED', 'Enable building OS images with Kiwi', null, 'true')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_LEGAL_NOTE', 'Legal note shown on the login page', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_MAX_CHANGELOG_ENTRIES', 'Maximum number of changelog entries per package in the repository metadata, 0 means unlimited', null, '20')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_NON_EXPIRABLE_PACKAGE_URLS', 'Comma separated list of package names with download URLs which do not expire', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_NOTIFICATIONS_LIFETIME', 'Lifetime of notification messages in days', null, '30')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_NOTIFICATIONS_TYPE_DISABLED', 'Comma separated list of disabled notification types', null, 'ChannelSyncFinished')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_OVAL_DEFINITIONS_BULK_SIZE', 'Number of OVAL definitions parsed in bulk', null, '500')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_POWER_MANAGEMENT_TYPES', 'Comma separated list of power management types supported by Cobbler', null, 'ipmilan, ipmilanplus, redfish')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_REBOOT_DELAY', 'Minutes to wait before performing a system reboot', null, '3')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_BATCH_DELAY', 'Delay in seconds before a new Salt batch is scheduled', null, '1.0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_BATCH_SIZE', 'Maximum number of minions executing a single action concurrently', null, '200')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_CHECK_DOWNLOAD_TOKENS', 'Check with tokens that files requested by a minion are accessible by that minion', null, 'true')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_CONTENT_STAGING_ADVANCE', 'Advance time in hours for the content staging window to open before the scheduled installation', null, '8')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_CONTENT_STAGING_WINDOW', 'Duration in hours of the time window for minions to stage packages before the scheduled installation', null, '8')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_PRESENCE_PING_GATHER_JOB_TIMEOUT', 'Timeout in seconds for gathering the presence ping jobs of Salt batch calls', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SALT_PRESENCE_PING_TIMEOUT', 'Timeout in seconds of the presence ping of Salt batch calls', null, '4')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SANDBOX_LIFETIME', 'Lifetime in days of configuration channel sandbox files', null, '3')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_SERVER_SATELLITE_TINY_URL_TIMEOUT', 'Lifetime in hours of tiny URLs', null, '4')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('JAVA_TASKOMATIC_COBBLER_USER', 'User used by Taskomatic to authenticate against Cobbler', null, 'taskomatic_user')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('KICKSTART_PACKAGES', 'Comma separated list of package names identifying kickstart trees', null, 'salt')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('PROXY_AUTH_TIMEOUT', 'Lifetime in seconds of proxy authentication tokens', null, '21600.0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REG_BATCH_SIZE', 'Number of systems forwarded to SCC in one batch', null, '200')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REG_ERROR_EXPIRE_TIME', 'Hours before a failed SCC registration is retried', null, '168')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_DOWNLOAD_THREADS', 'Number of parallel download threads of reposync', null, '5')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_MINRATE', 'Minimal download rate in bytes per second of reposync before a download is aborted', null, '1000')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_NEVRA_FILTER', 'Use the full NEVRA for reposync filters', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_TIMEOUT', 'Timeout in seconds of reposync downloads', null, '300')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('RESET_BASE_CHANNEL', 'Reset the base channel when a system changes its product', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SESSION_LIFETIME', 'Lifetime in seconds of client sessions', null, '1800')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYNC_SOURCE_PACKAGES', 'Synchronize source packages', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYNC_TO_TEMP', 'Download packages to a temporary file during satellite-sync', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('TEMP_TOKEN_LIFETIME', 'Lifetime in minutes of temporary download tokens', null, '60')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('TOKEN_LIFETIME', 'Lifetime in minutes of channel access tokens', null, '525600')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('TOKEN_REFRESH_AUTO_DEPLOY', 'Deploy refreshed channel access tokens automatically', null, 'true')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('UPDATE_HOST_DOMAIN', 'Domain of the official update host', null, '.suse.com')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('USE_TASKOMATIC_REPOMD', 'Use the repository metadata generated by Taskomatic', null, '1')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_CONFIG_DELIM_END', 'End delimiter of macros in configuration files', null, '|}')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_CONFIG_DELIM_START', 'Start delimiter of macros in configuration files', null, '{|')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_DEFAULT_MAIL_FROM', 'Sender address of e-mails sent by the server', null, 'Uyuni <dev-null@localhost>')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_DISALLOWED_MAIL_DOMAINS', 'Comma separated list of mail domains no e-mail is sent to', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_MAXIMUM_CONFIG_FILE_SIZE', 'Maximum size in bytes of configuration files', null, '131072')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_RESTRICT_MAIL_DOMAINS', 'Comma separated list of mail domains e-mails are restricted to', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_SESSION_DATABASE_LIFETIME', 'Lifetime in seconds of web UI and API sessions', null, '3600')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_SPA_ENABLE', 'Enable single page application navigation in the web UI', null, 'true')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_SPA_TIMEOUT', 'Timeout in seconds for single page application navigation', null, '60')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('WEB_SUBSCRIBE_PROXY_CHANNEL', 'Subscribe proxies automatically to the proxy channel', null, '0')
ON CONFLICT (key) DO NOTHING;
