/*
 * Copyright (c) 2026 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.manager.BaseTransactionCommand;

import com.suse.manager.saltboot.SaltbootMigrationException;
import com.suse.manager.saltboot.SaltbootMigrationUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class responsible for executing one-time upgrade logic
 */
public class UpgradeCommand extends BaseTransactionCommand {

    /**
     * Logger for this class
     */
    private static final Logger LOG = LogManager.getLogger(UpgradeCommand.class);

    private final SaltApi saltApi;
    private final MinionPillarManager minionPillarManager;

    public static final String UPGRADE_TASK_NAME = "upgrade_satellite_";
    public static final String REFRESH_ALL_SYSTEMS_PILLARS = UPGRADE_TASK_NAME + "all_systems_pillar_refresh";
    public static final String ALL_SYSTEMS_SYNC_ALL = UPGRADE_TASK_NAME + "all_systems_sync_all";
    public static final String MIGRATE_COBBLER = UPGRADE_TASK_NAME + "migrate_cobbler";

    /**
     * Constructor
     */
    public UpgradeCommand() {
        this(GlobalInstanceHolder.SALT_API, MinionPillarManager.INSTANCE);
    }

    /**
     * Constructor with parameters
     * @param saltApiIn Salt API instance
     * @param minionPillarManagerIn MinionPillarManager instance
     */
    public UpgradeCommand(SaltApi saltApiIn,  MinionPillarManager minionPillarManagerIn) {
        super(LOG);
        this.saltApi = saltApiIn;
        this.minionPillarManager = minionPillarManagerIn;
    }

    /**
     * Executes the upgrade step in an own transaction
     */
    public void store() {
        try {
            upgrade();
        }
        catch (Exception e) {
            LOG.error("Problem upgrading!", e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            handleTransaction();
        }
    }

    /**
     * Executes the upgrade step
     */
    public void upgrade() {
        List<Task> upgradeTasks = TaskFactory.getTaskListByNameLike(UPGRADE_TASK_NAME);
        // Loop over upgrade tasks and execute the steps.
        for (Task t : upgradeTasks) {
            // Use WARN because we want this logged.
            if (t != null) {
                LOG.warn("got upgrade task: {}", t.getName());
                switch (t.getName()) {
                    case REFRESH_ALL_SYSTEMS_PILLARS:
                        refreshAllSystemsPillar();
                        break;
                    case ALL_SYSTEMS_SYNC_ALL:
                        allSystemsSyncAll();
                        break;
                    case MIGRATE_COBBLER:
                        migrateCobbler(t);
                        return; // do not remove the task here, migrateCobbler handles it
                    default:
                }
                // always run this
                TaskFactory.remove(t);
            }
        }
    }

    /**
     * Regenerate pillar data for every registered system.
     */
    protected void refreshAllSystemsPillar() {
        try {
            List<MinionServer> hosts = MinionServerFactory.listMinions();
            hosts.forEach(minionPillarManager::generatePillar);
            List<String> minionIds = hosts.stream().map(MinionServer::getMinionId).collect(Collectors.toList());
            saltApi.refreshPillar(new MinionList(minionIds));
            LOG.info("Refreshed hosts pillar");
        }
        catch (Exception e) {
            LOG.error("Error refreshing hosts pillar. Ignoring.", e);
        }
    }

    /**
     * Run Sync_all on all systems
     */
    protected void allSystemsSyncAll() {
        try {
            List<String> minionIds = MinionServerFactory.listMinions()
                    .stream().map(MinionServer::getMinionId).collect(Collectors.toList());
            saltApi.syncAllAsync(new MinionList(minionIds));
            LOG.info("Sync all scheduled on all systems");
        }
        catch (Exception e) {
            LOG.error("Error running sync_all. Ignoring.", e);
        }
    }

    /**
     * Migrate cobbler entries.
     * The execution must be delayed, because cobbler auth needs a fully started tomcat.
     */
    protected void migrateCobbler(Task t) {
        new Thread(() -> {
            try {
                LOG.info("Cobbler migration: waiting");
                Thread.sleep(60000);
                LOG.info("Cobbler migration: started");
                SaltbootMigrationUtils.migrateSaltboot();
                TaskFactory.remove(t);
                HibernateFactory.commitTransaction();
                LOG.info("Cobbler migration: finished");
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (SaltbootMigrationException e) {
                LOG.error("Cobbler migration failed", e);
            }
            finally {
                HibernateFactory.closeSession();
            }
        }).start();
    }
}
