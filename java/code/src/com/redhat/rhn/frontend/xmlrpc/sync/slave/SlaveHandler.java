/*
 * Copyright (c) 2013--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.sync.slave;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IssDuplicateSlaveException;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SlaveHandler
 *
 *
 * @apidoc.namespace sync.slave
 * @apidoc.doc Contains methods to set up information about allowed-"slaves", for use
 * on the "master" side of ISS
 */
public class SlaveHandler extends BaseHandler {

    /**
     * Create a new Slave, known to this Master.
     * @param loggedInUser The current user
     * @param slaveFqdn Slave's fully-qualified domain name
     * @param isEnabled Is this Slave allowed to talk to us?
     * @param allowAllOrgs Should we export all orgs to this Slave?
     * @return Newly created ISSSlave object.
     *
     * @apidoc.doc Create a new Slave, known to this Master.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "slaveFqdn", "Slave's fully-qualified domain name")
     * @apidoc.param #param_desc("boolean",
     *    "isEnabled", "Let this slave talk to us?")
     * @apidoc.param #param_desc("boolean",
     *    "allowAllOrgs", "Export all our orgs to this slave?")
     * @apidoc.returntype $IssSlaveSerializer
     */
    public IssSlave create(User loggedInUser,
                           String slaveFqdn,
                           Boolean isEnabled,
                           Boolean allowAllOrgs) {
        ensureSatAdmin(loggedInUser);
        if (IssFactory.lookupSlaveByName(slaveFqdn) != null) {
            throw new IssDuplicateSlaveException(slaveFqdn);
        }

        IssSlave slave = new IssSlave();
        slave.setSlave(slaveFqdn);
        slave.setEnabled(isEnabled ? "Y" : "N");
        slave.setAllowAllOrgs(allowAllOrgs ? "Y" : "N");
        IssFactory.save(slave);
        slave = HibernateFactory.reload(slave);
        return slave;
    }

    /**
     * Updates attributes of the specified Slave
     * @param loggedInUser The current user
     * @param slaveId id of Slave to update
     * @param slaveFqdn Slave's fully-qualified domain name
     * @param isEnabled Is this Slave allowed to talk to us?
     * @param allowAllOrgs Should we export all orgs to this Slave?
     * @return updated IssSlave
     *
     * @apidoc.doc Updates attributes of the specified Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "slaveId", "ID of the Slave to update")
     * @apidoc.param #param_desc("string", "slaveFqdn", "Slave's fully-qualified domain name")
     * @apidoc.param #param_desc("boolean",
     *    "isEnabled", "Let this slave talk to us?")
     * @apidoc.param #param_desc("boolean",
     *    "allowAllOrgs", "Export all our orgs to this Slave?")
     * @apidoc.returntype $IssSlaveSerializer
     */
    public IssSlave update(User loggedInUser,
                           Integer slaveId,
                           String slaveFqdn,
                           Boolean isEnabled,
                           Boolean allowAllOrgs) {
        IssSlave slave = getSlave(loggedInUser, slaveId);
        slave.setSlave(slaveFqdn);
        slave.setEnabled(isEnabled ? "Y" : "N");
        slave.setAllowAllOrgs(allowAllOrgs ? "Y" : "N");
        IssFactory.save(slave);
        slave = HibernateFactory.reload(slave);
        return slave;
    }

    /**
     * Removes a specified Slave
     *
     * @param loggedInUser The current user
     * @param slaveId Id of the Slave to remove
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Remove the specified Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "slaveId", "ID of the Slave to remove")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer slaveId) {
        IssSlave slave = getSlave(loggedInUser, slaveId);
        IssFactory.delete(slave);
        return 1;
    }

    /**
     * Find a Slave by specifying its ID
     * @param loggedInUser The current user
     * @param slaveId Id of the Slave to look for
     * @return the specified Slave if found, exception otherwise
     *
     * @apidoc.doc Find a Slave by specifying its ID
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "slaveId", "ID of the desired Slave")
     * @apidoc.returntype $IssSlaveSerializer
     */
    @ReadOnly
    public IssSlave getSlave(User loggedInUser, Integer slaveId) {
        ensureSatAdmin(loggedInUser);
        IssSlave slave = IssFactory.lookupSlaveById(slaveId.longValue());
        validateExists(slave, slaveId.toString());
        return slave;
    }

    /**
     * Find a Slave by specifying its Fully-Qualified Domain Name
     * @param loggedInUser The current user
     * @param slaveFqdn Domain name of the Slave to look for
     * @return the specified Slave if found, exception otherwise
     *
     * @apidoc.doc Find a Slave by specifying its Fully-Qualified Domain Name
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "slaveFqdn", "Domain-name of the desired Slave")
     * @apidoc.returntype $IssSlaveSerializer
     */
    @ReadOnly
    public IssSlave getSlaveByName(User loggedInUser, String slaveFqdn) {
        ensureSatAdmin(loggedInUser);
        IssSlave slave = IssFactory.lookupSlaveByName(slaveFqdn);
        validateExists(slave, slaveFqdn);
        return slave;
    }

    /**
     * Get all the Slaves this Master knows about
     * @param loggedInUser The current user
     * @return list of all the IssSlaves we know about
     *
     * @apidoc.doc Get all the Slaves this Master knows about
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $IssSlaveSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<IssSlave> getSlaves(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return IssFactory.listAllIssSlaves();
    }

    /**
     * Get all the orgs that this Master is willing to export to the specified Slave
     * @param loggedInUser The current user
     * @param slaveId Id of the Slave to look for
     * @return list of all the IssSlaves we know about
     *
     * @apidoc.doc Get all orgs this Master is willing to export to the specified Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "slaveId", "Id of the desired Slave")
     * @apidoc.returntype #array_single("int", "ids of allowed organizations")
     */
    @ReadOnly
    public List<Integer> getAllowedOrgs(User loggedInUser, Integer slaveId) {
        IssSlave slave = getSlave(loggedInUser, slaveId);
        List<Integer> allowedOrgIds = new ArrayList<>();
        for (Org o : slave.getAllowedOrgs()) {
            allowedOrgIds.add(o.getId().intValue());
        }
        return allowedOrgIds;
    }

    /**
     * Set the orgs that this Master is willing to export to the specified Slave
     * @param loggedInUser The current user
     * @param slaveId Id of the Slave to look for
     * @param orgIds List of org-ids we're willing to export
     * @return 1 for success, exception otherwise
     *
     * @apidoc.doc Set the orgs this Master is willing to export to the specified Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "slaveId", "ID of the desired Slave")
     * @apidoc.param #array_single_desc("int", "orgIds", "List of org-ids we're willing to export")
     * @apidoc.returntype #return_int_success()
     */
    public int setAllowedOrgs(User loggedInUser, Integer slaveId, List<Integer> orgIds) {
        IssSlave slave = getSlave(loggedInUser, slaveId);
        Set<Org> orgs = getOrgsFromIds(orgIds);
        slave.setAllowedOrgs(orgs);
        return 1;
    }

    private void validateExists(IssSlave slave, String srchString) {
        if (slave == null) {
            fail("Unable to locate or access ISS Slave : " + srchString,
                    "lookup.issslave.title", "lookup.issslave.reason1", srchString);
        }
    }

    private void fail(String msg, String titleKey, String reasonKey, String arg) {
        LocalizationService ls = LocalizationService.getInstance();
        throw new LookupException(msg, ls.getMessage(titleKey), ls.getMessage(reasonKey, arg), null);
    }

    private Set<Org> getOrgsFromIds(List<Integer> orgIds) {
        Set<Org> orgs = new HashSet<>();
        for (Integer oid : orgIds) {
            Org o = OrgFactory.lookupById(oid.longValue());
            orgs.add(o);
        }
        return orgs;
    }
}
