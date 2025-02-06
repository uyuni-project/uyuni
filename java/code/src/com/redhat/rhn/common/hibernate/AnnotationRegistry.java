/*
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.common.hibernate;

import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.AppStreamApi;
import com.redhat.rhn.domain.channel.AppStreamApiKey;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelSyncFlag;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.PaygCredentialsProduct;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.common.ProvisionState;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.ErrataFilter;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.PtfFilter;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.credentials.BaseCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.RHUICredentials;
import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.image.DeltaImageInfo;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgAdminManagement;
import com.redhat.rhn.domain.org.OrgConfig;
import com.redhat.rhn.domain.org.TemplateString;
import com.redhat.rhn.domain.org.usergroup.UserGroupImpl;
import com.redhat.rhn.domain.org.usergroup.UserGroupMembers;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.RecurringInternalState;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageBreaks;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageConflicts;
import com.redhat.rhn.domain.rhnpackage.PackageEnhances;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.domain.rhnpackage.PackageFile;
import com.redhat.rhn.domain.rhnpackage.PackageObsoletes;
import com.redhat.rhn.domain.rhnpackage.PackagePreDepends;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageRecommends;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.PackageSuggests;
import com.redhat.rhn.domain.rhnpackage.PackageSupplements;
import com.redhat.rhn.domain.role.RoleImpl;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryNoAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.Capability;
import com.redhat.rhn.domain.server.ClientCapability;
import com.redhat.rhn.domain.server.ClientCapabilityId;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.SAPWorkload;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerAppStream;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.ServerPathId;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenChannelAppStream;
import com.redhat.rhn.domain.user.AddressImpl;
import com.redhat.rhn.domain.user.StateChange;
import com.redhat.rhn.domain.user.legacy.PersonalInfo;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.domain.user.legacy.UserInfo;
import com.redhat.rhn.manager.system.ServerGroupManager;

import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionResult;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoEnvironmentTypeConverter;
import com.suse.manager.model.attestation.CoCoResultTypeConverter;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import java.util.List;


/**
 * Stores a list of hibernate annotation classes for registration the first time the
 * ConnectionManager.
 */
public class AnnotationRegistry {

    private AnnotationRegistry() {
        // Hide the default constructor.
    }

    private static final List<Class<?>> ANNOTATION_CLASSES = List.of(
            // do not add class at the endi, but keep the alphabetical order
            AccessToken.class,
            AddressImpl.class,
            AnsiblePath.class,
            AppStreamApi.class,
            AppStreamApiKey.class,
            AppStream.class,
            BaseCredentials.class,
            Capability.class,
            ChannelArch.class,
            Channel.class,
            ChannelSyncFlag.class,
            ChannelTemplate.class,
            ClientCapability.class,
            ClientCapabilityId.class,
            ClonedChannel.class,
            CloudRMTCredentials.class,
            CloudRmtHost.class,
            CoCoAttestationResult.class,
            CoCoEnvironmentTypeConverter.class,
            CoCoResultTypeConverter.class,
            ConfigChannel.class,
            ConfigChannelType.class,
            ConfigFile.class,
            ContentEnvironment.class,
            ContentFilter.class,
            ContentProject.class,
            ContentProjectFilter.class,
            ContentProjectHistoryEntry.class,
            CryptoKey.class,
            CryptoKeyType.class,
            CustomDataValue.class,
            DeltaImageInfo.class,
            DockerfileProfile.class,
            EntitlementServerGroup.class,
            EnvironmentTarget.class,
            ErrataFilter.class,
            GroupRecurringAction.class,
            ImageFile.class,
            ImageInfo.class,
            ImageInfoCustomDataValue.class,
            ImageOverview.class,
            ImagePackage.class,
            ImageProfile.class,
            ImageRepoDigest.class,
            ImageStore.class,
            ImageStoreType.class,
            InstalledPackage.class,
            InternalState.class,
            InventoryPath.class,
            KiwiProfile.class,
            MaintenanceCalendar.class,
            MaintenanceSchedule.class,
            ManagedServerGroup.class,
            MinionRecurringAction.class,
            MinionServer.class,
            MinionServerFactory.class,
            MinionSummary.class,
            ModuleFilter.class,
            NetworkInterface.class,
            NotificationMessage.class,
            OrgAdminManagement.class,
            Org.class,
            OrgConfig.class,
            OrgRecurringAction.class,
            PackageArch.class,
            PackageBreaks.class,
            PackageCapability.class,
            PackageConflicts.class,
            PackageEnhances.class,
            PackageExtraTagsKeys.class,
            PackageFile.class,
            PackageFilter.class,
            PackageObsoletes.class,
            PackagePreDepends.class,
            PackageProvides.class,
            PackageRecommends.class,
            PackageRequires.class,
            PackageSuggests.class,
            PackageSupplements.class,
            PaygCredentialsProduct.class,
            PaygDimensionComputation.class,
            PaygDimensionResult.class,
            PaygSshData.class,
            PersonalInfo.class,
            Pillar.class,
            PlaybookPath.class,
            ProfileCustomDataValue.class,
            ProjectSource.class,
            ProvisionState.class,
            PtfFilter.class,
            RecurringConfigChannel.class,
            RecurringHighstate.class,
            RecurringInternalState.class,
            RecurringState.class,
            RegistryCredentials.class,
            ReportDBCredentials.class,
            RHUICredentials.class,
            RoleImpl.class,
            SAPWorkload.class,
            SCCCredentials.class,
            SCCOrderItem.class,
            SCCRegCacheItem.class,
            SCCRepositoryAuth.class,
            SCCRepositoryBasicAuth.class,
            SCCRepository.class,
            SCCRepositoryCloudRmtAuth.class,
            SCCRepositoryNoAuth.class,
            SCCRepositoryTokenAuth.class,
            SCCSubscription.class,
            ServerAppStream.class,
            Server.class,
            ServerCoCoAttestationConfig.class,
            ServerCoCoAttestationReport.class,
            ServerGroup.class,
            ServerGroupManager.class,
            ServerGroupType.class,
            ServerPath.class,
            ServerPathId.class,
            SoftwareEnvironmentTarget.class,
            SoftwareProjectSource.class,
            SslCryptoKey.class,
            StateChange.class,
            Task.class,
            TemplateString.class,
            TokenChannelAppStream.class,
            Token.class,
            UserGroupImpl.class,
            UserGroupMembers.class,
            UserImpl.class,
            UserInfo.class,
            UserNotification.class,
            VHMCredentials.class,
            VirtualHostManagerNodeInfo.class
    );

    /**
     * Returns the list of all available hibernate annotation classes.
     * @return List of hibernate annotation classes.
     */
    public static List<Class<?>> getAnnotationClasses() {
        return ANNOTATION_CLASSES;
    }
}
