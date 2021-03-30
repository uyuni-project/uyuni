--
-- Copyright (c) 2021 SUSE LLC
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


CREATE TABLE suseSCCRegCache
(
    id               NUMERIC
                         CONSTRAINT suse_sccregcache_id_pk
			 PRIMARY KEY,
    server_id        NUMERIC NULL
                         CONSTRAINT suse_sccregcache_sid_fk
                             REFERENCES rhnServer (id)
			 ON DELETE SET NULL,
    scc_reg_required char(1)
                         DEFAULT ('N') not null
                         CONSTRAINT suse_sccregcache_reg_ck
                             CHECK (scc_reg_required IN ('Y', 'N')),
    scc_id                 NUMERIC,
    scc_login              VARCHAR(64),
    scc_passwd             VARCHAR(64),
    scc_regerror_timestamp TIMESTAMPTZ,
    creds_id       NUMERIC NULL
                       CONSTRAINT suse_sccregcache_credsid_fk
                       REFERENCES suseCredentials (id),
---                    ON DELETE DO NOTHING !
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX suse_sccregcache_sid_idx
    ON suseSCCRegCache (server_id);

CREATE SEQUENCE suse_sccregcache_id_seq;
