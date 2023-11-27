--
-- Copyright (c) 2014 SUSE
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
    id             NUMERIC NOT NULL PRIMARY KEY,
    credentials_id NUMERIC
                       CONSTRAINT suse_sccrepo_credsid_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE CASCADE,
    autorefresh    CHAR(1) NOT NULL
                       CONSTRAINT suse_sccrepo_ck
                       CHECK (autorefresh in ('Y', 'N')),
    name           VARCHAR(256),
    distro_target  VARCHAR(256),
    description    VARCHAR(2048),
    url            VARCHAR(2048),
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

