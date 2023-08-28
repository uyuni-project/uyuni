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

import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
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
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.RecurringInternalState;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.rhnpackage.PackageBreaks;
import com.redhat.rhn.domain.rhnpackage.PackageConflicts;
import com.redhat.rhn.domain.rhnpackage.PackageEnhances;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.domain.rhnpackage.PackageObsoletes;
import com.redhat.rhn.domain.rhnpackage.PackagePreDepends;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageRecommends;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.PackageSuggests;
import com.redhat.rhn.domain.rhnpackage.PackageSupplements;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryNoAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;
import com.redhat.rhn.domain.task.Task;

import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionResult;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.oval.db.OVALPlatform;
import com.suse.oval.db.OVALPlatformVulnerablePackage;
import com.suse.oval.db.OVALVulnerablePackage;

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
        ImageStore.class,
        ImageStoreType.class,
        DockerfileProfile.class,
        KiwiProfile.class,
        ImageProfile.class,
        ProfileCustomDataValue.class,
        DeltaImageInfo.class,
        ImageFile.class,
        ImageInfo.class,
        ImageInfoCustomDataValue.class,
        ImageOverview.class,
        ImagePackage.class,
        ImageRepoDigest.class,
        VirtualHostManagerNodeInfo.class,
        NotificationMessage.class,
        UserNotification.class,
        SCCRepository.class,
        SCCSubscription.class,
        SCCOrderItem.class,
        SUSEProductSCCRepository.class,
        SCCRepositoryAuth.class,
        SCCRepositoryNoAuth.class,
        SCCRepositoryBasicAuth.class,
        SCCRepositoryTokenAuth.class,
        SCCRepositoryCloudRmtAuth.class,
        ContentProject.class,
        ContentEnvironment.class,
        ProjectSource.class,
        SoftwareProjectSource.class,
        ContentFilter.class,
        ContentProjectFilter.class,
        PackageFilter.class,
        ErrataFilter.class,
        ModuleFilter.class,
        PtfFilter.class,
        EnvironmentTarget.class,
        SoftwareEnvironmentTarget.class,
        ContentProjectHistoryEntry.class,
        PackageExtraTagsKeys.class,
        PackageProvides.class,
        PackageRequires.class,
        PackageRecommends.class,
        PackageObsoletes.class,
        PackageBreaks.class,
        PackageSupplements.class,
        PackageConflicts.class,
        PackageSuggests.class,
        PackagePreDepends.class,
        PackageEnhances.class,
        MinionRecurringAction.class,
        GroupRecurringAction.class,
        OrgRecurringAction.class,
        MaintenanceSchedule.class,
        MaintenanceCalendar.class,
        SCCRegCacheItem.class,
        AnsiblePath.class,
        InventoryPath.class,
        PlaybookPath.class,
        Pillar.class,
        CloudRmtHost.class,
        PaygSshData.class,
        Task.class,
        RecurringHighstate.class,
        RecurringState.class,
        RecurringConfigChannel.class,
        RecurringInternalState.class,
        InternalState.class,
        PaygDimensionComputation.class,
        PaygDimensionResult.class,
        OVALPlatform.class,
        OVALVulnerablePackage.class,
        OVALPlatformVulnerablePackage.class
    );

    /**
     * Returns the list of all available hibernate annotation classes.
     * @return List of hibernate annotation classes.
     */
    public static List<Class<?>> getAnnotationClasses() {
        return ANNOTATION_CLASSES;
    }
}
