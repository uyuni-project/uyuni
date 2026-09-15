--
-- Copyright (c) 2025 SUSE LLC
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

-- resets randomized cron_expr of errata-advisory-map-sync-default, to pass idempotency test
-- in order toreset the effects of "select randomize_bunch_schedule('errata-advisory-map-sync-default');"

update rhnTaskoschedule set cron_expr = '0 0 23 ? * *' where job_label = 'errata-advisory-map-sync-default'
