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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseSCCSubscription
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_sccsub_id_pk
                     PRIMARY KEY,
    scc_id         NUMERIC NOT NULL,
    credentials_id NUMERIC
                       CONSTRAINT suse_sccsub_credsid_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE CASCADE,
    name           VARCHAR(256),
    starts_at      TIMESTAMPTZ,
    expires_at     TIMESTAMPTZ,
    status         VARCHAR(20),
    regcode        VARCHAR(256) NOT NULL,
    subtype        VARCHAR(20) NOT NULL,
    system_limit   NUMERIC DEFAULT(0) NOT NULL,
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_sccsub_sccid_uq
    ON suseSCCSubscription (scc_id);

CREATE INDEX suse_sccsub_starts_at_idx
    ON suseSCCSubscription (starts_at);

CREATE INDEX suse_sccsub_expires_at_idx
    ON suseSCCSubscription (expires_at);

CREATE SEQUENCE suse_sccsub_id_seq;
