--
-- Copyright (c) 2020 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

alter table rhnActionType drop CONSTRAINT if exists rhn_action_type_mtmode_ck;
alter table rhnActionType add column if not exists
    maintenance_mode_only CHAR(1)
                          DEFAULT ('N') NOT NULL;
alter table rhnActionType add
    CONSTRAINT rhn_action_type_mtmode_ck
    CHECK (maintenance_mode_only in ('Y','N'));

update rhnActionType set maintenance_mode_only = 'Y'
where label in ('packages.update', 'packages.remove', 'errata.update',
       'up2date_config.get', 'up2date_config.update', 'packages.delta',
       'reboot.reboot', 'rollback.config', 'rollback.rollback', 'packages.autoupdate',
       'packages.runTransaction', 'configfiles.deploy', 'kickstart.initiate',
       'solarispkgs.install', 'solarispkgs.remove', 'solarispkgs.patchInstall',
       'solarispkgs.patchRemove', 'solarispkgs.patchClusterInstall', 'solarispkgs.patchClusterRemove',
       'script.run', 'solarispkgs.refresh_list', 'clientcert.update_client_cert',
       'distupgrade.upgrade', 'states.apply', 'cluster.group_refresh_nodes', 'cluster.join_node',
       'cluster.remove_node', 'cluster.upgrade_cluster')
and maintenance_mode_only = 'N';
