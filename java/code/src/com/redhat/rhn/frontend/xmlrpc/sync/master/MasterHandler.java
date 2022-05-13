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
package com.redhat.rhn.frontend.xmlrpc.sync.master;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.iss.IssMasterOrg;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IssDuplicateMasterException;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MasterHandler
 *
 *
 * @apidoc.namespace sync.master
 * @apidoc.doc Contains methods to set up information about known-"masters", for use
 * on the "slave" side of ISS
 */
public class MasterHandler extends BaseHandler {

    public static final String[] VALID_MASTER_ORG_ATTRS = {
        "masterId", "masterOrgId", "masterOrgName", "localOrgId"
    };
    private static final Set<String> VALIDMASTERORGATTR;
    static {
        VALIDMASTERORGATTR = new HashSet<>(Arrays.asList(VALID_MASTER_ORG_ATTRS));
    }

    public static final String[] REQUIRED_MASTER_ORG_ATTRS = {
        "masterOrgId", "masterOrgName"
    };
    private static final Set<String> REQUIREDMASTERORGATTRS;
    static {
        REQUIREDMASTERORGATTRS =
                new HashSet<>(Arrays.asList(REQUIRED_MASTER_ORG_ATTRS));
    }

    /**
     * Create a new Master, known to this Slave.
     * @param loggedInUser The current user
     * @param label Master's fully-qualified domain name
     * @return Newly created ISSMaster object.
     *
     * @apidoc.doc Create a new Master, known to this Slave.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "Master's fully-qualified domain name")
     * @apidoc.returntype $IssMasterSerializer
     */
    public IssMaster create(User loggedInUser, String label) {
        ensureSatAdmin(loggedInUser);
        if (IssFactory.lookupMasterByLabel(label) != null) {
            throw new IssDuplicateMasterException(label);
        }
        IssMaster master = new IssMaster();
        master.setLabel(label);
        IssFactory.save(master);
        master = (IssMaster) IssFactory.reload(master);
        return master;
    }

    /**
     * Updates the label of the specified Master
     * @param loggedInUser The current user
     * @param masterId Id of the Master to update
     * @param label new label
     * @return updated IssMaster
     *
     * @apidoc.doc Updates the label of the specified Master
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "ID of the Master to update")
     * @apidoc.param #param_desc("string", "label", "Desired new label")
     * @apidoc.returntype $IssMasterSerializer
     */
    public IssMaster update(User loggedInUser, Integer masterId, String label) {
        IssMaster master = getMaster(loggedInUser, masterId);
        master.setLabel(label);
        IssFactory.save(master);
        return master;
    }

    /**
     * Removes a specified Master
     *
     * @param loggedInUser The current user
     * @param masterId Id of the Master to remove
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Remove the specified Master
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "Id of the Master to remove")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer masterId) {
        IssMaster master = getMaster(loggedInUser, masterId);
        IssFactory.delete(master);
        return 1;
    }

    /**
     * Make the specified Master the default for this Slave's inter-server-sync
     * @param loggedInUser The current user
     * @param masterId Id of the Master to be the default
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Make the specified Master the default for this Slave's inter-server-sync
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "Id of the Master to make the default")
     * @apidoc.returntype #return_int_success()
     */
    public int makeDefault(User loggedInUser, Integer masterId) {
        IssMaster master = getMaster(loggedInUser, masterId);
        master.makeDefaultMaster();
        IssFactory.save(master);
        return 1;
    }

    /**
     * Return the current default-Master for this Slave
     * @param loggedInUser The current user
     * @return current default Master, null if there isn't one
     *
     * @apidoc.doc Return the current default-Master for this Slave
     * @apidoc.param #session_key()
     * @apidoc.returntype $IssMasterSerializer
     */
    @ReadOnly
    public IssMaster getDefaultMaster(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        IssMaster dflt = IssFactory.getCurrentMaster();
        validateExists(dflt, "Default Master");
        return dflt;
    }

    /**
     * Check if this host is reading configuration from an ISS master.
     * @return boolean if there is ISS master.
     *
     * @apidoc.doc Check if this host is reading configuration from an ISS master.
     * @apidoc.returntype #param_desc("boolean", "master", "True if has an ISS master, false otherwise")
     */
    public boolean hasMaster() {
        return IssFactory.getCurrentMaster() != null;
    }

    /**
     * Make this slave have no default Master for inter-server-sync
     * @param loggedInUser The current user
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Make this slave have no default Master for inter-server-sync
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public int unsetDefaultMaster(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        IssFactory.unsetCurrentMaster();
        return 1;
    }

    /**
     * Set the CA-CERT filename for specified Master on this Slave
     * @param loggedInUser The current user
     * @param masterId Id of the Master we're affecting
     * @param caCertFilename path to this Master's CA Cert on this Slave
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Set the CA-CERT filename for specified Master on this Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "ID of the Master to affect")
     * @apidoc.param #param_desc("string", "caCertFilename",
     *  "path to specified Master's CA cert")
     * @apidoc.returntype #return_int_success()
     */
    public int setCaCert(User loggedInUser, Integer masterId, String caCertFilename) {
        IssMaster master = getMaster(loggedInUser, masterId);
        master.setCaCert(caCertFilename);
        return 1;
    }

    /**
     * Find a Master by specifying its ID
     * @param loggedInUser The current user
     * @param masterId Id of the Master to look for
     * @return the specified Master if found, exception otherwise
     *
     * @apidoc.doc Find a Master by specifying its ID
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "ID of the desired Master")
     * @apidoc.returntype $IssMasterSerializer
     */
    @ReadOnly
    public IssMaster getMaster(User loggedInUser, Integer masterId) {
        ensureSatAdmin(loggedInUser);
        IssMaster master = IssFactory.lookupMasterById(masterId.longValue());
        validateExists(master, masterId.toString());
        return master;
    }

    /**
     * Find a Master by specifying its label
     * @param loggedInUser The current user
     * @param label Label of the Master to look for
     * @return the specified Master if found, exception otherwise
     *
     * @apidoc.doc Find a Master by specifying its label
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "Label of the desired Master")
     * @apidoc.returntype $IssMasterSerializer
     */
    @ReadOnly
    public IssMaster getMasterByLabel(User loggedInUser, String label) {
        ensureSatAdmin(loggedInUser);
        IssMaster master = IssFactory.lookupMasterByLabel(label);
        validateExists(master, label);
        return master;
    }

    /**
     * Get all the Masters this Slave knows about
     * @param loggedInUser The current user
     * @return list of all the IssMasters we know about
     *
     * @apidoc.doc Get all the Masters this Slave knows about
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $IssMasterSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<IssMaster> getMasters(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return IssFactory.listAllMasters();
    }

    /**
     * List all organizations the specified Master has exported to this Slave
     *
     * @param loggedInUser The current user
     * @param masterId Id of the Master to look for
     * @return List of MasterOrgs we know about
     *
     * @apidoc.doc List all organizations the specified Master has exported to this Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "ID of the desired Master")
     * @apidoc.returntype
     *   #return_array_begin()
     *     $IssMasterOrgSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<IssMasterOrg> getMasterOrgs(User loggedInUser, Integer masterId) {
        IssMaster master = getMaster(loggedInUser, masterId);
        ArrayList<IssMasterOrg> orgs = new ArrayList<>();
        orgs.addAll(master.getMasterOrgs());
        return orgs;
    }

    /**
     * Reset all organizations the specified Master has exported to this Slave
     *
     * @param loggedInUser The current user
     * @param masterId Id of the Master to look for
     * @param orgMaps List of MasterOrgs we know about
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Reset all organizations the specified Master has exported to this Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "Id of the desired Master")
     * @apidoc.param
     *   #array_begin("orgMaps")
     *      #struct_begin("master-org details")
     *          #prop("int", "masterOrgId")
     *          #prop("string", "masterOrgName")
     *          #prop("int", "localOrgId")
     *     #struct_end()
     *   #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setMasterOrgs(User loggedInUser,
                             Integer masterId,
                             List<Map<String, Object>> orgMaps) {
        IssMaster master = getMaster(loggedInUser, masterId);
        Set<IssMasterOrg> orgs = new HashSet<>();
        for (Map<String, Object> anOrgMap : orgMaps) {
            IssMasterOrg o = validateOrg(anOrgMap);
            orgs.add(o);
        }
        master.resetMasterOrgs(orgs);
        return 1;
    }

    /**
     * Add a single organizations to the list of those the specified Master has
     * exported to this Slave
     *
     * @param loggedInUser The current user
     * @param masterId Id of the Master to look for
     * @param orgMap new master-organization to add
     * @return 1 if success, exception otherwise
     *
     * @apidoc.doc Add a single organizations to the list of those the specified Master has
     * exported to this Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "Id of the desired Master")
     * @apidoc.param
     *      #struct_begin("orgMap")
     *          #prop("int", "masterOrgId")
     *          #prop("string", "masterOrgName")
     *          #prop("int", "localOrgId")
     *     #struct_end()
     * @apidoc.returntype #return_int_success()
     *
     */
    public int addToMaster(User loggedInUser,
                           Integer masterId,
                           Map<String, Object> orgMap) {
        IssMaster master = getMaster(loggedInUser, masterId);
        IssMasterOrg org = validateOrg(orgMap);
        master.addToMaster(org);
        return 1;
    }

    /**
     * Map a given master-organization to a specific local-organization
     *
     * @param loggedInUser The current user
     * @param masterId Id of the Master to look for
     * @param masterOrgId id of the master-organization to work with
     * @param localOrgId id of the local organization to map to masterOrgId
     * @return 1 if success, exception otherwise
     *
     * @apidoc.doc Add a single organizations to the list of those the specified Master has
     * exported to this Slave
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "masterId", "ID of the desired Master")
     * @apidoc.param #param_desc("int", "masterOrgId", "ID of the desired Master")
     * @apidoc.param #param_desc("int", "localOrgId", "ID of the desired Master")
     * @apidoc.returntype #return_int_success()
     *
     */
    public int mapToLocal(User loggedInUser,
                          Integer masterId,
                          Integer masterOrgId,
                          Integer localOrgId) {
        boolean found = false;

        IssMaster master = getMaster(loggedInUser, masterId);
        Set<IssMasterOrg> orgs = master.getMasterOrgs();

        Org localOrg = OrgFactory.lookupById(localOrgId.longValue());
        if (localOrg == null) {
            fail("Unable to locate or access Local Organization :" + localOrgId,
                    "lookup.issmaster.local.title", "lookup.issmaster.local.reason1",
                    localOrgId.toString());
        }

        for (IssMasterOrg o : orgs) {
            if (o.getMasterOrgId().equals(masterOrgId.longValue())) {
                o.setLocalOrg(localOrg);
                found = true;
                break;
            }
        }

        if (!found) {
            fail("Unable to locate or access ISS Master Organization : " + masterOrgId,
                    "lookup.issmasterorg.title", "lookup.issmasterorg.reason1",
                    masterOrgId.toString());
        }

        IssFactory.save(master);
        return 1;
    }

    private static Set<String> getValidMasterOrgsAttrs() {
        return VALIDMASTERORGATTR;
    }

    private static Set<String> getRequiredMasterOrgsAttrs() {
        return REQUIREDMASTERORGATTRS;
    }

    private IssMasterOrg validateOrg(Map<String, Object> anOrg) {
        validateMap(getValidMasterOrgsAttrs(), anOrg);
        Set<String> attrs = anOrg.keySet();

        if (!attrs.containsAll(getRequiredMasterOrgsAttrs())) {
            throw new FaultException(-6, "requiredOptionMissing",
                    "Required option missing. List of required options: " +
                            REQUIREDMASTERORGATTRS);
        }

        IssMasterOrg o = new IssMasterOrg();
        for (String attr : attrs) {
            if ("localOrgId".equals(attr)) {
                Integer localId = (Integer)anOrg.get(attr);
                Org local = OrgFactory.lookupById(localId.longValue());
                o.setLocalOrg(local);
            }
            else if ("masterOrgId".equals(attr)) {
                Integer moId = (Integer)anOrg.get(attr);
                o.setMasterOrgId(moId.longValue());
            }
            else {
                setEntityAttribute(attr, o, anOrg.get(attr));
            }
        }

        return o;
    }

    private void validateExists(IssMaster master, String srchString) {
        if (master == null) {
            fail("Unable to locate or access ISS Master : " + srchString,
                    "lookup.issmaster.title", "lookup.issmaster.reason1", srchString);
        }
    }

    private void fail(String msg, String titleKey, String reasonKey, String arg) {
        LocalizationService ls = LocalizationService.getInstance();
        throw new LookupException(msg, ls.getMessage(titleKey), ls.getMessage(reasonKey, arg), null);
    }
}
