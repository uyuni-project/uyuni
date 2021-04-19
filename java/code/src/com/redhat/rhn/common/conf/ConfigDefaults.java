/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.conf;

import com.redhat.rhn.common.validator.HostPortValidator;
import com.redhat.rhn.domain.kickstart.KickstartData;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * ConfigDefaults is the place to store application specific Config settings
 * and convenience methods.
 */
public class ConfigDefaults {

    private static ConfigDefaults instance = new ConfigDefaults();

    public static final List<String> SPACEWALK = Arrays.asList("Spacewalk", "Uyuni");
    //
    // Names of the configuration parameters
    //

    public static final String SYSTEM_CHECKIN_THRESHOLD = "web.system_checkin_threshold";
    public static final String WEB_DEFAULT_MAIL_FROM = "web.default_mail_from";
    public static final String WEB_ENCRYPTED_PASSWORDS = "web.encrypted_passwords";
    public static final String WEB_L10N_RESOURCEBUNDLES = "web.l10n_resourcebundles";
    public static final String WEB_PAM_AUTH_SERVICE = "web.pam_auth_service";
    public static final String WEB_SESSION_DATABASE_LIFETIME =
            "web.session_database_lifetime";

    public static final String WEB_SESSION_SECRET_1 = "web.session_secret_1";
    public static final String WEB_SESSION_SECRET_2 = "web.session_secret_2";
    public static final String WEB_SESSION_SECRET_3 = "web.session_secret_3";
    public static final String WEB_SESSION_SECRET_4 = "web.session_secret_4";

    public static final String WEB_SESSION_SWAP_SECRET_1 = "web.session_swap_secret_1";
    public static final String WEB_SESSION_SWAP_SECRET_2 = "web.session_swap_secret_2";
    public static final String WEB_SESSION_SWAP_SECRET_3 = "web.session_swap_secret_3";
    public static final String WEB_SESSION_SWAP_SECRET_4 = "web.session_swap_secret_4";

    public static final String WEB_SMTP_SERVER = "java.smtp_server";
    public static final String ERRATA_CACHE_COMPUTE_THRESHOLD
    = "errata_cache_compute_threshold";

    public static final String DOWNLOAD_URL_LIFETIME = "java.download_url_lifetime";

    public static final String NON_EXPIRABLE_PACKAGE_URLS =
        "java.non_expirable_package_urls";

    public static final String SATELLITE_PARENT = "server.satellite.rhn_parent";

    public static final String JABBER_SERVER = "server.jabber_server";

    public static final String KICKSTART_HOST = "kickstart_host";

    public static final String CONFIG_REVISION_MAX_SIZE = "web.maximum_config_file_size";

    public static final String WEB_EXCLUDED_COUNTRIES = "java.excluded_countries";

    public static final String DISCONNECTED = "disconnected";

    public static final String DEFAULT_SAT_PARENT = "satellite.rhn.redhat.com";

    public static final String PRODUCT_NAME = "web.product_name";
    public static final String VENDOR_NAME = "java.vendor_name";
    public static final String PRODUCT_VERSION_MGR = "web.version";
    public static final String PRODUCT_VERSION_UYUNI = "web.version.uyuni";
    public static final String ENTERPRISE_LINUX_NAME = "java.enterprise_linux_name";
    public static final String VENDOR_SERVICE_NAME = "java.vendor_service_name";

    private static final String COBBLER_AUTOMATED_USER = "java.taskomatic_cobbler_user";

    public static final String DOC_GETTING_STARTED_GUIDE = "docs.getting_started_guide";
    public static final String DOC_REFERENCE_GUIDE = "docs.reference_guide";
    public static final String DOC_BEST_PRACTICES_GUIDE = "docs.best_practices_guide";
    public static final String DOC_ADVANCED_TOPICS_GUIDE = "docs.advanced_topics_guide";
    public static final String DOC_RELEASE_NOTES = "docs.release_notes";

    public static final String WEB_SUBSCRIBE_PROXY_CHANNEL = "web.subscribe_proxy_channel";

    public static final String TAKE_SNAPSHOTS = "enable_snapshots";

    public static final String ACTIONS_DISPLAY_LIMIT = "java.actions_display_limit";

    public static final String CONFIG_FILE_EDIT_SIZE = "java.config_file_edit_size";

    /**
     * The default maximum size for config revisions,  (128 K)
     */
    public static final int DEFAULT_CONFIG_REVISION_MAX_SIZE = 131072;

    public static final String REPOMD_PATH_PREFIX = "taskomatic.repomd_path_prefix";

    public static final String REPOMD_CACHE_MOUNT_POINT = "repomd_cache_mount_point";

    //Comma separated names of possible kickstart packages
    private static final String POSSIBLE_KICKSTART_PACKAGE_NAMES = "spacewalk-koan,salt";

    private static final String KICKSTART_PACKAGE_NAMES = "kickstart_packages";

    public static final String MOUNT_POINT = "mount_point";
    public static final String KICKSTART_MOUNT_POINT = "kickstart_mount_point";

    public static final String PAGE_SIZES = "web.page_sizes";
    public static final String DEFAULT_PAGE_SIZE = "web.default_page_size";

    public static final String KICKSTART_COBBLER_DIR = "kickstart.cobbler.dir";
    public static final String COBBLER_SNIPPETS_DIR = "cobbler.snippets.dir";
    private static final String DEFAULT_COBBLER_SNIPPET_DIR = "/var/lib/cobbler/snippets";
    private static final String COBBLER_NAME_SEPARATOR = "cobbler.name.separator";
    public static final String POWER_MANAGEMENT_TYPES = "java.power_management.types";

    private static final String COBBLER_BOOTSTRAP_KERNEL = "java.cobbler_bootstrap.kernel";
    private static final String COBBLER_BOOTSTRAP_INITRD = "java.cobbler_bootstrap.initrd";
    private static final String COBBLER_BOOTSTRAP_BREED = "java.cobbler_bootstrap.breed";
    private static final String COBBLER_BOOTSTRAP_ARCH = "java.cobbler_bootstrap.arch";
    private static final String COBBLER_BOOTSTRAP_EXTRA_KERNEL_OPTIONS =
        "java.cobbler_bootstrap.extra_kernel_options";

    private static final String KVM_VIRT_PATH_DIR = "kickstart.virt_storage_path_kvm";
    private static final String XEN_VIRT_PATH_DIR = "kickstart.virt_storage_path_xen";
    private static final String DEFAULT_XEN_VIRT_PATH = "/var/lib/xen/images";
    private static final String DEFAULT_KVM_VIRT_PATH = "/var/lib/libvirt/images";
    private static final String VIRT_BRIDGE = "kickstart.virt_bridge";
    private static final String VIRT_MEM = "kickstart.virt_mem_size_mb";
    private static final String VIRT_CPU = "kickstart.virt_cpus";
    private static final String VIRT_DISK = "kickstart.virt_disk_size_gb";
    private static final String KICKSTART_NETWORK_INTERFACE = "kickstart.default_interface";

    public static final String SPACEWALK_REPOSYNC_PATH = "spacewalk_reposync_path";
    public static final String SPACEWALK_REPOSYNC_LOG_PATH = "spacewalk_reposync_logpath";
    private static final String USE_DB_REPODATA = "user_db_repodata";
    public static final String CONFIG_MACRO_ARGUMENT_REGEX = "config_macro_argument_regex";

    private static final String DB_BACKEND = "db_backend";
    private static final String DB_BACKEND_POSTGRESQL = "postgresql";
    public static final String DB_USER = "db_user";
    public static final String DB_PASSWORD = "db_password";
    public static final String DB_NAME = "db_name";
    public static final String DB_HOST = "db_host";
    public static final String DB_PORT = "db_port";
    private static final String DB_SSL_ENABLED = "db_ssl_enabled";
    private static final String DB_PROTO = "hibernate.connection.driver_proto";
    public static final String DB_CLASS = "hibernate.connection.driver_class";

    private static final String SSL_TRUSTSTORE = "java.ssl_truststore";

    public static final String LOOKUP_EXCEPT_SEND_EMAIL = "lookup_exception_email";

    public static final String KS_PARTITION_DEFAULT = "kickstart.partition.default";

    public static final String CONFIG_KEY_SUDO_USER = "ssh_push_sudo_user";

    /** Prometheus metric export flag */
    public static final String PROMETHEUS_MONITORING_ENABLED = "prometheus_monitoring_enabled";

    /**
     * System Currency defaults
     */
    private static final String SYSTEM_CURRENCY_CRIT = "java.sc_crit";
    private static final String SYSTEM_CURRENCY_IMP  = "java.sc_imp";
    private static final String SYSTEM_CURRENCY_MOD  = "java.sc_mod";
    private static final String SYSTEM_CURRENCY_LOW  = "java.sc_low";
    private static final String SYSTEM_CURRENCY_BUG  = "java.sc_bug";
    private static final String SYSTEM_CURRENCY_ENH  = "java.sc_enh";

    public static final String CHANGELOG_ENTRY_LIMIT = "java.max_changelog_entries";

    /**
     * Taskomatic defaults
     */
    private static final String TASKOMATIC_CHANNEL_REPODATA_WORKERS = "java.taskomatic_channel_repodata_workers";

    /**
     * HTTP proxy defaults
     */
    private static final String HTTP_PROXY = "server.satellite.http_proxy";
    private static final String HTTP_PROXY_USERNAME = "server.satellite.http_proxy_username";
    private static final String HTTP_PROXY_PASSWORD = "server.satellite.http_proxy_password";
    private static final int DEFAULT_HTTP_PROXY_PORT = 80;

    /**
     * SUSE Manager defaults
     */
    public static final String SCC_URL = "server.susemanager.scc_url";
    public static final String FORWARD_REGISTRATION = "server.susemanager.forward_registration";
    public static final String REG_ERROR_EXPIRE_TIME = "server.susemanager.reg_error_expire_time";
    public static final String SCC_BACKUP_SRV_USR = "server.susemanager.scc_backup_srv_usr";
    public static final String PRODUCT_TREE_TAG = "java.product_tree_tag";

    public static final String MESSAGE_QUEUE_THREAD_POOL_SIZE = "java.message_queue_thread_pool_size";

    /**
     * Token lifetime in seconds
     */
    public static final String TEMP_TOKEN_LIFETIME = "server.susemanager.temp_token_lifetime";
    public static final String TOKEN_LIFETIME = "server.susemanager.token_lifetime";
    /**
     * Controls if refreshed tokens get automatically deployed or not.
     */
    public static final String TOKEN_REFRESH_AUTO_DEPLOY = "server.susemanager.token_refresh_auto_deploy";

    public static final String SALT_SSH_CONNECT_TIMEOUT = "java.salt_ssh_connect_timeout";

    /**
     * Duration in hours of the time window for Salt minions to stage
     * packages in advance of scheduled installations or upgrades
     */
    public static final String SALT_CONTENT_STAGING_WINDOW = "java.salt_content_staging_window";

    /**
     * Advance time, in hours, for the content staging window to open with
     * respect to the scheduled installation/upgrade time
     */
    public static final String SALT_CONTENT_STAGING_ADVANCE = "java.salt_content_staging_advance";

    /**
     * If true, check via JWT tokens that files requested by a minion are actually accessible by that minion.
     * Turning this flag to false disables the checks.
     */
    public static final String SALT_CHECK_DOWNLOAD_TOKENS = "java.salt_check_download_tokens";

    /**
     * If true, Kiwi OS Image building feature preview will be enabled
     */
    public static final String KIWI_OS_IMAGE_BUILDING_ENABLED = "java.kiwi_os_image_building_enabled";

    /**
     * Lifetime of notification messages in days
     */
    public static final String NOTIFICATIONS_LIFETIME = "java.notifications_lifetime";

    /**
     * Notifications types to disable
     */
    public static final String NOTIFICATIONS_TYPE_DISABLED = "java.notifications_type_disabled";

    /**
     * Indicates the salt-api host to connect to (host
     */
    public static final String SALT_API_HOST = "java.salt_api_host";

    /**
     * Indicates the salt-api host to connect to (port)
     */
    public static final String SALT_API_PORT = "java.salt_api_port";

    /**
     * If true, signing metadata is enabled, otherwise metadata will not be signed
     */
    public static final String SIGN_METADATA = "sign_metadata";

    /**
     * Number of threads dedicated to processing Salt events.
     */
    public static final String SALT_EVENT_THREAD_POOL_SIZE = "java.salt_event_thread_pool_size";

    /**
     * Timeout in seconds of the presence ping performed in Salt Minions during salt batch calls
     */
    public static final String SALT_PRESENCE_PING_TIMEOUT = "java.salt_presence_ping_timeout";

    /**
     * Timeout in seconds for gathering the presence ping jobs performed in Salt Minions during salt batch calls
     */
    public static final String SALT_PRESENCE_PING_GATHER_JOB_TIMEOUT = "java.salt_presence_ping_gather_job_timeout";

    /**
     * Upper limit to the number of minions that execute a single Action concurrently. Lowering this value
     * prevents thundering-herd effects from Action execution but can decrease overall performance as the
     * overall level of parallelization is reduced. This is translated to the `--batch-size` Salt option.
     */
    public static final String SALT_BATCH_SIZE = "java.salt_batch_size";

    /**
     * Delay, in seconds, before a new batch is scheduled. After the first batch is scheduled with a size up to
     * java.salt_batch_size, subsequent batches will contain a smaller number of minions: the exact count will be
     * equal to the number of minions completing before java.salt_batch_delay expires.
     * Higher values will typically result in bigger batches with a lower CPU and I/O load on the Salt Master, while
     * smaller values will typically result in smaller batches with higher CPU and I/O load on the Salt Master.
     */
    public static final String SALT_BATCH_DELAY = "java.salt_batch_delay";

    /**
     * Maximum number of events processed before COMMITTing to the database. Raising this to any value above 1 will
     * decrease reliability, as failures will result in the loss of more events, but can improve performance in
     * high-scale scenarios.
     */
    public static final String SALT_EVENTS_PER_COMMIT = "java.salt_events_per_commit";

    /**
     * Single Sign-On associated config option name in rhn.conf
     */
    public static final String SINGLE_SIGN_ON_ENABLED = "java.sso";

    /**
     * List of distributions for which use salt for registration in kickstart
     */
    public static final String SALT_ENABLED_KICKSTART_INSTALL_TYPES = "salt_enabled_kickstart_install_types";

    /**
     * Specify if CaaSP nodes are system-locked by default
     */
    public static final String AUTOMATIC_SYSTEM_LOCK_CLUSTER_NODES_ENABLED = "java.automatic_system_lock_cluster_nodes";

    /**
     * Allows to publish erratas into the configured vendor channels via the api
     */
    public static final String ALLOW_ADDING_PATCHES_VIA_API = "java.allow_adding_patches_via_api";

    /**
     * Specify the list of web interface branded themes templates
     */
    public static final String WEB_THEMES = "web.themes";

    /**
     * Specify the default web interface branded theme template
     */
    public static final String WEB_THEME = "web.theme_default";

    /**
     * Specify the default language to use if user preferences are not available such as on the login page
     */
    public static final String DEFAULT_LOCALE = "web.locale";

    /**
     * Specify the default language to use for documentation if user
     * preferences are not available such as on the login page
     */
    public static final String DEFAULT_DOCS_LOCALE = "web.docs_locale";

    private ConfigDefaults() {
    }

    /**
     * Returns the System Currency multiplier for critical security errata
     * @return the System Currency multiplier for critical security errata
     */
    public Integer getSCCrit() {
        return Config.get().getInt(SYSTEM_CURRENCY_CRIT, 32);
    }

    /**
     * Returns the System Currency multiplier for important security errata
     * @return the System Currency multiplier for important security errata
     */
    public Integer getSCImp() {
        return Config.get().getInt(SYSTEM_CURRENCY_IMP, 16);
    }

    /**
     * Returns the System Currency multiplier for moderate security errata
     * @return the System Currency multiplier for moderate security errata
     */
    public Integer getSCMod() {
        return Config.get().getInt(SYSTEM_CURRENCY_MOD, 8);
    }

    /**
     * Returns the System Currency multiplier for low security errata
     * @return the System Currency multiplier for low security errata
     */
    public Integer getSCLow() {
        return Config.get().getInt(SYSTEM_CURRENCY_LOW, 4);
    }

    /**
     * Returns the System Currency multiplier for bug fix errata
     * @return the System Currency multiplier for bug fix errata
     */
    public Integer getSCBug() {
        return Config.get().getInt(SYSTEM_CURRENCY_BUG, 2);
    }

    /**
     * Returns the System Currency multiplier for enhancement errata
     * @return the System Currency multiplier for enhancement errata
     */
    public Integer getSCEnh() {
        return Config.get().getInt(SYSTEM_CURRENCY_ENH, 1);
    }

    /**
     * Get instance of ConfigDefaults.
     * @return ConfigDefaults instance.
     */
    public static ConfigDefaults get() {
        return instance;
    }

    /**
     * Return the kickstart mount point directory
     * Note the mount point is guaranteed to have a
     * '/' at the end of the string so you can use it
     * for appending sub directories.
     * @return the ks mount point directory.
     */
    public String getKickstartMountPoint() {
        String mount =  StringUtils.defaultIfEmpty(
                Config.get().getString(KICKSTART_MOUNT_POINT),
                Config.get().getString(MOUNT_POINT)).trim();
        if (!mount.endsWith("/")) {
            mount = mount + "/";
        }
        return mount;
    }

    /**
     * Returns the list of default kickstart packages names
     * @return the list of default kickstart packages names
     */
    public List<String> getKickstartPackageNames() {
        List<String> packageNames = Config.get().getList(KICKSTART_PACKAGE_NAMES);
        return packageNames.isEmpty() ?  Arrays.asList(POSSIBLE_KICKSTART_PACKAGE_NAMES.split(",")) : packageNames;
    }

    /**
     * Get the user string for use with authorization between Spacewalk
     * and Cobbler if there is no actual user in context.
     *
     * @return String from our config
     */
    public String getCobblerAutomatedUser() {
        return Config.get().getString(COBBLER_AUTOMATED_USER, "taskomatic_user");
    }

    /**
     * Returns all the available page sizes.
     * Note this is only meant to check
     * if the web.page_sizes config entry is set
     * you might want to use PageSizeDecorator.getPageSizes instead.
     * @see com.redhat.rhn.frontend.taglibs.list.decorators.PageSizeDecorator
     * for more info.
     * @return the comma separated list of page sizes or "".
     */
    public String getPageSizes() {
        return Config.get().getString(PAGE_SIZES, "");
    }

    /**
     * Returns the default page size config entry.
     * Note this is only meant to check
     * if the web.default_page_size config entry is set
     * you might want to use PageSizeDecorator.getDefaultPageSize instead.
     * @see com.redhat.rhn.frontend.taglibs.list.decorators.PageSizeDecorator
     * for more info.
     * @return the default page size config entry or "".
     */
    public String getDefaultPageSize() {
        return Config.get().getString(DEFAULT_PAGE_SIZE, "");
    }

    /**
     * Returns the directory which hosts all the
     * cobbler kickstart .cfg files..
     * All the .cfg files that have been generated
     * by spacewalk will be either at
     * ${kickstart.cobbler.dir}/wizard or
     * ${kickstart.cobbler.dir}/upload
     * @return the dir which has the kickstarts
     */
    public String getKickstartConfigDir() {
        return Config.get().getString(KICKSTART_COBBLER_DIR, "/var/lib/cobbler/templates/");
    }

    /**
     * Returns the directory which hosts all the
     * org specific cobbler snippets files..
     * All the snippet files that have been generated
     * by spacewalk will be at
     * /var/lib/cobbler/snippets
     *
     * @return the dir which has the kickstarts cobbler snippets
     */
    public String getCobblerSnippetsDir() {
        return Config.get().getString(COBBLER_SNIPPETS_DIR, DEFAULT_COBBLER_SNIPPET_DIR);
    }

    /**
     * Returns the base directory where the virt artifacts will be stored.
     * This information is used while setting up system records and so on..
     * @param xen true if the virt path required is for a xen virt type.
     * @return the virt path..
     */
    public File getVirtPath(boolean xen) {
        String virtPath = xen ? XEN_VIRT_PATH_DIR : KVM_VIRT_PATH_DIR;
        String defaultVirtPath = xen ? DEFAULT_XEN_VIRT_PATH : DEFAULT_KVM_VIRT_PATH;
        return new File(Config.get().getString(virtPath, defaultVirtPath));
    }

    /**
     * Returns the default value for the xen virt bridge
     * @return  the value for virt bridge.
     */
    public String getDefaultXenVirtBridge() {
        return Config.get().getString(VIRT_BRIDGE, "xenbr0");
    }

    /**
     * Returns the default value for the xen virt bridge
     * @return  the value for virt bridge.
     */
    public String getDefaultKVMVirtBridge() {
        return Config.get().getString(VIRT_BRIDGE, "virbr0");
    }


    /**
     * Returns the default virt disk size in GBs
     * @return the virt disk size
     */
    public int getDefaultVirtDiskSize() {
        return Config.get().getInt(VIRT_DISK, 3);
    }

    /**
     * Returns the default VirtMemory Size in MBs
     * @param data the kickstart data, so we can tell if it's RHEL 7
     * @return the memory size
     */
    public int getDefaultVirtMemorySize(KickstartData data) {
        // RHEL 7 requires at least 1024 MB of ram to install
        // SLES 12 + Updates needs 1024 MB.
        if (data.isRhel7OrGreater() || data.isSLES12OrGreater()) {
            return Config.get().getInt(VIRT_MEM, 1024);
        }
        return Config.get().getInt(VIRT_MEM, 512);
    }

    /**
     * Returns the default number of virt cpus
     * @return the number of virt cpus
     */
    public int getDefaultVirtCpus() {
        return Config.get().getInt(VIRT_CPU, 1);
    }

    /**
     * Check if this Sat is disconnected or not
     * @return boolean if this sat is disconnected or not
     */
    public boolean isDisconnected() {
        return (Config.get().getBoolean(DISCONNECTED));
    }

    /**
     * Get the configured hostname for this RHN Server.
     * @return String hostname
     */
    public String getHostname() {
        return Config.get().getString(JABBER_SERVER);
    }

    /**
     * Returns the URL for the search server, if not defined returns
     * http://localhost:2828/RPC2
     * @return the URL for the search server.
     */
    public String getSearchServerUrl() {
        String searchServerHost =
                Config.get().getString("search_server.host", "localhost");
        int searchServerPort = Config.get().getInt("search_server.port", 2828);
        return "http://" + searchServerHost + ":" + searchServerPort + "/RPC2";
    }

    /**
     * Returns the URL for the tasko server, if not defined returns
     * http://localhost:2829/RPC2
     * @return the URL for the search server.
     */
    public String getTaskoServerUrl() {
        String taskoServerHost =
                Config.get().getString("tasko_server.host", "localhost");
        int taskoServerPort = Config.get().getInt("tasko_server.port", 2829);
        return "http://" + taskoServerHost + ":" + taskoServerPort + "/RPC2";
    }

    /**
     * Get the URL to the cobbler server
     * @return http url
     */
    public String getCobblerServerUrl() {
        String cobblerServer = getCobblerHost();
        int cobblerServerPort = Config.get().getInt("cobbler.port", 80);
        return "http://" + cobblerServer + ":" + cobblerServerPort;
    }


    /**
     * Get just the cobbler hostname
     * @return the cobbler hostname
     */
    public String getCobblerHost() {
        return Config.get().getString("cobbler.host", "localhost");
    }

    /**
     * get the text to print at the top of a kickstart template
     * @return the header
     */
    public String getKickstartTemplateHeader() {
        return Config.get().getString("kickstart.header", "#errorCatcher ListErrors");
    }


    /**
     * Returns the default network interface for a kickstart profile
     * @return the network interface
     */
    public String getDefaultKickstartNetworkInterface() {
        return Config.get().getString(KICKSTART_NETWORK_INTERFACE, "eth0");
    }

    /**
     * Return true if this is an Uyuni or Spacewalk instance. (as opposed to SUSE Manager and Satellite)
     * @return true is this is an Uyuni or Spacewalk instance.
     */
    public boolean isSpacewalk() {
        return SPACEWALK.contains(Config.get().getString(PRODUCT_NAME));
    }

    /**
     * Return true if this is an Uyuni or Spacewalk instance. (as opposed to SUSE Manager and Satellite)
     * @return true is this is a Uyuni or Spacewalk instance.
     */
    public boolean isUyuni() {
        return isSpacewalk();
    }

    /**
     * Return the product version string depending on the product.
     * Either web.version or web.version.uyuni
     *
     * @return the product version
     */
    public String getProductVersion() {
        if (isUyuni()) {
            return Config.get().getString(PRODUCT_VERSION_UYUNI);
        }
        return Config.get().getString(PRODUCT_VERSION_MGR);
    }

    /**
     * Return true if you are to use/save repodata into the DB
     * @return true or false
     */
    public boolean useDBRepodata() {
        return Config.get().getString(USE_DB_REPODATA) == null || Config.get().getBoolean(USE_DB_REPODATA);
    }

    /**
     * Get the seperator to use when creating cobbler namse
     *  defaults to ':'
     * @return the seperator
     */
    public String getCobblerNameSeparator() {
        return Config.get().getString(COBBLER_NAME_SEPARATOR, ":");

    }

    /**
     * Returns power management types supported by Cobbler
     * @return the types
     */
    public String getCobblerPowerTypes() {
        return Config.get().getString(POWER_MANAGEMENT_TYPES);
    }

    /**
     * Returns the bootstrap kernel path
     * @return the path
     */
    public String getCobblerBootstrapKernel() {
        return Config.get().getString(COBBLER_BOOTSTRAP_KERNEL);
    }

    /**
     * Returns the bootstrap initrd path
     * @return the path
     */
    public String getCobblerBootstrapInitrd() {
        return Config.get().getString(COBBLER_BOOTSTRAP_INITRD);
    }

    /**
     * Returns the bootstrap breed
     * @return the breed
     */
    public String getCobblerBootstrapBreed() {
        return Config.get().getString(COBBLER_BOOTSTRAP_BREED);
    }

    /**
     * Returns the bootstrap kernel arch name
     * @return the arch
     */
    public String getCobblerBootstrapArch() {
        return Config.get().getString(COBBLER_BOOTSTRAP_ARCH);
    }

    /**
     * Returns the bootstrap extra kernel options
     * @return the options
     */
    public String getCobblerBootstrapExtraKernelOptions() {
        return Config.get().getString(COBBLER_BOOTSTRAP_EXTRA_KERNEL_OPTIONS);
    }

    /**
     * is the server configured to use postgresql
     * @return true if so
     */
    public boolean isPostgresql() {
        return DB_BACKEND_POSTGRESQL.equals(Config.get().getString(DB_BACKEND));
    }

    private void setSslTrustStore() throws ConfigException {
        String trustStore = Config.get().getString(SSL_TRUSTSTORE);
        if (trustStore == null || !new File(trustStore).isFile()) {
            throw new ConfigException("Can not find java truststore at " +
                trustStore + ". Path can be changed with " +
                SSL_TRUSTSTORE + " option.");
        }
        System.setProperty("javax.net.ssl.trustStore", trustStore);
    }

    /**
     * Constructs JDBC connection string based on configuration, checks for
     * some basic sanity.
     * @return JDBC connection string
     * @throws ConfigException if unknown database backend is set,
     */
    public String getJdbcConnectionString() throws ConfigException {
        String dbName = Config.get().getString(DB_NAME);
        String dbHost = Config.get().getString(DB_HOST);
        String dbPort = Config.get().getString(DB_PORT);
        String dbProto = Config.get().getString(DB_PROTO);
        boolean dbSslEnabled = Config.get().getBoolean(DB_SSL_ENABLED);

        String connectionUrl;

        if (isPostgresql()) {
            connectionUrl = dbProto + ":";
            if (dbHost != null && dbHost.length() > 0) {
                connectionUrl += "//" + dbHost;
                if (dbPort != null && dbPort.length() > 0) {
                    connectionUrl += ":" + dbPort;
                }
                connectionUrl += "/";
            }
            connectionUrl += dbName;

            if (dbSslEnabled) {
                connectionUrl += "?ssl=true";
                setSslTrustStore();
            }
        }
        else {
            throw new ConfigException(
                "Unknown db backend set, expecting postgresql");
        }
        return connectionUrl;
    }

    /**
     * is documentation available
     * @return true if so
     */
    public boolean isDocAvailable() {
        return !isSpacewalk();
    }

    /**
     * Returns Max taskomatic channel repodata workers
     * @return Max taskomatic channel repodata workers
     */
    public int getTaskoChannelRepodataWorkers() {
        return Config.get().getInt(TASKOMATIC_CHANNEL_REPODATA_WORKERS, 1);
    }

    /**
     * Gets the proxy host.
     * @return the proxy host
     */
    public String getProxyHost() {
        String proxyString = getProxyString();
        if (proxyString == null) {
            return null;
        }
        int colonIndex = proxyString.indexOf(":");
        return proxyString.substring(0, colonIndex > 0 ? colonIndex : proxyString.length());
    }

    /**
     * Gets the proxy port.
     * @return the proxy port
     */
    public int getProxyPort() {
        int result = DEFAULT_HTTP_PROXY_PORT;

        String proxyString = getProxyString();
        if (proxyString == null) {
            return result;
        }
        int colonIndex = proxyString.indexOf(":");
        if (colonIndex > 0) {
            String proxyPortString = proxyString.substring(colonIndex + 1);
            if (!StringUtils.isEmpty(proxyPortString)) {
                result = Integer.parseInt(proxyPortString);
            }
        }

        return result;
    }

    /**
     * Gets the whole proxy hostname:port string.
     * @return the proxy hostname:port string
     */
    private String getProxyString() {
        String result = Config.get().getString(HTTP_PROXY);
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        if (!HostPortValidator.getInstance().isValid(result)) {
            throw new ConfigException(
                "HTTP proxy address is not valid, check that it is in host:port form");
        }
        return result;
    }

    /**
     * Gets the proxy username.
     * @return the proxy username
     */
    public String getProxyUsername() {
        return Config.get().getString(HTTP_PROXY_USERNAME);
    }

    /**
     * Gets the proxy password.
     * @return the proxy password
     */
    public String getProxyPassword() {
        return Config.get().getString(HTTP_PROXY_PASSWORD);
    }

    /**
     * Returns actions display limit
     * @return actions display limit
     */
    public int getActionsDisplayLimit() {
        return Config.get().getInt(ACTIONS_DISPLAY_LIMIT, 10000);
    }

    /**
     * Returns config file editable size (in KB)
     * @return config file editable size (in KB)
     */
    public int getConfigFileEditSize() {
        return Config.get().getInt(CONFIG_FILE_EDIT_SIZE, 32);
    }

    /**
     * @return connection timeout for salt-ssh
     */
    public int getSaltSSHConnectTimeout() {
        return Config.get().getInt(SALT_SSH_CONNECT_TIMEOUT, 180);
    }

    /**
     * Returns salt batch presence ping job timeout
     * @return salt batch presence ping job timeout
     */
    public int getSaltPresencePingTimeout() {
        return Config.get().getInt(SALT_PRESENCE_PING_TIMEOUT, 4);
    }

    /**
     * Returns salt presence ping job gather_job_timeout
     * @return salt presence ping job gather_job_timeout
     */
    public int getSaltPresencePingGatherJobTimeout() {
        return Config.get().getInt(SALT_PRESENCE_PING_GATHER_JOB_TIMEOUT, 1);
    }

    /**
     * @return default batch size for salt jobs execution in batch mode
     */
    public int getSaltBatchSize() {
        return Config.get().getInt(SALT_BATCH_SIZE, 200);
    }

    /**
     * @return default batch delay for salt jobs execution in batch mode
     */
    public double getSaltBatchDelay() {
        return Config.get().getFloat(SALT_BATCH_DELAY, 1);
    }

    /**
     * Returns true if Prometheus monitoring is enabled
     * @return true if Prometheus monitoring is enabled
     */
    public boolean isPrometheusMonitoringEnabled() {
        return Config.get().getBoolean(PROMETHEUS_MONITORING_ENABLED);
    }

    /**
     * Returns the duration, in hours, of the time window for Salt minions to
     * stage packages in advance of scheduled installations or upgrades.
     *
     * A value of 0 disables content staging for minions.
     *
     * @return the duration
     */
    public float getSaltContentStagingWindow() {
        return Config.get().getFloat(SALT_CONTENT_STAGING_WINDOW, 8);
    }

    /**
     * Returns the advance time, in hours, for the content staging window to
     * open with respect to the scheduled installation/upgrade time.
     * @return the advance time
     */
    public float getSaltContentStagingAdvance() {
        return Config.get().getFloat(SALT_CONTENT_STAGING_ADVANCE, 8);
    }

    /**
     * Returns true if metadata signing is enabled, otherwise false.
     * @return metadata signing enabled
     */
    public boolean isMetadataSigningEnabled() {
        return Config.get().getBoolean(SIGN_METADATA);
    }

    /**
     * Returns the notifications lifetime.
     * @return notifications lifetime
     */
    public int getNotificationsLifetime() {
        return Config.get().getInt(NOTIFICATIONS_LIFETIME, 30);
    }

    /**
     * Returns the number of threads dedicated to processing Salt events.
     * @return the number of threads
     */
    public int getSaltEventThreadPoolSize() {
        return Config.get().getInt(SALT_EVENT_THREAD_POOL_SIZE, 8);
    }

    /**
     * Maximum number of events processed before COMMITTing to the database.
     * Each thread in the pool as defined by salt_event_thread_pool_size will process up to salt_events_per_commit
     * events before COMMITTing to the database and return to the pool for further work.
     *
     * Raising this to any value above 1 will decrease reliability: in case of failure multiple events
     * will be lost. On the other hand, this can reduce the overall number of COMMIT operation thus improving
     * performance in high-scale scenarios.
     * @return the number of events per commit
     */
    public int getSaltEventsPerCommit() {
        return Config.get().getInt(SALT_EVENTS_PER_COMMIT, 1);
    }


    /**
     * Returns the notifications type disabled.
     * @return notifications type disabled
     */
    public List<String> getNotificationsTypeDisabled() {
        return Config.get().getList(NOTIFICATIONS_TYPE_DISABLED);
    }

    /**
     * Returns if the Single Sign-On option is enabled
     * @return true if Single Sign-On option is enabled
     */
    public boolean isSingleSignOnEnabled() {
        return Config.get().getBoolean(SINGLE_SIGN_ON_ENABLED);
    }

    /**
     * Returns list of install type labels for which use salt for registration in kickstart profile.
     * @return list of distributions
     */
    public List<String> getUserSelectedSaltInstallTypeLabels() {
        return Config.get().getList(SALT_ENABLED_KICKSTART_INSTALL_TYPES);
    }

    /**
     * Returns if systems running a cluster product (CaaSP) are automatically system-locked upon bootstrapping or
     * after any package-related action.
     * @return true if system lock is automatically enabled
     */
    public boolean isAutomaticSystemLockForClusterNodesEnabled() {
        return Config.get().getBoolean(AUTOMATIC_SYSTEM_LOCK_CLUSTER_NODES_ENABLED);
    }

    /**
     * Return the default locale. If not supported return en_US as default language.
     *
     * @return the preferred locale
     */
    public String getDefaultLocale() {
        return Config.get().getString(DEFAULT_LOCALE, "en_US");
    }

    /**
     * Return the default documentation locale. If not supported return en_US as default language.
     *
     * @return the preferred documentation locale
     */
    public String getDefaultDocsLocale() {
        return Config.get().getString(DEFAULT_DOCS_LOCALE, "en");
    }

    /**
     * Return the list of possible themes for the webUI
     *
     * @return the list of possible themes for the webUI
     */
    public List<String> getWebThemesList() {
        return Config.get().getList(WEB_THEMES);
    }

    /**
     * Return the name of the theme for the webUI
     *
     * @return the name of the theme for the webUI
     */
    public String getDefaultWebTheme() {
        return Config.get().getString(WEB_THEME, "susemanager-light");
    }

    /**
     * Returns true if registrations happend to Uyuni should be forwarded to SCC
     *
     * @return true if registrations should be forwarded to SCC, otherwise false
     */
    public boolean isForwardRegistrationEnabled() {
        return Config.get().getBoolean(FORWARD_REGISTRATION);
    }
}
