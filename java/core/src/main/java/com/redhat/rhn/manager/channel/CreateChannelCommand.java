/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelNameException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChecksumLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidGPGKeyException;
import com.redhat.rhn.frontend.xmlrpc.InvalidGPGUrlException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParentChannelException;
import com.redhat.rhn.manager.content.ContentSyncManager;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * CreateChannelCommand - command to create a new channel.
 */
public class CreateChannelCommand {

    public static final int CHANNEL_NAME_MIN_LENGTH = 6;
    public static final int CHANNEL_NAME_MAX_LENGTH = 256;
    public static final int CHANNEL_LABEL_MIN_LENGTH = 6;

    public static final String CHANNEL_NAME_REGEX =
        "^[a-zA-Z\\d][\\w\\d\\s\\-\\.\\'\\(\\)\\/\\_]*$";
    public static final String CHANNEL_LABEL_REGEX =
        "^[a-z\\d][a-z\\d\\-\\.\\_]*$";

    protected static final String GPG_KEY_REGEX = "^[0-9A-F]{8}$";
    protected static final String GPG_URL_REGEX = "^(HTTPS?|FILE)://.*?$";
    protected static final String GPG_FP_REGEX = "^(\\s*[0-9A-F]{4}\\s*){10}$";
    protected static final String WEB_CHANNEL_CREATED = "web.channel_created";

    protected User user;
    protected String label;
    protected String name;
    protected String summary;
    protected String description;
    protected String archLabel;
    protected String parentLabel;
    protected Long parentId;
    protected String gpgKeyUrl;
    protected String gpgKeyId;
    protected String gpgKeyFp;
    protected boolean gpgCheck = true;
    protected String checksum;
    protected boolean vendorChannel = false;


    protected String maintainerName;
    protected String maintainerEmail;
    protected String maintainerPhone;
    protected String supportPolicy;
    protected String access = Channel.PRIVATE;
    protected boolean globallySubscribable = true;


    /**
     * default constructor.
     */
    public CreateChannelCommand() {
        user = null;
        label = null;
        name = null;
        summary = null;
        archLabel = null;
        checksum = null;
        parentLabel = null;
        parentId = null;
    }

    /**
     * @param archLabelIn The archLabel to set.
     */
    public void setArchLabel(String archLabelIn) {
        archLabel = archLabelIn;
    }

    /**
     * @param labelIn The label to set.
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @param checksumLabelIn The name to set.
     */
    public void setChecksumLabel(String checksumLabelIn) {
        this.checksum = checksumLabelIn;
    }

    /**
     * @param parentLabelIn The parentLabel to set.
     */
    public void setParentLabel(String parentLabelIn) {
        parentLabel = parentLabelIn;
    }

    /**
     * @param pid The parent id to set.
     */
    public void setParentId(Long pid) {
        parentId = pid;
    }


    /**
     * @param fp gpgkey fingerprint
     */
    public void setGpgKeyFp(String fp) {
        gpgKeyFp = fp;
    }


    /**
     * @param id gpgkey id
     */
    public void setGpgKeyId(String id) {
        gpgKeyId = id;
    }


    /**
     * @param url gpgkey url
     */
    public void setGpgKeyUrl(String url) {
        gpgKeyUrl = url;
    }

    /**
     * @param gpgCheckIn gpgCheck flag
     */
    public void setGpgCheck(boolean gpgCheckIn) {
        gpgCheck = gpgCheckIn;
    }

    /**
     * @param vendorChannelIn vendorChannel flag
     */
    public void setVendorChannel(boolean vendorChannelIn) {
        vendorChannel = vendorChannelIn;
    }

    /**
     * @param email maintainer's email address
     */
    public void setMaintainerEmail(String email) {
        maintainerEmail = email;
    }


    /**
     * @param mname maintainers name
     */
    public void setMaintainerName(String mname) {
        maintainerName = mname;
    }

    /**
     * @param phone maintainer's phone number (string)
     */
    public void setMaintainerPhone(String phone) {
        maintainerPhone = phone;
    }


    /**
     * @param policy support policy
     */
    public void setSupportPolicy(String policy) {
        supportPolicy = policy;
    }

    /**
     * @param summaryIn The summary to set.
     */
    public void setSummary(String summaryIn) {
        summary = summaryIn;
    }

    /**
     * @param desc The description.
     */
    public void setDescription(String desc) {
        description = desc;
    }

    /**
     * @param userIn The user to set.
     */
    public void setUser(User userIn) {
        user = userIn;
    }

    /**
     * @param acc public, protected, or private
     */
    public void setAccess(String acc) {
        if (acc == null || acc.equals("")) {
            access = Channel.PRIVATE;
        }
        else {
            access = acc;
        }
    }

    /**
     * @param globallySubscribableIn if the channel should be globally subscribable
     */
    public void setGloballySubscribable(boolean globallySubscribableIn) {
        globallySubscribable = globallySubscribableIn;
    }

    protected void validateChannel(ChannelArch ca, ChecksumType ct) {
        verifyRequiredParameters();
        verifyChannelName(name);
        verifyChannelLabel(label);
        verifyGpgInformation();

        if (ChannelFactory.doesChannelNameExist(name)) {
            throw new InvalidChannelNameException(name,
                InvalidChannelNameException.Reason.NAME_IN_USE,
                "edit.channel.invalidchannelname.nameinuse", name);
        }

        if (ChannelFactory.doesChannelLabelExist(label)) {
            throw new InvalidChannelLabelException(label,
                InvalidChannelLabelException.Reason.LABEL_IN_USE,
                "edit.channel.invalidchannellabel.labelinuse", label);
        }
        if (ContentSyncManager.isChannelNameReserved(name)) {
            throw new InvalidChannelNameException(name,
                    InvalidChannelNameException.Reason.NAME_RESERVED,
                    "edit.channel.invalidchannelname.namereserved", name);
        }

        if (ContentSyncManager.isChannelLabelReserved(label)) {
            throw new InvalidChannelLabelException(label,
                InvalidChannelLabelException.Reason.LABEL_RESERVED,
                "edit.channel.invalidchannellabel.labelreserved", label);
        }

        if (ca == null) {
            throw new IllegalArgumentException("Invalid architecture label");
        }

        if (ct == null) {
            throw new InvalidChecksumLabelException();
        }
    }

    /**
     * Creates the Channel based on the parameters that were set.
     * @return the newly created Channel
     * @throws InvalidChannelLabelException thrown if label is in use or invalid.
     * @throws InvalidChannelNameException throw if name is in use or invalid.
     * @throws IllegalArgumentException thrown if label, name or user are null.
     * @throws InvalidParentChannelException thrown if parent label is not a
     * valid base channel.
     */
    public Channel create() throws InvalidChannelLabelException,
            InvalidChannelNameException, InvalidParentChannelException {

        ChannelArch ca = ChannelFactory.findArchByLabel(archLabel);
        ChecksumType ct = ChannelFactory.findChecksumTypeByLabel(checksum);
        validateChannel(ca, ct);

        Channel c = new Channel();
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

        // need to save before calling stored proc below
        ChannelFactory.save(c);

        // this ends up being a mode query call, must have saved the channel to get an id
        c.setGloballySubscribable(globallySubscribable, user.getOrg());

        ChannelManager.queueChannelChange(c.getLabel(), "createchannel", "createchannel");
        ChannelFactory.refreshNewestPackageCache(c, WEB_CHANNEL_CREATED);

        return c;
    }

    /**
     * sets the parent channel of the given affected channel if pLabel or pid
     * is given. pLabel is preferred if both are given. If both pLabel and
     * pid are null or if no channel is found for the given label or pid, the
     * affected channel is unchanged.
     * @param affected The Channel to receive a new parent, if one is found.
     * @param usr The usr
     * @param lbl The parent Channel label, can be null.
     * @param pid The parent Channel id, can be null.
     */
    protected void setParentChannel(Channel affected, User usr,
                                    String lbl, Long pid) {
        Channel parent = null;

        if ((lbl == null || lbl.equals("")) &&
            pid == null) {
            // these are not the droids you seek
            return;
        }

        if (lbl != null && !lbl.equals("")) {
            parent = ChannelManager.lookupByLabelAndUser(lbl, usr);
        }
        else if (pid != null) {
            parent = ChannelManager.lookupByIdAndUser(pid, usr);
        }

        if (parent == null) {
            throw new IllegalArgumentException("Invalid Parent Channel lbl");
        }

        if (!parent.isBaseChannel()) {
            throw new InvalidParentChannelException();
        }

        // ensure child channel arch is compatible
        ChannelArch ca = affected.getChannelArch();
        if (parent != null) {
            List<Map<String, String>> compatibleArches = ChannelManager
                    .compatibleChildChannelArches(parent.getChannelArch().getLabel());
            Set<String> compatibleArchLabels = new HashSet<>();

            for (Map<String, String> arch : compatibleArches) {
                compatibleArchLabels.add(arch.get("label"));
            }

            if (!compatibleArchLabels.contains(ca.getLabel())) {
                throw new IllegalArgumentException(
                        "Incompatible parent and child channel architectures");
            }
        }

        // man that's a lot of conditionals :) finally we do what
        // we came here to do.
        affected.setParentChannel(parent);
    }

    /**
     * Verifies that the required parameters are not null.
     * @throws IllegalArgumentException thrown if label, name, user or summary
     *  are null.
     */
    protected void verifyRequiredParameters() {
        if (user == null || StringUtils.isEmpty(summary)) {
            throw new IllegalArgumentException(
                    "edit.channel.invalidchannelsummary");
        }
    }

    protected void verifyChannelName(String cname) throws InvalidChannelNameException {
        if (user == null) {
            // can never be too careful
            throw new IllegalArgumentException("Required param [user] is null");
        }

        if (cname == null || cname.trim().isEmpty()) {
            throw new InvalidChannelNameException(cname,
                InvalidChannelNameException.Reason.IS_MISSING,
                "edit.channel.invalidchannelname.missing", "");
        }

        if (!Pattern.compile(CHANNEL_NAME_REGEX).matcher(cname).find()) {
            throw new InvalidChannelNameException(cname,
                InvalidChannelNameException.Reason.REGEX_FAILS,
                "edit.channel.invalidchannelname.supportedregex", "");
        }

        if (cname.length() < CHANNEL_NAME_MIN_LENGTH) {
            Integer minLength = CreateChannelCommand.CHANNEL_NAME_MIN_LENGTH;
            throw new InvalidChannelNameException(cname,
                InvalidChannelNameException.Reason.TOO_SHORT,
                "edit.channel.invalidchannelname.minlength",
                minLength.toString());
        }

        if (cname.length() > CHANNEL_NAME_MAX_LENGTH) {
            Integer maxLength = CreateChannelCommand.CHANNEL_NAME_MAX_LENGTH;
            throw new InvalidChannelNameException(cname,
                InvalidChannelNameException.Reason.TOO_LONG,
                "edit.channel.invalidchannelname.maxlength",
                maxLength.toString());
        }
    }

    protected void verifyChannelLabel(String clabel) throws InvalidChannelLabelException {

        if (user == null) {
            // can never be too careful
            throw new IllegalArgumentException("Required param is null");
        }

        if (clabel == null || clabel.trim().isEmpty()) {
            throw new InvalidChannelLabelException(clabel,
                InvalidChannelLabelException.Reason.IS_MISSING,
                "edit.channel.invalidchannellabel.missing", "");
        }

        if (!Pattern.compile(CHANNEL_LABEL_REGEX).matcher(clabel).find()) {
            throw new InvalidChannelLabelException(clabel,
                InvalidChannelLabelException.Reason.REGEX_FAILS,
                "edit.channel.invalidchannellabel.supportedregex", "");
        }

        if (clabel.length() < CHANNEL_LABEL_MIN_LENGTH) {
            Integer minLength = CreateChannelCommand.CHANNEL_LABEL_MIN_LENGTH;
            throw new InvalidChannelLabelException(clabel,
                InvalidChannelLabelException.Reason.TOO_SHORT,
                "edit.channel.invalidchannellabel.minlength",
                minLength.toString());
        }
    }

    protected void verifyGpgInformation() {
        if (StringUtils.isNotEmpty(gpgKeyId)) {
            gpgKeyId = gpgKeyId.toUpperCase();
            if (!Pattern.compile(GPG_KEY_REGEX).matcher(gpgKeyId).find()) {
                throw new InvalidGPGKeyException();
            }
        }

        if (StringUtils.isNotEmpty(gpgKeyFp)) {
            gpgKeyFp = gpgKeyFp.toUpperCase();
            if (!Pattern.compile(GPG_FP_REGEX).matcher(gpgKeyFp).find()) {
                throw new InvalidGPGFingerprintException();
            }
        }

        if (StringUtils.isNotEmpty(gpgKeyUrl)) {
            // file: URLs can be case-sensitive, can't blindly uppercase here
            String tmp = gpgKeyUrl.toUpperCase();
            if (!Pattern.compile(GPG_URL_REGEX).matcher(tmp).find()) {
                throw new InvalidGPGUrlException();
            }
        }
    }
}
