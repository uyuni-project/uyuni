/*
 * Copyright (c) 2019--2020 SUSE LLC
 * Copyright (c) 2014--2015 Red Hat, Inc.
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
package com.suse.manager.webui.utils;

import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.org.usergroup.OrgUserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserGroupFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.AuthType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.UpdateErrataCacheEvent;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.manager.user.CreateUserCommand;
import com.redhat.rhn.manager.user.UpdateUserCommand;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.ldap.DefaultLdapAuthConfigProvider;
import com.suse.manager.ldap.DefaultLdapServiceFactory;
import com.suse.manager.ldap.LdapAuthConfigProvider;
import com.suse.manager.ldap.LdapAuthServerSettings;
import com.suse.manager.ldap.LdapServiceException;
import com.suse.manager.ldap.LdapServiceFactory;
import com.suse.manager.ldap.LdapUser;
import com.suse.manager.utils.DBDiskCheckHelper;
import com.suse.manager.utils.DiskCheckHelper;
import com.suse.manager.utils.DiskCheckSeverity;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LoginHelper
 */
public class LoginHelper {

    private static Logger log = LogManager.getLogger(LoginHelper.class);
    private static final String DEFAULT_KERB_USER_PASSWORD = "0";

    /**
     * Fixed prefix a directory group must carry to take part in Uyuni role mapping. Not
     * configurable in v1; stripped before the external-group lookup.
     */
    /**
     * Fixed prefixes a directory group must carry to take part in Uyuni role mapping. Not
     * configurable in v1; stripped before the external-group lookup. {@code uyuni_} is the
     * documented v1 prefix; {@code uyuni-} is accepted so RFC examples such as
     * {@code uyuni-admins} map through external group {@code admins}.
     */
    private static final String[] LDAP_GROUP_PREFIXES = {"uyuni_", "uyuni-"};

    private static LdapAuthConfigProvider ldapConfigProvider = new DefaultLdapAuthConfigProvider();
    private static LdapServiceFactory ldapServiceFactory = new DefaultLdapServiceFactory();
    private static final Long MIN_PG_DB_VERSION = 160001L;
    private static final Long MAX_PG_DB_VERSION = 189999L;
    private static final String MIN_PG_DB_VERSION_STRING = "16";
    private static final String MAX_PG_DB_VERSION_STRING = "18";
    public static final String DEFAULT_URL_BOUNCE = "/rhn/YourRhn.do";

    /**
     * Utility classes can't be instantiated.
     */
    private LoginHelper() {
    }

    /**
     * check whether we can login an externally authenticated user
     * @param request request
     * @param messages messages
     * @param errors errors
     * @return user, if externally authenticated
     */
    public static User checkExternalAuthentication(HttpServletRequest request,
            List<String> messages,
            List<String> errors) {
        String remoteUserString = request.getRemoteUser();
        User remoteUser = null;
        if (remoteUserString != null) {

            String firstname = decodeFromIso88591(
                    (String) request.getAttribute("REMOTE_USER_FIRSTNAME"), "");
            String lastname = decodeFromIso88591(
                    (String) request.getAttribute("REMOTE_USER_LASTNAME"), "");
            String email = decodeFromIso88591(
                    (String) request.getAttribute("REMOTE_USER_EMAIL"), null);

            Set<String> extGroups = getExtGroups(request);
            Set<Role> roles = getRolesFromExtGroups(extGroups);

                try {
                    remoteUser = UserFactory.lookupByLogin(remoteUserString);

                if (remoteUser.isDisabled()) {
                    errors.add("Account " + remoteUserString + " has been deactivated");
                    remoteUser = null;
                }
                if (remoteUser != null) {
                    updateRemoteUser(remoteUser, firstname, lastname, email, roles);
                }
            }
            catch (LookupException le) {
                return newRemoteUser(request, remoteUserString, firstname, lastname, email, roles, extGroups, messages);
            }
        }
        return remoteUser;
    }

    private static User newRemoteUser(HttpServletRequest request,
                                      String remoteUserString,
                                      String firstname,
                                      String lastname,
                                      String email,
                                      Set<Role> roles,
                                      Set<String> extGroups,
                                      List<String> messages) {
        User remoteUser = null;
        Org newUserOrg = null;
        RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
        Boolean useOrgUnit =
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT).getValue();
        if (BooleanUtils.isTrue(useOrgUnit)) {
            String orgUnitString =
                    (String) request.getAttribute("REMOTE_USER_ORGUNIT");
            newUserOrg = OrgFactory.lookupByName(orgUnitString);
            if (newUserOrg == null) {
                log.error("Cannot find organization with name: {}", orgUnitString);
            }
        }
        if (newUserOrg == null) {
            Long defaultOrgId =
                    factory.getLongConfiguration(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID).getValue();
            if (defaultOrgId != null) {
                newUserOrg = OrgFactory.lookupById(defaultOrgId);
                if (newUserOrg == null) {
                    log.error("Cannot find organization with id: {}", defaultOrgId);
                }
            }
        }
        if (newUserOrg != null) {
            Set<ServerGroup> sgs = getSgsFromExtGroups(extGroups, newUserOrg);
            remoteUser = createProvisionedUser(remoteUserString, firstname, lastname, email, roles, sgs,
                    newUserOrg, AuthType.LOCAL);
        }
        if (remoteUser != null &&
                remoteUser.getPassword().equals(DEFAULT_KERB_USER_PASSWORD)) {
            messages.add("You have logged in as an externally authenticated user. " +
                    "To be able to login using this account with login and password " +
                    "set your username and password in the user details page.");
        }
        return remoteUser;
    }

    /**
     * Just-in-time provisioning shared by all external authentication backends (header-based
     * {@code REMOTE_USER} and native LDAP). Creates a Uyuni account with a non-usable password and
     * the given temporary roles. The caller is responsible for having resolved the target
     * organization and, where relevant, server-group mappings.
     *
     * @param login the login name to create
     * @param firstname the first name, may be {@code null}
     * @param lastname the last name, may be {@code null}
     * @param email the e-mail address, may be {@code null}
     * @param roles the temporary roles to assign
     * @param sgs the server groups to assign (may be empty)
     * @param org the organization to create the user in
     * @param authType the authentication backend to stamp on the new account
     * @return the created user, or {@code null} if creation failed
     */
    private static User createProvisionedUser(String login, String firstname, String lastname, String email,
                                              Set<Role> roles, Set<ServerGroup> sgs, Org org, AuthType authType) {
        User created = null;
        try {
            CreateUserCommand createCmd = new CreateUserCommand();
            createCmd.setLogin(login);
            // set a password, that cannot really be used
            createCmd.setRawPassword(DEFAULT_KERB_USER_PASSWORD);
            createCmd.setFirstNames(firstname);
            createCmd.setLastName(lastname);
            createCmd.setEmail(email);
            createCmd.setOrg(org);
            createCmd.setTemporaryRoles(roles);
            createCmd.setServerGroups(sgs);
            createCmd.validate();
            createCmd.storeNewUser();
            created = createCmd.getUser();
            if (authType != null && authType != AuthType.LOCAL) {
                created.setAuthType(authType);
                UserManager.storeUser(created);
            }
            log.warn("Externally authenticated login {} ({} {}) created in {} as {}.", login,
                    firstname, lastname, org.getName(), authType);
        }
        catch (WrappedSQLException wse) {
            log.error("Creation of user failed with: {}", wse.getMessage());
            HibernateFactory.rollbackTransaction();
            // The transaction was rolled back; never hand back a half-provisioned user.
            created = null;
        }
        return created;
    }

    private static void updateRemoteUser(User remoteUser,
                                         String firstname,
                                         String lastname,
                                         String email,
                                         Set<Role> roles) {
        UpdateUserCommand updateCmd = new UpdateUserCommand(remoteUser);
        if (!StringUtils.isEmpty(firstname)) {
            updateCmd.setFirstNames(firstname);
        }
        if (!StringUtils.isEmpty(lastname)) {
            updateCmd.setLastName(lastname);
        }
        if (!StringUtils.isEmpty(email)) {
            updateCmd.setEmail(email);
        }
        updateCmd.setTemporaryRoles(roles);
        updateCmd.updateUser();
        if (log.isWarnEnabled()) {
            log.warn("Externally authenticated login {} ({} {})",
                    StringUtil.sanitizeLogInput(remoteUser.getLogin()), firstname, lastname);
        }
    }

    /**
     * Attempt to authenticate a password login against the configured LDAP directories.
     *
     * <p>This is the login-layer entry point for native LDAP. It mirrors
     * {@link #checkExternalAuthentication} but for a supplied login and password rather than trusted
     * request headers. It is called from every password entry point, but just-in-time provisioning
     * is gated by {@code allowJit}: the Web UI / HTTP API path enables it, while the XML-RPC path
     * only authenticates already-provisioned {@code auth_type = LDAP} users (RFC v1 decision).</p>
     *
     * <p>The three-way outcome tells the caller how to proceed:</p>
     * <ul>
     *   <li><b>non-{@code null} user</b> - LDAP handled the login; proceed to establish the session.</li>
     *   <li><b>{@code null} and {@code errors} empty</b> - LDAP does not apply to this login (LDAP
     *       disabled, a known non-LDAP user, or an unknown user when {@code allowJit} is
     *       {@code false}); the caller should fall through to local/PAM auth.</li>
     *   <li><b>{@code null} and {@code errors} non-empty</b> - the user is an LDAP user but
     *       authentication failed; the caller should reject the login and must not fall back to
     *       another backend.</li>
     * </ul>
     *
     * @param login the supplied login name
     * @param password the supplied password
     * @param allowReadOnly whether a read-only account may log in on this entry point (the Web UI
     *                      login passes {@code false}, the API entry points pass {@code true})
     * @param allowJit whether unknown users may be provisioned just-in-time on this entry point
     *                 (Web UI / HTTP API pass {@code true}; XML-RPC passes {@code false})
     * @param messages informational messages to surface to the user
     * @param errors error messages; a non-empty list means "reject, do not fall back"
     * @return the authenticated (or just-provisioned) user, or {@code null} as described above
     */
    public static User checkLdapAuthentication(String login, String password, boolean allowReadOnly,
            boolean allowJit, List<String> messages, List<String> errors) {
        if (login == null) {
            return null;
        }
        // Determine the configured servers up front. Routing by auth_type still happens even when
        // LDAP is disabled, so that a known LDAP user can never silently fall back to local/PAM.
        List<LdapAuthServerSettings> servers =
                ldapConfigProvider.isEnabled() ? ldapConfigProvider.getServers() : List.of();

        User existing;
        try {
            existing = UserFactory.lookupByLogin(login);
        }
        catch (LookupException le) {
            existing = null;
        }

        if (existing != null) {
            // Route strictly by auth_type. Non-LDAP users are not our concern; hand them back to the
            // caller's local/PAM path. LDAP users are always handled here and never fall back.
            if (existing.getAuthType() != AuthType.LDAP) {
                return null;
            }
            return authenticateKnownLdapUser(existing, login, password, servers, allowReadOnly, errors);
        }

        // Unknown user. JIT provisioning runs on the Web UI / HTTP API path only; the XML-RPC path
        // (allowJit == false) never creates accounts and lets the caller's local path reject the
        // login as usual (RFC v1: JIT on Web UI only, XML-RPC authenticates existing users only).
        if (!allowJit || servers.isEmpty()) {
            return null;
        }
        return provisionUnknownLdapUser(login, password, servers, messages);
    }

    private static User authenticateKnownLdapUser(User existing, String login, String password,
            List<LdapAuthServerSettings> servers, boolean allowReadOnly, List<String> errors) {
        LocalizationService ls = LocalizationService.getInstance();
        if (servers.isEmpty()) {
            // The user is bound to LDAP but LDAP is unavailable: reject, never try another backend.
            if (log.isErrorEnabled()) {
                log.error("LDAP user {} cannot be authenticated: native LDAP is disabled or unconfigured.",
                        StringUtil.sanitizeLogInput(login));
            }
            errors.add(ls.getMessage("error.invalid_login"));
            return null;
        }
        Optional<AuthenticatedLdapUser> authenticated = authenticateAgainstServers(servers, login, password);
        if (authenticated.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("LDAP AUTH FAILURE: [{}]", StringUtil.sanitizeLogInput(login));
            }
            errors.add(ls.getMessage("error.invalid_login"));
            return null;
        }
        // Credentials are valid; apply the same active/read-only gates as local login before granting
        // a session (mirrors UserManager.loginUser).
        if (existing.isDisabled()) {
            errors.add(ls.getMessage("account.disabled"));
            return null;
        }
        if (!allowReadOnly && existing.isReadOnly()) {
            errors.add(ls.getMessage("error.user_readonly"));
            return null;
        }
        AuthenticatedLdapUser result = authenticated.get();
        LdapUser ldapUser = result.user();
        Set<Role> roles = getRolesFromExtGroups(toExtGroupLabels(ldapUser.groupLabels()),
                result.server().serverId());
        updateRemoteUser(existing, ldapUser.firstName(), ldapUser.lastName(), ldapUser.email(), roles);
        recordLdapServer(existing, result.server());
        return existing;
    }

    /**
     * Records which directory authenticated the user, so an administrator can tell where an account
     * comes from. Only written when it actually changes, to avoid a pointless update on every login.
     */
    private static void recordLdapServer(User user, LdapAuthServerSettings server) {
        Long serverId = server.serverId();
        if (serverId != null && !serverId.equals(user.getLdapServerId())) {
            user.setLdapServerId(serverId);
            UserManager.storeUser(user);
        }
    }

    private static User provisionUnknownLdapUser(String login, String password,
            List<LdapAuthServerSettings> servers, List<String> messages) {
        // Unknown user: probe the servers in priority order. A server that authenticates the
        // credentials, allows just-in-time provisioning and resolves its organization creates the
        // account as an LDAP user. If any of those steps fails, keep probing the remaining servers
        // rather than giving up on the whole login.
        for (LdapAuthServerSettings server : servers) {
            Optional<LdapUser> authenticated = tryAuthenticate(server, login, password);
            if (authenticated.isEmpty()) {
                continue;
            }
            if (!server.allowsJit()) {
                if (log.isInfoEnabled()) {
                    log.info("LDAP user {} authenticated but just-in-time provisioning is disabled.",
                            StringUtil.sanitizeLogInput(login));
                }
                continue;
            }
            Org org = resolveLdapOrg(server);
            if (org == null) {
                continue;
            }
            LdapUser ldapUser = authenticated.get();
            Set<Role> roles = getRolesFromExtGroups(toExtGroupLabels(ldapUser.groupLabels()),
                    server.serverId());
            User created = createProvisionedUser(ldapUser.login(), ldapUser.firstName(), ldapUser.lastName(),
                    ldapUser.email(), roles, new HashSet<>(), org, AuthType.LDAP);
            if (created != null) {
                applyLdapProvisioningOptions(created, server);
                messages.add("You have logged in as an LDAP-authenticated user.");
                return created;
            }
        }
        return null;
    }

    /**
     * Applies the per-directory provisioning options to a freshly created account: records the
     * directory it came from and, when the directory opts out of the default membership, drops the
     * {@code regular_user} access group that {@code CreateUserCommand} gives every new user, so that
     * access is driven entirely by LDAP group mapping.
     */
    private static void applyLdapProvisioningOptions(User created, LdapAuthServerSettings server) {
        created.setLdapServerId(server.serverId());
        if (!server.autoJoinRegularUser()) {
            created.removeFromGroup(AccessGroupFactory.REGULAR_USER);
        }
        UserManager.storeUser(created);
    }

    private static Org resolveLdapOrg(LdapAuthServerSettings server) {
        Long orgId = server.defaultOrgId();
        Org org = orgId == null ? null : OrgFactory.lookupById(orgId);
        if (org == null) {
            log.error("Cannot provision LDAP user: organization with id {} not found.", orgId);
        }
        return org;
    }

    private static Optional<AuthenticatedLdapUser> authenticateAgainstServers(
            List<LdapAuthServerSettings> servers, String login, String password) {
        for (LdapAuthServerSettings server : servers) {
            Optional<LdapUser> authenticated = tryAuthenticate(server, login, password);
            if (authenticated.isPresent()) {
                return authenticated.map(user -> new AuthenticatedLdapUser(server, user));
            }
        }
        return Optional.empty();
    }

    /**
     * A directory entry together with the server that authenticated it, so the login layer can
     * record which directory a user came from.
     *
     * @param server the directory that accepted the credentials
     * @param user the directory entry of the authenticated user
     */
    private record AuthenticatedLdapUser(LdapAuthServerSettings server, LdapUser user) { }

    /**
     * Selects the directory groups that participate in role mapping and converts them to
     * external-group labels.
     *
     * <p>Only groups whose name starts with {@code uyuni_} or {@code uyuni-} are considered, and
     * that prefix is stripped before the lookup, so directory groups {@code uyuni_admins} and
     * {@code uyuni-admins} both map through external group {@code admins}. Every other directory
     * group is ignored, which keeps unrelated directory groups from accidentally matching an
     * existing external-group mapping. The prefixes are deliberately fixed rather than
     * configurable for v1.</p>
     *
     * @param ldapGroupLabels the group names as returned by the directory
     * @return the external-group labels to look up, prefix removed
     */
    private static Set<String> toExtGroupLabels(List<String> ldapGroupLabels) {
        Set<String> labels = new HashSet<>();
        for (String groupLabel : ldapGroupLabels) {
            if (groupLabel == null) {
                continue;
            }
            String trimmed = groupLabel.trim();
            String stripped = stripLdapGroupPrefix(trimmed);
            if (stripped != null) {
                labels.add(stripped);
            }
            else if (log.isDebugEnabled()) {
                log.debug("Ignoring LDAP group '{}': it does not start with uyuni_ or uyuni-.",
                        StringUtil.sanitizeLogInput(trimmed));
            }
        }
        return labels;
    }

    /**
     * @param groupLabel a directory group CN
     * @return the label with {@code uyuni_}/{@code uyuni-} removed, or {@code null} if it does not
     *         carry a mapping prefix
     */
    private static String stripLdapGroupPrefix(String groupLabel) {
        for (String prefix : LDAP_GROUP_PREFIXES) {
            if (groupLabel.length() > prefix.length() &&
                    groupLabel.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return groupLabel.substring(prefix.length());
            }
        }
        return null;
    }

    private static Optional<LdapUser> tryAuthenticate(LdapAuthServerSettings server, String login,
            String password) {
        try {
            return ldapServiceFactory.getInstance(server.connectionConfig()).authenticate(login, password);
        }
        catch (LdapServiceException e) {
            if (log.isErrorEnabled()) {
                log.error("LDAP directory could not be consulted for login {}: {}",
                        StringUtil.sanitizeLogInput(login), e.getMessage());
            }
            return Optional.empty();
        }
    }

    /**
     * Overrides the LDAP configuration provider. Intended for tests.
     *
     * @param providerIn the provider to use
     */
    public static void setLdapConfigProvider(LdapAuthConfigProvider providerIn) {
        ldapConfigProvider = providerIn;
    }

    /**
     * Overrides the LDAP service factory. Intended for tests that point the login layer at an
     * in-memory directory server.
     *
     * @param factoryIn the factory to use
     */
    public static void setLdapServiceFactory(LdapServiceFactory factoryIn) {
        ldapServiceFactory = factoryIn;
    }

    private static String decodeFromIso88591(String string, String defaultString) {
        if (string != null) {
            return new String(string.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        return defaultString;
    }

    private static Set<Role> getRolesFromExtGroups(Set<String> groupNames) {
        return getRolesFromExtGroups(groupNames, null);
    }

    /**
     * Resolves Uyuni roles from external-group labels. When {@code ldapServerId} is set, a mapping
     * scoped to that directory wins over a server-agnostic mapping of the same label.
     */
    private static Set<Role> getRolesFromExtGroups(Set<String> groupNames, Long ldapServerId) {
        Set<Role> roles = new HashSet<>();
        for (String extGroupName : groupNames) {
            UserExtGroup extGroup = UserGroupFactory.lookupExtGroupByLabel(extGroupName, ldapServerId);
            if (extGroup == null) {
                log.info("No role mapping defined for external group '{}'.", extGroupName);
                continue;
            }
            roles.addAll(extGroup.getRoles());
        }
        return roles;
    }

    private static Set<ServerGroup> getSgsFromExtGroups(Set<String> groupNames, Org org) {
        Set<ServerGroup> sgs = new HashSet<>();
        for (String extGroupName : groupNames) {
            OrgUserExtGroup extGroup =
                    UserGroupFactory.lookupOrgExtGroupByLabelAndOrg(extGroupName, org);
            if (extGroup == null) {
                log.info("No sg mapping defined for external group '{}'.", extGroupName);
                continue;
            }
            sgs.addAll(extGroup.getServerGroups());
        }
        return sgs;
    }

    private static Set<String> getExtGroups(HttpServletRequest requestIn) {
        Set<String> extGroups = new HashSet<>();
        Long nGroups = null;
        String nGroupsStr = (String) requestIn.getAttribute("REMOTE_USER_GROUP_N");
        if (nGroupsStr != null) {
            try {
                nGroups = Long.parseLong(nGroupsStr);
            }
            catch (NumberFormatException nfe) {
                // do nothing, nGroups stays null
            }
        }
        if (nGroups == null) {
            log.warn("REMOTE_USER_GROUP_N not set!");
            return extGroups;
        }
        for (int i = 1; i <= nGroups; i++) {
            String extGroupName = (String) requestIn.getAttribute("REMOTE_USER_GROUP_" + i);
            if (extGroupName == null) {
                log.warn("REMOTE_USER_GROUP_{} not set!", i);
                continue;
            }
            extGroups.add(extGroupName);

        }
        if (log.isWarnEnabled()) {
            log.warn("REMOTE_USER_GROUP_{}: {}", nGroupsStr, StringUtils.join(extGroups.toArray(), ";"));
        }
        return extGroups;
    }

    /** static method shared by LoginAction and LoginSetupAction
     * @param request actual request
     * @param response actual reponse
     * @param user logged in user
     */
    public static void successfulLogin(HttpServletRequest request,
            HttpServletResponse response, User user) {
        // set last logged in
        user.setLastLoggedIn(new Date());
        UserManager.storeUser(user);
        // update session with actual user
        PxtSessionDelegateFactory.getInstance().newPxtSessionDelegate().
            updateWebUserId(request, response, user.getId());

        LoginHelper.publishUpdateErrataCacheEvent(user.getOrg());
    }

    /**
     * update url_bounce
     * @param urlBounce url_bounce
     * @param requestMethod request method
     * @return updated url_bounce
     */
    public static String updateUrlBounce(String urlBounce, String requestMethod) {
        if (StringUtils.isBlank(urlBounce)) {
            urlBounce = DEFAULT_URL_BOUNCE;
        }
        else {
            String urlBounceTrimmed = urlBounce.trim();
            if (urlBounceTrimmed.equals("/rhn/") ||
                    urlBounceTrimmed.equals("/rhn/manager/login") ||
                    urlBounceTrimmed.endsWith("Logout.do") ||
                    !urlBounceTrimmed.startsWith("/")) {
                urlBounce = DEFAULT_URL_BOUNCE;
            }
        }
        if (requestMethod != null && requestMethod.equals("POST")) {
            urlBounce = DEFAULT_URL_BOUNCE;
        }
        return urlBounce;
    }

    /**
     * Schedule update of the errata cache for a given organization.
     *
     * @param orgIn organization
     */
    private static void publishUpdateErrataCacheEvent(Org orgIn) {
        StopWatch sw = new StopWatch();
        if (log.isDebugEnabled()) {
            log.debug("Updating errata cache");
            sw.start();
        }

        UpdateErrataCacheEvent uece = new
            UpdateErrataCacheEvent(UpdateErrataCacheEvent.TYPE_ORG);
        uece.setOrgId(orgIn.getId());
        MessageQueue.publish(uece);

        if (log.isDebugEnabled()) {
            sw.stop();
            log.debug("Finished Updating errata cache. Took [{}]", sw.getTime());
        }
    }

    /**
     * Validate the currently running DB version with the OS
     * @return validation errors
     */
    public static List<String> validateDBVersion() {
        List<String> validationErrors = new ArrayList<>();
        LocalizationService ls = LocalizationService.getInstance();
        Long serverVersion = 0L;
        String pgVersion = "";

        SelectMode m = ModeFactory.getMode("General_queries", "pg_version_num");
        DataResult<Map<String, Object>> dr = m.execute();
        if (!dr.isEmpty()) {
            serverVersion = Long.valueOf((String) dr.get(0).get("server_version_num"));
        }
        if (serverVersion == null) {
            serverVersion = 0L;
        }
        m = ModeFactory.getMode("General_queries", "pg_version");
        dr = m.execute();
        if (!dr.isEmpty()) {
            pgVersion = (String) dr.get(0).get("server_version");
        }

        if (log.isDebugEnabled()) {
            log.debug("PG DB version is: {}", serverVersion);
        }
        if (serverVersion < MIN_PG_DB_VERSION) {
            validationErrors.add(ls.getMessage("error.unsupported_db_min", pgVersion, MIN_PG_DB_VERSION_STRING));
            if (log.isErrorEnabled()) {
                log.error(ls.getMessage("error.unsupported_db_min", pgVersion, MIN_PG_DB_VERSION_STRING));
            }
        }
        else if (serverVersion > MAX_PG_DB_VERSION) {
            validationErrors.add(ls.getMessage("error.unsupported_db_max", pgVersion, MAX_PG_DB_VERSION_STRING));
            if (log.isErrorEnabled()) {
                log.error(ls.getMessage("error.unsupported_db_max", pgVersion, MAX_PG_DB_VERSION_STRING));
            }
        }

        m = ModeFactory.getMode("General_queries", "installed_schema_version");
        dr = m.execute();
        if (dr.isEmpty()) {
            validationErrors.add(ls.getMessage("error.unfinished_schema_upgrade"));
            if (log.isErrorEnabled()) {
                log.error(ls.getMessage("error.unfinished_schema_upgrade"));
            }
        }
        return validationErrors;
    }

    /**
     * @return returns whether installed schema version matches schema version of DB
     */
    public static Boolean isSchemaUpgradeRequired() {
        String rpmSchemaVersion = getRpmSchemaVersion("satellite-schema");
        if (rpmSchemaVersion == null) {
            rpmSchemaVersion = getRpmSchemaVersion("susemanager-schema");
        }
        if (rpmSchemaVersion == null) {
            rpmSchemaVersion = getRpmSchemaVersion("spacewalk-schema");
        }

        SelectMode m = ModeFactory.getMode("General_queries", "installed_schema_version");
        DataResult<Map<String, Object>> dr = m.execute();
        String installedSchemaVersion = null;
        if (!dr.isEmpty()) {
            installedSchemaVersion = (String) dr.get(0).get("version");
        }

        if (log.isDebugEnabled()) {
            log.debug("RPM version of schema: {}", rpmSchemaVersion == null ? "null" : rpmSchemaVersion);
            log.debug("Version of installed database schema: {}",
                    installedSchemaVersion == null ? "null" : installedSchemaVersion);
        }

        return rpmSchemaVersion != null && installedSchemaVersion != null &&
                !rpmSchemaVersion.equals(installedSchemaVersion);
    }

    private static String getRpmSchemaVersion(String schemaName) {
        String[] rpmCommand = new String[4];
        rpmCommand[0] = "/usr/bin/rpm";
        rpmCommand[1] = "-q";
        rpmCommand[2] = "--qf=%{VERSION}-%{RELEASE}";
        rpmCommand[3] = schemaName;
        SystemCommandExecutor ce = new SystemCommandExecutor();
        return ce.execute(rpmCommand) == 0 ?
            ce.getLastCommandOutput().replace("\n", "") : null;
    }

    /**
     * Validate the available disk space using an external script.
     * @return a string representing the severity level.
     */
    public static String validateDiskSpaceAvailability() {
        final DiskCheckHelper diskCheck = new DiskCheckHelper();

        final DiskCheckSeverity diskCheckSeverity = diskCheck.executeDiskCheck();
        return diskCheckSeverity.name().toLowerCase();
    }

    /**
     * Validate the DB available disk space using an external script.
     * @return a string representing the severity level.
     */
    public static String validateDBDiskSpaceAvailability() {
        final DBDiskCheckHelper dbDiskCheck = new DBDiskCheckHelper();

        final DiskCheckSeverity diskCheckSeverity = dbDiskCheck.executeDiskCheck();
        return diskCheckSeverity.name().toLowerCase();
    }
}
