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

CREATE TABLE suseSCCOrderItem
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_sccorder_id_pk
                     PRIMARY KEY,
    scc_id         NUMERIC NOT NULL,
    credentials_id NUMERIC
                       CONSTRAINT suse_sccorder_credsid_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE CASCADE,
    sku            VARCHAR(256),
    start_date     TIMESTAMPTZ,
    end_date       TIMESTAMPTZ,
    quantity       NUMERIC DEFAULT(0),
    subscription_id NUMERIC NOT NULL,
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_sccorder_sccid_uq
    ON suseSCCOrderItem (scc_id);

CREATE INDEX suse_sccorder_subid_idx
    ON suseSCCOrderItem (subscription_id);

CREATE SEQUENCE suse_sccorder_id_seq;
