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

CREATE TABLE susePinnedSubscription
(
    id               NUMERIC NOT NULL
                       CONSTRAINT suse_pinsub_id_pk PRIMARY KEY
                       ,
    system_id        NUMERIC NOT NULL,
    subscription_id  NUMERIC NOT NULL
)

;

CREATE UNIQUE INDEX suse_pinsub_sid_oid_uq
    ON susePinnedSubscription (system_id, subscription_id)
    ;

CREATE SEQUENCE suse_pinsub_id_seq;
