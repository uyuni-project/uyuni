--
-- Copyright (c) 2024 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseServerCoCoAttestationConfig
(
	id        NUMERIC NOT NULL
	            CONSTRAINT suse_srvcocoatt_cnf_id_pk PRIMARY KEY,
	server_id NUMERIC NOT NULL
                    CONSTRAINT suse_srvcocoatt_cnf_sid_fk
                      REFERENCES rhnServer (id) ON DELETE CASCADE,
	enabled   BOOLEAN NOT NULL DEFAULT FALSE,
	env_type  NUMERIC NULL
);

CREATE UNIQUE INDEX suse_srvcocoatt_cnf_sid_uq
    ON suseServerCoCoAttestationConfig (server_id);

CREATE SEQUENCE suse_srvcocoatt_cnf_id_seq;

