--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

DROP TABLE IF EXISTS rhnVirtualInstanceEventLog,
                     rhnVirtualInstanceEventType,
                     rhnActionVirtCreate,
                     rhnActionVirtDelete,
                     rhnActionVirtDestroy,
                     rhnActionVirtMigrate,
                     rhnActionVirtNetworkCreate,
                     rhnActionVirtNetworkStateChange,
                     rhnActionVirtPoolCreate,
                     rhnActionVirtPoolDelete,
                     rhnActionVirtPoolRefresh,
                     rhnActionVirtPoolStart,
                     rhnActionVirtPoolStop,
                     rhnActionVirtReboot,
                     rhnActionVirtRefresh,
                     rhnActionVirtResume,
                     rhnActionVirtSchedulePoller,
                     rhnActionVirtSetMemory,
                     rhnActionVirtShutdown,
                     rhnActionVirtStart,
                     rhnActionVirtSuspend,
                     rhnActionVirtVcpu,
                     rhnActionVirtVolDelete CASCADE;

DELETE FROM rhnAction WHERE action_type IN (SELECT id FROM rhnActionType WHERE label LIKE 'virt.%' AND label <> 'virt.refresh_list');

DELETE FROM rhnActionType WHERE label LIKE 'virt.%' AND label <> 'virt.refresh_list';
