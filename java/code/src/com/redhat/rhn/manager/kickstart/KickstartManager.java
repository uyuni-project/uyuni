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
package com.redhat.rhn.manager.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.common.util.download.DownloadUtils;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.kickstart.KickstartIpRangeDto;
import com.redhat.rhn.manager.BaseManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * KickstartManager
 */
public class KickstartManager extends BaseManager {
    private static final KickstartManager INSTANCE = new KickstartManager();

    /**
     * Returns an instance of kickstart manager
     * @return an instance
     */
    public static KickstartManager getInstance() {
        return INSTANCE;
    }

    /**
     * Render the kickstart using cobbler and return the contents with the Cobbler host
     * search/replaced.
     *
     * @param host the host to force into the ks file.  searches and replaces all
     * instances of the Cobbler Host with whatever you pass in.  Use with Proxies.
     * @param data the KickstartData
     * @return the rendered kickstart contents
     */
    public String renderKickstart(String host, KickstartData data) {
        return renderKickstart(host, KickstartUrlHelper.getCobblerProfileUrl(data));
    }

    /**
     * Render the kickstart using cobbler and return the contents with the Cobbler host
     * search/replaced.
     *
     * @param host the host to force into the ks file.  searches and replaces all
     * instances of the Cobbler Host with whatever you pass in.  Use with Proxies.
     * @param url The url to fetch
     * @return the rendered kickstart contents
     */
    public String renderKickstart(String host, String url) {
        String retval = renderKickstart(url);
        // Search/replacing all instances of cobbler host with host
        // we pass in, for use with Spacewalk Proxy.
        retval = retval.replaceAll(ConfigDefaults.get().getCobblerHost(), host);
        return retval;
    }

    /**
     * Render the kickstart using cobbler and return the contents
     * @param data the KickstartData
     * @return the rendered kickstart contents
     */
    public String renderKickstart(KickstartData data) {
        return renderKickstart(KickstartUrlHelper.getCobblerProfileUrl(data));
    }


    /**
     * Render the kickstart using cobbler and return the contents
     * @param url the url to fetch
     * @return the rendered kickstart contents
     */
    public String renderKickstart(String url) {
        return DownloadUtils.downloadUrl(url);
    }


    /**
     * Simple method to validate a generated kickstart
     * @param ksdata the kickstart data file whose ks
     * templates will be checked
     * @throws ValidatorException on parse error or ISE..
     */
    public void validateKickstartFile(KickstartData ksdata) {
        try {
            if (ksdata.isValid()) {
                String text = renderKickstart(ksdata);
                if (tryDetectErrors(text)) {
                    ValidatorException.
                        raiseException("kickstart.jsp.error.template_generation",
                                LocalizationService.getInstance().
                                getMessage("kickstartdownload.jsp.header"));
                }
            }
        }
        catch (DownloadException de) {
            ValidatorException.raiseException("kickstart.jsp.error.template_generation",
                    LocalizationService.getInstance().getMessage(
                            "kickstartdownload.jsp.header"));
        }
    }

    /**
     * HACK: Heuristics for detecting errors in the contents of the auto-installation file.
     * This is not a reliable method of detecting errors, but currently cobbler provides
     * no way of error reporting.
     *
     * @param contents - the auto-installation file contents
     * @return true if an error was detected in the auto-installation file contents
     */
    public static boolean tryDetectErrors(String contents) {
        Set<String> errorStrings = new HashSet<>();
        errorStrings.add("Traceback (most recent call last):");
        errorStrings.add("There is a templating error preventing this file from");
        errorStrings.add("# This kickstart had errors that prevented it from being " +
                "rendered correctly.\n");

        return errorStrings.stream()
                .map(contents::contains)
                .anyMatch(Predicate.isEqual(true));
    }

    /**
     * returns a list of systems in SSM
     * that are kickstartable
     * @param user the user for access info
     * @return the list of kickstartable systems
     */
    public DataResult<SystemOverview> kickstartableSystemsInSsm(User user) {
        SelectMode m = ModeFactory.getMode("System_queries", "ssm_kickstartable");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        return makeDataResult(params, Collections.emptyMap(), null, m);
    }

    /**
     * returns a list of IP ranges accessible to the
     * user
     * @param user the current user needed for org information
     * @return the the list of ip ranges accessible to the user.
     */
    public List<KickstartIpRange> listIpRanges(User user) {
        return KickstartFactory.lookupRangeByOrg(user.getOrg());
    }

    /**
     * Find a kickstart profile for a given server by searching by IP addresses
     * @param server the server to find the ksdata for
     * @return the kickstartData or 'null' if nothing found
     */
    public KickstartData findProfileForServersNetwork(Server server) {
        KickstartData ks = null;
        /*
         * So first get the IP address for eth0 and see if there's
         *  A kickstart that corresponds to it
         */
        NetworkInterface nic = server.getNetworkInterface("eth0");
        if (nic != null && nic.isPublic() && !nic.getIPv4Addresses().isEmpty()) {
            IpAddress ip = new IpAddress(nic.getIPv4Addresses().get(0).getAddress());
            ks = findProfileForIpAddress(ip, server.getOrg());
        }
        /*
         * If there wasn't, then lets just take the first good ip address we can find
         */
        if (ks == null) {
            for (NetworkInterface tmp : server.getNetworkInterfaces()) {
                if (tmp.isPublic() && !tmp.getIPv4Addresses().isEmpty()) {
                    IpAddress ip = new IpAddress(tmp.getIPv4Addresses().get(0).getAddress());
                    ks = findProfileForIpAddress(ip, server.getOrg());
                    if (ks != null) {
                        break;
                    }
                }
            }
        }
        return ks;
    }

    /**
     * Finds the profile which is a best fit.
     * @param clientIpIn IpAddress to search ip ranges for
     * @param orgIn Org coming in from the url
     * @return best KickstartData Profile
     */
    public KickstartData findProfileForIpAddress(IpAddress clientIpIn, Org orgIn) {
        DataResult ipRanges = null;
        SelectMode mode = ModeFactory.getMode("General_queries",
            "org_ks_ip_ranges_for_ip");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgIn.getId());
        params.put("ip", clientIpIn.getLongNumber());
        ipRanges = mode.execute(params);

        IpAddressRange bestRange = null;

        // find innermost range and return profile
        for (Object ipRangeIn : ipRanges) {
            KickstartIpRangeDto range = (KickstartIpRangeDto) ipRangeIn;
            IpAddressRange iprange = new IpAddressRange(range.getMin().longValue(),
                    range.getMax().longValue(),
                    range.getId().longValue());
            //seed range if null
            bestRange = (bestRange == null) ? iprange : bestRange;
            if (iprange.isSubset(bestRange)) {
                bestRange = iprange;
            }
        }

        return (bestRange == null) ? KickstartFactory.lookupOrgDefault(orgIn) :
            KickstartFactory.lookupKickstartDataByIdAndOrg(orgIn, bestRange.getKsid());
    }


    /**
     * Accept a list of trees and only return those
     * that are valid, as in they pass the isValid method
     * @param trees the input list
     * @return List of KickstartableTree objects
     */
    public List<KickstartableTree> removeInvalid(List<KickstartableTree> trees) {
        List<KickstartableTree> ret = new LinkedList<>(trees);
        ret.removeIf(tree -> !tree.isValid());
        return ret;
    }
}
