--
-- Copyright (c) 2023 SUSE
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

CREATE TABLE susePaygDimensionComputation
(
    id                   NUMERIC NOT NULL
                            CONSTRAINT susePaygDimensionComputation_pk PRIMARY KEY,
    timestamp            TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    success              BOOLEAN
);

CREATE SEQUENCE susePaygDimensionComputation_id_seq;

CREATE INDEX susePaygDimensionComputation_timestamp_idx
    ON susePaygDimensionComputation (timestamp DESC);
