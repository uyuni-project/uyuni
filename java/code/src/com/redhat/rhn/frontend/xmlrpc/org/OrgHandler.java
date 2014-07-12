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
package com.redhat.rhn.frontend.xmlrpc.org;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataList;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgConfig;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.MultiOrgEntitlementsDto;
import com.redhat.rhn.frontend.dto.OrgChannelFamily;
import com.redhat.rhn.frontend.dto.OrgDto;
import com.redhat.rhn.frontend.dto.OrgEntitlementDto;
import com.redhat.rhn.frontend.dto.OrgSoftwareEntitlementDto;
import com.redhat.rhn.frontend.dto.SystemEntitlementsDto;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.MigrationToSameOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.OrgNotInTrustException;
import com.redhat.rhn.frontend.xmlrpc.PamAuthNotConfiguredException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.SatelliteOrgException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.org.CreateOrgCommand;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.org.OrgManager;
import com.redhat.rhn.manager.org.UpdateOrgSoftwareEntitlementsCommand;
import com.redhat.rhn.manager.org.UpdateOrgSystemEntitlementsCommand;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OrgHandler
 *
 * @version $Rev$
 *
 * @xmlrpc.namespace org
 * @xmlrpc.doc Contains methods to access common organization management
 * functions available from the web interface.
 */
public class OrgHandler extends BaseHandler {

    private static final String VALIDATION_XSD =
            "/com/redhat/rhn/frontend/action/multiorg/validation/orgCreateForm.xsd";
    private static final String ORG_ID_KEY = "org_id";
    private static final String ORG_NAME_KEY = "org_name";
    private static final String ALLOCATED_KEY = "allocated";
    private static final String UN_ALLOCATED_KEY = "unallocated";
    private static final String USED_KEY = "used";
    private static final String FREE_KEY = "free";
    private static Logger log = Logger.getLogger(OrgHandler.class);

    protected boolean availableInRestrictedPeriod() {
        return true;
    }

    /**
     * Create a new organization.
     * @param loggedInUser The current user
     * @param orgName Organization name. Must meet same criteria as in the web UI.
     * @param adminLogin New administrator login name for the new org.
     * @param adminPassword New administrator password.
     * @param prefix New administrator's prefix.
     * @param firstName New administrator's first name.
     * @param lastName New administrator's last name.
     * @param email New administrator's e-mail.
     * @param usePamAuth Should PAM authentication be used for new administrators account.
     * @return Newly created organization object.
     *
     * @xmlrpc.doc Create a new organization and associated administrator account.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "orgName", "Organization name. Must meet same
     * criteria as in the web UI.")
     * @xmlrpc.param #param_desc("string", "adminLogin", "New administrator login name.")
     * @xmlrpc.param #param_desc("string", "adminPassword", "New administrator password.")
     * @xmlrpc.param #param_desc("string", "prefix", "New administrator's prefix. Must
     * match one of the values available in the web UI. (i.e. Dr., Mr., Mrs., Sr., etc.)")
     * @xmlrpc.param #param_desc("string", "firstName", "New administrator's first name.")
     * @xmlrpc.param #param_desc("string", "lastName", "New administrator's first name.")
     * @xmlrpc.param #param_desc("string", "email", "New administrator's e-mail.")
     * @xmlrpc.param #param_desc("boolean", "usePamAuth", "True if PAM authentication
     * should be used for the new administrator account.")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto create(User loggedInUser, String orgName, String adminLogin,
            String adminPassword, String prefix, String firstName, String lastName,
            String email, Boolean usePamAuth) {
        log.debug("OrgHandler.create");
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);

        validateCreateOrgData(orgName, adminPassword, firstName, lastName, email,
                usePamAuth);

        CreateOrgCommand cmd = new CreateOrgCommand(orgName, adminLogin, adminPassword,
                email);
        cmd.setFirstName(firstName);
        cmd.setLastName(lastName);
        cmd.setPrefix(prefix);

        String pamAuthService = Config.get().getString(ConfigDefaults.WEB_PAM_AUTH_SERVICE);
        if (usePamAuth) {
            if (pamAuthService != null && pamAuthService.trim().length() > 0) {
                cmd.setUsePam(usePamAuth);
            }
            else {
                // The user wants to use pam authentication, but the server has not been
                // configured to use pam... Throw an error...
                throw new PamAuthNotConfiguredException();
            }
        }

        ValidatorError[] verrors = cmd.store();
        if (verrors != null) {
            throw new ValidationException(verrors[0].getMessage());
        }

        return OrgManager.toDetailsDto(cmd.getNewOrg());
    }

    private void validateCreateOrgData(String orgName, String password, String firstName,
            String lastName, String email, Boolean usePamAuth) {

        Map<String, String> values = new HashMap<String, String>();
        values.put("orgName", orgName);
        values.put("desiredPassword", password);
        values.put("desiredPasswordConfirm", password);
        values.put("firstNames", firstName);
        values.put("lastName", lastName);

        ValidatorResult result = RhnValidationHelper.validate(this.getClass(),
                values, new LinkedList<String>(values.keySet()), VALIDATION_XSD);

        if (!result.isEmpty()) {
            log.error("Validation errors:");
            for (ValidatorError error : result.getErrors()) {
                log.error("   " + error.getMessage());
            }
            // Multiple errors could return here, but we'll have to just throw an
            // exception for the first one and return that to the user.
            ValidatorError e = result.getErrors().get(0);
            throw new ValidationException(e.getMessage());
        }

        if (!usePamAuth && StringUtils.isEmpty(password)) {
            throw new FaultException(-501, "passwordRequiredOrUsePam",
                    "Password is required if not using PAM authentication");
        }
    }

    /**
     * Returns the list of organizations.
     * @param loggedInUser The current user
     * @return list of orgs.
     * @xmlrpc.doc Returns the list of organizations.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     * $OrgDtoSerializer
     */
    public List<OrgDto> listOrgs(User loggedInUser) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        return OrgManager.activeOrgs(loggedInUser);
    }

    /**
     * Delete an organization.
     *
     * @param loggedInUser The current user
     * @param orgId ID of organization to delete.
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Delete an organization. The default organization
     * (i.e. orgId=1) cannot be deleted.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);

        // Verify we're not trying to delete the default org (id 1):
        Org defaultOrg = OrgFactory.getSatelliteOrg();
        if (orgId.longValue() == defaultOrg.getId().longValue()) {
            throw new SatelliteOrgException();
        }

        OrgFactory.deleteOrg(org.getId(), loggedInUser);

        return 1;
    }

    /**
     * Ensure the org exists
     * @param orgId the org id to check
     * @return the org
     */
    public static Org verifyOrgExists(Number orgId) {
        if (orgId == null) {
            throw new NoSuchOrgException("null Id");
        }
        Org org = OrgFactory.lookupById(orgId.longValue());
        if (org == null) {
            throw new NoSuchOrgException(orgId.toString());
        }
        return org;
    }

    private Org verifyOrgExists(String name) {
        Org org = OrgFactory.lookupByName(name);
        if (org == null) {
            throw new NoSuchOrgException(name);
        }
        return org;
    }

    private Entitlement verifyEntitlementExists(String sysLabel) {
        Entitlement ent = EntitlementManager.getByName(sysLabel);
        if (ent == null) {
            throw new NoSuchEntitlementException(sysLabel);
        }
        return ent;
    }

    /**
     * Returns the list of active users in a given organization
     * @param loggedInUser The current user
     * @param orgId the orgId of the organization to lookup on.
     * @return the list of users in a organization.
     * @xmlrpc.doc Returns the list of users in a given organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *   #array()
     *     $MultiOrgUserOverviewSerializer
     *   #array_end()
     */
    public List listUsers(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        verifyOrgExists(orgId);
        return OrgManager.activeUsers(Long.valueOf(orgId));
    }

    /**
     * Returns the detailed information about an organization
     * given the org_id.
     * @param loggedInUser The current user
     * @param orgId the orgId of the organization to lookup on.
     * @return the list of users in a organization.
     *
     * @xmlrpc.doc The detailed information about an organization given
     * the organization ID.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto getDetails(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        return OrgManager.toDetailsDto(verifyOrgExists(orgId));
    }

    /**
     * Returns the detailed information about an organization
     * given the org_name.
     * @param loggedInUser The current user
     * @param name the name of the organization to lookup on.
     * @return the list of users in a organization.
     *
     * @xmlrpc.doc The detailed information about an organization given
     * the organization name.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto getDetails(User loggedInUser, String name) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        return OrgManager.toDetailsDto(verifyOrgExists(name));
    }

    /**
     *
     * @param loggedInUser The current user
     * @param orgId the orgId of the organization to set name on
     * @param name the new name for the org.
     * @return the updated org.
     *
     * @xmlrpc.doc Updates the name of an organization
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "name", "Organization name. Must meet same
     * criteria as in the web UI.")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto updateName(User loggedInUser, Integer orgId, String name) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        if (!org.getName().equals(name)) {
            try {
                OrgManager.checkOrgName(name);
                org.setName(name);
            }
            catch (ValidatorException ve) {
                throw new ValidationException(ve.getMessage());
            }
        }
        return OrgManager.toDetailsDto(org);
    }

    /**
     * Lists software entitlement allocation/distribution information
     *  across all organizations.
     * User needs to be a satellite administrator to get this information
     * @param loggedInUser The current user
     * @return Array of MultiOrgEntitlementsDto.
     *
     * @xmlrpc.doc List software entitlement allocation information
     * across all organizations.
     * Caller must be a satellite administrator.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *   #array()
     *      $MultiOrgEntitlementsDtoSerializer
     *   #array_end()
     */
    public List<MultiOrgEntitlementsDto> listSoftwareEntitlements(User loggedInUser) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        return ChannelManager.entitlementsForAllMOrgs();
    }


    /**
     * List an organization's allocation for each software entitlement.
     * A value of -1 indicates unlimited entitlements.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID
     * @return Array of maps.
     *
     * @xmlrpc.doc List an organization's allocation of each software entitlement.
     * A value of -1 indicates unlimited entitlements.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *   #array()
     *      $OrgChannelFamilySerializer
     *   #array_end()
     */
    public List<OrgChannelFamily> listSoftwareEntitlementsForOrg(User loggedInUser,
            Integer orgId) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);

        return ChannelManager.listChannelFamilySubscriptionsFor(org);
    }

    /**
     * List each organization's allocation of a given software entitlement.
     * Organizations with no allocations will not be present in the list. A value of -1
     * indicates unlimited entitlements.
     *
     * @param loggedInUser The current user
     * @param channelFamilyLabel Software entitlement label.
     * @return Array of maps.
     * @deprecated being replaced by listSoftwareEntitlements(string sessionKey,
     * string label, boolean includeUnentitled)
     *
     * @xmlrpc.doc List each organization's allocation of a given software entitlement.
     * Organizations with no allocation will not be present in the list. A value of -1
     * indicates unlimited entitlements.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "label", "Software entitlement label.")
     * @xmlrpc.returntype
     *   #array()
     *     $OrgSoftwareEntitlementDtoSerializer
     *   #array_end()
     */
    @Deprecated
    public List<OrgSoftwareEntitlementDto> listSoftwareEntitlements(User loggedInUser,
            String channelFamilyLabel) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);

        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(channelFamilyLabel, null);
        if (cf == null) {
            throw new InvalidEntitlementException();
        }
        return ChannelManager.listEntitlementsForAllOrgs(cf, loggedInUser);
    }

    /**
     * List each organization's allocation of a given software entitlement.
     * A value of -1 indicates unlimited entitlements.
     *
     * @param loggedInUser The current user
     * @param channelFamilyLabel Software entitlement label.
     * @param includeUnentitled If true, the result will include both organizations
     * that have the entitlement as well as those that do not; otherwise, the
     * result will only include organizations that have the entitlement.
     * @return Array of maps.
     * @since 10.4
     *
     * @xmlrpc.doc List each organization's allocation of a given software entitlement.
     * A value of -1 indicates unlimited entitlements.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "label", "Software entitlement label.")
     * @xmlrpc.param #param_desc("boolean", "includeUnentitled", "If true, the
     * result will include both organizations that have the entitlement as well as
     * those that do not; otherwise, the result will only include organizations
     * that have the entitlement.")
     * @xmlrpc.returntype
     *   #array()
     *     $OrgSoftwareEntitlementDtoSerializer
     *   #array_end()
     */
    public List<OrgSoftwareEntitlementDto> listSoftwareEntitlements(User loggedInUser,
            String channelFamilyLabel, Boolean includeUnentitled) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);

        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(channelFamilyLabel, null);
        if (cf == null) {
            throw new InvalidEntitlementException();
        }

        if (includeUnentitled) {
            return ChannelManager.listEntitlementsForAllOrgsWithEmptyOrgs(cf, loggedInUser);
        }
        return ChannelManager.listEntitlementsForAllOrgs(cf, loggedInUser);
    }

    /**
     * Set an organizations entitlement allocation for a channel family.
     *
     * If increasing the entitlement allocation, the default organization
     * must have a sufficient number of free entitlements.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set allocation for.
     * @param channelFamilyLabel Channel family to set allocation for.
     * @param allocation New entitlement allocation.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set an organization's entitlement allocation for the given software
     * entitlement.
     *
     * If increasing the entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "label", "Software entitlement label.")
     * @xmlrpc.param #param("int", "allocation")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSoftwareEntitlements(User loggedInUser, Integer orgId,
            String channelFamilyLabel, Integer allocation) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        lookupChannelFamily(channelFamilyLabel);

        UpdateOrgSoftwareEntitlementsCommand cmd =
                new UpdateOrgSoftwareEntitlementsCommand(channelFamilyLabel, org,
                        Long.valueOf(allocation), null);
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new ValidationException(ve.getMessage());
        }

        return 1;
    }

    /**
     * Set an organizations entitlement allocation for a channel family.
     *
     * If increasing the entitlement allocation, the default organization
     * must have a sufficient number of free entitlements.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set allocation for.
     * @param channelFamilyLabel Channel family to set allocation for.
     * @param allocation New  flex entitlement allocation.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set an organization's flex entitlement allocation for the given software
     * entitlement.
     *
     * If increasing the flex entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free flex entitlements.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "label", "Software entitlement label.")
     * @xmlrpc.param #param("int", "allocation")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSoftwareFlexEntitlements(User loggedInUser, Integer orgId,
            String channelFamilyLabel, Integer allocation) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        lookupChannelFamily(channelFamilyLabel);

        UpdateOrgSoftwareEntitlementsCommand cmd =
                new UpdateOrgSoftwareEntitlementsCommand(channelFamilyLabel, org,
                        null, Long.valueOf(allocation));
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new ValidationException(ve.getMessage());
        }

        return 1;
    }



    /**
     * Lookup a channel family, throwing an exception if it cannot be found.
     *
     * @param channelFamilyLabel Channel family label to look up.
     */
    private ChannelFamily lookupChannelFamily(String channelFamilyLabel) {
        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(channelFamilyLabel, null);
        if (cf == null) {
            throw new InvalidEntitlementException();
        }
        return cf;
    }

    /**
     * Lists system entitlement allocation/distribution information
     *  across all organizations.
     * User needs to be a satellite administrator to get this information
     * @param loggedInUser The current user
     * @return Array of SystemEntitlementsDtoSerializer.
     *
     * @xmlrpc.doc Lists system entitlement allocation information
     * across all organizations.
     * Caller must be a satellite administrator.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *   #array()
     *     $SystemEntitlementsDtoSerializer
     *   #array_end()
     */
    public List<SystemEntitlementsDto> listSystemEntitlements(User loggedInUser) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        return OrgManager.allOrgsEntitlements();
    }

    /**
     * List an organization's allocation of a system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     *
     * @param loggedInUser The current user
     * @param label system entitlement label
     * @return a list of Maps having the system entitlements info.
     * @deprecated being replaced by listSystemEntitlements(string sessionKey,
     * string label, boolean includeUnentitled)
     *
     * @xmlrpc.doc List each organization's allocation of a system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype
     *   #array()
     *     #struct("entitlement usage")
     *       #prop("int", "org_id")
     *       #prop("string", "org_name")
     *       #prop("int", "allocated")
     *       #prop("int", "unallocated")
     *       #prop("int", "used")
     *       #prop("int", "free")
     *     #struct_end()
     *   #array_end()
     */
    @Deprecated
    public List<Map> listSystemEntitlements(User loggedInUser,
            String label) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        verifyEntitlementExists(label);
        DataList<Map> result = OrgManager.allOrgsSingleEntitlement(label);
        List<Map> details = new LinkedList<Map>();
        for (Map row : result) {
            Map <String, Object> map = new HashMap<String, Object>();
            Org org = OrgFactory.lookupById((Long)row.get("orgid"));
            map.put(ORG_ID_KEY, new Integer(org.getId().intValue()));
            map.put(ORG_NAME_KEY, org.getName());
            map.put(ALLOCATED_KEY, ((Long)row.get("total")).intValue());
            map.put(USED_KEY, row.get("usage"));
            long free  = (Long)row.get("total") - (Long)row.get("usage");
            map.put(FREE_KEY, free);
            long unallocated  = (Long)row.get("upper") - (Long)row.get("total");
            map.put(UN_ALLOCATED_KEY, unallocated);
            details.add(map);
        }
        return details;
    }

    /**
     * List an organization's allocation of a system entitlement.
     *
     * @param loggedInUser The current user
     * @param label System entitlement label.
     * @param includeUnentitled If true, the result will include both organizations
     * that have the entitlement as well as those that do not; otherwise, the
     * result will only include organizations that have the entitlement.
     * @return a list of Maps having the system entitlements info.
     * @since 10.4
     *
     * @xmlrpc.doc List each organization's allocation of a system entitlement.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param_desc("boolean", "includeUnentitled", "If true, the
     * result will include both organizations that have the entitlement as well as
     * those that do not; otherwise, the result will only include organizations
     * that have the entitlement.")
     * @xmlrpc.returntype
     *   #array()
     *     #struct("entitlement usage")
     *       #prop("int", "org_id")
     *       #prop("string", "org_name")
     *       #prop("int", "allocated")
     *       #prop("int", "unallocated")
     *       #prop("int", "used")
     *       #prop("int", "free")
     *     #struct_end()
     *   #array_end()
     */
    public List<Map> listSystemEntitlements(User loggedInUser,
            String label, Boolean includeUnentitled) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        verifyEntitlementExists(label);

        DataList<Map> result = null;
        if (includeUnentitled) {
            result = OrgManager.allOrgsSingleEntitlementWithEmptyOrgs(label);
        }
        else {
            result = OrgManager.allOrgsSingleEntitlement(label);
        }

        List<Map> details = new LinkedList<Map>();
        for (Map row : result) {
            Map <String, Object> map = new HashMap<String, Object>();
            Org org = OrgFactory.lookupById((Long)row.get("orgid"));
            map.put(ORG_ID_KEY, new Integer(org.getId().intValue()));
            map.put(ORG_NAME_KEY, org.getName());
            map.put(ALLOCATED_KEY, ((Long)row.get("total")).intValue());
            map.put(USED_KEY, row.get("usage"));
            long free  = (Long)row.get("total") - (Long)row.get("usage");
            map.put(FREE_KEY, free);
            long unallocated  = (Long)row.get("upper") - (Long)row.get("total");
            map.put(UN_ALLOCATED_KEY, unallocated);
            details.add(map);
        }
        return details;
    }

    /**
     * List an organization's allocations of each system entitlement.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID
     * @return Array of maps.
     *
     * @xmlrpc.doc List an organization's allocation of each system entitlement.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *   #array()
     *     $OrgEntitlementDtoSerializer
     *   #array_end()
     */
    public List<OrgEntitlementDto> listSystemEntitlementsForOrg(User loggedInUser,
            Integer orgId)  {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        return OrgManager.listEntitlementsFor(org);
    }

    /**
     * Set an organizations entitlement allocation for a channel family.
     *
     * If increasing the entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set allocation for.
     * @param systemEntitlementLabel System entitlement to set allocation for.
     * @param allocation New entitlement allocation.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set an organization's entitlement allocation for the given
     * software entitlement.
     *
     * If increasing the entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "label", "System entitlement label.
     * Valid values include:")
     *   #options()
     *     #item("enterprise_entitled")
     *     #item("monitoring_entitled")
     *     #item("provisioning_entitled")
     *     #item("virtualization_host")
     *     #item("virtualization_host_platform")
     *   #options_end()
     * @xmlrpc.param #param("int", "allocation")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSystemEntitlements(User loggedInUser, Integer orgId,
            String systemEntitlementLabel, Integer allocation) {

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);

        Org org = verifyOrgExists(orgId);

        Entitlement ent = EntitlementManager.getByName(systemEntitlementLabel);
        if (ent == null || (!EntitlementManager.getAddonEntitlements().contains(ent) &&
                !EntitlementManager.getBaseEntitlements().contains(ent))) {
            throw new InvalidEntitlementException();
        }

        UpdateOrgSystemEntitlementsCommand cmd =
                new UpdateOrgSystemEntitlementsCommand(ent, org, new Long(allocation));
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new ValidationException(ve.getMessage());
        }

        return 1;
    }

    /**
     * Migrate systems from one organization to another.  If executed by
     * a Satellite administrator, the systems will be migrated from their current
     * organization to the organization specified by the toOrgId.  If executed by
     * an organization administrator, the systems must exist in the same organization
     * as that administrator and the systems will be migrated to the organization
     * specified by the toOrgId. In any scenario, the origination and destination
     * organizations must be defined in a trust.
     *
     * @param loggedInUser The current user
     * @param toOrgId destination organization ID.
     * @param sids System IDs.
     * @return list of systems migrated.
     * @throws FaultException A FaultException is thrown if:
     *   - The user performing the request is not an organization administrator
     *   - The user performing the request is not a satellite administrator, but the
     *     from org id is different than the user's org id.
     *   - The from and to org id provided are the same.
     *   - One or more of the servers provides do not exist
     *   - The origination or destination organization does not exist
     *   - The user is not defined in the destination organization's trust
     *
     * @xmlrpc.doc Migrate systems from one organization to another.  If executed by
     * a Satellite administrator, the systems will be migrated from their current
     * organization to the organization specified by the toOrgId.  If executed by
     * an organization administrator, the systems must exist in the same organization
     * as that administrator and the systems will be migrated to the organization
     * specified by the toOrgId. In any scenario, the origination and destination
     * organizations must be defined in a trust.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("int", "toOrgId", "ID of the organization where the
     * system(s) will be migrated to.")
     * @xmlrpc.param #array_single("int", "systemId")
     * @xmlrpc.returntype
     * #array_single("int", "serverIdMigrated")
     */
    public Object[] migrateSystems(User loggedInUser, Integer toOrgId,
            List<Integer> sids) throws FaultException {

        // the user executing the request must at least be an org admin to perform
        // a system migration
        ensureUserRole(loggedInUser, RoleFactory.ORG_ADMIN);

        Org toOrg = verifyOrgExists(toOrgId);

        List<Server> servers = new LinkedList<Server>();

        for (Integer sid : sids) {
            Long serverId = new Long(sid.longValue());
            Server server = null;
            try {
                server = ServerFactory.lookupById(serverId);

                // throw a no_such_system exception if the server was not found.
                if (server == null) {
                    throw new NoSuchSystemException("No such system - sid[" + sid + "]");
                }
            }
            catch (LookupException e) {
                throw new NoSuchSystemException("No such system - sid[" + sid + "]");
            }
            servers.add(server);

            // As a pre-requisite to performing the actual migration, verify that each
            // server that is planned for migration passes the criteria that follows.
            // If any of the servers fails that criteria, none will be migrated.

            // unless the user is a satellite admin, they are not permitted to migrate
            // systems from an org that they do not belong to
            if ((!loggedInUser.hasRole(RoleFactory.SAT_ADMIN)) &&
                    (!loggedInUser.getOrg().equals(server.getOrg()))) {
                throw new PermissionCheckFailureException(server);
            }

            // do not allow the user to migrate systems to/from the same org.  doing so
            // would essentially remove entitlements, channels...etc from the systems
            // being migrated.
            if (toOrg.equals(server.getOrg())) {
                throw new MigrationToSameOrgException(server);
            }

            // if the originating org is not defined within the destination org's trust
            // the migration should not be permitted.
            if (!toOrg.getTrustedOrgs().contains(server.getOrg())) {
                throw new OrgNotInTrustException(server);
            }
        }

        List<Long> serversMigrated = MigrationManager.migrateServers(loggedInUser,
                toOrg, servers);
        return serversMigrated.toArray();
    }

    /**
     * Get organization wide crash file size limit.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @return Returns the organization wide crash file size limit.
     *
     * @xmlrpc.doc Get the organization wide crash file size limit. The limit value
     * must be a non-negative number, zero means no limit.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype int - Crash file size limit.
     */
    public int getCrashFileSizeLimit(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        return org.getOrgConfig().getCrashFileSizelimit().intValue();
    }

    /**
     * Set organization wide crash file size limit.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @param limit The limit to set.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set the organization wide crash file size limit. The limit value
     * must be non-negative, zero means no limit.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("int", "limit", "The limit to set (non-negative value).")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setCrashFileSizeLimit(User loggedInUser, Integer orgId, Integer limit) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        if (limit < 0) {
            throw new InvalidParameterException("Limit value must be non-negative.");
        }
        org.getOrgConfig().setCrashFileSizelimit(new Long(limit.longValue()));

        return 1;
    }

    /**
     * Get the status of crash reporting settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @return Returns the status of crash reporting settings.
     *
     * @xmlrpc.doc Get the status of crash reporting settings for the given organization.
     * Returns true if enabled, false otherwise.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype boolean - Get the status of crash reporting settings.
     */
    public boolean isCrashReportingEnabled(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        return org.getOrgConfig().isCrashReportingEnabled();
    }

    /**
     * Set the status of crash reporting settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @param enable Boolean to indicate desired settings.
     * @return Returns 1 for successfull change, traceback otherwise.
     *
     * @xmlrpc.doc Set the status of crash reporting settings for the given organization.
     * Disabling crash reporting will automatically disable crash file upload.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("boolean", "enable", "Use true/false to enable/disable")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer setCrashReporting(User loggedInUser, Integer orgId,
                                     Boolean enable) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        if (enable) {
            org.getOrgConfig().setCrashReportingEnabled(enable);
        }
        else {
            org.getOrgConfig().setCrashReportingEnabled(false);
            org.getOrgConfig().setCrashfileUploadEnabled(false);
        }

        return 1;
    }

    /**
     * Get the status of crash file upload settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @return Returns the status of crash file upload settings.
     *
     * @xmlrpc.doc Get the status of crash file upload settings for the given organization.
     * Returns true if enabled, false otherwise.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype boolean - Get the status of crash file upload settings.
     */
    public boolean isCrashfileUploadEnabled(User loggedInUser, Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        return org.getOrgConfig().isCrashfileUploadEnabled();
    }

    /**
     * Set the status of crash file upload settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId Organization ID to set the limit for.
     * @param enable Boolean to indicate desired settings.
     * @return Returns 1 for successfull change, 0 if the change failed.
     *
     * @xmlrpc.doc Set the status of crash file upload settings for the given organization.
     * Modifying the settings is possible as long as crash reporting is enabled.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("boolean", "enable", "Use true/false to enable/disable")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer setCrashfileUpload(User loggedInUser, Integer orgId,
                                      Boolean enable) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        if (org.getOrgConfig().isCrashReportingEnabled()) {
            org.getOrgConfig().setCrashfileUploadEnabled(enable);
        }
        else {
            return 0;
        }

        return 1;
    }

    /**
     * Get the status of SCAP detailed result file upload settings for the given
     * organization.
     *
     * @param loggedInUser The current user
     * @param orgId ID of organization to query.
     * @return Returns the status of SCAP detailed result file upload settings.
     *
     * @xmlrpc.doc Get the status of SCAP detailed result file upload settings
     * for the given organization.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *     #struct("scap_upload_info")
     *         #prop_desc("boolean", "enabled",
     *             "Aggregation of detailed SCAP results is enabled.")
     *         #prop_desc("int", "size_limit",
     *             "Limit (in Bytes) for a single SCAP file upload.")
     *     #struct_end()
     */
    public Map<String, Object> getPolicyForScapFileUpload(User loggedInUser,
            Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("enabled", org.getOrgConfig().isScapfileUploadEnabled());
        result.put("size_limit", org.getOrgConfig().getScapFileSizelimit());
        return result;
    }

    /**
     * Set the status of SCAP detailed result file upload settings for the given
     * organization.
     *
     * @param loggedInUser The current user
     * @param orgId ID of organization to work with.
     * @param newSettings New settings of the SCAP detailed result file upload.
     * @return Returns 1 for successfull change.
     *
     * @xmlrpc.doc Set the status of SCAP detailed result file upload settings
     * for the given organization.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param
     *     #struct("scap_upload_info")
     *         #prop_desc("boolean", "enabled",
     *             "Aggregation of detailed SCAP results is enabled.")
     *         #prop_desc("int", "size_limit",
     *             "Limit (in Bytes) for a single SCAP file upload.")
     *     #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setPolicyForScapFileUpload(User loggedInUser, Integer orgId,
            Map<String, Object> newSettings) {
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("enabled");
        validKeys.add("size_limit");
        validateMap(validKeys, newSettings);

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        OrgConfig orgConfig = verifyOrgExists(orgId).getOrgConfig();
        if (newSettings.containsKey("enabled")) {
            Boolean enabled = (Boolean) newSettings.get("enabled");
            orgConfig.setScapfileUploadEnabled(enabled);
        }
        if (newSettings.containsKey("size_limit")) {
            Long sizeLimit = new Long(((Integer)
                newSettings.get("size_limit")).longValue());
            orgConfig.setScapFileSizelimit(sizeLimit);
        }
        return 1;
    }

    /**
     * Get the status of SCAP result deletion settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId ID of organization to query.
     * @return Returns the status of SCAP result deletion settings.
     *
     * @xmlrpc.doc Get the status of SCAP result deletion settings for the given
     * organization.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *     #struct("scap_deletion_info")
     *         #prop_desc("boolean", "enabled", "Deletion of SCAP results is enabled")
     *         #prop_desc("int", "retention_period",
     *             "Period (in days) after which a scan can be deleted (if enabled).")
     *     #struct_end()
     */
    public Map<String, Object> getPolicyForScapResultDeletion(User loggedInUser,
            Integer orgId) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        Org org = verifyOrgExists(orgId);
        Long retentionPeriod = org.getOrgConfig().getScapRetentionPeriodDays();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("enabled", retentionPeriod != null);
        result.put("retention_period",
                (retentionPeriod != null) ? retentionPeriod : new Long(0));
        return result;
    }

    /**
     * Set the status of SCAP result deletion settings for the given organization.
     *
     * @param loggedInUser The current user
     * @param orgId ID of organization to work with.
     * @param newSettings New settings of the SCAP result deletion settings.
     * @return Returns 1 for successfull change.
     *
     * @xmlrpc.doc Set the status of SCAP result deletion settins for the given
     * organization.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param
     *     #struct("scap_deletion_info")
     *         #prop_desc("boolean", "enabled",
     *             "Deletion of SCAP results is enabled")
     *         #prop_desc("int", "retention_period",
     *             "Period (in days) after which a scan can be deleted (if enabled).")
     *     #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setPolicyForScapResultDeletion(User loggedInUser, Integer orgId,
            Map<String, Object> newSettings) {
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("enabled");
        validKeys.add("retention_period");
        validateMap(validKeys, newSettings);

        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        OrgConfig orgConfig = verifyOrgExists(orgId).getOrgConfig();
        if (newSettings.containsKey("enabled")) {
            if ((Boolean) newSettings.get("enabled")) {
                orgConfig.setScapRetentionPeriodDays(new Long(90));
            }
            else {
                orgConfig.setScapRetentionPeriodDays(null);
            }
        }
        if (newSettings.containsKey("retention_period")) {
            Long retentionPeriod = new Long(((Integer)
                newSettings.get("retention_period")).longValue());
            if (orgConfig.getScapRetentionPeriodDays() != null) {
                orgConfig.setScapRetentionPeriodDays(retentionPeriod);
            }
        }
        return 1;
    }
}
