/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.LdapCredentials;
import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserGroupFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.AuthType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.ldap.DbLdapAuthConfigProvider;
import com.suse.manager.ldap.DefaultLdapAuthConfigProvider;
import com.suse.manager.ldap.DefaultLdapServiceFactory;
import com.suse.manager.ldap.LdapAuthConfigProvider;
import com.suse.manager.ldap.LdapAuthServerSettings;
import com.suse.manager.ldap.LdapProvisioningMode;
import com.suse.manager.ldap.LdapServerConfig;
import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapTransport;
import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.model.ldap.LdapAuthServerFactory;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifies the login-layer routing added in {@link LoginHelper#checkLdapAuthentication} against an
 * embedded {@link InMemoryDirectoryServer}. Because this method is the single entry point shared by
 * the Web UI, the HTTP API and the XML-RPC API, these tests cover the group synchronization that
 * API-only users must also receive.
 */
public class LoginHelperLdapAuthenticationTest extends BaseTestCaseWithUser {

    private static final String BASE_DN = "dc=uyuni,dc=test";
    private static final String USERS_DN = "ou=users,dc=uyuni,dc=test";
    private static final String GROUPS_DN = "ou=groups,dc=uyuni,dc=test";
    private static final String ADMIN_DN = "cn=admin,dc=uyuni,dc=test";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String JIT_LOGIN = "bob";
    private static final String JIT_PASSWORD = "bob123";
    private static final String PREFIXED_EXT_GROUP = "ldap-channel-admins";
    private static final String UNPREFIXED_GROUP = "ldap-config-admins";

    private InMemoryDirectoryServer directory;

    @BeforeEach
    public void setUp() throws Exception {
        // Parent @BeforeEach (setUpBaseTestCaseWithUser) already created `user`.
        InMemoryDirectoryServerConfig dsConfig = new InMemoryDirectoryServerConfig(BASE_DN);
        dsConfig.addAdditionalBindCredentials(ADMIN_DN, ADMIN_PASSWORD);
        directory = new InMemoryDirectoryServer(dsConfig);
        directory.add("dn: " + BASE_DN, "objectClass: top", "objectClass: domain", "dc: uyuni");
        directory.add("dn: " + USERS_DN, "objectClass: organizationalUnit", "ou: users");
        directory.add("dn: " + GROUPS_DN, "objectClass: organizationalUnit", "ou: groups");
        // A directory entry matching the existing Uyuni user (whose login is randomly generated).
        addUser(user.getLogin(), "existing-secret");
        // A directory entry that has no Uyuni account yet, used for the JIT test.
        addUser(JIT_LOGIN, JIT_PASSWORD);
        directory.startListening();
    }

    @AfterEach
    public void stopDirectory() {
        if (directory != null) {
            directory.shutDown(true);
        }
        // Restore the defaults so we do not leak configuration into other tests.
        LoginHelper.setLdapConfigProvider(new DefaultLdapAuthConfigProvider());
        LoginHelper.setLdapServiceFactory(new DefaultLdapServiceFactory());
    }

    private void addGroup(String cn, String... memberUids) throws Exception {
        String[] entry = new String[3 + memberUids.length];
        entry[0] = "dn: cn=" + cn + "," + GROUPS_DN;
        entry[1] = "objectClass: groupOfNames";
        entry[2] = "cn: " + cn;
        for (int i = 0; i < memberUids.length; i++) {
            entry[3 + i] = "member: uid=" + memberUids[i] + "," + USERS_DN;
        }
        directory.add(entry);
    }

    /**
     * Maps an external group label to a single Uyuni role, as an administrator would through
     * Users &gt; External groups.
     */
    private void mapExtGroupToRole(String label, Role role) {
        UserExtGroup extGroup = new UserExtGroup();
        extGroup.setLabel(label);
        extGroup.setRoles(new HashSet<>(Set.of(role)));
        UserGroupFactory.save(extGroup);
    }

    private void addUser(String uid, String password) throws Exception {
        directory.add(
                "dn: uid=" + uid + "," + USERS_DN,
                "objectClass: inetOrgPerson",
                "cn: " + uid,
                "givenName: " + uid,
                "sn: Directory",
                "uid: " + uid,
                "mail: " + uid + "@uyuni.test",
                "userPassword: " + password);
    }

    private void enableLdap(LdapProvisioningMode mode) {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "127.0.0.1", USERS_DN)
                .transport(LdapTransport.PLAIN)
                .port(directory.getListenPort())
                .bind(ADMIN_DN, ADMIN_PASSWORD)
                .groupBaseDn(GROUPS_DN)
                .build();
        // No persisted record backs these settings, so no directory id is recorded on the user.
        LdapAuthServerSettings settings =
                new LdapAuthServerSettings(null, config, mode, user.getOrg().getId(), true, 0);
        LoginHelper.setLdapConfigProvider(new StubProvider(true, List.of(settings)));
        LoginHelper.setLdapServiceFactory(new DefaultLdapServiceFactory());
    }

    /**
     * Points the login layer at a persisted directory record aimed at the embedded server, which is
     * how a real installation is configured.
     *
     * @param mode the provisioning mode of the directory
     * @param autoJoinRegularUser whether provisioned users join the {@code regular_user} group
     * @return the persisted directory record
     */
    private LdapAuthServer enableLdapFromDatabase(LdapProvisioningMode mode, boolean autoJoinRegularUser) {
        LdapCredentials credentials = CredentialsFactory.createLdapCredentials(ADMIN_PASSWORD);
        CredentialsFactory.storeCredentials(credentials);

        LdapAuthServer server = new LdapAuthServer();
        server.setLabel("embedded-" + TestUtils.randomString());
        server.setServerType(LdapServerType.OPEN_LDAP);
        server.setHost("127.0.0.1");
        server.setPort(directory.getListenPort());
        server.setTransport(LdapTransport.PLAIN);
        server.setBindDn(ADMIN_DN);
        server.setCredentials(credentials);
        server.setUserBaseDn(USERS_DN);
        server.setGroupBaseDn(GROUPS_DN);
        server.setProvisioningMode(mode);
        server.setDefaultOrg(user.getOrg());
        server.setAutoJoinRegularUser(autoJoinRegularUser);
        LdapAuthServerFactory.save(server);
        TestUtils.flushSession();

        LoginHelper.setLdapConfigProvider(new DbLdapAuthConfigProvider(() -> List.of(server)));
        LoginHelper.setLdapServiceFactory(new DefaultLdapServiceFactory());
        return server;
    }

    @Test
    public void localUserIsNotApplicableWhenLdapDisabled() {
        LoginHelper.setLdapConfigProvider(new StubProvider(false, List.of()));
        List<String> errors = new ArrayList<>();
        assertNull(LoginHelper.checkLdapAuthentication(user.getLogin(), "whatever",
                true, true, new ArrayList<>(), errors));
        assertTrue(errors.isEmpty());
    }

    @Test
    public void knownLdapUserIsRejectedWhenLdapDisabled() {
        // Regression: an AuthType.LDAP user must never fall back to local auth (placeholder password)
        // when LDAP is turned off. Route by auth_type and reject.
        user.setAuthType(AuthType.LDAP);
        UserManager.storeUser(user);
        LoginHelper.setLdapConfigProvider(new StubProvider(false, List.of()));

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(user.getLogin(), "0",
                true, true, new ArrayList<>(), errors);

        assertNull(result);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void knownLocalUserIsNotHandledByLdap() {
        enableLdap(LdapProvisioningMode.JIT);
        // The base user defaults to AuthType.LOCAL, so LDAP must decline and let local auth run.
        List<String> errors = new ArrayList<>();
        assertNull(LoginHelper.checkLdapAuthentication(user.getLogin(), "existing-secret",
                true, true, new ArrayList<>(), errors));
        assertTrue(errors.isEmpty());
    }

    @Test
    public void knownLdapUserAuthenticates() {
        user.setAuthType(AuthType.LDAP);
        UserManager.storeUser(user);
        enableLdap(LdapProvisioningMode.JIT);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(user.getLogin(), "existing-secret",
                true, true, new ArrayList<>(), errors);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void knownLdapUserWithWrongPasswordIsRejectedWithoutFallback() {
        user.setAuthType(AuthType.LDAP);
        UserManager.storeUser(user);
        enableLdap(LdapProvisioningMode.JIT);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(user.getLogin(), "wrong-password",
                true, true, new ArrayList<>(), errors);

        assertNull(result);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void onlyPrefixedLdapGroupsAreMappedAndThePrefixIsStripped() throws Exception {
        // RFC v1: only directory groups starting with "uyuni_" take part in role mapping, and the
        // prefix is stripped before the external-group lookup. A directory group without the prefix
        // must be ignored even when an external group of that exact name exists.
        //
        // UserTestUtils permanently grants IMPLIEDROLES (CHANNEL_ADMIN, CONFIG_ADMIN, ...).
        // UserManager.resetTemporaryRoles skips any role the user already has, so those permanent
        // grants would hide LDAP temporary-role updates. Strip them first so the temporary-role
        // assertions below actually observe the mapping result.
        UserFactory.IMPLIEDROLES.forEach(user::removePermanentRole);
        user.setAuthType(AuthType.LDAP);
        UserManager.storeUser(user);
        addGroup("uyuni_" + PREFIXED_EXT_GROUP, user.getLogin());
        addGroup(UNPREFIXED_GROUP, user.getLogin());
        mapExtGroupToRole(PREFIXED_EXT_GROUP, RoleFactory.CHANNEL_ADMIN);
        mapExtGroupToRole(UNPREFIXED_GROUP, RoleFactory.CONFIG_ADMIN);
        enableLdap(LdapProvisioningMode.JIT);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(user.getLogin(), "existing-secret",
                true, true, new ArrayList<>(), errors);

        assertNotNull(result);
        assertTrue(errors.isEmpty());
        assertTrue(result.getTemporaryRoles().contains(RoleFactory.CHANNEL_ADMIN),
                "uyuni_-prefixed group should map through the stripped label");
        assertFalse(result.getTemporaryRoles().contains(RoleFactory.CONFIG_ADMIN),
                "group without the uyuni_ prefix must be ignored");
    }

    @Test
    public void unknownUserIsProvisionedJustInTime() {
        enableLdap(LdapProvisioningMode.JIT);

        List<String> messages = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(JIT_LOGIN, JIT_PASSWORD, true,
                true, messages, new ArrayList<>());

        assertNotNull(result);
        assertEquals(JIT_LOGIN, result.getLogin());
        assertEquals(AuthType.LDAP, result.getAuthType());
        assertNotNull(UserFactory.lookupByLogin(JIT_LOGIN));
    }

    @Test
    public void knownUserRecordsTheDirectoryThatAuthenticatedThem() {
        user.setAuthType(AuthType.LDAP);
        UserManager.storeUser(user);
        LdapAuthServer server = enableLdapFromDatabase(LdapProvisioningMode.JIT, true);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(user.getLogin(), "existing-secret",
                true, true, new ArrayList<>(), errors);

        assertNotNull(result);
        assertTrue(errors.isEmpty());
        assertEquals(server.getId(), result.getLdapServerId());
    }

    @Test
    public void provisionedUserRecordsTheDirectoryAndHonoursTheRegularUserOption() {
        // With auto-join turned off, access is meant to come entirely from LDAP group mapping, so
        // the account must not keep the regular_user group every locally created user gets.
        LdapAuthServer server = enableLdapFromDatabase(LdapProvisioningMode.JIT, false);

        List<String> messages = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(JIT_LOGIN, JIT_PASSWORD, true,
                true, messages, new ArrayList<>());

        assertNotNull(result);
        assertEquals(AuthType.LDAP, result.getAuthType());
        assertEquals(server.getId(), result.getLdapServerId());
        assertFalse(result.isMemberOf(AccessGroupFactory.REGULAR_USER));
    }

    @Test
    public void provisionedUserKeepsTheRegularUserGroupByDefault() {
        enableLdapFromDatabase(LdapProvisioningMode.JIT, true);

        User result = LoginHelper.checkLdapAuthentication(JIT_LOGIN, JIT_PASSWORD, true,
                true, new ArrayList<>(), new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isMemberOf(AccessGroupFactory.REGULAR_USER));
    }

    @Test
    public void unknownUserIsNotProvisionedWhenExistingOnly() {
        enableLdap(LdapProvisioningMode.EXISTING_ONLY);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(JIT_LOGIN, JIT_PASSWORD, true,
                true, new ArrayList<>(), errors);

        assertNull(result);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void unknownUserIsNotProvisionedWhenJitDisallowedOnEntryPoint() {
        // XML-RPC entry point (allowJit = false): even with JIT provisioning configured on the
        // server, an unknown user must not be created; the call declines so local auth rejects it.
        enableLdap(LdapProvisioningMode.JIT);

        List<String> errors = new ArrayList<>();
        User result = LoginHelper.checkLdapAuthentication(JIT_LOGIN, JIT_PASSWORD, true,
                false, new ArrayList<>(), errors);

        assertNull(result);
        assertTrue(errors.isEmpty());
        assertThrows(LookupException.class, () -> UserFactory.lookupByLogin(JIT_LOGIN));
    }

    /**
     * Minimal in-test {@link LdapAuthConfigProvider} returning a fixed enabled flag and server list.
     */
    private static final class StubProvider implements LdapAuthConfigProvider {
        private final boolean enabled;
        private final List<LdapAuthServerSettings> servers;

        private StubProvider(boolean enabledIn, List<LdapAuthServerSettings> serversIn) {
            this.enabled = enabledIn;
            this.servers = serversIn;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public List<LdapAuthServerSettings> getServers() {
            return servers;
        }
    }
}
