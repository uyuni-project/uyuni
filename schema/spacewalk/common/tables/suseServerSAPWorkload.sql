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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseServerSAPWorkload (
    id BIGINT CONSTRAINT suse_sap_workload_id_pk PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    sap_system_id VARCHAR NOT NULL,
    instance_type VARCHAR NOT NULL,
    server_id NUMERIC NOT NULL REFERENCES rhnServer(id) ON DELETE CASCADE
);
