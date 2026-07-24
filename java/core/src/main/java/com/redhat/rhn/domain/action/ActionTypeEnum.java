/*
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.action.ansible.InventoryAction;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.appstream.AppStreamAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.config.ConfigDeployAction;
import com.redhat.rhn.domain.action.config.ConfigDiffAction;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.config.ConfigUploadMtimeAction;
import com.redhat.rhn.domain.action.config.ConfigVerifyAction;
import com.redhat.rhn.domain.action.config.DaemonConfigAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.image.DeployImageAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartHostToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartScheduleSyncAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAutoUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageDeltaAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageLockAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRunTransactionAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageVerifyAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataAction;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum ActionTypeEnum {
    // Keep the alphabetical order
    TYPE_APPLY_STATES("states.apply", ApplyStatesAction.class),
    TYPE_APPSTREAM_CONFIGURE("appstreams.configure", AppStreamAction.class),
    TYPE_CLIENTCERT_UPDATE_CLIENT_CERT("clientcert.update_client_cert", CertificateUpdateAction.class),
    TYPE_COCO_ATTESTATION("coco.attestation", CoCoAttestationAction.class),
    TYPE_CONFIGFILES_DEPLOY("configfiles.deploy", ConfigDeployAction.class),
    TYPE_CONFIGFILES_DIFF("configfiles.diff", ConfigDiffAction.class),
    TYPE_CONFIGFILES_MTIME_UPLOAD("configfiles.mtime_upload", ConfigUploadMtimeAction.class),
    TYPE_CONFIGFILES_UPLOAD("configfiles.upload", ConfigUploadAction.class),
    TYPE_CONFIGFILES_VERIFY("configfiles.verify", ConfigVerifyAction.class),
    TYPE_DAEMON_CONFIG("rhnsd.configure", DaemonConfigAction.class),
    TYPE_DEPLOY_IMAGE("image.deploy", DeployImageAction.class),
    TYPE_DIST_UPGRADE("distupgrade.upgrade", DistUpgradeAction.class),
    TYPE_ERRATA("errata.update", ErrataAction.class),
    TYPE_HARDWARE_REFRESH_LIST("hardware.refresh_list", HardwareRefreshAction.class),
    TYPE_IMAGE_BUILD("image.build", ImageBuildAction.class),
    TYPE_IMAGE_INSPECT("image.inspect", ImageInspectAction.class),
    TYPE_INVENTORY("ansible.inventory", InventoryAction.class),
    TYPE_KICKSTART_INITIATE("kickstart.initiate", KickstartInitiateAction.class),
    TYPE_KICKSTART_INITIATE_GUEST("kickstart_guest.initiate", KickstartInitiateGuestAction.class),
    TYPE_KICKSTART_SCHEDULE_SYNC("kickstart.schedule_sync", KickstartScheduleSyncAction.class),
    TYPE_PACKAGES_AUTOUPDATE("packages.autoupdate", PackageAutoUpdateAction.class),
    TYPE_PACKAGES_DELTA("packages.delta", PackageDeltaAction.class),
    TYPE_PACKAGES_LOCK("packages.setLocks", PackageLockAction.class),
    TYPE_PACKAGES_REFRESH_LIST("packages.refresh_list", PackageRefreshListAction.class),
    TYPE_PACKAGES_REMOVE("packages.remove", PackageRemoveAction.class),
    TYPE_PACKAGES_RUNTRANSACTION("packages.runTransaction", PackageRunTransactionAction.class),
    TYPE_PACKAGES_UPDATE("packages.update", PackageUpdateAction.class),
    TYPE_PACKAGES_VERIFY("packages.verify", PackageVerifyAction.class),
    TYPE_PLAYBOOK("ansible.playbook", PlaybookAction.class),
    TYPE_REBOOT("reboot.reboot", RebootAction.class),
    TYPE_RHN_APPLET_USE_SATELLITE("rhn_applet.use_satellite", AppletUseSatelliteAction.class),
    TYPE_ROLLBACK_CONFIG("rollback.config", RollbackConfigAction.class),
    TYPE_ROLLBACK_LISTTRANSACTIONS("rollback.listTransactions", RollbackListTransactionsAction.class),
    TYPE_ROLLBACK_ROLLBACK("rollback.rollback", RollbackAction.class),
    TYPE_SCAP_XCCDF_EVAL("scap.xccdf_eval", ScapAction.class),
    TYPE_SCRIPT_RUN("script.run", ScriptRunAction.class),
    TYPE_SUBSCRIBE_CHANNELS("channels.subscribe", SubscribeChannelsAction.class),
    TYPE_SUPPORTDATA_GET("supportdata.get", SupportDataAction.class),
    TYPE_UP2DATE_CONFIG_GET("up2date_config.get", Up2DateConfigGetAction.class),
    TYPE_UP2DATE_CONFIG_UPDATE("up2date_config.update", Up2DateConfigUpdateAction.class),
    TYPE_VIRTIZATION_HOST_SUBSCRIBE_TO_TOOLS_CHANNEL("kickstart_host.add_tools_channel",
            KickstartHostToolsChannelSubscriptionAction.class),
    TYPE_VIRTUALIZATION_GUEST_SUBSCRIBE_TO_TOOLS_CHANNEL("kickstart_guest.add_tools_channel",
            KickstartGuestToolsChannelSubscriptionAction.class),
    TYPE_VIRT_PROFILE_REFRESH("virt.refresh_list", VirtualInstanceRefreshAction.class);


    private final String label;
    private final Class<? extends Action> actionClass;

    ActionTypeEnum(String labelIn, Class<? extends Action> clazzIn) {
        this.label = labelIn;
        this.actionClass = clazzIn;
    }

    /**
     * Returns the label of this action type
     * @return the label of this action type.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Create a new Action from scratch related to the enum.
     * @return the created Action
     * @throws ReflectiveOperationException if something goes wrong
     */
    public Action createAction() throws ReflectiveOperationException  {
        return actionClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Checks if an ActionType type compares with this enum value
     * @param actionTypeIn
     * @return true if equal, false otherwise
     */
    public boolean equalsType(ActionType actionTypeIn) {
        return (ActionTypeEnum.of(actionTypeIn).orElse(null) == this);
    }

    /**
     * Returns a name string from an Action type
     * @return a name
     */
    public String getPackageActionName() {
        return switch (this) {
            case TYPE_PACKAGES_REMOVE -> "Package Removal";
            case TYPE_PACKAGES_UPDATE -> "Package Install/Upgrade";
            case TYPE_PACKAGES_VERIFY -> "Package Verify";
            case TYPE_PACKAGES_REFRESH_LIST -> "Package List Refresh";
            case TYPE_PACKAGES_DELTA -> "Package Synchronization";
            case TYPE_PACKAGES_LOCK -> "Lock packages";
            default -> "";
        };
    }

    /**
     * Retrieves an action type enum by its label.
     *
     * @param labelIn the label to search
     * @return the ActionTypeEnum matching the given label, if present
     */
    public static Optional<ActionTypeEnum> of(String labelIn) {
        return Arrays.stream(ActionTypeEnum.values())
                .filter(type -> Objects.equals(labelIn, type.getLabel()))
                .findFirst();
    }

    /**
     * Retrieves an action type enum given an action type
     *
     * @param actionTypeIn the label to search
     * @return the ActionTypeEnum matching the given label, if present
     */
    public static Optional<ActionTypeEnum> of(ActionType actionTypeIn) {
        return Optional.ofNullable(actionTypeIn)
                .map(ActionType::getLabel)
                .flatMap(ActionTypeEnum::of);
    }
}
