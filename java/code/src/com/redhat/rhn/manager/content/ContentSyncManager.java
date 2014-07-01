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
import com.suse.contentsync.SUSEChannels;
import com.suse.contentsync.SUSEFamily;
import com.suse.contentsync.SUSEUpgradePath;
import com.suse.contentsync.SUSEUpgradePaths;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;
import java.io.File;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.simpleframework.xml.core.Persister;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {
    public static final String CHANNELS_XML = "/usr/share/susemanager/channels.xml";
    public static final String CHANNELS_FAMILIES_XML = "/usr/share/susemanager/channel_families.xml";
    public static final String UPGRADE_PATHS_XML = "/usr/share/susemanager/upgrade_paths.xml";

    // Logger instance
    private static Logger log = Logger.getLogger(ContentSyncManager.class);

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    private final Persister persister = new Persister();

    /**
     * Get channels.
     *
     * @return
     * @throws Exception
     */
    public List<SUSEChannel> readChannels() throws Exception {
        return this.persister.read(SUSEChannels.class,
                                   new File(CHANNELS_XML)).getChannels();
    }

    /**
     * Get families.
     *
     * @return
     * @throws Exception
     */
    public List<SUSEFamily> readFamilies() throws Exception {
        return this.persister.read(SUSEChannelFamilies.class,
                                   new File(CHANNELS_FAMILIES_XML)).getFamilies();
    }

    /**
     * Get upgrade paths.
     *
     * @return
     * @throws Exception
     */
    public List<SUSEUpgradePath> readUpgradePaths() throws Exception {
        return this.persister.read(SUSEUpgradePaths.class,
                                   new File(UPGRADE_PATHS_XML)).getPaths();
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
