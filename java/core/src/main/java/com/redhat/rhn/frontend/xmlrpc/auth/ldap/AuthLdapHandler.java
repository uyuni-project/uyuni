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

package com.redhat.rhn.frontend.xmlrpc.auth.ldap;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.admin.LdapAdminManager;
import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ApiType;
import com.suse.manager.api.ReadOnly;
import com.suse.manager.ldap.LdapServiceException;
import com.suse.manager.webui.controllers.admin.mappers.LdapResponseMappers;

import java.util.List;
import java.util.Map;

/**
 * XML-RPC surface for native LDAP directory administration (RFC {@code auth.ldap}).
 *
 * <p>CRUD of directory records stays on the HTTP Setup Wizard API. This namespace exposes
 * list/detail plus the three admin test operations so spacecmd and XML-RPC clients can
 * troubleshoot a configured directory without the Web UI.</p>
 *
 * @apidoc.namespace auth.ldap
 * @apidoc.doc Provides methods to inspect configured LDAP/AD directories and run the
 * connection, user-lookup, and group-resolution tests.
 */
public class AuthLdapHandler extends BaseHandler {

    private final LdapAdminManager ldapAdminManager;

    /**
     * Default constructor.
     */
    public AuthLdapHandler() {
        this(new LdapAdminManager());
    }

    /**
     * @param ldapAdminManagerIn manager used by the methods
     */
    public AuthLdapHandler(LdapAdminManager ldapAdminManagerIn) {
        this.ldapAdminManager = ldapAdminManagerIn;
    }

    /**
     * Lists configured LDAP servers.
     * @param loggedInUser the current user
     * @return server summaries, ordered by priority
     *
     * @apidoc.doc List configured LDAP directory servers. Satellite Administrator only.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *   #array_begin()
     *     #struct_begin("ldap_server")
     *       #prop("int", "id")
     *       #prop("string", "label")
     *       #prop("string", "host")
     *       #prop("int", "port")
     *       #prop("string", "transport")
     *       #prop("string", "serverType")
     *       #prop("boolean", "enabled")
     *       #prop("int", "priority")
     *       #prop("string", "provisioningMode")
     *     #struct_end()
     *   #array_end()
     */
    @ApiIgnore(ApiType.HTTP)
    @ReadOnly
    public List<Map<String, Object>> list(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return LdapResponseMappers.mapResumeFromDB(ldapAdminManager.list());
    }

    /**
     * Returns one LDAP server in full detail (never includes the bind password).
     * @param loggedInUser the current user
     * @param id the server id
     * @return server details
     *
     * @apidoc.doc Get one LDAP directory by id. Satellite Administrator only.
     * Bind passwords are never returned.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "id")
     * @apidoc.returntype
     *   #struct_begin("ldap_server")
     *     #prop("int", "id")
     *     #prop("string", "label")
     *     #prop("string", "host")
     *     #prop("int", "port")
     *     #prop("string", "transport")
     *     #prop("string", "serverType")
     *     #prop("boolean", "enabled")
     *     #prop("int", "priority")
     *     #prop("string", "provisioningMode")
     *   #struct_end()
     */
    @ApiIgnore(ApiType.HTTP)
    @ReadOnly
    public Map<String, Object> getDetails(User loggedInUser, Integer id) {
        ensureSatAdmin(loggedInUser);
        try {
            return LdapResponseMappers.mapFullFromDB(ldapAdminManager.get(id.longValue()));
        }
        catch (LookupException e) {
            throw noSuchServer(id, e);
        }
    }

    /**
     * Tests the service bind to a configured directory.
     * @param loggedInUser the current user
     * @param id the server id
     * @return 1 on success
     *
     * @apidoc.doc Test the service-account bind to the given LDAP server.
     * Satellite Administrator only.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "id")
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.HTTP)
    public int testConnection(User loggedInUser, Integer id) {
        ensureSatAdmin(loggedInUser);
        try {
            ldapAdminManager.testConnection(id.longValue());
            return 1;
        }
        catch (LookupException e) {
            throw noSuchServer(id, e);
        }
        catch (LdapServiceException e) {
            throw ldapFault("LDAP connection test failed", e);
        }
    }

    /**
     * Looks up a directory user by login (no password bind).
     * @param loggedInUser the current user
     * @param id the server id
     * @param login the login to search for
     * @return user attributes
     *
     * @apidoc.doc Look up a directory user by login. Satellite Administrator only.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "id")
     * @apidoc.param #param("string", "login")
     * @apidoc.returntype
     *   #struct_begin("ldap_user")
     *     #prop("string", "login")
     *     #prop("string", "dn")
     *     #prop("string", "firstName")
     *     #prop("string", "lastName")
     *     #prop("string", "email")
     *   #struct_end()
     */
    @ApiIgnore(ApiType.HTTP)
    public Map<String, Object> testUserLookup(User loggedInUser, Integer id, String login) {
        ensureSatAdmin(loggedInUser);
        try {
            return ldapAdminManager.testUserLookup(id.longValue(), login);
        }
        catch (LookupException e) {
            throw noSuchServer(id, e);
        }
        catch (ValidatorException | IllegalArgumentException e) {
            throw ldapFault(e.getMessage(), e);
        }
        catch (LdapServiceException e) {
            throw ldapFault("LDAP user lookup failed", e);
        }
    }

    /**
     * Looks up a directory user and resolves group membership.
     * @param loggedInUser the current user
     * @param id the server id
     * @param login the login to search for
     * @return user attributes including group labels
     *
     * @apidoc.doc Resolve directory group membership for a login. Satellite Administrator only.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "id")
     * @apidoc.param #param("string", "login")
     * @apidoc.returntype
     *   #struct_begin("ldap_user")
     *     #prop("string", "login")
     *     #prop("string", "dn")
     *     #prop("string", "firstName")
     *     #prop("string", "lastName")
     *     #prop("string", "email")
     *     #prop_array("string", "groupLabels", "Directory group CNs")
     *   #struct_end()
     */
    @ApiIgnore(ApiType.HTTP)
    public Map<String, Object> testGroupResolution(User loggedInUser, Integer id, String login) {
        ensureSatAdmin(loggedInUser);
        try {
            return ldapAdminManager.testGroupResolution(id.longValue(), login);
        }
        catch (LookupException e) {
            throw noSuchServer(id, e);
        }
        catch (ValidatorException | IllegalArgumentException e) {
            throw ldapFault(e.getMessage(), e);
        }
        catch (LdapServiceException e) {
            throw ldapFault("LDAP group resolution failed", e);
        }
    }

    private static FaultException noSuchServer(Integer id, Throwable cause) {
        return new FaultException(2851, "noSuchLdapServer",
                "LDAP server not found for id: " + id, cause);
    }

    private static FaultException ldapFault(String message, Throwable cause) {
        return new FaultException(2852, "ldapOperationFailed", message, cause);
    }
}
