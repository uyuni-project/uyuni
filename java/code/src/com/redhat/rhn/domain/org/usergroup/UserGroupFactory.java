/*
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
package com.redhat.rhn.domain.org.usergroup;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * UserGroupFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.org.usergroup.UserGroup objects from the
 * database.
 */
public class UserGroupFactory extends HibernateFactory {

    private static UserGroupFactory singleton = new UserGroupFactory();
    private static Logger log = LogManager.getLogger(UserGroupFactory.class);

    private UserGroupFactory() {
        super();
    }

    /** Get the Logger for the derived class so log messages
    *   show up on the correct class
    */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create a new UserGroup from scratch based on the passed in Role
     * object
     * @param org the Org to associate this UserGroup with
     * @param role the Role to base this new UserGroup on.
     * @return the UserGroup created
     */
    public static UserGroup createUserGroup(Org org, Role role) {
        UserGroup retval = new UserGroupImpl();
        LocalizationService ls = LocalizationService.getInstance();
        // Concat the Role name with the letter s to form the UserGroup name
        // such as: "Organization Applicants"
        String key = role.getName() + "s";
        retval.setName(ls.getMessage(key));
        String desc = retval.getName() + ls.getMessage("for Org") + org.getName() + " (" + org.getId() + ")";
        retval.setDescription(desc);
        retval.setOrgId(org.getId());
        retval.setRole(role);
        return retval;
    }

    /**
     * Returns the complete list of UserExtGroup
     * @param user needs to be satellite admin
     * @return UserExtGroup list
     */
    public static List<UserExtGroup> listExtAuthGroups(User user) {
        if (!user.getRoles().contains(RoleFactory.SAT_ADMIN)) {
            throw new PermissionException("Satellite admin role required to access extauth groups");
        }
        return singleton.listObjectsByNamedQuery("UserExtGroup.listAll", Map.of());
    }

    /**
     * Returns the complete list of OrgUserExtGroup
     * @param user needs to be org admin
     * @return OrgUserExtGroup list
     */
    public static List<OrgUserExtGroup> listExtAuthOrgGroups(User user) {
        if (!user.getRoles().contains(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException("Organization admin role required to access extauth organization groups");
        }
        return singleton.listObjectsByNamedQuery("OrgUserExtGroup.listAll", Map.of("org_id", user.getOrg().getId()));
    }

    /**
     * lookup function to search for external groups
     * @param gidIn external group id
     * @return external group object
     */
    public static UserExtGroup lookupExtGroupById(Long gidIn) {
        return singleton.lookupObjectByNamedQuery("UserExtGroup.lookupById", Map.of("gid", gidIn));
    }

    /**
     * lookup function to search for external groups
     * @param gidIn external group id
     * @param orgIn organization
     * @return external group object
     */
    public static OrgUserExtGroup lookupOrgExtGroupByIdAndOrg(Long gidIn, Org orgIn) {
        return singleton.lookupObjectByNamedQuery("OrgUserExtGroup.lookupByIdAndOrg",
                Map.of("gid", gidIn, "org_id", orgIn.getId()));
    }

    /**
     * save UserExtGroup object
     * @param extGroup external group
     */
    public static void save(UserExtGroup extGroup) {
        singleton.saveObject(extGroup);
    }

    /**
     * save OrgUserGroup object
     * @param extGroup org user group
     */
    public static void save(OrgUserExtGroup extGroup) {
        singleton.saveObject(extGroup);
    }

    /**
     * delete UserExtGroup object
     * @param extGroup external group
     */
    public static void delete(ExtGroup extGroup) {
        singleton.removeObject(extGroup);
    }

    /**
     * lookup function to search for external groups
     * @param labelIn external group label
     * @return external group object
     */
    public static UserExtGroup lookupExtGroupByLabel(String labelIn) {
        return singleton.lookupObjectByNamedQuery("UserExtGroup.lookupByLabel", Map.of("label", labelIn));
    }

    /**
     * lookup function to search for organization external groups
     * @param labelIn external group label
     * @param orgIn organization
     * @return external group object
     */
    public static OrgUserExtGroup lookupOrgExtGroupByLabelAndOrg(String labelIn, Org orgIn) {
        return singleton.lookupObjectByNamedQuery("OrgUserExtGroup.lookupByLabelAndOrg",
                Map.of("label", labelIn, "org_id", orgIn.getId()));
    }

    /**
     * deletes all temporary roles across the whole satellite
     * (users across all the orgs)
     * @return number of removed roles
     */
    public static int deleteTemporaryRoles() {
        return HibernateFactory.getSession()
        .getNamedQuery("UserGroupMembers.deleteTemporary")
        .executeUpdate();
    }

    /**
     * save UserGroupMembers object
     * @param ugmIn user group member
     */
    public static void save(UserGroupMembers ugmIn) {
        singleton.saveObject(ugmIn);
    }

    /**
     * delete UserGroupMembers object
     * @param ugmIn group members entry
     */
    public static void delete(UserGroupMembers ugmIn) {
        singleton.removeObject(ugmIn);
    }
}

