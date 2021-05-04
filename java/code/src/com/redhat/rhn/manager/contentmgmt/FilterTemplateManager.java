/**
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.manager.contentmgmt;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;

/**
 * Filter templates functionality
 */
public class FilterTemplateManager {

    /**
     * Create a new {@link ContentFilter}
     *
     * @param prefix the filter name prefix
     * @param kernelEvr the kernel EVR to base the filters on
     * @param user the user
     * @return the list of created filters
     */
    public List<ContentFilter> createLivePatchFilters(String prefix, PackageEvr kernelEvr, User user) {
        ensureOrgAdmin(user);

        Map<String, FilterCriteria> criteria =
                Map.of("livepatches", new FilterCriteria(FilterCriteria.Matcher.CONTAINS_PKG_GT_EVR,
                "package_nevr", "kernel-default " + kernelEvr.toString()),
                "noreboot", new FilterCriteria(FilterCriteria.Matcher.CONTAINS,
                "keyword", "reboot_suggested"));

        // Make sure none of the filters exist
        criteria.keySet().forEach(name ->
                ContentManager.lookupFilterByNameAndOrg(prefix + name, user).ifPresent(cp -> {
                    throw new EntityExistsException(cp);
                }));

        List<ContentFilter> createdFilters = new ArrayList<>(2);

        criteria.forEach((name, crit) -> createdFilters.add(
                ContentProjectFactory.createFilter(prefix + "-" + name, ContentFilter.Rule.DENY,
                        ContentFilter.EntityType.ERRATUM, crit, user)
        ));

        return createdFilters;
    }

    /**
     * Ensures that given user has the Org admin role
     *
     * @param user the user
     * @throws PermissionException if the user does not have Org admin role
     */
    private static void ensureOrgAdmin(User user) {
        if (!user.hasRole(ORG_ADMIN)) {
            throw new PermissionException(ORG_ADMIN);
        }
    }
}
