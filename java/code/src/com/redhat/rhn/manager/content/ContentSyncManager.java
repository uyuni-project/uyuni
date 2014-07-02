/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.content;

import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;

import com.suse.contentsync.SUSEChannel;
import com.suse.contentsync.SUSEChannelFamilies;
import com.suse.contentsync.SUSEChannelFamily;
import com.suse.contentsync.SUSEChannels;
import com.suse.contentsync.SUSEUpgradePath;
import com.suse.contentsync.SUSEUpgradePaths;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;

import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static Logger log = Logger.getLogger(ContentSyncManager.class);

    // Static files we parse
    public static final String CHANNELS_XML = "/usr/share/susemanager/channels.xml";
    public static final String CHANNEL_FAMILIES_XML = "/usr/share/susemanager/channel_families.xml";
    public static final String UPGRADE_PATHS_XML = "/usr/share/susemanager/upgrade_paths.xml";

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Read the channels.xml file.
     *
     * @return List of parsed channels
     * @throws ContentSyncException in case of an error
     */
    public List<SUSEChannel> readChannels() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEChannels.class,
                    new File(CHANNELS_XML)).getChannels();
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Read the channel_families.xml file.
     *
     * @return List of parsed channel families
     * @throws ContentSyncException in case of an error
     */
    public List<SUSEChannelFamily> readChannelFamilies() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEChannelFamilies.class,
                    new File(CHANNEL_FAMILIES_XML)).getFamilies();
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Read the upgrade_paths.xml file.
     *
     * @return List of upgrade paths
     * @throws ContentSyncException in case of an error
     */
    public List<SUSEUpgradePath> readUpgradePaths() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEUpgradePaths.class,
                    new File(UPGRADE_PATHS_XML)).getPaths();
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Returns all products available to all configured credentials.
     * @return list of all available products
     */
    public Collection<SCCProduct> getProducts() {
        Set<SCCProduct> productList = new HashSet<SCCProduct>();
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query products for all mirror credentials
        for (MirrorCredentialsDto c : credentials) {
            SCCClient scc = new SCCClient(c.getUser(), c.getPassword());
            try {
                List<SCCProduct> products = scc.listProducts();
                productList.addAll(products);
            } catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + productList.size() + " available products.");
        }
        return productList;
    }

    /**
     * Returns all repositories available to all configured credentials.
     * @return list of all available repositories
     */
    public Collection<SCCRepository> getRepositories() {
        Set<SCCRepository> reposList = new HashSet<SCCRepository>();
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query repos for all mirror credentials
        for (MirrorCredentialsDto c : credentials) {
            SCCClient scc = new SCCClient(c.getUser(), c.getPassword());
            try {
                List<SCCRepository> repos = scc.listRepositories();
                reposList.addAll(repos);
            } catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + reposList.size() + " available repositories.");
        }
        return reposList;
    }
}
