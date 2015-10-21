--
-- Copyright (c) 2015 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseVHMConfig
(
    id          number NOT NULL
                CONSTRAINT suse_vhm_config_id_pk PRIMARY KEY,
    virtual_host_manager_id NUMBER NOT NULL
                    CONSTRAINT suse_vhmc_vhms_fk
                    REFERENCES suseVirtualHostManager (id)
                    ON DELETE CASCADE,
    parameter   VARCHAR2(1024) NOT NULL,
    value       VARCHAR2(1024)
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_vhmc_id_para_uq
ON suseVHMConfig (virtual_host_manager_id, parameter)
TABLESPACE [[64k_tbs]];

CREATE SEQUENCE suse_vhm_config_id_seq;

