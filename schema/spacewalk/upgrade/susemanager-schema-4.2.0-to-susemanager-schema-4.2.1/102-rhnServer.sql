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

alter table rhnServer add column if not exists
    maintenance_schedule_id NUMERIC
                            CONSTRAINT rhn_server_mtsched_id_fk
                                REFERENCES suseMaintenanceSchedule (id)
                                ON DELETE SET NULL;
