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
package com.redhat.rhn.frontend.xmlrpc.packages.search;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.TokenPackage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.action.channel.PackageSearchHelper;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.SearchServerCommException;
import com.redhat.rhn.frontend.xmlrpc.SearchServerQueryException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.session.SessionManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redstone.xmlrpc.XmlRpcFault;


/**
 * PackagesSearchHandler
 * @apidoc.namespace packages.search
 * @apidoc.doc Methods to interface to package search capabilities in search server..
 */
public class PackagesSearchHandler extends BaseHandler {

    private static Logger log = LogManager.getLogger(PackagesSearchHandler.class);

    /**
     * Searches the lucene package indexes based on package name
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param name - package name to search for
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Search the lucene package indexes for all packages which
     *          match the given name.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "package name to search for")
     * @apidoc.returntype
     * #return_array_begin()
     *   $PackageOverviewSerializer
     * #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> name(String sessionKey, String name)
        throws FaultException {
        return performSearch(sessionKey, name, BaseSearchAction.OPT_NAME_ONLY);
    }

    /**
     * Searches the lucene package indexes based on package name or description
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param query -text to match in package name and description
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Search the lucene package indexes for all packages which
     *          match the given query in name or description
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "query", "text to match in package name or description")
     * @apidoc.returntype
     * #return_array_begin()
     *   $PackageOverviewSerializer
     * #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> nameAndDescription(String sessionKey, String query)
        throws FaultException {
        return performSearch(sessionKey, query, BaseSearchAction.OPT_NAME_AND_DESC);
    }

    /**
     * Searches the lucene package indexes based on package name or summary
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param query -text to match in package name and summary
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Search the lucene package indexes for all packages which
     *          match the given query in name or summary.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "query", "text to match in package name or summary")
     * @apidoc.returntype
     * #return_array_begin()
     *   $PackageOverviewSerializer
     * #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> nameAndSummary(String sessionKey, String query)
        throws FaultException {
        return performSearch(sessionKey, query, BaseSearchAction.OPT_NAME_AND_SUMMARY);
    }
    /**
     * Advanced method to search lucene indexes with a passed in query written in Lucene
     * Query Parser syntax. Lucene Query Parser syntax is defined at lucene.apache.org:
     * http://lucene.apache.org/java/3_5_0/queryparsersyntax.html
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param luceneQuery - a search query written in the form of
     *  Lucene QueryParser Syntax,
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Advanced method to search lucene indexes with a passed in query written
     * in Lucene Query Parser syntax.<br>
     * Lucene Query Parser syntax is defined at
     * <a href="http://lucene.apache.org/java/3_5_0/queryparsersyntax.html" target="_blank">
     * lucene.apache.org</a>.<br>
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary<br>
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "luceneQuery", "a query written in the form of Lucene QueryParser Syntax")
     * @apidoc.returntype
     *   #return_array_begin()
     *      $PackageOverviewSerializer
     *   #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> advanced(String sessionKey, String luceneQuery)
        throws FaultException {

        return performSearch(sessionKey, luceneQuery, BaseSearchAction.OPT_FREE_FORM);
    }



    /**
     * Advanced method to search lucene indexes with a passed in query written in Lucene
     * Query Parser syntax, additionally this method will limit results to those which are
     * in the passed in channel label.<br>
     * Lucene Query Parser syntax is defined at lucene.apache.org:
     * http://lucene.apache.org/java/3_5_0/queryparsersyntax.html
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary<br>
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param luceneQuery - a search query written in the form of Lucene QueryParser Syntax
     * @param channelLabel - channel label
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Advanced method to search lucene indexes with a passed in query written
     * in Lucene Query Parser syntax, additionally this method will limit results to those
     * which are in the passed in channel label.<br>
     * Lucene Query Parser syntax is defined at
     * <a href="http://lucene.apache.org/java/3_5_0/queryparsersyntax.html" target="_blank">
     * lucene.apache.org</a>.<br>
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary<br>
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "luceneQuery", "a query written in the form of Lucene QueryParser Syntax")
     * @apidoc.param #param_desc("string", "channelLabel", "the channel Label")
     * @apidoc.returntype
     *   #return_array_begin()
     *      $PackageOverviewSerializer
     *   #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> advancedWithChannel(String sessionKey,
            String luceneQuery, String channelLabel) throws FaultException {
        if (StringUtils.isBlank(channelLabel)) {
            throw new InvalidChannelLabelException();
        }
        List<PackageOverview> pkgs = performSearch(sessionKey, luceneQuery,
                        BaseSearchAction.OPT_FREE_FORM);
        WebSession session = SessionManager.loadSession(sessionKey);
        User user = session.getUser();
        Channel channel = ChannelManager.lookupByLabelAndUser(channelLabel, user);
        if (channel == null) {
            throw new InvalidChannelLabelException();
        }
        List<PackageDto> pkgsInChan = ChannelManager.listAllPackages(channel);
        Set<Long> temp = new HashSet<>();
        for (PackageDto pdto : pkgsInChan) {
            temp.add(pdto.getId());
        }
        // Lookup what packages are in what channel and filter
        List<PackageOverview> result = new ArrayList<>();
        for (PackageOverview pOver : pkgs) {
            if (temp.contains(pOver.getId())) {
                result.add(pOver);
            }
        }
        return result;
    }


    /**
     * Advanced method to search lucene indexes with a passed in query written in Lucene
     * Query Parser syntax, additionally this method will limit results to those which are
     * associated with a given activation key. Lucene Query Parser syntax is defined
     * at lucene.apache.org:
     * http://lucene.apache.org/java/3_5_0/queryparsersyntax.html
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     *
     * @param sessionKey The sessionKey for the logged in used
     * @param luceneQuery - a search query written in the form of Lucene QueryParser Syntax
     * @param activationKey - activation key
     * @return the package objects requested
     * @throws FaultException A FaultException is thrown on error.
     *
     * @apidoc.doc Advanced method to search lucene indexes with a passed in query written
     * in Lucene Query Parser syntax, additionally this method will limit results to those
     * which are associated with a given activation key.<br>
     * Lucene Query Parser syntax is defined at
     * <a href="http://lucene.apache.org/java/3_5_0/queryparsersyntax.html" target="_blank">
     * lucene.apache.org</a>.<br>
     * Fields searchable for Packages:
     * name, epoch, version, release, arch, description, summary<br>
     * Lucene Query Example: "name:kernel AND version:2.6.18 AND -description:devel"
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "luceneQuery", "a query written in the form of Lucene QueryParser Syntax")
     * @apidoc.param #param_desc("string", "activationKey", "activation key to look for packages in")
     * @apidoc.returntype
     *   #return_array_begin()
     *      $PackageOverviewSerializer
     *   #array_end()
     *  */
    @ReadOnly
    public List<PackageOverview> advancedWithActKey(String sessionKey,
            String luceneQuery, String activationKey) throws FaultException {
        if (StringUtils.isBlank(activationKey)) {
            throw new MethodInvalidParamException();
        }
        List<PackageOverview> pkgs = performSearch(sessionKey, luceneQuery,
                        BaseSearchAction.OPT_FREE_FORM);
        WebSession session = SessionManager.loadSession(sessionKey);
        User user = session.getUser();
        // Lookup what packages are in the activation key and filter
        ActivationKey key = ActivationKeyManager.getInstance().lookupByKey(activationKey, user);
        if (key == null) {
            throw new MethodInvalidParamException();
        }
        Set<TokenPackage> keyPkgs = key.getPackages();
        if (keyPkgs == null) {
            return Collections.emptyList();
        }
        // build up a set of all package names and archs in act key
        Set<String> ids = new HashSet<>();
        for (TokenPackage tPkg : keyPkgs) {
            String value = tPkg.getPackageName().getName();
            if (tPkg.getPackageArch() != null) {
                value += tPkg.getPackageArch().getLabel();
            }
            ids.add(value);
        }
        // filter out any packages not in act key
        List<PackageOverview> results = new ArrayList<>();
        for (PackageOverview pOver : pkgs) {
            String value = pOver.getPackageName();
            // First check to see if package name only is in act key
            if (ids.contains(value)) {
                results.add(pOver);
            }
            else {
                // Now check to see if package name is restricted with arch in key
                value += pOver.getPackageArch();
                if (ids.contains(value)) {
                    results.add(pOver);
                }
            }
        }
        return results;
    }

    protected List<PackageOverview> performSearch(String sessionKey, String query,
            String mode) throws FaultException {
        if (StringUtils.isBlank(query)) {
            throw new SearchServerQueryException();
        }
        WebSession session = SessionManager.loadSession(sessionKey);
        Long sessionId = session.getId();
        List<PackageOverview> pkgs = null;
        try {
            pkgs = PackageSearchHelper.performSearch(sessionId, query, mode, null, null,
                            true, null, BaseSearchAction.WHERE_ALL);
        }
        catch (MalformedURLException | XmlRpcFault e) {
            log.info("Caught Exception", e);
            throw new SearchServerCommException();
        }
        // Connection error

        if (log.isDebugEnabled()) {
            log.debug("Query = : {}, mode = {}", query, mode);
            log.debug("{} packages were fetched", pkgs.size());
        }
        return pkgs;
    }
}
