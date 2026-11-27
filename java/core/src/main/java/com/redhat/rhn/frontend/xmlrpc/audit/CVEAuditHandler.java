/*
 * Copyright (c) 2013 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc.audit;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.UnknownCVEIdentifierFaultException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.SystemCVEAuditManager;
import com.redhat.rhn.manager.audit.SystemCVEResult;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.utils.EnumNameParser;
import com.suse.oval.OvalSeverity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * CVESearchHandler
 *
 * @apidoc.namespace audit
 * @apidoc.doc Methods to audit systems.
 */
public class CVEAuditHandler extends BaseHandler {

    private final XmlRpcSystemHelper xmlRpcSystemHelper;

    /**
     * @param xmlRpcSystemHelperIn the helper used to look up the audited system
     */
    public CVEAuditHandler(XmlRpcSystemHelper xmlRpcSystemHelperIn) {
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
    }

    /**
     * List the CVEs affecting a system, with their patch status, severity and fixing patches.
     * Same data as the 'CVE' tab of the system 'Audit' page.
     *
     * @param loggedInUser The current user
     * @param sid the id of the system
     * @return the CVEs involving packages installed on the system
     *
     * @apidoc.doc List the CVEs affecting a system, with their patch status, severity and the patches fixing
     * them. This is the same data as the 'CVE' tab of the system 'Audit' page. The list is empty if OVAL based
     * CVE auditing is disabled or if there is no OVAL data for the operating system of the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #return_array_begin() $SystemCVEResultSerializer #array_end()
     */
    @ReadOnly
    public List<SystemCVEResult> listCvesBySystem(User loggedInUser, Integer sid) {
        return listCvesBySystem(loggedInUser, sid, null, null);
    }

    /**
     * List the CVEs affecting a system, filtered by patch status and severity.
     *
     * @param loggedInUser The current user
     * @param sid the id of the system
     * @param patchStatusLabels the patch statuses to include, null or empty for all
     * @param severities the severities to include, null or empty for all
     * @return the CVEs involving packages installed on the system
     * @throws FaultException if the system is not found or a label is not valid
     *
     * @apidoc.doc List the CVEs affecting a system, with their patch status, severity and the patches fixing
     * them, filtered by patch status and severity. This is the same data as the 'CVE' tab of the system 'Audit'
     * page. The list is empty if OVAL based CVE auditing is disabled or if there is no OVAL data for the
     * operating system of the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "patchStatusLabels")
     *  #options()
     *      #item_desc ("AFFECTED_PATCH_UNAVAILABLE", "Affected, patch is unavailable anywhere")
     *      #item_desc ("AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI",
     *          "Affected, patch is unavailable in any of the synced channels")
     *      #item_desc ("AFFECTED_PATCH_INAPPLICABLE", "Affected, patch available in unassigned channel")
     *      #item_desc ("AFFECTED_PARTIAL_PATCH_APPLICABLE",
     *          "Affected, partial patch available in assigned channel")
     *      #item_desc ("AFFECTED_FULL_PATCH_APPLICABLE", "Affected, full patch available in assigned channel")
     *      #item_desc ("AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT",
     *          "Affected, patch available in a Product Migration target")
     *      #item_desc ("PATCHED", "Patched")
     *  #options_end()
     * @apidoc.param #array_single("string", "severities")
     *  #options()
     *      #item ("CRITICAL")
     *      #item ("IMPORTANT")
     *      #item ("MODERATE")
     *      #item ("LOW")
     *      #item_desc ("UNKNOWN", "No severity is available in the OVAL data")
     *  #options_end()
     * @apidoc.returntype #return_array_begin() $SystemCVEResultSerializer #array_end()
     */
    @ReadOnly
    public List<SystemCVEResult> listCvesBySystem(User loggedInUser, Integer sid, List<String> patchStatusLabels,
                                                  List<String> severities) throws FaultException {
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);

        Set<PatchStatus> statuses = EnumNameParser.parse(patchStatusLabels, PatchStatus.class, e -> {
            throw new MethodInvalidParamException(e);
        });
        if (statuses.isEmpty()) {
            statuses = EnumSet.allOf(PatchStatus.class);
        }
        Set<OvalSeverity> severitySet = EnumNameParser.parse(severities, OvalSeverity.class, e -> {
            throw new MethodInvalidParamException(e);
        });

        return SystemCVEAuditManager.listCves(server, statuses, severitySet, new PageControl(1, 0));
    }

    /**
     * List visible systems with their patch status regarding a given CVE
     * identifier.
     *
     * Please note that the query code relies on data that is pre-generated
     * by the 'cve-server-channels' taskomatic job.
     * @param loggedInUser The current user
     * @param cveIdentifier the CVE number to search for
     * @return a list of systems with their patch status
     *
     * @apidoc.doc List visible systems with their patch status regarding a given CVE
     * identifier. Please note that the query code relies on data that is pre-generated
     * by the 'cve-server-channels' taskomatic job.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "cveIdentifier")
     * @apidoc.returntype #return_array_begin() $CVEAuditServerSerializer #array_end()
     */
    @ReadOnly
    public List<CVEAuditServer> listSystemsByPatchStatus(User loggedInUser,
                                                         String cveIdentifier) {
        return listSystemsByPatchStatus(loggedInUser, cveIdentifier, null);
    }

    /**
     * List visible systems with their patch status regarding a given CVE
     * identifier. Filter the results by passing in a list of patch status
     * labels. Please note that the query code relies on data that is
     * pre-generated by the 'cve-server-channels' taskomatic job.
     * @param loggedInUser The current user
     * @param cveIdentifier the CVE number to search for
     * @param patchStatusLabels patch status labels to filter, will only return
     *            results with those patch statuses
     * @return a list of systems with their patch status
     * @throws FaultException if the CVE number is not known
     *
     * @apidoc.doc List visible systems with their patch status regarding a given CVE
     * identifier. Filter the results by passing in a list of patch status labels.
     * Please note that the query code relies on data that is pre-generated by the
     * 'cve-server-channels' taskomatic job.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "cveIdentifier")
     * @apidoc.param #array_single("string", "patchStatusLabels")
     *  #options()
     *      #item_desc ("AFFECTED_PATCH_INAPPLICABLE",
     *          "Affected, patch available in unassigned channel")
     *      #item_desc ("AFFECTED_PATCH_APPLICABLE",
     *          "Affected, patch available in assigned channel")
     *      #item_desc ("NOT_AFFECTED", "Not affected")
     *      #item_desc ("PATCHED", "Patched")
     *  #options_end()
     * @apidoc.returntype #return_array_begin() $CVEAuditServerSerializer #array_end()
     */
    @ReadOnly
    public List<CVEAuditServer> listSystemsByPatchStatus(User loggedInUser,
            String cveIdentifier, List<String> patchStatusLabels) throws FaultException {

        // Convert list of strings to patch status objects
        EnumSet<PatchStatus> patchStatuses = EnumSet.noneOf(PatchStatus.class);
        if (patchStatusLabels == null) {
            patchStatuses = EnumSet.allOf(PatchStatus.class);
        }
        else {
            for (String label : patchStatusLabels) {
                try {
                    // TODO: Adapt to use new patch statuses
                    if (label.equals("AFFECTED_PATCH_APPLICABLE")) {
                        patchStatuses.add(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE);
                    }
                    else {
                        patchStatuses.add(PatchStatus.valueOf(label));
                    }
                }
                catch (IllegalArgumentException e) {
                    throw new MethodInvalidParamException(e);
                }
            }
        }

        try {
            List<CVEAuditServer> result = CVEAuditManagerOVAL.listSystemsByPatchStatus(
                    loggedInUser, cveIdentifier, patchStatuses);

            result.sort(Comparator.comparingInt(s -> s.getPatchStatus().getRank()));

            return result;
        }
        catch (UnknownCVEIdentifierException e) {
            throw new UnknownCVEIdentifierFaultException();
        }
    }

    /**
     * List visible images with their patch status regarding a given CVE
     * identifier.
     *
     * Please note that the query code relies on data that is pre-generated
     * by the 'cve-server-channels' taskomatic job.
     * @param loggedInUser The current user
     * @param cveIdentifier the CVE number to search for
     * @return a list of images with their patch status
     *
     * @apidoc.doc List visible images with their patch status regarding a given CVE
     * identifier. Please note that the query code relies on data that is pre-generated
     * by the 'cve-server-channels' taskomatic job.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "cveIdentifier")
     * @apidoc.returntype #return_array_begin() $CVEAuditImageSerializer #array_end()
     */
    @ReadOnly
    public List<CVEAuditImage> listImagesByPatchStatus(User loggedInUser,
            String cveIdentifier) {
        return listImagesByPatchStatus(loggedInUser, cveIdentifier, null);
    }

    /**
     * List visible images with their patch status regarding a given CVE
     * identifier. Filter the results by passing in a list of patch status
     * labels. Please note that the query code relies on data that is
     * pre-generated by the 'cve-server-channels' taskomatic job.
     * @param loggedInUser The current user
     * @param cveIdentifier the CVE number to search for
     * @param patchStatusLabels patch status labels to filter, will only return
     *            results with those patch statuses
     * @return a list of images with their patch status
     * @throws FaultException if the CVE number is not known
     *
     * @apidoc.doc List visible images with their patch status regarding a given CVE
     * identifier. Filter the results by passing in a list of patch status labels.
     * Please note that the query code relies on data that is pre-generated by the
     * 'cve-server-channels' taskomatic job.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "cveIdentifier")
     * @apidoc.param #array_single("string", "patchStatusLabels")
     *  #options()
     *      #item_desc ("AFFECTED_PATCH_INAPPLICABLE",
     *          "Affected, patch available in unassigned channel")
     *      #item_desc ("AFFECTED_PATCH_APPLICABLE",
     *          "Affected, patch available in assigned channel")
     *      #item_desc ("NOT_AFFECTED", "Not affected")
     *      #item_desc ("PATCHED", "Patched")
     *  #options_end()
     * @apidoc.returntype #return_array_begin() $CVEAuditImageSerializer #array_end()
     */
    @ReadOnly
    public List<CVEAuditImage> listImagesByPatchStatus(User loggedInUser,
                                                       String cveIdentifier, List<String> patchStatusLabels)
            throws FaultException {

        // Convert list of strings to patch status objects
        EnumSet<PatchStatus> patchStatuses = EnumSet.noneOf(PatchStatus.class);
        if (patchStatusLabels == null) {
            patchStatuses = EnumSet.allOf(PatchStatus.class);
        }
        else {
            for (String label : patchStatusLabels) {
                try {
                    // TODO: Adapt to use new patch statuses
                    if (label.equals("AFFECTED_PATCH_APPLICABLE")) {
                        patchStatuses.add(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE);
                    }
                    else {
                        patchStatuses.add(PatchStatus.valueOf(label));
                    }
                }
                catch (IllegalArgumentException e) {
                    throw new MethodInvalidParamException(e);
                }
            }
        }

        try {
            List<CVEAuditImage> result = CVEAuditManagerOVAL.listImagesByPatchStatus(
                    loggedInUser, cveIdentifier, patchStatuses);

            result.sort(Comparator.comparingInt(i -> i.getPatchStatus().getRank()));

            return result;
        }
        catch (UnknownCVEIdentifierException e) {
            throw new UnknownCVEIdentifierFaultException();
        }
    }
}
