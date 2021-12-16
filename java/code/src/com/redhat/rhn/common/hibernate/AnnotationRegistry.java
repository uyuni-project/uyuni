/**
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
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageBuildHistory;
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
import com.redhat.rhn.domain.recurringactions.RecurringAction;
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
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryNoAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;

import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import java.util.LinkedList;
import java.util.List;


/**
 * AnnotationRegistry
 *
 * Stores a list of hibernate annotation classes for registration the first time the
 * ConnectionManager.
*/
public class AnnotationRegistry {

    private AnnotationRegistry() {
        // Hide the default constructor.
    }

    private static final List<Class> ANNOTATION_CLASSES;
    static {
        ANNOTATION_CLASSES = new LinkedList<>();
        ANNOTATION_CLASSES.add(ImageStore.class);
        ANNOTATION_CLASSES.add(ImageStoreType.class);
        ANNOTATION_CLASSES.add(DockerfileProfile.class);
        ANNOTATION_CLASSES.add(KiwiProfile.class);
        ANNOTATION_CLASSES.add(ImageProfile.class);
        ANNOTATION_CLASSES.add(ProfileCustomDataValue.class);
        ANNOTATION_CLASSES.add(ImageInfo.class);
        ANNOTATION_CLASSES.add(ImageInfoCustomDataValue.class);
        ANNOTATION_CLASSES.add(ImageOverview.class);
        ANNOTATION_CLASSES.add(ImagePackage.class);
        ANNOTATION_CLASSES.add(ImageBuildHistory.class);
        ANNOTATION_CLASSES.add(ImageRepoDigest.class);
        ANNOTATION_CLASSES.add(VirtualHostManagerNodeInfo.class);
        ANNOTATION_CLASSES.add(NotificationMessage.class);
        ANNOTATION_CLASSES.add(UserNotification.class);
        ANNOTATION_CLASSES.add(SCCRepository.class);
        ANNOTATION_CLASSES.add(SCCSubscription.class);
        ANNOTATION_CLASSES.add(SCCOrderItem.class);
        ANNOTATION_CLASSES.add(SUSEProductSCCRepository.class);
        ANNOTATION_CLASSES.add(SCCRepositoryAuth.class);
        ANNOTATION_CLASSES.add(SCCRepositoryNoAuth.class);
        ANNOTATION_CLASSES.add(SCCRepositoryBasicAuth.class);
        ANNOTATION_CLASSES.add(SCCRepositoryTokenAuth.class);
        ANNOTATION_CLASSES.add(ContentProject.class);
        ANNOTATION_CLASSES.add(ContentEnvironment.class);
        ANNOTATION_CLASSES.add(ProjectSource.class);
        ANNOTATION_CLASSES.add(SoftwareProjectSource.class);
        ANNOTATION_CLASSES.add(ContentFilter.class);
        ANNOTATION_CLASSES.add(ContentProjectFilter.class);
        ANNOTATION_CLASSES.add(PackageFilter.class);
        ANNOTATION_CLASSES.add(ErrataFilter.class);
        ANNOTATION_CLASSES.add(ModuleFilter.class);
        ANNOTATION_CLASSES.add(EnvironmentTarget.class);
        ANNOTATION_CLASSES.add(SoftwareEnvironmentTarget.class);
        ANNOTATION_CLASSES.add(ContentProjectHistoryEntry.class);
        ANNOTATION_CLASSES.add(PackageExtraTagsKeys.class);
        ANNOTATION_CLASSES.add(PackageProvides.class);
        ANNOTATION_CLASSES.add(PackageRequires.class);
        ANNOTATION_CLASSES.add(PackageRecommends.class);
        ANNOTATION_CLASSES.add(PackageObsoletes.class);
        ANNOTATION_CLASSES.add(PackageBreaks.class);
        ANNOTATION_CLASSES.add(PackageSupplements.class);
        ANNOTATION_CLASSES.add(PackageConflicts.class);
        ANNOTATION_CLASSES.add(PackageSuggests.class);
        ANNOTATION_CLASSES.add(PackagePreDepends.class);
        ANNOTATION_CLASSES.add(PackageEnhances.class);
        ANNOTATION_CLASSES.add(RecurringAction.class);
        ANNOTATION_CLASSES.add(MinionRecurringAction.class);
        ANNOTATION_CLASSES.add(GroupRecurringAction.class);
        ANNOTATION_CLASSES.add(OrgRecurringAction.class);
        ANNOTATION_CLASSES.add(Cluster.class);
        ANNOTATION_CLASSES.add(MaintenanceSchedule.class);
        ANNOTATION_CLASSES.add(MaintenanceCalendar.class);
        ANNOTATION_CLASSES.add(SCCRegCacheItem.class);
    }

    /**
     * Returns the list of all available hibernate annotation classes.
     * @return List of hibernate annotation classes.
     */
    public static List<Class> getAnnotationClasses() {
        return ANNOTATION_CLASSES;
    }
}
