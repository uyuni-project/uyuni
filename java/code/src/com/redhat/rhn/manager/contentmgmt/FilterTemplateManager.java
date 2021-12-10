/*
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

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter templates functionality
 */
public class FilterTemplateManager {

    private final ModulemdApi modulemdApi;

    /**
     * Create an instance with default values
     */
    public FilterTemplateManager() {
        this(new ModulemdApi());
    }

    private FilterTemplateManager(ModulemdApi modulemdApiIn) {
        this.modulemdApi = modulemdApiIn;
    }

    /**
     * Create new {@link ContentFilter}s for a live patching application
     *
     * @param prefix the filter name prefix
     * @param kernelEvr the kernel EVR to base the filters on
     * @param user the user
     * @return the list of created filters
     */
    public List<ContentFilter> createLivePatchFilters(String prefix, PackageEvr kernelEvr, User user) {
        ensureOrgAdmin(user);

        Map<String, Pair<FilterCriteria, ContentFilter.Rule>> criteria = Map.of(
                "livepatches-" + kernelEvr, Pair.of(new FilterCriteria(Matcher.CONTAINS_PKG_EQ_EVR, "package_nevr",
                        "kernel-default " + kernelEvr), ALLOW),
                "noreboot", Pair.of(new FilterCriteria(Matcher.CONTAINS, "keyword", "reboot_suggested"), DENY),
                "noreboot-provides", Pair.of(new FilterCriteria(Matcher.CONTAINS_PROVIDES_NAME, "package_provides_name",
                        "installhint(reboot-needed)"), DENY));

        // Make sure none of the filters exist
        ensureNoFiltersExist(criteria.keySet(), prefix, user);

        List<ContentFilter> createdFilters = new ArrayList<>(3);
        criteria.forEach((name, crit) ->
                createdFilters.add(ContentProjectFactory.createFilter(prefix + name, crit.getRight(),
                        EntityType.ERRATUM, crit.getLeft(), user)));

        return createdFilters;
    }

    /**
     * Create new {@link ContentFilter}s for all AppStream modules with default streams
     *
     * @param prefix the filter name prefix
     * @param channel the modular channel with modules to be created
     * @param user the user
     * @return the list of created filters
     */
    public List<ContentFilter> createAppStreamFilters(String prefix, Channel channel, User user)
            throws ModulemdApiException {
        ensureOrgAdmin(user);

        // Create an AppStream filter for every module that has a default stream
        Map<String, FilterCriteria> criteria = modulemdApi.getAllModulesInChannel(channel).entrySet().stream()
                .filter(e -> StringUtils.isNotEmpty(e.getValue().getDefaultStream()))
                .collect(Collectors.toMap(
                        e -> "module-" + e.getKey(),
                        e -> new FilterCriteria(Matcher.EQUALS, "module_stream",
                                e.getKey() + ":" + e.getValue().getDefaultStream())));

        // Make sure none of the filters exist
        ensureNoFiltersExist(criteria.keySet(), prefix, user);

        List<ContentFilter> createdFilters = new ArrayList<>(criteria.size());

        criteria.forEach((name, crit) -> createdFilters.add(
                ContentProjectFactory.createFilter(prefix + name, DENY, EntityType.MODULE, crit, user)));

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

    private static void ensureNoFiltersExist(Collection<String> filterNames, String prefix, User user) {
        filterNames.forEach(name -> ContentManager.lookupFilterByNameAndOrg(prefix + name, user).ifPresent(cp -> {
            throw new EntityExistsException(cp);
        }));
    }
}
