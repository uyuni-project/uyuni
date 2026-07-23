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

package com.suse.manager.ldap;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * {@link LdapAuthenticationService} backed by the UnboundID LDAP SDK.
 *
 * <p>Implements the standard bind/search/bind sequence:</p>
 * <ol>
 *   <li>Bind a pooled connection as the service account (or anonymously, when allowed).</li>
 *   <li>Search for the user under the base DN with the configured filter, requiring exactly one
 *       match.</li>
 *   <li>Reject an empty supplied password outright, then bind as the discovered user DN with the
 *       supplied password on a separate connection.</li>
 *   <li>Resolve the user's group memberships and normalize them to the configured label
 *       attribute.</li>
 *   <li>Return the login, profile attributes, and group labels.</li>
 * </ol>
 *
 * <p>User input is never concatenated into a filter; see {@link LdapFilters}.</p>
 */
public class UnboundIdLdapAuthenticationService implements LdapAuthenticationService {

    private static final Logger LOG = LogManager.getLogger(UnboundIdLdapAuthenticationService.class);

    private final LdapServerConfig config;
    private final LdapConnectionFactory connectionFactory;

    /**
     * @param configIn the server this service authenticates against
     * @param connectionFactoryIn the factory used to open connections and the search pool
     */
    public UnboundIdLdapAuthenticationService(LdapServerConfig configIn,
                                              LdapConnectionFactory connectionFactoryIn) {
        this.config = configIn;
        this.connectionFactory = connectionFactoryIn;
    }

    @Override
    public Optional<LdapUser> authenticate(String login, String password) throws LdapException {
        if (login == null || login.isBlank()) {
            LOG.debug("Rejecting LDAP authentication with an empty login");
            return Optional.empty();
        }
        // Reject an empty password before any bind. Many directories treat a bind with a valid
        // DN and an empty password as a successful unauthenticated bind, which must never happen.
        if (password == null || password.isEmpty()) {
            LOG.debug("Rejecting LDAP authentication for [{}] because the password is empty", login);
            return Optional.empty();
        }

        try (LDAPConnectionPool pool = connectionFactory.createServicePool(config)) {
            SearchResultEntry entry = findUser(pool, login);
            if (entry == null) {
                LOG.info("LDAP authentication failed for [{}]: no matching directory entry", login);
                return Optional.empty();
            }

            if (!credentialBindSucceeds(entry.getDN(), password)) {
                LOG.info("LDAP authentication failed for [{}]: credential bind rejected", login);
                return Optional.empty();
            }

            List<String> groups = findGroups(pool, entry.getDN(), login);
            LdapUser user = toUser(login, entry, groups);
            LOG.info("LDAP authentication succeeded for [{}] with {} group label(s)", login, groups.size());
            return Optional.of(user);
        }
    }

    private SearchResultEntry findUser(LDAPConnectionPool pool, String login) throws LdapException {
        Filter filter = LdapFilters.userFilter(config.getUserFilter(), login);
        try {
            SearchResult result = pool.search(config.getUserBaseDn(), SearchScope.SUB, filter,
                    config.getLoginAttribute(), config.getFirstNameAttribute(),
                    config.getLastNameAttribute(), config.getEmailAttribute());
            int count = result.getEntryCount();
            if (count > 1) {
                LOG.warn("LDAP authentication failed for [{}]: ambiguous user search (base DN={}, filter={}, " +
                        "matched {} entries)", login, config.getUserBaseDn(), filter, count);
                return null;
            }
            return count == 1 ? result.getSearchEntries().get(0) : null;
        }
        catch (LDAPException e) {
            throw new LdapException("User search failed for [" + login + "]", e);
        }
    }

    private boolean credentialBindSucceeds(String userDn, String password) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = connectionFactory.openConnection(config);
            ResultCode code = connection.bind(userDn, password).getResultCode();
            return ResultCode.SUCCESS.equals(code);
        }
        catch (LDAPException e) {
            if (ResultCode.INVALID_CREDENTIALS.equals(e.getResultCode())) {
                // A wrong password is a normal, expected authentication rejection.
                return false;
            }
            // Any other result code (server unreachable, timeout, TLS failure, etc.) is an
            // infrastructure problem the caller must be able to tell apart from a wrong password,
            // so it is surfaced as an LdapException rather than silently reported as a failed login.
            throw new LdapException("Credential bind failed for [" + userDn + "]", e);
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<String> findGroups(LDAPConnectionPool pool, String userDn, String login) {
        Filter filter;
        try {
            filter = LdapFilters.groupFilter(config.getGroupFilter(), userDn);
        }
        catch (LdapException e) {
            LOG.warn("LDAP group lookup skipped for [{}]: invalid group filter (user DN={})", login, userDn, e);
            return List.of();
        }
        try {
            SearchResult result = pool.search(config.getGroupBaseDn(), SearchScope.SUB, filter,
                    config.getGroupNameAttribute());
            List<String> labels = new ArrayList<>();
            for (SearchResultEntry entry : result.getSearchEntries()) {
                String label = entry.getAttributeValue(config.getGroupNameAttribute());
                if (label != null && !label.isBlank()) {
                    labels.add(label);
                }
            }
            labels.sort(Comparator.naturalOrder());
            return labels;
        }
        catch (LDAPException e) {
            LOG.warn("LDAP group lookup failed for [{}] (base DN={}, filter={}): {}",
                    login, config.getGroupBaseDn(), filter, e.getMessage(), e);
            return List.of();
        }
    }

    private LdapUser toUser(String login, SearchResultEntry entry, List<String> groups) {
        String normalizedLogin = Optional.ofNullable(entry.getAttributeValue(config.getLoginAttribute()))
                .filter(value -> !value.isBlank())
                .orElse(login);
        return new LdapUser(
                normalizedLogin,
                entry.getDN(),
                entry.getAttributeValue(config.getFirstNameAttribute()),
                entry.getAttributeValue(config.getLastNameAttribute()),
                entry.getAttributeValue(config.getEmailAttribute()),
                groups);
    }
}
