/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.KickstartTreeUpdateType;
import com.redhat.rhn.manager.kickstart.KickstartFormatter;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Profile;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * KickstartData - Class representation of the table RhnKSData.
 */
@Entity
@Table(name = "rhnKSData")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ks_type")
@DiscriminatorValue("wizard")
public class KickstartData extends BaseDomainHelper {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_KS_ID_SEQ")
	@SequenceGenerator(name = "RHN_KS_ID_SEQ", sequenceName = "RHN_KS_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "ks_type", updatable = false, insertable = false)
    protected String kickstartType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @Column(nullable = false)
    private String label;

    @Column
    private String comments;

    @Column(nullable = false)
    @Convert(converter = YesNoConverter.class)
    private Boolean active;

    @Column
    @Convert(converter = YesNoConverter.class)
    private Boolean postLog;

    @Column
    @Convert(converter = YesNoConverter.class)
    private Boolean preLog;

    @Column(name = "kscfg")
    @Convert(converter = YesNoConverter.class)
    private Boolean ksCfg;

    @Column(name = "is_org_default", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean isOrgDefault;

    @Column(name = "kernel_params")
    private String kernelParams;

    @Column(name = "nonchrootpost")
    @Convert(converter = YesNoConverter.class)
    private Boolean nonChrootPost;

    @Column(name = "verboseup2date")
    @Convert(converter = YesNoConverter.class)
    private Boolean verboseUp2date;

    @Column(name = "cobbler_id")
    private String cobblerId;

    @Column(name = "partition_data")
    private byte[] partitionData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnCryptoKeyKickstart",
            joinColumns = @JoinColumn(name = "ksdata_id"),
            inverseJoinColumns = @JoinColumn(name = "crypto_key_id"))
    private Set<CryptoKey> cryptoKeys;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnKickstartChildChannel",
            joinColumns = @JoinColumn(name = "ksdata_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<Channel> childChannels;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnKickstartDefaultRegtoken",
            joinColumns = @JoinColumn(name = "kickstart_id"),
            inverseJoinColumns = @JoinColumn(name = "regtoken_id"))
    private Set<Token> defaultRegTokens;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnKickstartPreserveFileList",
            joinColumns = @JoinColumn(name = "kickstart_id"),
            inverseJoinColumns = @JoinColumn(name = "file_list_id"))
    private Set<FileList> preserveFileLists;

    @OneToMany(mappedBy = "ksData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @SortNatural
    private Set<KickstartPackage> ksPackages = new HashSet<>();

    @OneToMany(mappedBy = "kickstartData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Collection<KickstartCommand> commands = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ksdata", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<KickstartIpRange> ips; // rhnKickstartIpRange

    @OneToMany(mappedBy = "ksdata", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<KickstartScript> scripts;      // rhnKickstartScript

    @OneToOne(mappedBy = "ksdata", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private KickstartDefaults kickstartDefaults;

    @Column(name = "no_base")
    @Convert(converter = YesNoConverter.class)
    private boolean noBase;

    @Column(name = "ignore_missing")
    @Convert(converter = YesNoConverter.class)
    private boolean ignoreMissing;

    @Column(name = "update_type", nullable = false)
    private String updateType;

    private static final Pattern URL_REGEX =
            Pattern.compile("--url\\s*(\\S+)", Pattern.CASE_INSENSITIVE);
    public static final String LEGACY_KICKSTART_PACKAGE_NAME = "auto-kickstart-";

    public static final String WIZARD_DIR = "wizard";
    public static final String RAW_DIR = "upload";

    public static final String SELINUX_MODE_COMMAND = "selinux";

    public static final String TYPE_WIZARD = "wizard";
    public static final String TYPE_RAW = "raw";
    private static String[] advancedOptions =
        {"partitions", "raids", "logvols", "volgroups", "include",
        "repo", "custom", "custom_partition"};

    private static final List<String> ADANCED_OPTIONS = Arrays.asList(advancedOptions);

    /**
     * Initializes properties.
     */
    public KickstartData() {
        cryptoKeys = new HashSet<>();
        defaultRegTokens = new HashSet<>();
        preserveFileLists = new HashSet<>();
        ksPackages = new TreeSet<>();
        commands = new LinkedHashSet<>();
        ips = new HashSet<>();
        scripts = new HashSet<>();
        postLog = false;
        preLog = false;
        ksCfg = false;
        verboseUp2date = false;
        nonChrootPost = false;
        childChannels = new HashSet<>();
        kickstartType = TYPE_WIZARD;
        noBase = false;
        ignoreMissing = false;
        setUpdateType(KickstartTreeUpdateType.NONE.getType());
    }

    /**
     * Logger for this class
     */
    private static Logger logger = LogManager.getLogger(KickstartData.class);

    /**
     * Getter for id
     * @return Long to get
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Associates the KS with an Org.
     * @param orgIn Org to be associated to this KS.
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * Getter for org
     * @return org to get
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Getter for label
     * @return String to get
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for label
     * @param labelIn to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Getter for comments
     * @return String to get
     */
    public String getComments() {
        return this.comments;
    }

    /**
     * Setter for comments
     * @param commentsIn to set
     */
    public void setComments(String commentsIn) {
        this.comments = commentsIn;
    }

    /**
     * Getter for active
     * @return String to get
     */
    public Boolean getActive() {
        return this.active;
    }

    /**
     * Setter for active
     * @param activeIn to set
     */
    public void setActive(Boolean activeIn) {
        this.active = activeIn;
    }

    /**
     * Getter for isOrgDefault
     * @return String to get
     */
    public boolean isOrgDefault() {
        return getIsOrgDefault();
    }

    /**
     * Getter for isOrgDefault
     * @return String to get
     */
    protected boolean getIsOrgDefault() {
        return this.isOrgDefault;
    }
    /**
     * Setter for isOrgDefault
     * @param isDefault to set
     */
    protected void setIsOrgDefault(boolean isDefault) {
        this.isOrgDefault = isDefault;
    }

    /**
     * Setter for isOrgDefault
     * @param isDefault to set
     */
    public void setOrgDefault(boolean isDefault) {
        // We actually want to set the orgdefault
        if (!isOrgDefault() &&
                isDefault) {
            KickstartData existingDefault = KickstartFactory.
                    lookupOrgDefault(getOrg());
            if (existingDefault != null) {
                existingDefault.setIsOrgDefault(Boolean.FALSE);
                KickstartFactory.saveKickstartData(existingDefault);
            }
        }
        setIsOrgDefault(isDefault);
    }

    /**
     * Getter for kernelParams
     * @return String to get
     */
    public String getKernelParams() {
        return this.kernelParams;
    }

    /**
     * Setter for kernelParams
     * @param kernelParamsIn to set
     */
    public void setKernelParams(String kernelParamsIn) {
        this.kernelParams = kernelParamsIn;
    }

    /**
     * @return the cryptoKeys
     */
    public Set<CryptoKey> getCryptoKeys() {
        return cryptoKeys;
    }


    /**
     * @param cryptoKeysIn The cryptoKeys to set.
     */
    public void setCryptoKeys(Set<CryptoKey> cryptoKeysIn) {
        this.cryptoKeys = cryptoKeysIn;
    }

    /**
     * Add a CryptoKey to this kickstart
     * @param key to add
     */
    public void addCryptoKey(CryptoKey key) {
        this.cryptoKeys.add(key);
    }

    /**
     * Remove a crypto key from the set.
     * @param key to remove.
     */
    public void removeCryptoKey(CryptoKey key) {
        this.cryptoKeys.remove(key);
    }

    /**
     * @return the childChannels
     */
    public Set<Channel> getChildChannels() {
        return childChannels;
    }

    /**
     * @param childChannelsIn childChannels to set.
     */
    public void setChildChannels(Set<Channel> childChannelsIn) {
        this.childChannels = childChannelsIn;
    }

    /**
     * Add a ChildChannel to this kickstart
     * @param childChnl to add
     */
    public void addChildChannel(Channel childChnl) {
        if (this.childChannels == null) {
            this.childChannels = new HashSet<>();
        }
        this.childChannels.add(childChnl);
    }

    /**
     * Remove a child Channel from the set.
     * @param childChnl to remove.
     */
    public void removeChildChannel(Channel childChnl) {
        this.childChannels.remove(childChnl);
    }

    /**
     * Adds an Token object to default.
     * Note that an ActivationKey is almost the same as a Token.  Sorry.
     * @param key Token to add
     */
    public void addDefaultRegToken(Token key) {
        defaultRegTokens.add(key);
    }

    /**
     * Getter for defaultRegTokens
     * @return Returns the packageLists.
     */
    public Set<Token> getDefaultRegTokens() {
        return defaultRegTokens;
    }

    /**
     * Setter for defaultRegTokens
     * @param p The packageLists to set.
     */
    public void setDefaultRegTokens(Set<Token> p) {
        this.defaultRegTokens = p;
    }

    /**
     * Gets the value of preserveFileLists
     *
     * @return the value of preserveFileLists
     */
    public Set<FileList> getPreserveFileLists() {
        return this.preserveFileLists;
    }

    /**
     * Sets the value of preserveFileLists
     *
     * @param preserveFileListsIn set of FileList objects to assign to
     * this.preserveFileLists
     */
    public void setPreserveFileLists(Set<FileList> preserveFileListsIn) {
        this.preserveFileLists = preserveFileListsIn;
    }

    /**
     * Adds a PreserveFileList object to preserveFileLists
     * @param fileList preserveFileList to add
     */
    public void addPreserveFileList(FileList fileList) {
        preserveFileLists.add(fileList);
    }

    /**
     * Remove a file list from the set.
     * @param fileList to remove.
     */
    public void removePreserveFileList(FileList fileList) {
        this.preserveFileLists.remove(fileList);
    }

    /**
     * Adds a KickstartPackage object to ksPackages.
     * @param kp KickstartPackage to add
     */

    public void addKsPackage(KickstartPackage kp) {
        kp.setPosition((long)ksPackages.size());
        if (this.ksPackages.add(kp)) {              // save to collection
            KickstartFactory.savePackage(kp);       // save to DB
        }
    }

    /**
     * Removes a KickstartPackage object from ksPackages.
     * @param kp KickstartPackage to remove
     */

    public void removeKsPackage(KickstartPackage kp) {
        this.ksPackages.remove(kp);
    }

    /**
     * Getter for ksPackages
     * @return Returns the ksPackages.
     */
    public Set<KickstartPackage> getKsPackages() {
        return ksPackages;
    }

    /**
     * Setter for ksPackages
     * @param p The KickstartPackage set to set.
     */
    public void setKsPackages(Set<KickstartPackage> p) {
        this.ksPackages = p;
    }

    /**
     * Clear all ksPackages
     */
    public void clearKsPackages() {
        for (Iterator<KickstartPackage> iter = ksPackages.iterator(); iter.hasNext();) {
            // remove from DB
            KickstartFactory.removePackage(iter.next());
            // remove from collection
            iter.remove();
        }
    }

    /**
     *
     * @param pName Package name to check if Kickstart Data contains
     * @return if package name is in this kickstart data
     */
    public boolean hasKsPackage(PackageName pName) {
        for (KickstartPackage pack : ksPackages) {
            if (pName.equals(pack.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the KickstartScript of type "pre"
     * @return KickstartScript used by the Pre section.  Null if not used
     */
    public KickstartScript getPreKickstartScript() {
        return lookupScriptByType(KickstartScript.TYPE_PRE);
    }

    /**
     * Get the KickstartScript of type "post"
     * @return KickstartScript used by the post section.  Null if not used
     */
    public KickstartScript getPostKickstartScript() {
        return lookupScriptByType(KickstartScript.TYPE_POST);
    }


    private KickstartScript lookupScriptByType(String typeIn) {
        if (this.getScripts() != null &&
                !this.getScripts().isEmpty()) {
            for (KickstartScript kss : this.getScripts()) {
                if (kss.getScriptType().equals(typeIn)) {
                    return kss;
                }
            }
        }
        return null;
    }

    /**
     * Getter for commands
     * @return Returns commands
     */
    public Collection<KickstartCommand> getCommands() {
        return this.commands;
    }

    /**
     * Convenience method to detect if command is set
     * @param commandName Command name
     * @return true if found, otherwise false
     */
    public boolean hasCommand(String commandName) {
        boolean retval = false;
        if (this.commands != null && !this.commands.isEmpty()) {
            for (KickstartCommand cmd : this.commands) {
                if (cmd.getCommandName().getName().equals(label)) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Convenience method to remove commands by name
     * @param commandName Command name
     * @param removeFirst if true only stop at first instance, otherwise remove all
     */
    public void removeCommand(String commandName, boolean removeFirst) {
        if (this.commands != null && !this.commands.isEmpty()) {
            for (Iterator<KickstartCommand> iter = this.commands.iterator();
                    iter.hasNext();) {
                KickstartCommand cmd = iter.next();
                if (cmd.getCommandName().getName().equals(commandName)) {
                    iter.remove();
                    if (removeFirst) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Convenience method to find a command by name stopping at the first match
     * @param commandName Command name
     * @return command if found, otherwise null
     */
    public KickstartCommand getCommand(String commandName) {
        KickstartCommand retval = null;
        if (this.commands != null && !this.commands.isEmpty()) {
            for (KickstartCommand cmd : this.commands) {
                if (cmd.getCommandName().getName().equals(commandName)) {
                    retval = cmd;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Setter for commands
     * @param c The Command List to set.
     */
    public void setCommands(Collection<KickstartCommand> c) {
        this.commands = c;
    }

    /**
     * Convenience method to remove all commands
     */
    public void removeCommands() {
        if (this.commands != null && !this.commands.isEmpty()) {
            this.commands.clear();
        }
    }

    private Set<KickstartCommand> getCommandSubset(String name) {
        Set<KickstartCommand> retval = new LinkedHashSet<>();
        if (this.commands != null && !this.commands.isEmpty()) {
            for (KickstartCommand cmd : this.commands) {
                logger.debug("getCommandSubset : working with: {}", cmd.getCommandName().getName());
                if (cmd.getCommandName().getName().equals(name)) {
                    logger.debug("getCommandSubset : name equals, returning");
                    retval.add(cmd);
                }
            }
        }
        logger.debug("getCommandSubset : returning: {}", retval);
        return Collections.unmodifiableSet(retval);
    }


    /**
     * @return Returns the repos.
     */
    public Set<KickstartCommand> getRepos() {
        return getCommandSubset("repo");
    }

    /**
     * Updates the repos commands associated to this ks data.
     * @param repoCommands the repos to update
     */
    public void setRepos(Collection<KickstartCommand> repoCommands) {
        replaceSet(getRepos(), repoCommands);
    }

    /**
     * @return Returns the repos.
     */
    public Set<RepoInfo> getRepoInfos() {
        Set<KickstartCommand> repoCommands =  getRepos();
        Set<RepoInfo> info = new HashSet<>();
        for (KickstartCommand cmd : repoCommands) {
            info.add(RepoInfo.parse(cmd));
        }
        return info;
    }

    /**
     * Updates the repos commands associated to this ks data.
     * @param repos the repos to update
     **/
    public void setRepoInfos(Collection<RepoInfo> repos) {
        Set<KickstartCommand> repoCommands = new HashSet<>();
        for (RepoInfo repo : repos) {
            KickstartCommand cmd = KickstartFactory.createKickstartCommand(this, "repo");
            repo.setArgumentsIn(cmd);
            repoCommands.add(cmd);
        }
        setRepos(repoCommands);
    }


    /**
     * @return Returns the customOptions.
     */
    public Set<KickstartCommand> getCustomOptions() {
        return new LinkedHashSet<>(getCommandSubset("custom"));
    }

    /**
     * remove old custom options and replace with new
     * @param customIn to replace old with.
     */
    public void setCustomOptions(Collection<KickstartCommand> customIn) {
        replaceSet(this.getCustomOptions(), customIn);
    }


    /**
     * remove old options and replace with new
     * @param optionsIn to replace old with.
     */
    public void setOptions(Collection<KickstartCommand> optionsIn) {
        replaceSet(this.getOptions(), optionsIn);
    }

    private void replaceSet(Collection<KickstartCommand> oldSet,
            Collection<KickstartCommand> newSet) {
        logger.debug("replaceSet co.pre: {}", this.getCustomOptions());
        this.commands.removeAll(oldSet);
        logger.debug("replaceSet co.post: {}", this.getCustomOptions());
        this.commands.addAll(newSet);
        logger.debug("replaceSet co.done: {}", this.getCustomOptions());
    }

    /**
     * Getter for command options
     * @return Returns Kickstartcommand options
     */
    public Set<KickstartCommand> getOptions() {
        // 'partitions', 'raids', 'logvols', 'volgroups', 'include', 'repo', 'custom'
        logger.debug("returning all commands except: {}", ADANCED_OPTIONS);
        Set<KickstartCommand> retval = new HashSet<>();
        if (this.commands != null && !this.commands.isEmpty()) {
            for (KickstartCommand cmd : this.commands) {
                logger.debug("working with: {}", cmd.getCommandName().getName());
                if (!ADANCED_OPTIONS.contains(cmd.getCommandName().getName())) {
                    logger.debug("not contained within filtered list. adding to retval");
                    retval.add(cmd);
                }
            }
        }
        return Collections.unmodifiableSet(retval);
    }

    /**
     * @return the download url suffix
     */
    public String getUrl() {
        for (KickstartCommand c : getOptions()) {
            String a = c.getArguments();
            if (c.getCommandName().getName().equals("url") && a != null) {
                Matcher match = URL_REGEX.matcher(a);
                if (match.find()) {
                    return match.group(1);
                }
            }
        }
        return "";
    }

    /**
     *
     * @param kd KickstartDefaults to set
     */
    public void setKickstartDefaults(KickstartDefaults kd) {
        this.kickstartDefaults = kd;
    }

    /**
     *
     * @return the Kickstart Defaults assoc w/this Kickstart
     */
    public KickstartDefaults getKickstartDefaults() {
        return this.kickstartDefaults;
    }

    /**
     * Conv method
     * @return Install Type for Kickstart
     */
    public KickstartInstallType getInstallType() {
        if (this.getTree() != null) {
            return getTree().getInstallType();
        }
        return null;
    }

    /**
     * @return if this kickstart profile is rhel  installer type
     */
    public boolean isRhel() {
        if (getInstallType() != null) {
            return getInstallType().isRhel();
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 8 installer type
     */
    public boolean isRhel8() {
        if (getInstallType() != null) {
            return getInstallType().isRhel8();
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 7 installer type
     */
    public boolean isRhel7() {
        if (getInstallType() != null) {
            return getInstallType().isRhel7();
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 6 installer type
     */
    public boolean isRhel6() {
        if (getInstallType() != null) {
            return getInstallType().isRhel6();
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 7 installer type or greater (for rhel8)
     */
    public boolean isRhel7OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isRhel7OrGreater() || getInstallType().isFedora());
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 9 installer type or greater (for rhel8)
     */
    public boolean isRhel9OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isRhel9OrGreater() || getInstallType().isFedora());
        }
        return false;
    }

    /**
     * @return if this kickstart profile is rhel 6 installer type or greater (for rhel7)
     */
    public boolean isRhel6OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isRhel6OrGreater() ||
                    getInstallType().isFedora());
        }
        return false;
    }

    /**
     * returns true if this is a fedora kickstart
     * @return if this is a fedora kickstart or not
     */
    public boolean isFedora() {
        if (getInstallType() != null) {
            return getInstallType().isFedora();
        }
        return false;
    }

    /**
     * returns true if this is a generic kickstart
     * as in non rhel and non fedora.
     * @return if this is a generic kickstart or not
     */
    public boolean isGeneric() {
        if (getInstallType() != null) {
            return getInstallType().isGeneric();
        }
        return false;
    }

    /**
     * @return if this auto installation profile is SLES 11 installer type or greater
     *  (for SLES 12)
     */
    public boolean isSLES11OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isSLES11OrGreater());
        }
        return false;
    }

    /**
     * @return if this auto installation profile is SLES 12 installer type or greater
     *  (for SLES 15)
     */
    public boolean isSLES12OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isSLES12OrGreater());
        }
        return false;
    }

    /**
     * @return if this auto installation profile is SLES 15 installer type or greater
     *  (for SLES 15+)
     */
    public boolean isSLES15OrGreater() {
        if (getInstallType() != null) {
            return (getInstallType().isSLES15OrGreater());
        }
        return false;
    }

    /**
     * returns true if this is a SUSE autoinstallation
     * @return if this is a SUSE autoinstallation or not
     */
    public boolean isSUSE() {
        if (getInstallType() != null) {
            return getInstallType().isSUSE();
        }
        return false;
    }

    /**
     *
     * @return Set of IpRanges for Kickstart
     */
    public Set<KickstartIpRange> getIps() {
        return ips;
    }

    /**
     *
     * @param ipsIn Set of IPRanges to set
     */
    public void setIps(Set<KickstartIpRange> ipsIn) {
        this.ips = ipsIn;
    }

    /**
     *
     * @param ipIn KickstartIpRange to add
     */
    public void addIpRange(KickstartIpRange ipIn) {
        ips.add(ipIn);
    }

    /**
     * Convenience method to get the KickstartableTree object
     * @return KickstartableTree object associated with this KSData.
     */
    public KickstartableTree getTree() {
        if (this.getKickstartDefaults() != null) {
            return this.getKickstartDefaults().getKstree();
        }
        return null;
    }

    /**
     * Setter for KickstartableTree object
     * @param kstreeIn the KickstartableTree to set
     */
    public void setTree(KickstartableTree kstreeIn) {
        this.getKickstartDefaults().setKstree(kstreeIn);
    }

    /**
     * @return the scripts
     */
    public Set<KickstartScript> getScripts() {
        return scripts;
    }


    /**
     * @param scriptsIn The scripts to set.
     */
    public void setScripts(Set<KickstartScript> scriptsIn) {
        this.scripts = scriptsIn;
    }

    /**
     * Add a KickstartScript to the KickstartData
     * @param ksIn to add
     */
    public void addScript(KickstartScript ksIn) {
        // The ordering goes: Pre scripts and post (chroot) scripts have
        // positive positions, post nochroot scripts have negative
        // positions where the most negative script is run right before
        // Red Hat's scripts. The user can adjust these default positions
        // later, but this allows us to preserve previous behavor where
        // adding a nochroot post script added it right before Red Hat's
        // post scripts and adding a post (chroot) script added it as the
        // last script that runs.
        if (ksIn.getScriptType().equals(KickstartScript.TYPE_POST) &&
                !ksIn.thisScriptIsChroot()) {
            Long minPosition = 0L;
            for (KickstartScript script : scripts) {
                if (script.getPosition() < minPosition) {
                    minPosition = script.getPosition();
                }
            }
            ksIn.setPosition(minPosition - 1);
        }
        else {
            Long maxPosition = 0L;
            for (KickstartScript script : scripts) {
                if (script.getPosition() > maxPosition) {
                    maxPosition = script.getPosition();
                }
            }
            ksIn.setPosition(maxPosition + 1);
        }
        ksIn.setKsdata(this);

        scripts.add(ksIn);
    }

    /**
     * Remove a KickstartScript from this Profile.
     * @param ksIn to remove.
     */
    public void removeScript(KickstartScript ksIn) {
        scripts.remove(ksIn);
    }


    /**
     * Is ELILO required for this kickstart profile?
     * We base this off of the channel arch, because IA64 systems
     * require elilo
     * @return boolean - required, or not
     */
    public boolean getEliloRequired() {
        return this.getKickstartDefaults().getKstree().getChannel()
                .getChannelArch().getLabel().equals("channel-ia64");
    }

    /**
     * Get the bootloader type
     *
     * @return String: lilo or grub
     */
    public String getBootloaderType() {
        KickstartCommand bootloaderCommand = this.getCommand("bootloader");

        if (bootloaderCommand == null || bootloaderCommand.getArguments() == null) {
            return "grub";
        }

        if (bootloaderCommand.getArguments().contains("--useLilo")) {
            return "lilo";
        }
        return "grub";
    }

    /**
     * Changes the bootloader
     * @param type either "grub" or "lilo"
     * @return true if changed, false otherwise
     */
    public boolean changeBootloaderType(String type) {
        boolean retval = false;
        KickstartCommand bootloaderCommand = this.getCommand("bootloader");
        if (bootloaderCommand != null) {
            retval =  true;
            bootloaderCommand.setArguments(
                    bootloaderCommand.getArguments().replace(
                            "--useLilo", "").trim());
            if (type.equalsIgnoreCase("lilo")) {
                bootloaderCommand.setArguments(bootloaderCommand.getArguments() +
                        " --useLilo");
            }
        }

        return retval;
    }

    /**
     * Convenience method to get the Channel associated with this profile
     * {@literal KickstartData -> KickstartDefault -> KickstartTree -> Channel}
     * @return Channel object associated with this KickstartData
     */
    public Channel getChannel() {
        if (this.kickstartDefaults != null && this.kickstartDefaults.getKstree() != null) {
            return this.kickstartDefaults.getKstree().getChannel();
        }
        return null;
    }

    /**
     * Get the timezone - just the timezone, not the --utc or other args
     *
     * @return String: The timezone (like "Asia/Qatar")
     */
    public String getTimezone() {
        KickstartCommand tzCommand = this.getCommand("timezone");

        if (tzCommand == null || tzCommand.getArguments() == null) {
            return "";
        }

        List<String> tokens = StringUtil.stringToList(tzCommand.getArguments());

        for (String token : tokens) {
            if (!token.startsWith("--")) {
                return token;
            }
        }

        return null;
    }

    /**
     * Will the system hardware clock use UTC
     *
     * @return Boolean Are we using UTC?
     */
    public Boolean isUsingUtc() {
        KickstartCommand tzCommand = this.getCommand("timezone");

        if (tzCommand == null || tzCommand.getArguments() == null) {
            return Boolean.FALSE;
        }

        List<String> tokens = StringUtil.stringToList(tzCommand.getArguments());

        for (String token : tokens) {
            if (token.equals("--utc")) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Copy this KickstartData into a new one.  NOTE:  We don't clone
     * the following sub-objects:
     *
     * KickstartIpRange
     *
     * NOTE: We also don't clone isOrgDefault.
     *
     * @param user who is doing the cloning
     * @param newLabel to set on the cloned object
     * @return KickstartData that is cloned.
     */
    public KickstartData deepCopy(User user, String newLabel) {
        KickstartData cloned = new KickstartData();
        updateCloneDetails(cloned, newLabel);
        return cloned;
    }

    protected void updateCloneDetails(KickstartData cloned, String newLabel) {

        cloned.setLabel(newLabel);
        cloned.setActive(this.getActive());
        cloned.setPostLog(this.getPostLog());
        cloned.setPreLog(this.getPreLog());
        cloned.setKsCfg(this.getKsCfg());
        cloned.setComments(this.getComments());
        cloned.setNonChrootPost(this.getNonChrootPost());
        cloned.setVerboseUp2date(this.getVerboseUp2date());
        cloned.setOrg(this.getOrg());
        cloned.setChildChannels(new HashSet<>(this.getChildChannels()));
        cloned.setPartitionData(getPartitionData());
        copyKickstartCommands(getCommands(), cloned);

        // Gotta remember to create a new HashSet with
        // the other objects.  Otherwise hibernate will
        // complain that you are using the same collection
        // in two objects.
        if (this.getCryptoKeys() != null) {
            cloned.setCryptoKeys(new HashSet<>(this.getCryptoKeys()));
        }

        // NOTE: Make sure we *DONT* clone isOrgDefault
        cloned.setIsOrgDefault(Boolean.FALSE);
        cloned.setKernelParams(this.getKernelParams());
        if (this.getKickstartDefaults() != null) {
            cloned.setKickstartDefaults(this.getKickstartDefaults().deepCopy(cloned));
        }
        cloned.setOrg(this.getOrg());
        if (this.getKsPackages() != null) {
            this.getKsPackages().forEach(kp -> cloned.getKsPackages().add(kp.deepCopy(cloned)));
        }

        if (this.getPreserveFileLists() != null) {
            cloned.setPreserveFileLists(new HashSet<>(this.getPreserveFileLists()));
        }

        if (this.getScripts() != null) {
            for (KickstartScript kss : this.getScripts()) {
                KickstartScript ksscloned = kss.deepCopy(cloned);
                cloned.getScripts().add(ksscloned);
            }
        }

        //copy all of the non-session related kickstarts
        Set<Token> newTokens = new HashSet<>();
        if (this.getDefaultRegTokens() != null) {
            for (Token tok : this.getDefaultRegTokens()) {
                ActivationKey key = ActivationKeyFactory.lookupByToken(tok);
                if (key == null || key.getKickstartSession() == null) {
                    newTokens.add(tok);
                }
            }
        }
        cloned.setDefaultRegTokens(newTokens);
        cloned.setNoBase(this.getNoBase());
        cloned.setIgnoreMissing(this.getIgnoreMissing());

    }

    // Helper method to copy KickstartCommands
    private static void copyKickstartCommands(Collection<KickstartCommand> commands,
            KickstartData cloned) {
        if (commands != null) {
            for (KickstartCommand cmd : commands) {
                KickstartCommand clonedCmd = cmd.deepCopy(cloned);
                cloned.addCommand(clonedCmd);
            }
        }
    }

    /**
     * Add a kickstartCommand object
     * @param clonedCmd The kickstartCommand to add
     */
    public void addCommand(KickstartCommand clonedCmd) {
        commands.add(clonedCmd);
    }

    /**
     * Get the list of possible name of the kickstart packages this KS could use.
     * @return List of kickstart packages like auto-kickstart-ks-rhel-i386-as-4
     */
    public List<String> getKickstartPackageNames() {
        return ConfigDefaults.get().getKickstartPackageNames();

    }

    /**
     * @return Returns if the post scripts should be logged.
     */
    public boolean getPostLog() {
        return postLog;
    }

    /**
     * @return Returns if the pre scripts should be logged.
     */
    public boolean getPreLog() {
        return preLog;
    }

    /**
     * @return Returns if we should copy ks.cfg and %include'd fragments to /root
     */
    public boolean getKsCfg() {
        return ksCfg;
    }

    /**
     * @param postLogIn The postLog to set.
     */
    public void setPostLog(Boolean postLogIn) {
        this.postLog = postLogIn;
    }

    /**
     * @param preLogIn The preLog to set.
     */
    public void setPreLog(Boolean preLogIn) {
        this.preLog = preLogIn;
    }

    /**
     * @param ksCfgIn The ksCfg to set.
     */
    public void setKsCfg(Boolean ksCfgIn) {
        this.ksCfg = ksCfgIn;
    }

    /**
     * Returns the SE Linux mode associated to this kickstart profile
     * @return the se linux mode or the default SE Liunx mode (i.e. enforcing)..
     */
    public SELinuxMode getSELinuxMode() {
        KickstartCommand cmd = getCommand(SELINUX_MODE_COMMAND);
        if (cmd != null) {
            String args = cmd.getArguments();
            if (!StringUtils.isBlank(args)) {
                if (args.endsWith(SELinuxMode.PERMISSIVE.getValue())) {
                    return SELinuxMode.PERMISSIVE;
                }
                else if (args.endsWith(SELinuxMode.ENFORCING.getValue())) {
                    return SELinuxMode.ENFORCING;
                }
                else if (args.endsWith(SELinuxMode.DISABLED.getValue())) {
                    return SELinuxMode.DISABLED;
                }
            }
        }
        return SELinuxMode.ENFORCING;
    }

    /**
     * True if config management is enabled in this profile..
     * @return True if config management is enabled in this profile..
     */
    public boolean isConfigManageable() {
        return getKickstartDefaults() != null &&
                getKickstartDefaults().getCfgManagementFlag();
    }

    /**
     * True if remote command flag is  enabled in this profile..
     * @return True if remote command flag is  enabled in this profile..
     */
    public boolean isRemoteCommandable() {
        return getKickstartDefaults() != null &&
                getKickstartDefaults().getRemoteCommandFlag();
    }

    /**
     * @return the cobblerName
     */
    public String getCobblerFileName() {
        if (getCobblerId() != null) {
            Profile prof = Profile.lookupById(
                    CobblerXMLRPCHelper.getConnection(
                            ConfigDefaults.get().getCobblerAutomatedUser()),
                            getCobblerId());
            if (prof != null && !StringUtils.isBlank(prof.getKickstart())) {
                return prof.getKickstart();
            }
        }

        return null;
    }

    /**
     * Build std kickstart cfg template path
     * @return ks cfg template path
     */
    public String buildCobblerFileName() {
        if (isRawData()) {
            return CobblerCommand.makeCobblerFileName(RAW_DIR + "/" + getLabel(), getOrg());
        }
        return CobblerCommand.makeCobblerFileName(WIZARD_DIR + "/" + getLabel(), getOrg());
    }

    /**
     * @return Returns if up2date/yum should be verbose
     */
    public boolean getVerboseUp2date() {
        return this.verboseUp2date;
    }

    /**
     * @param verboseup2dateIn The verboseup2date to set.
     */
    public void setVerboseUp2date(Boolean verboseup2dateIn) {
        this.verboseUp2date = verboseup2dateIn;
    }

    /**
     * @return Returns if nonchroot post script is to be logged
     */
    public boolean getNonChrootPost() {
        return this.nonChrootPost;
    }


    /**
     * @param nonchrootpostIn The nonchrootpost to set.
     */
    public void setNonChrootPost(Boolean nonchrootpostIn) {
        this.nonChrootPost = nonchrootpostIn;
    }

    /**
     * Returns true if this is a
     * raw mode data .
     * @return true or false.
     */
    public boolean isRawData() {
        return false;
    }

    /**
     * Return the string containing the kickstart file
     * @param host the kickstart host
     * @param session the kickstart session,
     *               can be null if the data
     *               is not part of a session
     * @return String containing kickstart file
     */
    public String getFileData(String host,
            KickstartSession session) {
        KickstartFormatter formatter = new KickstartFormatter(host, this, session);
        return formatter.getFileData();
    }


    /**
     * @return Returns the cobblerId.
     */
    public String getCobblerId() {
        return cobblerId;
    }


    /**
     * @param cobblerIdIn The cobblerId to set.
     */
    public void setCobblerId(String cobblerIdIn) {
        this.cobblerId = cobblerIdIn;
    }


    /**
     * @return the kickstartType
     */
    protected String getKickstartType() {
        return kickstartType;
    }


    /**
     * @param kickstartTypeIn the kickstartType to set
     */
    protected void setKickstartType(String kickstartTypeIn) {
        this.kickstartType = kickstartTypeIn;
    }

    /**
     * @return Returns true if the base package group should be left off
     */
    public boolean getNoBase() {
        return noBase;
    }

    /**
     * @param noBaseIn the noBase to set
     */
    public void setNoBase(Boolean noBaseIn) {
        this.noBase = noBaseIn;
    }

    /**
     * @return Returns true if we should ignore missing packages
     */
    public boolean getIgnoreMissing() {
        return ignoreMissing;
    }

    /**
     * @param ignoreMissingIn the ignoreMissing to set
     */
    public void setIgnoreMissing(Boolean ignoreMissingIn) {
        this.ignoreMissing = ignoreMissingIn;
    }

    /**
     * Get the default virt bridge for this KickstartData object.
     *
     * @return String virt bridge (xenbr0, virbr0)
     */
    public String getDefaultVirtBridge() {
        if (this.getKickstartDefaults().getVirtualizationType().getLabel()
                .equals(KickstartVirtualizationType.KVM_FULLYVIRT)) {
            return ConfigDefaults.get().getDefaultKVMVirtBridge();
        }
        return ConfigDefaults.get().getDefaultXenVirtBridge();
    }

    /**
     * Returns the cobbler object associated to
     * to this profile.
     * @param user the user object needed for connection,
     *              enter null if you want to use the
     *              automated connection as provided by
     *              taskomatic.
     * @return the Profile associated to this ks data
     */

    public Profile getCobblerObject(User user) {
        if (StringUtils.isBlank(getCobblerId())) {
            return null;
        }
        CobblerConnection con;
        if (user == null) {
            con = CobblerXMLRPCHelper.getAutomatedConnection();
        }
        else {
            con = CobblerXMLRPCHelper.getConnection(user);
        }
        return Profile.lookupById(con, getCobblerId());
    }

    /**
     * Gets the Registration Type (i.e. the code that determines if the ks script needs to generate a reactivation key
     * or not)
     *
     * @param user the user object needed to load the profile from cobbler
     * @return the registration type
     */
    public RegistrationType getRegistrationType(User user) {
        Profile prof = getCobblerObject(null);
        if (prof == null) {
            return RegistrationType.getDefault();
        }

        Optional<Map<String, Object>> ksMeta = prof.getKsMeta();
        if (ksMeta.isEmpty()) {
            return RegistrationType.getDefault();
        }
        return RegistrationType.find((String)ksMeta.get().get(
                RegistrationType.COBBLER_VAR));
    }

    /**
     * Sets the registration type
     * @param type the registration type
     * @param user the user needed to load the profile form cobbler
     */
    public void setRegistrationType(RegistrationType type, User user) {
        Profile prof = getCobblerObject(user);
        Map<String, Object> meta = prof.getKsMeta().orElse(new HashMap<>());
        meta.put(RegistrationType.COBBLER_VAR, type.getType());
        prof.setKsMeta(Optional.of(meta));
        prof.save();
    }

    /**
     * Method to determine if the profile
     * is valid or if it needs to be corrected.
     * @return true if the profile is synced to cobbler
     * and the distro it hosts is valid.
     */
    public boolean isValid() {
        return !StringUtils.isBlank(getCobblerId()) && getTree().isValid();
    }


    /**
     * @return Returns the partitionData.
     */
    protected byte[] getPartitionDataBinary() {
        return partitionData;
    }


    /**
     * @param partitionDataIn The partitionData to set.
     */
    protected void setPartitionDataBinary(byte[] partitionDataIn) {
        partitionData = partitionDataIn;
    }

    /**
     * Get the partition data as string
     * @return partition data as string
     */
    public String getPartitionData() {
        return HibernateFactory.getByteArrayContents(getPartitionDataBinary());
    }

    /**
     * Set the partition data
     * @param data the partition info
     */
    public void setPartitionData(String data) {
        setPartitionDataBinary(HibernateFactory.stringToByteArray(data));
    }

    /**
     * get the update type
     * @return the update type
     */
    public String getUpdateType() {
        return this.updateType;
    }

    /**
     * get the update type
     * @return the update type
     */
    public KickstartTreeUpdateType getRealUpdateType() {
        return KickstartTreeUpdateType.find(this.updateType);
    }

    /**
     * Set the update type
     * Hibernate wigs out if this is called "setUpdateType", which is should be
     * @param updateTypeIn the update type to set
     */
    public void setUpdateType(String updateTypeIn) {
        this.updateType = updateTypeIn;
    }

    /**
     * Set the update type
     * Hibernate wigs out if this is called "setUpdateType", which is should be
     * @param updateTypeIn the update type to set
     */
    public void setRealUpdateType(KickstartTreeUpdateType updateTypeIn) {
        this.updateType = updateTypeIn.getType();
    }

    /**
     * Encrypt the password with whichever algorithm is appropriate for this ksdata
     * @param password the password to encrypt
     * @return the encrypted password
     */
    public String encryptPassword(String password) {
        return SHA256Crypt.crypt(password);
    }

    /**
     * Check if 'salt' should be used in registration process, for this install type
     * @return `true` if install type label is defined in `salt_enabled_kickstart_install_types` property in rhn.conf
     *
     */
    public boolean isUserSelectedSaltInstallType() {
        return ConfigDefaults.get().getUserSelectedSaltInstallTypeLabels().contains(getInstallType().getLabel());
    }
}
