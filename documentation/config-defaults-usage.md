# Usage of configuration defaults

This file lists every key defined in the configuration defaults installed to
`/usr/share/rhn/config-defaults/` and one place in the code where each key is
read. Where several places read the same key, only one representative
reference is listed. Line numbers refer to the state of the
`drop-unused-config-defaults` branch.

## How keys are resolved

- **Java** (`com.redhat.rhn.common.conf.Config`): every key of
  `rhn_<ns>.conf` is stored with the prefix derived from the file name
  (`rhn_server_susemanager.conf` -> `server.susemanager.`), unless the key already
  starts with it. A lookup with a namespace (`web.foo`) checks the bare key and
  then the namespaced key; a lookup without one (`foo`) also falls back to
  `web.foo` and `server.foo`. When the code uses a `ConfigDefaults`
  constant, the reference points to where that constant is read.
- **Python** (`spacewalk.common.rhnConfig`): `initCFG("server.app")` merges
  `rhn.conf`, `rhn_server.conf` and `rhn_server_app.conf`, so keys are read
  as plain attributes (`CFG.LOG_FILE`). Keys like `log_file` and `debug` that
  appear in several files are read by the same line, which picks up whichever
  component is active.
- **Shell**: the `*_daemon.conf` files are sourced by the launcher scripts,
  and `spacewalk-startup-helper` greps the schema version keys directly.

| Config file | Key name | Code reference |
|---|---|---|
| rhn.conf | `product_name` | [`python/spacewalk/common/rhnMail.py:28`](python/spacewalk/common/rhnMail.py#L28) |
| rhn.conf | `quiet_mail` | [`python/spacewalk/common/rhnTB.py:133`](python/spacewalk/common/rhnTB.py#L133) |
| rhn.conf | `debug` | [`python/spacewalk/server/apacheHandler.py:75`](python/spacewalk/server/apacheHandler.py#L75) |
| rhn.conf | `traceback_mail` | [`python/spacewalk/common/rhnMail.py:29`](python/spacewalk/common/rhnMail.py#L29) |
| rhn.conf | `log_file` | [`python/spacewalk/server/apacheHandler.py:75`](python/spacewalk/server/apacheHandler.py#L75) |
| rhn.conf | `enable_snapshots` | [`python/spacewalk/server/rhnServer/server_lib.py:269`](python/spacewalk/server/rhnServer/server_lib.py#L269) |
| rhn.conf | `mount_point` | [`python/spacewalk/server/handlers/app/packages.py:579`](python/spacewalk/server/handlers/app/packages.py#L579) |
| rhn.conf | `default_mail_from` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:73`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L73) |
| rhn.conf | `db_ssl_enabled` | [`python/spacewalk/server/rhnSQL/__init__.py:134`](python/spacewalk/server/rhnSQL/__init__.py#L134) |
| rhn.conf | `db_sslrootcert` | [`python/spacewalk/server/rhnSQL/__init__.py:136`](python/spacewalk/server/rhnSQL/__init__.py#L136) |
| rhn.conf | `documentroot` | [`spacewalk/certs-tools/rhn_bootstrap.py:77`](spacewalk/certs-tools/rhn_bootstrap.py#L77) |
| rhn.conf | `httpd_config_dir` | [`spacewalk/setup/bin/spacewalk-setup:120`](spacewalk/setup/bin/spacewalk-setup#L120) |
| rhn.conf | `httpd_group` | [`python/spacewalk/satellite_tools/disk_dumper/dumper.py:855`](python/spacewalk/satellite_tools/disk_dumper/dumper.py#L855) |
| rhn.conf | `httpd_user` | [`python/spacewalk/satellite_tools/disk_dumper/dumper.py:854`](python/spacewalk/satellite_tools/disk_dumper/dumper.py#L854) |
| rhn.conf | `db_backend` | [`python/spacewalk/satellite_tools/updatePackages.py:722`](python/spacewalk/satellite_tools/updatePackages.py#L722) |
| rhn.conf | `db_user` | [`python/spacewalk/server/rhnSQL/__init__.py:132`](python/spacewalk/server/rhnSQL/__init__.py#L132) |
| rhn.conf | `db_password` | [`python/spacewalk/server/rhnSQL/__init__.py:133`](python/spacewalk/server/rhnSQL/__init__.py#L133) |
| rhn.conf | `db_name` | [`python/spacewalk/server/rhnSQL/__init__.py:131`](python/spacewalk/server/rhnSQL/__init__.py#L131) |
| rhn.conf | `db_host` | [`python/spacewalk/server/rhnSQL/__init__.py:129`](python/spacewalk/server/rhnSQL/__init__.py#L129) |
| rhn.conf | `db_port` | [`python/spacewalk/server/rhnSQL/__init__.py:130`](python/spacewalk/server/rhnSQL/__init__.py#L130) |
| rhn.conf | `report_db_backend` | [`python/spacewalk/server/rhnSQL/__init__.py:115`](python/spacewalk/server/rhnSQL/__init__.py#L115) |
| rhn.conf | `report_db_user` | [`python/spacewalk/server/rhnSQL/__init__.py:119`](python/spacewalk/server/rhnSQL/__init__.py#L119) |
| rhn.conf | `report_db_password` | [`python/spacewalk/server/rhnSQL/__init__.py:120`](python/spacewalk/server/rhnSQL/__init__.py#L120) |
| rhn.conf | `report_db_name` | [`python/spacewalk/server/rhnSQL/__init__.py:118`](python/spacewalk/server/rhnSQL/__init__.py#L118) |
| rhn.conf | `report_db_host` | [`python/spacewalk/server/rhnSQL/__init__.py:116`](python/spacewalk/server/rhnSQL/__init__.py#L116) |
| rhn.conf | `report_db_port` | [`python/spacewalk/server/rhnSQL/__init__.py:117`](python/spacewalk/server/rhnSQL/__init__.py#L117) |
| rhn.conf | `report_db_ssl_enabled` | [`python/spacewalk/server/rhnSQL/__init__.py:121`](python/spacewalk/server/rhnSQL/__init__.py#L121) |
| rhn.conf | `report_db_sslrootcert` | [`python/spacewalk/server/rhnSQL/__init__.py:123`](python/spacewalk/server/rhnSQL/__init__.py#L123) |
| rhn.conf | `sign_metadata` | [`python/spacewalk/server/rhnChannel.py:757`](python/spacewalk/server/rhnChannel.py#L757) |
| rhn.conf | `kickstart_packages` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:580`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L580) |
| rhn_hibernate.conf | `hibernate.connection.driver_class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:83`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L83) |
| rhn_hibernate.conf | `hibernate.connection.driver_proto` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:950`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L950) |
| rhn_hibernate.conf | `hibernate.connection.provider_class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.use_outer_join` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.jdbc.batch_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.min_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.max_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.timeout` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.max_statements` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.idle_test_period` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.testConnectionOnCheckout` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.c3p0.preferredTestQuery` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.bytecode.use_reflection_optimizer` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.id.new_generator_mappings` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.cache.use_second_level_cache` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.cache.use_query_cache` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.cache.region.factory_class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.jakarta.cache.uri` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.jakarta.cache.provider` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.jakarta.cache.missing_cache_strategy` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_hibernate.conf | `hibernate.javax.cache.missing_cache_strategy` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java:34`](java/core/src/main/java/com/redhat/rhn/common/hibernate/DefaultConnectionManager.java#L34) |
| rhn_java.conf | `java.min_user_len` | [`java/webapp/src/main/webapp/WEB-INF/pages/admin/multiorg/orgcreate.jsp:57`](java/webapp/src/main/webapp/WEB-INF/pages/admin/multiorg/orgcreate.jsp#L57) |
| rhn_java.conf | `java.development_environment` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:171`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L171) |
| rhn_java.conf | `java.apiversion` | [`java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/api/ApiHandler.java:90`](java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/api/ApiHandler.java#L90) |
| rhn_java.conf | `java.sandbox_lifetime` | [`java/core/src/main/java/com/redhat/rhn/taskomatic/task/SandboxCleanup.java:45`](java/core/src/main/java/com/redhat/rhn/taskomatic/task/SandboxCleanup.java#L45) |
| rhn_java.conf | `java.server.satellite.tiny_url_timeout` | [`java/core/src/main/java/com/redhat/rhn/domain/common/CommonFactory.java:133`](java/core/src/main/java/com/redhat/rhn/domain/common/CommonFactory.java#L133) |
| rhn_java.conf | `java.errata_cache_compute_threshold` | [`java/core/src/main/java/com/redhat/rhn/manager/errata/cache/UpdateErrataCacheCommand.java:60`](java/core/src/main/java/com/redhat/rhn/manager/errata/cache/UpdateErrataCacheCommand.java#L60) |
| rhn_java.conf | `java.l10n_debug` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:302`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L302) |
| rhn_java.conf | `java.l10n_debug_marker` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:305`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L305) |
| rhn_java.conf | `java.l10n_missingmessage_exceptions` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:351`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L351) |
| rhn_java.conf | `java.supported_locales` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:136`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L136) |
| rhn_java.conf | `java.excluded_countries` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:570`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L570) |
| rhn_java.conf | `java.taskomatic_cobbler_user` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:591`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L591) |
| rhn_java.conf | `java.custom_header` | [`java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java:147`](java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java#L147) |
| rhn_java.conf | `java.custom_footer` | [`java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java:148`](java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java#L148) |
| rhn_java.conf | `java.legal_note` | [`java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java:149`](java/core/src/main/java/com/suse/manager/webui/controllers/login/LoginController.java#L149) |
| rhn_java.conf | `java.sc_crit` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:507`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L507) |
| rhn_java.conf | `java.sc_imp` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:515`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L515) |
| rhn_java.conf | `java.sc_mod` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:523`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L523) |
| rhn_java.conf | `java.sc_low` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:531`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L531) |
| rhn_java.conf | `java.sc_bug` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:539`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L539) |
| rhn_java.conf | `java.sc_enh` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:547`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L547) |
| rhn_java.conf | `java.taskomatic_channel_repodata_workers` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1046`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1046) |
| rhn_java.conf | `java.taskomatic_clmdiff_workers` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1050`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1050) |
| rhn_java.conf | `java.cobbler_bootstrap.kernel` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:884`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L884) |
| rhn_java.conf | `java.cobbler_bootstrap.initrd` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:892`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L892) |
| rhn_java.conf | `java.cobbler_bootstrap.breed` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:908`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L908) |
| rhn_java.conf | `java.cobbler_bootstrap.arch` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:916`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L916) |
| rhn_java.conf | `java.cobbler_bootstrap.extra_kernel_options` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:924`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L924) |
| rhn_java.conf | `java.power_management.types` | [`java/core/src/main/java/com/redhat/rhn/frontend/action/kickstart/PowerManagementAction.java:255`](java/core/src/main/java/com/redhat/rhn/frontend/action/kickstart/PowerManagementAction.java#L255) |
| rhn_java.conf | `java.config_file_edit_size` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1133`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1133) |
| rhn_java.conf | `java.vendor_name` | [`java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java:200`](java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java#L200) |
| rhn_java.conf | `java.enterprise_linux_name` | [`java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java:203`](java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java#L203) |
| rhn_java.conf | `java.vendor_service_name` | [`java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java:206`](java/core/src/main/java/com/redhat/rhn/common/localization/XmlMessages.java#L206) |
| rhn_java.conf | `java.base_domain` | [`java/core/src/main/java/com/redhat/rhn/frontend/events/NewUserEvent.java:83`](java/core/src/main/java/com/redhat/rhn/frontend/events/NewUserEvent.java#L83) |
| rhn_java.conf | `java.actions_display_limit` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1125`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1125) |
| rhn_java.conf | `java.smtp_server` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:65`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L65) |
| rhn_java.conf | `java.smtp_port` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:66`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L66) |
| rhn_java.conf | `java.smtp_auth` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:67`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L67) |
| rhn_java.conf | `java.smtp_ssl` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:68`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L68) |
| rhn_java.conf | `java.smtp_starttls` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:69`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L69) |
| rhn_java.conf | `java.smtp_user` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:71`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L71) |
| rhn_java.conf | `java.smtp_pass` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:72`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L72) |
| rhn_java.conf | `java.smtp_tls_protocols` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:70`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L70) |
| rhn_java.conf | `java.download_url_lifetime` | [`java/core/src/main/java/com/redhat/rhn/manager/download/DownloadManager.java:125`](java/core/src/main/java/com/redhat/rhn/manager/download/DownloadManager.java#L125) |
| rhn_java.conf | `java.non_expirable_package_urls` | [`java/core/src/main/java/com/redhat/rhn/manager/download/DownloadManager.java:56`](java/core/src/main/java/com/redhat/rhn/manager/download/DownloadManager.java#L56) |
| rhn_java.conf | `java.message_queue_thread_pool_size` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/MessageDispatcher.java:43`](java/core/src/main/java/com/redhat/rhn/common/messaging/MessageDispatcher.java#L43) |
| rhn_java.conf | `java.unify_custom_channel_management` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1332`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1332) |
| rhn_java.conf | `java.salt_content_staging_window` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1190`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1190) |
| rhn_java.conf | `java.salt_content_staging_advance` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1199`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1199) |
| rhn_java.conf | `java.salt_check_download_tokens` | [`java/core/src/main/java/com/suse/manager/webui/controllers/DownloadController.java:134`](java/core/src/main/java/com/suse/manager/webui/controllers/DownloadController.java#L134) |
| rhn_java.conf | `java.salt_event_thread_pool_size` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1223`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1223) |
| rhn_java.conf | `java.salt_presence_ping_timeout` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1148`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1148) |
| rhn_java.conf | `java.salt_presence_ping_gather_job_timeout` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1156`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1156) |
| rhn_java.conf | `java.salt_batch_size` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1163`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1163) |
| rhn_java.conf | `java.salt_batch_delay` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1170`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1170) |
| rhn_java.conf | `java.salt_events_per_commit` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1237`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1237) |
| rhn_java.conf | `java.salt_event_notification_poll_interval_ms` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1245`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1245) |
| rhn_java.conf | `java.salt_event_connection_watchdog_interval_seconds` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1254`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1254) |
| rhn_java.conf | `prometheus_monitoring_enabled` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1178`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1178) |
| rhn_java.conf | `java.salt_api_host` | [`java/core/src/main/java/com/suse/manager/webui/services/impl/SaltService.java:139`](java/core/src/main/java/com/suse/manager/webui/services/impl/SaltService.java#L139) |
| rhn_java.conf | `java.salt_api_port` | [`java/core/src/main/java/com/suse/manager/webui/services/impl/SaltService.java:141`](java/core/src/main/java/com/suse/manager/webui/services/impl/SaltService.java#L141) |
| rhn_java.conf | `java.kiwi_os_image_building_enabled` | [`java/core/src/main/java/com/redhat/rhn/domain/entitlement/OSImageBuildHostEntitlement.java:65`](java/core/src/main/java/com/redhat/rhn/domain/entitlement/OSImageBuildHostEntitlement.java#L65) |
| rhn_java.conf | `java.notifications_lifetime` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1215`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1215) |
| rhn_java.conf | `java.notifications_type_disabled` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1263`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1263) |
| rhn_java.conf | `java.mgr_sync_max_connections` | [`java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java:199`](java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java#L199) |
| rhn_java.conf | `java.max_changelog_entries` | [`java/core/src/main/java/com/redhat/rhn/taskomatic/task/repomd/OtherXmlWriter.java:125`](java/core/src/main/java/com/redhat/rhn/taskomatic/task/repomd/OtherXmlWriter.java#L125) |
| rhn_java.conf | `java.product_tree_tag` | [`java/core/src/main/java/com/redhat/rhn/domain/cloudpayg/PaygProductFactory.java:180`](java/core/src/main/java/com/redhat/rhn/domain/cloudpayg/PaygProductFactory.java#L180) |
| rhn_java.conf | `java.sso` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1271`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1271) |
| rhn_java.conf | `java.http_connection_timeout` | [`java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java:431`](java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java#L431) |
| rhn_java.conf | `java.http_socket_timeout` | [`java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java:439`](java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java#L439) |
| rhn_java.conf | `java.salt_api_http_socket_timeout` | [`java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java:447`](java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java#L447) |
| rhn_java.conf | `java.http_client_tls_protocols` | [`java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java:141`](java/core/src/main/java/com/redhat/rhn/common/util/http/HttpClientAdapter.java#L141) |
| rhn_java.conf | `taskomatic.sshminion_action_executor.parallel_threads` <sup>2</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/task/RhnJob.java:49`](java/core/src/main/java/com/redhat/rhn/taskomatic/task/RhnJob.java#L49) |
| rhn_java.conf | `java.min_schema_version` | [`spacewalk/admin/spacewalk-startup-helper:30`](spacewalk/admin/spacewalk-startup-helper#L30) |
| rhn_java.conf | `java.min_report_schema_version` | [`spacewalk/admin/spacewalk-startup-helper:43`](spacewalk/admin/spacewalk-startup-helper#L43) |
| rhn_java.conf | `java.disable_update_status` | [`java/core/src/main/java/com/redhat/rhn/manager/system/SystemManager.java:1244`](java/core/src/main/java/com/redhat/rhn/manager/system/SystemManager.java#L1244) |
| rhn_java.conf | `java.reboot_delay` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1357`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1357) |
| rhn_java.conf | `java.disable_remote_commands_from_ui` | [`java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/system/SystemHandler.java:5216`](java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/system/SystemHandler.java#L5216) |
| rhn_java.conf | `java.cve_audit.enable_oval_metadata` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1373`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1373) |
| rhn_java.conf | `java.oval_definitions_bulk_size` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1380`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1380) |
| rhn_java.conf | `java.disable_supportdata_upload` | [`java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/system/SystemHandler.java:9472`](java/core/src/main/java/com/redhat/rhn/frontend/xmlrpc/system/SystemHandler.java#L9472) |
| rhn_java.conf | `java.errata_advisory_map_csv_download_url` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1389`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1389) |
| rhn_java_sso.conf | `onelogin.saml2.strict` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.debug` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.entityid` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.assertion_consumer_service.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.assertion_consumer_service.binding` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.single_logout_service.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.single_logout_service.binding` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.nameidformat` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.x509cert` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.sp.privatekey` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.entityid` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.single_sign_on_service.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.single_sign_on_service.binding` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.single_logout_service.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.single_logout_service.response.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.single_logout_service.binding` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.idp.x509cert` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.nameid_encrypted` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.authnrequest_signed` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.logoutrequest_signed` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.logoutresponse_signed` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.want_messages_signed` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.want_assertions_signed` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.sign_metadata` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.want_assertions_encrypted` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.want_nameid_encrypted` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.want_xml_validation` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.security.signature_algorithm` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.organization.name` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.organization.displayname` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.organization.url` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.organization.lang` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.contacts.technical.given_name` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.contacts.technical.email_address` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.contacts.support.given_name` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_java_sso.conf | `onelogin.saml2.contacts.support.email_address` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java:60`](java/core/src/main/java/com/redhat/rhn/common/conf/sso/SSOConfig.java#L60) |
| rhn_org_quartz.conf | `org.quartz.scheduler.instanceName` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.scheduler.idleWaitTime` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.scheduler.rmi.export` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.scheduler.rmi.proxy` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.scheduler.wrapJobExecutionInUserTransaction` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.threadPool.class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.threadPool.threadCount` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.threadPool.threadPriority` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.jobStore.misfireThreshold` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.jobStore.class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_org_quartz.conf | `org.quartz.jobStore.tablePrefix` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/TaskoQuartzHelper.java:54`](java/core/src/main/java/com/redhat/rhn/taskomatic/TaskoQuartzHelper.java#L54) |
| rhn_org_quartz.conf | `org.quartz.scheduler.skipUpdateCheck` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java:73`](java/core/src/main/java/com/redhat/rhn/taskomatic/core/SchedulerKernel.java#L73) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.connection.driver_class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.connection.driver_proto` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:970`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L970) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.connection.provider_class` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.use_outer_join` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.jdbc.batch_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.min_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.max_size` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.timeout` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.max_statements` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.idle_test_period` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.testConnectionOnCheckout` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.c3p0.preferredTestQuery` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.bytecode.use_reflection_optimizer` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.id.new_generator_mappings` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_reporting_hibernate.conf | `reporting.hibernate.cache.use_second_level_cache` <sup>1</sup> | [`java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java:63`](java/core/src/main/java/com/redhat/rhn/common/hibernate/ReportDbConnectionManager.java#L63) |
| rhn_search.conf | `search.index_work_dir` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/DeleteIndexes.java:99`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/DeleteIndexes.java#L99) |
| rhn_search.conf | `search.rpc_handlers` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/rpc/RpcServer.java:74`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/rpc/RpcServer.java#L74) |
| rhn_search.conf | `search.connection.driver_class` | [`search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml:18`](search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml#L18) |
| rhn_search.conf | `search.connection.max_idle_time` | [`search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml:20`](search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml#L20) |
| rhn_search.conf | `search.connection.max_idle_time_excess_connections` | [`search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml:21`](search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml#L21) |
| rhn_search.conf | `search.score_threshold` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:95`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L95) |
| rhn_search.conf | `search.system_score_threshold` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:96`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L96) |
| rhn_search.conf | `search.errata_score_threshold` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:97`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L97) |
| rhn_search.conf | `search.errata.advisory_score_threshold` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:99`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L99) |
| rhn_search.conf | `search.min_ngram` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:100`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L100) |
| rhn_search.conf | `search.max_ngram` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:101`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L101) |
| rhn_search.conf | `search.doc.limit_results` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:102`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L102) |
| rhn_search.conf | `search.schedule.interval` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/scheduler/ScheduleManager.java:122`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/scheduler/ScheduleManager.java#L122) |
| rhn_search.conf | `search.log.explain.results` | [`search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java:103`](search-server/spacewalk-search/src/java/com/redhat/satellite/search/index/IndexManager.java#L103) |
| rhn_search.conf | `search.connection.maxPoolSize` | [`search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml:24`](search-server/spacewalk-search/src/config/com/redhat/satellite/search/db/config.xml#L24) |
| rhn_search_daemon.conf | `SEARCH_LIBRARY_PATH` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:19`](search-server/spacewalk-search/src/config/rhn-search#L19) |
| rhn_search_daemon.conf | `SEARCH_RHN_CLASSPATH` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:16`](search-server/spacewalk-search/src/config/rhn-search#L16) |
| rhn_search_daemon.conf | `SEARCH_RHN_JARS` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:16`](search-server/spacewalk-search/src/config/rhn-search#L16) |
| rhn_search_daemon.conf | `SEARCH_JARS` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:16`](search-server/spacewalk-search/src/config/rhn-search#L16) |
| rhn_search_daemon.conf | `SEARCH_INIT_MEMORY` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:17`](search-server/spacewalk-search/src/config/rhn-search#L17) |
| rhn_search_daemon.conf | `SEARCH_MAX_MEMORY` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:17`](search-server/spacewalk-search/src/config/rhn-search#L17) |
| rhn_search_daemon.conf | `JAVA_OPTS` <sup>3</sup> | [`search-server/spacewalk-search/src/config/rhn-search:19`](search-server/spacewalk-search/src/config/rhn-search#L19) |
| rhn_server_app.conf | `log_file` | [`python/spacewalk/server/apacheHandler.py:75`](python/spacewalk/server/apacheHandler.py#L75) |
| rhn_server.conf | `log_file` | [`python/spacewalk/server/apacheHandler.py:75`](python/spacewalk/server/apacheHandler.py#L75) |
| rhn_server.conf | `buffer_size` | [`python/spacewalk/server/apacheRequest.py:330`](python/spacewalk/server/apacheRequest.py#L330) |
| rhn_server.conf | `force_package_upload` | [`python/spacewalk/server/rhnPackageUpload.py:73`](python/spacewalk/server/rhnPackageUpload.py#L73) |
| rhn_server.conf | `proxy_auth_timeout` | [`python/spacewalk/server/handlers/xmlrpc/proxy.py:176`](python/spacewalk/server/handlers/xmlrpc/proxy.py#L176) |
| rhn_server.conf | `min_schema_version` | [`spacewalk/admin/spacewalk-startup-helper:36`](spacewalk/admin/spacewalk-startup-helper#L36) |
| rhn_server.conf | `min_user_len` | [`python/spacewalk/server/rhnUser.py:551`](python/spacewalk/server/rhnUser.py#L551) |
| rhn_server.conf | `max_user_len` | [`python/spacewalk/server/rhnUser.py:555`](python/spacewalk/server/rhnUser.py#L555) |
| rhn_server.conf | `max_email_len` | [`python/spacewalk/server/rhnUser.py:590`](python/spacewalk/server/rhnUser.py#L590) |
| rhn_server.conf | `disallowed_suffixes` | [`python/spacewalk/server/rhnUser.py:722`](python/spacewalk/server/rhnUser.py#L722) |
| rhn_server.conf | `nls_lang` | [`web/modules/rhn/RHN/DBI.pm:49`](web/modules/rhn/RHN/DBI.pm#L49) |
| rhn_server.conf | `disallow_user_creation` | [`python/spacewalk/server/rhnUser.py:410`](python/spacewalk/server/rhnUser.py#L410) |
| rhn_server.conf | `encrypted_passwords` | [`python/spacewalk/server/rhnUser.py:126`](python/spacewalk/server/rhnUser.py#L126) |
| rhn_server.conf | `prepended_dir` | [`python/spacewalk/server/rhnPackage.py:136`](python/spacewalk/server/rhnPackage.py#L136) |
| rhn_server.conf | `pam_auth_service` | [`python/spacewalk/server/rhnUser.py:94`](python/spacewalk/server/rhnUser.py#L94) |
| rhn_server.conf | `message_to_all` | [`python/spacewalk/server/apacheRequest.py:126`](python/spacewalk/server/apacheRequest.py#L126) |
| rhn_server.conf | `send_message_to_all` | [`python/spacewalk/server/apacheHandler.py:85`](python/spacewalk/server/apacheHandler.py#L85) |
| rhn_server.conf | `compress_headers` | [`python/spacewalk/server/rhnRepository.py:74`](python/spacewalk/server/rhnRepository.py#L74) |
| rhn_server.conf | `qos` | [`python/spacewalk/server/rhnHandler.py:48`](python/spacewalk/server/rhnHandler.py#L48) |
| rhn_server.conf | `throttle` | [`python/spacewalk/server/rhnHandler.py:46`](python/spacewalk/server/rhnHandler.py#L46) |
| rhn_server.conf | `session_lifetime` | [`python/spacewalk/server/rhnSession.py:50`](python/spacewalk/server/rhnSession.py#L50) |
| rhn_server.conf | `secret_key` | [`python/spacewalk/server/apacheAuth.py:94`](python/spacewalk/server/apacheAuth.py#L94) |
| rhn_server.conf | `reset_base_channel` | [`python/spacewalk/server/rhnServer/server_class.py:217`](python/spacewalk/server/rhnServer/server_class.py#L217) |
| rhn_server.conf | `repomd_path_prefix` | [`python/spacewalk/server/rhnRepository.py:249`](python/spacewalk/server/rhnRepository.py#L249) |
| rhn_server.conf | `use_taskomatic_repomd` | [`python/spacewalk/server/rhnRepository.py:284`](python/spacewalk/server/rhnRepository.py#L284) |
| rhn_server.conf | `checksum_priority_list` | [`python/spacewalk/satellite_tools/xmlSource.py:641`](python/spacewalk/satellite_tools/xmlSource.py#L641) |
| rhn_server.conf | `sync_source_packages` | [`python/spacewalk/satellite_tools/reposync.py:3169`](python/spacewalk/satellite_tools/reposync.py#L3169) |
| rhn_server.conf | `sync_cache_dir` | [`python/spacewalk/satellite_tools/syncCache.py:39`](python/spacewalk/satellite_tools/syncCache.py#L39) |
| rhn_server.conf | `reposync_download_threads` | [`python/spacewalk/satellite_tools/download.py:368`](python/spacewalk/satellite_tools/download.py#L368) |
| rhn_server.conf | `reposync_timeout` | [`python/spacewalk/satellite_tools/download.py:377`](python/spacewalk/satellite_tools/download.py#L377) |
| rhn_server.conf | `reposync_minrate` | [`python/spacewalk/satellite_tools/download.py:386`](python/spacewalk/satellite_tools/download.py#L386) |
| rhn_server.conf | `reposync_nevra_filter` | [`python/spacewalk/satellite_tools/repo_plugins/deb_src.py:449`](python/spacewalk/satellite_tools/repo_plugins/deb_src.py#L449) |
| rhn_server.conf | `urlgrabber_logspec` | [`python/spacewalk/satellite_tools/repo_plugins/yum_src.py:1654`](python/spacewalk/satellite_tools/repo_plugins/yum_src.py#L1654) |
| rhn_server_susemanager.conf | `scc_url` | [`java/core/src/main/java/com/suse/scc/SCCTaskManager.java:132`](java/core/src/main/java/com/suse/scc/SCCTaskManager.java#L132) |
| rhn_server_susemanager.conf | `update_host_domain` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1323`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1323) |
| rhn_server_susemanager.conf | `forward_registration` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1316`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1316) |
| rhn_server_susemanager.conf | `reg_error_expire_time` | [`java/core/src/main/java/com/redhat/rhn/domain/scc/SCCCachingFactory.java:546`](java/core/src/main/java/com/redhat/rhn/domain/scc/SCCCachingFactory.java#L546) |
| rhn_server_susemanager.conf | `reg_batch_size` | [`java/core/src/main/java/com/suse/scc/SCCSystemRegistrationManager.java:104`](java/core/src/main/java/com/suse/scc/SCCSystemRegistrationManager.java#L104) |
| rhn_server_susemanager.conf | `fromdir` | [`java/core/src/main/java/com/redhat/rhn/manager/content/ContentSyncManager.java:893`](java/core/src/main/java/com/redhat/rhn/manager/content/ContentSyncManager.java#L893) |
| rhn_server_susemanager.conf | `mirror` | [`java/core/src/main/java/com/redhat/rhn/manager/content/ContentSyncManager.java:1452`](java/core/src/main/java/com/redhat/rhn/manager/content/ContentSyncManager.java#L1452) |
| rhn_server_susemanager.conf | `temp_token_lifetime` | [`java/core/src/main/java/com/redhat/rhn/domain/action/salt/build/ImageBuildAction.java:231`](java/core/src/main/java/com/redhat/rhn/domain/action/salt/build/ImageBuildAction.java#L231) |
| rhn_server_susemanager.conf | `token_lifetime` | [`java/core/src/main/java/com/suse/manager/webui/utils/token/AbstractTokenBuilder.java:36`](java/core/src/main/java/com/suse/manager/webui/utils/token/AbstractTokenBuilder.java#L36) |
| rhn_server_susemanager.conf | `token_refresh_auto_deploy` | [`java/core/src/main/java/com/redhat/rhn/taskomatic/task/TokenCleanup.java:74`](java/core/src/main/java/com/redhat/rhn/taskomatic/task/TokenCleanup.java#L74) |
| rhn_server_susemanager.conf | `bootstrap_repo_flush` | [`susemanager/src/mgr-create-bootstrap-repo:411`](susemanager/src/mgr-create-bootstrap-repo#L411) |
| rhn_server_susemanager.conf | `bootstrap_repo_datamodule` | [`susemanager/src/mgr-create-bootstrap-repo:431`](susemanager/src/mgr-create-bootstrap-repo#L431) |
| rhn_server_susemanager.conf | `auto_generate_bootstrap_repo` | [`python/spacewalk/satellite_tools/reposync.py:936`](python/spacewalk/satellite_tools/reposync.py#L936) |
| rhn_server_upload_package-push.conf | `log_file` | [`python/spacewalk/server/apacheUploadServer.py:47`](python/spacewalk/server/apacheUploadServer.py#L47) |
| rhn_taskomatic_daemon.conf | `TASKO_LIBRARY_PATH` <sup>3</sup> | [`java/scripts/taskomatic:17`](java/scripts/taskomatic#L17) |
| rhn_taskomatic_daemon.conf | `TASKO_RHN_CLASSPATH` <sup>3</sup> | [`java/scripts/taskomatic:14`](java/scripts/taskomatic#L14) |
| rhn_taskomatic_daemon.conf | `TASKO_RHN_JARS` <sup>3</sup> | [`java/scripts/taskomatic:14`](java/scripts/taskomatic#L14) |
| rhn_taskomatic_daemon.conf | `TASKO_JARS` <sup>3</sup> | [`java/scripts/taskomatic:14`](java/scripts/taskomatic#L14) |
| rhn_taskomatic_daemon.conf | `TASKO_JAVA_OPTS` <sup>3</sup> | [`java/scripts/taskomatic:13`](java/scripts/taskomatic#L13) |
| rhn_taskomatic_daemon.conf | `TASKO_INIT_MEMORY` <sup>3</sup> | [`java/scripts/taskomatic:13`](java/scripts/taskomatic#L13) |
| rhn_taskomatic_daemon.conf | `TASKO_MAX_MEMORY` <sup>3</sup> | [`java/scripts/taskomatic:13`](java/scripts/taskomatic#L13) |
| rhn_web.conf | `web.disallowed_mail_domains` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:55`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L55) |
| rhn_web.conf | `web.encrypted_passwords` | [`java/core/src/main/java/com/redhat/rhn/domain/user/legacy/UserImpl.java:266`](java/core/src/main/java/com/redhat/rhn/domain/user/legacy/UserImpl.java#L266) |
| rhn_web.conf | `web.max_user_len` | [`java/core/src/main/java/com/redhat/rhn/common/conf/UserDefaults.java:38`](java/core/src/main/java/com/redhat/rhn/common/conf/UserDefaults.java#L38) |
| rhn_web.conf | `web.kickstart_mount_point` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:567`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L567) |
| rhn_web.conf | `web.pam_auth_service` | [`java/core/src/main/java/com/redhat/rhn/domain/user/legacy/UserImpl.java:463`](java/core/src/main/java/com/redhat/rhn/domain/user/legacy/UserImpl.java#L463) |
| rhn_web.conf | `web.restrict_mail_domains` | [`java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java:56`](java/core/src/main/java/com/redhat/rhn/common/messaging/SmtpMail.java#L56) |
| rhn_web.conf | `web.satellite` | [`susemanager-utils/susemanager-sls/src/grains/mgr_server.py:41`](susemanager-utils/susemanager-sls/src/grains/mgr_server.py#L41) |
| rhn_web.conf | `web.session_database_lifetime` | [`java/core/src/main/java/com/redhat/rhn/common/security/PermissionException.java:129`](java/core/src/main/java/com/redhat/rhn/common/security/PermissionException.java#L129) |
| rhn_web.conf | `web.session_secret_1` | [`java/core/src/main/java/com/redhat/rhn/frontend/integration/IntegrationService.java:98`](java/core/src/main/java/com/redhat/rhn/frontend/integration/IntegrationService.java#L98) |
| rhn_web.conf | `web.session_secret_2` | [`java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java:142`](java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java#L142) |
| rhn_web.conf | `web.session_secret_3` | [`java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java:146`](java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java#L146) |
| rhn_web.conf | `web.session_secret_4` | [`java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java:148`](java/core/src/main/java/com/redhat/rhn/manager/session/SessionManager.java#L148) |
| rhn_web.conf | `web.session_swap_secret_1` | [`java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java:111`](java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java#L111) |
| rhn_web.conf | `web.session_swap_secret_2` | [`java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java:113`](java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java#L113) |
| rhn_web.conf | `web.session_swap_secret_3` | [`java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java:117`](java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java#L117) |
| rhn_web.conf | `web.session_swap_secret_4` | [`java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java:119`](java/core/src/main/java/com/redhat/rhn/common/security/SessionSwap.java#L119) |
| rhn_web.conf | `web.traceback_mail` | [`java/core/src/main/java/com/redhat/rhn/frontend/events/TraceBackAction.java:51`](java/core/src/main/java/com/redhat/rhn/frontend/events/TraceBackAction.java#L51) |
| rhn_web.conf | `web.config_delim_start` | [`java/core/src/main/java/com/redhat/rhn/frontend/action/configuration/ConfigFileForm.java:94`](java/core/src/main/java/com/redhat/rhn/frontend/action/configuration/ConfigFileForm.java#L94) |
| rhn_web.conf | `web.config_delim_end` | [`java/core/src/main/java/com/redhat/rhn/frontend/action/configuration/ConfigFileForm.java:96`](java/core/src/main/java/com/redhat/rhn/frontend/action/configuration/ConfigFileForm.java#L96) |
| rhn_web.conf | `web.version` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:829`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L829) |
| rhn_web.conf | `web.version.uyuni` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:827`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L827) |
| rhn_web.conf | `web.version.eol` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:839`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L839) |
| rhn_web.conf | `web.buildtimestamp` | [`java/core/src/main/java/com/suse/manager/webui/utils/SparkApplicationHelper.java:523`](java/core/src/main/java/com/suse/manager/webui/utils/SparkApplicationHelper.java#L523) |
| rhn_web.conf | `web.maximum_config_file_size` | [`java/core/src/main/java/com/redhat/rhn/domain/config/ConfigFile.java:141`](java/core/src/main/java/com/redhat/rhn/domain/config/ConfigFile.java#L141) |
| rhn_web.conf | `web.l10n_resourcebundles` | [`java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java:104`](java/core/src/main/java/com/redhat/rhn/common/localization/LocalizationService.java#L104) |
| rhn_web.conf | `server.nls_lang` | [`web/modules/rhn/RHN/DBI.pm:49`](web/modules/rhn/RHN/DBI.pm#L49) |
| rhn_web.conf | `web.subscribe_proxy_channel` | [`java/core/src/main/java/com/redhat/rhn/manager/system/SystemManager.java:2053`](java/core/src/main/java/com/redhat/rhn/manager/system/SystemManager.java#L2053) |
| rhn_web.conf | `ssh_push_port_https` | [`java/core/src/main/java/com/suse/manager/webui/controllers/bootstrap/AbstractMinionBootstrapper.java:328`](java/core/src/main/java/com/suse/manager/webui/controllers/bootstrap/AbstractMinionBootstrapper.java#L328) |
| rhn_web.conf | `ssh_push_sudo_user` | [`java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java:192`](java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java#L192) |
| rhn_web.conf | `ssh_salt_pre_flight_script` | [`java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java:201`](java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java#L201) |
| rhn_web.conf | `ssh_use_salt_thin` | [`java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java:210`](java/core/src/main/java/com/suse/manager/webui/services/impl/SaltSSHService.java#L210) |
| rhn_web.conf | `web.spa.enable` | [`java/webapp/src/main/webapp/WEB-INF/decorators/layout_c.jsp:61`](java/webapp/src/main/webapp/WEB-INF/decorators/layout_c.jsp#L61) |
| rhn_web.conf | `web.spa.timeout` | [`java/webapp/src/main/webapp/WEB-INF/decorators/layout_c.jsp:62`](java/webapp/src/main/webapp/WEB-INF/decorators/layout_c.jsp#L62) |
| rhn_web.conf | `web.locale` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1280`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1280) |
| rhn_web.conf | `web.docs_locale` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1289`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1289) |
| rhn_web.conf | `web.themes` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1298`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1298) |
| rhn_web.conf | `web.theme_default` | [`java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java:1307`](java/core/src/main/java/com/redhat/rhn/common/conf/ConfigDefaults.java#L1307) |

`rhn_server_upload.conf` defines no keys.

<sup>1</sup> Passed as a whole namespace to Hibernate, Quartz or the OneLogin
SAML library via `Config.getNamespaceProperties()`; the library itself reads
the individual keys.

<sup>2</sup> The key is built at runtime from the job's config namespace
(`taskomatic.<namespace>.parallel_threads`).

<sup>3</sup> Shell variable sourced and used by the daemon launcher script.
