--
-- Copyright (c) 2014--2018 SUSE LLC
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

CREATE TABLE suseSCCRepository
(
    id             NUMBER NOT NULL PRIMARY KEY,
    scc_id         NUMBER NOT NULL,
    autorefresh    CHAR(1) NOT NULL
                       CONSTRAINT suse_sccrepo_ck
                       CHECK (autorefresh in ('Y', 'N')),
    name           VARCHAR2(256) NOT NULL,
    distro_target  VARCHAR2(256) NULL,
    description    VARCHAR2(2048) NOT NULL,
    url            VARCHAR2(2048) NOT NULL,
    signed         CHAR(1) DEFAULT ('N') NOT NULL
                           CONSTRAINT suse_sccrepo_sig_ck
                           CHECK (signed in ('Y', 'N')),
    created        timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL,
    modified       timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_sccrepository_id_seq;

CREATE UNIQUE INDEX suse_sccrepo_sccid_uq
    ON suseSCCRepository (scc_id);

CREATE INDEX suse_sccrepo_url_idx
    ON suseSCCRepository (url);
