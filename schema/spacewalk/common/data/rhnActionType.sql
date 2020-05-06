--
-- Copyright (c) 2012--2014 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
--
--
-- data for rhnActionType

-- last two values are for TRIGGER_SNAPSHOT, UNLOCKED_ONLY
insert into rhnActionType values (1, 'packages.refresh_list', 'Package List Refresh', 'Y', 'N', 'N');
insert into rhnActionType values (2, 'hardware.refresh_list', 'Hardware List Refresh', 'N', 'N', 'N');
insert into rhnActionType values (3, 'packages.update', 'Package Install', 'Y', 'Y', 'Y');
insert into rhnActionType values (4, 'packages.remove', 'Package Removal', 'Y', 'Y', 'Y');
insert into rhnActionType values (5, 'errata.update', 'Patch Update', 'Y', 'Y', 'Y');
insert into rhnActionType values (6, 'up2date_config.get', 'Get server up2date config', 'Y', 'Y', 'Y');
insert into rhnActionType values (7, 'up2date_config.update', 'Update server up2date config', 'Y', 'Y', 'Y');
insert into rhnActionType values (8, 'packages.delta', 'Package installation and removal in one RPM transaction', 'Y', 'Y', 'N');
insert into rhnActionType values (9, 'reboot.reboot', 'System reboot', 'N', 'Y', 'Y');
insert into rhnActionType values (10, 'rollback.config', 'Enable or Disable RPM Transaction Rollback', 'N', 'Y', 'Y');
insert into rhnActionType values (11, 'rollback.listTransactions', 'Refresh server-side transaction list', 'N', 'N', 'N');
insert into rhnActionType values (12, 'rollback.rollback', 'RPM Transaction Rollback', 'Y', 'Y', 'Y');
insert into rhnActionType values (13, 'packages.autoupdate', 'Automatic package installation', 'Y', 'Y', 'Y');
insert into rhnActionType values (14, 'packages.runTransaction', 'Package Synchronization', 'Y', 'Y', 'Y');
insert into rhnActionType values (15, 'configfiles.upload', 'Upload config file data to server', 'N', 'N', 'N');
insert into rhnActionType values (16, 'configfiles.deploy', 'Deploy config files to system', 'Y', 'Y', 'Y');
insert into rhnActionType values (17, 'configfiles.verify', 'Verify deployed config files', 'N', 'N', 'N');
insert into rhnActionType values (18, 'configfiles.diff', 'Show differences between profiled config files and deployed config files', 'N', 'N', 'N');
insert into rhnActionType values (19, 'kickstart.initiate', 'Initiate an auto installation', 'N', 'Y', 'Y');
insert into rhnActionType values (20, 'kickstart.schedule_sync', 'Schedule a package sync for auto installations', 'N', 'N', 'N');
insert into rhnActionType values (21, 'activation.schedule_pkg_install', 'Schedule a package install for activation key', 'N', 'N', 'N');
insert into rhnActionType values (22, 'activation.schedule_deploy', 'Schedule a config deploy for activation key', 'N', 'N', 'N');
insert into rhnActionType values (23, 'configfiles.mtime_upload', 'Upload config file data based upon mtime to server', 'N', 'N', 'N');
insert into rhnActionType values (24, 'solarispkgs.install', 'Solaris Package Install', 'Y', 'Y', 'Y');
insert into rhnActionType values (25, 'solarispkgs.remove', 'Solaris Package Removal', 'Y', 'Y', 'Y');
insert into rhnActionType values (26, 'solarispkgs.patchInstall', 'Solaris Patch Install', 'Y', 'Y', 'Y');
insert into rhnActionType values (27, 'solarispkgs.patchRemove', 'Solaris Patch Removal', 'Y', 'Y', 'Y');
insert into rhnActionType values (28, 'solarispkgs.patchClusterInstall', 'Solaris Patch Cluster Install', 'Y', 'Y', 'Y');
insert into rhnActionType values (29, 'solarispkgs.patchClusterRemove', 'Solaris Patch Cluster Removal', 'Y', 'Y', 'Y');
insert into rhnActionType values (30, 'script.run', 'Run an arbitrary script', 'N', 'N', 'Y');
insert into rhnActionType values (31, 'solarispkgs.refresh_list', 'Solaris Package List Refresh','Y','Y', 'Y');
insert into rhnActionType values (32, 'rhnsd.configure', 'SUSE Manager Network Daemon Configuration','N','N', 'N');
insert into rhnActionType values (33, 'packages.verify', 'Verify deployed packages','N','N', 'N');
insert into rhnActionType values (34, 'rhn_applet.use_satellite', 'Allows for rhn-applet use with an Spacewalk','N','N', 'N');
insert into rhnActionType values (35, 'kickstart_guest.initiate', 'Initiate an auto installation for a virtual guest.','N','Y', 'N');
insert into rhnActionType values (36, 'virt.shutdown', 'Shuts down a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (37, 'virt.start', 'Starts up a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (38, 'virt.suspend', 'Suspends a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (39, 'virt.resume', 'Resumes a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (40, 'virt.reboot', 'Reboots a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (41, 'virt.destroy', 'Destroys a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (42, 'virt.setMemory', 'Sets the maximum memory usage for a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (43, 'virt.schedulePoller', 'Sets when the poller should run.', 'N', 'N', 'N');
insert into rhnActionType values (44, 'kickstart_host.schedule_virt_host_pkg_install', 'Schedule a package install of host specific functionality.', 'N', 'N', 'N');
insert into rhnActionType values (45, 'kickstart_guest.schedule_virt_guest_pkg_install', 'Schedule a package install of guest specific functionality.', 'N', 'N', 'N');
insert into rhnActionType values (46, 'kickstart_host.add_tools_channel', 'Subscribes a server to the Spacewalk Tools channel associated with its base channel.', 'N', 'N', 'N');
insert into rhnActionType values (47, 'kickstart_guest.add_tools_channel', 'Subscribes a virtualization guest to the Spacewalk Tools channel associated with its base channel.', 'N', 'N', 'N');
insert into rhnActionType values (48, 'virt.setVCPUs', 'Sets the Vcpu usage for a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (49, 'proxy.deactivate', 'Deactivate Proxy', 'N', 'N', 'N');
insert into rhnActionType values (50, 'scap.xccdf_eval', 'OpenSCAP xccdf scanning', 'N', 'Y', 'N');
insert into rhnActionType values (51, 'clientcert.update_client_cert', 'Update Client Certificate', 'N', 'Y', 'Y');
insert into rhnActionType values (500, 'image.deploy', 'Deploy an image to a virtual host.', 'N', 'N', 'N');
insert into rhnActionType values (501, 'distupgrade.upgrade', 'Service Pack Migration', 'N', 'N', 'Y');
insert into rhnActionType values (502, 'packages.setLocks', 'Lock packages', 'N', 'N', 'N');
insert into rhnActionType values (503, 'states.apply', 'Apply states', 'N', 'N', 'Y');
insert into rhnActionType values (504, 'image.build', 'Build an Image Profile', 'N', 'N', 'N');
insert into rhnActionType values (505, 'image.inspect', 'Inspect an Image', 'N', 'N', 'N');
insert into rhnActionType values (506, 'channels.subscribe', 'Subscribe to channels', 'N', 'N', 'N');
insert into rhnActionType values (507, 'virt.delete', 'Deletes a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (508, 'virt.create', 'Creates a virtual domain.', 'N', 'N', 'N');
insert into rhnActionType values (509, 'virt.pool_refresh', 'Refresh a virtual storage pool', 'N', 'N', 'N');
insert into rhnActionType values (510, 'virt.pool_start', 'Starts a virtual storage pool', 'N', 'N', 'N');
insert into rhnActionType values (511, 'virt.pool_stop', 'Stops a virtual storage pool', 'N', 'N', 'N');
insert into rhnActionType values (512, 'virt.pool_delete', 'Deletes a virtual storage pool', 'N', 'N', 'N');
insert into rhnActionType values (513, 'virt.pool_create', 'Creates a virtual storage pool', 'N', 'N', 'N');
insert into rhnActionType values (514, 'virt.volume_delete', 'Deletes a virtual storage volume', 'N', 'N', 'N');
commit;

