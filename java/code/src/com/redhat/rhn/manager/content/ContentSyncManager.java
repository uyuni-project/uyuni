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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static Logger log = Logger.getLogger(ContentSyncManager.class);

    // Static files we parse
    private static final String CHANNELS_XML = "channels.xml";
    private static final String CHANNEL_FAMILIES_XML = "channel_families.xml";
    private static final String UPGRADE_PATHS_XML = "upgrade_paths.xml";

    // The default path where to find those
    private String pathPrefix = "/usr/share/susemanager/";

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set a directory where to find channels.xml etc.
     * @param path the path prefix to set
     */
    public void setPathPrefix(String path) {
        this.pathPrefix = path;
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
                    new File(pathPrefix + CHANNELS_XML)).getChannels();
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
                    new File(pathPrefix + CHANNEL_FAMILIES_XML)).getFamilies();
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
                    new File(pathPrefix + UPGRADE_PATHS_XML)).getPaths();
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
            }
            catch (SCCClientException e) {
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
            }
            catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + reposList.size() + " available repositories.");
        }
        return reposList;
    }

    /**
     * Refresh functionality doing the same thing as --refresh in mgr-ncc-sync.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void refresh() throws ContentSyncException {
        updateChannels();
        updateChannelFamilies();
        updateSUSEProducts();
    }

    /**
     * Update channel information in the database.
     */
    public void updateChannels() throws ContentSyncException {
        // TODO if ISS slave then do nothing
        // Read contents of channels.xml into a map
        Map<String, SUSEChannel> channelsXML = new HashMap<String, SUSEChannel>();
        for (SUSEChannel c : readChannels()) {
            channelsXML.put(c.getLabel(), c);
        }

        // Get all vendor channels from the database
        List<Channel> channelsDB = ChannelFactory.listVendorChannels();
        for (Channel c : channelsDB) {
            if (channelsXML.containsKey(c.getLabel())) {
                SUSEChannel channel = channelsXML.get(c.getLabel());
                if (!channel.getDescription().equals(c.getDescription()) ||
                        !channel.getName().equals(c.getName()) ||
                        !channel.getSummary().equals(c.getSummary()) ||
                        !channel.getUpdateTag().equals(c.getUpdateTag())) {
                    // There is a difference, copy channel attributes and save
                    c.setDescription(channel.getDescription());
                    c.setName(channel.getName());
                    c.setSummary(channel.getSummary());
                    c.setUpdateTag(channel.getUpdateTag());
                    ChannelFactory.save(c);
                }
            }
            else {
                // Channel no longer mirrorable
            }
        }

        // Update content source URLs
        List<ContentSource> contentSources = ChannelFactory.listVendorContentSources();
        for (ContentSource cs : contentSources) {
            if (channelsXML.containsKey(cs.getLabel())) {
                // TODO: Check if self._alternativeMirrorUrl(channel) is needed
                SUSEChannel channel = channelsXML.get(cs.getLabel());
                if (!channel.getSourceUrl().equals(cs.getSourceUrl())) {
                    cs.setSourceUrl(channel.getSourceUrl());
                    ChannelFactory.save(cs);
                }
            }
        }
    }

    /**
     * Create new or update an existing channel family.
     * @return {@link ChannelFamily}
     */
    private ChannelFamily configureChannelFamilyByLabel(SUSEChannelFamily channel) {
        ChannelFamily family = ChannelFamilyFactory.lookupByLabel(channel.getLabel(), null);
        if (family == null) {
            family = new ChannelFamily();
            family.setLabel(channel.getLabel());
            family.setOrg(null);
            family.setName(channel.getName());
            ChannelFamilyFactory.save(family);
        }

        return family;
    }

    private PrivateChannelFamily newPrivateChannelFamily(ChannelFamily family) {
        PrivateChannelFamily pf = new PrivateChannelFamily();
        pf.setCreated(new Date());
        pf.setCurrentMembers(0L);
        pf.setMaxMembers(0L);
        pf.setOrg(null);
        pf.setChannelFamily(family);

        return pf;
    }

    /**
     * Update channel families in the database.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateChannelFamilies() throws ContentSyncException {
        for (SUSEChannelFamily scf : this.readChannelFamilies()) {
            ChannelFamily family = this.configureChannelFamilyByLabel(scf);
            if (family.getPrivateChannelFamilies().isEmpty()) {
                PrivateChannelFamily pf = this.newPrivateChannelFamily(family);
                if (scf.getDefaultNodeCount() < 0) {
                    // The "limitless or endless in space" at SUSE is 200000. Of type Long.
                    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L43
                    pf.setMaxMembers(200000L);
                }

                family.addPrivateChannelFamily(pf);
                ChannelFamilyFactory.save(family);
            }
        }
    }

    /**
     * Creates/updates entries in the SUSE Products DB table.
     */
    private void updateSUSEProducts() throws ContentSyncException {
        // TODO: Implement this!
    }
}
