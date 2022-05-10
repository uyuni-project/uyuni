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

package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.SELinuxMode;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.FileListNotFoundException;
import com.redhat.rhn.frontend.xmlrpc.InvalidLocaleCodeException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchKickstartException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.XmlRpcKickstartHelper;
import com.redhat.rhn.manager.kickstart.KickstartCryptoKeyCommand;
import com.redhat.rhn.manager.kickstart.KickstartEditCommand;
import com.redhat.rhn.manager.kickstart.KickstartLocaleCommand;
import com.redhat.rhn.manager.kickstart.KickstartPartitionCommand;
import com.redhat.rhn.manager.kickstart.SystemDetailsCommand;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* SystemDetailsHandler
* @apidoc.namespace kickstart.profile.system
* @apidoc.doc Provides methods to set various properties of a kickstart profile.
*/
public class SystemDetailsHandler extends BaseHandler {

    /**
      * Check the configuration management status for a kickstart profile
      * so that a system created using this profile will be configuration capable.
      * @param loggedInUser The current user
      * @param ksLabel the ks profile label
      * @return returns true if configuration management is enabled; otherwise, false
      *
      * @apidoc.doc Check the configuration management status for a kickstart profile.
      * @apidoc.param #session_key()
      * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
      * @apidoc.returntype #param("boolean", "true if configuration management is enabled; otherwise, false")
      */
    public boolean checkConfigManagement(User loggedInUser, String ksLabel) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        return command.getKickstartData().getKickstartDefaults().getCfgManagementFlag();
    }

    /**
     * Enables the configuration management flag in a kickstart profile
     * so that a system created using this profile will be configuration capable.
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @return 1 on success
     *
     *
     * @apidoc.doc Enables the configuration management flag in a kickstart profile
     * so that a system created using this profile will be configuration capable.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype #return_int_success()
     */
    public int enableConfigManagement(User loggedInUser, String ksLabel) {
        return setConfigFlag(loggedInUser, ksLabel, true);
    }

    /**
     * Disables the configuration management flag in a kickstart profile
     * so that a system created using this profile will be NOT be configuration capable.
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @return 1 on success
     *
     * @apidoc.doc Disables the configuration management flag in a kickstart profile
     * so that a system created using this profile will be NOT be configuration capable.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype #return_int_success()
     */
    public int disableConfigManagement(User loggedInUser, String ksLabel) {
        return setConfigFlag(loggedInUser, ksLabel, false);
    }

    private int setConfigFlag(User loggedInUser, String ksLabel, boolean flag) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        command.enableConfigManagement(flag);
        command.store();
        return 1;
    }

    /**
    * Check the remote commands status flag for a kickstart profile
    * so that a system created using this profile
    * will be capable of running remote commands
    * @param loggedInUser The current user
    * @param ksLabel the ks profile label
    * @return returns true if remote command support is enabled; otherwise, false
    *
    * @apidoc.doc Check the remote commands status flag for a kickstart profile.
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
    * @apidoc.returntype #param("boolean", "true if remote commands support is enabled; otherwise, false")
    */
    public boolean checkRemoteCommands(User loggedInUser, String ksLabel) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        return command.getKickstartData().getKickstartDefaults().getRemoteCommandFlag();
    }

    /**
     * Enables the remote command flag in a kickstart profile
     * so that a system created using this profile
     * will be capable of running remote commands
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @return 1 on success
     *
     * @apidoc.doc Enables the remote command flag in a kickstart profile
     * so that a system created using this profile
     * will be capable of running remote commands
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype #return_int_success()
     */
    public int enableRemoteCommands(User loggedInUser, String ksLabel) {
        return setRemoteCommandsFlag(loggedInUser, ksLabel, true);
    }

    /**
     * Disables the remote command flag in a kickstart profile
     * so that a system created using this profile
     * will be capable of running remote commands
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @return 1 on success
     *
     * @apidoc.doc Disables the remote command flag in a kickstart profile
     * so that a system created using this profile
     * will be capable of running remote commands
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype #return_int_success()

     */
    public int disableRemoteCommands(User loggedInUser, String ksLabel) {
        return setRemoteCommandsFlag(loggedInUser, ksLabel, false);
    }

    private int setRemoteCommandsFlag(User loggedInUser, String ksLabel, boolean flag) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        command.enableRemoteCommands(flag);
        command.store();
        return 1;
    }

    /**
     * Retrieves the SELinux enforcing mode property of a kickstart
     * profile.
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @return the enforcing mode
     *
     * @apidoc.doc Retrieves the SELinux enforcing mode property of a kickstart
     * profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     * #param("string", "enforcing mode")
     *      #options()
     *          #item ("enforcing")
     *          #item ("permissive")
     *          #item ("disabled")
     *      #options_end()
     */
    @ReadOnly
    public String getSELinux(User loggedInUser, String ksLabel) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        return command.getKickstartData().getSELinuxMode().getValue();
    }

    /**
     * Sets the SELinux enforcing mode property of a kickstart profile
     * so that a system created using this profile will be have
     * the appropriate SELinux enforcing mode.
     * @param loggedInUser The current user
     * @param ksLabel the ks profile label
     * @param enforcingMode the SELinux enforcing mode.
     * @return 1 on success
     *
     * @apidoc.doc Sets the SELinux enforcing mode property of a kickstart profile
     * so that a system created using this profile will be have
     * the appropriate SELinux enforcing mode.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #param_desc("string", "enforcingMode", "the SELinux enforcing mode")
     *      #options()
     *          #item ("enforcing")
     *          #item ("permissive")
     *          #item ("disabled")
     *      #options_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setSELinux(User loggedInUser, String ksLabel, String enforcingMode) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, loggedInUser);
        command.setMode(SELinuxMode.lookup(enforcingMode));
        return setRemoteCommandsFlag(loggedInUser, ksLabel, true);
    }

    /**
     * Retrieves the locale for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel The kickstart profile label
     * @return Returns a map containing the local and useUtc.
     * @throws FaultException A FaultException is thrown if:
     *   - The profile associated with ksLabel cannot be found
     *
     * @apidoc.doc Retrieves the locale for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     *          #struct_begin("locale info")
     *              #prop("string", "locale")
     *              #prop("boolean", "useUtc")
     *                  #options()
     *                      #item_desc ("true", "the hardware clock uses UTC")
     *                      #item_desc ("false", "the hardware clock does not use UTC")
     *                  #options_end()
     *          #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getLocale(User loggedInUser, String ksLabel)
            throws FaultException {

        ensureConfigAdmin(loggedInUser);

        KickstartLocaleCommand command  = getLocaleCommand(ksLabel, loggedInUser);

        Map<String, Object> locale = new HashMap<>();
        locale.put("locale", command.getTimezone());
        locale.put("useUtc", command.isUsingUtc());

        return locale;
    }

    /**
     * Sets the locale for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel The kickstart profile label
     * @param locale The locale
     * @param useUtc true if the hardware clock uses UTC
     * @return 1 on success, exception thrown otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The profile associated with ksLabel cannot be found
     *   - The locale provided is invalid
     *
     * @apidoc.doc Sets the locale for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #param_desc("string", "locale", "the locale")
     * @apidoc.param #param("boolean", "useUtc")
     *      #options()
     *          #item_desc ("true", "the hardware clock uses UTC")
     *          #item_desc ("false", "the hardware clock does not use UTC")
     *      #options_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setLocale(User loggedInUser, String ksLabel, String locale,
            Boolean useUtc) throws FaultException {

        ensureConfigAdmin(loggedInUser);

        KickstartLocaleCommand command  = getLocaleCommand(ksLabel, loggedInUser);

        if (command.isValidTimezone(locale) == Boolean.FALSE) {
            throw new InvalidLocaleCodeException(locale);
        }

        command.setTimezone(locale);
        if (useUtc) {
            command.useUtc();
        }
        else {
            command.doNotUseUtc();
        }
        command.store();
        return 1;
    }

    /**
     * Set the partitioning scheme for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label.
     * @param scheme The partitioning scheme.
     * @return 1 on success
     * @throws FaultException fault exception
     * @apidoc.doc Set the partitioning scheme for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of the kickstart profile to update")
     * @apidoc.param #array_single_desc("string", "scheme", "the partitioning scheme
     * is a list of partitioning command strings used to setup the partitions,
     * volume groups and logical volumes.")
     * @apidoc.returntype #return_int_success()
     */
    public int setPartitioningScheme(User loggedInUser, String ksLabel,
            List<String> scheme) {
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        Long ksid = ksdata.getId();
        KickstartPartitionCommand command = new KickstartPartitionCommand(ksid,
                loggedInUser);
        StringBuilder sb = new StringBuilder();
        for (String s : scheme) {
            sb.append(s);
            sb.append('\n');
        }
        ValidatorError err = command.setPartitionData(sb.toString());
        if (err != null) {
            throw new FaultException(-4, "PartitioningSchemeInvalid", err
                    .toString());
        }
        command.store();
        return 1;
    }

    /**
     * Get the partitioning scheme for a kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel A kickstart profile label
     * @return The profile's partitioning scheme. This is a list of commands
     * used to setup the partitions, logical volumes and volume groups.
     * @throws FaultException fault exception
     * @apidoc.doc Get the partitioning scheme for a kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the label of a kickstart profile")
     * @apidoc.returntype #array_single("string", "a list of partitioning commands used to
     * setup the partitions, logical volumes and volume groups")
     */
    @ReadOnly
    public List<String> getPartitioningScheme(User loggedInUser, String ksLabel) {
        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());
        List<String> list = new LinkedList<>();
        for (String str : ksdata.getPartitionData().split("\\r?\\n")) {
            if (!StringUtils.isBlank(str)) {
                list.add(str);
            }
        }
        return list;
    }


    private KickstartData lookupKsData(String label, Org org) {
        return XmlRpcKickstartHelper.getInstance().lookupKsData(label, org);
    }


    private KickstartLocaleCommand getLocaleCommand(String label, User user) {
        XmlRpcKickstartHelper helper = XmlRpcKickstartHelper.getInstance();
        return new KickstartLocaleCommand(helper.lookupKsData(label, user), user);
    }

    private SystemDetailsCommand getSystemDetailsCommand(String label, User user) {
        XmlRpcKickstartHelper helper = XmlRpcKickstartHelper.getInstance();
        return new SystemDetailsCommand(helper.lookupKsData(label, user), user);
    }

    /**
     * Returns the set of all keys associated with the indicated kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @return set of all keys associated with the given profile
     *
     * @apidoc.doc Returns the set of all keys associated with the given kickstart
     *             profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("key")
     *              #prop("string", "description")
     *              #prop("string", "type")
     *              #prop("string", "content")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Set<CryptoKey> listKeys(User loggedInUser, String ksLabel) {

        // TODO: Determine if null or empty set is returned when no keys associated

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        // Set will contain crypto key
        return data.getCryptoKeys();
    }

    /**
     * Adds the given list of keys to the specified kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @param descriptions   list identifiying the keys to add
     * @return 1 if the associations were performed correctly
     *
     * @apidoc.doc Adds the given list of keys to the specified kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #array_single_desc("string", "descriptions", "the list identifying the keys to add")
     * @apidoc.returntype #return_int_success()
     */
    public int addKeys(User loggedInUser, String ksLabel, List<String> descriptions) {

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (descriptions == null) {
            throw new IllegalArgumentException("descriptions cannot be null");
        }

        // Load the kickstart profile
        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        if (data == null) {
            throw new NoSuchKickstartException(ksLabel);
        }

        // Associate the keys
        KickstartCryptoKeyCommand command =
            new KickstartCryptoKeyCommand(data.getId(), loggedInUser);

        command.addKeysByDescriptionAndOrg(descriptions, org);
        command.store();

        return 1;
    }

    /**
     * Removes the given list of keys from the specified kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @param descriptions   list identifiying the keys to remove
     * @return 1 if the associations were performed correctly
     *
     * @apidoc.doc Removes the given list of keys from the specified kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #array_single_desc("string", "descriptions", "the list identifying the keys to remove")
     * @apidoc.returntype #return_int_success()
     */
    public int removeKeys(User loggedInUser, String ksLabel, List<String> descriptions) {

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (descriptions == null) {
            throw new IllegalArgumentException("descriptions cannot be null");
        }

        // Load the kickstart profile
        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        KickstartCryptoKeyCommand command =
            new KickstartCryptoKeyCommand(data.getId(), loggedInUser);

        command.removeKeysByDescriptionAndOrg(descriptions, org);
        command.store();

        return 1;
    }

    /**
     * Returns the set of all file preservations associated with the given kickstart
     * profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     * @return set of all file preservations associated with the given profile
     *
     * @apidoc.doc Returns the set of all file preservations associated with the given
     * kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     *     #return_array_begin()
     *         $FileListSerializer
     *     #array_end()
     */
    @ReadOnly
    public Set<FileList> listFilePreservations(User loggedInUser, String ksLabel)
        throws FaultException {

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        return data.getPreserveFileLists();
    }

    /**
     * Adds the given list of file preservations to the specified kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @param filePreservations   list identifying the file preservations to add
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     *   - One of the filePreservations is invalid
     * @return 1 if the associations were performed correctly
     *
     * @apidoc.doc Adds the given list of file preservations to the specified kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param
     *     #array_single_desc("string", "filePreservations", "the list identifying the file preservations to add")
     * @apidoc.returntype #return_int_success()
     */
    public int addFilePreservations(User loggedInUser, String ksLabel,
                             List<String> filePreservations) throws FaultException {

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (filePreservations == null) {
            throw new IllegalArgumentException("filePreservations cannot be null");
        }

        // Load the kickstart profile
        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        // Add the file preservations
        KickstartEditCommand command =
            new KickstartEditCommand(data.getId(), loggedInUser);

        Set<FileList> fileLists = new HashSet<>();
        for (String name : filePreservations) {
            FileList fileList = CommonFactory.lookupFileList(name, loggedInUser.getOrg());
            if (fileList == null) {
                throw new FileListNotFoundException(name);
            }
            fileLists.add(fileList);
        }
        // Cycle through the list of file list objects retrieved and add
        // them to the profile.  We do this on a second pass because, we
        // don't want to remove anything if there was an error that would have
        // resulted in an exception being thrown.
        for (FileList fileList : fileLists) {
            command.getKickstartData().addPreserveFileList(fileList);
        }
        command.store();
        return 1;
    }

    /**
     * Removes the given list of file preservations from the specified kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @param filePreservations   list identifying the file preservations to remove
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     *   - One of the filePreservations is invalid
     * @return 1 if the associations were performed correctly
     *
     * @apidoc.doc Removes the given list of file preservations from the specified
     * kickstart profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param
     *     #array_single_desc("string", "filePreservations", "the list identifying the file preservations to add")
     * @apidoc.returntype #return_int_success()
     */
    public int removeFilePreservations(User loggedInUser, String ksLabel, List<String> filePreservations)
            throws FaultException {

        if (ksLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (filePreservations == null) {
            throw new IllegalArgumentException("filePreservations cannot be null");
        }

        // Load the kickstart profile
        Org org = loggedInUser.getOrg();

        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                org.getId());

        // Associate the file preservations
        KickstartEditCommand command =
            new KickstartEditCommand(data.getId(), loggedInUser);

        Set<FileList> fileLists = new HashSet<>();
        for (String name : filePreservations) {
            FileList fileList = CommonFactory.lookupFileList(name, loggedInUser.getOrg());
            if (fileList == null) {
                throw new FileListNotFoundException(name);
            }
            fileLists.add(fileList);
        }
        // Cycle through the list of file list objects retrieved and remove
        // them from the profile.  We do this on a second pass because, we
        // don't want to remove anything if there was an error that would have
        // resulted in an exception being thrown.
        for (FileList fileList : fileLists) {
            command.getKickstartData().removePreserveFileList(fileList);
        }
        command.store();
        return 1;
    }


    /**
     * Sets the registration type of a given kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @param registrationType   registration type
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     *   - registration type is not reactivation/deletion/none
     * @return 1 if the associations were performed correctly
     *
     * @apidoc.doc Sets the registration type of a given kickstart profile.
     * Registration Type can be one of reactivation/deletion/none
     * These types determine the behaviour of the re registration when using
     * this profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #param("string", "registrationType")
     *      #options()
     *         #item_desc ("reactivation", "to try and generate a reactivation key
     *              and use that to register the system when reprovisioning a system.")
     *         #item_desc ("deletion", "to try and delete the existing system profile
     *              and reregister the system being reprovisioned as new")
     *         #item_desc ("none", "to preserve the status quo and leave the current system
     *              as a duplicate on a reprovision.")
     *      #options_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setRegistrationType(User loggedInUser, String ksLabel, String registrationType) {
        ensureConfigAdmin(loggedInUser);
        SystemDetailsCommand command = getSystemDetailsCommand(ksLabel,
                loggedInUser);
        command.setRegistrationType(registrationType);
        command.store();
        return 1;
    }


    /**
     * Returns the registration type of a given kickstart profile.
     *
     * @param loggedInUser The current user
     * @param ksLabel identifies the profile; cannot be &lt;code&gt;null&lt;/code&gt;
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     * @return the registration type -&gt; one of reactivation/deletion/none
     *
     * @apidoc.doc returns the registration type of a given kickstart profile.
     * Registration Type can be one of reactivation/deletion/none
     * These types determine the behaviour of the registration when using
     * this profile for reprovisioning.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     * #param("string", "the registration type")
     *      #options()
     *         #item ("reactivation")
     *         #item ("deletion")
     *         #item ("none")
     *      #options_end()
     */
    public String  getRegistrationType(User loggedInUser, String ksLabel) {
        ensureConfigAdmin(loggedInUser);
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(ksLabel,
                    loggedInUser.getOrg().getId());
        return data.getRegistrationType(loggedInUser).getType();
    }
}
