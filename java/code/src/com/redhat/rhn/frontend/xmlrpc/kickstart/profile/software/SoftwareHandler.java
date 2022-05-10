/*
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartPackage;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.XmlRpcKickstartHelper;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SoftwareHandler
 * @apidoc.namespace kickstart.profile.software
 * @apidoc.doc Provides methods to access and modify the software list
 * associated with a kickstart profile.
 */
public class SoftwareHandler extends BaseHandler {

    /**
     * Get a list of a kickstart profile's software packages.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label
     * @return A list of package names.
     * @throws FaultException fault exception
     * @apidoc.doc Get a list of a kickstart profile's software packages.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.returntype
     * #array_single("string", "the list of the kickstart profile's software packages")
     */
    @ReadOnly
    public List<String> getSoftwareList(User loggedInUser, String ksLabel) {

        checkKickstartPerms(loggedInUser);
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        List<String> list = new ArrayList<>();
        for (KickstartPackage p : ksdata.getKsPackages()) {
            list.add(p.getPackageName().getName());
        }
        return list;
    }

    /**
     * Set the list of software packages for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label
     * @param packageList  A list of package names.
     * @return 1 on success.
     * @throws FaultException fault exception
     * @apidoc.doc Set the list of software packages for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.param #array_single_desc("string", "packageList", "the list of package names to be set on the profile")
     * @apidoc.returntype #return_int_success()
     */
    public int setSoftwareList(
            User loggedInUser,
            String ksLabel,
            List<String> packageList) {

        checkKickstartPerms(loggedInUser);
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        Set<KickstartPackage> packages = ksdata.getKsPackages();
        packages.clear();
        KickstartFactory.saveKickstartData(ksdata);
        //We need to flush session to make the change cascade into DB
        HibernateFactory.getSession().flush();
        Long pos = (long) packages.size(); // position package in list
        for (String p : packageList) {
            PackageName pn = PackageFactory.lookupOrCreatePackageByName(p);
            pos++;
            packages.add(new KickstartPackage(ksdata, pn, pos));
        }
        KickstartFactory.saveKickstartData(ksdata);
        return 1;
    }

    /**
     * Set the list of software packages for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label
     * @param packageList  A list of package names.
     * @param ignoreMissing The boolean value setting --ignoremissing in %packages line
     * when true
     * @param noBase The boolean value setting --nobase in the %packages line when true
     * @return 1 on success.
     * @throws FaultException fault exception
     * @apidoc.doc Set the list of software packages for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.param #array_single_desc("string", "packageList", "a list of package names to be set on the profile")
     * @apidoc.param #param_desc("boolean", "ignoreMissing", "ignore missing packages if true")
     * @apidoc.param #param_desc("boolean", "noBase", "don't install @Base package group if true")
     * @apidoc.returntype #return_int_success()
     */
    public int setSoftwareList(
            User loggedInUser,
            String ksLabel,
            List<String> packageList,
            Boolean ignoreMissing,
            Boolean noBase) {

        checkKickstartPerms(loggedInUser);
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        ksdata.setNoBase(noBase);
        ksdata.setIgnoreMissing(ignoreMissing);
        KickstartFactory.saveKickstartData(ksdata);
        return setSoftwareList(loggedInUser, ksLabel, packageList);
    }

    /**
     * Append the list of software packages to a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label
     * @param packageList  A list of package names.
     * @return 1 on success.
     * @throws FaultException fault exception
     * @apidoc.doc Append the list of software packages to a kickstart profile.
     * Duplicate packages will be ignored.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.param #array_single_desc("string", "packageList", "the list of package names to be added to the profile")
     * @apidoc.returntype #return_int_success()
     */
    public int appendToSoftwareList(User loggedInUser, String ksLabel, List<String> packageList) {
        checkKickstartPerms(loggedInUser);
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        Set<KickstartPackage> packages = ksdata.getKsPackages();
        Long pos = (long) packages.size(); // position package in list
        for (String p : packageList) {
            PackageName pn = PackageFactory.lookupOrCreatePackageByName(p);
            pos++;
            KickstartPackage kp = new KickstartPackage(ksdata, pn, pos);
            if (!ksdata.hasKsPackage(kp.getPackageName())) {
                packages.add(kp);
            }
        }
        KickstartFactory.saveKickstartData(ksdata);
        return 1;
    }

    private void checkKickstartPerms(User user) {
        if (!user.hasRole(RoleFactory.CONFIG_ADMIN)) {
            throw new PermissionException(LocalizationService.getInstance()
                    .getMessage("permission.configadmin.needed"));
        }
    }

    private KickstartData lookupKsData(String label, Org org) {
        return XmlRpcKickstartHelper.getInstance().lookupKsData(label, org);
    }

    /**
     * @param loggedInUser The current user
     * @param ksLabel Kickstart profile label
     * @param params Map containing software parameters
     * @return 1 if successful, exception otherwise.
     * @apidoc.doc Sets kickstart profile software details.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.param
     *          #struct_desc("params", "kickstart packages info")
     *              #prop_desc("string", "noBase", "install @Base package group")
     *              #prop_desc("string", "ignoreMissing", "ignore missing packages")
     *          #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setSoftwareDetails(User loggedInUser, String ksLabel, Map params) {
        KickstartData ksData = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                ksLabel, loggedInUser.getOrg().getId());
        if (params.containsKey("noBase")) {
            ksData.setNoBase((Boolean)params.get("noBase"));
        }
        if (params.containsKey("ignoreMissing")) {
            ksData.setIgnoreMissing((Boolean)params.get("ignoreMissing"));
        }
        return 1;
    }

    /**
     * @param loggedInUser The current user
     * @param ksLabel Kickstart profile label
     * @return Map of KS profile software parameters noBase, ignoreMissingPackages
     * @apidoc.doc Gets kickstart profile software details.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile")
     * @apidoc.returntype
     *          #struct_begin("kickstart packages info")
     *              #prop_desc("string", "noBase", "install @Base package group")
     *              #prop_desc("string", "ignoreMissing", "ignore missing packages")
     *          #struct_end()
     */
    @ReadOnly
    public Map<String, Boolean> getSoftwareDetails(User loggedInUser, String ksLabel) {
        KickstartData ksData = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                ksLabel, loggedInUser.getOrg().getId());
        Map<String, Boolean> returnValues = new HashMap<>();
        returnValues.put("noBase", ksData.getNoBase());
        returnValues.put("ignoreMissing", ksData.getIgnoreMissing());
        return returnValues;
    }
}
