/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.task;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.frontend.dto.PackageCapabilityDto;
import com.redhat.rhn.frontend.dto.PackageChangelogDto;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * TaskManager
 * @version $Rev$
 */
public class TaskManager {


    private TaskManager() {

    }

    /**
     *  Get the channel package list for a channel
     * @param channel channel info
     * @return the iterator
     */
    public static DataResult<PackageDto> getChannelPackageDtos(Channel channel) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_GENERATOR_CHANNEL_PACKAGES);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_id", channel.getId());
        return m.execute(params);
    }

    /**
     *  Get the channel package list for a channel
     * @param channel channel info
     * @param start index of first element
     * @param pageSize how many elements to fetch
     * @return the iterator
     */
    public static DataResult<PackageDto> getChannelPackageDtos(Channel channel, long start, int pageSize) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_GENERATOR_CHANNEL_PACKAGES_BATCH);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_id", channel.getId());
        params.put("offset", start);
        params.put("limit", pageSize);
        return m.execute(params);
    }

    /**
     * Get capabilities of a certain type for a package
     * @param packageId the package's id
     * @param query the query to execute
     * @return a collection of capabilities
     */
    public static Collection<PackageCapabilityDto> getPackageCapabilityDtos(
            Long packageId, String query) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME, query);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("package_id", packageId);
        return m.execute(params);
    }

    /**
     * Get changelog entries for a particular package
     * @param packageId the package's id
     * @return a collection of changelogs
     */
    public static Collection<PackageChangelogDto> getPackageChangelogDtos(
            Long packageId) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_GENERATOR_PACKAGE_CHANGELOG);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("package_id", packageId);
        return m.execute(params);
    }

    /**
     * Get keyword labels for a particular package in a channel
     * @param channelId the channel's id
     * @param packageId the package's id
     * @return a collection of keywords
     */
    public static Collection<String> getChannelPackageKeywords(
            Long channelId, Long packageId) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_PACKAGE_KEYWORDS);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_id", channelId);
        params.put("package_id", packageId);
        DataResult<Map<String, Object>> dataResult = m.execute(params);

        // if the result is empty and the channel is a clone, we'll try to look for the
        // information in the original channel (in a recursive manner)
        if (dataResult.isEmpty()) {
            SelectMode originalMode = ModeFactory.getMode(TaskConstants.MODE_NAME,
                    TaskConstants.TASK_QUERY_REPOMD_GENERATOR_CLONED_FROM);
            Map<String, Object> originalQueryParams = new HashMap<>();
            originalQueryParams.put("channel_id", channelId);
            DataResult<Map<String, Long>> originalId =
                    originalMode.execute(originalQueryParams);
            if (!originalId.isEmpty()) {
                return getChannelPackageKeywords(originalId.get(0).get("original_id"),
                        packageId);
            }
        }

        List<String> result = new LinkedList<>();
        for (Map<String, Object> row: dataResult) {
            result.add((String)row.get("label"));
        }
        return result;
    }

    /**
     * Return task status info
     * @return task status info
     */
    public static DataResult getTaskStatusInfo() {
        SelectMode m = ModeFactory.getMode("Task_queries", "taskomatic_task_status");
        return m.execute(new HashMap());
    }
}
