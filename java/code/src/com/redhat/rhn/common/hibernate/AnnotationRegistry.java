/*
 * Copyright (c) 2018--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.hibernate;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.WebEndpoint;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionArchType;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.AppletUseSatelliteAction;
import com.redhat.rhn.domain.action.CertificateUpdateAction;
import com.redhat.rhn.domain.action.CoCoAttestationAction;
import com.redhat.rhn.domain.action.HardwareRefreshAction;
import com.redhat.rhn.domain.action.RebootAction;
import com.redhat.rhn.domain.action.RollbackAction;
import com.redhat.rhn.domain.action.RollbackConfigAction;
import com.redhat.rhn.domain.action.RollbackListTransactionsAction;
import com.redhat.rhn.domain.action.Up2DateConfigGetAction;
import com.redhat.rhn.domain.action.Up2DateConfigUpdateAction;
import com.redhat.rhn.domain.action.VirtualInstanceRefreshAction;
import com.redhat.rhn.domain.action.ansible.InventoryAction;
import com.redhat.rhn.domain.action.ansible.InventoryActionDetails;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.ansible.PlaybookActionDetails;
import com.redhat.rhn.domain.action.appstream.AppStreamAction;
import com.redhat.rhn.domain.action.appstream.AppStreamActionDetails;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ActivationScheduleDeployAction;
import com.redhat.rhn.domain.action.config.ActivationSchedulePackageDeployAction;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigChannelAssociation;
import com.redhat.rhn.domain.action.config.ConfigDateDetails;
import com.redhat.rhn.domain.action.config.ConfigDateFileAction;
import com.redhat.rhn.domain.action.config.ConfigDeployAction;
import com.redhat.rhn.domain.action.config.ConfigDiffAction;
import com.redhat.rhn.domain.action.config.ConfigFileNameAssociation;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.config.ConfigUploadMtimeAction;
import com.redhat.rhn.domain.action.config.ConfigVerifyAction;
import com.redhat.rhn.domain.action.config.DaemonConfigAction;
import com.redhat.rhn.domain.action.config.DaemonConfigDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.errata.ActionPackageDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.image.DeployImageAction;
import com.redhat.rhn.domain.action.image.DeployImageActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartHostToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartScheduleSyncAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionRemovalFailure;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionResult;
import com.redhat.rhn.domain.action.rhnpackage.PackageAutoUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageDeltaAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageLockAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRunTransactionAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageVerifyAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionResult;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionResult;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataActionDetails;
import com.redhat.rhn.domain.audit.XccdfBenchmark;
import com.redhat.rhn.domain.audit.XccdfIdent;
import com.redhat.rhn.domain.audit.XccdfIdentSystem;
import com.redhat.rhn.domain.audit.XccdfProfile;
import com.redhat.rhn.domain.audit.XccdfRuleResult;
import com.redhat.rhn.domain.audit.XccdfRuleResultType;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.AppStreamApi;
import com.redhat.rhn.domain.channel.AppStreamApiKey;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ChannelSyncFlag;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.Comps;
import com.redhat.rhn.domain.channel.ContentSourceFilter;
import com.redhat.rhn.domain.channel.ContentSourceType;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.channel.MediaProducts;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.PublicChannelFamily;
import com.redhat.rhn.domain.channel.ReleaseChannelMap;
import com.redhat.rhn.domain.channel.RepoMetadata;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.PaygCredentialsProduct;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.common.ArchType;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.common.ExceptionMessage;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.common.ProvisionState;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.TinyUrl;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigFileState;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironmentDiff;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.ErrataFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.PtfFilter;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.credentials.BaseCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.RHUICredentials;
import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.ErrataFileType;
import com.redhat.rhn.domain.errata.Keyword;
import com.redhat.rhn.domain.errata.Severity;
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
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.iss.IssMasterOrg;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartDefaultRegToken;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.kickstart.KickstartPackage;
import com.redhat.rhn.domain.kickstart.KickstartPreserveFileList;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.domain.kickstart.KickstartSessionHistory;
import com.redhat.rhn.domain.kickstart.KickstartSessionState;
import com.redhat.rhn.domain.kickstart.KickstartTreeType;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.domain.kickstart.crypto.GpgCryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgAdminManagement;
import com.redhat.rhn.domain.org.OrgConfig;
import com.redhat.rhn.domain.org.SystemMigration;
import com.redhat.rhn.domain.org.TemplateCategory;
import com.redhat.rhn.domain.org.TemplateString;
import com.redhat.rhn.domain.org.usergroup.UserGroupImpl;
import com.redhat.rhn.domain.org.usergroup.UserGroupMembers;
import com.redhat.rhn.domain.org.usergroup.UserGroupMembersId;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.RecurringInternalState;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageBreaks;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageConflicts;
import com.redhat.rhn.domain.rhnpackage.PackageDelta;
import com.redhat.rhn.domain.rhnpackage.PackageEnhances;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.domain.rhnpackage.PackageFile;
import com.redhat.rhn.domain.rhnpackage.PackageGroup;
import com.redhat.rhn.domain.rhnpackage.PackageKey;
import com.redhat.rhn.domain.rhnpackage.PackageKeyType;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.rhnpackage.PackageObsoletes;
import com.redhat.rhn.domain.rhnpackage.PackagePreDepends;
import com.redhat.rhn.domain.rhnpackage.PackageProvider;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageRecommends;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.rhnpackage.PackageSuggests;
import com.redhat.rhn.domain.rhnpackage.PackageSupplements;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileEntry;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileType;
import com.redhat.rhn.domain.role.RoleImpl;
import com.redhat.rhn.domain.rpm.SourceRpm;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryNoAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.Capability;
import com.redhat.rhn.domain.server.ClientCapability;
import com.redhat.rhn.domain.server.ClientCapabilityId;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.Feature;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.InvalidSnapshotReason;
import com.redhat.rhn.domain.server.Location;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.PushClient;
import com.redhat.rhn.domain.server.PushClientState;
import com.redhat.rhn.domain.server.Ram;
import com.redhat.rhn.domain.server.SAPWorkload;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerAppStream;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.ServerLock;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.ServerPathId;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.ServerSnapshotTagLink;
import com.redhat.rhn.domain.server.ServerUuid;
import com.redhat.rhn.domain.server.SnapshotTag;
import com.redhat.rhn.domain.server.SnapshotTagName;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;
import com.redhat.rhn.domain.session.WebSessionImpl;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.RegTokenOrgDefault;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenChannelAppStream;
import com.redhat.rhn.domain.token.TokenPackage;
import com.redhat.rhn.domain.user.AddressImpl;
import com.redhat.rhn.domain.user.Pane;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.State;
import com.redhat.rhn.domain.user.StateChange;
import com.redhat.rhn.domain.user.UserServerPreference;
import com.redhat.rhn.domain.user.legacy.PersonalInfo;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.domain.user.legacy.UserInfo;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTask;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;

import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionResult;
import com.suse.manager.errata.model.errata.ErrataAdvisoryMap;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoEnvironmentTypeConverter;
import com.suse.manager.model.attestation.CoCoResultTypeConverter;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.scc.proxy.SCCProxyRecord;

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
            // do not add class at the end, but keep the alphabetical order
            AccessGroup.class,
            AccessToken.class,
            Action.class,
            ActionArchType.class,
            ActionChain.class,
            ActionChainEntry.class,
            ActionPackageDetails.class,
            ActionStatus.class,
            ActionType.class,
            ActivationKey.class,
            ActivationScheduleDeployAction.class,
            ActivationSchedulePackageDeployAction.class,
            AddressImpl.class,
            AnsiblePath.class,
            AppletUseSatelliteAction.class,
            AppStreamAction.class,
            AppStreamActionDetails.class,
            AppStreamApi.class,
            AppStreamApiKey.class,
            AppStream.class,
            ApplyStatesAction.class,
            ApplyStatesActionDetails.class,
            ApplyStatesActionResult.class,
            ArchType.class,
            BaseCredentials.class,
            Bug.class,
            CPU.class,
            CPUArch.class,
            Capability.class,
            ChannelArch.class,
            Channel.class,
            ChannelProduct.class,
            ChannelSyncFlag.class,
            ChannelTemplate.class,
            Checksum.class,
            ChecksumType.class,
            CertificateUpdateAction.class,
            ClientCapability.class,
            ClientCapabilityId.class,
            ClonedChannel.class,
            CloudRMTCredentials.class,
            CloudRmtHost.class,
            CoCoAttestationAction.class,
            CoCoAttestationResult.class,
            CoCoEnvironmentTypeConverter.class,
            CoCoResultTypeConverter.class,
            Comps.class,
            ConfigAction.class,
            ConfigChannel.class,
            ConfigChannelAssociation.class,
            ConfigChannelType.class,
            ConfigContent.class,
            ConfigDateDetails.class,
            ConfigDateFileAction.class,
            ConfigDeployAction.class,
            ConfigDiffAction.class,
            ConfigFile.class,
            ConfigFileName.class,
            ConfigFileNameAssociation.class,
            ConfigFileState.class,
            ConfigFileType.class,
            ConfigInfo.class,
            ConfigRevisionAction.class,
            ConfigRevisionActionResult.class,
            ConfigUploadAction.class,
            ConfigUploadMtimeAction.class,
            ConfigVerifyAction.class,
            ContentEnvironment.class,
            ContentEnvironmentDiff.class,
            ContentFilter.class,
            ContentProject.class,
            ContentProjectFilter.class,
            ContentProjectHistoryEntry.class,
            ContentSourceFilter.class,
            ContentSourceType.class,
            CryptoKey.class,
            CryptoKeyType.class,
            CustomDataKey.class,
            CustomDataValue.class,
            Cve.class,
            DaemonConfigAction.class,
            DaemonConfigDetails.class,
            DeltaImageInfo.class,
            DeployImageAction.class,
            DeployImageActionDetails.class,
            Device.class,
            DistChannelMap.class,
            DistUpgradeAction.class,
            DistUpgradeActionDetails.class,
            DistUpgradeChannelTask.class,
            Dmi.class,
            DockerfileProfile.class,
            EntitlementServerGroup.class,
            EnvironmentTarget.class,
            ErrataAction.class,
            ErrataAdvisoryMap.class,
            ErrataFileType.class,
            ErrataFilter.class,
            ExceptionMessage.class,
            Feature.class,
            FileList.class,
            FilterCriteria.class,
            GroupRecurringAction.class,
            GpgCryptoKey.class,
            HardwareRefreshAction.class,
            HubSCCCredentials.class,
            ImageBuildAction.class,
            ImageBuildActionDetails.class,
            ImageBuildActionResult.class,
            ImageFile.class,
            ImageInfo.class,
            ImageInfoCustomDataValue.class,
            ImageInspectAction.class,
            ImageInspectActionDetails.class,
            ImageInspectActionResult.class,
            ImageInspectActionResult.ImageInspectActionResultId.class,
            ImageOverview.class,
            ImagePackage.class,
            ImageProfile.class,
            ImageRepoDigest.class,
            ImageStore.class,
            ImageStoreType.class,
            InstalledPackage.class,
            InstalledProduct.class,
            InternalState.class,
            InvalidSnapshotReason.class,
            InventoryAction.class,
            InventoryActionDetails.class,
            InventoryPath.class,
            IssAccessToken.class,
            IssHub.class,
            IssMaster.class,
            IssMasterOrg.class,
            IssPeripheral.class,
            IssPeripheralChannels.class,
            IssSlave.class,
            Keyword.class,
            KickstartCommand.class,
            KickstartAction.class,
            KickstartActionDetails.class,
            KickstartCommandName.class,
            KickstartDefaultRegToken.class,
            KickstartGuestAction.class,
            KickstartGuestActionDetails.class,
            KickstartGuestToolsChannelSubscriptionAction.class,
            KickstartHostToolsChannelSubscriptionAction.class,
            KickstartInitiateAction.class,
            KickstartInitiateGuestAction.class,
            KickstartInstallType.class,
            KickstartIpRange.class,
            KickstartPackage.class,
            KickstartPreserveFileList.class,
            KickstartScheduleSyncAction.class,
            KickstartScript.class,
            KickstartSessionHistory.class,
            KickstartSessionState.class,
            KickstartTreeType.class,
            KickstartVirtualizationType.class,
            KickstartableTree.class,
            KiwiProfile.class,
            Location.class,
            MaintenanceCalendar.class,
            MaintenanceSchedule.class,
            ManagedServerGroup.class,
            MatcherRunData.class,
            MediaProducts.class,
            MinionRecurringAction.class,
            MinionServer.class,
            MinionServerFactory.class,
            MinionSummary.class,
            Modules.class,
            ModuleFilter.class,
            Namespace.class,
            NetworkInterface.class,
            Note.class,
            NotificationMessage.class,
            OrgAdminManagement.class,
            Org.class,
            OrgConfig.class,
            OrgRecurringAction.class,
            OrgStateRevision.class,
            PackageAction.class,
            PackageActionDetails.class,
            PackageActionRemovalFailure.class,
            PackageActionResult.class,
            PackageArch.class,
            PackageAutoUpdateAction.class,
            PackageBreaks.class,
            PackageCapability.class,
            PackageConflicts.class,
            PackageDelta.class,
            PackageDeltaAction.class,
            PackageEnhances.class,
            PackageEvr.class,
            PackageExtraTagsKeys.class,
            PackageFile.class,
            PackageFilter.class,
            PackageGroup.class,
            PackageKey.class,
            PackageKeyType.class,
            PackageLockAction.class,
            PackageName.class,
            PackageNevra.class,
            PackageObsoletes.class,
            PackagePreDepends.class,
            PackageProvider.class,
            PackageProvides.class,
            PackageRecommends.class,
            PackageRefreshListAction.class,
            PackageRemoveAction.class,
            PackageRequires.class,
            PackageRunTransactionAction.class,
            PackageSource.class,
            PackageState.class,
            PackageSuggests.class,
            PackageSupplements.class,
            PackageUpdateAction.class,
            PackageVerifyAction.class,
            Pane.class,
            PaygCredentialsProduct.class,
            PaygDimensionComputation.class,
            PaygDimensionResult.class,
            PaygSshData.class,
            PersonalInfo.class,
            Pillar.class,
            PinnedSubscription.class,
            PlaybookAction.class,
            PlaybookActionDetails.class,
            PlaybookPath.class,
            PrivateChannelFamily.class,
            ProductName.class,
            Profile.class,
            ProfileCustomDataValue.class,
            ProfileEntry.class,
            ProfileType.class,
            ProjectSource.class,
            ProvisionState.class,
            ProxyInfo.class,
            PtfFilter.class,
            PublicChannelFamily.class,
            PushClient.class,
            PushClientState.class,
            Ram.class,
            RebootAction.class,
            ReleaseChannelMap.class,
            RepoMetadata.class,
            RhnConfiguration.class,
            RecurringConfigChannel.class,
            RecurringHighstate.class,
            RecurringInternalState.class,
            RecurringPlaybook.class,
            RecurringState.class,
            RegistryCredentials.class,
            RegTokenOrgDefault.class,
            ReportDBCredentials.class,
            RHUICredentials.class,
            RhnTimeZone.class,
            RoleImpl.class,
            RollbackAction.class,
            RollbackConfigAction.class,
            RollbackListTransactionsAction.class,
            SAPWorkload.class,
            SCCCredentials.class,
            SCCOrderItem.class,
            SCCProxyRecord.class,
            SCCRegCacheItem.class,
            SCCRepositoryAuth.class,
            SCCRepositoryBasicAuth.class,
            SCCRepository.class,
            SCCRepositoryCloudRmtAuth.class,
            SCCRepositoryNoAuth.class,
            SCCRepositoryTokenAuth.class,
            SCCSubscription.class,
            ScapAction.class,
            ScapActionDetails.class,
            ScriptAction.class,
            ScriptActionDetails.class,
            ScriptResult.class,
            ScriptRunAction.class,
            ServerAction.class,
            ServerAppStream.class,
            Server.class,
            ServerArch.class,
            ServerCoCoAttestationConfig.class,
            ServerCoCoAttestationReport.class,
            ServerFQDN.class,
            ServerGroup.class,
            ServerGroupManager.class,
            ServerGroupStateRevision.class,
            ServerGroupType.class,
            ServerHistoryEvent.class,
            ServerInfo.class,
            ServerLock.class,
            ServerNetAddress4.class,
            ServerNetAddress6.class,
            ServerPath.class,
            ServerPathId.class,
            ServerSnapshot.class,
            ServerSnapshotTagLink.class,
            ServerStateRevision.class,
            ServerUuid.class,
            Severity.class,
            SnapshotTag.class,
            SnapshotTagName.class,
            SoftwareEnvironmentTarget.class,
            SoftwareProjectSource.class,
            SourceRpm.class,
            SslCryptoKey.class,
            State.class,
            StateChange.class,
            StateRevision.class,
            SubscribeChannelsAction.class,
            SubscribeChannelsActionDetails.class,
            SupportDataAction.class,
            SupportDataActionDetails.class,
            SUSEProduct.class,
            SUSEProductChannel.class,
            SUSEProductExtension.class,
            SUSEProductUpgrade.class,
            SystemMigration.class,
            Task.class,
            TaskoBunch.class,
            TaskoRun.class,
            TaskoSchedule.class,
            TaskoTask.class,
            TaskoTemplate.class,
            TemplateCategory.class,
            TemplateString.class,
            TinyUrl.class,
            TokenChannelAppStream.class,
            Token.class,
            TokenPackage.class,
            Up2DateConfigGetAction.class,
            Up2DateConfigUpdateAction.class,
            UserGroupImpl.class,
            UserGroupMembers.class,
            UserGroupMembersId.class,
            UserImpl.class,
            UserInfo.class,
            UserNotification.class,
            UserServerPreference.class,
            VHMCredentials.class,
            VirtualHostManager.class,
            VirtualHostManagerConfig.class,
            VirtualHostManagerNodeInfo.class,
            VirtualInstanceRefreshAction.class,
            VirtualInstanceState.class,
            VirtualInstanceType.class,
            XccdfBenchmark.class,
            XccdfIdent.class,
            XccdfIdentSystem.class,
            XccdfProfile.class,
            XccdfRuleResult.class,
            XccdfRuleResultType.class,
            WebSessionImpl.class,
            XccdfTestResult.class,
            WebEndpoint.class
    );

    /**
     * Returns the list of all available hibernate annotation classes.
     * @return List of hibernate annotation classes.
     */
    public static List<Class<?>> getAnnotationClasses() {
        return ANNOTATION_CLASSES;
    }
}
