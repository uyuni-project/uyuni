/*
 * Copyright (c) 2015 Red Hat, Inc.
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

package com.redhat.rhn.manager.channel;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelNameException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParentChannelException;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.cloud.CloudPaygManager;

import java.util.Optional;

/**
 * CreateChannelCommand - command to clone a channel.
 */
public class CloneChannelCommand extends CreateChannelCommand {

    private CloneBehavior cloneBehavior;
    private Channel original;
    private String DEFAULT_PREFIX = "clone-of-";
    private boolean stripModularMetadata = false;
    private CloudPaygManager cloudPaygManager;

    /**
     * Clone Behavior type
     */
    public enum CloneBehavior {
        ORIGINAL_STATE,
        CURRENT_STATE,
        EMPTY
    }

    /**
     * Constructor
     * @param cloneBehaviorIn the cloning behavior
     * @param cloneFrom channel to clone from
     */
    public CloneChannelCommand(CloneBehavior cloneBehaviorIn, Channel cloneFrom) {
        user = null;
        label = DEFAULT_PREFIX + cloneFrom.getLabel();
        name =  DEFAULT_PREFIX + cloneFrom.getName();
        original = cloneFrom;
        cloneBehavior = cloneBehaviorIn;
        summary = cloneFrom.getSummary();
        checksum = cloneFrom.getChecksumTypeLabel();
        gpgKeyUrl = cloneFrom.getGPGKeyUrl();
        gpgKeyId = cloneFrom.getGPGKeyId();
        gpgKeyFp = cloneFrom.getGPGKeyFp();
        gpgCheck  = cloneFrom.isGPGCheck();
        archLabel = Optional.ofNullable(cloneFrom.getChannelArch()).map(ChannelArch::getLabel).orElse("");
        cloudPaygManager = GlobalInstanceHolder.PAYG_MANAGER;
    }

    /**
     * Constructor for testing
     * @param cloneBehaviorIn the cloning behavior
     * @param cloneFrom channel to clone from
     * @param testCloudPaygManager {@link CloudPaygManager} to use
     */
    public CloneChannelCommand(CloneBehavior cloneBehaviorIn, Channel cloneFrom,
                               CloudPaygManager testCloudPaygManager) {
        this(cloneBehaviorIn, cloneFrom);
        if (testCloudPaygManager != null) {
            cloudPaygManager = testCloudPaygManager;
        }
    }

    /**
     * Clones Channel based on the parameters that were set.
     * @return the newly cloned Channel
     * @throws InvalidChannelLabelException thrown if label is in use or invalid.
     * @throws InvalidChannelNameException throw if name is in use or invalid.
     * @throws IllegalArgumentException thrown if label, name or user are null.
     * @throws InvalidParentChannelException thrown if parent label is not a
     * valid base channel.
     */
    @Override
    public Channel create()
        throws InvalidChannelLabelException, InvalidChannelNameException,
        InvalidParentChannelException {

        ChannelArch ca = ChannelFactory.findArchByLabel(archLabel);
        ChecksumType ct = ChannelFactory.findChecksumTypeByLabel(checksum);
        validateChannel(ca, ct);

        ClonedChannel c = new ClonedChannel();
        c.setLabel(label);
        c.setName(name);
        c.setSummary(summary);
        c.setDescription(description);
        c.setOrg(user.getOrg());
        c.setBaseDir("/dev/null");
        c.setChannelArch(ca);

        // handles either parent id or label
        setParentChannel(c, user, parentLabel, parentId);
        c.setChecksumType(ct);
        c.setGPGKeyId(gpgKeyId);
        c.setGPGKeyUrl(gpgKeyUrl);
        c.setGPGKeyFp(gpgKeyFp);
        c.setGPGCheck(gpgCheck);
        c.setAccess(access);
        c.setMaintainerName(maintainerName);
        c.setMaintainerEmail(maintainerEmail);
        c.setMaintainerPhone(maintainerPhone);
        c.setSupportPolicy(supportPolicy);
        c.addChannelFamily(user.getOrg().getPrivateChannelFamily());

        // cloned channel stuff
        c.setProductName(original.getProductName());
        c.setUpdateTag(original.getUpdateTag());
        c.setInstallerUpdates(original.isInstallerUpdates());
        c.setOriginal(original);

        // PAYG Code to avoid cloning channels under forbidden channels
        if (cloudPaygManager.isPaygInstance()) {
            if (c.getParentChannel() != null) {
                Optional<Channel> channelTest = c.getParentChannel().originChain()
                        .flatMap(n -> n.getAccessibleChildrenFor(user).stream())
                        .filter(n -> n.getId() != null) // We filter out the cloned channel from the list
                        .filter(n -> n.getProductName().getLabel().equals(original.getProductName().getLabel()))
                        .findFirst();

                if (channelTest.isEmpty()) {
                    throw new ForbiddenCloneChannelPAYGException();
                }
            }
        }

        // need to save before calling stored procs below
        ChannelFactory.save(c);
        c = HibernateFactory.reload(c);

        if (stripModularMetadata) {
            if (c.getModules() != null) {
                HibernateFactory.getSession().delete(c.getModules());
            }
            c.setModules(null);
            AppStreamsManager.listChannelAppStreams(c.getId()).forEach(a ->
                    HibernateFactory.getSession().delete(a)
            );
        }
        else {
            c.cloneModulesFrom(original);
        }

        // This ends up being a mode query call so need to save first to get channel id
        c.setGloballySubscribable(globallySubscribable, user.getOrg());

        if (cloneBehavior == CloneBehavior.ORIGINAL_STATE) {
            // original packages only, no errata
            ChannelManager.cloneOriginalChannelPackages(original.getId(), c.getId());
            ChannelFactory.refreshNewestPackageCache(c.getId(), "cloning as original");
        }
        else if (cloneBehavior == CloneBehavior.CURRENT_STATE) {
            ChannelManager.cloneChannelPackages(original.getId(), c.getId());
            ChannelFactory.cloneNewestPackageCache(original.getId(), c.getId());
            ErrataManager.cloneChannelErrata(original.getId(), c.getId(), user);
        }

        ChannelManager.queueChannelChange(c.getLabel(), "clonechannel", "cloned from " +
                original.getLabel());

        return c;
    }

    /**
     * Controls of behavior of cloning the modular metadata to the channel
     *
     * @param stripModularMetadataIn true if the metadata should be stripped
     */
    public void setStripModularMetadata(boolean stripModularMetadataIn) {
        this.stripModularMetadata = stripModularMetadataIn;
    }
}
