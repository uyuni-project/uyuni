/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.xmlrpc.serializer.MaintenanceCalendarSerializer;
import com.suse.manager.xmlrpc.serializer.MaintenanceScheduleSerializer;
import com.suse.manager.xmlrpc.serializer.RescheduleResultSerializer;
import com.suse.manager.xmlrpc.serializer.SystemEventDetailsDtoSerializer;
import com.suse.manager.xmlrpc.serializer.SystemEventDtoSerializer;

import java.util.LinkedList;
import java.util.List;


/**
 * SerializerRegistry
 *
 * Stores a list of serializer classes for registration the first time a SerializerFactory
 * is used. Previously we were doing this by searching a package in the jar and extracting
 * classes that implement the correct interface, but problems were encountered with
 * existing satellite's and likely Tomcat caching. We're unsure of how stable this will be
 * in the future so resorting to an explicit method of declaring serializer classes once
 * again.
 */
public class SerializerRegistry {

    private SerializerRegistry() {
        // Hide the default constructor.
    }

    private static final List<Class<? extends ApiResponseSerializer<?>>> SERIALIZER_CLASSES;
    static {
        SERIALIZER_CLASSES = new LinkedList<>();
        SERIALIZER_CLASSES.add(ActivationKeySerializer.class);
        SERIALIZER_CLASSES.add(TokenSerializer.class);
        SERIALIZER_CLASSES.add(ChannelArchSerializer.class);
        SERIALIZER_CLASSES.add(ChannelInfoSerializer.class);
        SERIALIZER_CLASSES.add(ChannelSerializer.class);
        SERIALIZER_CLASSES.add(CpuSerializer.class);
        SERIALIZER_CLASSES.add(DeltaImageSerializer.class);
        SERIALIZER_CLASSES.add(DeviceSerializer.class);
        SERIALIZER_CLASSES.add(DmiSerializer.class);
        SERIALIZER_CLASSES.add(EndpointInfoSerializer.class);
        SERIALIZER_CLASSES.add(ErrataOverviewSerializer.class);
        SERIALIZER_CLASSES.add(ErrataSerializer.class);
        SERIALIZER_CLASSES.add(HistoryEventSerializer.class);
        SERIALIZER_CLASSES.add(ManagedServerGroupSerializer.class);
        SERIALIZER_CLASSES.add(OrgSerializer.class);
        SERIALIZER_CLASSES.add(OrgTrustOverviewSerializer.class);
        SERIALIZER_CLASSES.add(PackageMetadataSerializer.class);
        SERIALIZER_CLASSES.add(PackageSerializer.class);
        SERIALIZER_CLASSES.add(RhnTimeZoneSerializer.class);
        SERIALIZER_CLASSES.add(ScriptResultSerializer.class);
        SERIALIZER_CLASSES.add(ServerSerializer.class);
        SERIALIZER_CLASSES.add(ServerPathSerializer.class);
        SERIALIZER_CLASSES.add(SystemSearchResultSerializer.class);
        SERIALIZER_CLASSES.add(SystemOverviewSerializer.class);
        SERIALIZER_CLASSES.add(ShortSystemInfoSerializer.class);
        SERIALIZER_CLASSES.add(SystemGroupsDTOSerializer.class);
        SERIALIZER_CLASSES.add(FormulaDataSerializer.class);
        SERIALIZER_CLASSES.add(FormulaSerializer.class);
        SERIALIZER_CLASSES.add(EmptySystemProfileSerializer.class);
        SERIALIZER_CLASSES.add(UserSerializer.class);
        SERIALIZER_CLASSES.add(KickstartTreeSerializer.class);
        SERIALIZER_CLASSES.add(KickstartTreeDetailSerializer.class);
        SERIALIZER_CLASSES.add(ConfigRevisionSerializer.class);
        SERIALIZER_CLASSES.add(ConfigChannelSerializer.class);
        SERIALIZER_CLASSES.add(ConfigChannelDtoSerializer.class);
        SERIALIZER_CLASSES.add(ConfigChannelTypeSerializer.class);
        SERIALIZER_CLASSES.add(ConfigFileDtoSerializer.class);
        SERIALIZER_CLASSES.add(ConfigFileNameDtoSerializer.class);
        SERIALIZER_CLASSES.add(ConfigSystemDtoSerializer.class);
        SERIALIZER_CLASSES.add(OrgDtoSerializer.class);
        SERIALIZER_CLASSES.add(MultiOrgUserOverviewSerializer.class);
        SERIALIZER_CLASSES.add(VirtualSystemOverviewSerializer.class);
        SERIALIZER_CLASSES.add(EntitlementSerializer.class);
        SERIALIZER_CLASSES.add(NetworkInterfaceSerializer.class);
        SERIALIZER_CLASSES.add(ScheduleActionSerializer.class);
        SERIALIZER_CLASSES.add(ScheduleSystemSerializer.class);
        SERIALIZER_CLASSES.add(KickstartDtoSerializer.class);
        SERIALIZER_CLASSES.add(KickstartScriptSerializer.class);
        SERIALIZER_CLASSES.add(ServerSnapshotSerializer.class);
        SERIALIZER_CLASSES.add(PackageNevraSerializer.class);
        SERIALIZER_CLASSES.add(NoteSerializer.class);
        SERIALIZER_CLASSES.add(KickstartIpRangeSerializer.class);
        SERIALIZER_CLASSES.add(CryptoKeySerializer.class);
        SERIALIZER_CLASSES.add(CryptoKeyDtoSerializer.class);
        SERIALIZER_CLASSES.add(CryptoKeyTypeSerializer.class);
        SERIALIZER_CLASSES.add(KickstartCommandSerializer.class);
        SERIALIZER_CLASSES.add(KickstartCommandNameSerializer.class);
        SERIALIZER_CLASSES.add(KickstartOptionValueSerializer.class);
        SERIALIZER_CLASSES.add(KickstartAdvancedOptionsSerializer.class);
        SERIALIZER_CLASSES.add(CustomDataKeySerializer.class);
        SERIALIZER_CLASSES.add(KickstartInstallTypeSerializer.class);
        SERIALIZER_CLASSES.add(FilePreservationDtoSerializer.class);
        SERIALIZER_CLASSES.add(FileListSerializer.class);
        SERIALIZER_CLASSES.add(ServerActionSerializer.class);
        SERIALIZER_CLASSES.add(ChannelTreeNodeSerializer.class);
        SERIALIZER_CLASSES.add(TrustedOrgDtoSerializer.class);
        SERIALIZER_CLASSES.add(PackageKeySerializer.class);
        SERIALIZER_CLASSES.add(PackageProviderSerializer.class);
        SERIALIZER_CLASSES.add(PackageDtoSerializer.class);
        SERIALIZER_CLASSES.add(PackageOverviewSerializer.class);
        SERIALIZER_CLASSES.add(ProfileOverviewDtoSerializer.class);
        SERIALIZER_CLASSES.add(SnippetSerializer.class);
        SERIALIZER_CLASSES.add(NetworkDtoSerializer.class);
        SERIALIZER_CLASSES.add(DistChannelMapSerializer.class);
        SERIALIZER_CLASSES.add(ContentSourceSerializer.class);
        SERIALIZER_CLASSES.add(ContentSourceFilterSerializer.class);
        SERIALIZER_CLASSES.add(SslContentSourceSerializer.class);
        SERIALIZER_CLASSES.add(XccdfTestResultDtoSerializer.class);
        SERIALIZER_CLASSES.add(XccdfTestResultSerializer.class);
        SERIALIZER_CLASSES.add(XccdfRuleResultDtoSerializer.class);
        SERIALIZER_CLASSES.add(IssMasterSerializer.class);
        SERIALIZER_CLASSES.add(IssMasterOrgSerializer.class);
        SERIALIZER_CLASSES.add(IssSlaveSerializer.class);
        SERIALIZER_CLASSES.add(CVEAuditServerSerializer.class);
        SERIALIZER_CLASSES.add(CVEAuditImageSerializer.class);
        SERIALIZER_CLASSES.add(UserExtGroupSerializer.class);
        SERIALIZER_CLASSES.add(OrgUserExtGroupSerializer.class);
        SERIALIZER_CLASSES.add(PackageSourceOverviewSerializer.class);
        SERIALIZER_CLASSES.add(MgrSyncProductDtoSerializer.class);
        SERIALIZER_CLASSES.add(MgrSyncChannelDtoSerializer.class);
        SERIALIZER_CLASSES.add(MirrorCredentialsDtoSerializer.class);
        SERIALIZER_CLASSES.add(VirtualHostManagerSerializer.class);
        SERIALIZER_CLASSES.add(PinnedSubscriptionSerializer.class);
        SERIALIZER_CLASSES.add(SUSEInstalledProductSerializer.class);
        SERIALIZER_CLASSES.add(ImageStoreTypeSerializer.class);
        SERIALIZER_CLASSES.add(ImageStoreSerializer.class);
        SERIALIZER_CLASSES.add(ImageProfileSerializer.class);
        SERIALIZER_CLASSES.add(ImageInfoSerializer.class);
        SERIALIZER_CLASSES.add(ImageFileSerializer.class);
        SERIALIZER_CLASSES.add(ImageOverviewSerializer.class);
        SERIALIZER_CLASSES.add(ContentProjectSerializer.class);
        SERIALIZER_CLASSES.add(ContentEnvironmentSerializer.class);
        SERIALIZER_CLASSES.add(ContentProjectSourceSerializer.class);
        SERIALIZER_CLASSES.add(ContentFilterSerializer.class);
        SERIALIZER_CLASSES.add(ContentProjectFilterSerializer.class);
        SERIALIZER_CLASSES.add(RecurringActionSerializer.class);
        SERIALIZER_CLASSES.add(PackageStateSerializer.class);
        SERIALIZER_CLASSES.add(MaintenanceScheduleSerializer.class);
        SERIALIZER_CLASSES.add(MaintenanceCalendarSerializer.class);
        SERIALIZER_CLASSES.add(RescheduleResultSerializer.class);
        SERIALIZER_CLASSES.add(AnsiblePathSerializer.class);
        SERIALIZER_CLASSES.add(AnsiblePlaybookSerializer.class);
        SERIALIZER_CLASSES.add(SystemEventDtoSerializer.class);
        SERIALIZER_CLASSES.add(SystemEventDetailsDtoSerializer.class);
        SERIALIZER_CLASSES.add(PaygSshDataSerializer.class);
        SERIALIZER_CLASSES.add(CVEAffectedPackageSerializer.class);
        SERIALIZER_CLASSES.add(CVEAffectedServerSerializer.class);
    }

    /**
     * Returns the list of all available custom XMLRPC serializers.
     * @return List of serializer classes.
     */
    public static List<Class<? extends ApiResponseSerializer<?>>> getSerializationClasses() {
        return SERIALIZER_CLASSES;
    }
}
