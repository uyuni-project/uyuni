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
package com.redhat.rhn.frontend.xmlrpc.kickstart.tree;


import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartableTreeDetail;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.kickstart.tree.TreeCreateOperation;
import com.redhat.rhn.manager.kickstart.tree.TreeDeleteOperation;
import com.redhat.rhn.manager.kickstart.tree.TreeEditOperation;

import com.suse.manager.api.ReadOnly;

import java.util.List;

/**
 * KickstartTreeHandler - methods related to CRUD operations
 * on KickstartableTree objects.
 * @apidoc.namespace kickstart.tree
 * @apidoc.doc Provides methods to access and modify the kickstart trees.
 */
public class KickstartTreeHandler extends BaseHandler {

    /**
     * Returns details of kickstartable tree specified by the label
     * @param loggedInUser The current user
     * @param treeLabel Label of kickstartable tree to search.
     * @return found KickstartableTreeObject
     *
     * @apidoc.doc The detailed information about a kickstartable tree given the tree name.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel", "Label of kickstartable tree to
     * search.")
     * @apidoc.returntype $KickstartTreeDetailSerializer
     */
    @ReadOnly
    public KickstartableTreeDetail getDetails(User loggedInUser, String treeLabel) {
        ensureConfigAdmin(loggedInUser);

        KickstartableTree tree = KickstartFactory.lookupKickstartTreeByLabel(treeLabel);
        if (tree == null) {
            throw new InvalidChannelLabelException();
        }

        return new KickstartableTreeDetail(tree);
    }

    /**
     * List the available kickstartable trees for the given channel.
     * @param loggedInUser The current user
     * @param channelLabel Label of channel to search.
     * @return Array of KickstartableTreeObjects
     *
     * @apidoc.doc List the available kickstartable trees for the given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "Label of channel to
     * search.")
     * @apidoc.returntype #return_array_begin() $KickstartTreeSerializer #array_end()
     */
    public List list(User loggedInUser,
            String channelLabel) {
        ensureConfigAdmin(loggedInUser);

        return KickstartFactory.lookupKickstartableTrees(getChannel(channelLabel, loggedInUser).getId(),
                loggedInUser.getOrg());
    }

    /**
     * List the available kickstartable tree types (rhel2,3,4,5 and fedora9+)
     * @param loggedInUser The current user
     * @return Array of KickstartInstallType objects
     *
     * @apidoc.doc List the available kickstartable install types (rhel2,3,4,5 and
     * fedora9+).
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $KickstartInstallTypeSerializer #array_end()
     */
    @ReadOnly
    public List listInstallTypes(User loggedInUser) {
        return KickstartFactory.lookupKickstartInstallTypes();
    }

    /**
     * Create a Kickstart Tree (Distribution).
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the new kickstart tree
     * @param basePath path to the base/root of the kickstart tree.
     * @param channelLabel label of channel to associate with ks tree.
     * @param installType String label for KickstartInstallType (rhel_6,
     * rhel_7, rhel_8, rhel_9, fedora_9)
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Create a Kickstart Tree (Distribution) in #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "The new kickstart tree label.")
     * @apidoc.param #param_desc("string", "basePath", "Path to the base or
     * root of the kickstart tree.")
     * @apidoc.param #param_desc("string", "channelLabel", "Label of channel to
     * associate with the kickstart tree. ")
     * @apidoc.param #param_desc("string", "installType", "Label for
     * KickstartInstallType (rhel_6, rhel_7, rhel_8, rhel_9, fedora_9).")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String treeLabel, String basePath, String channelLabel, String installType) {

        return create(loggedInUser, treeLabel, basePath, channelLabel, installType, "", "");
    }

    /**
     * Create a Kickstart Tree (Distribution).
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the new kickstart tree
     * @param basePath path to the base/root of the kickstart tree.
     * @param channelLabel label of channel to associate with ks tree.
     * @param installType String label for KickstartInstallType (rhel_2.1,
     * rhel_3, rhel_4, rhel_5, fedora_9)
     * @param kernelOptions options to be passed to the kernel when booting for the installation
     * @param postKernelOptions options to be passed to the kernel after installation
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Create a Kickstart Tree (Distribution) in #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "The new kickstart tree label.")
     * @apidoc.param #param_desc("string", "basePath", "Path to the base or
     * root of the kickstart tree.")
     * @apidoc.param #param_desc("string", "channelLabel", "Label of channel to
     * associate with the kickstart tree. ")
     * @apidoc.param #param_desc("string", "installType", "Label for
     * KickstartInstallType (rhel_2.1, rhel_3, rhel_4, rhel_5, fedora_9).")
     * @apidoc.param #param_desc("string", "kernelOptions", "Options to be passed to the kernel
     * when booting for the installation. ")
     * @apidoc.param #param_desc("string", "postKernelOptions", "Options to be passed to the kernel
     * when booting for the installation. ")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String treeLabel,
            String basePath, String channelLabel,
            String installType, String kernelOptions, String postKernelOptions) {

        ensureConfigAdmin(loggedInUser);

        TreeCreateOperation create = new TreeCreateOperation(loggedInUser);
        create.setBasePath(basePath);
        create.setChannel(getChannel(channelLabel, loggedInUser));
        create.setInstallType(getInstallType(installType));
        create.setLabel(treeLabel);
        create.setServerName(ConfigDefaults.get().getJavaHostname());
        create.setKernelOptions(kernelOptions);
        create.setKernelOptionsPost(postKernelOptions);

        ValidatorError ve = create.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey(), ve.getValues());
        }
        return 1;
    }

    /**
     * Delete a Kickstart Tree (Distribution).
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the new kickstart tree
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Delete a Kickstart Tree (Distribution) from #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "Label for the
     * kickstart tree to delete.")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String treeLabel) {

        ensureConfigAdmin(loggedInUser);

        TreeDeleteOperation op = new TreeDeleteOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    /**
     * Delete a kickstarttree and any profiles associated with this kickstart tree.
     * WARNING:  This will delete all profiles associated with this kickstart tree!
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the new kickstart tree
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Delete a kickstarttree and any profiles associated with
     * this kickstart tree.  WARNING:  This will delete all profiles
     * associated with this kickstart tree!
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "Label for the
     * kickstart tree to delete.")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteTreeAndProfiles(User loggedInUser, String treeLabel) {

        ensureConfigAdmin(loggedInUser);

        TreeDeleteOperation op = new TreeDeleteOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        op.setDeleteProfiles(Boolean.TRUE);
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    /**
     * Edit a kickstarttree.  This method will not edit the label of the tree, see
     * renameTree().
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the existing kickstart tree
     * @param basePath New basepath for tree.
     * rhn-kickstart.
     * @param channelLabel New channel label to lookup and assign to
     * the kickstart tree.
     * @param installType String label for KickstartInstallType (rhel_6,
     * rhel_7, rhel_8, rhel_9, fedora_9)
     *
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Edit a Kickstart Tree (Distribution) in #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "Label for the kickstart tree.")
     * @apidoc.param #param_desc("string", "basePath", "Path to the base or
     * root of the kickstart tree.")
     * @apidoc.param #param_desc("string", "channelLabel", "Label of channel to
     * associate with kickstart tree.")
     * @apidoc.param #param_desc("string", "installType", "Label for
     * KickstartInstallType (rhel_6, rhel_7, rhel_8, rhel_9, fedora_9).")
     *
     * @apidoc.returntype #return_int_success()
     */
    public int update(User loggedInUser, String treeLabel, String basePath, String channelLabel, String installType) {

     return update(loggedInUser, treeLabel, basePath, channelLabel, installType, "", "");
    }

    /**
     * Edit a kickstarttree.  This method will not edit the label of the tree, see
     * renameTree().
     *
     * @param loggedInUser The current user
     * @param treeLabel Label for the existing kickstart tree
     * @param basePath New basepath for tree.
     * rhn-kickstart.
     * @param channelLabel New channel label to lookup and assign to
     * the kickstart tree.
     * @param installType String label for KickstartInstallType (rhel_2.1,
     * rhel_3, rhel_4, rhel_5, fedora_9)
     * @param kernelOptions Options to be passed to the kernel when booting for the installation
     * @param postKernelOptions Options to be passed to the kernel after installation
     *
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Edit a Kickstart Tree (Distribution) in #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "treeLabel" "Label for the kickstart tree.")
     * @apidoc.param #param_desc("string", "basePath", "Path to the base or
     * root of the kickstart tree.")
     * @apidoc.param #param_desc("string", "channelLabel", "Label of channel to
     * associate with kickstart tree.")
     * @apidoc.param #param_desc("string", "installType", "Label for
     * KickstartInstallType (rhel_2.1, rhel_3, rhel_4, rhel_5, fedora_9).")
     * @apidoc.param #param_desc("string", "kernelOptions", "Options to be passed to the kernel
     * when booting for the installation. ")
     * @apidoc.param #param_desc("string", "postKernelOptions", "Options to be passed to the kernel
     * when booting for the installation. ")
     *
     * @apidoc.returntype #return_int_success()
     */
    public int update(User loggedInUser, String treeLabel, String basePath,
                      String channelLabel, String installType, String kernelOptions, String postKernelOptions) {

        ensureConfigAdmin(loggedInUser);

        TreeEditOperation op = new TreeEditOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        op.setBasePath(basePath);
        op.setChannel(getChannel(channelLabel, loggedInUser));
        op.setInstallType(getInstallType(installType));
        op.setKernelOptions(kernelOptions);
        op.setKernelOptionsPost(postKernelOptions);

        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    /**
     * Rename a kickstart tree.
     *
     * @param loggedInUser The current user
     * @param originalLabel Label for tree we want to edit
     * @param newLabel to assign to tree.
     * @return 1 if successful, exception otherwise.
     *
     * @apidoc.doc Rename a Kickstart Tree (Distribution) in #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "originalLabel" "Label for the
     * kickstart tree to rename.")
     * @apidoc.param #param_desc("string", "newLabel" "The kickstart tree's new label.")
     * @apidoc.returntype #return_int_success()
     */
    public int rename(User loggedInUser, String originalLabel, String newLabel) {

        ensureConfigAdmin(loggedInUser);

        TreeEditOperation op = new TreeEditOperation(originalLabel, loggedInUser);

        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        op.setLabel(newLabel);
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    private Channel getChannel(String label, User user) {
        Channel channel = ChannelManager.lookupByLabelAndUser(label,
                user);
        if (channel == null) {
            throw new InvalidChannelLabelException();
        }
        return channel;
    }

    private KickstartInstallType getInstallType(String installType) {
        KickstartInstallType type =
            KickstartFactory.lookupKickstartInstallTypeByLabel(installType);
        if (type == null) {
            throw new NoSuchKickstartInstallTypeException(installType);
        }
        return type;

    }

}
