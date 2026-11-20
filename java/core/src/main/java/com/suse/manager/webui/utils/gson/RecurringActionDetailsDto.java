/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecurringActionDetailsDto {

    /** Array containing Quartz information */
    private Map<String, String> cronTimes;

    /** Is test run */
    private boolean test;

    /** Execute through transactional-update on transactional systems */
    private boolean useTransactionalUpdate;

    /** Schedule creation date **/
    private Date createdAt;

    /**  Login of the schedule creator **/
    private String creatorLogin;

    /** The schedule type */
    private String type;

    /** The states assigned to a custom state schedule */
    private Set<StateConfigJson> states;

    /** The playbook path of a playbook schedule */
    private String playbookPath;

    /** The inventory path of a playbook schedule */
    private String inventoryPath;

    /** Whether the cache should be flushed on playbook execution */
    private boolean flushCache;

    /** The extra_vars to use for execution */
    private String extraVars;

    /** The policies assigned to a scap policy schedule */
    private Set<ScapPolicyJson> policies;

    /** Target Content Project label for CLM actions */
    private String projectLabel;

    /** Target Content Project name for CLM actions */
    private String projectName;

    /** Target Content Environment label for CLM promote actions */
    private String environmentLabel;

    /** Target Content Environment name for CLM promote actions */
    private String environmentName;

    /** Target Content Successor Environment label for CLM promote actions */
    private String nextEnvironmentLabel;

    /** Target Content Successor Environment name for CLM promote actions */
    private String nextEnvironmentName;

    /** Whether the recurring action schedule is active */
    private boolean active;

    /** Next scheduled execution timestamp of the trigger */
    private Date nextExecution;

    /** Current deployed version in source environment */
    private Long sourceEnvironmentVersion;

    /** Current deployed version in target environment */
    private Long targetEnvironmentVersion;

    /** Timestamp when target environment was last modified/promoted */
    private Date targetEnvironmentModified;

    /** Recent build history for CLM build schedules */
    private List<ClmBuildHistoryDto> buildHistory;

    /**
     * @return the Array containing Quartz information
     */
    public Map<String, String> getCronTimes() {
        return cronTimes;
    }

    /**
     * @return the Array containing Quartz information
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @return true when transactional systems should execute through transactional-update
     */
    public boolean isUseTransactionalUpdate() {
        return useTransactionalUpdate;
    }

    /**
     * Sets the cronTimes.
     *
     * @param cronTimesIn the cronTimes
     */
    public void setCronTimes(Map<String, String> cronTimesIn) {
        cronTimes = cronTimesIn;
    }

    /**
     * Sets the test.
     *
     * @param testIn the test
     */
    public void setTest(boolean testIn) {
        test = testIn;
    }

    /**
     * Sets whether transactional systems should execute through transactional-update.
     *
     * @param useTransactionalUpdateIn whether transactional-update should be used
     */
    public void setUseTransactionalUpdate(boolean useTransactionalUpdateIn) {
        useTransactionalUpdate = useTransactionalUpdateIn;
    }

    /**
     * Gets the creation date
     *
     * @return createdAt
     */
    public Date getCreated() {
        return createdAt;
    }

    /**
     *  Sets the creation date
     *
     * @param createdAtIn schedule creation date
     */
    public void setCreated(Date createdAtIn) {
        this.createdAt = createdAtIn;
    }

    /**
     *  Sets the creator login
     *
     * @param creatorLoginIn login name of the creator
     */
    public void setCreatorLogin(String creatorLoginIn) {
        this.creatorLogin = creatorLoginIn;
    }

    /**
     *  Get the creator login
     *
     * @return creatorLoginIn
     */
    public String getCreatorLogin() {
        return this.creatorLogin;
    }

    /**
     * @return the type of the schedule
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param typeIn the type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the set of states
     */
    public Set<StateConfigJson> getStates() {
        return this.states;
    }

    /**
     * Sets the states
     *
     * @param statesIn the states
     */
    public void setStates(Set<StateConfigJson> statesIn) {
        this.states = statesIn;
    }

    /**
     * Gets the playbook path
     *
     * @return the playbook path
     */
    public String getPlaybookPath() {
        return playbookPath;
    }

    /**
     * Sets the playbook path
     *
     * @param playbookPathIn the playbook path
     */
    public void setPlaybookPath(String playbookPathIn) {
        playbookPath = playbookPathIn;
    }

    /**
     * Gets the inventory path
     *
     * @return the inventory path
     */
    public String getInventoryPath() {
        return inventoryPath;
    }

    /**
     * Sets the inventory path
     *
     * @param inventoryPathIn the inventory path
     */
    public void setInventoryPath(String inventoryPathIn) {
        inventoryPath = inventoryPathIn;
    }

    /**
     * Gets the flush cache
     *
     * @return if the cache should be flushed
     */
    public boolean isFlushCache() {
        return flushCache;
    }

    /**
     * Sets if the cache should be flushed
     *
     * @param flushCacheIn the flush cache
     */
    public void setFlushCache(boolean flushCacheIn) {
        flushCache = flushCacheIn;
    }

    /**
     * Gets the extra vars
     *
     * @return the extra vars
     */
    public String getExtraVars() {
        return extraVars;
    }

    /**
     * Sets the extra vars
     *
     * @param extraVarsIn the extra vars
     */
    public void setExtraVars(String extraVarsIn) {
        extraVars = extraVarsIn;
    }
    /**
     * @return the set of states
     */
    public Set<ScapPolicyJson> getPolicies() {
        return this.policies;
    }

    /**
     * Sets the policies
     *
     * @param policiesIn the polices
     */
    public void setPolicies(Set<ScapPolicyJson> policiesIn) {
        this.policies = policiesIn;
    }

    /**
     * @return the project label
     */
    public String getProjectLabel() {
        return projectLabel;
    }

    /**
     * @param projectLabelIn the project label
     */
    public void setProjectLabel(String projectLabelIn) {
        this.projectLabel = projectLabelIn;
    }

    /**
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectNameIn the project name
     */
    public void setProjectName(String projectNameIn) {
        this.projectName = projectNameIn;
    }

    /**
     * @return the environment label
     */
    public String getEnvironmentLabel() {
        return environmentLabel;
    }

    /**
     * @param environmentLabelIn the environment label
     */
    public void setEnvironmentLabel(String environmentLabelIn) {
        this.environmentLabel = environmentLabelIn;
    }

    /**
     * @return the environment name
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * @param environmentNameIn the environment name
     */
    public void setEnvironmentName(String environmentNameIn) {
        this.environmentName = environmentNameIn;
    }

    /**
     * @return the next environment label
     */
    public String getNextEnvironmentLabel() {
        return nextEnvironmentLabel;
    }

    /**
     * @param nextEnvironmentLabelIn the next environment label
     */
    public void setNextEnvironmentLabel(String nextEnvironmentLabelIn) {
        this.nextEnvironmentLabel = nextEnvironmentLabelIn;
    }

    /**
     * @return the next environment name
     */
    public String getNextEnvironmentName() {
        return nextEnvironmentName;
    }

    /**
     * @param nextEnvironmentNameIn the next environment name
     */
    public void setNextEnvironmentName(String nextEnvironmentNameIn) {
        this.nextEnvironmentName = nextEnvironmentNameIn;
    }

    /**
     * @return true if the recurring action is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param activeIn whether active
     */
    public void setActive(boolean activeIn) {
        this.active = activeIn;
    }

    /**
     * @return the next execution timestamp
     */
    public Date getNextExecution() {
        return nextExecution;
    }

    /**
     * @param nextExecutionIn the next execution timestamp
     */
    public void setNextExecution(Date nextExecutionIn) {
        this.nextExecution = nextExecutionIn;
    }

    /**
     * @return the source environment version
     */
    public Long getSourceEnvironmentVersion() {
        return sourceEnvironmentVersion;
    }

    /**
     * @param sourceEnvironmentVersionIn the source environment version
     */
    public void setSourceEnvironmentVersion(Long sourceEnvironmentVersionIn) {
        this.sourceEnvironmentVersion = sourceEnvironmentVersionIn;
    }

    /**
     * @return the target environment version
     */
    public Long getTargetEnvironmentVersion() {
        return targetEnvironmentVersion;
    }

    /**
     * @param targetEnvironmentVersionIn the target environment version
     */
    public void setTargetEnvironmentVersion(Long targetEnvironmentVersionIn) {
        this.targetEnvironmentVersion = targetEnvironmentVersionIn;
    }

    /**
     * @return the target environment modified timestamp
     */
    public Date getTargetEnvironmentModified() {
        return targetEnvironmentModified;
    }

    /**
     * @param targetEnvironmentModifiedIn the target environment modified timestamp
     */
    public void setTargetEnvironmentModified(Date targetEnvironmentModifiedIn) {
        this.targetEnvironmentModified = targetEnvironmentModifiedIn;
    }

    /**
     * @return the build history list
     */
    public List<ClmBuildHistoryDto> getBuildHistory() {
        return buildHistory;
    }

    /**
     * @param buildHistoryIn the build history list
     */
    public void setBuildHistory(List<ClmBuildHistoryDto> buildHistoryIn) {
        this.buildHistory = buildHistoryIn;
    }
}
